package com.rocket.cosmic_detox.data.datasource.user

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.Trophy
import com.rocket.cosmic_detox.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Date
import javax.inject.Inject

class UserDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val packageManager: PackageManager
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
//        return withContext(Dispatchers.IO) {
//            runCatching {
//                val userDocRef = firestore.collection("users").document(uid)
//                val batch = firestore.batch()
//
//                // 모든 앱의 아이콘을 Firebase Storage에 비동기로 업로드
//                val uploadTasks = apps.map { app ->
//                    async {
//                        val appIconBitmap = packageManager.getApplicationIcon(app.packageId).toBitmap()
//                        val imageUrl = uploadAppIconToStorage(uid, app.packageId, appIconBitmap) // Firebase Storage에 업로드 후 URL 반환
//                        Log.d("UserDataSourceImpl", "imageUrl: $imageUrl")
//                        app.copy(appIcon = imageUrl) // 앱 객체에 이미지 URL을 포함
//                    }
//                }
//
//                val updatedApps = uploadTasks.awaitAll()
//
//                // Firestore에 일괄 저장
//                updatedApps.forEach { app ->
//                    val appDocRef = userDocRef.collection("apps").document(app.packageId)
//                    batch.set(appDocRef, app) // 업데이트된 앱 정보로 일괄 설정
//                }
//
////            apps.forEach { app ->
////                val appIconBitmap = packageManager.getApplicationIcon(app.packageId).toBitmap()
////                val imageUrl = uploadAppIconToStorage(uid, app.packageId, appIconBitmap)
////
////                val appDocRef = userDocRef.collection("apps").document(app.packageId)
////                batch.set(appDocRef, app.copy(appIcon = imageUrl)) // 새로운 앱 추가 (덮어쓰기)
////            }
//
//                batch.commit().await()
//                true
//            }.onFailure {
//                Log.e("UserDataSourceImpl", "허용 앱 추가 실패", it)
//            }
//        }
        return runCatching {
            val userDocRef = firestore.collection("users").document(uid)
            val batch = firestore.batch()

            // 아이콘을 비워서 먼저 Firestore에 저장
            apps.forEach { app ->
                val appWithoutIcon = app.copy(appIcon = "")
                val appDocRef = userDocRef.collection("apps").document(app.packageId)
                batch.set(appDocRef, appWithoutIcon) // 아이콘 없이 데이터 저장
            }

            // 일괄 커밋 실행
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

    override suspend fun uploadAppIconsInBackground(uid: String, apps: List<AllowedApp>) {
        withContext(Dispatchers.IO) {
            try {
                // 모든 앱에 대한 이미지 업로드 작업을 비동기적으로 처리
                val uploadTasks = apps.map { app ->
                    async {
                        val appIconBitmap = packageManager.getApplicationIcon(app.packageId).toBitmap() // 앱 아이콘을 Bitmap으로 변환
                        val imageUrl = uploadAppIconToStorage(uid, app.packageId, appIconBitmap) // Firebase Storage에 업로드 후 URL 반환
                        app.packageId to imageUrl // packageId와 업로드된 이미지 URL 반환
                    }
                }

                // 모든 업로드 작업이 완료될 때까지 기다림
                val uploadedResults = uploadTasks.awaitAll()

                // Firestore에 일괄적으로 업데이트
                val batch = firestore.batch()
                uploadedResults.forEach { (packageId, imageUrl) ->
                    val appDocRef = firestore.collection("users").document(uid).collection("apps").document(packageId)
                    batch.update(appDocRef, "appIcon", imageUrl)
                }
                batch.commit().await() // Firestore에 일괄 커밋
            } catch (e: Exception) {
                Log.e("UserDataSourceImpl", "이미지 업로드 실패", e)
            }
        }
    }

    private suspend fun uploadAppIconToStorage(uid: String, packageId: String, iconBitmap: Bitmap): String {
        return withContext(Dispatchers.IO) {
            val storageRef = FirebaseStorage.getInstance().reference
            val appIconRef = storageRef.child("users/$uid/apps/$packageId/icon.jpg")

            // 비트맵을 ByteArray로 변환
            val baos = ByteArrayOutputStream()
            iconBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            // Firebase Storage에 이미지 업로드
            appIconRef.putBytes(data).await()

            // 업로드한 이미지의 다운로드 URL을 반환
            appIconRef.downloadUrl.await().toString()
        }
    }
}