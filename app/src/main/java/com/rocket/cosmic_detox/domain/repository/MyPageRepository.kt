package com.rocket.cosmic_detox.domain.repository

import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.AppUsage
import com.rocket.cosmic_detox.data.model.Trophy
import com.rocket.cosmic_detox.data.model.User
import kotlinx.coroutines.flow.Flow

interface MyPageRepository {

    suspend fun getUid(): String

    suspend fun getUserInfo(uid: String): Flow<User>

    suspend fun getUserApps(uid: String): Flow<List<AllowedApp>>

    suspend fun getUserTrophies(uid: String): Flow<List<Trophy>>

    fun getMyAppUsage(): Flow<List<AppUsage>>

    suspend fun updateAppUsageLimit(allowedApp: AllowedApp): Result<Boolean>
}