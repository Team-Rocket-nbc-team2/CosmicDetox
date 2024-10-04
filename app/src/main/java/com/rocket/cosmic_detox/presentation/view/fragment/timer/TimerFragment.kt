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
import androidx.annotation.RequiresApi
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
import com.rocket.cosmic_detox.presentation.service.TimerService
import com.rocket.cosmic_detox.presentation.viewmodel.UserViewModel
import com.rocket.cosmic_detox.presentation.viewmodel.AllowedAppViewModel
import com.rocket.cosmic_detox.presentation.viewmodel.PermissionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!
    private var isFinishingTimer = false


    private val userViewModel: UserViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()
    private val allowedAppViewModel: AllowedAppViewModel by viewModels<AllowedAppViewModel>() // 허용 앱 리스트 가져오기 위한 뷰모델

    private lateinit var windowManager: WindowManager // 오버레이를 위한 WindowManager
    private var overlayView: View? = null // 오버레이 뷰

    private val timerUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val time = intent?.getLongExtra("time", 0L) ?: 0L
            updateTime(time)
        }
    }
    private var isOverlayVisible = false // 오버레이가 보이는지 여부
    private var allowedAppList = mutableListOf<String>()

    private lateinit var telephonyManager: TelephonyManager
    private var isCallActive = false // 전화가 활성화된 상태인지 확인하는 변수

    private val overlayPermissionLauncher = registerForActivityResult( // 오버레이 권한 요청
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (Settings.canDrawOverlays(requireContext())) {
            // 오버레이 권한이 허용된 경우 수행할 작업
            Log.d("Overlay디버그", "overlayPermissionLauncher 실행")
            //showOverlay()
        } else {
            // 권한이 거부된 경우 처리
            findNavController().popBackStack()
            Toast.makeText(requireContext(), "오버레이 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        windowManager = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        // TelephonyManager 초기화
        telephonyManager = requireActivity().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 버전에 따른 전화 상태 감지 콜백과 리스너 분류 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager.registerTelephonyCallback(requireActivity().mainExecutor, telephonyCallback!!)
        } else {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        }

        initView()
        allowedAppViewModel.getAllAllowedApps() // 허용 앱 리스트 가져오기 -> 서비스에 전달하려고 가져온거긴 한데 지금 당장은 필요없을 듯
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), backPressedCallBack)

        observeViewModel()

        userViewModel.fetchTotalTime()
        userViewModel.fetchDailyTime() // dailyTime도 함께 초기화
    }

    // api 31 이상 통화 상태 콜백
    private val telephonyCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        object : TelephonyCallback(), TelephonyCallback.CallStateListener {
            override fun onCallStateChanged(state: Int) {
                when (state) {
                    TelephonyManager.CALL_STATE_RINGING,  // 전화가 걸려올 때
                    TelephonyManager.CALL_STATE_OFFHOOK -> {  // 통화 중일 때
                        isCallActive = true
                        if (!BottomSheetState.getIsBottomSheetOpen()) {
                            removeOverlay()
                        }
                    }
                    TelephonyManager.CALL_STATE_IDLE -> {  // 전화가 종료될 때
                        if (isCallActive) { // 이전에 통화 중이었던 경우만 실행
                            isCallActive = false
                            // 통화 종료 후 바로 오버레이 띄우면 앱 꺼지는 현상 발생해서 핸들러로 오버레이를 조금 지연시켜 띄우기
                            Handler(Looper.getMainLooper()).postDelayed({
                                showOverlay() // 허용된 앱이 아닐 경우 다시 오버레이 띄우기
                            }, 1000)
                        }
                    }
                }
            }
        }
    } else {
        null
    }

    // api 31 미만 통화 상태 리스너 정의
    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            when(state) {
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
                            showOverlay() // 허용된 앱이 아닐 경우 다시 오버레이 띄우기
                        }, 1000)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter("com.rocket.cosmic_detox.TIMER_UPDATE")
        requireContext().registerReceiver(timerUpdateReceiver, intentFilter, Context.RECEIVER_EXPORTED)
        val isRequestOverlay = permissionViewModel.isOverlayPermissionGranted(requireContext())

        if (!isRequestOverlay && !Settings.canDrawOverlays(requireContext())) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${requireContext().packageName}")
            )
            overlayPermissionLauncher.launch(intent)
        }
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(timerUpdateReceiver)
    }

    private fun startTimerService(dailyTime: Long) {
        val intent = Intent(requireContext(), TimerService::class.java)
        intent.putExtra("dailyTime", dailyTime)
        requireContext().startService(intent)
    }

    private fun stopTimerService() {
        val intent = Intent(requireContext(), TimerService::class.java)
        requireContext().stopService(intent)
    }


    override fun onPause() {
        super.onPause()
        if (isFinishingTimer) return // 타이머 종료 중인 경우에는 오버레이 띄우지 않음
        if (!isOverlayVisible && !isCallActive) { // 오버레이가 보이지 않는 상태일 때, 통화 상태가 아닐 때만 오버레이 권한 요청, 일단 GPT가 하라는 대로 추가한 것
            if(!BottomSheetState.getIsBottomSheetOpen() && permissionViewModel.isOverlayPermissionGranted(requireContext())){
                Log.d("Overlay디버그", "onPause 실행")
                Log.d("오버레이뷰",
                    "isFinishingTimer>> $isFinishingTimer, isOverlayVisible>> $isOverlayVisible, BottomSheetState>> ${BottomSheetState.getIsBottomSheetOpen()}, permissionViewModel>> ${permissionViewModel.isOverlayPermissionGranted(requireContext())}")
                showOverlay()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isOverlayVisible) { // 오버레이가 보이는 상태일 때 ==  다시 타이머 화면으로 돌아왔을 때
            removeOverlay() // 오버레이 제거
        }
    }

    private fun showOverlay() { // 오버레이 띄우기
        // 권한이 없을 경우
        if (!isOverlayVisible && !Settings.canDrawOverlays(requireContext())) {
            stopTimerService()  // 없으면 타이머 종료 후 시간 저장
            findNavController().popBackStack()  // 홈으로 이동
            Toast.makeText(requireContext(), "오버레이 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }

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
                it.findViewById<Button>(R.id.btn_back).setOnClickListener { // "이전화면으로 돌아가기" 버튼 클릭 시
                    returnToTimer() // 타이머로 돌아가기
                    //removeOverlay() // 오버레이 제거
                }

                Log.d("Overlay디버그", "showOverlay 실행")

                windowManager.addView(it, overlayParams) // 오버레이 뷰 추가
                isOverlayVisible = true // 오버레이가 보이는 상태로 변경
            }
//            overlayView?.let {
//                it.findViewById<Button>(R.id.btn_back).setOnClickListener { // "이전화면으로 돌아가기" 버튼 클릭 시
//                    // 현재 포그라운드 앱 패키지명 가져오기
//                    val currentAppPackageName = getCurrentForegroundApp()
//                    if (allowedAppList.contains(currentAppPackageName)) {
//                        // 허용된 앱이면 해당 앱으로 이동
//                        val launchIntent = requireContext().packageManager.getLaunchIntentForPackage(currentAppPackageName)
//                        if (launchIntent != null) {
//                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
//                            requireContext().startActivity(launchIntent)
//                        }
//                    } else {
//                        returnToTimer() // 타이머로 돌아가기
//                    }
//                    removeOverlay() // 오버레이 제거
//                }
//
//                Log.d("Overlay디버그", "showOverlay 실행")
//
//                windowManager.addView(it, overlayParams) // 오버레이 뷰 추가
//                isOverlayVisible = true // 오버레이가 보이는 상태로 변경
//            }
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
        try {   // try, catch로 통화 시 앱 꺼지는 현상 로그 확인
            overlayView?.let { // 오버레이 제거
                if (it.isAttachedToWindow) {
                    windowManager.removeView(it)
                    isOverlayVisible = false
                }
            }
        } catch (e: Exception) {
            Log.e("Overlay", "오버레이 제거 중 오류 발생: ${e.message}")
        }
    }

    //    private fun isInAllowedApp(): Boolean {
//        val currentApp = getCurrentForegroundApp()
//        return allowedAppList.contains(currentApp)
//    }
//
//    // 사용자의 현재 실행 중인 앱을 가져오는 메소드
//    private fun getCurrentForegroundApp(): String {
//        val activityManager = requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        val tasks = activityManager.getRunningTasks(1)
//        return tasks[0].topActivity?.packageName ?: ""
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        stopTimerService() // 서비스 중지
        //removeOverlay() // Fragment 종료 시 오버레이 제거
        // 콜백 해제
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager.unregisterTelephonyCallback(telephonyCallback!!)
        } else {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        }
    }

    private fun initView() = with(binding) {
        btnTimerFinish.setOnClickListener {
            val dialog = TwoButtonDialogFragment(
                title = getString(R.string.timer_dialog_finish),
                onClickConfirm = {
                    isFinishingTimer = true
                    stopTimerService() // 타이머 종료 시 서비스 중지
                    findNavController().popBackStack()
                },
                onClickCancel = { false }
            )
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, "ConfirmDialog")
        }

        btnTimerRest.setOnClickListener {
            val bottomSheet = TimerAllowedAppBottomSheet()
            bottomSheet.show(parentFragmentManager, "BottomSheet")
            BottomSheetState.setIsBottomSheetOpen(true) // 바텀시트 열림 -> isBottomSheetOpen을 true로 변경
        }
    }

    private val backPressedCallBack = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showTwoButtonDialog()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            userViewModel.dailyTimeState.collect { state -> // 기존 totalTimeState 대신 dailyTimeState 사용
                when (state) {
                    is UiState.Loading -> {
                        // 추후 작업필요할 떄 활용 로딩 상태 처리 (예: ProgressBar 표시)
                    }
                    is UiState.Success -> {
                        // Firebase에서 dailyTime을 받아와 UI에 반영
                        val dailyTime = state.data
                        updateTime(dailyTime)
                        // dailyTime을 인텐트로 TimerService에 전달
                        startTimerService(dailyTime)
                    }
                    is UiState.Failure -> {
                        showError(state.e?.message)
                    }
                    is UiState.Init -> {
                        // 초기 상태 처리코드 추후 작성
                    } else -> {}
                }
            }
        }

        lifecycleScope.launch { // 허용 앱 리스트 가져오기 -> AppMonitorService에 리스트 전달하려고 뷰모델 가져와서 구현하긴 했는데.. Service를 안 써서.. 굳이?
            allowedAppViewModel.allowedAppList.collectLatest {
                if (it is GetListUiState.Success) {
                    allowedAppList.clear()
                    it.data.forEach { allowedApp ->
                        allowedAppList.add(allowedApp.packageId)
                    }
                    Log.d("AllowedAppList", "앱 리스트 가져오기 성공!!!!!!!!!1 : $allowedAppList")
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

    private fun updateTime(time: Long) {
        val hours = time / 3600
        val minutes = (time % 3600) / 60
        val seconds = time % 60
        binding.tvTimerTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}

object BottomSheetState { // 바텀시트 상태 저장 -> 이걸 해야 바텀시트에서 허용 앱 이동했을 때 오버레이뷰가 뜨는 것을 방지할 수 있음.
    // 결국 TimerFragment에서 바텀시트로 이동해도 TimerFragment 위에 바텀시트를 덮어씌우는 거나 마찬가지라 TimerFragment도 살아있는 상태라 바텀시트랑 중복이 됨.
    // 바텀시트에서 허용 앱 이동을 해도 TimerFragment도 이를 감지하기 때문에 중복 발생한다는 의미. 따로 동작하도록 바텀시트 상태 저장
    private var isBottomSheetOpen = false

    fun setIsBottomSheetOpen(value: Boolean) {
        Log.d("BottomSheetState", "isBottomSheetOpen: $value")
        isBottomSheetOpen = value
    }

    fun getIsBottomSheetOpen(): Boolean {
        return isBottomSheetOpen
    }
}


// 아래 코드 혹시 몰라서 냅둔 코드! 나중에 정상 작동하는 거 확인 되면 삭제 가능
//private fun requestOverlayPermission() { // 오버레이 권한 요청
//    if (!Settings.canDrawOverlays(requireContext())) {
//        val intent = Intent(
//            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//            Uri.parse("package:${requireContext().packageName}")
//        )
//        overlayPermissionLauncher.launch(intent)
//    } else {
//        if (!BottomSheetState.getIsBottomSheetOpen()) { // 바텀시트가 열려있지 않은 경우에만 오버레이 띄우기 -> 바텀시트가 열려있을 때는 오버레이 띄우지 않음
//            // 이걸 안 해주면 바텀시트에서 허용 앱으로 이동할 때 TimerFragment도 살아있어서 이거 같이 호출됨. 중복 호출되는 것을 방지하기 위함.
//            showOverlay() // 오버레이 띄우기
//        }
//    }
//}