package com.rocket.cosmic_detox.presentation.view.fragment.mypage.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.data.model.Trophy

class MyTrophyAdapter(private val trophyList: List<Trophy>) :
    RecyclerView.Adapter<MyTrophyAdapter.TrophyViewHolder>() {

    class TrophyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val trophyIcon: ImageView = itemView.findViewById(R.id.ic_trophy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrophyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_my_trophy, parent, false)
        return TrophyViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrophyViewHolder, position: Int) {
        val trophy = trophyList[position]
        holder.trophyIcon.setImageResource(R.drawable.ic_race_mercury)
    }

    override fun getItemCount(): Int = trophyList.size
}
