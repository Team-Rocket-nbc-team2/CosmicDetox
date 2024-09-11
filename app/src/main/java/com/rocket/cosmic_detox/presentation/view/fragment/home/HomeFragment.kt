package com.rocket.cosmic_detox.presentation.view.fragment.home

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.data.model.User
import com.rocket.cosmic_detox.databinding.FragmentHomeBinding
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogFragment
import com.rocket.cosmic_detox.presentation.extensions.*
import com.rocket.cosmic_detox.presentation.uistate.UiState
import com.rocket.cosmic_detox.presentation.view.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val Int.dp: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            resources.displayMetrics
        ).toInt()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
    }

    private fun initView() = with(binding) {
        btnNavigateToTimer.setOnClickListener {
            val dialog = TwoButtonDialogFragment(
                title = getString(R.string.home_travel_start),
                onClickConfirm = {
                    val action = HomeFragmentDirections.actionHomeToTimer2()
                    findNavController().navigate(action) },
                onClickCancel = { }
            )
            dialog.isCancelable = false
            dialog.show(getParentFragmentManager(), "ConfirmDialog")
        }
    }

    private fun initViewModel() = with(userViewModel) {
        fetchUserData()

        viewLifecycleOwner.lifecycleScope.launch {
            userState.collectLatest { uiState ->
                when (uiState) {
                    is UiState.Success -> {
                        bindingUserData(uiState.data)
                    }
                    else -> { }
                }
            }
        }
    }

    private fun bindingUserData(user: User) = with(binding) {
        val totalTime = user.totalTime.toBigDecimal()
        ivHomeMyPlanet.loadHomePlanetImage(totalTime)
        tvHomePlanetName.setCurrentLocation(totalTime)
        tvHomeHoursCount.setCumulativeTime(totalTime)
        tvHomeTravelingTime.setTravelingTime(user.dailyTime.toBigDecimal())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}