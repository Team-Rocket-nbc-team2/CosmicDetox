package com.rocket.cosmic_detox.domain.repository

import android.app.Activity

interface SignInRepository {
    suspend fun googleSignIn(activity: Activity, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit, onCancel: () -> Unit)

    fun kakaoSignIn(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit, onCancel: () -> Unit)

    fun twitterSignIn(activity: Activity, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit, onCancel: () -> Unit)
}