package com.rocket.cosmic_detox.data.datasource.local

import com.rocket.cosmic_detox.data.datasource.local.allowedapp.AllowedAppLocalDataSource
import com.rocket.cosmic_detox.data.datasource.local.model.AllowedAppSessionLocal
import javax.inject.Inject

class AllowedAppLocalDataSourceImpl @Inject constructor(
    private val allowedAppSessionDao: AllowedAppSessionDao
) : AllowedAppLocalDataSource {

    override suspend fun upsertSession(session: AllowedAppSessionLocal) {
        allowedAppSessionDao.upsert(session)
    }

    override suspend fun getSession(packageId: String): AllowedAppSessionLocal? =
        allowedAppSessionDao.getSession(packageId)

    override suspend fun getUnsyncedSessions(): List<AllowedAppSessionLocal> =
        allowedAppSessionDao.getUnsyncedSessions()

    override suspend fun markAsSynced(packageId: String) {
        allowedAppSessionDao.markAsSynced(packageId)
    }

    override suspend fun deleteSession(packageId: String) {
        allowedAppSessionDao.delete(packageId)
    }
}