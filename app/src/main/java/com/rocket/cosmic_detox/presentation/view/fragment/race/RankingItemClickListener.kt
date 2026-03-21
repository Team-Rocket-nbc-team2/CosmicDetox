package com.rocket.cosmic_detox.presentation.view.fragment.race

import com.rocket.cosmic_detox.data.datasource.remote.model.RankingInfo

interface RankingItemClickListener {

    fun onRankingItemClick(ranking: RankingInfo)
}