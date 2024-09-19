package com.rocket.cosmic_detox.di

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.google.firebase.auth.OAuthProvider
import com.kakao.sdk.user.UserApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun providePackageManager(@ApplicationContext context: Context): PackageManager = context.packageManager

    @Provides
    fun provideUsageStatsManager(@ApplicationContext context: Context): UsageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    @Provides
    fun provideUserApiClient(): UserApiClient = UserApiClient.instance
}