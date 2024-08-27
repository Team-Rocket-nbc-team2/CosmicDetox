package com.rocket.cosmic_detox.presentation.view.fragment.race

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
) : ListAdapter<RankingInfo, RecyclerView.ViewHolder>(RankingListDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holderType = RankingType.entries.find {
            it.type == viewType
        } ?: RankingType.EMPTY
        Log.d("ViewHolder 체크", "리스트 : $currentList")
        return when (holderType) {
            RankingType.RANKING_TOP -> RankingTopViewHolder.from(parent, onClick)
            RankingType.RANKING_LIST -> RankingViewHolder.from(parent, onClick)
            RankingType.EMPTY -> TODO()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RankingTopViewHolder -> {
                // 첫 번째 뷰홀더에서 리스트의 첫 두 개의 아이템을 전달
                val topItems = currentList.take(2)
                holder.bind(topItems)
            }
            is RankingViewHolder -> {
                // 나머지 아이템을 전달
                holder.bind(getItem(position + 1))  // 인덱스 보정
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> RankingType.RANKING_TOP.type
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

    class RankingViewHolder(
        private val binding: ItemRankingBottomListBinding,
        private val onClick: (RankingInfo) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        private val rankingBottomListAdapter = RankingBottomListAdapter {
            onClick(it)
        }

        init {
            binding.rvRankingBottom.apply {
                adapter = rankingBottomListAdapter
                itemAnimator = null
            }
        }

        fun bind(ranking: RankingInfo) {
            rankingBottomListAdapter.submitList(listOf(ranking))
        }

        companion object {
            fun from(parent: ViewGroup, onClick: (RankingInfo) -> Unit, ): RankingViewHolder {
                return RankingViewHolder(
                    ItemRankingBottomListBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                    onClick
                )
            }
        }
    }
}

private class RankingListDiffCallback : DiffUtil.ItemCallback<RankingInfo>() {
    override fun areItemsTheSame(oldItem: RankingInfo, newItem: RankingInfo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RankingInfo, newItem: RankingInfo): Boolean {
        return oldItem == newItem
    }
}