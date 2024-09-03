package com.rocket.cosmic_detox.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rocket.cosmic_detox.domain.repository.SignInRepository
import javax.inject.Inject

class SignInRepositoryImpl @Inject constructor(
    private val firestoreDB: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : SignInRepository {
    override fun setDataToFireBase() {
        val authUser = firebaseAuth.currentUser
        val uId = authUser?.uid.toString()

        val userRef = firestoreDB.collection("users").document(uId)
        val rankingUserRef = firestoreDB.collection("season").document("season-2024-08")

        userRef.get().addOnSuccessListener { document ->
            Log.d("User Data document>>", "${document}")

            if (document.exists()) {
                val userData = document.data
                // 재로그인
                // TODO : 해당 코드 정상작동하는지 검토 필요 (데이터 직접 넣어보며 테스트 요망)
                if (userData != null) {
                    userRef.set(userData)
                        .addOnSuccessListener { Log.d("User Data 업데이트 성공", "User data is successfully updated!!") }
                        .addOnFailureListener { exception -> Log.w("User Data 업데이트 실패", "Error updating document", exception) }
                }
            } else {
                // 최초 로그인 (회원가입)
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
                    .addOnSuccessListener { Log.d("Ranking User Data 전송 성공", "App document written!") }
                    .addOnFailureListener { exception -> Log.w("Ranking User Data 전송 실패", "Error adding app document", exception) }

                userRef.set(firstUser)
                    .addOnSuccessListener { Log.d("User Data 전송 성공", "User data is successfully written!") }
                    .addOnFailureListener { exception -> Log.w("User Data 전송 실패", "Error writing document", exception) }

            }
        }
    }
}