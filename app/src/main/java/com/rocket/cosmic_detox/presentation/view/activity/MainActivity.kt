package com.rocket.cosmic_detox.presentation.view.activity

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ActivityMainBinding
import com.rocket.cosmic_detox.presentation.component.dialog.ProgressDialogFragment
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogDescFragment
import com.rocket.cosmic_detox.presentation.uistate.UiState
import com.rocket.cosmic_detox.presentation.view.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val userViewModel: UserViewModel by viewModels()

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

    //권한 체크 함수
    private fun checkPermissions() {
        val isUsageStateAllowed = isUsageStatsPermissionGranted(this)
        val isRequestOverlay = isOverlayPermissionGranted(this)
        Log.d("권한 뭔 일이다냐?", "isUsageStateAllowed>> $isUsageStateAllowed, isRequestOverlay>> $isRequestOverlay")


        //거절된 퍼미션이 있다면...
        if (!isUsageStateAllowed || !isRequestOverlay) {
            //권한 요청!
            val dialog = TwoButtonDialogDescFragment(
                title = getString(R.string.dialog_permission_title),
                description = getString(R.string.dialog_permission_desc),
                onClickConfirm = {
                    if(isUsageStateAllowed) {
                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        startActivity(intent)
                    } else {
                        if (!Settings.canDrawOverlays(this)) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${this.packageName}")
                            )
                            startActivity(intent)
                        }

                    }
                },
                onClickCancel = { }
            )
            dialog.isCancelable = false
            dialog.show(supportFragmentManager, "ConfirmDialog")
        }
    }

    private fun isUsageStatsPermissionGranted(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun isOverlayPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
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