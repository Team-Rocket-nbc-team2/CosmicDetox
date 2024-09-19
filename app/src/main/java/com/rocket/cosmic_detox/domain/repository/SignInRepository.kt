package com.rocket.cosmic_detox.domain.repository

interface SignInRepository {
    suspend fun googleSignIn(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit, onCancel: () -> Unit)

    fun kakaoSignIn(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit, onCancel: () -> Unit)
}