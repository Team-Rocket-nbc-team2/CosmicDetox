package com.rocket.cosmic_detox.presentation.uistate

sealed interface GetListUiState<out T> {
    data class Success<T>(val data: T): GetListUiState<T>
    data object Empty: GetListUiState<Nothing>
    data class Failure(val e: Throwable?): GetListUiState<Nothing>
    data object Loading: GetListUiState<Nothing>
    data object Init: GetListUiState<Nothing>
    data class Error(val message: String): GetListUiState<Nothing>
}