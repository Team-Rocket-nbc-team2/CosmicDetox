package com.rocket.cosmic_detox.presentation.view.fragment.timer

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.FragmentTimerBinding
import com.rocket.cosmic_detox.presentation.component.bottomsheet.TimerAllowedAppBottomSheet
import com.rocket.cosmic_detox.presentation.component.dialog.OneButtonDialogFragment
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogFragment
import com.rocket.cosmic_detox.presentation.uistate.UiState
import com.rocket.cosmic_detox.presentation.view.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!
    private var isFinishingTimer = false
    private var time = 0 // 시간
    private val handler = Handler(Looper.getMainLooper()) // 메인 스레드에서 실행할 핸들러
    private var isTimerRunning = false // 타이머가 실행 중인지 확인하는 변수

    private val userViewModel: UserViewModel by viewModels()

    private val runnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 1000) // 1초마다 다시 실행
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallBack)

        observeViewModel()

        userViewModel.fetchTotalTime()  // ViewModel을 통해 totalTime을 불러오기
        startTimer()  // 뷰가 생성될 때 타이머 바로 시작
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            userViewModel.totalTimeState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        // 추후 작업필요할 떄 활용 로딩 상태 처리 (예: ProgressBar 표시)
                    }
                    is UiState.Success -> {
                        time = state.data.toInt()
                        updateTime()
                    }
                    is UiState.Failure -> {
                        showError(state.e?.message)
                    }
                    is UiState.Init -> {
                        // 초기 상태 처리코드 추후 작성
                    }
                }
            }
        }
    }

    private fun showError(message: String?) {
        val dialog = OneButtonDialogFragment(
            title = message ?: getString(R.string.dialog_common_error),
            onClickConfirm = {
                // 오류처리는 추후에 작성
                // ex "확인" 버튼 클릭 시 실행할 작업 여기에 작성하면됨
            }
        )
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, "ErrorDialog")
    }

    override fun onResume() {
        super.onResume()
        if (!isFinishingTimer && !isTimerRunning) {
            startTimer()
        }

        // Receiver 등록
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        requireContext().registerReceiver(appSwitchReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        if (!isFinishingTimer) {
            showTwoButtonDialog()
        }
        stopTimer()

        // Receiver 해제
        requireContext().unregisterReceiver(appSwitchReceiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        stopTimer()
    }

    private fun initView() = with(binding) {
        btnTimerFinish.setOnClickListener {
            val dialog = TwoButtonDialogFragment(
                title = getString(R.string.timer_dialog_finish),
                onClickConfirm = {
                    isFinishingTimer = true
                    stopTimer()
                    findNavController().popBackStack()
                },
                onClickCancel = {
                    // 취소 버튼 클릭 시의 행동을 정의 (현재는 아무것도 하지 않음)
                }
            )
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, "ConfirmDialog")
        }

        btnTimerRest.setOnClickListener {
            stopAppPinning()
            val bottomSheet = TimerAllowedAppBottomSheet {
                startAppPinning()
            }
            bottomSheet.show(parentFragmentManager, "BottomSheet")
        }
    }

    private val appSwitchReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_USER_PRESENT) {
                if (isAdded && view != null) {
                    findNavController().navigate(R.id.navigation_timer)
                }
            }
        }
    }

    private val backPressedCallBack = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showTwoButtonDialog()
        }
    }

    private fun showTwoButtonDialog() {
        val dialog = OneButtonDialogFragment(
            getString(R.string.dialog_common_focus)
        ) {
            navigateToHomeFragment() // 앱 고정 모드 여부와 상관없이 홈 화면으로 이동
        }
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, "ConfirmDialog")
    }

    private fun startAppPinning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity?.startLockTask()
        }
    }

    private fun stopAppPinning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity?.stopLockTask()
        }
    }

    fun navigateToHomeFragment() {
        findNavController().navigate(R.id.navigation_home)
    }

    private fun startTimer() {
        if (!isTimerRunning) {
            startAppPinning()
            handler.post(runnable)
            isTimerRunning = true
        }
    }

    private fun stopTimer() {
        handler.removeCallbacks(runnable)
        isTimerRunning = false

        stopAppPinning()
        userViewModel.updateTotalTime(time.toLong())
    }

    private fun updateTime() {
        time++
        val hours = time / 3600
        val minutes = (time % 3600) / 60
        val seconds = time % 60
        binding.tvTimerTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
