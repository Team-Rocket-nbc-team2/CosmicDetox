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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.zip
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
            val uid = repository.getUid()
            // getUserInfo, getUserApps, getUserTrophies를 zip으로 묶어서 한 번에 처리
            repository.getUserInfo(uid)
                .zip(repository.getUserApps(uid)) { user, apps ->
                    user to apps // User와 AllowedApp을 함께 반환
                }
                .zip(repository.getUserTrophies(uid)) { (user, apps), trophies ->
                    return@zip user.copy(apps = apps, trophies = trophies) // 세 결과를 조합하여 User 객체 생성, 이렇게 하면 되나..?
                }
                .flowOn(Dispatchers.IO)
                .catch { exception ->
                    Log.e("MyPageViewModel", "loadMyInfo: $exception")
                    _myInfo.value = MyPageUiState.Error(exception.toString())
                }
                .collect { myInfo ->
                    Log.d("MyPageViewModel", "User: $myInfo")
                    _myInfo.value = MyPageUiState.Success(myInfo)
                }
        }
    }

    fun loadMyAppUsage() {
        viewModelScope.launch {
            repository.getMyAppUsage()
                .flowOn(Dispatchers.IO)
                .catch { exception ->
                    _myAppUsageList.value = MyPageUiState.Error(exception.toString())
                    Log.e("MyPageViewModel", "loadMyAppUsage: $exception")
                }
                .collect { apps ->
                    _myAppUsageList.value = MyPageUiState.Success(apps)
                }
        }
    }

    fun setAppUsageLimit(allowedApp: AllowedApp, hour: String, minute: String) {
        viewModelScope.launch {
            //val limitedTime = (hour.toInt() * 60 + minute.toInt()) * 60 * 1000L // 시간과 분을 밀리초로 변환
            val limitedTime = (hour.toInt() * 60 + minute.toInt()) * 60L // 시간과 분을 초로 변환

            repository.updateAppUsageLimit(allowedApp.copy(limitedTime = limitedTime))
                .onSuccess {
                    // TODO: 나중에 UiState로 변경해보기
                    _updateResult.value = true
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