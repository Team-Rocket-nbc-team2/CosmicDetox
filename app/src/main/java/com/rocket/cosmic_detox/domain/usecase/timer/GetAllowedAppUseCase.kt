package com.rocket.cosmic_detox.domain.usecase.timer

import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.domain.repository.AllowedAppRepository
import javax.inject.Inject

class GetAllowedAppUseCase @Inject constructor(private val allowedAppRepository: AllowedAppRepository) {
    operator fun invoke(callback: (List<AllowedApp>) -> Unit, failCallback: (Throwable?) -> Unit) {
        allowedAppRepository.getAllowedApps(callback, failCallback)
    }
}