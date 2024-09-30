package com.rocket.cosmic_detox.presentation.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.rocket.cosmic_detox.presentation.viewmodel.UserViewModel
import com.rocket.cosmic_detox.presentation.viewmodel.PermissionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val userViewModel: UserViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()

    private lateinit var requestOverlayPermissionLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestUsageAccessLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestNotificationPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var requestPhoneStatePermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var requestExactAlarmPermissionLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        initPermissionLauncher()
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

    private fun initPermissionLauncher() {
        requestOverlayPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            checkPermissions()
        }

        requestUsageAccessLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (permissionViewModel.isUsageStatsPermissionGranted(this)) {
                requestOverlayPermission()
            }
        }

        requestNotificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            requestPhoneStatePermission()
        }

        requestPhoneStatePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            Log.d("TelephonyManager", "READ_PHONE_STATE 권한을 요청.")
        }
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
        val isUsageStateAllowed = permissionViewModel.isUsageStatsPermissionGranted(this)
        val isRequestOverlay = permissionViewModel.isOverlayPermissionGranted(this)
        val isPostNotificationGrantedAllowed = permissionViewModel.isPostNotificationGranted(this)
        val isReadPhoneStatePermissionAllowed = permissionViewModel.isReadPhoneStatePermissionGranted(this)
        val isExactAlarmAllowed = permissionViewModel.isExactAlarmPermissionGranted(this)

        Log.d("권한 뭔 일이다냐?", "isUsageStateAllowed>> $isUsageStateAllowed, isRequestOverlay>> $isRequestOverlay, isReadPhoneStatePermissionAllowed>> $isReadPhoneStatePermissionAllowed")

        if (!isUsageStateAllowed || !isRequestOverlay || !isPostNotificationGrantedAllowed || !isReadPhoneStatePermissionAllowed || !isExactAlarmAllowed) {
            val dialog = TwoButtonDialogDescFragment(
                title = getString(R.string.dialog_permission_title),
                description = getString(R.string.dialog_permission_desc),
                onClickConfirm = {
                    if (!isUsageStateAllowed) {
                        requestUsageAccessPermission()
                    } else if (!isRequestOverlay) {
                        requestOverlayPermission()
                    } else if (!isPostNotificationGrantedAllowed) {
                        requestNotificationPermission()
                    } else if (!isReadPhoneStatePermissionAllowed) {
                        requestPhoneStatePermission()
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Log.d("ExactAlarm", "권한 상태 --> $isExactAlarmAllowed")

                        // 권한이 자동으로 부여되면 별도의 처리 불필요
                        if (!isExactAlarmAllowed) {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            startActivity(intent)
                        } else {
                            Log.d("ExactAlarm", "정확한 알람 권한이 이미 자동으로 부여되었습니다.")
                        }
                    }
                },
                onClickCancel = { }
            )
            dialog.isCancelable = false
            dialog.show(supportFragmentManager, "ConfirmDialog")
        }
    }

    private fun requestUsageAccessPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        requestUsageAccessLauncher.launch(intent)
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${this.packageName}"))
            requestOverlayPermissionLauncher.launch(intent)
        }
    }

    private fun requestNotificationPermission() {
        requestNotificationPermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
    }

    private fun requestPhoneStatePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPhoneStatePermissionLauncher.launch(arrayOf(Manifest.permission.READ_PHONE_STATE))
        } else {
            Log.d("TelephonyManager", "READ_PHONE_STATE 권한이 이미 허용되어 있습니다.")
        }
    }
}