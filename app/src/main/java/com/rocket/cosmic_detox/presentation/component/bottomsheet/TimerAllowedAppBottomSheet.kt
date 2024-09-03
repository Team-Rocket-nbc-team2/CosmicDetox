package com.rocket.cosmic_detox.presentation.component.bottomsheet

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.rocket.cosmic_detox.presentation.view.fragment.timer.BottomSheetState
import com.rocket.cosmic_detox.presentation.view.fragment.timer.TimerFragment
import com.rocket.cosmic_detox.presentation.viewmodel.AllowedAppViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TimerAllowedAppBottomSheet : BottomSheetDialogFragment() {

    private val modalBottomSheetIconBinding by lazy { ModalBottomsheetIconBinding.inflate(layoutInflater) }
    private lateinit var modalContentAllowedAppBinding: ModalContentAllowedAppBinding
    private val allowedAppViewModel: AllowedAppViewModel by viewModels<AllowedAppViewModel>()
    private var isChecked = false

    private val adapter by lazy {
        AllowedAppAdapter(requireContext()) { packageId, limitedTime ->
            isChecked = true
            val intent = context?.packageManager?.getLaunchIntentForPackage(packageId)
            context?.startActivity(intent)

            initCountDownTimer(packageId, 5) // TODO: limitedTime.toLong()으로 변경
            allowedAppViewModel.setSelectedAllowedAppPackage(packageId)
        }
    }
    private var countDownTimer: CountDownTimer? = null
    private var overlayView: View? = null
    private var isOverlayVisible = false
    private val windowManager by lazy {
        requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d("onActivityResult", "onActivityResult")
                dismiss() // BottomSheet를 닫습니다.
            } else {
                Log.d("onActivityResult", "onActivityResult fail")
            }
        }
    }

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
            BottomSheetState.setIsBottomSheetOpen(false)
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

        // 오버레이가 표시된 상태에서 돌아왔을 경우 오버레이 제거 및 BottomSheet 닫기
        if (isOverlayVisible) {
            Log.d("isOverlayVisible", "$isOverlayVisible true")
            isChecked = false
            removeOverlay()
            //dismiss()
        } else {
           Log.d("isOverlayVisible", "$isOverlayVisible false")
        }

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

    private fun initCountDownTimer(packageId: String, initTimer: Long) {
        countDownTimer = object : CountDownTimer(initTimer * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                allowedAppViewModel.updateRemainTime((millisUntilFinished / 1000).toInt())
            }

            override fun onFinish() {
                showOverlay()
                allowedAppViewModel.updateLimitedTimeAllowApp(packageId, 0, failCallback = {})
            }
        }
        countDownTimer?.start()
    }

    private fun showOverlay() {
        if (!isOverlayVisible && Settings.canDrawOverlays(requireContext())) {
            val overlayParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )

            overlayView = LayoutInflater.from(requireContext()).inflate(R.layout.activity_dialog, null)

            overlayView?.let {
                it.findViewById<Button>(R.id.btn_back).setOnClickListener {
                    //removeOverlay() -> onResume에서 처리하는 걸로 변경
                    returnToTimer()
                }

                windowManager.addView(it, overlayParams)
                isOverlayVisible = true
                Log.d("isOverlayVisible", " showOverlay $isOverlayVisible")
            }
        }
    }

    private fun returnToTimer() {
        val intent = Intent(requireContext(), requireActivity()::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        startActivity(intent)
    }

    private fun removeOverlay() {
        overlayView?.let {
            windowManager.removeView(it)
            isOverlayVisible = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        BottomSheetState.setIsBottomSheetOpen(false)
        removeOverlay()
    }

    override fun onPause() {
        super.onPause()
        if (!isChecked) {
            showOverlay()
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChecked) {
            showOverlay()
        }
    }
}
