package com.rocket.cosmic_detox.presentation.view.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ActivitySignInBinding
import com.rocket.cosmic_detox.presentation.component.dialog.TwoButtonDialogDescFragment
import com.rocket.cosmic_detox.presentation.viewmodel.SignInViewModel
import com.rocket.cosmic_detox.util.Authentication
import com.rocket.cosmic_detox.util.Constants.NOTION_LINK
import com.rocket.cosmic_detox.util.Constants.PROVIDER_TWITTER
import dagger.hilt.android.AndroidEntryPoint

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
            signInViewModel.googleLogin()
        }
        signInBinding.ivKakao.setOnClickListener {
            signInViewModel.kakaoLogin()
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
        signInBinding.ivX.setOnClickListener {
            signInWithTwitter()
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
                    //signInViewModel.signInWithX()
                    Log.d("Twitter", "로그인 성공: 새로운 사용자가 로그인했습니다.")
                }.addOnFailureListener {
                    // 실패
                    Log.e("Twitter", "로그인 실패: 사용자가 로그인하지 않았습니다.")
                }
        }
    }
}