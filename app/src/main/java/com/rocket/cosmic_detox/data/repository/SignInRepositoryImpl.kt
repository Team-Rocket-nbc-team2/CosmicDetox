package com.rocket.cosmic_detox.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.rocket.cosmic_detox.data.model.App
import com.rocket.cosmic_detox.domain.repository.SignInRepository
import com.rocket.cosmic_detox.presentation.view.fragment.mypage.adapter.Trophy
import javax.inject.Inject

class SignInRepositoryImpl @Inject constructor(

) : SignInRepository {
    override fun setDataToFireBase(auth: FirebaseAuth) {
        val firestoreDB = FirebaseFirestore.getInstance()

        val user = auth.currentUser
        val uId = user?.uid.toString()

        val userRef = firestoreDB.collection("users").document(uId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // 기존 유저
                    val userData = document.toObject(User::class.java)
                    // 기존 유저 정보를 사용하여 UI 업데이트 등
                    Log.d("기존 유저 정보 업데이트", "Loaded user data: $userData")
                } else {
                    // 최초 로그인
                    // val newUser = User(uid = uid, name = user.displayName, email = user.email)
                    val firstUser = hashMapOf(
                        "uID" to uId,
                        "name" to user?.displayName.toString(),
                        "dailyTime" to 0L,
                        "totalTime" to 0L,
                        "totalDay" to 0,
                        "isWithdrawn" to false,
                        "trophies" to listOf<Trophy>(),
                        "apps" to listOf<App>(),
                    )

                    userRef.set(firstUser)
                        .addOnSuccessListener {
                            Log.d(
                                "User Data 전송 성공",
                                "User data is successfully written!"
                            )
                        }
                        .addOnFailureListener { exception ->
                            Log.w(
                                "User Data 전송 실패",
                                "Error writing document",
                                exception
                            )
                        }
                }
            }
    }

    override fun googleLogin(idToken: String) {
        Log.d("LOGIN--3", idToken)
        val firestoreDB = FirebaseFirestore.getInstance()

        val credential = GoogleAuthProvider.getCredential(idToken, null)
//        auth.signInWithCredential(credential)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    // 로그인 성공
//                    val user = auth.currentUser
//                    val uId = user?.uid.toString()
//                    val name = user?.displayName.toString()
//                    val dailyTime = 0L
//                    val totalTime = 0L
//                    val totalDay = 0
//                    val isWithdrawn = false
//                    val trophies = listOf<Trophy>()
//                    val apps = listOf<App>()
//
//                    val userRef = firestoreDB.collection("users").document(uId)
//                    val userJson = hashMapOf(
//                        "uID" to uId,
//                        "name" to name,
//                        "dailyTime" to dailyTime,
//                        "totalTime" to totalTime,
//                        "totalDay" to totalDay,
//                        "isWithdrawn" to isWithdrawn,
//                        "trophies" to trophies,
//                        "apps" to apps,
//                    )
//
//                    userRef.set(userJson)
//                        .addOnSuccessListener { Log.d("User Data 전송 성공", "User data is successfully written!") }
//                        .addOnFailureListener { exception -> Log.w("User Data 전송 실패", "Error writing document", exception) }
//
//                    // 여기서 로그인 후 화면 전환 등의 작업을 수행할 수 있습니다.
//                    startActivity(Intent(this, MainActivity::class.java))
//                    finish()
//                } else {
//                    // 로그인 실패
//
//                }
//            }

    }

    override fun kakaoLogin() {
//        TODO("Not yet implemented")
    }

    override fun xLogin() {
//        TODO("Not yet implemented")
    }
}