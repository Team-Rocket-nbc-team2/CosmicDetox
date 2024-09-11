package com.rocket.cosmic_detox.presentation

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.rocket.cosmic_detox.BuildConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CosmicDetoxApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, BuildConfig.KAKAO_APP_KEY)
    }
}