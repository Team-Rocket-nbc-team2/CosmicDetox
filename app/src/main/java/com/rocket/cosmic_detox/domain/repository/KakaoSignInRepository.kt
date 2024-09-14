package com.rocket.cosmic_detox.domain.repository

interface KakaoSignInRepository {
    fun kakaoLogin(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}