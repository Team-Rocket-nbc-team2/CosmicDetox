package com.rocket.cosmic_detox.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.domain.repository.SignInRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

// TODO: UseCase로 리팩토링 필요
class SignInRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userApiClient: UserApiClient,
    private val fireStore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val functions: FirebaseFunctions
) : SignInRepository {
    override suspend fun googleSignIn(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .build()
        val credentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val googleSignInRequest = credentialManager.getCredential(
                request = credentialRequest,
                context = context
            )
            val credential = googleSignInRequest.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                googleSignInWithFirebaseAuth(googleIdTokenCredential.idToken, onSuccess, onFailure)
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    private fun googleSignInWithFirebaseAuth(
        idToken: String,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val authCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(authCredential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                initUserData(onSuccess, onFailure)
            } else {
                onFailure(task.exception!!)
            }
        }
    }

    // --- google login ---

    override fun kakaoSignIn(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        if (userApiClient.isKakaoTalkLoginAvailable(context)) {
            signInWithKakaoTalk(onSuccess, onFailure)
        } else {
            signInWithKaKaoAccount(onSuccess, onFailure)
        }
    }

    private fun signInWithKakaoTalk(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        userApiClient.loginWithKakaoTalk(context) { token, error ->
            if (error != null) {
                if (error is ClientError && error.reason == ClientErrorCause.Cancelled) onFailure(error)
                else signInWithKaKaoAccount(onSuccess, onFailure)
            } else if (token != null) {
                getCustomToken(token.accessToken, onSuccess, onFailure)
            }
        }
    }

    // 카카오 계정으로 로그인
    private fun signInWithKaKaoAccount(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
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

                    kakaoSignInWithFirebaseAuth(customToken, onSuccess, onFailure)
                } catch (e: Exception) {
                    // 호출 실패
                    onFailure(e)
                }
            }
    }

    private fun kakaoSignInWithFirebaseAuth(customToken: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        auth.signInWithCustomToken(customToken).addOnCompleteListener { result ->
            if (result.isSuccessful) initUserData(onSuccess, onFailure)
            else onFailure(result.exception!!)
        }
    }

    // --- kakao login ---

    private fun initUserData(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid.toString()

        // todo :: rankingUserRef의 document를 현재 달로 맞출 필요가 있음. 다른 랭킹쪽 로직도 포함.
        val userRef = fireStore.collection("users").document(uid)
        val rankingUserRef = fireStore.collection("season").document("season-2024-08")

        try {
            userRef.get().addOnCompleteListener { userTask ->
                if (userTask.isSuccessful) {
                    if (!userTask.result.exists()) {
                        // 최초 로그인
                        val userMap = hashMapOf(
                            "uID" to uid,
                            "name" to currentUser?.displayName.toString(),
                            "dailyTime" to 0L,
                            "totalTime" to 0L,
                            "totalDay" to 1,
                            "isWithdrawn" to false,
                        )
                        val userRankingMap = hashMapOf(
                            "uid" to uid,
                            "name" to currentUser?.displayName.toString(),
                            "point" to 0,
                            "totalTime" to 0,
                        )

                        userRef.set(userMap).addOnCompleteListener { userSetTask ->
                            if (!userSetTask.isSuccessful) throw userSetTask.exception!!
                        }
                        rankingUserRef.collection("ranking").document(uid).set(userRankingMap).addOnCompleteListener { rankingSetTask ->
                            if (!rankingSetTask.isSuccessful) throw rankingSetTask.exception!!
                        }
                    }
                    onSuccess()
                }
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}