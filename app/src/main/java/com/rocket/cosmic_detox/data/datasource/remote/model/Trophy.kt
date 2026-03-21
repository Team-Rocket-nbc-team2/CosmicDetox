package com.rocket.cosmic_detox.data.datasource.remote.model

import java.util.Date

data class Trophy(
    val trophyId: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val winningDate: Date = Date(),
)