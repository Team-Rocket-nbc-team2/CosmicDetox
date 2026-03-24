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
    private var sessionElapsedTime: Long = 0L
    private var lastTickRealtime: Long = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var isTimerRunning = false

    private val timerRunnable = object : Runnable {
        override fun run() {
            val currentTime = SystemClock.elapsedRealtime()
            val timeElapsed = (currentTime - lastTickRealtime) / 1000
            time += timeElapsed
            sessionElapsedTime += timeElapsed
            lastTickRealtime = currentTime

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
        if (intent == null) {
            // START_STICKY 재시작(null intent) 시 0초로 시작되는 문제 방지
            stopSelf()
            return START_NOT_STICKY
        }

        when (intent?.action) {
            ACTION_RESET_TIMER -> {
                resetTimer()
            }
            else -> {
                if (isTimerRunning) return START_NOT_STICKY
                val dailyTime = intent.getLongExtra("dailyTime", 0L)
                initialDailyTime = dailyTime // 타이머 시작 시 dailyTime 저장
                time = dailyTime // 전달받은 dailyTime으로 초기화
                sessionElapsedTime = 0L
                startTimer() // 타이머 시작
            }
        }
        return START_NOT_STICKY
    }

    private fun startTimer() {
        if (!isTimerRunning) {
            lastTickRealtime = SystemClock.elapsedRealtime()
            handler.post(timerRunnable) // 1초마다 타이머 업데이트
            isTimerRunning = true
        }
    }

    private fun stopTimer() {
        handler.removeCallbacks(timerRunnable)
        isTimerRunning = false
        currentDailyTime = time // 타이머 종료 시의 dailyTime 저장

        // 세션 경과시간이 꼬이더라도 누적시간이 감소하지 않도록 방어
        val elapsedSeconds = sessionElapsedTime.coerceAtLeast((currentDailyTime - initialDailyTime).coerceAtLeast(0L))
        saveTimeToFirebase(elapsedSeconds)
    }

    private fun saveTimeToFirebase(elapsedSeconds: Long) {
        // 기존의 totalTime을 가져와서 dailyTime 차이를 더함
        getTotalTimeUseCase({ currentTotalTime ->
            val updatedTotalTime = currentTotalTime + elapsedSeconds

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
        val wasRunning = isTimerRunning

        // 타이머가 동작 중일 때는 먼저 멈춤
        if (wasRunning) {
            handler.removeCallbacks(timerRunnable)
            isTimerRunning = false
            currentDailyTime = time // 타이머가 멈춘 시점의 시간을 저장
        } else {
            currentDailyTime = 0L
        }

        // 자정 시점에는 "이번 세션에서 아직 totalTime에 반영되지 않은 시간"만 누적
        val elapsedSecondsToSave = if (wasRunning) {
            sessionElapsedTime.coerceAtLeast((currentDailyTime - initialDailyTime).coerceAtLeast(0L))
        } else {
            0L
        }

        // 기존의 totalTime과 dailyTime을 가져옴
        getTotalTimeUseCase({ currentTotalTime ->
            val updatedTotalTime = currentTotalTime + elapsedSecondsToSave

            // totalTime 업데이트 후 dailyTime 초기화
            updateTotalTimeUseCase(updatedTotalTime, {
                updateDailyTimeUseCase(0L, {
                    Log.d("TimerService", "dailyTime이 0으로 초기화되었습니다.")
                    time = 0L // 로컬 타이머도 0으로 초기화
                    initialDailyTime = 0L // resetTimer 후 타이머를 재시작할 때 다시 설정되도록 초기화
                    sessionElapsedTime = 0L
                    sendTimeUpdate() // UI 업데이트

                    if (wasRunning) {
                        // 타이머 실행 중 자정이 지난 경우: 0부터 즉시 재시작
                        startTimer()
                    } else {
                        stopSelf()
                    }
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
