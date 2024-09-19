package com.rocket.cosmic_detox.presentation.receiver

// MidnightResetReceiver.kt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.rocket.cosmic_detox.presentation.CosmicDetoxApplication
import com.rocket.cosmic_detox.presentation.service.TimerService

class MidnightResetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("MidnightResetReceiver", "MidnightReset 호출 성공 타이머를 초기화.")
        context?.let {
            val resetIntent = Intent(context, TimerService::class.java).apply {
                action = TimerService.ACTION_RESET_TIMER
            }
            context.startService(resetIntent)  // TimerService에 초기화 요청

/*
            // 임시 주석 , 자정 알림하려면 풀기
            // 다음 자정을 위한 알람 재설정
            val app = context.applicationContext as CosmicDetoxApplication
            app.scheduleExactAlarm(context)  // 다음 자정을 위해 다시 알람 설정

 */
        }
    }
}
