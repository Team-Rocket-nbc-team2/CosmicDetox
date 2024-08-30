package com.rocket.cosmic_detox.domain.repository

interface SignInRepository {
    fun googleLogin()

    fun kakaoLogin()

    fun xLogin()
}