package com.rocket.cosmic_detox.presentation.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ActivitySignInBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val signInBinding by lazy { ActivitySignInBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(signInBinding.root)

        //auth 객체 초기화
        auth = FirebaseAuth.getInstance()

        //GoogleSignInClient 객체 초기화
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //기본 로그인 방식 사용
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 이미 로그인되어 있는지 확인
        if (auth.currentUser != null) {
            // 이미 로그인된 경우 메인 화면으로 이동
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        signInBinding.ivGoogle.setOnClickListener {
            Log.d("working~?", "clicked!")
            googleLogin()
        }
    }

    private fun googleLogin() {
        //1. 구글로 로그인을 한다.
        val signInIntent = googleSignInClient.signInIntent
        testLauncher.launch(signInIntent)
    }

    private val testLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            Log.d("LOGIN--", task.toString())

            try {
                // Google 로그인이 성공하면, Firebase로 인증합니다.
                val account = task.getResult(ApiException::class.java)!!
                Log.d("LOGIN--22", account.idToken!!)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.d("LOGIN--", e.toString())
                // Google 로그인 실패
                Toast.makeText(this, "Google 로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.d("LOGIN--3", idToken)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 로그인 성공
                    val user = auth.currentUser
                    Toast.makeText(this, "환영합니다, ${user?.displayName}!", Toast.LENGTH_SHORT).show()
                    // 여기서 로그인 후 화면 전환 등의 작업을 수행할 수 있습니다.
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // 로그인 실패
                    Toast.makeText(this, "Firebase 인증에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}