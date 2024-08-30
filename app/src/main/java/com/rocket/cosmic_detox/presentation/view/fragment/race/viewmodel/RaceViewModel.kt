package com.rocket.cosmic_detox.presentation.view.fragment.race.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rocket.cosmic_detox.UiState
import com.rocket.cosmic_detox.domain.usecase.RaceUseCase
import com.rocket.cosmic_detox.data.model.RankingInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RaceViewModel @Inject constructor(private val raceUseCase: RaceUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<RankingInfo>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<RankingInfo>>> = _uiState.asStateFlow()

    init {
        getRanking()
    }

    private fun getRanking() {
        viewModelScope.launch {
            raceUseCase.getRanking()
                .catch { e -> _uiState.value = UiState.Error(e) }
                .collect { ranking -> _uiState.value = UiState.Success(ranking) }
        }
    }
}