package com.rocket.cosmic_detox.data.repository

import android.app.usage.UsageStatsManager
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.auth.FirebaseAuth
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.AppUsage
import com.rocket.cosmic_detox.data.model.Trophy
import com.rocket.cosmic_detox.data.model.User
import com.rocket.cosmic_detox.data.datasource.user.UserDataSource
import com.rocket.cosmic_detox.domain.repository.MyPageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MyPageRepositoryImpl @Inject constructor(
    private val userDataSource: UserDataSource,
    private val usageStatsManager: UsageStatsManager,
    private val packageManager: PackageManager,
    private val firebaseAuth: FirebaseAuth,
) : MyPageRepository {

    override suspend fun getUid(): String {
        return userDataSource.getUid()
    }

    override suspend fun getUserInfo(uid: String): Flow<User> = flow {
        val userResult = userDataSource.getUserInfo(uid)
        emit(userResult.getOrElse { User() })
    }

    override suspend fun getUserApps(uid: String): Flow<List<AllowedApp>> = flow {
        val appsResult = userDataSource.getUserApps(uid)
        emit(appsResult.getOrElse { emptyList() })
    }

    override suspend fun getUserTrophies(uid: String): Flow<List<Trophy>> = flow {
        val trophiesResult = userDataSource.getUserTrophies(uid)
        emit(trophiesResult.getOrElse { emptyList() })
    }

//    override suspend fun getMyInfo(): Flow<User> = flow {
//        val uid = userDataSource.getUid()
//        //val uid = "test2" // TODO: 나중에 uid로 수정
//
//        val userResult = userDataSource.getUserInfo(uid) // TODO: onSuccess, onFailure 처리를 해줘야 하나?
//        val appsResult = userDataSource.getUserApps(uid)
//        val trophiesResult = userDataSource.getUserTrophies(uid)
//
//        val user = userResult.getOrElse { User() }
//        val apps = appsResult.getOrElse { emptyList() }
//        val trophies = trophiesResult.getOrElse { emptyList() }
//
//        emit(user.copy(apps = apps, trophies = trophies))
//    }

    override fun getMyAppUsage(): Flow<List<AppUsage>> = flow {
        val endTime = System.currentTimeMillis()
        //val startTime = endTime - (1000 * 60 * 60 * 24 * 7) // 일주일 동안의 데이터
        val startTime = endTime - (1000 * 60 * 60 * 24) // 하루(24시간) 동안의 데이터

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        if (usageStats.isNullOrEmpty()) {
            emit(emptyList())
        } else {
            val sortedUsageStats = usageStats
                .filter {
                    it.totalTimeInForeground > 0 && packageManager.getLaunchIntentForPackage(it.packageName) != null // 실행 가능한 앱만 필터링
                }
                .groupBy { it.packageName }
                .mapValues { entry ->
                    entry.value.sumOf { it.totalTimeInForeground } // 동일한 앱의 사용 시간을 합산
                }
                .toList()
                .sortedByDescending { it.second } // 합산된 사용 시간을 기준으로 정렬
                .take(5) // 많이 사용한 앱 5개만 가져옴

            // 최대 사용 시간을 기준으로 usagePercentage 계산
            val maxUsageTime = sortedUsageStats.maxOfOrNull { it.second } ?: 1L // 0으로 나누는 것을 방지하기 위해 1로 설정

            val appUsageList = sortedUsageStats.map { (packageId, usageTime) ->
                val appName = packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(packageId, 0)
                ).toString()
                val appIcon = packageManager.getApplicationIcon(packageId).toBitmap()

                // 사용 비율 계산
                val usagePercentage = (usageTime.toDouble() / maxUsageTime * 100).toInt()

                AppUsage(
                    packageId = packageId,
                    appName = appName,
                    appIcon = appIcon,
                    usageTime = usageTime,
                    usagePercentage = usagePercentage
                )
            }

            Log.d("MyPageRepositoryImpl", "appUsageList: $appUsageList")

            emit(appUsageList)
        }
    }

    override suspend fun updateAppUsageLimit(allowedApp: AllowedApp): Result<Boolean> {
        val uid = userDataSource.getUid()
        return userDataSource.updateAppUsageLimit(uid, allowedApp)
    }
}