package com.rocket.cosmic_detox.presentation.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rocket.cosmic_detox.databinding.ActivityDialogBinding

class DialogActivity : AppCompatActivity() {

    private val binding: ActivityDialogBinding by lazy {
        ActivityDialogBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}