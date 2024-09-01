package com.rocket.cosmic_detox.presentation.uistate

sealed interface MyPageUiState<out T> {
    object Loading : MyPageUiState<Nothing>

    data class Success<T>(val data: T) : MyPageUiState<T>

    data class Error(val message: String) : MyPageUiState<Nothing>
}