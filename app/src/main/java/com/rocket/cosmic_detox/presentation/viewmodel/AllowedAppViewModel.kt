package com.rocket.cosmic_detox.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.domain.usecase.timer.GetAllowedAppUseCase
import com.rocket.cosmic_detox.presentation.uistate.GetListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllowedAppViewModel @Inject constructor(
    private val getAllowedAppUseCase: GetAllowedAppUseCase
): ViewModel() {
    private val _allowedAppList =  MutableStateFlow<GetListUiState<List<AllowedApp>>>(GetListUiState.Init)
    val allowedAppList: StateFlow<GetListUiState<List<AllowedApp>>> = _allowedAppList

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

    // todo :: 허용 앱 설정하는 로직 여기에 구현.
}