package com.rocket.cosmic_detox.domain.repository

import com.rocket.cosmic_detox.presentation.model.App

interface AllowAppRepository {

    fun getInstalledApps(): List<App>
}