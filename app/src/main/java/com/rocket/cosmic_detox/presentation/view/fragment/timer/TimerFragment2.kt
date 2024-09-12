package com.rocket.cosmic_detox.presentation.view.fragment.timer

import android.content.Context
import android.content.Intent
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
import com.rocket.cosmic_detox.databinding.FragmentTimer2Binding
import com.rocket.cosmic_detox.presentation.component.bottomsheet.TimerAllowedAppBottomSheet
import com.rocket.cosmic_detox.presentation.component.dialog.OneButtonDialogFragment
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogFragment
import com.rocket.cosmic_detox.presentation.uistate.GetListUiState
import com.rocket.cosmic_detox.presentation.uistate.UiState
import com.rocket.cosmic_detox.presentation.service.AppMonitorService
import com.rocket.cosmic_detox.presentation.view.viewmodel.UserViewModel
import com.rocket.cosmic_detox.presentation.viewmodel.AllowedAppViewModel
import com.rocket.cosmic_detox.presentation.viewmodel.PermissionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TimerFragment2 : Fragment() {

    // FragmentTimer2Binding으로 임의 명칭 변경
    private var _binding: FragmentTimer2Binding? = null
    private val binding get() = _binding!!
    private var isFinishingTimer = false
    private var time = 0 // 시간
    private val handler = Handler(Looper.getMainLooper()) // 메인 스레드에서 실행할 핸들러
    private var isTimerRunning = false // 타이머가 실행 중인지 확인하는 변수

    private val userViewModel: UserViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()
    private val allowedAppViewModel: AllowedAppViewModel by viewModels<AllowedAppViewModel>() // 허용 앱 리스트 가져오기 위한 뷰모델

    private val runnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 1000) // 1초마다 다시 실행
        }
    }

    private lateinit var windowManager: WindowManager // 오버레이를 위한 WindowManager
    private var overlayView: View? = null // 오버레이 뷰
    private var isOverlayVisible = false // 오버레이가 보이는지 여부
    private var allowedAppList = mutableListOf<String>()

    private lateinit var telephonyManager: TelephonyManager
    private var isCallActive = false // 전화가 활성화된 상태인지 확인하는 변수

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTimer2Binding.inflate(inflater, container, false)
        windowManager = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // TelephonyManager 초기화
        telephonyManager = requireActivity().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isRequestOverlay = permissionViewModel.isOverlayPermissionGranted(requireContext())

        if(isRequestOverlay){
            //showOverlay()
        } else {
            if (!Settings.canDrawOverlays(requireContext())) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${requireContext().packageName}")
                )
                overlayPermissionLauncher.launch(intent)
            }
        }

        // 버전에 따른 전화 상태 감지 콜백과 리스너 분류 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager?.registerTelephonyCallback(requireActivity().mainExecutor, telephonyCallback)
        } else {
            telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        }

        initView()
        allowedAppViewModel.getAllAllowedApps() // 허용 앱 리스트 가져오기 -> 서비스에 전달하려고 가져온거긴 한데 지금 당장은 필요없을 듯
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), backPressedCallBack)

        observeViewModel()

        userViewModel.fetchTotalTime()
        userViewModel.fetchDailyTime() // dailyTime도 함께 초기화
        //startTimer()
    }

    // sdk 31 이상 통화 상태 콜백
    // TODO api가 31 아래인 경우는 어떻게 해야? stateListner로? if문으로 SDK_VERSION 감지해서 콜백과 리스너로 분류
    private val telephonyCallback = @RequiresApi(Build.VERSION_CODES.S)
    object : TelephonyCallback(), TelephonyCallback.CallStateListener {
        override fun onCallStateChanged(state: Int) {
            when (state) {
                TelephonyManager.CALL_STATE_RINGING,  // 전화가 걸려올 때
                TelephonyManager.CALL_STATE_OFFHOOK -> {  // 통화 중일 때
                    isCallActive = true
                    removeOverlay() // 오버레이 제거
                }
                TelephonyManager.CALL_STATE_IDLE -> {  // 전화가 종료될 때
                    // TODO 통화가 종료됐을 때 다시 오버레이뷰 띄우기 아래 주석으로는 잘 되지 않음
                    isCallActive = false
//                    showOverlay()  // 통화가 종료되면 다시 오버레이 띄우기
                }
            }
        }
    }

    // sdk 31 미만 통화 상태 리스너 정의
    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            when(state) {
                TelephonyManager.CALL_STATE_RINGING,
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    isCallActive = true
                    removeOverlay()
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    isCallActive = false
                }
            }
        }
    }

    private fun startAppMonitorService() { // 허용 앱 리스트 가져오면 서비스 시작 -> 이해도 안되지만 타이머에서는 필요없는 것 같음. 근데 나중에 허용 앱 실행 중에 뒤로가기나 홈으로 이동하면 이걸 감지하려면 필요할라나
        Log.d("AllowedAppList", "startAppMonitorService!!!!!!!!!1 : $allowedAppList")
        val intent = Intent(requireContext(), AppMonitorService::class.java).apply {
            putStringArrayListExtra("ALLOWED_APP_LIST", allowedAppList as ArrayList<String>)
            putExtra("asdf", "asdf")
        }
        requireContext().startService(intent)
    }

    private fun stopAppMonitorService() { // 타이머 종료 시 앱 모니터링 서비스 종료
        val intent = Intent(requireContext(), AppMonitorService::class.java)
        requireContext().stopService(intent)
    }


    override fun onPause() {
        super.onPause()
        if (isFinishingTimer) return // 타이머 종료 중인 경우에는 오버레이 띄우지 않음
        if (!isOverlayVisible) { // 오버레이가 보이지 않는 상태일 때만 오버레이 권한 요청, 일단 GPT가 하라는 대로 추가한 것
            if(!BottomSheetState.getIsBottomSheetOpen() && permissionViewModel.isOverlayPermissionGranted(requireContext())){
                Log.d("Overlay디버그", "onPause 실행")
                Log.d("오버레이뷰",
                    "isFinishingTimer>> $isFinishingTimer, isOverlayVisible>> $isOverlayVisible, BottomSheetState>> ${BottomSheetState.getIsBottomSheetOpen()}, permissionViewModel>> ${permissionViewModel.isOverlayPermissionGranted(requireContext())}")
                showOverlay()
            }
        }
    }

