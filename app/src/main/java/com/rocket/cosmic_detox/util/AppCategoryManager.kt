package com.rocket.cosmic_detox.util

import android.content.pm.ApplicationInfo
import com.rocket.cosmic_detox.presentation.extensions.fromMinutesToSeconds

object AppCategoryManager {

    fun getLimitedTimeByCategory(category: Int): Long {
        return when(category) {
            ApplicationInfo.CATEGORY_GAME -> 30L.fromMinutesToSeconds()
            ApplicationInfo.CATEGORY_AUDIO -> 30L.fromMinutesToSeconds()
            ApplicationInfo.CATEGORY_VIDEO -> 30L.fromMinutesToSeconds()
            ApplicationInfo.CATEGORY_IMAGE -> 30L.fromMinutesToSeconds()
            ApplicationInfo.CATEGORY_SOCIAL -> 30L.fromMinutesToSeconds()
            ApplicationInfo.CATEGORY_NEWS -> 30L.fromMinutesToSeconds()
            ApplicationInfo.CATEGORY_MAPS -> 30L.fromMinutesToSeconds()
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> 30L.fromMinutesToSeconds()
            else -> 30L.fromMinutesToSeconds()
        }
    }
}