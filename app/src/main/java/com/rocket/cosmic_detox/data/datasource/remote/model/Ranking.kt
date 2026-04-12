package com.rocket.cosmic_detox.data.datasource.remote.model

import androidx.annotation.Keep

@Keep
data class RankingInfo(
    val name: String = "",
    val point: Int = 0,
    val totalTime: Long = 0,
    val uid: String = "",
    val rank: Int = 0
)
