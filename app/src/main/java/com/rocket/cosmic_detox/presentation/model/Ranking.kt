package com.rocket.cosmic_detox.presentation.model

sealed class Ranking

data class RankingTop(
    val topItems: List<RankingInfo>
) : Ranking()

data class RankingBottom(
    val bottomItems: List<RankingInfo>
) : Ranking()

data class RankingInfo(
    val id: String,
    val name: String,
    val time: Long,
    val point: Long,
)
