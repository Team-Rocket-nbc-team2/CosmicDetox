package com.rocket.cosmic_detox.presentation.model

object RankingManager {

    private val rankingList: List<RankingInfo> = getDummyData()

    private fun getDummyData(): List<RankingInfo> {
        return listOf(
            RankingInfo(
                id = "1",
                name = "Alice",
                time = 1000,
                point = 100
            ),
            RankingInfo(
                id = "2",
                name = "Bob",
                time = 2000,
                point = 90
            ),
            RankingInfo(
                id = "3",
                name = "Charlie",
                time = 3000,
                point = 80
            ),
            RankingInfo(
                id = "4",
                name = "David",
                time = 4000,
                point = 70
            ),
            RankingInfo(
                id = "5",
                name = "Eve",
                time = 5000,
                point = 60
            ),
            RankingInfo(
                id = "6",
                name = "Frank",
                time = 6000,
                point = 50
            ),
            RankingInfo(
                id = "7",
                name = "Grace",
                time = 7000,
                point = 40
            ),
            RankingInfo(
                id = "8",
                name = "Hank",
                time = 8000,
                point = 30
            ),
            RankingInfo(
                id = "9",
                name = "Ivy",
                time = 9000,
                point = 20
            ),
            RankingInfo(
                id = "10",
                name = "Jack",
                time = 10000,
                point = 10
            ),
            RankingInfo(
                id = "11",
                name = "Kate",
                time = 11000,
                point = 5
            ),
            RankingInfo(
                id = "12",
                name = "Leo",
                time = 12000,
                point = 3
            ),
            RankingInfo(
                id = "13",
                name = "Mia",
                time = 13000,
                point = 2
            ),
            RankingInfo(
                id = "14",
                name = "Nina",
                time = 14000,
                point = 1
            ),
            RankingInfo(
                id = "15",
                name = "Owen",
                time = 15000,
                point = 1
            ),
            RankingInfo(
                id = "16",
                name = "Pam",
                time = 16000,
                point = 1
            ),
            RankingInfo(
                id = "17",
                name = "Quinn",
                time = 17000,
                point = 1
            ),
            RankingInfo(
                id = "18",
                name = "Rex",
                time = 18000,
                point = 1
            ),
            RankingInfo(
                id = "19",
                name = "Sue",
                time = 19000,
                point = 1
            ),
            RankingInfo(
                id = "20",
                name = "Tom",
                time = 20000,
                point = 1
            )
        )
    }

    fun getRankingList(): List<RankingInfo> {
        return rankingList.sortedByDescending { it.time }
    }
}