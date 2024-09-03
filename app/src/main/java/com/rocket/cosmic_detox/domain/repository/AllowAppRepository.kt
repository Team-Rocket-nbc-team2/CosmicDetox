package com.rocket.cosmic_detox.domain.repository

import com.rocket.cosmic_detox.data.model.AllowedApp
import kotlinx.coroutines.flow.Flow

interface AllowAppRepository {

    fun getInstalledApps(): Flow<List<AllowedApp>>

    suspend fun updateAllowedApps(apps: List<AllowedApp>): Result<Boolean>
}