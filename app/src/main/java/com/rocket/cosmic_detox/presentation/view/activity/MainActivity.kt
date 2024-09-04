package com.rocket.cosmic_detox.presentation.view.activity

import android.app.ActivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ActivityMainBinding
import com.rocket.cosmic_detox.presentation.view.fragment.timer.TimerFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }


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


//        if (!hasUsageStatsPermission(this)) {
//            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
//            startActivity(intent)
//        }
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