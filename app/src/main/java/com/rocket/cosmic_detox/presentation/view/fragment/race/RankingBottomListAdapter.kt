package com.rocket.cosmic_detox.presentation.view.fragment.race

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ItemRankingBottomListBinding
import com.rocket.cosmic_detox.databinding.ItemRankingListBinding
import com.rocket.cosmic_detox.databinding.ItemRankingTopBinding
import com.rocket.cosmic_detox.presentation.model.RankingInfo

class RankingBottomListAdapter(private val onClick: (RankingInfo) -> Unit) : ListAdapter<RankingInfo, RankingBottomListAdapter.RankingBottomViewHolder>(
    RankingTopDiffCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingBottomViewHolder {
        val binding = ItemRankingListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RankingBottomViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: RankingBottomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RankingBottomViewHolder(
        private val binding: ItemRankingListBinding,
        private val onClick: (RankingInfo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ranking: RankingInfo) {
            itemView.setOnClickListener {
                onClick(ranking)
            }
            with(binding) {
                with(binding) {
                    // 3등부터 순위 표시
                    tvRankingListRank.text = 3.toString()
                    ivRankingListUserProfile.setImageResource(R.drawable.saturn)
                    tvRankingListUserName.text = ranking.name
                    tvRankingListStats.text = "${ranking.time}시간 ${ranking.point}점"
                }
            }
        }
    }
}

private class RankingTopDiffCallback : DiffUtil.ItemCallback<RankingInfo>() {
    override fun areItemsTheSame(oldItem: RankingInfo, newItem: RankingInfo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RankingInfo, newItem: RankingInfo): Boolean {
        return oldItem == newItem
    }
}