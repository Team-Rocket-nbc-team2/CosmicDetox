package com.rocket.cosmic_detox.di

import com.rocket.cosmic_detox.data.repository.AllowAppRepositoryImpl
import com.rocket.cosmic_detox.data.repository.AllowedAppRepositoryImpl
import com.rocket.cosmic_detox.data.repository.RaceRepositoryImpl
import com.rocket.cosmic_detox.domain.repository.AllowAppRepository
import com.rocket.cosmic_detox.domain.repository.AllowedAppRepository
import com.rocket.cosmic_detox.domain.repository.RaceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAllowAppRepository(allowedAppRepositoryImpl: AllowedAppRepositoryImpl): AllowedAppRepository

    @Binds
    abstract fun bindRaceRepository(raceRepositoryImpl: RaceRepositoryImpl): RaceRepository

    @Binds
    abstract fun bindAllowAppRepository(allowAppRepositoryImpl: AllowAppRepositoryImpl): AllowAppRepository
}