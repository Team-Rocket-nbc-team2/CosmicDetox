package com.rocket.cosmic_detox.presentation.view.fragment.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.AppUsage
import com.rocket.cosmic_detox.data.model.User
import com.rocket.cosmic_detox.domain.repository.MyPageRepository
import com.rocket.cosmic_detox.presentation.uistate.MyPageUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val repository: MyPageRepository
) : ViewModel() {

    private val _myInfo = MutableStateFlow<MyPageUiState<User>>(MyPageUiState.Loading)
    val myInfo: StateFlow<MyPageUiState<User>> = _myInfo

    private val _myAppUsageList = MutableStateFlow<MyPageUiState<List<AppUsage>>>(MyPageUiState.Loading)
    val myAppUsageList: StateFlow<MyPageUiState<List<AppUsage>>> = _myAppUsageList

    private val _updateResult = MutableStateFlow(false)
    val updateResult: StateFlow<Boolean> = _updateResult

    fun loadMyInfo() {
        viewModelScope.launch {
            repository.getMyInfo()
                .catch {
                    Log.e("MyPageViewModel", "loadMyInfo: $it")
                    _myInfo.value = MyPageUiState.Error(it.toString())
                }
                .collect {
                    Log.d("MyPageViewModel", "User: $it")
                    _myInfo.value = MyPageUiState.Success(it)
                }
        }
    }

    fun loadMyAppUsage() {
        viewModelScope.launch {
            repository.getMyAppUsage()
                .catch {
                    _myAppUsageList.value = MyPageUiState.Error(it.toString())
                    Log.e("MyPageViewModel", "loadMyAppUsage: $it")
                }
                .collect {
                    _myAppUsageList.value = MyPageUiState.Success(it)
                }
        }
    }

    fun setAppUsageLimit(allowedApp: AllowedApp, hour: String, minute: String) {
        viewModelScope.launch {
            //val limitedTime = (hour.toInt() * 60 + minute.toInt()) * 60 * 1000L // 시간과 분을 밀리초로 변환
            val limitedTime = (hour.toInt() * 60 + minute.toInt()) * 60 // 시간과 분을 초로 변환

//            val success = repository.updateAppUsageLimit(allowedApp.copy(limitedTime = limitedTime.toInt()))
//            _updateResult.value = success
//            if (success) {
//                loadMyInfo()
//            }
            repository.updateAppUsageLimit(allowedApp.copy(limitedTime = limitedTime.toInt()))
                .onSuccess {
                    // TODO: 나중에 UiState로 변경해보기
                    _updateResult.value = it
                    loadMyInfo()
                }.onFailure {
                    Log.e("MyPageViewModel", "업데이트 실패: $it")
                }
        }
    }

    fun resetUpdateResult() {
        _updateResult.value = false
    }
}
