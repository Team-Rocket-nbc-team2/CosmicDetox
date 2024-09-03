package com.rocket.cosmic_detox.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.domain.usecase.timer.GetAllowedAppUseCase
import com.rocket.cosmic_detox.domain.usecase.timer.UpdateLimitedTimeAppUseCase
import com.rocket.cosmic_detox.presentation.uistate.GetListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllowedAppViewModel @Inject constructor(
    private val getAllowedAppUseCase: GetAllowedAppUseCase,
    private val updateLimitedTimeAppUseCase: UpdateLimitedTimeAppUseCase
): ViewModel() {
    private val _allowedAppList = MutableStateFlow<GetListUiState<List<AllowedApp>>>(GetListUiState.Init)
    val allowedAppList: StateFlow<GetListUiState<List<AllowedApp>>> = _allowedAppList

    private val _countDownRemainTime = MutableStateFlow<Int?>(null)
    val countDownRemainTime: StateFlow<Int?> = _countDownRemainTime

    private val _selectedAllowedAppPackage = MutableStateFlow<String?>(null)
    val selectedAllowedAppPackage: StateFlow<String?> = _selectedAllowedAppPackage

    fun getAllAllowedApps() {
        viewModelScope.launch {
            _allowedAppList.value = GetListUiState.Loading

            getAllowedAppUseCase(
                callback = { allowedApps ->
                    _allowedAppList.value = if (allowedApps.isEmpty()) GetListUiState.Empty
                    else GetListUiState.Success(allowedApps)
                },
                failCallback = { exception ->
                    _allowedAppList.value = GetListUiState.Failure(exception)
                }
            )
        }
    }

    fun setSelectedAllowedAppPackage(packageId: String) {
        _selectedAllowedAppPackage.value = packageId
    }

    fun updateRemainTime(remainTime: Int) {
        _countDownRemainTime.value = remainTime
    }

    fun updateLimitedTimeAllowApp(
        packageId: String,
        remainTime: Int,
        failCallback: (Throwable?) -> Unit
    ) {
        viewModelScope.launch {
            updateLimitedTimeAppUseCase(
                packageId = packageId,
                remainTime = remainTime,
                failCallback = failCallback
            )
        }
    }
}