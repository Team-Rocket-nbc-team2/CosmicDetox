package com.rocket.cosmic_detox.di

import com.rocket.cosmic_detox.data.datasource.season.SeasonDataSource
import com.rocket.cosmic_detox.data.datasource.season.SeasonDataSourceImpl
import com.rocket.cosmic_detox.data.datasource.user.UserDataSource
import com.rocket.cosmic_detox.data.datasource.user.UserDataSourceImpl
import com.rocket.cosmic_detox.data.repository.AllowAppRepositoryImpl
import com.rocket.cosmic_detox.data.repository.AllowedAppRepositoryImpl
import com.rocket.cosmic_detox.data.repository.KakaoSignInRepositoryImpl
import com.rocket.cosmic_detox.data.repository.MyPageRepositoryImpl
import com.rocket.cosmic_detox.data.repository.RaceRepositoryImpl
import com.rocket.cosmic_detox.data.repository.RankingRepositoryImpl
import com.rocket.cosmic_detox.data.repository.SignInRepositoryImpl
import com.rocket.cosmic_detox.data.repository.UserRepositoryImpl
import com.rocket.cosmic_detox.domain.repository.AllowAppRepository
import com.rocket.cosmic_detox.domain.repository.AllowedAppRepository
import com.rocket.cosmic_detox.domain.repository.KakaoSignInRepository
import com.rocket.cosmic_detox.domain.repository.MyPageRepository
import com.rocket.cosmic_detox.domain.repository.RaceRepository
import com.rocket.cosmic_detox.domain.repository.RankingRepository
import com.rocket.cosmic_detox.domain.repository.SignInRepository
import com.rocket.cosmic_detox.domain.repository.UserRepository
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
    abstract fun allowAppRepository(allowAppRepositoryImpl: AllowAppRepositoryImpl): AllowAppRepository

    @Binds
    abstract fun signInRepository(signInRepositoryImpl: SignInRepositoryImpl): SignInRepository

    @Binds
    abstract fun bindMyPageRepository(myPageRepositoryImpl: MyPageRepositoryImpl): MyPageRepository

    @Binds
    abstract fun bindUserDataSource(userDataSourceImpl: UserDataSourceImpl): UserDataSource

    @Binds
    abstract fun bindSeasonDataSource(seasonDataSourceImpl: SeasonDataSourceImpl): SeasonDataSource

    @Binds
    abstract fun bindUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindRankingRepository(rankingRepositoryImpl: RankingRepositoryImpl): RankingRepository

    @Binds
    abstract fun bindKakaoSignInRepository(kakaoSignInRepositoryImpl: KakaoSignInRepositoryImpl): KakaoSignInRepository
}
