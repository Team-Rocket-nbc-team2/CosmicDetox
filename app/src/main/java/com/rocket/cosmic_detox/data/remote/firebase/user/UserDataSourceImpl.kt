package com.rocket.cosmic_detox.data.remote.firebase.user

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.Trophy
import com.rocket.cosmic_detox.data.model.User
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class UserDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserDataSource {

    override suspend fun getUid(): String {
        return firebaseAuth.currentUser?.uid ?: ""
    }

    override suspend fun getUserCreatedDate(uid: String): Date? {
        val creationTimestamp = firebaseAuth.currentUser?.metadata?.creationTimestamp
        return creationTimestamp?.let { Date(it) }
    }

    override suspend fun getUserInfo(uid: String): Result<User> { // Result는 코루틴에서 예외처리를 위한 클래스 (Result.success(true), Result.failure(exception)) -> 성공, 실패 시 로그 출력
        return runCatching {
            val userDoc = firestore.collection("users").document(uid).get().await()
            userDoc.toObject<User>() ?: User()
        }
    }

    override suspend fun getUserApps(uid: String): Result<List<AllowedApp>> {
        return runCatching {
            val appsSnapshot = firestore.collection("users")
                .document(uid)
                .collection("apps")
                .get()
                .await()
            appsSnapshot.toObjects<AllowedApp>()
        }
    }

    override suspend fun getUserTrophies(uid: String): Result<List<Trophy>> {
        return runCatching {
            val trophiesSnapshot = firestore.collection("users")
                .document(uid)
                .collection("trophies")
                .get()
                .await()
            trophiesSnapshot.toObjects<Trophy>()
        }
    }

    override suspend fun updateAppUsageLimit(uid: String, allowedApp: AllowedApp): Result<Boolean> {
        return runCatching {
            val appDocRef = firestore.collection("users")
                .document(uid)
                .collection("apps")
                .document(allowedApp.packageId)

            appDocRef.update("limitedTime", allowedApp.limitedTime).await()
            true
        }
    }

    override suspend fun updateAllowedApps(uid: String, apps: List<AllowedApp>): Result<Boolean> {
        return runCatching {
            val userDocRef = firestore.collection("users").document(uid)
            val batch = firestore.batch() //batch로 여러 개를 한번에 업로드, 부분 성공 or 부분 실패는 없음

            apps.forEach { app ->
                val appDocRef = userDocRef.collection("apps").document(app.packageId)
                batch.set(appDocRef, app) // set으로 하면 기존 데이터를 덮어씌운다
            }

            batch.commit().await()
            true
        }.onFailure {
            Log.e("UserDataSourceImpl", "허용 앱 업로드 실패", it)
        }
    }

    override suspend fun addAllowedApps(uid: String, apps: List<AllowedApp>): Result<Boolean> {
        return runCatching {
            val userDocRef = firestore.collection("users").document(uid)
            val batch = firestore.batch()

            apps.forEach { app ->
                val appDocRef = userDocRef.collection("apps").document(app.packageId)
                batch.set(appDocRef, app) // 새로운 앱 추가 (덮어쓰기)
            }

            batch.commit().await()
            true
        }.onFailure {
            Log.e("UserDataSourceImpl", "허용 앱 추가 실패", it)
        }
    }

    override suspend fun deleteAllowedApps(uid: String, appIds: List<String>): Result<Boolean> {
        return runCatching {
            val userDocRef = firestore.collection("users").document(uid)
            val batch = firestore.batch()

            appIds.forEach { appId ->
                val appDocRef = userDocRef.collection("apps").document(appId)
                batch.delete(appDocRef) // 앱 삭제
            }

            batch.commit().await()
            true
        }.onFailure {
            Log.e("UserDataSourceImpl", "허용 앱 삭제 실패", it)
        }
    }
}