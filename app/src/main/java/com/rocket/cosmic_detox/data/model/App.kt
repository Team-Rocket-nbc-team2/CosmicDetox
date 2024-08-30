package com.rocket.cosmic_detox.data.model

import android.graphics.drawable.Drawable

data class App(
    val packageId: String,
    val appName: String,
    val appIcon: Drawable?,
    val limitedTime: Long
)
