package com.rocket.cosmic_detox.presentation.view.fragment.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rocket.cosmic_detox.data.model.AppUsage
import com.rocket.cosmic_detox.data.model.MyInfo
import com.rocket.cosmic_detox.data.model.User
import com.rocket.cosmic_detox.domain.repository.MyPageRepository
import com.rocket.cosmic_detox.presentation.uistate.MyPageUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
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

    fun loadMyInfo() {
        viewModelScope.launch {
            repository.getMyInfo()
                .catch {
                    Log.e("jade", "loadMyInfo: $it")
                    _myInfo.value = MyPageUiState.Error(it.toString())
                }
                .collect{
                    _myInfo.value = MyPageUiState.Success(it)
                    Log.d("jade", "loadMyInfo: $it")
                }
        }
    }

    fun loadMyAppUsage() {
        viewModelScope.launch {
            repository.getMyAppUsage()
                .catch {
                    _myAppUsageList.value = MyPageUiState.Error(it.toString())
                    Log.e("jade", "loadMyAppUsage: $it")
                }
                .collect {
                    _myAppUsageList.value = MyPageUiState.Success(it)
                    Log.d("jade", "loadMyAppUsage: $it")
                }
        }
    }
}
