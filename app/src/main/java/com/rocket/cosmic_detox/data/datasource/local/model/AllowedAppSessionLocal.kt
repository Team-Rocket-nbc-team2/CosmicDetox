package com.rocket.cosmic_detox.data.datasource.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rocket.cosmic_detox.data.datasource.local.RoomConstants

@Entity(tableName = RoomConstants.Table.ALLOWED_APP_SESSION)
data class AllowedAppSessionLocal(
    @PrimaryKey val packageId: String,
    val remainTime: Long,
    val lastUpdated: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)