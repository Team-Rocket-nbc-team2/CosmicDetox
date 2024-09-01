package com.rocket.cosmic_detox.presentation.view.fragment.mypage.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.data.model.AppUsage
import com.rocket.cosmic_detox.presentation.extensions.toMinutes

class AppUsageAdapter(private val appUsageList: List<AppUsage>) :
    RecyclerView.Adapter<AppUsageAdapter.AppUsageViewHolder>() {

    class AppUsageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
        val appName: TextView = itemView.findViewById(R.id.appName)
        val appUsageTime: TextView = itemView.findViewById(R.id.appUsageTime)
        val usageProgressBar: ProgressBar = itemView.findViewById(R.id.usageProgressBar)
        val itemContainer: View = itemView.findViewById(R.id.item_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppUsageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_usage, parent, false)
        return AppUsageViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppUsageViewHolder, position: Int) {
        val appUsage = appUsageList[position]
        holder.appIcon.setImageDrawable(appUsage.appIcon)
        holder.appName.text = appUsage.appName
        holder.appUsageTime.text = appUsage.usageTime.toBigDecimal().toMinutes().toString()
        //holder.usageProgressBar.progress = appUsage.usagePercentage

        // 첫 번째와 마지막 아이템에 둥근 모서리 배경 설정
        when (position) {
            0 -> holder.itemContainer.setBackgroundResource(R.drawable.shape_rounded_top) // 첫 번째 꺼
            itemCount - 1 -> holder.itemContainer.setBackgroundResource(R.drawable.shape_rounded_bottom) // 마지막
            else -> holder.itemContainer.setBackgroundResource(R.color.background_light) // 일반
        }
    }

    override fun getItemCount(): Int = appUsageList.size
}
