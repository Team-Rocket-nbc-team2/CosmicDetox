package com.rocket.cosmic_detox.presentation.component.bottomsheet.modifyallowedapp

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.CheckedApp
import com.rocket.cosmic_detox.databinding.ItemAppCheckboxListBinding
import com.rocket.cosmic_detox.presentation.view.common.ViewHolder

class AllowAppListAdapter(
    private val context: Context,
    private val onClick: (CheckedApp) -> Unit
) : ListAdapter<CheckedApp, ViewHolder<CheckedApp>>(CheckedAppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<CheckedApp> {
        return AllowAppViewHolder.from(parent, onClick, context)
    }

    override fun onBindViewHolder(holder: ViewHolder<CheckedApp>, position: Int) {
        holder.onBind(currentList[position])
    }

    class AllowAppViewHolder(
        private val binding: ItemAppCheckboxListBinding,
        private val onClick: (CheckedApp) -> Unit,
        private val context: Context
    ) : ViewHolder<CheckedApp>(binding.root) {

        override fun onBind(item: CheckedApp) {
            with(binding) {
                checkboxAllowApp.isChecked = item.isChecked
                itemView.setOnClickListener {
                    checkboxAllowApp.isChecked = !checkboxAllowApp.isChecked
                }
                checkboxAllowApp.setOnCheckedChangeListener { _, isChecked ->
                    onClick(item)
                }

                Glide.with(ivAllowAppIcon)
                    .load(context.packageManager.getApplicationIcon(item.packageId))
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(12)))
                    .into(ivAllowAppIcon)
                tvAllowAppName.text = item.appName
                tvAllowAppLimitedTime.text = item.limitedTime.toString()
            }
        }

        companion object {
            fun from(parent: ViewGroup, onClick: (CheckedApp) -> Unit, context: Context): AllowAppViewHolder {
                val binding = ItemAppCheckboxListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return AllowAppViewHolder(binding, onClick, context)
            }
        }
    }
}

class CheckedAppDiffCallback : DiffUtil.ItemCallback<CheckedApp>() {
    override fun areItemsTheSame(oldItem: CheckedApp, newItem: CheckedApp): Boolean {
        return oldItem.packageId == newItem.packageId
    }

    override fun areContentsTheSame(oldItem: CheckedApp, newItem: CheckedApp): Boolean {
        return oldItem == newItem
    }
}