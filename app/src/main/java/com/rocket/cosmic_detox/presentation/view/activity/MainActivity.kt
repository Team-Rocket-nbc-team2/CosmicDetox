package com.rocket.cosmic_detox.presentation.view.activity

import android.app.ActivityManager
import android.content.Intent
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

    private var lastBackPressedTime: Long = 0

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
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.navigation_home, R.id.navigation_race, R.id.navigation_my -> {
                    bottomNavigationMain.visibility = View.VISIBLE
                }
                else -> {
                    bottomNavigationMain.visibility = View.GONE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("Main","hello")
        checkLockTaskMode() // 앱 고정 모드 상태 체크
    }

    private fun checkLockTaskMode() {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.container_main) as? NavHostFragment
        val currentFragment = navHostFragment?.childFragmentManager?.primaryNavigationFragment

        if (activityManager.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE && currentFragment is TimerFragment) {
            currentFragment.navigateToHomeFragment()
        } else {
        }
    }

    override fun onPause() {
        super.onPause()
        // 잠금 모드가 아닐 때만 리셋
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        if (activityManager.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.container_main) as? NavHostFragment
            navHostFragment?.navController?.navigate(R.id.navigation_home)
        }
    }
}
