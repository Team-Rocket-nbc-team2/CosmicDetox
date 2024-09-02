package com.rocket.cosmic_detox.domain.usecase.timer


import com.rocket.cosmic_detox.domain.repository.UserRepository
import javax.inject.Inject

class UpdateTotalTimeUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(totalTime: Long, callback: () -> Unit, failCallback: (Exception?) -> Unit) {
        userRepository.updateTotalTime(totalTime, callback, failCallback)
    }
}
