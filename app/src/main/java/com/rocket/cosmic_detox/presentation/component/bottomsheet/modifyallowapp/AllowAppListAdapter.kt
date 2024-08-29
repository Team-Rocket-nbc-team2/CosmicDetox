package com.rocket.cosmic_detox.presentation.component.bottomsheet.modifyallowapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ItemAppCheckboxListBinding
import com.rocket.cosmic_detox.presentation.model.App
import com.rocket.cosmic_detox.presentation.view.common.ViewHolder

class AllowAppListAdapter(
    private val onClick: (App) -> Unit
) : ListAdapter<App, ViewHolder<App>>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<App> {
        return AllowAppViewHolder.from(parent, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder<App>, position: Int) {
        holder.onBind(currentList[position])
    }

    class AllowAppViewHolder(
        private val binding: ItemAppCheckboxListBinding,
        private val onClick: (App) -> Unit
    ) : ViewHolder<App>(binding.root) {

        override fun onBind(item: App) {
            itemView.setOnClickListener {
                onClick(item)
            }
            with(binding) {
                ivAllowAppIcon.setImageDrawable(item.appIcon)
                //ivAllowAppIcon.setImageResource(R.drawable.ic_race_mercury)
                tvAllowAppName.text = item.appName
                tvAllowAppLimitedTime.text = item.limitedTime.toString()
            }
        }

        companion object {
            fun from(parent: ViewGroup, onClick: (App) -> Unit): AllowAppViewHolder {
                val binding = ItemAppCheckboxListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return AllowAppViewHolder(binding, onClick)
            }
        }
    }
}

class AppDiffCallback : DiffUtil.ItemCallback<App>() {
    override fun areItemsTheSame(oldItem: App, newItem: App): Boolean {
        return oldItem.packageId == newItem.packageId
    }

    override fun areContentsTheSame(oldItem: App, newItem: App): Boolean {
        return oldItem == newItem
    }
}