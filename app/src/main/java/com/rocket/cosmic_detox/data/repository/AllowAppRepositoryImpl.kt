package com.rocket.cosmic_detox.data.repository

import android.content.pm.PackageManager
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.remote.firebase.user.UserDataSource
import com.rocket.cosmic_detox.domain.repository.AllowAppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AllowAppRepositoryImpl @Inject constructor(
    private val packageManager: PackageManager,
    private val userDataSource: UserDataSource
) : AllowAppRepository {

    override fun getInstalledApps(): Flow<List<AllowedApp>> = flow {
        val apps = mutableListOf<AllowedApp>()
        val packages = packageManager.getInstalledPackages(0)
        Log.d("AllowAppRepositoryImpl", "packages: $packages")
        for (packageInfo in packages) {
            Log.d("AllowAppRepositoryImpl", "packageInfo: $packageInfo")
            val app = AllowedApp(
                packageId = packageInfo.packageName,
                appName = packageInfo.applicationInfo.loadLabel(packageManager).toString(),
                limitedTime = 0
            )
            apps.add(app)
        }
        Log.d("AllowAppRepositoryImpl", "apps: ${apps.size}")
        emit(apps.sortedBy { it.appName })
    }.flowOn(Dispatchers.IO) // Background thread에서 작업 실행

    override suspend fun updateAllowedApps(uid: String, apps: List<AllowedApp>): Result<Boolean> {
        return userDataSource.updateAllowedApps(uid, apps)
    }
}