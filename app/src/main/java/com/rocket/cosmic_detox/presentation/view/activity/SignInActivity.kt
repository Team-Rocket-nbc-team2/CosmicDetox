package com.rocket.cosmic_detox.presentation.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.Global
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ActivityMainBinding
import com.rocket.cosmic_detox.databinding.ActivitySignInBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {
    //nullable한 FirebaseAuth 객체 선언
    var auth: FirebaseAuth? = null;
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
            //requestIdToken :필수사항이다. 사용자의 식별값(token)을 사용하겠다.
            //(App이 구글에게 요청)
            .requestEmail()
            // 사용자의 이메일을 사용하겠다.(App이 구글에게 요청)
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)

        signInBinding.ivGoogle.setOnClickListener {
            Log.d("working~?", "clicked!")
            googleLogin()
        }
    }

    private fun googleLogin() {
        //1. 구글로 로그인을 한다.
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent,1004)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode:Int, resultCode: Int, data: Intent?){
        //Activity.Result_OK : 정상완료
        //Activity.Result_CANCEL : 중간에 취소 되었음(실패)
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==1004){
            if(resultCode == Activity.RESULT_OK){
                //결과 Intent(data 매개변수) 에서 구글로그인 결과 꺼내오기
                val result = data?.let { Auth.GoogleSignInApi.getSignInResultFromIntent(it) }!!

                //정상적으로 로그인되었다면
                if(result.isSuccess){
                    //우리의 Firebase 서버에 사용자 이메일정보보내기
                    val account = result.signInAccount
                    firebaseAuthWithGoogle(account)
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        //구글로부터 로그인된 사용자의 정보(Credentail)을 얻어온다.
        val credential = GoogleAuthProvider.getCredential(account?.idToken!!, null)
        //그 정보를 사용하여 Firebase의 auth를 실행한다.
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener {  //통신 완료가 된 후 무슨일을 할지
                    task ->
                if (task.isSuccessful) {
                    // 로그인 처리를 해주면 됨!
                    startActivity(Intent(this, MainActivity::class.java))
//                    goMainActivity(task.result?.user)
                } else {
                    // 오류가 난 경우!
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
//                progressBar.visibility = View.GONE
            }
    }
}