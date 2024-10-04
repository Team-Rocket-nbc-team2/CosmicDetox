package com.rocket.cosmic_detox.presentation.service

import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
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
    private var initialDailyTime: Long = 0L
    private var currentDailyTime: Long = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var isTimerRunning = false

    private val timerRunnable = object : Runnable {
        private var lastTime: Long = SystemClock.elapsedRealtime()

        override fun run() {
            val currentTime = SystemClock.elapsedRealtime()
            val timeElapsed = (currentTime - lastTime) / 1000
            time += timeElapsed
            lastTime = currentTime

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
                initialDailyTime = dailyTime // 타이머 시작 시 dailyTime 저장
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
        currentDailyTime = time // 타이머 종료 시의 dailyTime 저장

        // dailyTime 차이 계산 후 totalTime에 반영
        val dailyTimeDifference = currentDailyTime - initialDailyTime
        saveTimeToFirebase(dailyTimeDifference)
    }

    private fun saveTimeToFirebase(dailyTimeDifference: Long) {
        // 기존의 totalTime을 가져와서 dailyTime 차이를 더함
        getTotalTimeUseCase({ currentTotalTime ->
            val updatedTotalTime = currentTotalTime + dailyTimeDifference

            // totalTime 업데이트 및 dailyTime 저장
            updateTotalTimeUseCase(updatedTotalTime, {
                // dailyTime도 Firebase에 업데이트
                updateDailyTimeUseCase(currentDailyTime, {
                    updateRankingTotalTime(updatedTotalTime)
                    Log.d("TimerService", "Total time 및 Daily time 업데이트 완료")
                }, { exception ->
                    Log.e("TimerService", "Daily time 업데이트 실패: $exception")
                })
            }, { exception ->
                Log.e("TimerService", "Total time 업데이트 실패: $exception")
            })
        }, { exception ->
            Log.e("TimerService", "Total time 가져오기 실패: $exception")
        })
    }

    // Reset Timer 추가
    private fun resetTimer() {
        // 타이머가 동작 중일 때는 먼저 멈춤
        if (isTimerRunning) {
            handler.removeCallbacks(timerRunnable)
            isTimerRunning = false
            currentDailyTime = time // 타이머가 멈춘 시점의 시간을 저장
        }

        // 기존의 totalTime과 dailyTime을 가져옴
        getTotalTimeUseCase({ currentTotalTime ->
            val updatedTotalTime = currentTotalTime + currentDailyTime // 기존 totalTime에 currentDailyTime을 더함

            // totalTime 업데이트 후 dailyTime 초기화
            updateTotalTimeUseCase(updatedTotalTime, {
                updateDailyTimeUseCase(0L, {
                    Log.d("TimerService", "dailyTime이 0으로 초기화되었습니다.")
                    time = 0L // 로컬 타이머도 0으로 초기화
                    initialDailyTime = 0L // resetTimer 후 타이머를 재시작할 때 다시 설정되도록 초기화
                    sendTimeUpdate() // UI 업데이트

                    // 타이머를 다시 시작
                    startTimer()
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
