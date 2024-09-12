package com.rocket.cosmic_detox.domain

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.rocket.cosmic_detox.presentation.view.activity.MainActivity

class AlarmReceiver : BroadcastReceiver() {
    private lateinit var notificationManager: NotificationManager

    private val channelId = "one-notification_channel"
    private val channelName = "Cosmic Detox Alarm"

    override fun onReceive(context: Context, intent: Intent) {
        notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel()
        deliverNotification(context)
    }

    // Notification 을 띄우기 위한 Channel 등록
    fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(
                channelId, // 채널의 아이디
                channelName, // 채널의 이름
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true) // 불빛
            notificationChannel.lightColor = Color.RED // 색상
            notificationChannel.enableVibration(true) // 진동 여부
            notificationChannel.description = "채널의 상세정보입니다." // 채널 정보
            notificationManager.createNotificationChannel(
                notificationChannel)
        }
    }

    // Notification 등록
    private fun deliverNotification(context: Context){
        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0, // requestCode
            contentIntent, // 알림 클릭 시 이동할 인텐트
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
//            .setSmallIcon(R.drawable.ic_alert_on) // 아이콘
            .setContentTitle("앱 사용 시간이 5분 밖에 남지 않았어요!") // 제목
            .setContentText("곧 앱 사용 시간이 종료됩니다. 이제 다시 우주 여행 할 준비를 하세요.") // 내용
//            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        notificationManager.notify(0, builder.build())
    }
}