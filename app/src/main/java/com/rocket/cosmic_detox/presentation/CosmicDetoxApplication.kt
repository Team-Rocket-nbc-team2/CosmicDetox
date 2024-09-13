package com.rocket.cosmic_detox.presentation

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.rocket.cosmic_detox.presentation.receiver.MidnightResetReceiver
import dagger.hilt.android.HiltAndroidApp
import java.util.Calendar

@HiltAndroidApp
class CosmicDetoxApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        scheduleMidnightAlarm(this)
    }

    fun scheduleMidnightAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, MidnightResetReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
/*
        // 자정 시간을 설정
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // 현재 시간이 자정 이후라면 다음 날로 설정
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
            // 테스트 목적으로 2분 후에 알람을 울리게 설정 (테스트가 끝나면 이 부분을 제거)
        }
*/
        // 현재 시간을 기준으로 1분 후에 알람을 설정 (테스트용) , 위에 꺼랑 바궈치기하면 됨
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.MINUTE, 1)  // 5분 후로 알람 설정
        }
        // 매일 자정에 알람이 반복되도록 설정
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        Log.d("CosmicDetoxApplication", "자정 알람이 설정되었습니다: ${calendar.time}")
    }
}
