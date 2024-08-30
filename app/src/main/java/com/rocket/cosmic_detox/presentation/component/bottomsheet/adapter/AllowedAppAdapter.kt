package com.rocket.cosmic_detox.presentation.component.bottomsheet.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.databinding.ItemAppTimeListBinding

class AllowedAppAdapter(
    private val allowedApps: List<AllowedApp>,
    private val context: Context,
    private val onItemClick: (String) -> Unit
): RecyclerView.Adapter<AllowedAppAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllowedAppAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_time_list, parent, false)
        return ViewHolder(ItemAppTimeListBinding.bind(view))
    }

    override fun onBindViewHolder(holder: AllowedAppAdapter.ViewHolder, position: Int) {
        holder.bind(allowedApps[position])
    }

    override fun getItemCount(): Int = allowedApps.size

    inner class ViewHolder(private val binding: ItemAppTimeListBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(allowedApp: AllowedApp) {
            with(binding) {
                if (allowedApp.limitedTime == 0) emphasizeDarkerLayout.visibility = View.VISIBLE
                else {
                    root.setOnClickListener {
                        onItemClick(allowedApp.packageId)
                    }
                }

                Glide.with(context)
                    .load(context.packageManager.getApplicationIcon(allowedApp.packageId))
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(12)))
                    .into(appIcon)

                appName.text = allowedApp.appName
                appUsageTime.text = getTimeString(allowedApp.limitedTime)
            }
        }

        private fun getTimeString(limitedTime: Int): String {
            var remainTime = limitedTime
            val hour = remainTime / 3600

            remainTime %= 3600
            val minute = remainTime / 60

            return if (hour == 0) "${minute}분"
            else "${hour}시간 ${minute}분"
        }
    }
}