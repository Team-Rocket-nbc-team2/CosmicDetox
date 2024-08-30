package com.rocket.cosmic_detox.domain.usecase

import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.repository.AllowedAppRepositoryImpl
import javax.inject.Inject

class AllowedAppUseCase @Inject constructor(private val allowedAppRepositoryImpl: AllowedAppRepositoryImpl) {
    operator fun invoke(callback: (List<AllowedApp>) -> Unit, failCallback: (Exception?) -> Unit) {
        allowedAppRepositoryImpl.getAllowedApps(callback, failCallback)
    }
}