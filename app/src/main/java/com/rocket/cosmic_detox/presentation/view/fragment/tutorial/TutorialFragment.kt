package com.rocket.cosmic_detox.presentation.view.fragment.tutorial

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.core.view.setPadding
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.presentation.view.activity.TutorialActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TutorialFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tutorial, container, false)

        val position = arguments?.getInt("position") ?: 0
        val imageView = view.findViewById<ImageView>(R.id.iv_tutorial_icon)
        val textView1 = view.findViewById<TextView>(R.id.tv_tutorial_addiction)
        val textView2 = view.findViewById<TextView>(R.id.tv_tutorial_cosmic)
        val button = view.findViewById<Button>(R.id.btn_tutorial)

        when (position) {
            0 -> {
                imageView.setImageResource(R.drawable.tutorial_icon)
                textView1.text = getString(R.string.tutorial_addiction)
                textView2.text = getString(R.string.tutorial_cosmic)
            }
            1 -> {
                imageView.setImageResource(R.drawable.tutorial_allowed_app)
                textView2.text = getString(R.string.tutorial_app)
                textView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                textView2.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                textView2.setTypeface(null, Typeface.BOLD)
            }
            2 -> {
                imageView.setImageResource(R.drawable.tutorial_overlay)
                textView2.text = getString(R.string.tutorial_overlay)
                textView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                textView2.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                textView2.setTypeface(null, Typeface.BOLD)
            }
            3 -> {
                imageView.setImageResource(R.drawable.tutorial_race)
                textView2.text = getString(R.string.tutorial_race)
                textView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                textView2.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                textView2.setTypeface(null, Typeface.BOLD)
            }
            4 -> {
                imageView.setImageResource(R.drawable.tutorial_planet)
                imageView.setPadding(15)
                textView2.text = getString(R.string.tutorial_planet)
                textView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                textView2.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                textView2.setTypeface(null, Typeface.BOLD)
                button.text = getString(R.string.tutorial_start)
            }
        }

        button.setOnClickListener {
            if (position < 4) {
                // 다음 페이지로 이동
                (activity as? TutorialActivity)?.moveToNextPage(position)
            } else {
                // 튜토리얼 완료 후 메인 화면으로 이동
                (activity as? TutorialActivity)?.finishTutorial()
            }
        }

        return view
    }

    companion object {
        fun newInstance(position: Int): TutorialFragment {
            val fragment = TutorialFragment()
            val args = Bundle()
            args.putInt("position", position)
            fragment.arguments = args
            return fragment
        }
    }
}