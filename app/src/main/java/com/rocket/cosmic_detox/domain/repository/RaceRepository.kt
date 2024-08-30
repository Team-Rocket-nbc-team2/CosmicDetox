package com.rocket.cosmic_detox.domain.repository

import com.rocket.cosmic_detox.presentation.model.RankingInfo
import kotlinx.coroutines.flow.Flow

// annotation 추가 해야함 아직 이해 잘 안감
interface RaceRepository {
    fun getRanking() : Flow<List<RankingInfo>>
}