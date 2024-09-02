package com.rocket.cosmic_detox.data.remote.firebase.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.Trophy
import com.rocket.cosmic_detox.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserDataSource {

    override suspend fun getUserInfo(uid: String): Result<User> {
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
}