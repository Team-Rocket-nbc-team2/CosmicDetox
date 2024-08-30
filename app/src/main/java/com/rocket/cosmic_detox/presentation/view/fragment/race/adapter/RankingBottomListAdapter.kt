package com.rocket.cosmic_detox.presentation.view.fragment.race.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rocket.cosmic_detox.databinding.ItemRankingBottomBinding
import com.rocket.cosmic_detox.presentation.extensions.loadRankingPlanetImage
import com.rocket.cosmic_detox.presentation.extensions.setStats
import com.rocket.cosmic_detox.data.model.RankingInfo
import com.rocket.cosmic_detox.presentation.view.fragment.race.RankingItemClickListener

private const val RANK_START = 3

class RankingBottomListAdapter(
    private val listener: RankingItemClickListener
) : ListAdapter<RankingInfo, RankingBottomListAdapter.RankingBottomViewHolder>(RankingItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingBottomViewHolder {
        return RankingBottomViewHolder.from(parent, listener)
    }

    override fun onBindViewHolder(holder: RankingBottomViewHolder, position: Int) {
        // 3부터 시작하는 순위를 계산하여 전달
        val rank = position + RANK_START
        holder.bind(getItem(position), rank)
    }

    class RankingBottomViewHolder(
        private val binding: ItemRankingBottomBinding,
        private val listener: RankingItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ranking: RankingInfo, rank: Int) {
            itemView.setOnClickListener {
                listener.onRankingItemClick(ranking)
            }
//            with(binding) {
//                tvRankingBottomRank.text = rank.toString()
//                ivRankingBottomUserProfile.loadRankingPlanetImage(ranking.cumulativeTime)
//                tvRankingBottomUserName.text = ranking.name
//                tvRankingBottomStats.setStats(ranking.cumulativeTime, ranking.points)
//            }
        }

        companion object {
            fun from(parent: ViewGroup, listener: RankingItemClickListener): RankingBottomViewHolder {
                val binding = ItemRankingBottomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return RankingBottomViewHolder(binding, listener)
            }
        }
    }
}