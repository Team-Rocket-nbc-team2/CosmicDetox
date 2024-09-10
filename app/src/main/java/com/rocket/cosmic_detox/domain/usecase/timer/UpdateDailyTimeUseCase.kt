package com.rocket.cosmic_detox.domain.usecase.timer


import com.rocket.cosmic_detox.domain.repository.UserRepository
import javax.inject.Inject

class UpdateDailyTimeUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(dailyTime: Long, callback: () -> Unit, failCallback: (Exception?) -> Unit) {
        userRepository.updateDailyTime(dailyTime, callback, failCallback)
    }
}
