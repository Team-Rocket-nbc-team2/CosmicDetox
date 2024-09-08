package com.rocket.cosmic_detox.di

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rocket.cosmic_detox.data.repository.RankingRepositoryImpl
import com.rocket.cosmic_detox.data.repository.UserRepositoryImpl
import com.rocket.cosmic_detox.domain.repository.RankingRepository
import com.rocket.cosmic_detox.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providePackageManager(@ApplicationContext context: Context): PackageManager {
        return context.packageManager
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): UserRepository {
        return UserRepositoryImpl(firestore, auth)
    }
    @Provides
    @Singleton
    fun provideRankingRepository(
        firestore: FirebaseFirestore
    ): RankingRepository {
        return RankingRepositoryImpl(firestore)
    }


    @Singleton
    @Provides
    fun provideUsageStatsManager(@ApplicationContext context: Context): UsageStatsManager {
        return context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    @Singleton
    @Provides
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }
}