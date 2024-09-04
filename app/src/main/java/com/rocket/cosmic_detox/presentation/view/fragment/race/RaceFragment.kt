package com.rocket.cosmic_detox.presentation.view.fragment.race

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.FragmentRaceBinding
import com.rocket.cosmic_detox.data.model.RankingInfo
import com.rocket.cosmic_detox.presentation.extensions.loadRankingPlanetImage
import com.rocket.cosmic_detox.presentation.extensions.setStats
import com.rocket.cosmic_detox.presentation.uistate.MyPageUiState
import com.rocket.cosmic_detox.presentation.view.fragment.race.adapter.RaceAdapter
import com.rocket.cosmic_detox.presentation.view.fragment.race.viewmodel.RaceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RaceFragment : Fragment(), RankingItemClickListener {

    private var _binding: FragmentRaceBinding? = null
    private val binding get() = _binding!!
    private val raceAdapter by lazy { RaceAdapter(this) }
    private val viewModel: RaceViewModel by viewModels()

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeViewModel()

    }

    private fun initView() = with(binding) {
        rvRace.adapter = raceAdapter
        viewModel.getRanking()
        viewModel.getMyRank()

        // 내 데이터 받아오는 부분
        val myRanking = db.collection("season")
            .document("season-2024-08")
            .collection("ranking")
            .document("test1")

        myRanking.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val point = document.getLong("point")?: 0
                    val totalTime = document.getLong("totalTime")?: 0
                    val name = document.getString("name")

                    // TODO() 내 순위도 순위에 반영하는 로직 구현 못하는 중

//                    val myRank = viewModel.uiState.value.let { uiState ->
//                        if (uiState is MyPageUiState.Success) {
//                            uiState.data.indexOfFirst { it.uid == document.id } + 1
//                        } else {
//                            // 순위를 찾지 못했을 때 아무 것도 반환 x
//                        }
//                    }

                    layoutMyRanking.apply {
                        root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary))
                        ivRankingBottomUserProfile.loadRankingPlanetImage(totalTime.toBigDecimal())
                        tvRankingBottomStats.setStats(point.toBigDecimal(), totalTime.toBigDecimal())
                        tvRankingBottomUserName.text = "$name"
//                        tvRankingBottomRank.text = myRank.toString()
                    }
                } else {
                    Log.d("db", "No Documents")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("db", "get failed with ", exception)
            }
    }

    private fun observeViewModel() = with(viewModel) {
        lifecycleScope.launchWhenStarted {
            uiState.collect { uiState ->
                when (uiState) {
//                    is UiState.Loading -> {   ////  로딩 상태 처리
                    is MyPageUiState.Success -> {
                        val ranking = uiState.data
                        val topRanking = ranking.take(2)
                        val myRanking = ranking.drop(2)
                        raceAdapter.submitRankingList(topRanking, myRanking)
                        Log.d("Success", "${uiState.data}")
                    }

                    is MyPageUiState.Error -> {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${uiState.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {
                        Log.d("ggil", "uiState")
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            myRank
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest { uiState ->
                    when (uiState) {
                        MyPageUiState.Loading -> {
                            Log.d("MyPageFragment", "MyPageFragment - Loading")
                        }

                        is MyPageUiState.Success -> {
                            binding.layoutMyRanking.tvRankingBottomRank.text = uiState.data.toString()
                        }

                        is MyPageUiState.Error -> {
                            Log.d(
                                "MyPageFragment",
                                "MyPageFragment - Error: ${uiState.message}"
                            )
                        }
                    }
                }
        }
    }

    override fun onRankingItemClick(ranking: RankingInfo) {
        Toast.makeText(requireContext(), ranking.name, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}