package com.rocket.cosmic_detox.presentation.view.fragment.timer

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.FragmentTimerBinding
import com.rocket.cosmic_detox.presentation.component.bottomsheet.TimerAllowedAppBottomSheet
import com.rocket.cosmic_detox.presentation.component.dialog.OneButtonDialogFragment
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogFragment
import com.rocket.cosmic_detox.presentation.uistate.GetListUiState
import com.rocket.cosmic_detox.presentation.uistate.UiState
import com.rocket.cosmic_detox.presentation.view.AppMonitorService
import com.rocket.cosmic_detox.presentation.view.activity.DialogActivity
import com.rocket.cosmic_detox.presentation.view.fragment.timer.BottomSheetState.isBottomSheetOpen
import com.rocket.cosmic_detox.presentation.view.viewmodel.UserViewModel
import com.rocket.cosmic_detox.presentation.viewmodel.AllowedAppViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!
    private var isFinishingTimer = false
    private var time = 0 // 시간
    private val handler = Handler(Looper.getMainLooper())
    private var isTimerRunning = false

    private val userViewModel: UserViewModel by viewModels()
    private val allowedAppViewModel: AllowedAppViewModel by viewModels<AllowedAppViewModel>()

    private val runnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 1000)
        }
    }

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var isOverlayVisible = false

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Settings.canDrawOverlays(requireContext())) {
            // 오버레이 권한이 허용된 경우 수행할 작업
            showOverlay()
        } else {
            // 권한이 거부된 경우 처리
            Toast.makeText(requireContext(), "오버레이 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }
    private var allowedAppList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        windowManager = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkAndRequestOverlayPermission()
        initView()
        allowedAppViewModel.getAllAllowedApps()
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), backPressedCallBack)

        observeViewModel()

        userViewModel.fetchTotalTime()
        startTimer()
    }

    private fun startAppMonitorService() {
        Log.d("AllowedAppList", "startAppMonitorService!!!!!!!!!1 : $allowedAppList")
        val intent = Intent(requireContext(), AppMonitorService::class.java).apply {
            putStringArrayListExtra("ALLOWED_APP_LIST", allowedAppList as ArrayList<String>)
            putExtra("asdf", "asdf")
        }
        requireContext().startService(intent)
    }

    private fun stopAppMonitorService() {
        val intent = Intent(requireContext(), AppMonitorService::class.java)
        requireContext().stopService(intent)
    }

    private fun checkAndRequestOverlayPermission() {
        if (!Settings.canDrawOverlays(requireContext())) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${requireContext().packageName}")
            )
            overlayPermissionLauncher.launch(intent)
        } else {
            // 이미 권한이 허용된 경우 바로 오버레이를 띄움
            showOverlay()
        }
    }


    override fun onPause() {
        super.onPause()
        if (!isOverlayVisible) {
            requestOverlayPermission()
        }
        stopTimer()
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(requireContext())) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${requireContext().packageName}")
            )
            overlayPermissionLauncher.launch(intent)
        } else {
            if (!BottomSheetState.getIsBottomSheetOpen()) {
                showOverlay()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isOverlayVisible) {
            removeOverlay()
        }
        if (!isFinishingTimer && !isTimerRunning) {
            startTimer()
        }
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
                    removeOverlay()
                    returnToTimer()
                }

                windowManager.addView(it, overlayParams)
                isOverlayVisible = true
            }
        }
    }

    private fun returnToTimer() {
        // 현재 Activity를 포그라운드로 가져옴
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
        _binding = null
        stopTimer()
        removeOverlay()
        stopAppMonitorService()
    }

    private fun initView() = with(binding) {
        btnTimerFinish.setOnClickListener {
            val dialog = TwoButtonDialogFragment(
                title = getString(R.string.timer_dialog_finish),
                onClickConfirm = {
                    isFinishingTimer = true
                    stopTimer()
                    findNavController().popBackStack()
                },
                onClickCancel = { false }
            )
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, "ConfirmDialog")
            stopAppMonitorService()
        }

        btnTimerRest.setOnClickListener {
            val bottomSheet = TimerAllowedAppBottomSheet()
            bottomSheet.show(parentFragmentManager, "BottomSheet")
            BottomSheetState.setIsBottomSheetOpen(true)
        }
    }

    private val backPressedCallBack = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showTwoButtonDialog()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            userViewModel.totalTimeState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        // 추후 작업필요할 떄 활용 로딩 상태 처리 (예: ProgressBar 표시)
                    }
                    is UiState.Success -> {
                        time = state.data.toInt()
                        updateTime()
                    }
                    is UiState.Failure -> {
                        showError(state.e?.message)
                    }
                    is UiState.Init -> {
                        // 초기 상태 처리코드 추후 작성
                    }
                }
            }
        }
        lifecycleScope.launch {
            allowedAppViewModel.allowedAppList.collectLatest {
                if (it is GetListUiState.Success) {
                    allowedAppList.clear()
                    it.data.forEach { allowedApp ->
                        allowedAppList.add(allowedApp.packageId)
                    }
                    Log.d("AllowedAppList", "앱 리스트 가져오기 성공!!!!!!!!!1 : $allowedAppList")
                    startAppMonitorService()
                } else if (it is GetListUiState.Failure) {
                    Log.e("AllowedAppList", "앱 리스트 가져오기 실패!!!!!!!!!1")
                } else if (it is GetListUiState.Empty) {
                    Log.d("AllowedAppList", "앱 리스트가 비어있습니다!!!!!!!!")
                }
            }
        }
    }

    private fun showError(message: String?) {
        val dialog = OneButtonDialogFragment(
            title = message ?: getString(R.string.dialog_common_error),
            onClickConfirm = {
                // 오류처리는 추후에 작성
                // ex "확인" 버튼 클릭 시 실행할 작업 여기에 작성하면됨
            }
        )
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, "ErrorDialog")
    }


    private fun showTwoButtonDialog() {
        val dialog = OneButtonDialogFragment(
            getString(R.string.dialog_common_focus)
        ) {
            // 화면 강제 유지하는 코드
        }
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, "ConfirmDialog")
    }

    private fun startTimer() {
        if (!isTimerRunning) {
            handler.post(runnable)
            isTimerRunning = true
        }
    }

    private fun stopTimer() {
        handler.removeCallbacks(runnable)
        isTimerRunning = false

        // 타이머가 중지될 때 totalTime을 Firestore에 저장
        userViewModel.updateTotalTime(time.toLong())
    }

    private fun updateTime() {
        time++
        val hours = time / 3600
        val minutes = (time % 3600) / 60
        val seconds = time % 60
        binding.tvTimerTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}

object BottomSheetState {
    private var isBottomSheetOpen = false

    fun setIsBottomSheetOpen(value: Boolean) {
        Log.d("BottomSheetState", "isBottomSheetOpen: $value")
        isBottomSheetOpen = value
    }

    fun getIsBottomSheetOpen(): Boolean {
        return isBottomSheetOpen
    }
}
