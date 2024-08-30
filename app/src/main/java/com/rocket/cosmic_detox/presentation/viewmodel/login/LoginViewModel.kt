package com.rocket.cosmic_detox.presentation.viewmodel.login

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.rocket.cosmic_detox.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {
    private val _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> = _user

    var auth: FirebaseAuth? = null;
    private lateinit var googleSignInClient: GoogleSignInClient

    init{
        //auth 객체 초기화
        auth = FirebaseAuth.getInstance()

        //GoogleSignInClient 객체 초기화
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //기본 로그인 방식 사용
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

//        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

//    fun googleSignIn() = viewModelScope.launch {
//        val signInIntent = googleSignInClient.signInIntent
//        startActivityForResult(signInIntent, RC_SIGN_IN)
//
//        // Firebase 인증
//        val account = result.signInAccount
//        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
//        val authResult = firebaseAuth.signInWithCredential(credential).await()
//
//        if(requestCode==1004){
//            Log.d("Google Login ...", "0. request Code : $requestCode")
//            if(resultCode == Activity.RESULT_OK){
//                //결과 Intent(data 매개변수) 에서 구글로그인 결과 꺼내오기
//                val result = data?.let { Auth.GoogleSignInApi.getSignInResultFromIntent(it) }!!
//                Log.d("Google Login ...", "1. api result : $result")
//
//                //정상적으로 로그인되었다면
//                if(result.isSuccess){
//                    //우리의 Firebase 서버에 사용자 이메일정보보내기
//                    val account = result.signInAccount
//                    firebaseAuthWithGoogle(account)
//                }
//            }
//        }
//    }
}