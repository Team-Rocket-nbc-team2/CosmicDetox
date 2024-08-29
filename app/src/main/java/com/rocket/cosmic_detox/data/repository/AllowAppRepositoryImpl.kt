package com.rocket.cosmic_detox.data.repository

import android.content.pm.PackageManager
import android.util.Log
import com.rocket.cosmic_detox.domain.repository.AllowAppRepository
import com.rocket.cosmic_detox.presentation.model.App
import javax.inject.Inject

class AllowAppRepositoryImpl @Inject constructor(
    private val packageManager: PackageManager
) : AllowAppRepository {

    override fun getInstalledApps(): List<App> {
        val apps = mutableListOf<App>()
        val packages = packageManager.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES)
        Log.d("AllowAppRepositoryImpl", "packages: $packages")
        for (packageInfo in packages) {
            Log.d("AllowAppRepositoryImpl", "packageInfo: $packageInfo")
            val app = App(
                packageId = packageInfo.packageName,
                appName = packageInfo.applicationInfo.loadLabel(packageManager).toString(),
                appIcon = packageInfo.applicationInfo.loadIcon(packageManager) ?: null,
                limitedTime = 0
            )
            apps.add(app)
        }
        Log.d("AllowAppRepositoryImpl", "apps: ${apps.size}")
        return apps
    }
}