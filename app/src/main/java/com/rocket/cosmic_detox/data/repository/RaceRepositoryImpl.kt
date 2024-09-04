package com.rocket.cosmic_detox.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.Ranking
import com.rocket.cosmic_detox.domain.repository.RaceRepository
import com.rocket.cosmic_detox.data.model.RankingInfo
import com.rocket.cosmic_detox.data.remote.firebase.season.SeasonDataSource
import com.rocket.cosmic_detox.data.remote.firebase.user.UserDataSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class RaceRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val userDataSource: UserDataSource,
    private val seasonDataSource: SeasonDataSource
) : RaceRepository {

    override fun getRanking(): Flow<List<RankingInfo>> = callbackFlow {
        val fireStoreRef = db.collection("season")
            .document("season-2024-08")
            .collection("ranking")

        fireStoreRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val docs = task.result?.documents?.mapNotNull {
                    it.toObject<RankingInfo>()
                } ?: emptyList()
                // 추후에 포인트 제도로 변경 시 totalTime point로 변경
                trySend(docs.sortedByDescending { it.totalTime })
            } else {
                Log.e("ggil", "실패")
            }
        }
        awaitClose() // 추가된 부분, 플로우가 종료될 때까지 대기
    }

    override suspend fun getMyRank(): Result<Int> {
        val uid = userDataSource.getUid()
        val seasonId = "season-2024-08" // 실제 시즌 ID를 동적으로 설정하거나 하드코딩할 수 있음

        return seasonDataSource.getMyRank(uid, seasonId)
    }
}