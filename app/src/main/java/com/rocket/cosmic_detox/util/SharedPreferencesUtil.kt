package com.rocket.cosmic_detox.util

import android.content.Context

object SharedPreferencesUtil {

    private const val PREFS_NAME = "app_preferences"
    private const val FIRST_TIME_USER_KEY = "is_first_time_user"

    fun isFirstTimeUser(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(FIRST_TIME_USER_KEY, true)
    }

    fun setFirstTimeUserCompleted(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(FIRST_TIME_USER_KEY, false).apply()
    }
}