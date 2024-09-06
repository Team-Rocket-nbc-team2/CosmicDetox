package com.rocket.cosmic_detox.presentation.viewmodel

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.domain.usecase.timer.GetAllowedAppUseCase
import com.rocket.cosmic_detox.domain.usecase.timer.UpdateLimitedTimeAppUseCase
import com.rocket.cosmic_detox.presentation.uistate.GetListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllowedAppViewModel @Inject constructor(
    private val getAllowedAppUseCase: GetAllowedAppUseCase,
    private val updateLimitedTimeAppUseCase: UpdateLimitedTimeAppUseCase
): ViewModel() {
    private val _allowedAppList = MutableStateFlow<GetListUiState<List<AllowedApp>>>(GetListUiState.Init)
    val allowedAppList: StateFlow<GetListUiState<List<AllowedApp>>> = _allowedAppList

    private val _countDownRemainTime = MutableStateFlow<Int?>(null)
    val countDownRemainTime: StateFlow<Int?> = _countDownRemainTime

    private val _selectedAllowedAppPackage = MutableStateFlow<String?>(null)
    val selectedAllowedAppPackage: StateFlow<String?> = _selectedAllowedAppPackage

    private val _running = MutableStateFlow(true)

    fun getAllAllowedApps() {
        viewModelScope.launch {
            _allowedAppList.value = GetListUiState.Loading

            getAllowedAppUseCase(
                callback = { allowedApps ->
                    _allowedAppList.value = if (allowedApps.isEmpty()) GetListUiState.Empty
                    else GetListUiState.Success(allowedApps)
                },
                failCallback = { exception ->
                    _allowedAppList.value = GetListUiState.Failure(exception)
                }
            )
        }
    }

    fun setSelectedAllowedAppPackage(packageId: String) {
        _selectedAllowedAppPackage.value = packageId
    }

    fun updateRemainTime(remainTime: Int) {
        _countDownRemainTime.value = remainTime
    }

    fun updateLimitedTimeAllowApp(
        packageId: String,
        remainTime: Int,
        failCallback: (Throwable?) -> Unit
    ) {
        viewModelScope.launch {
            updateLimitedTimeAppUseCase(
                packageId = packageId,
                remainTime = remainTime,
                failCallback = failCallback
            )
        }
    }

    fun initObserveAppOpenRunnable(
        context: Context,
        currentOpenAppPackage: String,
        showOverlay: () -> Unit
    ): Runnable {
        return Runnable {
            while (_running.value) {
                val currentPackageName = getCurrentOpenedAppPackageName(context)

                if (currentPackageName != context.packageName && currentPackageName != currentOpenAppPackage &&
                    currentPackageName != "com.sec.android.app.launcher" && currentPackageName != "com.android.systemui") {

                    Handler(Looper.getMainLooper()).postDelayed({
                        showOverlay()
                    }, 0)
                }

                Thread.sleep(1500)
            }
        }
    }

    fun startObserveAppOpenRunnable() {
        _running.value = true
    }

    fun stopObserveAppOpenRunnable() {
        _running.value = false
    }

    private fun getCurrentOpenedAppPackageName(context: Context): String {
        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - 1500
        val usageStatsManager: UsageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val usageEvents = usageStatsManager.queryEvents(startTime, currentTime)
        var lastEvent: String = context.packageName

        while (usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            usageEvents.getNextEvent(event)

            if (isForeGround(event)) {
                lastEvent = event.packageName
            }
        }

        return lastEvent
    }

    private fun isForeGround(event: UsageEvents.Event): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            event.eventType == UsageEvents.Event.ACTIVITY_RESUMED
        } else {
            event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
        }
    }
}