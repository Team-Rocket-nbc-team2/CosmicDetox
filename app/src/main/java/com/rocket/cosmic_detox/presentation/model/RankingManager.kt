package com.rocket.cosmic_detox.presentation.model

import java.math.BigDecimal

object RankingManager {

    private val rankingList: List<RankingInfo> = getDummyData()

    private fun getDummyData(): List<RankingInfo> {
        return listOf(
            RankingInfo(
                id = "1",
                name = "Alice",
                cumulativeTime = BigDecimal(1000),
                points = BigDecimal(100)
            ),
            RankingInfo(
                id = "2",
                name = "Bob",
                cumulativeTime = BigDecimal(2000),
                points = BigDecimal(90)
            ),
            RankingInfo(
                id = "3",
                name = "Charlie",
                cumulativeTime = BigDecimal(3000),
                points = BigDecimal(80)
            ),
            RankingInfo(
                id = "4",
                name = "David",
                cumulativeTime = BigDecimal(4000),
                points = BigDecimal(70)
            ),
            RankingInfo(
                id = "5",
                name = "Eve",
                cumulativeTime = BigDecimal(5000),
                points = BigDecimal(60)
            ),
            RankingInfo(
                id = "6",
                name = "Frank",
                cumulativeTime = BigDecimal(6000),
                points = BigDecimal(50)
            ),
            RankingInfo(
                id = "7",
                name = "Grace",
                cumulativeTime = BigDecimal(7000),
                points = BigDecimal(40)
            ),
            RankingInfo(
                id = "8",
                name = "Hank",
                cumulativeTime = BigDecimal(8000),
                points = BigDecimal(30)
            ),
            RankingInfo(
                id = "9",
                name = "Ivy",
                cumulativeTime = BigDecimal(9000),
                points = BigDecimal(20)
            ),
            RankingInfo(
                id = "10",
                name = "Jack",
                cumulativeTime = BigDecimal(10000),
                points = BigDecimal(10)
            ),
            RankingInfo(
                id = "11",
                name = "Kate",
                cumulativeTime = BigDecimal(11000),
                points = BigDecimal(5)
            ),
            RankingInfo(
                id = "12",
                name = "Leo",
                cumulativeTime = BigDecimal(12000),
                points = BigDecimal(3)
            ),
            RankingInfo(
                id = "13",
                name = "Mia",
                cumulativeTime = BigDecimal(13000),
                points = BigDecimal(2)
            ),
            RankingInfo(
                id = "14",
                name = "Nina",
                cumulativeTime = BigDecimal(14000),
                points = BigDecimal(1)
            ),
            RankingInfo(
                id = "15",
                name = "Owen",
                cumulativeTime = BigDecimal(15000),
                points = BigDecimal(1)
            ),
            RankingInfo(
                id = "16",
                name = "Pam",
                cumulativeTime = BigDecimal(16000),
                points = BigDecimal(1)
            ),
            RankingInfo(
                id = "17",
                name = "Quinn",
                cumulativeTime = BigDecimal(17000),
                points = BigDecimal(1)
            ),
            RankingInfo(
                id = "18",
                name = "Rex",
                cumulativeTime = BigDecimal(18000),
                points = BigDecimal(1)
            ),
            RankingInfo(
                id = "19",
                name = "Sue",
                cumulativeTime = BigDecimal(19000),
                points = BigDecimal(1)
            ),
            RankingInfo(
                id = "20",
                name = "Tom",
                cumulativeTime = BigDecimal(20000),
                points = BigDecimal(1)
            )
        )
    }

    fun getRankingList(): List<RankingInfo> {
        return rankingList.sortedByDescending { it.cumulativeTime }
    }
}