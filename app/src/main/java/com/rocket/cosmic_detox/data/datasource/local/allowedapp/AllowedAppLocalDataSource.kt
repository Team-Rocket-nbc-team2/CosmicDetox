package com.rocket.cosmic_detox.data.datasource.local.allowedapp

import com.rocket.cosmic_detox.data.datasource.local.model.AllowedAppSessionLocal

interface AllowedAppLocalDataSource {

    suspend fun upsertSession(session: AllowedAppSessionLocal)
    suspend fun getSession(packageId: String): AllowedAppSessionLocal?
    suspend fun getUnsyncedSessions(): List<AllowedAppSessionLocal>
    suspend fun markAsSynced(packageId: String)
    suspend fun deleteSession(packageId: String)
}