package com.rocket.cosmic_detox.data.model

import java.math.BigDecimal

private const val RANKING_TOP = "top"
private const val RANKING_BOTTOM = "bottom"

sealed class Ranking {
    abstract val id: String
}

data class RankingTop(
    val topItems: List<RankingInfo>
) : Ranking() {
    override val id: String = RANKING_TOP
}

data class RankingBottom(
    val bottomItems: List<RankingInfo>
) : Ranking() {
    override val id: String = RANKING_BOTTOM
}

data class RankingInfo(
    val uid: String,
    val name: String,
    val cumulativeTime: BigDecimal,
    val points: BigDecimal
)