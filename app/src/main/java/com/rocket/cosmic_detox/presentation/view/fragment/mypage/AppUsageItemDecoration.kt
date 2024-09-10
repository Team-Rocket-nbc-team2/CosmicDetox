package com.rocket.cosmic_detox.presentation.view.fragment.mypage

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class AppUsageItemDecoration(private val verticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        // 마지막 아이템을 제외한 모든 아이템에 간격 추가
        if (parent.getChildAdapterPosition(view) != parent.adapter?.itemCount?.minus(1)) {
            outRect.bottom = verticalSpaceHeight
        }
    }
}