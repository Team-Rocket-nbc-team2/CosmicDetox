package com.rocket.cosmic_detox.presentation.view.fragment.introduce

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.data.model.Planet
import com.rocket.cosmic_detox.databinding.DialogPlanetInfoBinding
import com.rocket.cosmic_detox.presentation.view.fragment.introduce.adapter.PlanetPagerAdapter

class PlanetInfoDialogFragment : DialogFragment() {

    private lateinit var binding: DialogPlanetInfoBinding
    private val planetAdapter by lazy { PlanetPagerAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogPlanetInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewPager2 설정 (TabLayout 제거, ViewPager만 유지)
        binding.viewPagerPlanetInfo.adapter = planetAdapter

        // 행성 정보 데이터 설정
        planetAdapter.submitList(getPlanetInfoList())

        // 확인 버튼 클릭 시 다이얼로그 닫기
        binding.btnConfirm.setOnClickListener {
            dismiss() // 다이얼로그 닫기
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 1.0).toInt(),  // 화면의 90% 너비로 설정
            (resources.displayMetrics.heightPixels * 0.8).toInt()  // 화면의 60% 높이로 설정
        )
    }

    private fun getPlanetInfoList(): List<Planet> {
        // 행성 정보 리스트 반환 (이미지와 설명 포함)
        return listOf(
            Planet(
                "수성", "누적 6시간 미만", "태양계에서 가장 작은 행성이자,\n" +
                        "태양과 가장 가까운 천체입니다.", R.drawable.introduce_mercury
            ),
            Planet(
                "화성", "누적 12시간 미만", "우리 지구와 가장 유사한 행성으로\n" +
                        "표면 탐사가 가장 많이 진행되고 있습니다.", R.drawable.introduce_mars
            ),
            Planet(
                "금성", "누적 12시간 미만", "지구에서 관측할 수 있는 행성으로\n" +
                        "태양, 달 다음으로 밝게 보입니다.", R.drawable.introduce_venus
            ),
            Planet(
                "지구", "누적 12시간 미만", "태양계에서 유일하게 생명체가 서식하며\n" +
                        "우리 은하에서 중간 거리에 위치해있습니다.", R.drawable.introduce_earth
            ),
            Planet(
                "해왕성", "누적 12시간 미만", "명왕성 제외 이후, 태양계 마지막 행성으로\n" +
                        "메탄 때문에 천체가 푸르게 보입니다.", R.drawable.introduce_neptune
            ),
            Planet(
                "천왕성", "누적 12시간 미만", "천왕성의 고리는 육안으로 관찰이 어려우며\n" +
                        "유일하게 옆으로 누워 자전하는 행성입니다.", R.drawable.introduce_uranus
            ),
            Planet(
                "토성", "누적 12시간 미만", "태양계에서 두 번째로 큰 행성으로,\n" +
                        "물에 넣으면 뜰 수 있는 가스 행성입니다.", R.drawable.introduce_saturn
            ),
            Planet(
                "목성", "누적 12시간 미만", "태양계에서 가장 부피가 크고\n" +
                        "매우 빠른 속도로 자전하고 있습니다.", R.drawable.introduce_jupiter
            ),
            Planet(
                "태양", "누적 12시간 미만", "태양계의 중심이자 유일한 항성이며\n" +
                        "우리 은하의 중심부를 지금도 공전 중입니다.", R.drawable.introduce_sun
            )
        )
    }
}
