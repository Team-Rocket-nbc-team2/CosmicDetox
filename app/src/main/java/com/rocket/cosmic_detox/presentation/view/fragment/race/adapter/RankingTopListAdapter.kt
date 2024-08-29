package com.rocket.cosmic_detox.presentation.view.fragment.race.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rocket.cosmic_detox.databinding.ItemRankingTopBinding
import com.rocket.cosmic_detox.presentation.extensions.loadRankingPlanetImage
import com.rocket.cosmic_detox.presentation.extensions.setCumulativeTime
import com.rocket.cosmic_detox.presentation.extensions.setPoints
import com.rocket.cosmic_detox.presentation.model.RankingInfo
import com.rocket.cosmic_detox.presentation.view.fragment.race.RankingItemClickListener

class RankingTopListAdapter(
    private val listener: RankingItemClickListener
) : ListAdapter<RankingInfo, RankingTopListAdapter.RankingTopViewHolder>(RankingItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingTopViewHolder {
        return RankingTopViewHolder.from(parent, listener)
    }

    override fun onBindViewHolder(holder: RankingTopViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RankingTopViewHolder(
        private val binding: ItemRankingTopBinding,
        private val listener: RankingItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ranking: RankingInfo) {
            itemView.setOnClickListener {
                listener.onRankingItemClick(ranking)
            }
            with(binding) {
                ivRankingTopUserProfile.loadRankingPlanetImage(ranking.cumulativeTime)
                tvRankingTopUserName.text = ranking.name
                tvRankingTopTime.setCumulativeTime(ranking.cumulativeTime)
                tvRankingTopPoint.setPoints(ranking.points)
            }
        }

        companion object {
            fun from(parent: ViewGroup, listener: RankingItemClickListener): RankingTopViewHolder {
                val binding = ItemRankingTopBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return RankingTopViewHolder(binding, listener)
            }
        }
    }
}