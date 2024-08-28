package com.rocket.cosmic_detox.presentation.extensions

import android.widget.ImageView
import com.rocket.cosmic_detox.R
import java.math.BigDecimal

fun ImageView.loadRankingImage(cumulativeTime: BigDecimal) {
    val imageRes = when {
        cumulativeTime < BigDecimal(6 * 3600) -> R.drawable.ic_race_mercury // 수성: 누적 6시간 (21600초)
        cumulativeTime < BigDecimal(12 * 3600) -> R.drawable.ic_race_mars // 화성: 누적 12시간 (43200초)
        cumulativeTime < BigDecimal(24 * 3600) -> R.drawable.ic_race_venus // 금성: 누적 24시간 (86400초)
        cumulativeTime < BigDecimal(48 * 3600) -> R.drawable.ic_race_earth // 지구: 누적 48시간 (172800초)
        cumulativeTime < BigDecimal(64 * 3600) -> R.drawable.ic_race_neptune // 해왕성: 누적 64시간 (230400초)
        cumulativeTime < BigDecimal(120 * 3600) -> R.drawable.ic_race_uranus // 천왕성: 누적 120시간 (432000초)
        cumulativeTime < BigDecimal(240 * 3600) -> R.drawable.ic_race_saturn // 토성: 누적 240시간 (864000초)
        cumulativeTime < BigDecimal(400 * 3600) -> R.drawable.ic_race_jupiter // 목성: 누적 400시간 (1440000초)
//        cumulativeTime < BigDecimal(500 * 3600) -> R.drawable.ic_race_sun // 태양: 누적 500시간 (1800000초)
        else -> R.drawable.ic_race_sun // 500시간 이상일 경우 태양 이미지
    }
    this.setImageResource(imageRes)
}