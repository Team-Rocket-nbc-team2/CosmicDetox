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

            initCountDownTimer(packageId, 5) // TODO: limitedTime.toLong()으로 변경, 지금은 임시로 5초로 설정되어있음. 근데 limitedTime으로 바꾸고 허용앱에서 오래 있어도 우리 앱 초기화 안될라나.
                                                    // 핸드폰 쓰레기면 램 용량 적어서 다시 로그인 화면으로 이동할 수도 있지 않나.. 일단 작동은 잘됨.
            allowedAppViewModel.setSelectedAllowedAppPackage(packageId)
        }
    }
    private var countDownTimer: CountDownTimer? = null
    private var overlayView: View? = null
    private var isOverlayVisible = false
    private val windowManager by lazy {
        requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent> // 이거 필요없을 듯

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityResultLauncher = registerForActivityResult( // 이거 필요없을 듯
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
        if (isOverlayVisible) { // 밑의 onPause 쪽에서 설명해놓음
            Log.d("isOverlayVisible", "$isOverlayVisible true")
            isChecked = false
            removeOverlay()
            //dismiss() // 근데 처음엔 이거 있어야 돌아와서 뒤로가기 하면 BottomSheet랑 타이머가 같이 동시에 닫히고 홈이나 로그인으로 이동해버려서 추가했었는데, 막상 또 지금은 잘돼서 주석 처리
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
                showOverlay() // 남은 시간이 다 되면 오버레이 띄우기 -> 지금은 5초로 설정되어 있음
                allowedAppViewModel.updateLimitedTimeAllowApp(packageId, 0, failCallback = {})
            }
        }
        countDownTimer?.start()
    }

    private fun showOverlay() {
        if (!isOverlayVisible && Settings.canDrawOverlays(requireContext())) { // 오버레이가 뜨지 않았고 오버레이 권한이 허용되어 있을 때
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
                    //removeOverlay() -> onResume에서 처리하는 걸로 변경, 버튼 클릭하자마자 오버레이 뷰를 없애는 게 아니라 우리 앱으로 돌아와서 onResume에서 처리(일단 주석처리 해놓음). 없애도 될 듯?
                    returnToTimer() // 오버레이 뷰에서 "이전화면으로 돌아가기" 버튼 클릭 시 타이머 화면으로 이동
                }

                windowManager.addView(it, overlayParams)   // 중요!!!!) 오버레이 뷰를 추가
                isOverlayVisible = true                     // 중요!!!!) 오버레이가 뜨면 true로 변경
                Log.d("isOverlayVisible", " showOverlay $isOverlayVisible")
            }
        }
    }

    private fun returnToTimer() { // 오버레이뷰에서 "이전화면으로 돌아가기" 버튼 클릭 시 타이머 화면으로 이동? (이거 잘 모르겠지만 이렇게 해야 동작은 함)
        val intent = Intent(requireContext(), requireActivity()::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        startActivity(intent)
    }

    private fun removeOverlay() { // 허용 앱 사용하다가 다시 돌아올 때 onResume에서 오버레이 제거하고 isChecked도 다시 false로 변경
        overlayView?.let {
            windowManager.removeView(it)
            isOverlayVisible = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        BottomSheetState.setIsBottomSheetOpen(false) // BottomSheet가 닫히면 BottomSheetState를 닫힌 상태로 변경
        removeOverlay()
    }

    override fun onPause() {
        super.onPause()
        // 이거 조건문 없이 바로 showOverlay() 호출하면 허용 앱으로 이동할 때마다 오버레이가 뜸
        // 리사이클러뷰 아이템 클릭 시 isChecked를 true로 변경하고 허용 앱 이동하게 하면 밑의 조건문이 실행되지 않아서 오버레이가 뜨지 않음
        // 허용 앱 사용하다가 다시 돌아올 때 onResume에서 오버레이 제거하고 isChecked도 다시 false로 변경
        if (!isChecked) {
            showOverlay()
        }
    }

    override fun onStop() {
        super.onStop()
        // 이것도 onPause랑 같은 이유로 추가, 일단 생명주기가 정확히 어디서 끝나고 어디서 시작하는지 모르겠어서 추가
        if (!isChecked) {
            showOverlay()
        }
    }
}
