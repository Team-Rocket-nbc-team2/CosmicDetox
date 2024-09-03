package com.rocket.cosmic_detox.domain.usecase.timer

import com.rocket.cosmic_detox.domain.repository.AllowedAppRepository
import javax.inject.Inject

class UpdateLimitedTimeAppUseCase @Inject constructor(
    private val allowedAppRepository: AllowedAppRepository
) {
    operator fun invoke(packageId: String, remainTime: Int, failCallback: (Throwable?) -> Unit) {
        allowedAppRepository.updateLimitedTimeAllowApp(packageId, remainTime, failCallback)
    }
}