package com.rocket.cosmic_detox.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.domain.repository.AllowedAppRepository
import java.lang.Exception
import javax.inject.Inject

class AllowedAppRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
): AllowedAppRepository {
    override fun getAllowedApps(callback: (List<AllowedApp>) -> Unit, failCallback: (Exception?) -> Unit) {
        val fireStoreRef = fireStore.collection("users")
            .document(firebaseAuth.currentUser?.uid ?: "test1")
            .collection("apps")

        fireStoreRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val docs = task.result.documents.map {
                    it.toObject<AllowedApp>() ?: AllowedApp()
                }
                callback(docs)
            } else {
                failCallback(task.exception)
            }
        }
    }
}