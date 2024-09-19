package com.rocket.cosmic_detox.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rocket.cosmic_detox.domain.usecase.kakao.KakaoLoginUseCase
import com.rocket.cosmic_detox.domain.usecase.signin.GoogleSignInUseCase
import com.rocket.cosmic_detox.presentation.uistate.LoginUiState
import com.rocket.cosmic_detox.presentation.uistate.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val kakaoLoginUseCase: KakaoLoginUseCase
) : ViewModel() {
    private val _isSignIn = MutableStateFlow<LoginUiState>(LoginUiState.Init)
    val isSignIn: StateFlow<LoginUiState> = _isSignIn.asStateFlow()

    fun googleLogin() {
        viewModelScope.launch {
            _isSignIn.value = LoginUiState.Loading
            googleSignInUseCase.invoke(
                onSuccess = { _isSignIn.value = LoginUiState.Success },
                onFailure = { exception ->
                    _isSignIn.value = LoginUiState.Failure(exception)
                },
                onCancel = {
                    _isSignIn.value = LoginUiState.Cancel
                }
            )
        }
    }

    fun kakaoLogin() {
        viewModelScope.launch {
            _isSignIn.value = LoginUiState.Loading

            kakaoLoginUseCase(
                onSuccess = { _isSignIn.value = LoginUiState.Success },
                onFailure = { exception ->
                    _isSignIn.value = LoginUiState.Failure(exception)
                },
                onCancel = {
                    _isSignIn.value = LoginUiState.Cancel
                }
            )
        }
    }

//    fun signInWithX() {
//        _status.value = UiState.Loading
//        _user.value = auth.currentUser
//        viewModelScope.launch {
//            try {
//                repository.setDataToFireBase()
//                    .onSuccess {
//                        _status.value = UiState.Success(_user.value!!)
//                        Log.d("LOGIN-- SUCCESS: setUserToFirestoreWithX() ", "FireStore DB에 데이터 저장을 성공했습니다, ${_user.value}")
//                    }
//                    .onFailure {
//                        _status.value = UiState.Failure(it)
//                        Log.e("LOGIN-- FAILURE: setUserToFirestoreWithX()", "FireStore DB에 데이터 저장을 실패했습니다. ${it}")
//                    }
//            } catch (e: Exception) {
//                _status.value = UiState.Failure(e)
//                Log.e("LOGIN-- FAILURE: setUserToFirestoreWithX() catch", "FireStore DB에 데이터 저장을 실패했습니다. ${e}")
//            }
//        }
//    }
}