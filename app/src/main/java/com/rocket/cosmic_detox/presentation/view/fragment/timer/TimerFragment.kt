package com.rocket.cosmic_detox.presentation.view.fragment.timer

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!
    private var time = 0 // 시간
    private val handler = Handler(Looper.getMainLooper()) // 메인 스레드에서 실행할 핸들러
    private var isTimerRunning = false // 타이머가 실행 여부
    private var isPinningRequested = false // 앱 고정 요청 여부

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
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            userViewModel.totalTimeState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                    }
                    is UiState.Success -> {
                        time = state.data.toInt()
                        updateTime()
                    }
                    is UiState.Failure -> {
                        showError(state.e?.message)
                    }
                    is UiState.Init -> {
                    }
                }
            }
        }
    }

    private fun showError(message: String?) {
        val dialog = OneButtonDialogFragment(
            title = message ?: getString(R.string.dialog_common_error),
            onClickConfirm = {
                // 오류처리 코드
            }
        )
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, "ErrorDialog")
    }

    override fun onResume() {
        super.onResume()
        updateButtonState()

        // Receiver 등록
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        requireContext().registerReceiver(appSwitchReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
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

        btnTimerStart.setOnClickListener {
            // 버튼 텍스트를 "타이머 시작"으로 변경 , 다른 로직 추가해야 될 수도 ?
            binding.btnTimerStart.text = getString(R.string.timer_button_start)

            // 앱이 고정되어 있는지 확인
            if (isAppPinned()) {
                startTimer()
            } else {
                if (!isPinningRequested) {
                    showPinningRequestDialog()
                }
            }
        }

    }

    private fun updateButtonState() = with(binding) {
        if (isAppPinned()) {
            btnTimerStart.text = getString(R.string.timer_button_start)
        } else {
            btnTimerStart.text = getString(R.string.timer_button_pinning)
        }
    }

    private fun showPinningRequestDialog() {
        val dialog = TwoButtonDialogFragment(
            title = getString(R.string.timer_dialog_pinning_request),
            onClickConfirm = {
                startAppPinning()
                isPinningRequested = true
            },
            onClickCancel = {
                // 사용자가 앱 고정을 거부한 경우 ? , 처리 해야 될 코드 필요 할수도
            }
        )
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, "PinningRequestDialog")
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

    private fun startTimer() {
        if (!isTimerRunning) {
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

    fun showTwoButtonDialog() {
        val dialog = OneButtonDialogFragment(
            getString(R.string.dialog_common_focus)
        ) {
            navigateToHomeFragment() // 홈 화면으로 이동
        }
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, "ConfirmDialog")
    }

    fun navigateToHomeFragment() {
        findNavController().navigate(R.id.navigation_home)
    }

    private fun isAppPinned(): Boolean {
        val activityManager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val isPinned = activityManager.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
        return isPinned
    }
}