package com.rocket.cosmic_detox.presentation.view.fragment.race

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rocket.cosmic_detox.databinding.ItemRankingListBinding
import com.rocket.cosmic_detox.presentation.model.RankingInfo

class RankingListAdapter(
    private val onClick: (RankingInfo) -> Unit
) : ListAdapter<RankingInfo, RankingListAdapter.RankingViewHolder>(
    FavoriteVideoDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        return RankingViewHolder.from(parent, onClick)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RankingViewHolder(
        private val binding: ItemRankingListBinding,
        private val onClick: (RankingInfo) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ranking: RankingInfo) {
            with(binding) {

            }
        }

        companion object {
            fun from(
                parent: ViewGroup,
                onClick: (RankingInfo) -> Unit,
            ): RankingViewHolder {
                return RankingViewHolder(
                    ItemRankingListBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ), onClick
                )
            }
        }
    }
}

private class FavoriteVideoDiffCallback : DiffUtil.ItemCallback<RankingInfo>() {
    override fun areItemsTheSame(oldItem: RankingInfo, newItem: RankingInfo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RankingInfo, newItem: RankingInfo): Boolean {
        return oldItem == newItem
    }
}