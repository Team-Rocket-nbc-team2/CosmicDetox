package com.rocket.cosmic_detox.domain.usecase.timer

import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.repository.AllowedAppRepositoryImpl
import javax.inject.Inject

class GetAllowedAppUseCase @Inject constructor(private val allowedAppRepositoryImpl: AllowedAppRepositoryImpl) {
    operator fun invoke(callback: (List<AllowedApp>) -> Unit, failCallback: (Exception?) -> Unit) {
        allowedAppRepositoryImpl.getAllowedApps(callback, failCallback)
    }
}