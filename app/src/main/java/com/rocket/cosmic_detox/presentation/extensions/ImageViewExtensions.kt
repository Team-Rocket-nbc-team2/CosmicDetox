package com.rocket.cosmic_detox.presentation.extensions

import android.content.Context
import android.widget.ImageView
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.rocket.cosmic_detox.R
import java.math.BigDecimal

fun ImageView.loadHomePlanetImage(cumulativeTime: BigDecimal) {
    // when(cumulativeTime)으로 변경 후 in, until 사용해도 되는데 코드가 더 길어져서(아래 이유) 일단 when으로만 함.
    // 숫자관리는 BigDecimal 클래스를 이용하기 때문에 코드가 길어짐
    val (imageResId, paddingValue) = when {
        cumulativeTime < BigDecimal(6 * 3600) -> Pair(R.drawable.mercury, 130) // 수성: 누적 6시간 (21600초)
        cumulativeTime < BigDecimal(12 * 3600) -> Pair(R.drawable.mars, 120) // 화성: 누적 12시간 (43200초)
        cumulativeTime < BigDecimal(24 * 3600) -> Pair(R.drawable.venus, 110) // 금성: 누적 24시간 (86400초)
        cumulativeTime < BigDecimal(48 * 3600) -> Pair(R.drawable.earth, 107) // 지구: 누적 48시간 (172800초)
        cumulativeTime < BigDecimal(64 * 3600) -> Pair(R.drawable.neptune, 100) // 해왕성: 누적 64시간 (230400초)
        cumulativeTime < BigDecimal(120 * 3600) -> Pair(R.drawable.uranus, 69) // 천왕성: 누적 120시간 (432000초)
        cumulativeTime < BigDecimal(240 * 3600) -> Pair(R.drawable.saturn, 47) // 토성: 누적 240시간 (864000초)
        cumulativeTime < BigDecimal(400 * 3600) -> Pair(R.drawable.jupiter, 60) // 목성: 누적 400시간 (1440000초)
//        cumulativeTime < BigDecimal(500 * 3600) -> R.drawable.ic_race_sun // 태양: 누적 500시간 (1800000초)
        else -> Pair(R.drawable.sun, 7) // 500시간 이상일 경우 태양 이미지
    }
    setPadding(paddingValue.dpToPx(context))
    setImageResource(imageResId)
}

fun ImageView.loadRankingPlanetImage(cumulativeTime: BigDecimal) {
    val (imageResId, paddingValue) = when {
        cumulativeTime < BigDecimal(6 * 3600) -> Pair(R.drawable.ic_race_mercury, -1) // 수성: 누적 6시간 (21600초)
        cumulativeTime < BigDecimal(12 * 3600) -> Pair(R.drawable.ic_race_mars, -1) // 화성: 누적 12시간 (43200초)
        cumulativeTime < BigDecimal(24 * 3600) -> Pair(R.drawable.ic_race_venus, -1) // 금성: 누적 24시간 (86400초)
        cumulativeTime < BigDecimal(48 * 3600) -> Pair(R.drawable.ic_race_earth, -1) // 지구: 누적 48시간 (172800초)
        cumulativeTime < BigDecimal(64 * 3600) -> Pair(R.drawable.ic_race_neptune, -1) // 해왕성: 누적 64시간 (230400초)
        cumulativeTime < BigDecimal(120 * 3600) -> Pair(R.drawable.ic_race_uranus, 0) // 천왕성: 누적 120시간 (432000초)
        cumulativeTime < BigDecimal(240 * 3600) -> Pair(R.drawable.ic_race_saturn, 0) // 토성: 누적 240시간 (864000초)
        cumulativeTime < BigDecimal(400 * 3600) -> Pair(R.drawable.ic_race_jupiter, 4) // 목성: 누적 400시간 (1440000초)
//        cumulativeTime < BigDecimal(500 * 3600) -> R.drawable.ic_race_sun // 태양: 누적 500시간 (1800000초)
        else -> Pair(R.drawable.ic_race_sun, 0) // 500시간 이상일 경우 태양 이미지
    }
    if (paddingValue != -1) {
        setPadding(paddingValue.dpToPx(context))
    }
    setImageResource(imageResId)
}

fun ImageView.loadAppIcon(context: Context, packageId: String) {
    Glide.with(this)
        .load(context.packageManager.getApplicationIcon(packageId))
        .apply(RequestOptions.bitmapTransform(RoundedCorners(12)))
        .placeholder(R.drawable.shape_default_app_icon)
        .into(this)
}
