package com.rocket.cosmic_detox.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.rocket.cosmic_detox.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


@HiltViewModel
class SignInViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {
    private val _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> = _user

    //auth 객체 초기화
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    var googleSignInClient: GoogleSignInClient


    init{
        //GoogleSignInClient 객체 초기화
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //기본 로그인 방식 사용
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun googleLauncherFunction(result: ActivityResult) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            // Google 로그인 실패
            // TODO: 여기서 Dialog 띄우기
            Log.d("LOGIN--", e.toString())
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.d("LOGIN--3", idToken)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 로그인 성공
                    _user.value = auth.currentUser
//                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    // 로그인 실패
                    // TODO: 여기서 Dialog 띄우기2
                    Log.d("LOGIN--", "Firebase 인증에 실패했습니다.")
                }
            }
    }
}