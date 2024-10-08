package com.rocket.cosmic_detox.presentation.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ActivitySignInBinding
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogDescFragment
import com.rocket.cosmic_detox.presentation.uistate.LoginUiState
import com.rocket.cosmic_detox.presentation.viewmodel.SignInViewModel
import com.rocket.cosmic_detox.util.Constants.NOTION_LINK
import com.rocket.cosmic_detox.util.SharedPreferencesUtil.isFirstTimeUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {
    private val signInBinding by lazy { ActivitySignInBinding.inflate(layoutInflater) }
    private val signInViewModel by viewModels<SignInViewModel>()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(signInBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sign_in)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        signInBinding.ivGoogle.setOnClickListener {
            signInViewModel.googleLogin(this)
        }
        signInBinding.ivKakao.setOnClickListener {
            signInViewModel.kakaoLogin()
        }
        signInBinding.ivX.setOnClickListener {
            signInViewModel.twitterLogin(this)
        }
        signInBinding.tvRulesPolicy.setOnClickListener {
            val dialog = TwoButtonDialogDescFragment(
                title = getString(R.string.dialog_personal_policy_terms_title),
                description = getString(R.string.dialog_personal_policy_terms_desc),
                onClickConfirm = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(NOTION_LINK))
                    startActivity(intent)
                },
                onClickCancel = {})
            dialog.isCancelable = false
            dialog.show(supportFragmentManager, "ConfirmDialog")
        }

        observeIsSignIn()
    }

    private fun observeIsSignIn() {
        lifecycleScope.launch {
            signInViewModel.isSignIn.collectLatest {
                signInBinding.layoutSignInLoading.isVisible = it is LoginUiState.Loading

                when (it) {
                    is LoginUiState.Success -> {
                        if (isFirstTimeUser(applicationContext)) {
                            val intent = Intent(this@SignInActivity, TutorialActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        } else {
                            val intent = Intent(this@SignInActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                    is LoginUiState.Failure -> {
                        Toast.makeText(this@SignInActivity, "${getString(R.string.sign_failure)} ${it.e}", Toast.LENGTH_SHORT).show()
                        Log.e("SignInActivity", "signInViewModel.isSignIn.collectLatest: ${it.e}")
                    }
                    is LoginUiState.Cancel -> {
                        Toast.makeText(this@SignInActivity, getString(R.string.sign_canceled), Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }
}