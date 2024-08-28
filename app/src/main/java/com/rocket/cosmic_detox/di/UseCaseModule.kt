package com.rocket.cosmic_detox.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {

//    @Provides
//    @Singleton
//    fun usecase(repository: Repository): UseCase {
//        return UseCase(repotiory)
//    }
}