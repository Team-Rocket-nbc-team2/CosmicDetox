package com.rocket.cosmic_detox.presentation.view.fragment.race.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rocket.cosmic_detox.domain.usecase.RaceUseCase
import com.rocket.cosmic_detox.data.model.RankingInfo
import com.rocket.cosmic_detox.domain.repository.RaceRepository
import com.rocket.cosmic_detox.presentation.uistate.MyPageUiState
import com.rocket.cosmic_detox.presentation.uistate.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RaceViewModel @Inject constructor(
    private val raceUseCase: RaceUseCase,
    private val repository: RaceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<MyPageUiState<List<RankingInfo>>>(MyPageUiState.Loading)
    val uiState: StateFlow<MyPageUiState<List<RankingInfo>>> = _uiState.asStateFlow()

    private val _myRank = MutableStateFlow<MyPageUiState<Int>>(MyPageUiState.Loading)
    val myRank: StateFlow<MyPageUiState<Int>> = _myRank

    init {
        getRanking()
    }

    fun getRanking() {
        viewModelScope.launch {
            raceUseCase.getRanking()
                .catch { e -> _uiState.value = MyPageUiState.Error(e.message.toString()) }
                .collect { ranking -> _uiState.value = MyPageUiState.Success(ranking) }
        }
    }

    fun getMyRank() {
        viewModelScope.launch {
            repository.getMyRank()
                .onSuccess { rank ->
                    _myRank.value = MyPageUiState.Success(rank)
                    Log.d("RaceViewModel", "Rank: $rank")
                }
                .onFailure { exception ->
                    _myRank.value = MyPageUiState.Error(exception.message ?: "Unknown error")
                    Log.e("RaceViewModel", "getMyRank: $exception")
                }
        }
    }
}