package com.rocket.cosmic_detox.data.datasource.season

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rocket.cosmic_detox.data.model.RankingInfo
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SeasonDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SeasonDataSource {

    override suspend fun getMyRank(uid: String, seasonId: String): Result<Int> {
        return runCatching {
            val seasonRef = firestore.collection("season").document(seasonId).collection("ranking")
            val querySnapshot = seasonRef.orderBy("totalTime", Query.Direction.DESCENDING).get().await()

            val rankingList = querySnapshot.toObjects(RankingInfo::class.java).sortedWith(compareByDescending<RankingInfo> { it.totalTime }.thenBy { it.name })

            val rank = rankingList.indexOfFirst { it.uid == uid } + 1

            if (rank > 0) {
                rank
            } else {
                throw Exception("Uid 존재 하지 않음: $uid")
            }
        }.onFailure {
            Log.e("SeasonDataSourceImpl", "내 랭크 가져오는 거에서 에러: ${it.message}", it)
        }
    }
}