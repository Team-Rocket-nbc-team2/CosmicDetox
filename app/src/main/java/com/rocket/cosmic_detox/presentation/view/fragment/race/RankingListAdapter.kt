package com.rocket.cosmic_detox.presentation.view.fragment.race

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ItemRankingListBinding
import com.rocket.cosmic_detox.databinding.ItemRankingTopBinding
import com.rocket.cosmic_detox.databinding.ItemRankingTopListBinding
import com.rocket.cosmic_detox.presentation.model.RankingInfo

enum class RankingType(val type: Int) {
    RANKING_TOP_1(0),
    RANKING_TOP_2(1),
    RANKING_LIST(2),
    EMPTY(-1)
}

class RankingListAdapter(
    private val onClick: (RankingInfo) -> Unit
) : ListAdapter<RankingInfo, RecyclerView.ViewHolder>(RankingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holderType = RankingType.entries.find {
            it.type == viewType
        } ?: RankingType.EMPTY
        Log.d("ViewHolder 체크", "리스트 : $currentList")
        return when (holderType) {
            RankingType.RANKING_TOP_1 -> RankingTopViewHolder.from(parent, onClick)
            RankingType.RANKING_TOP_2 -> RankingTopViewHolder.from(parent, onClick)
            RankingType.RANKING_LIST -> RankingViewHolder.from(parent, onClick)
            RankingType.EMPTY -> TODO()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RankingTopViewHolder -> holder.bind(getItem(position), position)
            is RankingViewHolder -> holder.bind(getItem(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0, 1 -> RankingType.RANKING_TOP_1.type
            else -> RankingType.RANKING_LIST.type
        }
    }

    class RankingTopViewHolder(
        private val binding: ItemRankingTopBinding,
        private val onClick: (RankingInfo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ranking: RankingInfo, position: Int) {
            with(binding) {
                //val layout = if (position == 0) layoutRankingFirst else layoutRankingSecond
                //layout.apply {
                    ivRankingTopUserProfile.setImageResource(R.drawable.mars)
                    tvRankingTopUserName.text = ranking.name
                    tvRankingTopTime.text = ranking.time.toString()
                    tvRankingTopPoint.text = ranking.point.toString()
                //}
            }
        }

        companion object {
            fun from(
                parent: ViewGroup,
                onClick: (RankingInfo) -> Unit
            ): RankingTopViewHolder {
                return RankingTopViewHolder(ItemRankingTopBinding.inflate(LayoutInflater.from(parent.context), parent, false), onClick)
            }
        }
    }

    class RankingViewHolder(
        private val binding: ItemRankingListBinding,
        private val onClick: (RankingInfo) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ranking: RankingInfo) {
            with(binding) {
                // 3등부터 순위 표시
                tvRankingListRank.text = 3.toString()
                ivRankingListUserProfile.setImageResource(R.drawable.saturn)
                tvRankingListUserName.text = ranking.name
                tvRankingListStats.text = "${ranking.time}시간 ${ranking.point}점"
            }
        }

        companion object {
            fun from(
                parent: ViewGroup,
                onClick: (RankingInfo) -> Unit,
            ): RankingViewHolder {
                return RankingViewHolder(ItemRankingListBinding.inflate(LayoutInflater.from(parent.context), parent, false), onClick)
            }
        }
    }
}

private class RankingDiffCallback : DiffUtil.ItemCallback<RankingInfo>() {
    override fun areItemsTheSame(oldItem: RankingInfo, newItem: RankingInfo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RankingInfo, newItem: RankingInfo): Boolean {
        return oldItem == newItem
    }
}