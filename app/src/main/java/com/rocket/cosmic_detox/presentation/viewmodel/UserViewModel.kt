package com.rocket.cosmic_detox.presentation.view.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.rocket.cosmic_detox.data.model.User
import com.rocket.cosmic_detox.domain.usecase.GetUserDataUseCase
import com.rocket.cosmic_detox.domain.usecase.ranking.UpdateRankingTotalTimeUseCase
import com.rocket.cosmic_detox.domain.usecase.timer.GetDailyTimeUseCase
import com.rocket.cosmic_detox.domain.usecase.timer.GetTotalTimeUseCase
import com.rocket.cosmic_detox.domain.usecase.timer.UpdateDailyTimeUseCase
import com.rocket.cosmic_detox.domain.usecase.timer.UpdateTotalTimeUseCase
import com.rocket.cosmic_detox.presentation.uistate.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val getTotalTimeUseCase: GetTotalTimeUseCase,
    private val updateTotalTimeUseCase: UpdateTotalTimeUseCase,
    private val getDailyTimeUseCase: GetDailyTimeUseCase,
    private val updateDailyTimeUseCase: UpdateDailyTimeUseCase,
    private val updateRankingTotalTimeUseCase: UpdateRankingTotalTimeUseCase


    ) : ViewModel() {
    private val _userState = MutableStateFlow<UiState<User>>(UiState.Init)
    val userState: StateFlow<UiState<User>> get() = _userState.asStateFlow()

    private val _totalTimeState = MutableStateFlow<UiState<Long>>(UiState.Init)
    val totalTimeState: StateFlow<UiState<Long>> get() = _totalTimeState.asStateFlow()

    private val _dailyTimeState = MutableStateFlow<UiState<Long>>(UiState.Init)
    val dailyTimeState: StateFlow<UiState<Long>> get() = _dailyTimeState.asStateFlow()

    private var currentTotalTime: Long = 0L
    private var currentDailyTime: Long = 0L


    val currentUserUID: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid



    fun fetchUserData() {
        viewModelScope.launch {
            _userState.value = UiState.Loading
            getUserDataUseCase(
                callback = { user ->
                    _userState.value = UiState.Success(user)
                },
                failCallback = { exception ->
                    _userState.value = UiState.Failure(exception ?: Exception("Unknown error"))
                }
            )
        }
    }

    fun fetchTotalTime() {
        viewModelScope.launch {
            _totalTimeState.value = UiState.Loading
            getTotalTimeUseCase(
                callback = { totalTime ->
                    currentTotalTime = totalTime
                    _totalTimeState.value = UiState.Success(totalTime)
                },
                failCallback = { exception ->
                    _totalTimeState.value = UiState.Failure(exception ?: Exception("에러 발생"))
                }
            )
        }
    }
    fun updateDailyTime(dailyTime: Long) {
        viewModelScope.launch {
            _dailyTimeState.value = UiState.Loading
            updateDailyTimeUseCase(
                dailyTime,
                callback = {
                    currentDailyTime = dailyTime
                    _dailyTimeState.value = UiState.Success(dailyTime)
                },
                failCallback = { exception ->
                    _dailyTimeState.value = UiState.Failure(exception ?: Exception("에러 발생"))
                }
            )
        }
    }

    fun fetchDailyTime() {
        viewModelScope.launch {
            _dailyTimeState.value = UiState.Loading
            getDailyTimeUseCase(
                callback = { dailyTime ->
                    currentDailyTime = dailyTime
                    _dailyTimeState.value = UiState.Success(dailyTime)
                },
                failCallback = { exception ->
                    _dailyTimeState.value = UiState.Failure(exception ?: Exception("에러 발생"))
                }
            )
        }
    }

    fun updateTotalTime(totalTime: Long) {
        viewModelScope.launch {
            _totalTimeState.value = UiState.Loading
            updateTotalTimeUseCase(
                totalTime,
                callback = {
                    currentTotalTime = totalTime
                    _totalTimeState.value = UiState.Success(totalTime)
                },
                failCallback = { exception ->
                    _totalTimeState.value = UiState.Failure(exception ?: Exception("에러 발생"))
                }
            )
        }
    }


    fun updateRankingTotalTime(totalTime: Long) {
        val uid = currentUserUID
        Log.d("Uid체크", uid.toString())

        if (uid != null) {
            viewModelScope.launch {
                updateRankingTotalTimeUseCase(
                    totalTime,
                    uid,
                    callback = {
                        Log.d("rank", "Ranking totalTime 업데이트 성공")
                    },
                    failCallback = { exception ->
                        Log.e("rank", "Ranking totalTime 업데이트 실패", exception)
                    }
                )
            }
        } else {
            Log.e("UserViewModel", "로그인된 사용자가 없습니다.")
        }
    }
}
