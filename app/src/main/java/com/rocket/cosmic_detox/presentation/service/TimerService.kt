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

    private val timerRunnable = object : Runnable {
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

    // 서비스가 시작되면 실행
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
            handler.post(timerRunnable) // 1초마다 타이머 업데이트
            isTimerRunning = true
        }
    }

    private fun stopTimer() {
        handler.removeCallbacks(timerRunnable)
        isTimerRunning = false
        saveDailyTimeToFirebase() // 오직 dailyTime만 저장
    }

    private fun resetTimer() {
        getTotalTimeUseCase({ currentTotalTime ->
            val updatedTotalTime = currentTotalTime + time // 기존 totalTime에 dailyTime 추가

            // totalTime을 업데이트하고 dailyTime을 0으로 초기화
            updateTotalTimeUseCase(updatedTotalTime, {
                updateDailyTimeUseCase(0L, {
                    Log.d("TimerService", "dailyTime이 0으로 초기화")
                    time = 0L // 로컬 타이머도 0으로 초기화
                    sendTimeUpdate() // UI 업데이트

                    // 랭킹 업데이트
                    updateRankingTotalTime(updatedTotalTime)

                }, { exception ->
                    Log.e("TimerService", "dailyTime 초기화 실패: $exception")
                })
            }, { exception ->
                Log.e("TimerService", "totalTime 업데이트 실패: $exception")
            })
        }, { exception ->
            Log.e("TimerService", "totalTime 가져오기 실패: $exception")
        })
    }

    // Firebase에 dailyTime만 저장 (타이머 멈출 때)
    private fun saveDailyTimeToFirebase() {
        updateDailyTimeUseCase(time, {
            Log.d("TimerService", "dailyTime이 성공적으로 저장되었습니다.")
        }, { exception ->
            Log.e("TimerService", "dailyTime 저장 실패: $exception")
        })
    }

    // UI 업데이트를 위한 타이머 시간 브로드캐스트
    private fun sendTimeUpdate() {
        val intent = Intent("com.rocket.cosmic_detox.TIMER_UPDATE")
        intent.putExtra("time", time)
        sendBroadcast(intent) // Broadcast를 통해 UI에 업데이트
    }

    // 랭킹을 업데이트하는 함수
    private fun updateRankingTotalTime(updatedTotalTime: Long) {
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            updateRankingTotalTimeUseCase(updatedTotalTime, uid, {}, { exception ->
                Log.e("TimerService", "랭킹 업데이트 오류: $exception")
            })
        }
    }

    // 서비스가 종료될 때 타이머를 중지
    override fun onDestroy() {
        super.onDestroy()
        stopTimer() // 서비스가 종료될 때 타이머 중지 및 시간 저장
    }
}
