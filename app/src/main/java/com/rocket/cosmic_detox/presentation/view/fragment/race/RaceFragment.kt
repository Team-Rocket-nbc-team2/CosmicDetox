package com.rocket.cosmic_detox.presentation.view.fragment.race

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.FragmentRaceBinding
import com.rocket.cosmic_detox.presentation.model.RankingBottom
import com.rocket.cosmic_detox.presentation.model.RankingInfo
import com.rocket.cosmic_detox.presentation.model.RankingManager
import com.rocket.cosmic_detox.presentation.model.RankingTop
import com.rocket.cosmic_detox.presentation.view.fragment.race.adapter.RaceAdapter

class RaceFragment : Fragment(), RankingItemClickListener {

    private var _binding: FragmentRaceBinding? = null
    private val binding get() = _binding!!
    private val raceAdapter by lazy { RaceAdapter(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setDummyData()
    }

    private fun initView() = with(binding) {
        val dividerHeight = resources.getDimensionPixelSize(R.dimen.divider_height)
        val dividerColor = ContextCompat.getColor(requireContext(), R.color.stroke_dark)
        rvRace.apply {
            adapter = raceAdapter
            addItemDecoration(RankingDividerItemDecoration(dividerHeight, dividerColor))
        }
    }

    private fun setDummyData() {
        val list = RankingManager.getRankingList()
        // 0, 1번째 인덱스는 Top, 나머지는 Bottom
        val topList = list.take(2)
        val bottomList = list.drop(2)
        val rankingList = listOf(
            RankingTop(topList),
            RankingBottom(bottomList)
        )
        raceAdapter.submitList(rankingList)
    }

    override fun onRankingItemClick(ranking: RankingInfo) {
        Toast.makeText(requireContext(), ranking.name, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}