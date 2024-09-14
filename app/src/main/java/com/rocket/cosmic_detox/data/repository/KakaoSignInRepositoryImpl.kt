package com.rocket.cosmic_detox.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.rocket.cosmic_detox.domain.repository.KakaoSignInRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class KakaoSignInRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userApiClient: UserApiClient,
    private val auth: FirebaseAuth,
    private val functions: FirebaseFunctions
): KakaoSignInRepository {
    override fun kakaoLogin(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        if (userApiClient.isKakaoTalkLoginAvailable(context)) {
            loginWithKakaoTalk(onSuccess, onFailure)
        } else {
            loginWithKaKaoAccount(onSuccess, onFailure)
        }
    }

    private fun loginWithKakaoTalk(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        userApiClient.loginWithKakaoTalk(context) { token, error ->
            if (error != null) {
                if (error is ClientError && error.reason == ClientErrorCause.Cancelled) onFailure(error)
                else loginWithKaKaoAccount(onSuccess, onFailure)
            } else if (token != null) {
                getCustomToken(token.accessToken, onSuccess, onFailure)
            }
        }
    }

    // 카카오 계정으로 로그인
    private fun loginWithKaKaoAccount(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        userApiClient.loginWithKakaoAccount(context) { token: OAuthToken?, error: Throwable? ->
            if (error != null) {
                onFailure(error)
            } else if (token != null) {
                getCustomToken(token.accessToken, onSuccess, onFailure)
            }
        }
    }

    private fun getCustomToken(accessToken: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        val data = hashMapOf("token" to accessToken)

        functions.getHttpsCallable("kakaoCustomAuth")
            .call(data)
            .addOnCompleteListener { task ->
                try {
                    val result = task.result?.data as HashMap<*, *>
                    var mKey: String? = null
                    for (key in result.keys) {
                        mKey = key.toString()
                    }
                    val customToken = result[mKey!!].toString()

                    firebaseAuthWithKakao(customToken, onSuccess, onFailure)
                } catch (e: Exception) {
                    // 호출 실패
                    onFailure(e)
                }
            }
    }

    private fun firebaseAuthWithKakao(customToken: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        auth.signInWithCustomToken(customToken).addOnCompleteListener { result ->
            if (result.isSuccessful) {
                onSuccess()
            } else {
                onFailure(result.exception!!)
            }
        }
    }
}