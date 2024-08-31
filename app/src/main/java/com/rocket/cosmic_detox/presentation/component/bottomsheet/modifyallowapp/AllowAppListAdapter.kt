package com.rocket.cosmic_detox.presentation.component.bottomsheet.modifyallowapp

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.databinding.ItemAppCheckboxListBinding
import com.rocket.cosmic_detox.data.model.App
import com.rocket.cosmic_detox.presentation.view.common.ViewHolder

class AllowAppListAdapter(
    private val context: Context,
    private val onClick: (AllowedApp) -> Unit
) : ListAdapter<AllowedApp, ViewHolder<AllowedApp>>(AppDiffCallback()) {

    private val checkedItems = mutableSetOf<AllowedApp>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<AllowedApp> {
        return AllowAppViewHolder.from(parent, onClick, context, checkedItems)
    }

    override fun onBindViewHolder(holder: ViewHolder<AllowedApp>, position: Int) {
        holder.onBind(currentList[position])
    }

    fun getCheckedItems(): List<AllowedApp> {
        return checkedItems.toList()
    }

    class AllowAppViewHolder(
        private val binding: ItemAppCheckboxListBinding,
        private val onClick: (AllowedApp) -> Unit,
        private val context: Context,
        private val checkedItems: MutableSet<AllowedApp>
    ) : ViewHolder<AllowedApp>(binding.root) {

        override fun onBind(item: AllowedApp) {
            with(binding) {
                itemView.setOnClickListener {
                    val isAllowed = !item.isAllowed
                    checkboxAllowApp.isChecked = isAllowed
                    if (isAllowed) {
                        checkedItems.add(item)
                    } else {
                        checkedItems.remove(item)
                    }
                    onClick(item)
                }

                Glide.with(ivAllowAppIcon)
                    .load(context.packageManager.getApplicationIcon(item.packageId))
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(12)))
                    .into(ivAllowAppIcon)
                tvAllowAppName.text = item.appName
                tvAllowAppLimitedTime.text = item.limitedTime.toString()

                checkboxAllowApp.isChecked = item.isAllowed
//                checkboxAllowApp.setOnCheckedChangeListener { _, isChecked ->
//                    val newItem = item.copy(isAllowed = isChecked)
//                }
            }
        }

        companion object {
            fun from(parent: ViewGroup, onClick: (AllowedApp) -> Unit, context: Context, checkedItems: MutableSet<AllowedApp>): AllowAppViewHolder {
                val binding = ItemAppCheckboxListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return AllowAppViewHolder(binding, onClick, context, checkedItems)
            }
        }
    }
}

class AppDiffCallback : DiffUtil.ItemCallback<AllowedApp>() {
    override fun areItemsTheSame(oldItem: AllowedApp, newItem: AllowedApp): Boolean {
        return oldItem.packageId == newItem.packageId
    }

    override fun areContentsTheSame(oldItem: AllowedApp, newItem: AllowedApp): Boolean {
        return oldItem == newItem
    }
}