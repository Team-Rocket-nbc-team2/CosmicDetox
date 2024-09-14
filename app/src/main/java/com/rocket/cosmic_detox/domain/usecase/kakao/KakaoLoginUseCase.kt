package com.rocket.cosmic_detox.domain.usecase.kakao

import com.rocket.cosmic_detox.domain.repository.KakaoSignInRepository
import javax.inject.Inject

class KakaoLoginUseCase @Inject constructor(
    private val kakaoSignInRepository: KakaoSignInRepository
) {
    operator fun invoke(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        kakaoSignInRepository.kakaoLogin(onSuccess, onFailure)
    }
}