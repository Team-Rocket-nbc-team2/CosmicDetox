package com.rocket.cosmic_detox.util

import com.rocket.cosmic_detox.data.model.RankingInfo

object RankingCalculator {

    fun assignRanks(sortedDocs: List<RankingInfo>): List<RankingInfo> {
        val rankingList = mutableListOf<RankingInfo>()
        var currentRank = 1
        var previousTime = -1L
        var sameRankCount = 0

        sortedDocs.forEach { rankingInfo ->
            if (rankingInfo.totalTime != previousTime) {
                // 새로운 totalTime이면 순위를 증가
                currentRank += sameRankCount
                sameRankCount = 1 // 초기화
                previousTime = rankingInfo.totalTime
            } else {
                // 같은 totalTime을 가진 사람들의 수 증가
                sameRankCount++
            }

            // 현재 순위로 공동 순위 부여
            rankingList.add(rankingInfo.copy(rank = currentRank))
        }

        return rankingList
    }
}