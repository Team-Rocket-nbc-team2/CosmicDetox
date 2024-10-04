package com.rocket.cosmic_detox.domain.usecase.user

import com.rocket.cosmic_detox.domain.repository.UserRepository
import javax.inject.Inject

class DeleteUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        userRepository.deleteUser(onSuccess, onFailure)
    }
}