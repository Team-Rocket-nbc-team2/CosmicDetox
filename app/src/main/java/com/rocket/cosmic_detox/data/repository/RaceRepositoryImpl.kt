package com.rocket.cosmic_detox.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rocket.cosmic_detox.domain.repository.RaceRepository
import com.rocket.cosmic_detox.data.model.RankingInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class RaceRepositoryImpl @Inject constructor(private val db : FirebaseFirestore) : RaceRepository {

    override fun getRanking(): Flow<List<RankingInfo>> = callbackFlow{
        val listenerRegistration = db.collection("season")
            .document("season-2024-08")
            .collection("ranking")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val ranking = snapshot.documents.map { doc ->
                        doc.toObject(RankingInfo::class.java)!!
                    }
                    trySend(ranking)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }
}