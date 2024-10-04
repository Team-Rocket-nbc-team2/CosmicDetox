package com.rocket.cosmic_detox.domain.usecase.signin

import android.app.Activity
import com.rocket.cosmic_detox.domain.repository.SignInRepository
import javax.inject.Inject

class GoogleSignInUseCase @Inject constructor(
    private val signInRepository: SignInRepository
) {
    suspend operator fun invoke(activity: Activity, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit, onCancel: () -> Unit) {
        signInRepository.googleSignIn(activity, onSuccess, onFailure, onCancel)
    }
}