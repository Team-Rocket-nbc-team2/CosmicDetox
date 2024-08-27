package com.rocket.cosmic_detox.presentation.view.fragment.race.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ItemRankingListBinding
import com.rocket.cosmic_detox.presentation.model.RankingInfo

class RankingBottomListAdapter(private val onClick: (RankingInfo) -> Unit) : ListAdapter<RankingInfo, RankingBottomListAdapter.RankingBottomViewHolder>(
    RankingDiffCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingBottomViewHolder {
        return RankingBottomViewHolder.from(parent, onClick)
    }

    override fun onBindViewHolder(holder: RankingBottomViewHolder, position: Int) {
        // 3부터 시작하는 순위를 계산하여 전달
        val rank = position + 3
        holder.bind(getItem(position), rank)
    }

    class RankingBottomViewHolder(
        private val binding: ItemRankingListBinding,
        private val onClick: (RankingInfo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ranking: RankingInfo, rank: Int) {
            itemView.setOnClickListener {
                onClick(ranking)
            }
            with(binding) {
                // 순위 표시
                Log.d("RankingBottomViewHolder", "rank : $rank")
                tvRankingListRank.text = rank.toString()
                ivRankingListUserProfile.setImageResource(R.drawable.saturn)
                tvRankingListUserName.text = ranking.name
                tvRankingListStats.text = "${ranking.time}시간 ${ranking.point}점"
            }
        }

        companion object {
            fun from(parent: ViewGroup, onClick: (RankingInfo) -> Unit): RankingBottomViewHolder {
                val binding = ItemRankingListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return RankingBottomViewHolder(binding, onClick)
            }
        }
    }
}