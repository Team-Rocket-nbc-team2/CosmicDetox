package com.rocket.cosmic_detox.presentation.view.fragment.tutorial

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.FragmentTutorialBinding
import com.rocket.cosmic_detox.presentation.view.activity.TutorialActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TutorialFragment : Fragment() {

    private var _binding: FragmentTutorialBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentTutorialBinding.inflate(inflater, container, false)

        val position = arguments?.getInt("position") ?: 0

        // position에 따라 이미지, 텍스트 및 버튼 설정
        setupTutorialPage(position)
        binding.btnTutorial.setOnClickListener {
            if (position < 4) {
                // 다음 페이지로 이동
                (activity as? TutorialActivity)?.moveToNextPage(position)
            } else {
                // 튜토리얼 완료 후 메인 화면으로 이동
                (activity as? TutorialActivity)?.finishTutorial()
            }
        }
        return binding.root
    }

    // 프래그먼트의 각 페이지에 대한 설정을 처리하는 함수
    private fun setupTutorialPage(position: Int) {
        when (position) {
            0 -> {
                setPageContent(
                    R.drawable.tutorial_icon,
                    R.string.tutorial_addiction,
                    R.string.tutorial_cosmic,
                    R.string.tutorial_next
                )
            }
            1 -> {
                setPageContent(
                    R.drawable.tutorial_allowed_app,
                    null,
                    R.string.tutorial_app,
                    R.string.tutorial_next,
                    20f,
                    R.color.white,
                    Typeface.BOLD
                )
            }
            2 -> {
                setPageContent(
                    R.drawable.tutorial_overlay,
                    null,
                    R.string.tutorial_overlay,
                    R.string.tutorial_next,
                    20f,
                    R.color.white,
                    Typeface.BOLD
                )
            }
            3 -> {
                setPageContent(
                    R.drawable.tutorial_race,
                    null,
                    R.string.tutorial_race,
                    R.string.tutorial_next,
                    20f,
                    R.color.white,
                    Typeface.BOLD
                )
            }
            4 -> {
                setPageContent(
                    R.drawable.tutorial_planet,
                    null,
                    R.string.tutorial_planet,
                    R.string.tutorial_start,
                    20f,
                    R.color.white,
                    Typeface.BOLD
                )
                binding.ivTutorialIcon.setPadding(15)
            }
        }
    }

    // 이미지 및 텍스트 설정을 위한 함수
    private fun setPageContent(
        imageResId: Int,
        text1ResId: Int? = null,
        text2ResId: Int,
        buttonTextResId: Int,
        textSize: Float = 14f,
        textColorResId: Int = R.color.white20,
        textStyle: Int = Typeface.NORMAL
    ) {
        binding.ivTutorialIcon.setImageResource(imageResId)

        text1ResId?.let {
            binding.tvTutorialAddiction.text = getString(it)
            binding.tvTutorialAddiction.isVisible = true
        } ?: run {
            binding.tvTutorialAddiction.isVisible = false
        }

        binding.tvTutorialCosmic.apply {
            text = getString(text2ResId)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
            setTextColor(ContextCompat.getColor(requireContext(), textColorResId))
            setTypeface(null, textStyle)
        }

        binding.btnTutorial.text = getString(buttonTextResId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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