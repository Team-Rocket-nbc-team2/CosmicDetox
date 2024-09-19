package com.rocket.cosmic_detox.domain.usecase.signin

import com.rocket.cosmic_detox.domain.repository.SignInRepository
import javax.inject.Inject

class GoogleSignInUseCase @Inject constructor(
    private val signInRepository: SignInRepository
) {
    suspend operator fun invoke(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        signInRepository.googleSignIn(onSuccess, onFailure)
    }
}