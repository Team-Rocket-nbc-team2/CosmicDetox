package com.rocket.cosmic_detox.presentation.view.activity

import android.Manifest
import android.Manifest.permission.PACKAGE_USAGE_STATS
import android.Manifest.permission.SYSTEM_ALERT_WINDOW
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ActivityMainBinding
import com.rocket.cosmic_detox.presentation.component.dialog.ProgressDialogFragment
import com.rocket.cosmic_detox.presentation.uistate.UiState
import com.rocket.cosmic_detox.presentation.view.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val userViewModel: UserViewModel by viewModels()

    private val multiplePermissionsCode = 100 //퍼미션 응답 처리 코드
    private val requiredPermissions = arrayOf(PACKAGE_USAGE_STATS, SYSTEM_ALERT_WINDOW)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        setBottomNavigation()

        userViewModel.fetchUserData()

        lifecycleScope.launch {
            val progressDialog = ProgressDialogFragment()
            userViewModel.userState.collectLatest { uiState ->
                when (uiState) {
                    is UiState.Loading -> {
                        progressDialog.setCancelable(false)
                        progressDialog.show(supportFragmentManager, "ConfirmDialog")
                    }
                    is UiState.Success -> {
                        progressDialog.dismiss()
                    }
                    else -> {
                        Log.e("HomeFragment get UserData", "유저 정보 불러오기를 실패했습니다.")
                    }
                }
            }
        }

        checkPermissions()
    }

    private fun setBottomNavigation() = with(binding) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.container_main) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNavigationMain.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home, R.id.navigation_race, R.id.navigation_my, R.id.navigation_modify_allow_app_dialog, R.id.navigation_set_limit_app_dialog -> {
                    bottomNavigationMain.visibility = View.VISIBLE
                }
                else -> {
                    bottomNavigationMain.visibility = View.GONE
                }
            }
        }
    }

    //퍼미션 체크 및 권한 요청 함수
    private fun checkPermissions() {
        //거절되었거나 아직 수락하지 않은 권한(퍼미션)을 저장할 문자열 배열 리스트
        var rejectedPermissionList = ArrayList<String>()

        //필요한 퍼미션들을 하나씩 끄집어내서 현재 권한을 받았는지 체크
        for(permission in requiredPermissions){
            Log.d("권한체크!", "워킹하는거 맞냐?1")
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                //만약 권한이 없다면 rejectedPermissionList에 추가
                Log.d("권한체크!", "워킹하는거 맞냐?2")
                rejectedPermissionList.add(permission)
            }
        }
        //거절된 퍼미션이 있다면...
        if(rejectedPermissionList.isNotEmpty()){
            Log.d("권한체크!", "워킹하는거 맞냐?3")
            //권한 요청!
            val array = arrayOfNulls<String>(rejectedPermissionList.size)
            ActivityCompat.requestPermissions(this, rejectedPermissionList.toArray(array), multiplePermissionsCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            multiplePermissionsCode -> {
                if(grantResults.isNotEmpty()) {
                    for((i, permission) in permissions.withIndex()) {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            //권한 획득 실패
                            Log.i("TAG", "The user has denied to $permission")
                            // 사용량 통계 권한 확인 및 요청
                            if (!Settings.canDrawOverlays(this)) {
                                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                startActivity(intent)
                            }

                            // 오버레이 권한 확인 및 요청
                            if (!Settings.canDrawOverlays(this)) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:$packageName")
                                )
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
        }
    }

//    override fun onResume() {
//        super.onResume()
//        Log.d("Main", "hello lock mode")
//        checkLockTaskMode() // 앱 고정 모드 상태 체크
//    }
//
//    private fun checkLockTaskMode() {
//        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
//        val navHostFragment = supportFragmentManager.findFragmentById(R.id.container_main) as? NavHostFragment
//        val currentFragment = navHostFragment?.childFragmentManager?.primaryNavigationFragment
//
//        if (activityManager.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE && currentFragment is TimerFragment) {
//            currentFragment.navigateToHomeFragment()
//        } else {
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        // 잠금 모드가 아닐 때만 리셋
//        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
//        if (activityManager.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
//            val navHostFragment =
//                supportFragmentManager.findFragmentById(R.id.container_main) as? NavHostFragment
//            navHostFragment?.navController?.navigate(R.id.navigation_home)
//        }
//    }
//
//    override fun onBackPressed() {
//        val navHostFragment =
//            supportFragmentManager.findFragmentById(R.id.container_main) as? NavHostFragment
//        val currentFragment = navHostFragment?.childFragmentManager?.primaryNavigationFragment
//
//        if (currentFragment is TimerFragment) {
//            // 타이머 화면이 열려 있을 때는 종료하지 않도록 ..
//            currentFragment.showTwoButtonDialog()
//        } else {
//            super.onBackPressed() // 다른 화면일 때는 기본 , 없어도 됨 다른 코드로 대체 할 수 있는 것 찾기
//        }
//    }
}