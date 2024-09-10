package com.rocket.cosmic_detox.presentation.view.fragment.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.AppUsage
import com.rocket.cosmic_detox.data.model.User
import com.rocket.cosmic_detox.domain.repository.MyPageRepository
import com.rocket.cosmic_detox.presentation.uistate.MyPageUiState
import com.rocket.cosmic_detox.presentation.uistate.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
            val uid = repository.getUid()
            // getUserInfo, getUserApps, getUserTrophies를 zip으로 묶어서 한 번에 처리
            repository.getUserInfo(uid)
                .zip(repository.getUserApps(uid)) { user, apps ->
                    user to apps // User와 AllowedApp을 함께 반환
                }
                .zip(repository.getUserTrophies(uid)) { (user, apps), trophies ->
                    return@zip user.copy(apps = apps, trophies = trophies) // 세 결과를 조합하여 User 객체 생성, 이렇게 하면 되나..?
                }
                .flowOn(Dispatchers.IO)
                .catch { exception ->
                    Log.e("MyPageViewModel", "loadMyInfo: $exception")
                    _myInfo.value = MyPageUiState.Error(exception.toString())
                }
                .collect { myInfo ->
                    Log.d("MyPageViewModel", "User: $myInfo")
                    _myInfo.value = MyPageUiState.Success(myInfo)
                }
        }
    }

    fun loadMyAppUsage() {
        viewModelScope.launch {
            repository.getMyAppUsage()
                .flowOn(Dispatchers.IO)
                .catch { exception ->
                    _myAppUsageList.value = MyPageUiState.Error(exception.toString())
                    Log.e("MyPageViewModel", "loadMyAppUsage: $exception")
                }
                .collect { apps ->
                    _myAppUsageList.value = MyPageUiState.Success(apps)
                }
        }
    }

    fun setAppUsageLimit(allowedApp: AllowedApp, hour: String, minute: String) {
        viewModelScope.launch {
            //val limitedTime = (hour.toInt() * 60 + minute.toInt()) * 60 * 1000L // 시간과 분을 밀리초로 변환
            val limitedTime = (hour.toInt() * 60 + minute.toInt()) * 60L // 시간과 분을 초로 변환

            repository.updateAppUsageLimit(allowedApp.copy(limitedTime = limitedTime))
                .onSuccess {
                    // TODO: 나중에 UiState로 변경해보기
                    _updateResult.value = true
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

        // trophies와 apps 서브컬렉션 참조
        val trophiesRef = userRef.collection("trophies")
        val appsRef = userRef.collection("apps")

        try {
            // 두 서브컬렉션을 동시에 삭제
            awaitAll(
                async { deleteSubCollections(trophiesRef) },
                async { deleteSubCollections(appsRef) }
            )

            // 메인 문서 삭제
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
            _userStatus.value = UiState.Failure(e)
        }
    }

    // 서브컬렉션을 삭제하는 함수
    private suspend fun deleteSubCollections(subCollectionRef: CollectionReference) {
        try {
            // 서브컬렉션 내 모든 문서를 가져와서 삭제
            val documents = subCollectionRef.get().await()

            for (doc in documents) {
                // 서브컬렉션 내 문서를 삭제 (필요시 서브컬렉션 내의 서브컬렉션도 삭제 가능)
                doc.reference.delete().await()
            }

            Log.d("서브컬렉션 삭제", "Subcollection data successfully deleted!")
        } catch (e: Exception) {
            Log.e("서브컬렉션 삭제 실패", "Error deleting subcollection", e)
            throw e // 실패 시 예외 발생 -> 위에 withdrawUserCoroutine에서 catch
        }
    }
}
