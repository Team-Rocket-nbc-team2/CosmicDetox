package com.rocket.cosmic_detox.presentation.view.fragment.mypage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.rocket.cosmic_detox.data.model.Trophy
import com.rocket.cosmic_detox.databinding.ItemMyTrophyBinding
import com.rocket.cosmic_detox.presentation.common.ViewHolder

class MyTrophyAdapter(
    private val onClick: (Trophy) -> Unit
) : ListAdapter<Trophy, ViewHolder<Trophy>>(TrophyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<Trophy> {
        return MyTrophyViewHolder.from(parent, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder<Trophy>, position: Int) {
        holder.onBind(currentList[position])
    }

    class MyTrophyViewHolder(
        private val binding: ItemMyTrophyBinding,
        private val onClick: (Trophy) -> Unit
    ) : ViewHolder<Trophy>(binding.root) {

        override fun onBind(item: Trophy) {
            itemView.setOnClickListener {
                onClick(item)
            }
            with(binding) {
                Glide.with(ivTrophy)
                    .load(item.imageUrl)
                    .into(ivTrophy)
            }
        }

        companion object {
            fun from(parent: ViewGroup, onClick: (Trophy) -> Unit): MyTrophyViewHolder {
                val binding = ItemMyTrophyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return MyTrophyViewHolder(binding, onClick)
            }
        }
    }
}

class TrophyDiffCallback : DiffUtil.ItemCallback<Trophy>() {
    override fun areItemsTheSame(oldItem: Trophy, newItem: Trophy): Boolean {
        return oldItem.trophyId == newItem.trophyId
    }

    override fun areContentsTheSame(oldItem: Trophy, newItem: Trophy): Boolean {
        return oldItem == newItem
    }
}