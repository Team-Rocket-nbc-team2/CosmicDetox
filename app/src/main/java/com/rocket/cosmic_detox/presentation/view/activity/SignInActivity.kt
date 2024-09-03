package com.rocket.cosmic_detox.presentation.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ActivitySignInBinding
import com.rocket.cosmic_detox.presentation.uistate.UiState
import com.rocket.cosmic_detox.presentation.viewmodel.SignInViewModel
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

    // 자동 로그인 로직
    override fun onStart() {
        super.onStart()

        val user = signInViewModel.auth.currentUser

        user?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(signInBinding.root)

        signInBinding.ivGoogle.setOnClickListener {
            signInViewModel.googleLogin(googleSignInClient, launcher)
        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            signInViewModel.googleLauncherFunction(result)
            signInObserve(this)
        }

    private fun signInObserve(context: Context) {
        lifecycleScope.launch {
            signInViewModel.status.collectLatest {
                when (it) {
                    is UiState.Success -> {
                        val intent = Intent(context, MainActivity::class.java)
                        startActivity(intent)
                    }
                    else -> {
                        // 로그인 실패 또는 로딩 중
                    }
                }
            }
        }
    }
}