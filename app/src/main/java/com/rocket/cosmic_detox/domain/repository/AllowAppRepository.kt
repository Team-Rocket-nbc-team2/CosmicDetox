package com.rocket.cosmic_detox.domain.repository

import com.rocket.cosmic_detox.data.datasource.remote.model.AllowedApp
import com.rocket.cosmic_detox.data.datasource.remote.model.CheckedApp
import kotlinx.coroutines.flow.Flow

interface AllowAppRepository {

    fun getInstalledApps(): Flow<List<CheckedApp>>

    suspend fun updateAllowedApps(originApps: List<AllowedApp>, updatedApps: List<AllowedApp>): Result<Boolean>
}