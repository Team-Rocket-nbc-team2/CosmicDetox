package com.rocket.cosmic_detox.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rocket.cosmic_detox.domain.repository.SignInRepository
import javax.inject.Inject

enum class LoginType {
    RE_SIGN_IN,
    SIGN_UP,
    NONE
}

//TODO UseCase Refactoring
class SignInRepositoryImpl @Inject constructor(
    private val firestoreDB: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : SignInRepository {
    private var isReSignInUser = false
    private var isSignUpUser = false
    private var isRankingUser = false

    private var loginType = LoginType.NONE

    override fun setDataToFireBase(): Boolean {
        val authUser = firebaseAuth.currentUser
        val uId = authUser?.uid.toString()

        Log.d("User Data uId>>", "${uId}: 이게 uId야!!!!!!")

        val userRef = firestoreDB.collection("users").document(uId)
        val rankingUserRef = firestoreDB.collection("season").document("season-2024-08")

        userRef.get().addOnFailureListener {
            Log.d("debug2323", it.toString())
        }

        userRef.get().addOnSuccessListener { document -> //컬렉션에 로그인한 유저있음
            var userUId : String? = null
            userUId = document.getString("uID")
            Log.d("User Data document>>", "${document.getString("uID")}")
            if (userUId != null) {
                loginType = LoginType.RE_SIGN_IN
                // 재로그인
                Log.d("User Data 존재", "다큐먼트가 있어!!!!")
                val userData = document.data
                if (userData != null) {
                    userRef.set(userData)
                        .addOnSuccessListener {
                            // 1
                            isReSignInUser = true
                            Log.d("User Data 업데이트 성공", "User data is successfully updated!!")
                        }
                        .addOnFailureListener { exception ->
                            isReSignInUser = false
                            Log.w("User Data 업데이트 실패", "Error updating document", exception)
                        }
                }
            } else {
                loginType = LoginType.SIGN_UP

                // 최초 로그인 (회원가입)
                Log.d("User Data 존재하지 않음", "다큐먼트가 없어!!!!")
                val firstUser = hashMapOf(
                    "uID" to uId,
                    "name" to authUser?.displayName.toString(),
                    "dailyTime" to 0L,
                    "totalTime" to 0L,
                    "totalDay" to 0,
                    "isWithdrawn" to false,
                )

                val firstRankingUser = hashMapOf(
                    "uid" to uId,
                    "name" to authUser?.displayName.toString(),
                    "point" to 0,
                    "totalTime" to 0,
                )

                rankingUserRef.collection("ranking").document(uId).set(firstRankingUser)
                    .addOnSuccessListener {
                        // 2
                        isSignUpUser = true
                        Log.d("Ranking User Data 전송 성공", "App document written!")
                    }
                    .addOnFailureListener { exception ->
                        isSignUpUser = false
                        Log.w("Ranking User Data 전송 실패", "Error adding app document", exception)
                    }


                userRef.set(firstUser)
                    .addOnSuccessListener {
                        // 3
                        isRankingUser = true
                        Log.d("User Data 전송 성공", "User data is successfully written!")
                    }
                    .addOnFailureListener { exception ->
                        isRankingUser = false
                        Log.w("User Data 전송 실패", "Error writing document", exception) }
            }
        }

        return when(loginType) {
            LoginType.RE_SIGN_IN -> isReSignInUser
            LoginType.SIGN_UP -> isSignUpUser && isRankingUser
            LoginType.NONE -> false
        }
    }
}