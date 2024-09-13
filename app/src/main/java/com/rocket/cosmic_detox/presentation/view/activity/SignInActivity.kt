package com.rocket.cosmic_detox.presentation.view.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ActivitySignInBinding
import com.rocket.cosmic_detox.presentation.component.dialog.OneButtonDialogFragment
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogDescFragment
import com.rocket.cosmic_detox.presentation.uistate.LoginUiState
import com.rocket.cosmic_detox.presentation.uistate.UiState
import com.rocket.cosmic_detox.presentation.viewmodel.SignInViewModel
import com.rocket.cosmic_detox.util.Authentication
import com.rocket.cosmic_detox.util.Constants.NOTION_LINK
import com.rocket.cosmic_detox.util.Constants.PROVIDER_TWITTER
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignInActivity() : AppCompatActivity() {
    private val signInBinding by lazy { ActivitySignInBinding.inflate(layoutInflater) }
    private val signInViewModel by viewModels<SignInViewModel>()
    val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //기본 로그인 방식 사용
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        GoogleSignIn.getClient(this, gso)
    }

    private val auth = FirebaseAuth.getInstance()

    // 자동 로그인 로직
    override fun onStart() {
        super.onStart()

        val user = auth.currentUser

        user?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(signInBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sign_in)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        signInObserve(this)

        signInBinding.ivGoogle.setOnClickListener {
            signInViewModel.googleLogin(googleSignInClient, launcher)
        }
        signInBinding.ivKakao.setOnClickListener {
            signInViewModel.kakaoLogin()
        }
        signInBinding.tvRulesPolicy.setOnClickListener {
            val dialog =
                TwoButtonDialogDescFragment(
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
        signInBinding.ivX.setOnClickListener {
            signInWithTwitter()
        }

        observeKakaoLogin()
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            signInViewModel.googleLauncherFunction(result)
        }

    private fun signInObserve(context: Context) {
        lifecycleScope.launch {
            signInViewModel.status.collectLatest {
                when (it) {
                    is UiState.Success -> {
                        val intent = Intent(this@SignInActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        Authentication.setCurrentUser(it.data)
                    }
                    is UiState.Failure -> {
                        val dialog = OneButtonDialogFragment(getString(R.string.sign_failure)){}
                        dialog.isCancelable = false
                        dialog.show(supportFragmentManager, "ConfirmDialog")
                    }
                    else -> {
                        // 로딩 중
                    }
                }
            }
        }
    }

    private fun observeKakaoLogin() {
        lifecycleScope.launch {
            signInViewModel.kakaoLogin.collectLatest {
                when (it) {
                    LoginUiState.Init -> {}
                    LoginUiState.Loading -> {}
                    LoginUiState.Success -> {
                        val intent = Intent(this@SignInActivity, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }

                    is LoginUiState.Failure -> {
                        Log.e("TAG", "observeKakaoLogin failed: ${it.e}", it.e.cause)
                    }
                }
            }
        }
    }

    private fun signInWithTwitter() {
        val provider = OAuthProvider.newBuilder(PROVIDER_TWITTER)

        val pendingResultTask = auth.pendingAuthResult
        if (pendingResultTask != null) {
            pendingResultTask.addOnSuccessListener {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                Log.d("Twitter", "로그인 성공: 이전에 로그인한 사용자가 있습니다.")

            }.addOnFailureListener {
                // 실패
                Log.e("Twitter", "로그인 실패: 이전에 로그인한 사용자가 없습니다.")
            }
        } else {
            auth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener {
                    val user = auth.currentUser
                    // 성공
                    signInViewModel.signInWithX()
                    Log.d("Twitter", "로그인 성공: 새로운 사용자가 로그인했습니다.")
                }.addOnFailureListener {
                    // 실패
                    Log.e("Twitter", "로그인 실패: 사용자가 로그인하지 않았습니다.")
                }
        }
    }
}