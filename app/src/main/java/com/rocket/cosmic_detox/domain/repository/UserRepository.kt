package com.rocket.cosmic_detox.domain.repository

import com.rocket.cosmic_detox.data.model.User
import java.lang.Exception

interface UserRepository {
    fun getUserData(successCallback: (User) -> Unit, failCallback: (Exception?) -> Unit)
    fun saveUserData(user: User, successCallback: () -> Unit, failCallback: (Exception?) -> Unit)
    fun updateTotalTime(totalTime: Long, successCallback: () -> Unit, failCallback: (Exception?) -> Unit)
    fun getTotalTime(successCallback: (Long) -> Unit, failCallback: (Exception?) -> Unit)
    fun updateDailyTime(dailyTime: Long, successCallback: () -> Unit, failCallback: (Exception?) -> Unit)
    fun getDailyTime(successCallback: (Long) -> Unit, failCallback: (Exception?) -> Unit)
}

