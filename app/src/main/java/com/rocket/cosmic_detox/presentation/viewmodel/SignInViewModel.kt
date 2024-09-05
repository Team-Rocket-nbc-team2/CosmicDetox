package com.rocket.cosmic_detox.presentation.viewmodel

import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.rocket.cosmic_detox.domain.repository.SignInRepository
import com.rocket.cosmic_detox.presentation.uistate.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SignInViewModel @Inject constructor(
    private val repository: SignInRepository,
) : ViewModel() {
    private val _user = MutableStateFlow<FirebaseUser?>(null)
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _status =  MutableStateFlow<UiState<FirebaseUser>>(UiState.Init)
    val status: StateFlow<UiState<FirebaseUser>> = _status.asStateFlow()

    fun googleLogin(googleSignInClient: GoogleSignInClient, launcher: ActivityResultLauncher<Intent>) {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    fun googleLauncherFunction(result: ActivityResult) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            _status.value = UiState.Failure(e)
            Log.e("LOGIN-- FAILURE: googleLauncherFunction", e.toString())
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        _status.value = UiState.Loading

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _user.value = auth.currentUser
                    // 코루틴 블록 내에서 suspend 함수 호출
                    viewModelScope.launch {
                        try {
                            val result = repository.setDataToFireBase()
                            if (result.isSuccess) {
                                _status.value = UiState.Success(_user.value!!)
                                Log.d("LOGIN-- SUCCESS: firebaseAuthWithGoogle", "Firebase 인증에 성공했습니다.")
                            } else {
                                _status.value = UiState.Failure(result.exceptionOrNull())
                                Log.e("LOGIN-- FAILURE: firebaseAuthWithGoogle", "Firebase 인증에 실패했습니다. ${result.exceptionOrNull()}")
                            }
                        } catch (e: Exception) {
                            _status.value = UiState.Failure(e)
                            Log.e("LOGIN-- FAILURE: firebaseAuthWithGoogle", "Firebase 인증에 실패했습니다. ${e}")
                        }
                    }
                } else {
                    _status.value = UiState.Failure(task.exception)
                    Log.e("LOGIN-- FAILURE: firebaseAuthWithGoogle", "Firebase 인증에 실패했습니다. ${task.exception}")
                }
            }
    }
}