package com.rocket.cosmic_detox.presentation.view.fragment.timer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.FragmentTimerBinding
import com.rocket.cosmic_detox.presentation.component.bottomsheet.TimerAllowedAppBottomSheet
import com.rocket.cosmic_detox.presentation.component.dialog.OneButtonDialogFragment
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import android.os.Handler
import android.os.Looper



@AndroidEntryPoint
class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!
    private var isFinishingTimer = false
    private var time = 0 // 시간 경과를 저장할 변수
    private val handler = Handler(Looper.getMainLooper()) // 메인 스레드에서 실행할 핸들러
    private var isTimerRunning = false // 타이머가 실행 중인지 확인하는 변수
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
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), backPressedCallBack)

        startTimer()  // 뷰가 생성될 때 타이머 바로 시작
    }

    override fun onPause() {
        super.onPause()
        if (!isFinishingTimer) {
            showTwoButtonDialog()
        }
        stopTimer()  // 프래그먼트가 일시 정지될 때 타이머를 중지
    }

    override fun onResume() {
        super.onResume()
        if (!isFinishingTimer && !isTimerRunning) {  // 타이머가 실행 중이 아닌 경우에만 시작
            startTimer()  // 프래그먼트가 다시 활성화되면 타이머를 재개
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        stopTimer()  // 뷰가 파괴될 때 타이머를 중지합니다.
    }

    private fun initView() = with(binding) {
        btnTimerFinish.setOnClickListener {
            val dialog = TwoButtonDialogFragment(
                getString(R.string.timer_dialog_finish)
            ) {
                isFinishingTimer = true
                stopTimer() // 타이머를 종료하는 버튼이 눌릴 때 타이머를 중지
                findNavController().popBackStack()
            }
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, "ConfirmDialog")
        }

        btnTimerRest.setOnClickListener {
            val bottomSheet = TimerAllowedAppBottomSheet()
            bottomSheet.show(parentFragmentManager, "BottomSheet")
        }
    }

    private val backPressedCallBack = object : OnBackPressedCallback(true) {
        @SuppressLint("NotifyDataSetChanged")
        override fun handleOnBackPressed() {
            showTwoButtonDialog()
        }
    }

    private fun showTwoButtonDialog() {
        val dialog = OneButtonDialogFragment(
            getString(R.string.dialog_common_focus)
        ) {
            // 화면 강제 유지하는 코드
        }
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, "ConfirmDialog")
    }


    private fun startTimer() {
        if (!isTimerRunning) {  // 타이머가 실행 중이 아닌 경우에만 시작
            handler.post(runnable)
            isTimerRunning = true  // 타이머 실행 상태를 true로 설정
        }
    }

    private fun stopTimer() {
        handler.removeCallbacks(runnable)
        isTimerRunning = false  // 타이머 실행 상태를 false로 설정
    }

    private fun updateTime() {
        time++ // 시간
        val minutes = time / 60
        val seconds = time % 60
        binding.tvTimerTime.text = String.format("%02d:%02d", minutes, seconds) // TextView 업데이트
    }
}

