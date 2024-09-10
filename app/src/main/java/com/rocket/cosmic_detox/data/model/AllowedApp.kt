package com.rocket.cosmic_detox.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AllowedApp(
    val packageId: String = "",
    val appName: String = "",
    val limitedTime: Long = 0,
) : Parcelable
