package com.rocket.cosmic_detox.presentation.component.bottomsheet

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.data.datasource.remote.model.AllowedApp
import com.rocket.cosmic_detox.databinding.ModalBottomsheetIconBinding
import com.rocket.cosmic_detox.databinding.ModalContentAllowedAppBinding
import com.rocket.cosmic_detox.presentation.component.bottomsheet.adapter.AllowedAppAdapter
import com.rocket.cosmic_detox.presentation.service.AllowedAppMonitorService
import com.rocket.cosmic_detox.presentation.uistate.GetListUiState
import com.rocket.cosmic_detox.presentation.view.activity.MainActivity
import com.rocket.cosmic_detox.presentation.view.fragment.timer.BottomSheetState
import com.rocket.cosmic_detox.presentation.viewmodel.AllowedAppViewModel
import com.rocket.cosmic_detox.presentation.viewmodel.PermissionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TimerAllowedAppBottomSheet : BottomSheetDialogFragment() {
    private val modalBottomSheetIconBinding by lazy { ModalBottomsheetIconBinding.inflate(layoutInflater) }
    private lateinit var modalContentAllowedAppBinding: ModalContentAllowedAppBinding

    private val allowedAppViewModel: AllowedAppViewModel by viewModels<AllowedAppViewModel>()
    private val permissionViewModel: PermissionViewModel by viewModels()
    private var cachedAllowedApps: List<AllowedApp> = emptyList()
    private var overlayView: View? = null
    private var isLaunchingAllowedApp = false
    private var isSheetClosing = false

    private val windowManager by lazy { requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private val adapter by lazy {
        AllowedAppAdapter(requireContext()) { packageId, limitedTime, appName ->
            isLaunchingAllowedApp = true
            val intent = context?.packageManager?.getLaunchIntentForPackage(packageId)
            context?.startActivity(intent)

            requireContext().startService(
                AllowedAppMonitorService.createStartIntent(
                    context = requireContext(),
                    packageId = packageId,
                    remainTime = limitedTime,
                    appName = appName
                )
            )
        }
    }
    //private var countDownTimer: CountDownTimer? = null

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
            isSheetClosing = true
            BottomSheetState.setIsBottomSheetOpen(false)
            dismiss()
        }

        modalContentAllowedAppBinding.rvAllowedAppList.adapter = adapter
        modalContentAllowedAppBinding.rvAllowedAppList.layoutManager = LinearLayoutManager(context)

        allowedAppViewModel.getAllAllowedApps()
        observeAllowAppList()
        observeServiceRemainTime()
        return modalBottomSheetIconBinding.root
    }

    override fun onResume() {
        super.onResume()
        removeOverlayIfNeeded()
        isLaunchingAllowedApp = false
        isSheetClosing = false

        if (AllowedAppMonitorService.isServiceActive.value) {
            requireContext().startService(
                AllowedAppMonitorService.createUiReadyIntent(requireContext())
            )
            // 서비스 종료 직전에 UI에 마지막 남은 시간을 먼저 반영
            updateRunningAppRemainTime(
                running = true,
                packageId = AllowedAppMonitorService.currentPackageIdState.value,
                remainTime = AllowedAppMonitorService.remainTime.value
            )
            requireContext().startService(
                AllowedAppMonitorService.createStopIntent(requireContext())
            )
            allowedAppViewModel.getAllAllowedApps()
        }
    }

    override fun onPause() {
        super.onPause()
        if (shouldShowOverlayOnPause()) {
            showOverlay()
        }
    }

    private fun observeAllowAppList() = with(modalContentAllowedAppBinding) {
        lifecycleScope.launch {
            allowedAppViewModel.allowedAppList.collectLatest {
                tvAllowedAppIsEmpty.isVisible = it is GetListUiState.Empty
                indicatorDataLoading.isVisible = it is GetListUiState.Loading
                rvAllowedAppList.isVisible = it is GetListUiState.Success

                if (it is GetListUiState.Success) {
                    cachedAllowedApps = it.data
                    updateRunningAppRemainTime(
                        running = AllowedAppMonitorService.isServiceActive.value,
                        packageId = AllowedAppMonitorService.currentPackageIdState.value,
                        remainTime = AllowedAppMonitorService.remainTime.value
                    )
                }
            }
        }
    }

    private fun observeServiceRemainTime() {
        lifecycleScope.launch {
            combine(
                AllowedAppMonitorService.isServiceActive,
                AllowedAppMonitorService.currentPackageIdState,
                AllowedAppMonitorService.remainTime
            ) { running, packageId, remainTime ->
                Triple(running, packageId, remainTime)
            }.collectLatest { (running, packageId, remainTime) ->
                updateRunningAppRemainTime(running, packageId, remainTime)
            }
        }
    }

    private fun updateRunningAppRemainTime(
        running: Boolean,
        packageId: String?,
        remainTime: Long
    ) {
        if (cachedAllowedApps.isEmpty()) return

        val displayList = if (running && !packageId.isNullOrBlank()) {
            cachedAllowedApps.map { app ->
                if (app.packageId == packageId) app.copy(limitedTime = remainTime) else app
            }
        } else {
            cachedAllowedApps
        }
        adapter.submitList(displayList)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) { // 뒤로 가기 버튼(KEYCODE_BACK)이 눌린 순간(ACTION_DOWN), ACTION_UP은 떼어졌을 때 둘다 작동은 하는 듯?
                isSheetClosing = true
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

    private fun shouldShowOverlayOnPause(): Boolean {
        if (isSheetClosing) return false
        if (isLaunchingAllowedApp) return false
        if (AllowedAppMonitorService.isServiceActive.value) return false
        if (!BottomSheetState.getIsBottomSheetOpen()) return false
        return permissionViewModel.isOverlayPermissionGranted(requireContext()) &&
                Settings.canDrawOverlays(requireContext())
    }

    private fun showOverlay() {
        if (overlayView != null) return

        val overlayParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        overlayView = LayoutInflater.from(requireContext()).inflate(R.layout.activity_dialog, null)
        overlayView?.findViewById<Button>(R.id.btn_back)?.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            removeOverlayIfNeeded()
        }
        windowManager.addView(overlayView, overlayParams)
    }

    private fun removeOverlayIfNeeded() {
        overlayView?.let { view ->
            if (view.isAttachedToWindow) {
                windowManager.removeView(view)
            }
            overlayView = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isSheetClosing = true
        removeOverlayIfNeeded()
        BottomSheetState.setIsBottomSheetOpen(false) // 바텀시트에서 뒤로가기 눌렀을 때도 isBottomSheetOpen을 false로 변경
    }
}
