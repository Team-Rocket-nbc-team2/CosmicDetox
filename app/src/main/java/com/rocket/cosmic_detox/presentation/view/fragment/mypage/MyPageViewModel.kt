package com.rocket.cosmic_detox.presentation.view.fragment.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.AppUsage
import com.rocket.cosmic_detox.data.model.User
import com.rocket.cosmic_detox.domain.repository.MyPageRepository
import com.rocket.cosmic_detox.presentation.uistate.MyPageUiState
import com.rocket.cosmic_detox.presentation.uistate.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val repository: MyPageRepository,
    private val firestoreDB: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : ViewModel() {
    private val _myInfo = MutableStateFlow<MyPageUiState<User>>(MyPageUiState.Loading)
    val myInfo: StateFlow<MyPageUiState<User>> = _myInfo

    private val _myAppUsageList = MutableStateFlow<MyPageUiState<List<AppUsage>>>(MyPageUiState.Loading)
    val myAppUsageList: StateFlow<MyPageUiState<List<AppUsage>>> = _myAppUsageList

    private val _updateResult = MutableStateFlow(false)
    val updateResult: StateFlow<Boolean> = _updateResult

    private val _userStatus =  MutableStateFlow<UiState<FirebaseUser>>(UiState.Init)
    val userStatus: StateFlow<UiState<FirebaseUser>> = _userStatus.asStateFlow()

    fun loadMyInfo() {
        viewModelScope.launch {
            repository.getMyInfo()
                .catch {
                    Log.e("MyPageViewModel", "loadMyInfo: $it")
                    _myInfo.value = MyPageUiState.Error(it.toString())
                }
                .collect {
                    Log.d("MyPageViewModel", "User: $it")
                    _myInfo.value = MyPageUiState.Success(it)
                }
        }
    }

    fun loadMyAppUsage() {
        viewModelScope.launch {
            repository.getMyAppUsage()
                .catch {
                    _myAppUsageList.value = MyPageUiState.Error(it.toString())
                    Log.e("MyPageViewModel", "loadMyAppUsage: $it")
                }
                .collect {
                    _myAppUsageList.value = MyPageUiState.Success(it)
                }
        }
    }

    fun setAppUsageLimit(allowedApp: AllowedApp, hour: String, minute: String) {
        viewModelScope.launch {
            //val limitedTime = (hour.toInt() * 60 + minute.toInt()) * 60 * 1000L // 시간과 분을 밀리초로 변환
            val limitedTime = (hour.toInt() * 60 + minute.toInt()) * 60 // 시간과 분을 초로 변환

//            val success = repository.updateAppUsageLimit(allowedApp.copy(limitedTime = limitedTime.toInt()))
//            _updateResult.value = success
//            if (success) {
//                loadMyInfo()
//            }
            repository.updateAppUsageLimit(allowedApp.copy(limitedTime = limitedTime.toInt()))
                .onSuccess {
                    // TODO: 나중에 UiState로 변경해보기
                    _updateResult.value = it
                    loadMyInfo()
                }.onFailure {
                    Log.e("MyPageViewModel", "업데이트 실패: $it")
                }
        }
    }

    fun resetUpdateResult() {
        _updateResult.value = false
    }

    // Google ID Token을 사용하여 Firebase에서 재인증하는 함수
    fun reAuthenticateWithGoogle(idToken: String) {
        val user = firebaseAuth.currentUser
        if (user != null) {
            _userStatus.value = UiState.Loading

            val credential = GoogleAuthProvider.getCredential(idToken, null)

            // 재인증 로직
            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // 재인증 성공 후 회원 탈퇴 로직 실행
                        withdraw(user)
                    } else {
                        _userStatus.value = UiState.Failure(task.exception)
                        Log.e("withdrawal", "재인증 실패 XXXXX: ${task.exception}")
                    }
                }
        } else {
            _userStatus.value = UiState.Failure(Exception("유저가 없음."))
        }
    }

    // Firebase 사용자 삭제 로직
    // Firebase 사용자 삭제 로직 및 Firestore 데이터 삭제 로직
    private fun withdraw(user: FirebaseUser) {
        user.delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("withdrawal", "유저 삭제 성공 OOOOO")
                    withdrawUserCoroutine(user) // Firestore 데이터 삭제
                } else {
                    _userStatus.value = UiState.Failure(task.exception)
                    Log.e("withdrawal", "유저 삭제 실패 XXXXX: ${task.exception}")
                }
            }
    }

    private fun withdrawUserCoroutine(user: FirebaseUser) = viewModelScope.launch {
        val userRef = firestoreDB.collection("users").document(user.uid)
        val rankingRef = firestoreDB.collection("season").document("season-2024-08").collection("ranking").document(user.uid)

        try {
            awaitAll(
                async { userRef.delete() },
                async { rankingRef.delete() }
            )

            Log.d("회원탈퇴 FireStore DB 삭제", "User data successfully deleted!")
            _userStatus.value = UiState.Success(user)
        } catch (e: FirebaseAuthException) {
            Log.e("LOGIN-- FAILURE: firebaseAuthWithGoogle", "Firebase 인증에 실패했습니다. ${e.message}")
            _userStatus.value = UiState.Failure(e)
        } catch (e: Exception) {
            Log.e("회원탈퇴 FireStore DB 삭제", "Error deleting document", e)
        }
    }
}
