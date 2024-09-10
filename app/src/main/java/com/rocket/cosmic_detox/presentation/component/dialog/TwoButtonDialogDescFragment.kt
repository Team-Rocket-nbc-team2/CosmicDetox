package com.rocket.cosmic_detox.presentation.component.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.rocket.cosmic_detox.databinding.DialogTwobuttonBinding
import com.rocket.cosmic_detox.databinding.DialogTwobuttonDescBinding

class TwoButtonDialogDescFragment(
    private val title: String,
    private val description: String,
    private val onClickConfirm: () -> Unit,
    private val onClickCancel: () -> Unit
) : DialogFragment() {
    private var _binding: DialogTwobuttonDescBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTwobuttonDescBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() = with(binding) {
        tvDialogTitle.text = title
        tvDialogDesc.text = description

        btnDialogCancel.setOnClickListener {
            onClickCancel()
            dismiss()
        }

        btnDialogConfirm.setOnClickListener {
            onClickConfirm()
            dismiss()
        }
    }


    override fun onResume() {
        super.onResume()
        // full Screen code
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }
}