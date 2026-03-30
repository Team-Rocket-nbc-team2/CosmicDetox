package com.rocket.cosmic_detox.presentation.extensions

import com.rocket.cosmic_detox.data.datasource.remote.model.AllowedApp
import com.rocket.cosmic_detox.data.datasource.remote.model.InstalledApp

fun InstalledApp.toAllowedApp(): AllowedApp {
    return AllowedApp(
        packageId = packageId,
        appName = appName,
        limitedTime = limitedTime,
    )
}

infix fun InstalledApp.has(app: AllowedApp) = packageId == app.packageId