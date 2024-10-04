package com.rocket.cosmic_detox.data.datasource.season

interface SeasonDataSource {

    suspend fun getMyRank(uid: String, seasonId: String): Result<Int>
}