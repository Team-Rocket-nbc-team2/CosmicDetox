package com.rocket.cosmic_detox.data.datasource.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rocket.cosmic_detox.data.datasource.local.model.AllowedAppSessionLocal

@Dao
interface AllowedAppSessionDao {

    @Upsert
    suspend fun upsert(session: AllowedAppSessionLocal)

    @Query("SELECT * FROM ${RoomConstants.Table.ALLOWED_APP_SESSION} WHERE packageId = :packageId")
    suspend fun getSession(packageId: String): AllowedAppSessionLocal?

    @Query("SELECT * FROM ${RoomConstants.Table.ALLOWED_APP_SESSION} WHERE isSynced = 0")
    suspend fun getUnsyncedSessions(): List<AllowedAppSessionLocal>

    @Query("UPDATE ${RoomConstants.Table.ALLOWED_APP_SESSION} SET isSynced = 1 WHERE packageId = :packageId")
    suspend fun markAsSynced(packageId: String)

    @Query("DELETE FROM ${RoomConstants.Table.ALLOWED_APP_SESSION} WHERE packageId = :packageId")
    suspend fun delete(packageId: String)
}