package com.rocket.cosmic_detox.domain.repository

import com.rocket.cosmic_detox.data.model.AllowedApp

interface AllowedAppRepository {
    fun getAllowedApps(callback: (List<AllowedApp>) -> Unit, failCallback: (Throwable?) -> Unit)
}