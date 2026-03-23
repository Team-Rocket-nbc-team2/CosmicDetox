package com.rocket.cosmic_detox.domain.repository

import com.rocket.cosmic_detox.data.datasource.remote.model.AllowedApp

interface AllowedAppRepository {
    fun getAllowedApps(callback: (List<AllowedApp>) -> Unit, failCallback: (Throwable?) -> Unit)

    fun updateLimitedTimeAllowApp(
        packageId: String,
        remainTime: Int,
        callback: () -> Unit,
        failCallback: (Throwable?) -> Unit
    )
}
