package com.rocket.cosmic_detox.presentation.view.fragment.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rocket.cosmic_detox.databinding.FragmentSettingsBinding
import com.rocket.cosmic_detox.util.Constants.PRIVACY_POLICY_LINK
import com.rocket.cosmic_detox.util.Constants.TERMS_OF_USE_LINK

class SettingsFragment : Fragment() {
    private val binding by lazy { FragmentSettingsBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        with(binding) {
            tvAppVersion.text = getAppVersion()

            layoutPrivacyPolicy.setOnClickListener { startLink(Uri.parse(PRIVACY_POLICY_LINK)) }
            layoutTermsOfService.setOnClickListener { startLink(Uri.parse(TERMS_OF_USE_LINK)) }
            layoutDeleteUser.setOnClickListener {

            }
            layoutSignOut.setOnClickListener {

            }
            ivBackMyPage.setOnClickListener {
                findNavController().navigate(SettingsFragmentDirections.actionNavigationSettingToNavigationMy())
            }
        }


        return binding.root
    }

    private fun showDialog() {

    }

    private fun startLink(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun getAppVersion(): String {
        val packageInfo = activity?.packageManager?.getPackageInfo(activity?.packageName.toString(), 0)
        return packageInfo?.versionName.toString()
    }
}