package com.rocket.cosmic_detox.presentation.view.fragment.race

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rocket.cosmic_detox.R

class RankingDividerItemDecoration(private val dividerHeight: Int, private val dividerColor: Int) : RecyclerView.ItemDecoration() {

    private val paint = Paint().apply {
        color = dividerColor
        style = Paint.Style.FILL
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + dividerHeight

//            if (i != parent.childCount - 1) {  // 마지막 아이템에는 구분선을 그리지 않음
//                c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
//            }
        }
    }
}