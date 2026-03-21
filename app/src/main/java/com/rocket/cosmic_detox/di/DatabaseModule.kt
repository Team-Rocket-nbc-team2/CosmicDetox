package com.rocket.cosmic_detox.di

import android.content.Context
import androidx.room.Room
import com.rocket.cosmic_detox.data.datasource.local.CdDatabase
import com.rocket.cosmic_detox.data.datasource.local.RoomConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCdDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(
            context,
            CdDatabase::class.java,
            RoomConstants.ROOM_DB_NAME
        ).build()

    @Provides
    @Singleton
    fun provideAllowedAppSessionDao(cdDatabase: CdDatabase) = cdDatabase.allowedAppSessionDao()
}