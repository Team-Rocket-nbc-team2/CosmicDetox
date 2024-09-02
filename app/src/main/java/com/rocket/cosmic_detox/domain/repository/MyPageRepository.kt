package com.rocket.cosmic_detox.domain.repository

import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.AppUsage
import com.rocket.cosmic_detox.data.model.User
import kotlinx.coroutines.flow.Flow

interface MyPageRepository {

    fun getMyInfo(): Flow<User>

    fun getMyAppUsage(): Flow<List<AppUsage>>

    suspend fun updateAppUsageLimit(allowedApp: AllowedApp): Result<Boolean>
}