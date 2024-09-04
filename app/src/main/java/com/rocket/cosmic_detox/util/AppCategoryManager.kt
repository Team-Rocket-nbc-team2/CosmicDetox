package com.rocket.cosmic_detox.util

import android.content.pm.ApplicationInfo
import com.rocket.cosmic_detox.presentation.extensions.fromMinutesToSeconds
import com.rocket.cosmic_detox.presentation.extensions.fromSecondsToMinutes
import com.rocket.cosmic_detox.presentation.extensions.toSeconds

object AppCategoryManager {

    fun getCategory(category: Int): Long {
        return when(category) {
            ApplicationInfo.CATEGORY_GAME -> 30L.fromMinutesToSeconds()
            ApplicationInfo.CATEGORY_AUDIO -> 60L.fromMinutesToSeconds()
            ApplicationInfo.CATEGORY_VIDEO -> 60L.fromMinutesToSeconds()
            ApplicationInfo.CATEGORY_IMAGE -> 30L.fromMinutesToSeconds()
            ApplicationInfo.CATEGORY_SOCIAL -> 60L.fromMinutesToSeconds()
            ApplicationInfo.CATEGORY_NEWS -> 60L.fromMinutesToSeconds()
            ApplicationInfo.CATEGORY_MAPS -> 30L.fromMinutesToSeconds()
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> 60L.fromMinutesToSeconds()
            else -> 30L.fromMinutesToSeconds()
        }
    }
}