package com.rocket.cosmic_detox.util

import android.content.Context
import androidx.core.content.edit

object SharedPreferencesUtil {

    private const val PREFS_NAME = "app_preferences"
    private const val FIRST_TIME_USER_KEY = "is_first_time_user"
    private const val LAST_DAILY_RESET_DATE_KEY = "last_daily_reset_date"

    fun isFirstTimeUser(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(FIRST_TIME_USER_KEY, true)
    }

    fun setFirstTimeUserCompleted(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(FIRST_TIME_USER_KEY, false).apply()
    }

    fun getLastDailyResetDate(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(LAST_DAILY_RESET_DATE_KEY, null)
    }

    fun setLastDailyResetDate(context: Context, date: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit { putString(LAST_DAILY_RESET_DATE_KEY, date) }
    }
}
