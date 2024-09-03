package com.rocket.cosmic_detox.data.repository

import android.content.pm.ApplicationInfo
import android.content.pm.InstallSourceInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.remote.firebase.user.UserDataSource
import com.rocket.cosmic_detox.domain.repository.AllowAppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class AllowAppRepositoryImpl @Inject constructor(
    private val packageManager: PackageManager,
    private val userDataSource: UserDataSource
) : AllowAppRepository {

    private val commonlyUsedSystemApps = listOf(
        "com.android.contacts",       // 전화
        "com.android.mms",            // 문자
        "com.android.dialer",         // 전화 앱
        "com.android.calculator2",    // 계산기
        //"com.android.chrome",         // 크롬 브라우저
        "com.android.settings",       // 설정
        "com.android.camera",         // 카메라
        // 필요에 따라 더 추가 가능
    )


//    override fun getInstalledApps(): Flow<List<AllowedApp>> = flow {
//        val apps = mutableListOf<AllowedApp>()
//        val packages = packageManager.getInstalledPackages(0)
//        Log.d("AllowAppRepositoryImpl", "packages: $packages")
//
//        for (packageInfo in packages) {
//            val packageName = packageInfo.packageName
//            val isSystemApp = (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
//            val isUpdatedSystemApp = (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
//
//            // 앱이 실행 가능한지 확인 (런처에서 표시되는지)
//            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
//            if (launchIntent != null) {  // 앱이 실행 가능한 경우에만 처리
//                try {
//                    val installSourceInfo = packageManager.getInstallSourceInfo(packageName)
//                    val installerPackageName = installSourceInfo.installingPackageName
//
//                    // 자주 사용하는 기본 앱, Google Play 스토어, 갤럭시 스토어 또는 원스토어에서 설치된 앱만 포함
//                    if (commonlyUsedSystemApps.contains(packageName)
//                        || installerPackageName == "com.android.vending"
//                        || installerPackageName == "com.sec.android.app.samsungapps"
//                        || installerPackageName == "com.skt.skaf.OA00000000") {
//                        val app = AllowedApp(
//                            packageId = packageName,
//                            appName = packageInfo.applicationInfo.loadLabel(packageManager).toString(),
//                            limitedTime = 0
//                        )
//                        apps.add(app)
//                    }
//                } catch (e: IllegalArgumentException) {
//                    Log.e("AllowAppRepositoryImpl", "Package not installed: $packageName", e)
//                }
//            }
//        }
//        emit(apps.sortedBy { it.appName })
//    }.flowOn(Dispatchers.IO) // Background thread에서 작업 실행

    override fun getInstalledApps(): Flow<List<AllowedApp>> = flow {
        val apps = mutableListOf<AllowedApp>()
        val packages = packageManager.getInstalledPackages(0)

        for (packageInfo in packages) {
            val packageName = packageInfo.packageName
            val isSystemApp = (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystemApp = (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

            // 앱이 실행 가능한지 확인 (런처에서 표시되는지)
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {  // 앱이 실행 가능한 경우에만 처리
                try {
                    val installerPackageName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val installSourceInfo = packageManager.getInstallSourceInfo(packageName)
                        installSourceInfo.installingPackageName
                    } else {
                        // Android 11 (API 30) 이전 버전에서는 getInstallerPackageName 사용
                        packageManager.getInstallerPackageName(packageName)
                    }

                    // 기본 앱(시스템 앱) 또는 Google Play 스토어, 갤럭시 스토어, 원스토어에서 설치된 앱만 포함
                    if (isSystemApp || isUpdatedSystemApp
                        || installerPackageName == "com.android.vending"
                        || installerPackageName == "com.sec.android.app.samsungapps"
                        || installerPackageName == "com.skt.skaf.OA00000000") {
                        val app = AllowedApp(
                            packageId = packageName,
                            appName = packageInfo.applicationInfo.loadLabel(packageManager).toString(),
                            limitedTime = 0
                        )
                        apps.add(app)
                    }
                } catch (e: IllegalArgumentException) {
                    Log.e("AllowAppRepositoryImpl", "Package not installed: $packageName", e)
                }
            }
        }
        emit(apps.sortedBy { it.appName })
    }.flowOn(Dispatchers.IO) // Background thread에서 작업 실행

    override suspend fun updateAllowedApps(apps: List<AllowedApp>): Result<Boolean> {
        val uid = userDataSource.getUid()
        return userDataSource.updateAllowedApps(uid, apps)
    }
}