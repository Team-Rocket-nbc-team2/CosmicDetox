package com.rocket.cosmic_detox.presentation.view.fragment.race.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ItemRankingTopBinding
import com.rocket.cosmic_detox.presentation.model.RankingInfo

class RankingTopListAdapter(private val onClick: (RankingInfo) -> Unit) : ListAdapter<RankingInfo, RankingTopListAdapter.RankingTopViewHolder>(
    RankingDiffCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingTopViewHolder {
        return RankingTopViewHolder.from(parent, onClick)
    }

    override fun onBindViewHolder(holder: RankingTopViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RankingTopViewHolder(
        private val binding: ItemRankingTopBinding,
        private val onClick: (RankingInfo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ranking: RankingInfo) {
            itemView.setOnClickListener {
                onClick(ranking)
            }
            with(binding) {
                ivRankingTopUserProfile.setImageResource(R.drawable.mars)
                tvRankingTopUserName.text = ranking.name
                tvRankingTopTime.text = ranking.time.toString()
                tvRankingTopPoint.text = ranking.point.toString()
            }
        }

        companion object {
            fun from(parent: ViewGroup, onClick: (RankingInfo) -> Unit): RankingTopViewHolder {
                val binding = ItemRankingTopBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return RankingTopViewHolder(binding, onClick)
            }
        }
    }
}