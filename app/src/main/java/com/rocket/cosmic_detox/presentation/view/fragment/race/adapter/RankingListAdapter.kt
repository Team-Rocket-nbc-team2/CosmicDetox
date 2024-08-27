package com.rocket.cosmic_detox.presentation.view.fragment.race.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ItemRankingBottomListBinding
import com.rocket.cosmic_detox.databinding.ItemRankingListBinding
import com.rocket.cosmic_detox.databinding.ItemRankingTopListBinding
import com.rocket.cosmic_detox.presentation.model.RankingInfo

enum class RankingType(val type: Int) {
    RANKING_TOP(0),
    RANKING_LIST(1),
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
            RankingType.RANKING_TOP -> RankingTopViewHolder.from(parent, onClick)
            RankingType.RANKING_LIST -> RankingBottomViewHolder.from(parent, onClick)
            RankingType.EMPTY -> TODO()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RankingTopViewHolder -> {
                // 첫 번째와 두 번째 아이템 처리
                val topItems = currentList.take(2)
                holder.bind(topItems)
            }
            is RankingBottomViewHolder -> {
                // 세 번째 아이템부터 나머지 아이템을 전달
                if (position >= 2) {
                    val rankingItem = currentList[position]
                    holder.bind(rankingItem, position + 1)  // 올바른 순위를 전달
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0, 1 -> RankingType.RANKING_TOP.type
            else -> RankingType.RANKING_LIST.type
        }
    }

    override fun getItemCount(): Int {
        // 첫 두 개 아이템은 RankingTopViewHolder에서 처리되므로 전체 아이템 수에서 1을 뺀다
        return if (currentList.size > 2) currentList.size - 1 else currentList.size
    }

    class RankingTopViewHolder(
        private val binding: ItemRankingTopListBinding,
        private val onClick: (RankingInfo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val rankingTopListAdapter = RankingTopListAdapter {
            onClick(it)
        }

        init {
            binding.rvRankingTop.adapter = rankingTopListAdapter
        }

        fun bind(rankingList: List<RankingInfo>) {
            Log.d("RankingTopViewHolder", "rankingList: $rankingList")
            rankingTopListAdapter.submitList(rankingList)
        }

        companion object {
            fun from(parent: ViewGroup, onClick: (RankingInfo) -> Unit): RankingTopViewHolder {
                return RankingTopViewHolder(
                    ItemRankingTopListBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                    onClick
                )
            }
        }
    }

    class RankingBottomViewHolder(
        private val binding: ItemRankingListBinding,
        private val onClick: (RankingInfo) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ranking: RankingInfo, rank: Int) {
            itemView.setOnClickListener {
                onClick(ranking)
            }
            with(binding) {
                tvRankingListRank.text = rank.toString()
                ivRankingListUserProfile.setImageResource(R.drawable.saturn)
                tvRankingListUserName.text = ranking.name
                tvRankingListStats.text = "${ranking.time}시간 ${ranking.point}점"
            }
        }

        companion object {
            fun from(parent: ViewGroup, onClick: (RankingInfo) -> Unit, ): RankingBottomViewHolder {
                return RankingBottomViewHolder(
                    ItemRankingListBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                    onClick
                )
            }
        }
    }
}

class RankingDiffCallback : DiffUtil.ItemCallback<RankingInfo>() {
    override fun areItemsTheSame(oldItem: RankingInfo, newItem: RankingInfo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RankingInfo, newItem: RankingInfo): Boolean {
        return oldItem == newItem
    }
}