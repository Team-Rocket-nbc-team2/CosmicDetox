package com.rocket.cosmic_detox.di

import com.rocket.cosmic_detox.data.repository.AllowAppRepositoryImpl
import com.rocket.cosmic_detox.domain.repository.AllowAppRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AllowAppRepositoryModule {

    @Binds
    abstract fun allowAppRepository(
        allowAppRepositoryImpl: AllowAppRepositoryImpl
    ): AllowAppRepository
}