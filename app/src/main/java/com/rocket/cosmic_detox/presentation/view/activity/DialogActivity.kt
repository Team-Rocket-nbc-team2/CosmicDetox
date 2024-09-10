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
    }
}