//    override fun onResume() { // showOverlay() 에서 버튼 클릭 시 이미 removeOverlay()를 호출하고 있어서 onResume은 없애도 될 것 같음
//        super.onResume()
//        if (isOverlayVisible) { // 오버레이가 보이는 상태일 때 ==  다시 타이머 화면으로 돌아왔을 때
//            //removeOverlay() // 오버레이 제거
//        }
//    }

    private val overlayPermissionLauncher = registerForActivityResult( // 오버레이 권한 요청
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (Settings.canDrawOverlays(requireContext())) {
            // 오버레이 권한이 허용된 경우 수행할 작업
            Log.d("Overlay디버그", "overlayPermissionLauncher 실행")
            showOverlay()
        } else {
            // 권한이 거부된 경우 처리
            findNavController().popBackStack()
            Toast.makeText(requireContext(), "오버레이 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showOverlay() { // 오버레이 띄우기
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
                    removeOverlay() // 오버레이 제거
                }

                Log.d("Overlay디버그", "showOverlay 실행")

                windowManager.addView(it, overlayParams) // 오버레이 뷰 추가
                isOverlayVisible = true // 오버레이가 보이는 상태로 변경
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
        overlayView?.let { // 오버레이 제거
            windowManager.removeView(it)
            isOverlayVisible = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        stopTimer()
        //removeOverlay() // Fragment 종료 시 오버레이 제거
        stopAppMonitorService() // 타이머 종료 시 앱 모니터링 서비스 종료
        // 콜백 해제
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager?.unregisterTelephonyCallback(telephonyCallback)
        } else {
            telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        }
    }

    private fun initView() = with(binding) {
        btnTimerFinish2.setOnClickListener {
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
            stopAppMonitorService() // 타이머 종료 시 앱 모니터링 서비스 종료 -> TODO: 이거 없애도 될 것 같은데.. 일단 올림. 차라리 해도 바텀시트에서 허용앱 갔을 때 서비스 시작하는게 맞나..?
        }

        btnTimerRest2.setOnClickListener {
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
                        time = state.data.toInt()
                        updateTime()
                        startTimer()
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
                    startAppMonitorService() // 허용 앱 리스트 가져오면 서비스 시작
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
        if (!isTimerRunning) {  // 타이머가 실행 중이 아닌 경우에만 시작
            handler.post(runnable)
            isTimerRunning = true  // 타이머 실행 상태를 true로 설정
        }
    }

    private fun stopTimer() {
        handler.removeCallbacks(runnable)
        isTimerRunning = false  // 타이머 실행 상태를 false로 설정

        // 타이머가 중지될 때 dailyTime과 totalTime을 Firestore에 저장
        userViewModel.updateDailyTime(time.toLong()) // dailyTime 업데이트
        userViewModel.updateTotalTime(time.toLong()) // totalTime 업데이트

        // 타이머가 중지될 때 Firestore의 ranking에 totalTime 업데이트
        userViewModel.updateRankingTotalTime(time.toLong())
    }

    private fun updateTime() {
        time++
        val hours = time / 3600
        val minutes = (time % 3600) / 60
        val seconds = time % 60
        binding.tvTimerTime2.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
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