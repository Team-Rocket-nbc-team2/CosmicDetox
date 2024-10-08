package com.rocket.cosmic_detox.presentation.view.fragment.tutorial.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.rocket.cosmic_detox.presentation.view.fragment.tutorial.TutorialFragment

class TutorialPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return TutorialFragment.newInstance(position)
    }
}