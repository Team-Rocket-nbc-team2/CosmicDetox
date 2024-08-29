package com.rocket.cosmic_detox.presentation.view.fragment

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    }


    /**
     *  TODO : onPause에서 실행하여 Dialog는 뜨나 홈키, 메뉴키를 막는 방법 필요
     *  */
    override fun onPause() {
        super.onPause()
        showTwoButtonDialog()
    }

    // onUserLeaveHint


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

                findNavController().popBackStack()
            }
            // 알림창이 띄워져있는 동안 배경 클릭 막기
            dialog.isCancelable = false
            dialog.show(getParentFragmentManager(), "ConfirmDialog")
        }

        btnTimerRest.setOnClickListener {
            val bottomSheet = TimerAllowedAppBottomSheet()
            bottomSheet.show(getParentFragmentManager(), "BottomSheet")
        }
    }

    private val backPressedCallBack = object : OnBackPressedCallback(true) {
        @SuppressLint("NotifyDataSetChanged")
        override fun handleOnBackPressed() {
            showTwoButtonDialog()
        }
    }

    private fun showTwoButtonDialog(){
        val dialog = OneButtonDialogFragment(
            getString(R.string.dialog_common_focus)
        ){
            // 화면 강제 유지하는 코드
        }
        // 알림창이 띄워져있는 동안 배경 클릭 막기
        dialog.isCancelable = false
        dialog.show(getParentFragmentManager(), "ConfirmDialog")
    }
}