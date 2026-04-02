package com.rocket.cosmic_detox

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.rocket.cosmic_detox.presentation.receiver.MidnightResetReceiver
import dagger.hilt.android.HiltAndroidApp
import java.util.Calendar
import javax.inject.Inject

@HiltAndroidApp
class CosmicDetoxApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleExactAlarm(this)
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleExactAlarm(context: Context) {
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, MidnightResetReceiver::class.java)
            .setAction(MidnightResetReceiver.ACTION_MIDNIGHT_RESET)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 자정 시간을 설정
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0) // 자정(0시) 설정
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // 현재 시간이 자정 이후라면 다음 날 자정으로 설정
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // 정확한 자정에 알람 설정
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        /*
                // 테스트 용 5분 단위로 초기화 됨
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    add(Calendar.MINUTE, 5) // 5분 후 알람 설정
                }

                // 정확한 5분 후 알람 설정
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )

         */

        Log.d("TimerService", "5분 후에 알람이 설정ㅇ")
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleDebugResetAlarmInOneMinute(context: Context) {
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MidnightResetReceiver::class.java)
            .setAction(MidnightResetReceiver.ACTION_MIDNIGHT_RESET)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + 20_000L
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )

        Log.d("TimerService", "디버그: 1분 뒤 자정 리셋 알람 예약")
    }
}
