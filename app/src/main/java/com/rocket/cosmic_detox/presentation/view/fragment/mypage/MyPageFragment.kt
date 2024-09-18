package com.rocket.cosmic_detox.presentation.view.fragment.mypage

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.User
import com.rocket.cosmic_detox.databinding.FragmentMyPageBinding
import com.rocket.cosmic_detox.presentation.component.dialog.OneButtonDialogFragment
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogDescFragment
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogFragment
import com.rocket.cosmic_detox.presentation.extensions.loadRankingPlanetImage
import com.rocket.cosmic_detox.presentation.extensions.setMyDescription
import com.rocket.cosmic_detox.presentation.uistate.MyPageUiState
import com.rocket.cosmic_detox.presentation.uistate.UiState
import com.rocket.cosmic_detox.presentation.view.activity.SignInActivity
import com.rocket.cosmic_detox.presentation.view.fragment.mypage.adapter.MyAppUsageAdapter
import com.rocket.cosmic_detox.presentation.view.fragment.mypage.adapter.MyTrophyAdapter
import com.rocket.cosmic_detox.presentation.viewmodel.PermissionViewModel
import com.rocket.cosmic_detox.util.Authentication
import com.rocket.cosmic_detox.util.Constants.NOTION_LINK
import com.rocket.cosmic_detox.util.Constants.PROVIDER_GOOGLE
import com.rocket.cosmic_detox.util.Constants.PROVIDER_TWITTER
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

    // 구글 로그인 클라이언트 객체
    private val googleSignInClient by lazy {
        GoogleSignIn.getClient(
            requireContext(),
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )
    }

    // 구글 로그인 클라이언트 런처 객체 (registerForActivityResult 사용)
    private val signInLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    account?.idToken?.let { idToken ->
                        // Firebase에 Google 인증 자격 증명으로 재인증 시도
                        myPageViewModel.reAuthenticateWithGoogle(idToken)
                    }
                } catch (e: ApiException) {
                    Log.e("MyPageFragment", "Google SignIn failed", e)
                }
            }
        }

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
                    //launchGoogleSignInClient()
                    // Authentication 제공자가 Google, Twitter일 경우
                    // Firebase에 Google, Twitter 인증 자격 증명으로 재인증 시도
                    val platform = Authentication.currentUser?.providerData?.get(1)?.providerId
                    when (platform) {
                        PROVIDER_GOOGLE -> {
                            Toast.makeText(requireContext(), "google", Toast.LENGTH_SHORT).show()
                            launchGoogleSignInClient()
                        }
                        PROVIDER_TWITTER -> {
                            reAuthenticationWithTwitter()
                        }
                        else -> {
                            Toast.makeText(requireContext(), "unknown", Toast.LENGTH_SHORT).show()
                        }
                    }
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

    // 구글 로그인 클라이언트를 런칭시키는 함수
    private fun launchGoogleSignInClient() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun initView() = with(binding) {
        val verticalSpaceHeight =
            resources.getDimensionPixelSize(R.dimen.item_app_usage_vertical_space)
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
        tvPolicy.setOnClickListener {
            val dialog =
                TwoButtonDialogDescFragment(
                    title = getString(R.string.dialog_personal_policy_terms_title),
                    description = getString(R.string.dialog_personal_policy_terms_desc),
                    onClickConfirm = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(NOTION_LINK))
                        startActivity(intent)
                    },
                    onClickCancel = {})
            dialog.isCancelable = false
            dialog.show(getParentFragmentManager(), "ConfirmDialog")
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

        viewLifecycleOwner.lifecycleScope.launch {
            myPageViewModel.userStatus.collectLatest {
                when (it) {
                    is UiState.Success -> {
                        val intent = Intent(requireContext(), SignInActivity::class.java)
                        startActivity(intent)
                        Toast.makeText(requireContext(), getString(R.string.dialog_withdrawal_success), Toast.LENGTH_SHORT).show()
                        requireActivity().finish() // 로그인 화면으로 이동 후 다시 뒤로가기 방지
                    }
                    is UiState.Failure -> {
                        val dialog = OneButtonDialogFragment(getString(R.string.dialog_withdrawal_failure)) {}
                        dialog.isCancelable = false
                        dialog.show(getParentFragmentManager(), "ConfirmDialog")
                    }
                    is UiState.SigningFailure -> {
                        val dialog =
                            TwoButtonDialogDescFragment(
                                title = getString(R.string.dialog_withdrawal_logout_title),
                                description = getString(R.string.dialog_withdrawal_logout_title),
                                onClickConfirm = {
                                    FirebaseAuth.getInstance().signOut()

                                    val intent = Intent(requireContext(), SignInActivity::class.java)
                                    startActivity(intent)
                                },
                                onClickCancel = {})
                        dialog.isCancelable = false
                        dialog.show(getParentFragmentManager(), "ConfirmDialog")
                    }
                    else -> {
                        // 로딩 중
                    }
                }
            }
        }
    }

    private fun setMyInfo(user: User) = with(binding) {
        user.apply {
            ivMyProfileImage.loadRankingPlanetImage(totalTime.toBigDecimal())
            tvMyName.text = name
            tvMyDescription.setMyDescription(DateFormatText.getTotalDays(totalDay), totalTime.toBigDecimal()) // TODO: totalDay 수정 필요
        }
    }

    private fun reAuthenticationWithTwitter() {
        val provider = OAuthProvider.newBuilder(PROVIDER_TWITTER)
        val currentUser = Authentication.currentUser

        currentUser?.let {
            currentUser.startActivityForReauthenticateWithProvider(requireActivity(), provider.build())
                .addOnSuccessListener {
                    // 회원 탈퇴 로직 실행
                    myPageViewModel.withdraw(currentUser)
                }
                .addOnFailureListener {
                    Log.e("withdrawal", "재인증 실패 XXXXX: $it")
                }
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
            onClickCancel = { false }
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