package com.rocket.cosmic_detox.presentation.component.bottomsheet.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.databinding.ItemAppTimeListBinding
import com.rocket.cosmic_detox.presentation.extensions.isAppInstalled
import com.rocket.cosmic_detox.presentation.extensions.loadAllowedAppIcon

class AllowedAppAdapter(
    private val context: Context,
    private val onItemClick: (String, Long) -> Unit
): ListAdapter<AllowedApp, AllowedAppAdapter.ViewHolder>(
    object: DiffUtil.ItemCallback<AllowedApp>() {
        override fun areItemsTheSame(oldItem: AllowedApp, newItem: AllowedApp): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: AllowedApp, newItem: AllowedApp): Boolean {
            return oldItem.limitedTime == newItem.limitedTime
        }
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllowedAppAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_time_list, parent, false)
        return ViewHolder(ItemAppTimeListBinding.bind(view))
    }

    override fun onBindViewHolder(holder: AllowedAppAdapter.ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAppTimeListBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(allowedApp: AllowedApp) {
            with(binding) {
                if (allowedApp.limitedTime == 0L || !context.isAppInstalled(allowedApp.packageId)) emphasizeDarkerLayout.visibility = View.VISIBLE
                else {
                    root.setOnClickListener {
                        onItemClick(allowedApp.packageId, allowedApp.limitedTime)
                    }
                }
                appIcon.loadAllowedAppIcon(context, allowedApp.packageId, allowedApp.appIcon)
                appName.text = allowedApp.appName
                appUsageTime.text = getTimeString(allowedApp.limitedTime)
            }
        }

        private fun getTimeString(limitedTime: Long): String {
            var remainTime = limitedTime
            val hour = remainTime / 3600

            remainTime %= 3600
            val minute = remainTime / 60

            return if (hour == 0L) "${minute}분"
            else "${hour}시간 ${minute}분"
        }
    }
}