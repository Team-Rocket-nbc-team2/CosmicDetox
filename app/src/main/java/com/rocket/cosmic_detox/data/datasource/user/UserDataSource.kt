package com.rocket.cosmic_detox.data.datasource.user

import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.Trophy
import com.rocket.cosmic_detox.data.model.User
import java.util.Date

interface UserDataSource {

    suspend fun getUid(): String

    suspend fun getUserCreatedDate(uid: String): Result<Date>

    suspend fun getUserInfo(uid: String): Result<User>

    suspend fun getUserApps(uid: String): Result<List<AllowedApp>>

    suspend fun getUserTrophies(uid: String): Result<List<Trophy>>

    suspend fun updateAppUsageLimit(uid: String, allowedApp: AllowedApp): Result<Boolean>

    suspend fun updateAllowedApps(uid: String, apps: List<AllowedApp>): Result<Boolean>

    suspend fun addAllowedApps(uid: String, apps: List<AllowedApp>): Result<Boolean>

    suspend fun deleteAllowedApps(uid: String, appIds: List<String>): Result<Boolean>

    suspend fun uploadAppIconsInBackground(uid: String, apps: List<AllowedApp>)
}