package com.rocket.cosmic_detox.presentation.view.fragment.mypage

import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.Context
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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.User
import com.rocket.cosmic_detox.databinding.FragmentMyPageBinding
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogDescFragment
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogFragment
import com.rocket.cosmic_detox.presentation.extensions.loadRankingPlanetImage
import com.rocket.cosmic_detox.presentation.extensions.setMyDescription
import com.rocket.cosmic_detox.presentation.extensions.toHours
import com.rocket.cosmic_detox.presentation.uistate.MyPageUiState
import com.rocket.cosmic_detox.presentation.view.activity.SignInActivity
import com.rocket.cosmic_detox.presentation.view.fragment.mypage.adapter.MyAppUsageAdapter
import com.rocket.cosmic_detox.presentation.view.fragment.mypage.adapter.MyTrophyAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyPageFragment : Fragment() {

    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!
    private val myPageViewModel by activityViewModels<MyPageViewModel>()
    private val myAppUsageAdapter by lazy { MyAppUsageAdapter() }
    private val myTrophyAdapter by lazy {
        MyTrophyAdapter { trophy ->
            Toast.makeText(requireContext(), trophy.name, Toast.LENGTH_SHORT).show()
        }
    }
    private lateinit var allowedApps: List<AllowedApp>

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
            val action = MyPageFragmentDirections.actionMyToModifyAllowApp(allowedApps.toTypedArray())
            findNavController().navigate(action)
        }
        // 개인정보보호정책 및 이용약관
//        binding.tvPolicy.setOnClickListener {
//        노션에 개인정보보호정책 및 이용약관 작업 후 move to notion 작업
//        }

        // 회원 탈퇴 기능 구현
        binding.tvWithdrawal.setOnClickListener {
            val dialog = TwoButtonDialogDescFragment(
                title = getString(R.string.dialog_withdrawal),
                description = getString(R.string.dialog_withdrawal_desc),
                onClickConfirm = {
                    val user = Firebase.auth.currentUser!!
                    Log.d("withdrawal2", user.email.toString())
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val intent = Intent(requireContext(), SignInActivity::class.java)
                                startActivity(intent)
                                Log.d("withdrawal", "User account deleted.")
                            }
                        }
                    Log.d("intent", "Intent Successfully")
                },
                onClickCancel = { false }
            )
            dialog.isCancelable = false
            dialog.show(getParentFragmentManager(), "ConfirmDialog")
        }

        // 로그아웃 기능 구현
        binding.tvSignOut.setOnClickListener {
            val dialog = TwoButtonDialogFragment(
                title = getString(R.string.dialog_sign_out),
                onClickConfirm = {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(requireContext(), SignInActivity::class.java)
                    startActivity(intent)
                },
                onClickCancel = { false }
            )
            dialog.isCancelable = false
            dialog.show(getParentFragmentManager(), "ConfirmDialog")
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
        //loadMyAppUsage() // 권한 확인된 후 호출
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
            tvMyDescription.setMyDescription(totalDay, totalTime.toBigDecimal())
        }
    }

    private fun checkAndRequestUsageStatsPermission() {
        if (!hasUsageStatsPermission(requireContext())) {
            //requestUsageStatsPermission()
            binding.rvMyAppUsage.visibility = View.GONE
            binding.tvNoAppUsageMessage.visibility = View.VISIBLE
            binding.btnAllowAppUsagePermission.visibility = View.VISIBLE
        } else {
            binding.rvMyAppUsage.visibility = View.VISIBLE
            binding.tvNoAppUsageMessage.visibility = View.GONE
            binding.btnAllowAppUsagePermission.visibility = View.GONE
            myPageViewModel.loadMyAppUsage()
        }
    }

    private fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        AlertDialog.Builder(requireContext())
            .setTitle("권한 필요")
            .setMessage("앱 사용 통계에 접근하려면 권한이 필요합니다. 설정에서 권한을 부여해주세요.")
            .setPositiveButton("설정으로 이동") { dialog, _ ->
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (hasUsageStatsPermission(requireContext())) {
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