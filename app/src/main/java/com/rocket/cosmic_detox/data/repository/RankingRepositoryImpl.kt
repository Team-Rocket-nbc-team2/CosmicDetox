package com.rocket.cosmic_detox.data.repository


import com.google.firebase.firestore.FirebaseFirestore
import com.rocket.cosmic_detox.domain.repository.RankingRepository
import javax.inject.Inject

class RankingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RankingRepository {
    override fun updateTotalTime(
        userUID: String,
        totalTime: Long,
        successCallback: () -> Unit,
        failCallback: (Exception?) -> Unit
    ) {
        val rankingRef = firestore.collection("season")
            .document("season-2024-08")
            .collection("ranking").document(userUID)
        rankingRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {

                // 기존 totalTime을 더하지 않고, 전달된 totalTime을 그대로 업데이트
                rankingRef.update("totalTime", totalTime)
                    .addOnSuccessListener {
                        successCallback()
                    }
                    .addOnFailureListener { exception ->
                        failCallback(exception)
                    }
            }
        }.addOnFailureListener { exception ->
            failCallback(exception)
        }
    }
}
