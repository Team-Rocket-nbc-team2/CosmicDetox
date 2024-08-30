package com.rocket.cosmic_detox.presentation.uistate

import java.lang.Exception

sealed interface UiState<out T> {
    data class Success<T>(val data: T): UiState<T>
    data class Failure(val e: Exception?): UiState<Nothing>
    data object Loading: UiState<Nothing>
    data object Init: UiState<Nothing>
}