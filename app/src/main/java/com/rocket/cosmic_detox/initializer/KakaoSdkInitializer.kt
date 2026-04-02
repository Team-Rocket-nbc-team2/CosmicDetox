package com.rocket.cosmic_detox.initializer

import android.content.Context
import androidx.startup.Initializer
import com.kakao.sdk.common.KakaoSdk
import com.rocket.cosmic_detox.BuildConfig

class KakaoSdkInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        KakaoSdk.init(context, BuildConfig.KAKAO_APP_KEY)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
