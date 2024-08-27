package com.rocket.cosmic_detox.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rocket.cosmic_detox.R

data class AppUsage(val iconResId: Int, val appName: String, val usageTime: String)

class AppUsageAdapter(private val appUsageList: List<AppUsage>) :
    RecyclerView.Adapter<AppUsageAdapter.AppUsageViewHolder>() {

    class AppUsageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
        val appName: TextView = itemView.findViewById(R.id.appName)
        val appUsageTime: TextView = itemView.findViewById(R.id.appUsageTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppUsageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_usage, parent, false)
        return AppUsageViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppUsageViewHolder, position: Int) {
        val appUsage = appUsageList[position]
        holder.appIcon.setImageResource(appUsage.iconResId)
        holder.appName.text = appUsage.appName
        holder.appUsageTime.text = appUsage.usageTime
    }

    override fun getItemCount(): Int = appUsageList.size
}
