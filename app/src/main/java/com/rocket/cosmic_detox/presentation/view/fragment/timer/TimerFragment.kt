package com.rocket.cosmic_detox.presentation.view.fragment.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.FragmentTimerBinding
import com.rocket.cosmic_detox.presentation.component.bottomsheet.TimerAllowedAppBottomSheet
import com.rocket.cosmic_detox.presentation.component.dialog.OneButtonDialogFragment
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogFragment
import com.rocket.cosmic_detox.presentation.service.TimerService
import com.rocket.cosmic_detox.presentation.uistate.UiState
import com.rocket.cosmic_detox.presentation.view.activity.MainActivity
import com.rocket.cosmic_detox.presentation.viewmodel.PermissionViewModel
import com.rocket.cosmic_detox.presentation.viewmodel.UserViewModel
import com.rocket.cosmic_detox.util.SharedPreferencesUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri

@AndroidEntryPoint
class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!
    private var isFinishingTimer = false
    private var isTimerServiceStarted = false

    private val userViewModel: UserViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var isOverlayVisible = false

    private lateinit var telephonyManager: TelephonyManager
    private var isCallActive = false

    private val timerUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val time = intent?.getLongExtra("time", 0L) ?: 0L
            updateTime(time)
        }
    }

    private val openAllowedAppSheetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == MainActivity.ACTION_OPEN_ALLOWED_APP_SHEET) {
                openAllowedAppSheet()
            }
        }
    }

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (!Settings.canDrawOverlays(requireContext())) {
            findNavController().popBackStack()
            Toast.makeText(requireContext(), "오버레이 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // api 31 이상 통화 상태 콜백
    private val telephonyCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        object : TelephonyCallback(), TelephonyCallback.CallStateListener {
            override fun onCallStateChanged(state: Int) {
                when (state) {
                    TelephonyManager.CALL_STATE_RINGING,
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        isCallActive = true
                        if (!BottomSheetState.getIsBottomSheetOpen()) {
                            removeOverlay()
                        }
                    }
                    TelephonyManager.CALL_STATE_IDLE -> {
                        if (isCallActive) {
                            isCallActive = false
                            Handler(Looper.getMainLooper()).postDelayed({
                                showOverlay()
                            }, 1000)
                        }
                    }
                }
            }
        }
    } else null

    // api 31 미만 통화 상태 리스너
    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            when (state) {
                TelephonyManager.CALL_STATE_RINGING,
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    isCallActive = true
                    if (!BottomSheetState.getIsBottomSheetOpen()) {
                        removeOverlay()
                    }
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    if (isCallActive) {
                        isCallActive = false
                        Handler(Looper.getMainLooper()).postDelayed({
                            showOverlay()
                        }, 1000)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        windowManager = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        telephonyManager = requireActivity().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager.registerTelephonyCallback(requireActivity().mainExecutor, telephonyCallback!!)
        } else {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        }

        initView()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallBack)
        observeViewModel()
        userViewModel.fetchTotalTime()
        userViewModel.fetchDailyTime()
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter("com.rocket.cosmic_detox.TIMER_UPDATE")
        requireContext().registerReceiver(timerUpdateReceiver, intentFilter, Context.RECEIVER_EXPORTED)
        val openAllowedAppSheetFilter = IntentFilter(MainActivity.ACTION_OPEN_ALLOWED_APP_SHEET)
        requireContext().registerReceiver(
            openAllowedAppSheetReceiver,
            openAllowedAppSheetFilter,
            Context.RECEIVER_EXPORTED
        )

        if (!permissionViewModel.isOverlayPermissionGranted(requireContext()) &&
            !Settings.canDrawOverlays(requireContext())) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:${requireContext().packageName}".toUri()
            )
            overlayPermissionLauncher.launch(intent)
        }
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(timerUpdateReceiver)
        requireContext().unregisterReceiver(openAllowedAppSheetReceiver)
    }

    override fun onPause() {
        super.onPause()
        if (isFinishingTimer) return
        if (!isOverlayVisible && !isCallActive) {
            if (!BottomSheetState.getIsBottomSheetOpen() &&
                permissionViewModel.isOverlayPermissionGranted(requireContext())) {
                showOverlay()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isOverlayVisible) {
            removeOverlay()
        }
    }

    override fun onDestroyView() {
        backPressedCallBack.remove()
        super.onDestroyView()
        _binding = null
        stopTimerService()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager.unregisterTelephonyCallback(telephonyCallback!!)
        } else {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        }
    }

    private fun startTimerService(dailyTime: Long) {
        if (isTimerServiceStarted) return
        val intent = Intent(requireContext(), TimerService::class.java)
        intent.putExtra("dailyTime", dailyTime)
        requireContext().startService(intent)
        isTimerServiceStarted = true
    }

    private fun stopTimerService() {
        if (!isTimerServiceStarted) return
        val intent = Intent(requireContext(), TimerService::class.java)
        requireContext().stopService(intent)
        isTimerServiceStarted = false
    }

    private fun showOverlay() {
        if (!isOverlayVisible && !Settings.canDrawOverlays(requireContext())) {
            stopTimerService()
            findNavController().popBackStack()
            Toast.makeText(requireContext(), "오버레이 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isOverlayVisible && Settings.canDrawOverlays(requireContext())) {
            val overlayParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )

            val view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_dialog, null)
            view.findViewById<Button>(R.id.btn_back).setOnClickListener {
                returnToTimer()
            }
            overlayView = view
            windowManager.addView(overlayView, overlayParams)
            isOverlayVisible = true
        }
    }

    private fun removeOverlay() {
        try {
            val view = overlayView ?: return
            windowManager.removeView(view)
            overlayView = null
            isOverlayVisible = false
        } catch (e: Exception) {
            Log.e("Overlay", "오버레이 제거 중 오류 발생: ${e.message}")
        }
    }

    private fun returnToTimer() {
        val intent = Intent(requireContext(), requireActivity()::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        startActivity(intent)
    }

    private fun initView() = with(binding) {
        btnTimerFinish.setOnClickListener {
            val dialog = TwoButtonDialogFragment(
                title = getString(R.string.timer_dialog_finish),
                onClickConfirm = {
                    isFinishingTimer = true
                    stopTimerService()
                    findNavController().popBackStack()
                },
                onClickCancel = { false }
            )
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, "ConfirmDialog")
        }

        btnTimerRest.setOnClickListener {
            openAllowedAppSheet()
        }
    }

    private fun openAllowedAppSheet() {
        if (BottomSheetState.getIsBottomSheetOpen()) return
        val bottomSheet = TimerAllowedAppBottomSheet()
        bottomSheet.show(parentFragmentManager, "BottomSheet")
        BottomSheetState.setIsBottomSheetOpen(true)
    }

    private val backPressedCallBack = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!isAdded || _binding == null) return
            showTwoButtonDialog()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            userViewModel.dailyTimeState.collect { state ->
                when (state) {
                    is UiState.Loading -> {}
                    is UiState.Success -> {
                        val dailyTime = normalizeDailyTime(state.data)
                        updateTime(dailyTime)
                        startTimerService(dailyTime)
                    }
                    is UiState.Failure -> showError(state.e?.message)
                    is UiState.Init -> {}
                    else -> {}
                }
            }
        }
    }

    private fun showError(message: String?) {
        val dialog = OneButtonDialogFragment(
            title = message ?: getString(R.string.dialog_common_error),
            onClickConfirm = {}
        )
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, "ErrorDialog")
    }

    private fun showTwoButtonDialog() {
        if (!isAdded || _binding == null) return
        val fragmentManager = parentFragmentManager
        if (fragmentManager.isStateSaved) return
        if (fragmentManager.findFragmentByTag("ConfirmDialog") != null) return

        val dialog = OneButtonDialogFragment(
            getString(R.string.dialog_common_focus)
        ) {}
        dialog.isCancelable = false
        dialog.show(fragmentManager, "ConfirmDialog")
    }

    private fun updateTime(time: Long) {
        val hours = time / 3600
        val minutes = (time % 3600) / 60
        val seconds = time % 60
        binding.tvTimerTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun normalizeDailyTime(fetchedDailyTime: Long): Long {
        val today = getTodayKey()
        val lastResetDate = SharedPreferencesUtil.getLastDailyResetDate(requireContext())

        return if (lastResetDate != today && fetchedDailyTime > 0L) {
            userViewModel.updateDailyTime(0L)
            SharedPreferencesUtil.setLastDailyResetDate(requireContext(), today)
            0L
        } else {
            SharedPreferencesUtil.setLastDailyResetDate(requireContext(), today)
            fetchedDailyTime
        }
    }

    private fun getTodayKey(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        return formatter.format(Date())
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
