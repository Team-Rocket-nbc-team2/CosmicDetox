package com.rocket.cosmic_detox.domain.usecase.twitter

import android.app.Activity
import com.rocket.cosmic_detox.domain.repository.SignInRepository
import javax.inject.Inject

class TwitterLoginUseCase @Inject constructor(
    private val signInRepository: SignInRepository
) {
    operator fun invoke(activity: Activity, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit, onCancel: () -> Unit) {
        signInRepository.twitterSignIn(activity, onSuccess, onFailure, onCancel)
    }
}