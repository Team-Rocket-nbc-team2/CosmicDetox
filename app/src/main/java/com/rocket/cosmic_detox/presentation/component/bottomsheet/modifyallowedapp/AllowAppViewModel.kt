package com.rocket.cosmic_detox.presentation.component.bottomsheet.modifyallowedapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.CheckedApp
import com.rocket.cosmic_detox.domain.repository.AllowAppRepository
import com.rocket.cosmic_detox.presentation.uistate.GetListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllowAppViewModel @Inject constructor(
    private val repository: AllowAppRepository
) : ViewModel() {

    private val _installedApps = MutableStateFlow<GetListUiState<List<CheckedApp>>>(GetListUiState.Init)
    val installedApps: StateFlow<GetListUiState<List<CheckedApp>>> = _installedApps

    private val _updateResult = MutableStateFlow(false)
    val updateResult: StateFlow<Boolean> = _updateResult

    private var currentApps: List<CheckedApp> = emptyList() // 현재 앱 리스트를 저장하는 변수

    fun loadInstalledApps() {
        viewModelScope.launch {
            _installedApps.value = GetListUiState.Loading
            repository.getInstalledApps()
                .flowOn(Dispatchers.IO)
                .catch { exception ->
                    _installedApps.value = GetListUiState.Error(exception.toString())
                    Log.e("AllowAppViewModel", "알 수 없는 에러 발생", exception)
                }
                .collect { apps ->
                    currentApps = apps // 데이터를 로드할 때 저장
                    _installedApps.value = if (apps.isEmpty()) {
                        GetListUiState.Empty
                    } else {
                        GetListUiState.Success(apps)
                    }
                    Log.d("AllowAppViewModel", "loadInstalledApps: ${_installedApps.value}")
                }
        }
    }

    fun updateAllowApps(originApps: List<AllowedApp>, updatedApps: List<AllowedApp>) {
        viewModelScope.launch {
            repository.updateAllowedApps(originApps, updatedApps)
                .onSuccess {
                    Log.d("AllowAppViewModel", "허용 앱 업로드 성공")
                    _updateResult.value = true
                }
                .onFailure {
                    Log.e("AllowAppViewModel", "허용 앱 업로드 실패 $it")
                }
        }
    }

    fun searchApp(query: String) {
        val filteredApps = if (query.isEmpty()) {
            currentApps
        } else {
            currentApps.filter { app -> app.appName.contains(query, ignoreCase = true) }
        }

        _installedApps.value = if (filteredApps.isEmpty()) {
            GetListUiState.Empty
        } else {
            GetListUiState.Success(filteredApps)
        }
    }

    fun resetUpdateResult() {
        _updateResult.value = false
    }
}