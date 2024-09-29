package com.rocket.cosmic_detox.presentation.view.fragment.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.data.model.User
import com.rocket.cosmic_detox.databinding.FragmentHomeBinding
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogDescFragment
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogFragment
import com.rocket.cosmic_detox.presentation.extensions.*
import com.rocket.cosmic_detox.presentation.uistate.UiState
import com.rocket.cosmic_detox.presentation.view.fragment.introduce.PlanetInfoDialogFragment
import com.rocket.cosmic_detox.presentation.viewmodel.PermissionViewModel
import com.rocket.cosmic_detox.presentation.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()
    private val overlayPermissionLauncher = registerForActivityResult( // 오버레이 권한 요청
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (Settings.canDrawOverlays(requireContext())) {
            // 권한이 허용된 경우 처리
            navigateToTimer()
        } else {
            // 권한이 거부된 경우 처리
            Toast.makeText(requireContext(), "오버레이 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

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
//                    val action = HomeFragmentDirections.actionHomeToTimer()
//                    findNavController().navigate(action)
                    checkOverlayPermission()
                },
                onClickCancel = { }
            )
            dialog.isCancelable = false
            dialog.show(getParentFragmentManager(), "ConfirmDialog")
        }

        ivHomePlanetInfo.setOnClickListener {
            Log.d("hi","hi")
            val dialog = PlanetInfoDialogFragment()
            dialog.show(parentFragmentManager, "PlanetInfoDialog")
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

    // 오버레이 권한 확인 및 요청
    private fun checkOverlayPermission() {
        val isRequestOverlay = permissionViewModel.isOverlayPermissionGranted(requireContext())

        if (!isRequestOverlay) {
            //권한 요청!
            val dialog = TwoButtonDialogDescFragment(
                title = getString(R.string.dialog_permission_title),
                description = getString(R.string.dialog_permission_desc),
                onClickConfirm = {
                    if (!Settings.canDrawOverlays(requireContext())) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${requireContext().packageName}")
                        )
                        overlayPermissionLauncher.launch(intent)
                    }
                },
                onClickCancel = { }
            )
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, "ConfirmDialog")
        } else {
            navigateToTimer()
        }
    }

    private fun navigateToTimer() {
        val action = HomeFragmentDirections.actionHomeToTimer()
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}