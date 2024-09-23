package com.rocket.cosmic_detox.presentation.view.fragment.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.FragmentSettingsBinding
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogDescFragment
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogFragment
import com.rocket.cosmic_detox.presentation.uistate.LoginUiState
import com.rocket.cosmic_detox.presentation.view.activity.SignInActivity
import com.rocket.cosmic_detox.presentation.viewmodel.UserViewModel
import com.rocket.cosmic_detox.util.Constants.PRIVACY_POLICY_LINK
import com.rocket.cosmic_detox.util.Constants.TERMS_OF_USE_LINK
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private val binding by lazy { FragmentSettingsBinding.inflate(layoutInflater) }
    private val userViewModel by viewModels<UserViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observeSignOutState()
        observeDeleteUserState()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        with(binding) {
            tvAppVersion.text = getAppVersion()

            layoutPrivacyPolicy.setOnClickListener { startLink(Uri.parse(PRIVACY_POLICY_LINK)) }
            layoutTermsOfService.setOnClickListener { startLink(Uri.parse(TERMS_OF_USE_LINK)) }
            layoutDeleteUser.setOnClickListener {
                showDialogWithDescription(
                    title = getString(R.string.dialog_withdrawal),
                    description = getString(R.string.dialog_withdrawal_desc),
                    onConfirmClick = { userViewModel.deleteUser() }
                )
            }
            layoutSignOut.setOnClickListener {
                showDialog(
                    title = getString(R.string.dialog_sign_out),
                    onConfirmClick = { userViewModel.signOut() }
                )
            }
            ivBackMyPage.setOnClickListener {
                findNavController().navigate(SettingsFragmentDirections.actionNavigationSettingToNavigationMy())
            }
        }


        return binding.root
    }

    private fun observeSignOutState() {
        lifecycleScope.launch {
            userViewModel.signOutState.collectLatest {
                when (it) {
                    is LoginUiState.Success -> {
                        val intent = Intent(context, SignInActivity::class.java)
                        startActivity(intent)

                        activity?.finish()
                    }
                    is LoginUiState.Failure -> {
                        Toast.makeText(context, "signOut failed. ${it.e}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun observeDeleteUserState() {
        lifecycleScope.launch {
            userViewModel.deleteUserState.collectLatest {
                when (it) {
                    is LoginUiState.Success -> {
                        val intent = Intent(context, SignInActivity::class.java)
                        startActivity(intent)

                        activity?.finish()
                    }
                    is LoginUiState.Failure -> {
                        Toast.makeText(context, "deleteUser failed. ${it.e}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun showDialogWithDescription(
        title: String,
        description: String,
        onConfirmClick: () -> Unit
    ) {
        val dialog = TwoButtonDialogDescFragment(
            title = title,
            description = description,
            onClickConfirm = onConfirmClick,
            onClickCancel = {}
        )
        dialog.isCancelable = false
        dialog.show(getParentFragmentManager(), "ConfirmDialog")
    }

    private fun showDialog(
        title: String,
        onConfirmClick: () -> Unit
    ) {
        val dialog = TwoButtonDialogFragment(
            title = title,
            onClickConfirm = onConfirmClick,
            onClickCancel = {}
        )
        dialog.isCancelable = false
        dialog.show(getParentFragmentManager(), "ConfirmDialog")
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