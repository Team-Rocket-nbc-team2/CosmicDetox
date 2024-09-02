package com.rocket.cosmic_detox.presentation.view.fragment.mypage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.rocket.cosmic_detox.data.model.AppUsage
import com.rocket.cosmic_detox.databinding.ItemAppUsageBinding
import com.rocket.cosmic_detox.presentation.extensions.toMinutes
import com.rocket.cosmic_detox.presentation.view.common.ViewHolder

class MyAppUsageAdapter : ListAdapter<AppUsage, ViewHolder<AppUsage>>(AppUsageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<AppUsage> {
        return MyAppUsageViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder<AppUsage>, position: Int) {
        holder.onBind(currentList[position])
    }

    class MyAppUsageViewHolder(private val binding: ItemAppUsageBinding) : ViewHolder<AppUsage>(binding.root) {
        override fun onBind(item: AppUsage) {
            with(binding) {
                Glide.with(ivAppUsageIcon)
                    .load(item.appIcon)
                    .into(ivAppUsageIcon)
                tvAppUsageName.text = item.appName
                tvAppUsageTime.text = item.usageTime.toBigDecimal().toMinutes().toString()
                //progressbarAppUsage.setProgress(item.usagePercentage, true) // TODO: 퍼센트 설정해야 함.
            }
        }

        companion object {
            fun from(parent: ViewGroup): MyAppUsageViewHolder {
                val binding = ItemAppUsageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return MyAppUsageViewHolder(binding)
            }
        }
    }
}

class AppUsageDiffCallback : DiffUtil.ItemCallback<AppUsage>() {
    override fun areItemsTheSame(oldItem: AppUsage, newItem: AppUsage): Boolean {
        return oldItem.packageId == newItem.packageId
    }

    override fun areContentsTheSame(oldItem: AppUsage, newItem: AppUsage): Boolean {
        return oldItem == newItem
    }
}