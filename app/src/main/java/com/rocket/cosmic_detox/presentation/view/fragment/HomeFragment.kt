package com.rocket.cosmic_detox.presentation.view.fragment

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.FragmentHomeBinding
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogFragment

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

        // 이미지뷰 받아오기 및 이렇게 가능한 코드인지 여쭤보기

        val imageContainer = view?.findViewById<ConstraintLayout>(R.id.cl_home_illustration)

        val imageResIds = listOf(
            R.drawable.mercury,
            R.drawable.mars,
            R.drawable.venus,
            R.drawable.earth,
            R.drawable.neptune,
            R.drawable.uranus,
            R.drawable.saturn,
            R.drawable.jupiter,
            R.drawable.sun
        )

        for (i in imageResIds.indices) {
            val imageView = ImageView(requireContext())

            imageView.setImageResource(imageResIds[i])

            val imageSize = ConstraintLayout.LayoutParams(
                when (i) {
                    0 -> 100.dp
                    1 -> 120.dp
                    2 -> 140.dp
                    3 -> 145.dp
                    4 -> 160.dp
                    5 -> 222.dp
                    6 -> 265.dp
                    7 -> 290.dp
                    8 -> 345.dp
                },
                when (i) {
                    0 -> 100.dp
                    1 -> 120.dp
                    2 -> 140.dp
                    3 -> 145.dp
                    4 -> 160.dp
                    5 -> 222.dp
                    6 -> 265.dp
                    7 -> 290.dp
                    8 -> 345.dp
                }
            )

            // margin 설정
            imageSize.setMargins(16.dp)
            imageView.layoutParams = imageSize

            // ImageView cl에 추가
            imageContainer?.addView(imageView)
        }

        return view
    }

    private val Int.dp: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            resources.displayMetrics
        ).toInt()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() = with(binding) {
        btnNavigateToTimer.setOnClickListener {
            val dialog = TwoButtonDialogFragment(
                getString(R.string.home_travel_start)
            ) {
                val action = HomeFragmentDirections.actionHomeToTimer()
                findNavController().navigate(action)
            }
            dialog.isCancelable = false
            dialog.show(getParentFragmentManager(), "ConfirmDialog")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}