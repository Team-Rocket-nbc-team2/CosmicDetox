package com.rocket.cosmic_detox.presentation.view

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.graphics.PixelFormat
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.presentation.view.activity.MainActivity

class AppMonitorService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var monitorRunnable: Runnable? = null
    private lateinit var usageStatsManager: UsageStatsManager
    private var lastAppPackage: String? = null
    private var allowedAppList: List<String> = emptyList() // 허용된 앱 리스트를 저장할 변수
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null

    override fun onCreate() {
        super.onCreate()
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        Log.d("AppMonitorService", "서비스 create!!!!!!!!!!")
        startMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Intent로부터 허용된 앱 리스트를 가져옴
        allowedAppList = intent?.getStringArrayListExtra("ALLOWED_APP_LIST") ?: emptyList()
        startMonitoring()
        Log.d("AppMonitorService", "!!!!!!!Service allowedAppList: $allowedAppList")
        return START_STICKY
    }

    private fun startMonitoring() {
        monitorRunnable = object : Runnable {
            override fun run() {
                checkForegroundApp()
                handler.postDelayed(this, 1000) // 1초마다 확인
            }
        }
        monitorRunnable?.let { handler.post(it) }
    }

    fun checkForegroundApp() {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 // 최근 1초 동안의 앱 사용 정보를 가져옴

        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        var lastEvent: UsageEvents.Event? = null

        while (usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastEvent = event
            }
        }

        lastEvent?.let {
            val currentApp = it.packageName
            Log.d("AppMonitorService", "currentApp: $currentApp")

            // 현재 앱이 허용된 앱 리스트에 없는 경우 바로 오버레이 띄우기, 우리 앱이 아닌 경우에만
            if (!allowedAppList.contains(currentApp) && currentApp != packageName) {
                lastAppPackage = currentApp
                //showOverlay()
                Log.d("AppMonitorService", "허용되지 않은 앱입니다.")
            } else {
                Log.d("AppMonitorService", "허용된 앱입니다.")
                lastAppPackage = currentApp
                removeOverlay() // 허용된 앱으로 돌아오면 오버레이 제거
            }
        } ?: run {
            // 포그라운드에서 활동하는 앱이 없는 경우 (홈, 앱 스택 등)
            // showOverlay()
        }
    }

    private fun showOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Log.d("AppMonitorService", "오버레이 권한이 없습니다.")
            return
        }

        // 이미 오버레이가 띄워져 있다면 다시 띄우지 않음
        if (overlayView != null) {
            return
        }

        val overlayParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        // 오버레이 레이아웃을 인플레이트함
        overlayView = LayoutInflater.from(this).inflate(R.layout.activity_dialog, null)

        overlayView?.let { view ->
            view.findViewById<Button>(R.id.btn_back).setOnClickListener {
                removeOverlay()
                returnToApp()
            }

            // WindowManager를 사용해 오버레이 뷰를 화면에 추가
            windowManager.addView(view, overlayParams)
            Log.d("AppMonitorService", "오버레이를 띄웁니다.")
        }
    }

    private fun removeOverlay() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
            Log.d("AppMonitorService", "오버레이를 제거합니다.")
        }
    }

    private fun returnToApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        monitorRunnable?.let { handler.removeCallbacks(it) }
        removeOverlay() // 서비스가 종료될 때 오버레이 제거
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}