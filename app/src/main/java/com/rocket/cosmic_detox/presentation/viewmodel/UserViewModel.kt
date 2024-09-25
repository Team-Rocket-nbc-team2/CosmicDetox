package com.rocket.cosmic_detox.presentation.viewmodel

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
import com.rocket.cosmic_detox.domain.usecase.user.DeleteUserUseCase
import com.rocket.cosmic_detox.domain.usecase.user.SignOutUseCase
import com.rocket.cosmic_detox.presentation.uistate.LoginUiState
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
    private val updateRankingTotalTimeUseCase: UpdateRankingTotalTimeUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val deleteUserUseCase: DeleteUserUseCase
) : ViewModel() {
    private val _userState = MutableStateFlow<UiState<User>>(UiState.Init)
    val userState: StateFlow<UiState<User>> get() = _userState.asStateFlow()

    private val _totalTimeState = MutableStateFlow<UiState<Long>>(UiState.Init)
    val totalTimeState: StateFlow<UiState<Long>> get() = _totalTimeState.asStateFlow()

    private val _dailyTimeState = MutableStateFlow<UiState<Long>>(UiState.Init)
    val dailyTimeState: StateFlow<UiState<Long>> get() = _dailyTimeState.asStateFlow()

    private val _signOutState = MutableStateFlow<LoginUiState>(LoginUiState.Init)
    val signOutState: StateFlow<LoginUiState> = _signOutState.asStateFlow()

    private val _deleteUserState = MutableStateFlow<LoginUiState>(LoginUiState.Init)
    val deleteUserState: StateFlow<LoginUiState> = _deleteUserState.asStateFlow()

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

    fun updateTotalTime(updatedTime: Long) {
        viewModelScope.launch {
            _totalTimeState.value = UiState.Loading
            val deltaDailyTime = updatedTime - currentDailyTime // 전달된 time과 이전 dailyTime의 차이 계산
            val updatedTotalTime = currentTotalTime + deltaDailyTime // 차이를 기존 totalTime에 더함

            // 계산된 totalTime을 업데이트
            updateTotalTimeUseCase(
                updatedTotalTime,
                callback = {
                    currentTotalTime = updatedTotalTime
                    currentDailyTime = updatedTime
                    _totalTimeState.value = UiState.Success(updatedTotalTime)
                },
                failCallback = { exception ->
                    _totalTimeState.value = UiState.Failure(exception ?: Exception("totalTime 업데이트 중 에러 발생"))
                }
            )
        }
    }

    fun updateRankingTotalTime(updatedTime: Long) {
        val uid = currentUserUID
        Log.d("Uid체크", uid.toString())

        if (uid != null) {
            viewModelScope.launch {
                // 이전 dailyTime과 현재 time의 차이
                val deltaDailyTime = updatedTime - currentDailyTime
                val updatedTotalTime = currentTotalTime + deltaDailyTime // 차이를 기존 totalTime에 더함

                updateRankingTotalTimeUseCase(
                    updatedTotalTime,
                    uid,
                    callback = {
                        Log.d("rank", "Ranking totalTime 업데이트 성공")

                        currentTotalTime = updatedTotalTime
                        currentDailyTime = updatedTime
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

    fun signOut() {
        viewModelScope.launch {
            _signOutState.value = LoginUiState.Loading
            signOutUseCase(
                onSuccess = {
                    _signOutState.value = LoginUiState.Success
                },
                onFailure = {
                    _signOutState.value = LoginUiState.Failure(it)
                }
            )
        }
    }

    fun deleteUser() {
        viewModelScope.launch {
            _deleteUserState.value = LoginUiState.Loading
            deleteUserUseCase(
                onSuccess = {
                    _deleteUserState.value = LoginUiState.Success
                },
                onFailure = {
                    _deleteUserState.value = LoginUiState.Failure(it)
                }
            )
        }
    }
}
