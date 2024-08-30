package com.rocket.cosmic_detox.di

import com.rocket.cosmic_detox.data.repository.AllowedAppRepositoryImpl
import com.rocket.cosmic_detox.domain.repository.AllowedAppRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAllowAppRepository(allowedAppRepositoryImpl: AllowedAppRepositoryImpl): AllowedAppRepository
}