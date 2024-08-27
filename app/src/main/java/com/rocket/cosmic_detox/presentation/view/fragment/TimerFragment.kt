package com.rocket.cosmic_detox.presentation.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.FragmentTimerBinding
import com.rocket.cosmic_detox.presentation.component.bottomsheet.MyPageModifyAllowAppBottomSheet
import com.rocket.cosmic_detox.presentation.component.bottomsheet.MyPageSetLimitAppBottomSheet
import com.rocket.cosmic_detox.presentation.component.bottomsheet.TimerAllowedAppBottomSheet
import com.rocket.cosmic_detox.presentation.component.dialog.OneButtonDialogFragment
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogFragment

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

    override fun onStop() {
        super.onStop()
        /**
         *  TODO : 현재 디톡스를 계속 유지 해야 되는지 state를 추가하여 state가 true일 때만 해당 showTwoButtonDialog() 작동 하도록 구현 필요
         *  현재는 종료버튼을 눌러도 아래 집중하세요! Dialog 계속 노출 되는 중
         *  */
        showTwoButtonDialog()
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