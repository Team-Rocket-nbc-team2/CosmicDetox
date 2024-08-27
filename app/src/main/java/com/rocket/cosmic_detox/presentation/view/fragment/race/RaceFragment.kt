package com.rocket.cosmic_detox.presentation.view.fragment.race

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.FragmentRaceBinding
import com.rocket.cosmic_detox.presentation.model.RankingManager
import com.rocket.cosmic_detox.presentation.view.fragment.race.adapter.RankingListAdapter

class RaceFragment : Fragment() {

    private var _binding: FragmentRaceBinding? = null
    private val binding get() = _binding!!
    private val rankingListAdapter by lazy {
        RankingListAdapter {
            Toast.makeText(requireContext(), it.name, Toast.LENGTH_SHORT).show()
        }
    }

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
        rvRankingList.apply {
            adapter = rankingListAdapter
            addItemDecoration(RankingDividerItemDecoration(dividerHeight, dividerColor))
        }
    }

    private fun setDummyData() {
        rankingListAdapter.submitList(RankingManager.getRankingList())
        Log.d("RankingList", "List : ${RankingManager.getRankingList()}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}