package com.rocket.cosmic_detox.domain.repository

import com.rocket.cosmic_detox.data.model.RankingInfo
import kotlinx.coroutines.flow.Flow

interface RaceRepository {
    fun getRanking() : Flow<List<RankingInfo>>
}