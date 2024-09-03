package com.rocket.cosmic_detox.presentation.component.bottomsheet

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ModalBottomsheetIconBinding
import com.rocket.cosmic_detox.databinding.ModalContentAllowedAppBinding
import com.rocket.cosmic_detox.presentation.component.bottomsheet.adapter.AllowedAppAdapter
import com.rocket.cosmic_detox.presentation.uistate.GetListUiState
import com.rocket.cosmic_detox.presentation.viewmodel.AllowedAppViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TimerAllowedAppBottomSheet: BottomSheetDialogFragment() {
    private val modalBottomSheetIconBinding by lazy { ModalBottomsheetIconBinding.inflate(layoutInflater) }
    private lateinit var modalContentAllowedAppBinding: ModalContentAllowedAppBinding
    private val allowedAppViewModel: AllowedAppViewModel by viewModels<AllowedAppViewModel>()

    private val adapter by lazy {
        AllowedAppAdapter(requireContext()) { packageId, limitedTime ->
            val intent = context?.packageManager?.getLaunchIntentForPackage(packageId)
            context?.startActivity(intent)

            initCountDownTimer(packageId, limitedTime.toLong())
            allowedAppViewModel.setSelectedAllowedAppPackage(packageId)
        }
    }
    private var countDownTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        modalBottomSheetIconBinding.bottomSheetBody.layoutResource = R.layout.modal_content_allowed_app
        val viewStub = modalBottomSheetIconBinding.bottomSheetBody.inflate()
        modalContentAllowedAppBinding = ModalContentAllowedAppBinding.bind(viewStub)

        modalBottomSheetIconBinding.tvBottomSheetTitle.text = getString(R.string.timer_bottom_sheet_title)
        modalBottomSheetIconBinding.ivBottomSheetClose.setOnClickListener {
            dismiss()
        }

        modalContentAllowedAppBinding.rvAllowedAppList.adapter = adapter
        modalContentAllowedAppBinding.rvAllowedAppList.layoutManager = LinearLayoutManager(context)

        allowedAppViewModel.getAllAllowedApps()
        observeAllowAppList()
        return modalBottomSheetIconBinding.root
    }

    override fun onResume() {
        super.onResume()

        val remainTime = allowedAppViewModel.countDownRemainTime.value
        val selectedPackageId = allowedAppViewModel.selectedAllowedAppPackage.value
        if (remainTime != null && countDownTimer != null && selectedPackageId != null) {
            allowedAppViewModel.updateLimitedTimeAllowApp(
                packageId = selectedPackageId,
                remainTime = remainTime,
                failCallback = {}
            )
            allowedAppViewModel.getAllAllowedApps()

            countDownTimer?.cancel()
        }
    }

    private fun observeAllowAppList() = with(modalContentAllowedAppBinding) {
        lifecycleScope.launch {
            allowedAppViewModel.allowedAppList.collectLatest {
                tvAllowedAppIsEmpty.isVisible = it is GetListUiState.Empty
                indicatorDataLoading.isVisible = it is GetListUiState.Loading
                rvAllowedAppList.isVisible = it is GetListUiState.Success

                if (it is GetListUiState.Success) {
                    adapter.submitList(it.data)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            setUpRatio(bottomSheetDialog)
        }
        return dialog
    }

    private fun setUpRatio(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View
        val behavior = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = getWindowHeight()

        bottomSheet.layoutParams = layoutParams
        behavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isDraggable = false
        }
    }

    private fun getWindowHeight(): Int {
        val wm = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = wm.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            return windowMetrics.bounds.height() - insets.bottom - insets.top
        } else {
            val displayMetrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.heightPixels
        }
    }

    private fun initCountDownTimer(packageId: String ,initTimer: Long) {
        countDownTimer = object: CountDownTimer(initTimer * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                allowedAppViewModel.updateRemainTime((millisUntilFinished / 1000).toInt())
            }

            override fun onFinish() {
                val toFrontIntent = context?.packageManager?.getLaunchIntentForPackage(requireContext().packageName)
                    ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context?.startActivity(toFrontIntent)
                allowedAppViewModel.updateLimitedTimeAllowApp(packageId, 0, failCallback = {})
            }
        }

        countDownTimer?.start()
    }
}
