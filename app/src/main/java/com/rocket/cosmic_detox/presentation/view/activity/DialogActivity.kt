package com.rocket.cosmic_detox.presentation.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ActivityDialogBinding

class DialogActivity : AppCompatActivity() {

    private val binding: ActivityDialogBinding by lazy {
        ActivityDialogBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
//        binding.btnBack.setOnClickListener {
//            val allowedAppPackageName = intent.getStringExtra("allowedAppPackageName")
//
//            if (!allowedAppPackageName.isNullOrEmpty()) {
//                val launchIntent = packageManager.getLaunchIntentForPackage(allowedAppPackageName)
//                if (launchIntent != null) {
//                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
//                    startActivity(launchIntent)
//                } else {
//                    finish() // 허용된 앱이 없으면 DialogActivity 종료
//                }
//            } else {
//                finish() // 허용된 앱 정보가 없으면 종료
//            }
//        }
    }
}