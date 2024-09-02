package com.rocket.cosmic_detox.data.model

import java.util.Date

data class Trophy(
    val trophyId: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val winningDate: Date = Date(),
)