package com.rocket.cosmic_detox.presentation.uistate

sealed interface UiState<out T> {
    data class Success<T>(val data: T): UiState<T>
    data class Failure(val e: Throwable?): UiState<Nothing>
    data object Loading: UiState<Nothing>
    data object Init: UiState<Nothing>
}