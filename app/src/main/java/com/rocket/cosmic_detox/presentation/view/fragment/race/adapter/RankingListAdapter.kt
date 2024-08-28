package com.rocket.cosmic_detox.presentation.view.fragment.race.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rocket.cosmic_detox.databinding.ItemRankingBottomListBinding
import com.rocket.cosmic_detox.databinding.ItemRankingTopListBinding
import com.rocket.cosmic_detox.presentation.model.Ranking
import com.rocket.cosmic_detox.presentation.model.RankingBottom
import com.rocket.cosmic_detox.presentation.model.RankingInfo
import com.rocket.cosmic_detox.presentation.model.RankingTop
import com.rocket.cosmic_detox.presentation.view.fragment.race.RankingItemClickListener

enum class RankingType(val type: Int) {
    RANKING_TOP(0),
    RANKING_LIST(1),
    EMPTY(-1)
}

class RankingListAdapter(
    private val listener: RankingItemClickListener
) : ListAdapter<Ranking, RecyclerView.ViewHolder>(RankingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holderType = RankingType.entries.find {
            it.type == viewType
        } ?: RankingType.EMPTY
        Log.d("ViewHolder 체크", "리스트 : $currentList")
        return when (holderType) {
            RankingType.RANKING_TOP -> RankingTopViewHolder.from(parent, listener)
            RankingType.RANKING_LIST -> RankingBottomViewHolder.from(parent, listener)
            RankingType.EMPTY -> TODO()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
//            is RankingTopViewHolder -> {
//                // 첫 번째와 두 번째 아이템 처리
//                val topItems = currentList.take(2)
//                holder.bind(topItems)
//            }
//            is RankingBottomViewHolder -> {
//                // 세 번째 아이템부터 나머지 아이템을 전달
//                if (position >= 2) {
//                    val rankingItem = currentList[position]
//                    holder.bind(rankingItem, position + 1)  // 올바른 순위를 전달
//                }
//            }
            is RankingTopViewHolder -> {
                val item = currentList[position] as RankingTop
                holder.bind(item)
            }
            is RankingBottomViewHolder -> {
                val item = currentList[position] as RankingBottom
                holder.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
//            currentList.first() -> RankingType.RANKING_TOP.type
//            else -> RankingType.RANKING_LIST.type
            is RankingTop -> RankingType.RANKING_TOP.type
            is RankingBottom -> RankingType.RANKING_LIST.type
            else -> RankingType.EMPTY.type
        }
    }

//    override fun getItemCount(): Int {
//        // 첫 두 개 아이템은 RankingTopViewHolder에서 처리되므로 전체 아이템 수에서 1을 뺀다
//        return if (currentList.size > 2) currentList.size - 1 else currentList.size
//    }

    class RankingTopViewHolder(
        private val binding: ItemRankingTopListBinding,
        listener: RankingItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        private val rankingTopListAdapter = RankingTopListAdapter(listener)

        init {
            binding.rvRankingTop.adapter = rankingTopListAdapter
        }

        fun bind(rankingTop: RankingTop) {
            Log.d("RankingTopViewHolder", "rankingTop.topItems: $rankingTop.topItems")
            rankingTopListAdapter.submitList(rankingTop.topItems)
        }

        companion object {
            fun from(parent: ViewGroup, listener: RankingItemClickListener): RankingTopViewHolder {
                return RankingTopViewHolder(
                    ItemRankingTopListBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                    listener
                )
            }
        }
    }

    class RankingBottomViewHolder(
        private val binding: ItemRankingBottomListBinding,
        listener: RankingItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        private val rankingBottomListAdapter = RankingBottomListAdapter(listener)

        init {
            binding.rvRankingBottom.adapter = rankingBottomListAdapter
        }

        fun bind(rankingBottom: RankingBottom) {
            rankingBottomListAdapter.submitList(rankingBottom.bottomItems)
        }

        companion object {
            fun from(parent: ViewGroup, listener: RankingItemClickListener): RankingBottomViewHolder {
                return RankingBottomViewHolder(
                    ItemRankingBottomListBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                    listener
                )
            }
        }
    }
}

class RankingDiffCallback : DiffUtil.ItemCallback<Ranking>() {
    override fun areItemsTheSame(oldItem: Ranking, newItem: Ranking): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Ranking, newItem: Ranking): Boolean {
        return oldItem == newItem
    }
}

class RankingDetailDiffCallback : DiffUtil.ItemCallback<RankingInfo>() {
    override fun areItemsTheSame(oldItem: RankingInfo, newItem: RankingInfo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RankingInfo, newItem: RankingInfo): Boolean {
        return oldItem == newItem
    }
}