package com.rocket.cosmic_detox.presentation.receiver


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rocket.cosmic_detox.presentation.CosmicDetoxApplication
// 기기 재부팅 시 타이머 초기화 알림 재설정
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {

            val app = context.applicationContext as CosmicDetoxApplication
            app.scheduleExactAlarm(context)
        }
    }
}
