package com.rocket.cosmic_detox.presentation.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.FragmentMyPageBinding
import com.rocket.cosmic_detox.presentation.adapter.AppUsage
import com.rocket.cosmic_detox.presentation.adapter.AppUsageAdapter
import com.rocket.cosmic_detox.presentation.adapter.MyTrophyAdapter
import com.rocket.cosmic_detox.presentation.adapter.Trophy
import com.rocket.cosmic_detox.presentation.component.bottomsheet.MyPageModifyAllowAppBottomSheet
import com.rocket.cosmic_detox.presentation.component.bottomsheet.MyPageSetLimitAppBottomSheet


class MyPageFragment : Fragment() {

    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val trophyRecyclerView = binding.trophyRecyclerView
        val noTrophyMessage = binding.tvNoTrophyMessage

        val trophyList = listOf(
            Trophy(R.drawable.sample_trophy_image),
            Trophy(R.drawable.sample_trophy_image),
            Trophy(R.drawable.sample_trophy_image),
            Trophy(R.drawable.sample_trophy_image),
            Trophy(R.drawable.sample_trophy_image),
            Trophy(R.drawable.sample_trophy_image),
            Trophy(R.drawable.sample_trophy_image),
            Trophy(R.drawable.sample_trophy_image),

            )
        val appUsageList = listOf(
            AppUsage(R.drawable.ic_app, "인스타그램", "120분"),
            AppUsage(R.drawable.ic_app, "유튜브", "70분"),
            AppUsage(R.drawable.ic_app, "카카오톡", "40분"),
            AppUsage(R.drawable.ic_app, "당근", "30분"),
            AppUsage(R.drawable.ic_app, "슬랙", "3분")
        )


        trophyRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        trophyRecyclerView.adapter = MyTrophyAdapter(trophyList)

        val usageRecyclerView = binding.usageRecyclerView
        val appUsageList = listOf(
            AppUsage(R.drawable.ic_app, "인스타그램", "200분",100),
            AppUsage(R.drawable.ic_app, "유튜브", "140분",70),
            AppUsage(R.drawable.ic_app, "카카오톡", "100분",50),
            AppUsage(R.drawable.ic_app, "당근마켓", "70분",35),
            AppUsage(R.drawable.ic_app, "슬랙", "40분",20),

            )

        usageRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        usageRecyclerView.adapter = AppUsageAdapter(appUsageList)

        binding.btnSetLimitAppUseTime.setOnClickListener {
            val setLimitAppBottomSheet = MyPageSetLimitAppBottomSheet()
            setLimitAppBottomSheet.show(parentFragmentManager, setLimitAppBottomSheet.tag)
        }

        binding.btnAllowAppSetting.setOnClickListener {
            val modifyAllowAppBottomSheet = MyPageModifyAllowAppBottomSheet()
            modifyAllowAppBottomSheet.show(parentFragmentManager, modifyAllowAppBottomSheet.tag)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}