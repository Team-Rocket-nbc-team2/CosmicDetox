package com.rocket.cosmic_detox.domain.usecase

import com.rocket.cosmic_detox.data.model.User
import com.rocket.cosmic_detox.domain.repository.UserRepository
import javax.inject.Inject

class GetUserDataUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(callback: (User) -> Unit, failCallback: (Exception?) -> Unit) {
        userRepository.getUserData(callback, failCallback)
    }
}
