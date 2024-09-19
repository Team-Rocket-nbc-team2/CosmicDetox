package com.rocket.cosmic_detox.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.CheckedApp
import com.rocket.cosmic_detox.data.datasource.user.UserDataSource
import com.rocket.cosmic_detox.domain.repository.AllowAppRepository
import com.rocket.cosmic_detox.util.AppCategoryManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AllowAppRepositoryImpl @Inject constructor(
    private val packageManager: PackageManager,
    private val userDataSource: UserDataSource,
    @ApplicationContext private val context: Context
) : AllowAppRepository {

//    override fun getInstalledApps(): Flow<List<CheckedApp>> = flow {
//        val apps = mutableListOf<CheckedApp>()
//        val packages = packageManager.getInstalledPackages(0)
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
//                    val installerPackageName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                        val installSourceInfo = packageManager.getInstallSourceInfo(packageName)
//                        installSourceInfo.installingPackageName
//                    } else {
//                        // Android 11 (API 30) 이전 버전에서는 getInstallerPackageName 사용
//                        packageManager.getInstallerPackageName(packageName)
//                    }
//
//                    // 기본 앱(시스템 앱) 또는 Google Play 스토어, 갤럭시 스토어, 원스토어에서 설치된 앱만 포함
//                    if (isSystemApp || isUpdatedSystemApp
//                        || installerPackageName == "com.android.vending"
//                        || installerPackageName == "com.sec.android.app.samsungapps"
//                        || installerPackageName == "com.skt.skaf.OA00000000") {
//                        val app = CheckedApp(
//                            packageId = packageName,
//                            appName = packageInfo.applicationInfo.loadLabel(packageManager).toString(),
//                            limitedTime = 0
//                        )
//                        apps.add(app)
//                    }
//                } catch (e: IllegalArgumentException) {
//                    Log.e("AllowAppRepositoryImpl", "앱 정보를 가져오는 중 에러 발생", e)
//                }
//            }
//        }
//        emit(apps.sortedBy { it.appName })
//    }

    override fun getInstalledApps(): Flow<List<CheckedApp>> = flow {
        val apps = mutableListOf<CheckedApp>()
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList = context.packageManager.queryIntentActivities(intent, 0)

        // 앱 목록을 순회하여 CheckedApp 리스트 생성
        resolveInfoList.map {
            val packageName = it.activityInfo.packageName
            val appName = context.getAppNameFromPackageName(packageName)
            // 앱 아이콘을 Drawable로 로드 -> Bitmap으로 변환
            val appIcon = it.loadIcon(context.packageManager).toBitmap()

            // 시스템 앱을 필터링
            if (!context.isSystemPackage(packageName) && packageName != context.packageName) { // 시스템 앱, 우리 앱은 제외
                val app = CheckedApp(
                    packageId = packageName,
                    appName = appName,
                    appIcon = appIcon,
                    limitedTime = 0
                )
                apps.add(app)
            }
        }

        emit(apps.distinct().sortedBy { it.appName }) // 앱 목록을 정렬하여 emit
    }


    override suspend fun updateAllowedApps(originApps: List<AllowedApp>, updatedApps: List<AllowedApp>): Result<Boolean> {
        val uid = userDataSource.getUid()

        // originApps와 updatedApps를 비교하여 없어진 앱을 삭제하고 추가된 앱을 추가
        val deletedApps = originApps.filter { originApp -> updatedApps.none { it.packageId == originApp.packageId } }
        var addedApps = updatedApps.filter { updatedApp -> originApps.none { it.packageId == updatedApp.packageId } }

        // 삭제할 앱이 있는 경우 삭제
        if (deletedApps.isNotEmpty()) {
            val deletedAppIds = deletedApps.map { it.packageId }
            val result = userDataSource.deleteAllowedApps(uid, deletedAppIds)
            if (result.isFailure) {
                Log.e("AllowAppRepositoryImpl", "허용 앱 삭제 실패", result.exceptionOrNull())
                return result // 삭제 작업이 실패하면 실패 결과 반환
            }
        }

        // 추가할 앱의 카테고리 기반으로 제한 시간을 설정
        addedApps = addedApps.map { addedApp ->
            val packageInfo = packageManager.getApplicationInfo(addedApp.packageId, 0)
            val appCategory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                packageInfo.category
            } else {
                ApplicationInfo.CATEGORY_UNDEFINED
            }

            // 카테고리별 제한 시간을 설정
            val limitedTime = AppCategoryManager.getLimitedTimeByCategory(appCategory)

            // 제한 시간을 설정한 새 객체로 변환
            addedApp.copy(limitedTime = limitedTime)
        }

        // 추가할 앱이 있는 경우 추가
        if (addedApps.isNotEmpty()) {
            val result = userDataSource.addAllowedApps(uid, addedApps)
            if (result.isFailure) {
                Log.e("AllowAppRepositoryImpl", "허용 앱 추가 실패", result.exceptionOrNull())
                return result // 추가 작업이 실패하면 해당 실패 결과 반환
            }

            // 아이콘 업로드는 백그라운드에서 진행
            userDataSource.uploadAppIconsInBackground(uid, addedApps)
        }

        // 모든 작업이 성공적으로 완료되면 성공 결과 반환
        return Result.success(true)
    }
}

private fun Context.isSystemPackage(packageName: String): Boolean {
    try {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        return packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e("isSystemPackage", e.toString())
    }
    return false
}

private fun Context.getAppNameFromPackageName(packageName: String): String = try {
    val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    packageManager.getApplicationLabel(appInfo).toString()
} catch (e: PackageManager.NameNotFoundException) {
    "Unknown"
}