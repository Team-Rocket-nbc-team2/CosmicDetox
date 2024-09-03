package com.rocket.cosmic_detox.presentation.component.bottomsheet.modifyallowedapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.domain.repository.AllowAppRepository
import com.rocket.cosmic_detox.presentation.uistate.GetListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllowAppViewModel @Inject constructor(
    private val repository: AllowAppRepository
) : ViewModel() {

    private val _installedApps = MutableStateFlow<GetListUiState<List<AllowedApp>>>(GetListUiState.Init)
    val installedApps: StateFlow<GetListUiState<List<AllowedApp>>> = _installedApps

    private val _updateResult = MutableStateFlow(false)
    val updateResult: StateFlow<Boolean> = _updateResult

    fun loadInstalledApps() {
        viewModelScope.launch {
            _installedApps.value = GetListUiState.Loading
            repository.getInstalledApps()
                .catch { exception ->
                    _installedApps.value = GetListUiState.Error(exception.toString())
                    Log.e("AllowAppViewModel", "알 수 없는 에러 발생", exception)
                }
                .collect { apps ->
                    _installedApps.value = if (apps.isEmpty()) {
                        GetListUiState.Empty
                    } else {
                        GetListUiState.Success(apps)
                    }
                    Log.d("AllowAppViewModel", "loadInstalledApps: ${_installedApps.value}")
                }
        }
    }

    fun updateAllowApps(apps: List<AllowedApp>) {
        viewModelScope.launch {
            repository.updateAllowedApps(apps)
                .onSuccess { // TODO: 나중에 UiState로 변경해서 Toast 띄우던가 하기
                    Log.d("AllowAppViewModel", "허용 앱 업로드 성공")
                    _updateResult.value = true
                }
                .onFailure {
                    Log.e("AllowAppViewModel", "허용 앱 업로드 실패 $it")
                }
//                .catch { exception ->
//                    Log.e("AllowAppViewModel", "허용 앱 업로드 실패 ${exception.message}")
//                }
//                .collect { result ->
//                    if (result) {
//                        Log.d("AllowAppViewModel", "허용 앱 업로드 성공")
//                    }
//                }
        }
    }

    fun resetUpdateResult() {
        _updateResult.value = false
    }
}