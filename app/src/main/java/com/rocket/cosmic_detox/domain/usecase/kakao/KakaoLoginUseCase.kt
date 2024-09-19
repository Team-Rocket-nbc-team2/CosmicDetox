package com.rocket.cosmic_detox.domain.usecase.kakao

import com.rocket.cosmic_detox.domain.repository.SignInRepository
import javax.inject.Inject

class KakaoLoginUseCase @Inject constructor(
    private val signInRepository: SignInRepository
) {
    operator fun invoke(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        signInRepository.kakaoSignIn(onSuccess, onFailure)
    }
}