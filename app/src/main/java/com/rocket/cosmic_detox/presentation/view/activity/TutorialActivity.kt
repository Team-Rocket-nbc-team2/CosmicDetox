package com.rocket.cosmic_detox.presentation.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ActivityTutorialBinding
import com.rocket.cosmic_detox.presentation.view.fragment.tutorial.adapter.TutorialPagerAdapter
import com.rocket.cosmic_detox.util.SharedPreferencesUtil.setFirstTimeUserCompleted
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TutorialActivity : AppCompatActivity() {

    private val binding by lazy { ActivityTutorialBinding.inflate(layoutInflater) }
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: TutorialPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewPager = findViewById(R.id.viewPager)
        adapter = TutorialPagerAdapter(this)
        viewPager.adapter = adapter
    }

    fun moveToNextPage(position: Int) {
        viewPager.currentItem = position + 1
    }

    fun finishTutorial() {
        setFirstTimeUserCompleted(applicationContext)  // SharedPreferences에 튜토리얼 완료 상태 저장
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}