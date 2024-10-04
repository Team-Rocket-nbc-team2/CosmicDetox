package com.rocket.cosmic_detox.presentation.uistate

sealed interface LoginUiState {
    data object Init: LoginUiState
    data object Loading: LoginUiState
    data object Success: LoginUiState
    data class Failure(val e: Throwable): LoginUiState
    data object Cancel: LoginUiState
}