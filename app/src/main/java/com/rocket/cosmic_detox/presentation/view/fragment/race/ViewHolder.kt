package com.rocket.cosmic_detox.presentation.view.fragment.race

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.rocket.cosmic_detox.presentation.model.Ranking

abstract class ViewHolder<T>(root: View) : RecyclerView.ViewHolder(root) {

    abstract fun onBind(item: Ranking)
}