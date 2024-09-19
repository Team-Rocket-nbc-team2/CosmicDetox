package com.rocket.cosmic_detox.presentation.service

import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LifecycleService
import com.google.firebase.auth.FirebaseAuth
import com.rocket.cosmic_detox.domain.usecase.ranking.UpdateRankingTotalTimeUseCase
import com.rocket.cosmic_detox.domain.usecase.timer.GetTotalTimeUseCase
import com.rocket.cosmic_detox.domain.usecase.timer.UpdateDailyTimeUseCase
import com.rocket.cosmic_detox.domain.usecase.timer.UpdateTotalTimeUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : LifecycleService() {

    companion object {
        const val ACTION_RESET_TIMER = "com.rocket.cosmic_detox.RESET_TIMER"
    }

    @Inject
    lateinit var updateDailyTimeUseCase: UpdateDailyTimeUseCase

    @Inject
    lateinit var updateTotalTimeUseCase: UpdateTotalTimeUseCase

    @Inject
    lateinit var updateRankingTotalTimeUseCase: UpdateRankingTotalTimeUseCase

    @Inject
    lateinit var getTotalTimeUseCase: GetTotalTimeUseCase

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private var time: Long = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var isTimerRunning = false

    private val runnable = object : Runnable {
        override fun run() {
            time++
            sendTimeUpdate() // 1초마다 UI에 타이머 업데이트
            handler.postDelayed(this, 1000) // 1초마다 실행
        }
    }

    // Binder를 통한 서비스와의 통신을 위한 LocalBinder 클래스
    inner class LocalBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_RESET_TIMER -> {
                resetTimer()
            }
            else -> {
                val dailyTime = intent?.getLongExtra("dailyTime", 0L) ?: 0L
                time = dailyTime // 전달받은 dailyTime으로 초기화
                startTimer() // 타이머 시작
            }
        }
        return START_STICKY
    }

    private fun startTimer() {
        if (!isTimerRunning) {
            handler.post(runnable)
            isTimerRunning = true
        }
    }

    private fun stopTimer() {
        handler.removeCallbacks(runnable)
        isTimerRunning = false
        saveTimeToFirebase() // 타이머가 멈출 때 시간 저장
    }

    private fun resetTimer() {
        stopTimer() // 기존 타이머 중지 및 시간 저장
        time = 0L // 시간 초기화
        updateDailyTimeUseCase(0L, {
            Log.d("TimerService", "dailyTime이 0으로 초기화됨.")
            // 필요 시 UI 업데이트를 위한 브로드캐스트 전송
            sendTimeUpdate()
            startTimer() // 타이머 재시작
        }, { exception ->
            Log.e("TimerService", "dailyTime 초기화 실패: $exception")
        })
    }

    private fun sendTimeUpdate() {
        val intent = Intent("com.rocket.cosmic_detox.TIMER_UPDATE")
        intent.putExtra("time", time)
        sendBroadcast(intent) // Broadcast를 통해 UI에 업데이트
    }

    // Firebase에 저장
    private fun saveTimeToFirebase() {
        getTotalTimeUseCase({ currentTotalTime ->
            val updatedTotalTime = currentTotalTime + (time) // 누적된 시간 반영

            updateDailyTimeUseCase(time, {
                updateTotalTimeUseCase(updatedTotalTime, {
                    updateRankingTotalTime(updatedTotalTime)
                }, { exception -> Log.e("TimerService", "total time오류: $exception") })
            }, { exception -> Log.e("TimerService", "dailyTime오류: $exception") })
        }, { exception -> Log.e("TimerService", "시간 오류 $exception") })
    }

    // 랭킹 업데이트
    private fun updateRankingTotalTime(updatedTotalTime: Long) {
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            updateRankingTotalTimeUseCase(updatedTotalTime, uid, {}, { exception ->
                Log.e("TimerService", "랭킹 업데이트 오류: $exception")
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer() // 서비스가 종료될 때 타이머 중지 및 시간 저장
    }
}
