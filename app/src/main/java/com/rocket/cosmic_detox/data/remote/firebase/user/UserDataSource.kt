package com.rocket.cosmic_detox.data.remote.firebase.user

import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.Trophy
import com.rocket.cosmic_detox.data.model.User

interface UserDataSource {
    suspend fun getUserInfo(uid: String): Result<User>

    suspend fun getUserApps(uid: String): Result<List<AllowedApp>>

    suspend fun getUserTrophies(uid: String): Result<List<Trophy>>

    suspend fun updateAppUsageLimit(uid: String, allowedApp: AllowedApp): Result<Boolean>

    suspend fun updateAllowedApps(uid: String, apps: List<AllowedApp>): Result<Boolean>
}