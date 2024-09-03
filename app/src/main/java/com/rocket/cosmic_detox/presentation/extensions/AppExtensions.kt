package com.rocket.cosmic_detox.presentation.extensions

import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.CheckedApp

fun CheckedApp.toAllowedApp(): AllowedApp {
    return AllowedApp(
        packageId = packageId,
        appName = appName,
        limitedTime = limitedTime,
    )
}

infix fun CheckedApp.has(app: AllowedApp) = packageId == app.packageId