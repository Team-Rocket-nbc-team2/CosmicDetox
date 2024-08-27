package com.rocket.cosmic_detox.presentation.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.FragmentTimerBinding
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
    }

    private fun initView() = with(binding) {
        btnTimerFinish.setOnClickListener {
            val dialog = TwoButtonDialogFragment(
                getString(R.string.timer_dialog_finish)
            ) {
                // timer 종료하기
            }
            // 알림창이 띄워져있는 동안 배경 클릭 막기
            dialog.isCancelable = false
            dialog.show(getParentFragmentManager(), "ConfirmDialog")

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}