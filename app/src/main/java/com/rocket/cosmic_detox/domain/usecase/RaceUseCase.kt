package com.rocket.cosmic_detox.domain.usecase

import com.rocket.cosmic_detox.domain.repository.RaceRepository
import com.rocket.cosmic_detox.presentation.model.RankingInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RaceUseCase @Inject constructor(private val repository: RaceRepository) {
    fun getRanking() : Flow<List<RankingInfo>> {
        return repository.getRanking()
    }
}