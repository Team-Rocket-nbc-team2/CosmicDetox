package com.rocket.cosmic_detox.data.repository

import android.app.usage.UsageStatsManager
import android.content.pm.PackageManager
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.AppUsage
import com.rocket.cosmic_detox.data.model.Trophy
import com.rocket.cosmic_detox.data.model.User
import com.rocket.cosmic_detox.data.remote.firebase.user.UserDataSource
import com.rocket.cosmic_detox.domain.repository.MyPageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MyPageRepositoryImpl @Inject constructor(
    private val userDataSource: UserDataSource,
    private val usageStatsManager: UsageStatsManager,
    private val packageManager: PackageManager,
    private val firebaseAuth: FirebaseAuth // 유저 ID를 얻기 위해 사용
) : MyPageRepository {

    override fun getMyInfo(): Flow<User> = flow {
        //val uid = firebaseAuth.currentUser?.uid ?: "test2" // TODO: 나중에 uid로 수정
        val uid = "test2" // TODO: 나중에 uid로 수정

        val userResult = userDataSource.getUserInfo(uid)
        val appsResult = userDataSource.getUserApps(uid)
        val trophiesResult = userDataSource.getUserTrophies(uid)

        val user = userResult.getOrNull() ?: User()
        val apps = appsResult.getOrNull() ?: emptyList()
        val trophies = trophiesResult.getOrNull() ?: emptyList()

        emit(user.copy(apps = apps, trophies = trophies))
    }.flowOn(Dispatchers.IO)

    override fun getMyAppUsage(): Flow<List<AppUsage>> = flow {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (1000 * 60 * 60 * 24 * 7) // 일주일 동안의 데이터 -> TODO: 나중에 당일로 수정

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        if (usageStats.isNullOrEmpty()) {
            emit(emptyList())
        } else {
            val sortedUsageStats = usageStats
                .filter { it.totalTimeInForeground > 0 }
                .sortedByDescending { it.totalTimeInForeground }
                .take(5) // 많이 사용한 앱 5개만 가져옴

            val appUsageList = sortedUsageStats.map { usageStat ->
                val packageId = usageStat.packageName
                val appName = packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(packageId, 0)
                ).toString()
                val appIcon = packageManager.getApplicationIcon(packageId)
                val usageTime = usageStat.totalTimeInForeground

                AppUsage(
                    packageId = packageId,
                    appName = appName,
                    appIcon = appIcon,
                    usageTime = usageTime
                )
            }

            emit(appUsageList)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateAppUsageLimit(allowedApp: AllowedApp): Result<Boolean> {
        //val uid = firebaseAuth.currentUser?.uid ?: "test2" // TODO: 나중에 uid로 수정
        val uid = "test2"
        return userDataSource.updateAppUsageLimit(uid, allowedApp)
    }
}