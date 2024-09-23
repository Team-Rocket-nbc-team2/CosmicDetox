package com.rocket.cosmic_detox.presentation.view.fragment.mypage

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.User
import com.rocket.cosmic_detox.databinding.FragmentMyPageBinding
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogDescFragment
import com.rocket.cosmic_detox.presentation.extensions.loadRankingPlanetImage
import com.rocket.cosmic_detox.presentation.extensions.setMyDescription
import com.rocket.cosmic_detox.presentation.uistate.MyPageUiState
import com.rocket.cosmic_detox.presentation.view.fragment.mypage.adapter.MyAppUsageAdapter
import com.rocket.cosmic_detox.presentation.view.fragment.mypage.adapter.MyTrophyAdapter
import com.rocket.cosmic_detox.presentation.viewmodel.PermissionViewModel
import com.rocket.cosmic_detox.util.DateFormatText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyPageFragment : Fragment() {

    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!
    private val myPageViewModel by activityViewModels<MyPageViewModel>()
    private val permissionViewModel: PermissionViewModel by activityViewModels()
    private val myAppUsageAdapter by lazy { MyAppUsageAdapter() }
    private val myTrophyAdapter by lazy {
        MyTrophyAdapter { trophy ->
            Toast.makeText(requireContext(), trophy.name, Toast.LENGTH_SHORT).show()
        }
    }
    private lateinit var allowedApps: List<AllowedApp>
    private var isPermissionChecked = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()

        binding.btnSetLimitAppUseTime.setOnClickListener {
            val action = MyPageFragmentDirections.actionMyToSetLimitApp(allowedApps.toTypedArray())
            findNavController().navigate(action)
        }

        binding.btnAllowAppSetting.setOnClickListener {
            val action =
                MyPageFragmentDirections.actionMyToModifyAllowApp(allowedApps.toTypedArray())
            findNavController().navigate(action)
        }

        binding.ivSettings.setOnClickListener {
            findNavController().navigate(MyPageFragmentDirections.actionNavigationMyToNavigationSetting())
        }
    }

    private fun initView() = with(binding) {
        val verticalSpaceHeight = resources.getDimensionPixelSize(R.dimen.item_app_usage_vertical_space)
        rvMyAppUsage.apply {
            adapter = myAppUsageAdapter
            addItemDecoration(AppUsageItemDecoration(verticalSpaceHeight))
        }
        rvMyTrophies.adapter = myTrophyAdapter
        myPageViewModel.loadMyInfo()
        allowedApps = emptyList()

        checkAndRequestUsageStatsPermission()
        btnAllowAppUsagePermission.setOnClickListener {
            requestUsageStatsPermission()
        }
    }

    private fun initViewModel() = with(myPageViewModel) {
        viewLifecycleOwner.lifecycleScope.launch {
            myInfo
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest { uiState ->
                    binding.groupMyPageProgressBar.isVisible = uiState is MyPageUiState.Loading
                    when (uiState) {
                        is MyPageUiState.Success -> {
                            setMyInfo(uiState.data)
                            binding.apply {
                                rvMyTrophies.isVisible = uiState.data.trophies.isNotEmpty()
                                tvNoTrophyMessage.isVisible = uiState.data.trophies.isEmpty()
                            }
                            myTrophyAdapter.submitList(uiState.data.trophies)
                            allowedApps = uiState.data.apps
                        }
                        is MyPageUiState.Error -> {
                            Log.d(
                                "MyPageFragment",
                                "MyPageFragment - Error: ${uiState.message}"
                            )
                        }
                        else -> Unit
                    }
                }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            myAppUsageList
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest { uiState ->
                    when (uiState) {
                        MyPageUiState.Loading -> {
                            Log.d("MyPageFragment", "myAppUsageList - Loading")
                        }
                        is MyPageUiState.Success -> {
                            myAppUsageAdapter.submitList(uiState.data)
                        }
                        is MyPageUiState.Error -> {
                            Log.d(
                                "MyPageFragment",
                                "myAppUsageList - Error: ${uiState.message}"
                            )
                        }
                    }
                }
        }
    }

    private fun setMyInfo(user: User) = with(binding) {
        user.apply {
            ivMyProfileImage.loadRankingPlanetImage(totalTime.toBigDecimal())
            tvMyName.text = name
            tvMyDescription.setMyDescription(DateFormatText.getTotalDays(createdAt), totalTime.toBigDecimal()) // TODO: totalDay 수정 필요
        }
    }

    private fun checkAndRequestUsageStatsPermission() {
        if (!permissionViewModel.isUsageStatsPermissionGranted(requireContext())) {
            // 권한이 없을 때만 버튼과 메시지를 보여줍니다.
            binding.rvMyAppUsage.visibility = View.GONE
            binding.tvNoAppUsageMessage.visibility = View.VISIBLE
            binding.btnAllowAppUsagePermission.visibility = View.VISIBLE
        } else {
            binding.rvMyAppUsage.visibility = View.VISIBLE
            binding.tvNoAppUsageMessage.visibility = View.GONE
            binding.btnAllowAppUsagePermission.visibility = View.GONE
            myPageViewModel.loadMyAppUsage()
            isPermissionChecked = true // 권한이 확인되었음을 기록
        }
    }

    private fun requestUsageStatsPermission() {
        val dialog = TwoButtonDialogDescFragment(
            title = getString(R.string.dialog_permission_title),
            description = getString(R.string.dialog_my_permission_desc),
            onClickConfirm = {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                startActivity(intent)
            },
            onClickCancel = {}
        )
        dialog.isCancelable = false
        dialog.show(getParentFragmentManager(), "ConfirmDialog")
    }

    override fun onResume() {
        super.onResume()
        // 권한이 있을 경우만 앱 사용 통계 로딩
        if (!isPermissionChecked && permissionViewModel.isUsageStatsPermissionGranted(requireContext())) {
            isPermissionChecked = true
            binding.rvMyAppUsage.visibility = View.VISIBLE
            binding.tvNoAppUsageMessage.visibility = View.GONE
            binding.btnAllowAppUsagePermission.visibility = View.GONE
            myPageViewModel.loadMyAppUsage()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}