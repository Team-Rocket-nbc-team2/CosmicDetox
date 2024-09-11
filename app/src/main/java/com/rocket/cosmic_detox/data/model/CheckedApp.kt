package com.rocket.cosmic_detox.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckedApp(
    val packageId: String,
    val appName: String,
    val limitedTime: Long,
    val isChecked: Boolean = false
) : Parcelable
