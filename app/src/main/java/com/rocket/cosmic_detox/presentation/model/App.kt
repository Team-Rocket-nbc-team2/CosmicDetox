package com.rocket.cosmic_detox.presentation.model

import android.graphics.drawable.Drawable

data class App(
    val packageId: String,
    val appName: String,
    val appIcon: Int,
    val limitedTime: Long
)
