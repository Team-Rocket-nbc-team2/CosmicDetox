package com.rocket.cosmic_detox.data.datasource.remote.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class AllowedApp(
    val packageId: String = "",
    val appName: String = "",
    val appIcon: String = "",
    val limitedTime: Long = 0,
) : Parcelable
