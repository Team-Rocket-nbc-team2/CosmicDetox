package com.rocket.cosmic_detox.domain.usecase.timer


import com.rocket.cosmic_detox.domain.repository.UserRepository
import javax.inject.Inject

class GetDailyTimeUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(callback: (Long) -> Unit, failCallback: (Exception?) -> Unit) {
        userRepository.getDailyTime(callback, failCallback)
    }
}
