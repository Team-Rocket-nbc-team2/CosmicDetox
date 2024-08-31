package com.rocket.cosmic_detox.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.rocket.cosmic_detox.data.model.User
import com.rocket.cosmic_detox.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : UserRepository {

    override fun getUserData(successCallback: (User) -> Unit, failCallback: (Exception?) -> Unit) {
        val fireStoreRef = fireStore.collection("users")
            .document(firebaseAuth.currentUser?.uid ?: "chanhue")

        fireStoreRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                val user = document.toObject<User>()
                if (user != null) {
                    successCallback(user)
                } else {
                    failCallback(Exception("유저가 존재하지 않음"))
                }
            } else {
                failCallback(task.exception)
            }
        }
    }

    override fun saveUserData(user: User, successCallback: () -> Unit, failCallback: (Exception?) -> Unit) {
        val fireStoreRef = fireStore.collection("users")
            .document(firebaseAuth.currentUser?.uid ?: "chanhue")

        fireStoreRef.set(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                successCallback()
            } else {
                failCallback(task.exception)
            }
        }
    }

    override fun updateTotalTime(totalTime: Long, successCallback: () -> Unit, failCallback: (Exception?) -> Unit) {
        val fireStoreRef = fireStore.collection("users")
            .document(firebaseAuth.currentUser?.uid ?: "chanhue")

        fireStoreRef.update("totalTime", totalTime).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                successCallback()
            } else {
                failCallback(task.exception)
            }
        }
    }

    override fun getTotalTime(successCallback: (Long) -> Unit, failCallback: (Exception?) -> Unit) {
        val fireStoreRef = fireStore.collection("users")
            .document(firebaseAuth.currentUser?.uid ?: "chanhue")

        fireStoreRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                val totalTime = document.getLong("totalTime") ?: 0L
                successCallback(totalTime)
            } else {
                failCallback(task.exception)
            }
        }
    }
}
