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
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val usageStatsManager: UsageStatsManager,
    private val packageManager: PackageManager
) : MyPageRepository {

    override fun getMyInfo(): Flow<User> = flow {
        val uid = firebaseAuth.currentUser?.uid ?: "test2"
        val userDocRef = firestore.collection("users").document("test2") // TODO: 나중에 uid로 변경

        coroutineScope {
            // 사용자 기본 정보 가져오기
            val userDeferred = async(Dispatchers.IO) {
                val userDoc = userDocRef.get().await()
                userDoc.toObject<User>() ?: User()
            }

            // Apps 서브 컬렉션 가져오기
            val appsDeferred = async(Dispatchers.IO) {
                val appsSnapshot = userDocRef.collection("apps").get().await()
                appsSnapshot.toObjects<AllowedApp>()
            }

            // Trophies 서브 컬렉션 가져오기
            val trophiesDeferred = async(Dispatchers.IO) {
                val trophiesSnapshot = userDocRef.collection("trophies").get().await()
                trophiesSnapshot.toObjects<Trophy>()
            }

            // 결과 수집 및 MyInfo 객체 생성
            val user = userDeferred.await()
            val apps = appsDeferred.await()
            val trophies = trophiesDeferred.await()

            emit(user.copy(apps = apps, trophies = trophies))
        }
    }.flowOn(Dispatchers.IO)

    override fun getMyAppUsage(): Flow<List<AppUsage>> = flow {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (1000 * 60 * 60 * 24 * 7) // 일주일 동안의 데이터

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        if (usageStats.isNullOrEmpty()) {
            Log.d("jade", "getMyAppUsage: empty")
            emit(emptyList<AppUsage>())
        } else {
            val sortedUsageStats = usageStats
                .filter { it.totalTimeInForeground > 0 }
                .sortedByDescending { it.totalTimeInForeground }
                .take(5) // 상위 5개의 앱

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

            Log.d("jade", "getMyAppUsage: $appUsageList")

            emit(appUsageList)
        }
    }.flowOn(Dispatchers.IO)
}