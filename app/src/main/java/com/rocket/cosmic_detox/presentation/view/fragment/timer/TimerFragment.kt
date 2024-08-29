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

@AndroidEntryPoint
class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!
    private var isFinishingTimer = false // 타이머 종료 체크 변수

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
    }

    override fun onPause() {
        super.onPause()
        if (!isFinishingTimer) { // 타이머 종료 중이 아닌 경우에만 다이얼로그 표시
            showTwoButtonDialog()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView() = with(binding) {
        btnTimerFinish.setOnClickListener {
            val dialog = TwoButtonDialogFragment(
                getString(R.string.timer_dialog_finish)
            ) {
                // timer 종료하기
                isFinishingTimer = true // 타이머 종료 플래그 설정
                findNavController().popBackStack()
            }
            // 알림창이 띄워져있는 동안 배경 클릭 막기
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
        // 알림창이 띄워져있는 동안 배경 클릭 막기
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, "ConfirmDialog")
    }
}
