package com.rocket.cosmic_detox.data.datasource.local

import com.rocket.cosmic_detox.data.datasource.local.allowedapp.AllowedAppLocalDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalDataSourceModule {

    @Binds
    @Singleton
    abstract fun bindAllowedAppLocalDataSource(impl: AllowedAppLocalDataSourceImpl): AllowedAppLocalDataSource
}