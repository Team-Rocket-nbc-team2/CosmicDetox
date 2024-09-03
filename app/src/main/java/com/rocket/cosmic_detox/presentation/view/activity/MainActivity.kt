package com.rocket.cosmic_detox.presentation.view.activity

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess

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

        onBackPressedDispatcher.addCallback(this, backPressedCallBack)

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
                R.id.navigation_home, R.id.navigation_race, R.id.navigation_my, R.id.navigation_modify_allow_app_dialog, R.id.navigation_set_limit_app_dialog -> {
                    bottomNavigationMain.visibility = View.VISIBLE
                }
                else -> {
                    bottomNavigationMain.visibility = View.GONE
                }
            }
        }
    }

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }


    private val backPressedCallBack = object : OnBackPressedCallback(true) {
        @SuppressLint("NotifyDataSetChanged")
        override fun handleOnBackPressed() {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressedTime >= 2000) {
                Toast.makeText(this@MainActivity, getString(R.string.terminate_app_alert), Toast.LENGTH_SHORT).show()
                lastBackPressedTime = currentTime
            } else {
                finishAffinity()
                exitProcess(0)
            }
        }
    }
}