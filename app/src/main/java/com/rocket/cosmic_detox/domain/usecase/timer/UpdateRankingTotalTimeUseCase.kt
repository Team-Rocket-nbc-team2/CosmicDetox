package com.rocket.cosmic_detox.domain.usecase.ranking

import com.rocket.cosmic_detox.domain.repository.RankingRepository
import javax.inject.Inject

class UpdateRankingTotalTimeUseCase @Inject constructor(
    private val rankingRepository: RankingRepository
) {
    operator fun invoke(totalTime: Long, userUID: String, callback: () -> Unit, failCallback: (Exception?) -> Unit) {
        rankingRepository.updateTotalTime(userUID, totalTime, callback, failCallback)
    }
}

