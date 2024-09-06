package com.rocket.cosmic_detox.domain.repository


interface RankingRepository {
    fun updateTotalTime(userUID: String, totalTime: Long, successCallback: () -> Unit, failCallback: (Exception?) -> Unit)
}
