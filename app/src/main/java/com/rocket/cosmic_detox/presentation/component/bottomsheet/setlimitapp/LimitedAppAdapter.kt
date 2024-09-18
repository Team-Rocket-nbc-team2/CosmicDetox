package com.rocket.cosmic_detox.presentation.component.bottomsheet.setlimitapp

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.databinding.ItemAppDepthListBinding
import com.rocket.cosmic_detox.presentation.extensions.loadAllowedAppIcon
import com.rocket.cosmic_detox.presentation.extensions.setCumulativeTime
import com.rocket.cosmic_detox.presentation.common.ViewHolder
import com.rocket.cosmic_detox.presentation.extensions.isAppInstalled

class LimitedAppAdapter(
    private val context: Context,
    private val onClick: (AllowedApp) -> Unit
) : ListAdapter<AllowedApp, ViewHolder<AllowedApp>>(AllowedAppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<AllowedApp> {
        return LimitedAppViewHolder.from(parent, onClick, context)
    }

    override fun onBindViewHolder(holder: ViewHolder<AllowedApp>, position: Int) {
        holder.onBind(currentList[position])
    }

    class LimitedAppViewHolder(
        private val binding: ItemAppDepthListBinding,
        private val onClick: (AllowedApp) -> Unit,
        private val context: Context,
    ) : ViewHolder<AllowedApp>(binding.root) {

        override fun onBind(item: AllowedApp) {
            itemView.setOnClickListener {
                onClick(item)
            }
            with(binding) {
                ivDepthAppIcon.loadAllowedAppIcon(context, item.packageId, item.appIcon)
                tvAppDepthName.text = item.appName
                tvAppDepthUsageTime.setCumulativeTime(item.limitedTime.toBigDecimal())
                emphasizeDarkerLayoutDepth.isVisible = !context.isAppInstalled(item.packageId)
            }
        }

        companion object {
            fun from(parent: ViewGroup, onClick: (AllowedApp) -> Unit, context: Context): LimitedAppViewHolder {
                val binding = ItemAppDepthListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return LimitedAppViewHolder(binding, onClick, context)
            }
        }
    }
}

class AllowedAppDiffCallback : DiffUtil.ItemCallback<AllowedApp>() {
    override fun areItemsTheSame(oldItem: AllowedApp, newItem: AllowedApp): Boolean {
        return oldItem.packageId == newItem.packageId
    }

    override fun areContentsTheSame(oldItem: AllowedApp, newItem: AllowedApp): Boolean {
        return oldItem == newItem
    }
}