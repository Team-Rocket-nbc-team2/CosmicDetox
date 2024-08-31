package com.rocket.cosmic_detox.data.model

import com.rocket.cosmic_detox.presentation.view.fragment.mypage.adapter.Trophy

data class User(
    val uld: String,
    val name: String,
    val dailyTime: Long,
    val totalTime: Long,
    val totalDay: Int,
    val isWithdrawn: Boolean,
    val trophies: List<Trophy>,
    val apps: List<App>
)
