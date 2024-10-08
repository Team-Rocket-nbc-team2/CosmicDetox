package com.rocket.cosmic_detox.presentation.view.fragment.race.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ItemRankingBottomListBinding
import com.rocket.cosmic_detox.databinding.ItemRankingTopListBinding
import com.rocket.cosmic_detox.data.model.RankingInfo
import com.rocket.cosmic_detox.presentation.view.fragment.race.RankingDividerItemDecoration
import com.rocket.cosmic_detox.presentation.view.fragment.race.RankingItemClickListener
import com.rocket.cosmic_detox.presentation.common.ViewHolder
import com.rocket.cosmic_detox.presentation.model.Ranking
import com.rocket.cosmic_detox.presentation.model.RankingBottom
import com.rocket.cosmic_detox.presentation.model.RankingTop

enum class RankingType(val type: Int) {
    RANKING_TOP(0),
    RANKING_LIST(1),
    EMPTY(-1)
}

class RaceAdapter(
    private val listener: RankingItemClickListener
) : ListAdapter<Ranking, ViewHolder<Ranking>>(RankingListDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<Ranking> {
        val holderType = RankingType.entries.find {
            it.type == viewType
        } ?: RankingType.EMPTY
        return when (holderType) {
            RankingType.RANKING_TOP -> RankingTopViewHolder.from(parent, listener)
            RankingType.RANKING_LIST -> RankingBottomViewHolder.from(parent, listener)
            RankingType.EMPTY -> TODO()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder<Ranking>, position: Int) {
        holder.onBind(currentList[position])
    }

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position]) {
            is RankingTop -> RankingType.RANKING_TOP.type
            is RankingBottom -> RankingType.RANKING_LIST.type
            else -> RankingType.EMPTY.type
        }
    }

    fun submitRankingList(topItems: List<RankingInfo>, bottomItems: List<RankingInfo>) {
        val rankingList = listOf(
            RankingTop(topItems),
            RankingBottom(bottomItems)
        )
        submitList(rankingList)
    }

    class RankingTopViewHolder(
        binding: ItemRankingTopListBinding,
        listener: RankingItemClickListener
    ) : ViewHolder<Ranking>(binding.root) {

        private val rankingTopListAdapter = RankingTopListAdapter(listener)

        init {
            binding.rvRankingTop.adapter = rankingTopListAdapter
        }

        override fun onBind(item: Ranking) {
            if (item is RankingTop) {
                rankingTopListAdapter.submitList(item.topItems)
            }
        }

        companion object {
            fun from(parent: ViewGroup, listener: RankingItemClickListener): RankingTopViewHolder {
                val binding = ItemRankingTopListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return RankingTopViewHolder(binding, listener)
            }
        }
    }

    class RankingBottomViewHolder(
        binding: ItemRankingBottomListBinding,
        listener: RankingItemClickListener
    ) : ViewHolder<Ranking>(binding.root) {

        private val rankingBottomListAdapter = RankingBottomListAdapter(listener)

        init {
            binding.rvRankingBottom.adapter = rankingBottomListAdapter

            val context = binding.root.context
            val dividerHeight = context.resources.getDimensionPixelSize(R.dimen.divider_height)
            val dividerColor = ContextCompat.getColor(context, R.color.stroke_dark)
            binding.rvRankingBottom.addItemDecoration(RankingDividerItemDecoration(dividerHeight, dividerColor))
        }

        override fun onBind(item: Ranking) {
            if (item is RankingBottom) {
                rankingBottomListAdapter.submitList(item.bottomItems)
            }
        }

        companion object {
            fun from(parent: ViewGroup, listener: RankingItemClickListener): RankingBottomViewHolder {
                val binding = ItemRankingBottomListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return RankingBottomViewHolder(binding, listener)
            }
        }
    }
}

class RankingListDiffCallback : DiffUtil.ItemCallback<Ranking>() {
    override fun areItemsTheSame(oldItem: Ranking, newItem: Ranking): Boolean {
//        return (oldItem as? RankingTop)?.topItems == (newItem as? RankingTop)?.topItems &&
//                (oldItem as? RankingBottom)?.bottomItems == (newItem as? RankingBottom)?.bottomItems
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Ranking, newItem: Ranking): Boolean {
        return oldItem == newItem
    }
}

class RankingItemDiffCallback : DiffUtil.ItemCallback<RankingInfo>() {
    override fun areItemsTheSame(oldItem: RankingInfo, newItem: RankingInfo): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: RankingInfo, newItem: RankingInfo): Boolean {
        return oldItem == newItem
    }
}