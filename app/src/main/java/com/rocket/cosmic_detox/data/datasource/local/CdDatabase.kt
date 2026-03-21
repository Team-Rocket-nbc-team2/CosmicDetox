package com.rocket.cosmic_detox.data.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rocket.cosmic_detox.data.datasource.local.model.AllowedAppSessionLocal

@Database(entities = [AllowedAppSessionLocal::class], version = RoomConstants.ROOM_VERSION)
abstract class CdDatabase : RoomDatabase() {

    abstract fun allowedAppSessionDao(): AllowedAppSessionDao
}