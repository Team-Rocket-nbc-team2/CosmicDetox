package com.rocket.cosmic_detox.data.remote.firebase.season

interface SeasonDataSource {

    suspend fun getMyRank(uid: String, seasonId: String): Result<Int>
}