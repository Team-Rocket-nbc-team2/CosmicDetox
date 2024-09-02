package com.rocket.cosmic_detox.presentation.view.fragment

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.FragmentHomeBinding
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogFragment
import com.rocket.cosmic_detox.presentation.extensions.loadHomePlanetImage
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
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
                title = getString(R.string.home_travel_start),
                onClickConfirm = {
                    val action = HomeFragmentDirections.actionHomeToTimer()
                    findNavController().navigate(action) },
                onClickCancel = { false }
            )
            dialog.isCancelable = false
            dialog.show(getParentFragmentManager(), "ConfirmDialog")
        }
        binding.ivHomeMyPlanet.loadHomePlanetImage(BigDecimal(800000))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}