package com.rocket.cosmic_detox.presentation.component.bottomsheet

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
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
import com.rocket.cosmic_detox.presentation.service.AlarmService
import com.rocket.cosmic_detox.presentation.component.bottomsheet.adapter.AllowedAppAdapter
import com.rocket.cosmic_detox.presentation.uistate.GetListUiState
import com.rocket.cosmic_detox.presentation.view.activity.MainActivity
import com.rocket.cosmic_detox.presentation.view.fragment.timer.BottomSheetState
import com.rocket.cosmic_detox.presentation.viewmodel.AllowedAppViewModel
import com.rocket.cosmic_detox.presentation.viewmodel.PermissionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TimerAllowedAppBottomSheet : BottomSheetDialogFragment() {
    private val modalBottomSheetIconBinding by lazy { ModalBottomsheetIconBinding.inflate(layoutInflater) }
    private lateinit var modalContentAllowedAppBinding: ModalContentAllowedAppBinding

    private val allowedAppViewModel: AllowedAppViewModel by viewModels<AllowedAppViewModel>()
    private val permissionViewModel: PermissionViewModel by viewModels()

    private var isChecked = false

    private var rootView: View? = null
    private val windowManager by lazy { requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private val adapter by lazy {
        AllowedAppAdapter(requireContext()) { packageId, limitedTime ->
            isChecked = true
            val intent = context?.packageManager?.getLaunchIntentForPackage(packageId)
            context?.startActivity(intent)

            initCountDownTimer(limitedTime)
            allowedAppViewModel.setSelectedAllowedAppPackage(packageId)
            allowedAppViewModel.startObserveAppOpenRunnable()
            val runnable = allowedAppViewModel.initObserveAppOpenRunnable(packageId) {
                if (rootView == null) showOverlay()
            }
            val thread = Thread(runnable)
            thread.start()
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
            allowedAppViewModel.stopObserveAppOpenRunnable()
        }
    }

    override fun onStop() {
        super.onStop()

        if (!allowedAppViewModel.running.value && BottomSheetState.getIsBottomSheetOpen()) {
            if (rootView == null) showOverlay()
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

        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) { // 뒤로 가기 버튼(KEYCODE_BACK)이 눌린 순간(ACTION_DOWN), ACTION_UP은 떼어졌을 때 둘다 작동은 하는 듯?
                BottomSheetState.setIsBottomSheetOpen(false)
                dismiss()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            return windowMetrics.bounds.height() - insets.bottom - insets.top
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.heightPixels
        }
    }

    private fun initCountDownTimer(initTimer: Long) {
        var state = false
        val isPostNotificationGrantedAllowed = permissionViewModel.isPostNotificationGranted(requireContext())

        countDownTimer = object : CountDownTimer(initTimer * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // 여기여기
                allowedAppViewModel.updateRemainTime((millisUntilFinished / 1000).toInt())
                if(isPostNotificationGrantedAllowed){
                    if(millisUntilFinished < 300000 && !state){ // 원래는 300000
                        state = true
                        val serviceIntent = Intent(requireActivity(), AlarmService::class.java)
                        requireActivity().startService(serviceIntent)
                        Toast.makeText(requireActivity(), "Service start", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFinish() {
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }

        countDownTimer?.start()
    }

    private fun showOverlay() {
        if (Settings.canDrawOverlays(context)) {
            val overlayParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )
            rootView = LayoutInflater.from(context).inflate(R.layout.activity_dialog, null)
            windowManager.addView(rootView, overlayParams)

            rootView!!.findViewById<Button>(R.id.btn_back).setOnClickListener {
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                context?.startActivity(intent)

                removeOverlay()
            }
        }
    }

    private fun removeOverlay() {
        windowManager.removeView(rootView)
        rootView = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        BottomSheetState.setIsBottomSheetOpen(false) // 바텀시트에서 뒤로가기 눌렀을 때도 isBottomSheetOpen을 false로 변경
    }
}
