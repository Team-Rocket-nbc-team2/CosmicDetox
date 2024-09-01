package com.rocket.cosmic_detox.presentation.component.bottomsheet.setlimitapp

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.databinding.ItemAppDepthListBinding
import com.rocket.cosmic_detox.presentation.component.bottomsheet.modifyallowedapp.AppDiffCallback
import com.rocket.cosmic_detox.presentation.extensions.toMinutes
import com.rocket.cosmic_detox.presentation.view.common.ViewHolder

class LimitedAppAdapter(
    private val context: Context,
    private val onClick: (AllowedApp) -> Unit
) : ListAdapter<AllowedApp, ViewHolder<AllowedApp>>(AppDiffCallback()) {

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
                Glide.with(ivDepthAppIcon)
                    .load(context.packageManager.getApplicationIcon(item.packageId))
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(12)))
                    .into(ivDepthAppIcon)
                tvAppDepthName.text = item.appName
                tvAppDepthUsageTime.text = item.limitedTime.toString()
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