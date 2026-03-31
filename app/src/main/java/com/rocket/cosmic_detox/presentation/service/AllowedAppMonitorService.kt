package com.rocket.cosmic_detox.presentation.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.core.app.NotificationCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.data.datasource.local.allowedapp.AllowedAppLocalDataSource
import com.rocket.cosmic_detox.data.datasource.local.model.AllowedAppSessionLocal
import com.rocket.cosmic_detox.data.worker.SyncSessionWorker
import com.rocket.cosmic_detox.domain.usecase.timer.GetAllowedAppUseCase
import com.rocket.cosmic_detox.domain.usecase.timer.UpdateLimitedTimeAppUseCase
import com.rocket.cosmic_detox.presentation.view.activity.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class AllowedAppMonitorService : Service() {

    companion object {
        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive = _isServiceActive.asStateFlow()

        private val _currentPackageIdState = MutableStateFlow<String?>(null)
        val currentPackageIdState = _currentPackageIdState.asStateFlow()

        private val _remainTime = MutableStateFlow(0L)
        val remainTime = _remainTime.asStateFlow()

        private val _elapsedOutsideTime = MutableStateFlow(0L)
        val elapsedOutsideTime = _elapsedOutsideTime.asStateFlow()

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_UI_READY = "ACTION_UI_READY"
        const val EXTRA_PACKAGE_ID = "EXTRA_PACKAGE_ID"
        const val EXTRA_REMAIN_TIME = "EXTRA_REMAIN_TIME"
        const val EXTRA_APP_NAME = "EXTRA_APP_NAME"
        const val EXTRA_OPEN_ALLOWED_SHEET = "EXTRA_OPEN_ALLOWED_SHEET"

        private const val CHANNEL_ID = "allowed_app_monitor"
        private const val NOTIFICATION_ID = 2
        private const val SAVE_INTERVAL_MS = 60_000L

        fun createStartIntent(
            context: Context,
            packageId: String,
            remainTime: Long,
            appName: String
        ): Intent = Intent(context, AllowedAppMonitorService::class.java).apply {
            action = ACTION_START
            putExtra(EXTRA_PACKAGE_ID, packageId)
            putExtra(EXTRA_REMAIN_TIME, remainTime)
            putExtra(EXTRA_APP_NAME, appName)
        }

        fun createStopIntent(context: Context): Intent =
            Intent(context, AllowedAppMonitorService::class.java).apply {
                action = ACTION_STOP
            }

        fun createUiReadyIntent(context: Context): Intent =
            Intent(context, AllowedAppMonitorService::class.java).apply {
                action = ACTION_UI_READY
            }
    }

    @Inject lateinit var getAllowedAppUseCase: GetAllowedAppUseCase
    @Inject lateinit var updateLimitedTimeAppUseCase: UpdateLimitedTimeAppUseCase
    @Inject lateinit var allowedAppLocalDataSource: AllowedAppLocalDataSource

    private val notificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }
    private val windowManager by lazy {
        getSystemService(WINDOW_SERVICE) as WindowManager
    }
    private val usageStatsManager by lazy {
        getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
    }
    private val homeLauncherPackages by lazy { resolveHomeLauncherPackages() }

    private var serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var currentPackageId: String? = null
    private var currentAppName: String = ""
    private var allowedPackages = emptySet<String>()
    private var overlayView: View? = null
    private var lastSaveTime = 0L
    private var lastForegroundPackage: String = ""
    private var lastNonSystemPackage: String = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val packageId = intent.getStringExtra(EXTRA_PACKAGE_ID) ?: return START_STICKY
                val remainTime = intent.getLongExtra(EXTRA_REMAIN_TIME, 0L)
                val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: ""
                start(packageId, remainTime, appName)
            }
            ACTION_STOP -> stop()
            ACTION_UI_READY -> removeOverlayOnMain()
        }
        return START_STICKY
    }

    private fun start(packageId: String, remainTime: Long, appName: String) {
        // 이미 다른 허용앱 감시 중이면 현재 상태 저장 후 scope 재생성
        if (_isServiceActive.value) {
            currentPackageId?.let { previousPackageId ->
                runBlocking {
                    saveCurrentSessionNow(previousPackageId, _remainTime.value)
                }
                syncToFirestore(previousPackageId, _remainTime.value)
            }
            serviceScope.cancel()
            serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        }

        _isServiceActive.value = true
        currentPackageId = packageId
        _currentPackageIdState.value = packageId
        currentAppName = appName
        lastForegroundPackage = packageId
        lastNonSystemPackage = packageId
        _remainTime.value = remainTime
        _elapsedOutsideTime.value = 0L
        lastSaveTime = System.currentTimeMillis()

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(appName, remainTime, 0L))

        loadAllowedApps()
        startMonitoring()
        schedulePeriodicSync()
    }

    private fun startMonitoring() {
        serviceScope.launch {
            while (_isServiceActive.value) {
                val foregroundApp = getCurrentForegroundApp()
                val shouldCountDown = shouldCountDown(foregroundApp)
                val isSystemOrOwn = isSystemOrOwnApp(foregroundApp)

                when {
                    shouldCountDown -> {
                        if (_elapsedOutsideTime.value > 0L) {
                            _elapsedOutsideTime.value = 0L
                            saveCurrentSession()
                            removeOverlayOnMain()
                        }

                        if (_remainTime.value > 0L) {
                            _remainTime.value--
                        } else {
                            onTimerFinished()
                            break
                        }
                    }
                    isSystemOrOwn -> {
                        // 시스템 UI or 우리 앱 → 무시
                    }
                    else -> {
                        if (_elapsedOutsideTime.value == 0L) {
                            Log.d("AllowedAppMonitor", "outside detected -> overlay request, fg=$foregroundApp")
                            saveCurrentSession()
                            showOverlayOnMain()
                        }
                        _elapsedOutsideTime.value++
                    }
                }

                val now = System.currentTimeMillis()
                if (now - lastSaveTime >= SAVE_INTERVAL_MS) {
                    saveCurrentSession()
                    lastSaveTime = now
                }

                updateNotification()
                delay(1000L)
            }
        }
    }

    private fun loadAllowedApps() {
        serviceScope.launch {
            getAllowedAppUseCase(
                callback = { apps ->
                    allowedPackages = apps.map { it.packageId }.toSet()
                },
                failCallback = {
                    Log.e("MonitorService", "허용앱 로드 실패")
                }
            )
        }
    }

    private fun saveCurrentSession() {
        serviceScope.launch {
            currentPackageId?.let { packageId ->
                saveCurrentSessionNow(packageId, _remainTime.value)
            }
        }
    }

    private suspend fun saveCurrentSessionNow(packageId: String, remainTime: Long) {
        withContext(Dispatchers.IO) {
            allowedAppLocalDataSource.upsertSession(
                AllowedAppSessionLocal(
                    packageId = packageId,
                    remainTime = remainTime,
                    lastUpdated = System.currentTimeMillis(),
                    isSynced = false
                )
            )
        }
    }

    private fun syncToFirestore(packageId: String, remainTime: Long) {
        updateLimitedTimeAppUseCase(
            packageId = packageId,
            remainTime = remainTime.toInt(),
            callback = {},
            failCallback = {
                Log.e("MonitorService", "Firestore sync 실패 → WorkManager가 재시도 예정")
            }
        )
    }

    private fun stop() {
        currentPackageId?.let { packageId ->
            runBlocking {
                saveCurrentSessionNow(packageId, _remainTime.value)
            }
            syncToFirestore(packageId, _remainTime.value)
        }

        removeOverlayOnMain()
        serviceScope.cancel()
        serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        _isServiceActive.value = false
        currentPackageId = null
        _currentPackageIdState.value = null
        _remainTime.value = 0L
        _elapsedOutsideTime.value = 0L
        lastForegroundPackage = ""
        lastNonSystemPackage = ""

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun onTimerFinished() {
        currentPackageId?.let { syncToFirestore(it, 0L) }
        removeOverlayOnMain()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        stop()
    }

    @SuppressLint("InflateParams")
    private fun showOverlayOnMain() {
        Handler(Looper.getMainLooper()).post {
            if (overlayView != null) return@post
            if (!Settings.canDrawOverlays(this)) return@post

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )

            val contextThemeWrapper = ContextThemeWrapper(this, R.style.Base_Theme_CosmicDetox)
            overlayView = LayoutInflater.from(contextThemeWrapper).inflate(R.layout.activity_dialog, null)

            overlayView?.findViewById<Button>(R.id.btn_back)?.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(EXTRA_OPEN_ALLOWED_SHEET, true)
                }
                startActivity(intent)
            }
            windowManager.addView(overlayView, params)
        }
    }

    private fun removeOverlayOnMain() {
        Handler(Looper.getMainLooper()).post {
            overlayView?.let {
                if (it.isAttachedToWindow) windowManager.removeView(it)
                overlayView = null
            }
        }
    }

    private fun buildNotification(
        appName: String,
        remainTime: Long,
        elapsedOutside: Long
    ): Notification {
        val contentText = if (elapsedOutside > 0L) {
            "$appName | 남은: ${formatTime(remainTime)}  이탈: ${formatTime(elapsedOutside)}"
        } else {
            "$appName | 남은시간: ${formatTime(remainTime)}"
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.label_focus_mode))
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification() {
        notificationManager.notify(
            NOTIFICATION_ID,
            buildNotification(currentAppName, _remainTime.value, _elapsedOutsideTime.value)
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "허용 앱 모니터",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun schedulePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<SyncSessionWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                2000L,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sync_allowed_app_session",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun isSystemOrOwnApp(packageName: String): Boolean {
        return packageName == this.packageName ||
                packageName == "com.android.systemui" ||
                packageName == "com.samsung.android.incallui" ||
                packageName == "com.android.incallui" ||
                isEssentialSystemScreen(packageName) ||
                isHomeLauncherPackage(packageName)
    }

    private fun isEssentialSystemScreen(packageName: String): Boolean {
        return packageName == "com.android.settings" ||
                packageName == "com.samsung.android.app.settings"
    }

    private fun isHomeLauncherPackage(packageName: String): Boolean {
        // 일부 기기에서 설정 앱이 HOME 후보로 섞여 들어오는 케이스 제외
        if (isEssentialSystemScreen(packageName)) return false
        return packageName in homeLauncherPackages
    }

    private fun resolveHomeLauncherPackages(): Set<String> {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }

        val launchers = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                homeIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        }

        return launchers
            .mapNotNull { it.activityInfo?.packageName }
            .filterNot { isEssentialSystemScreen(it) }
            .toSet()
    }

    private fun isOwnApp(packageName: String): Boolean = packageName == this.packageName

    private fun shouldCountDown(foregroundApp: String): Boolean {
        val targetPackage = currentPackageId ?: return false

        if (!isSystemOrOwnApp(foregroundApp)) {
            lastNonSystemPackage = foregroundApp
        }

        return foregroundApp == targetPackage ||
                (isOwnApp(foregroundApp) && lastNonSystemPackage == targetPackage)
    }

    private fun getCurrentForegroundApp(): String {
        val currentTime = System.currentTimeMillis()
        val usageEvents = usageStatsManager.queryEvents(currentTime - 5000, currentTime)
        var hasForegroundEvent = false

        while (usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            usageEvents.getNextEvent(event)
            if (isForeground(event)) {
                hasForegroundEvent = true
                lastForegroundPackage = event.packageName
            }
        }

        if (!hasForegroundEvent) {
            val recentPackage = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                currentTime - 10_000,
                currentTime
            )
                .maxByOrNull { it.lastTimeUsed }
                ?.packageName

            if (!recentPackage.isNullOrBlank()) {
                lastForegroundPackage = recentPackage
            }
        }

        return lastForegroundPackage.ifBlank { packageName }
    }

    private fun isForeground(event: UsageEvents.Event): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            event.eventType == UsageEvents.Event.ACTIVITY_RESUMED
        } else {
            event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (_isServiceActive.value) {
            currentPackageId?.let { packageId ->
                runBlocking {
                    saveCurrentSessionNow(packageId, _remainTime.value)
                }
                syncToFirestore(packageId, _remainTime.value)
            }
        }
        removeOverlayOnMain()
        serviceScope.cancel()
        _isServiceActive.value = false
    }
}
