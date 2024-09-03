package com.rocket.cosmic_detox.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.Trophy
import com.rocket.cosmic_detox.domain.repository.SignInRepository
import javax.inject.Inject

class SignInRepositoryImpl @Inject constructor(
    private val firestoreDB: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : SignInRepository {
    override fun setDataToFireBase() {
        val authUser = firebaseAuth.currentUser
        val uId = authUser?.uid.toString()

        Log.d("User Data uId>>", "${uId}: 이게 uId야!!!!!!")

        val userRef = firestoreDB.collection("users").document(uId)
        val rankingUserRef = firestoreDB.collection("season").document("season-2024-08")

        userRef.get().addOnSuccessListener { document ->
            Log.d("User Data document>>", "${document}")

            if (document.exists()) {
                Log.d("User Data 존재", "다큐먼트가 있어!!!!")
                val userData = document.data
                // 재로그인
                // TODO : 해당 코드 정상작동하는지 검토 필요 (데이터 직접 넣어보며 테스트 요망)
                if (userData != null) {
                    userRef.set(userData)
                        .addOnSuccessListener { Log.d("User Data 업데이트 성공", "User data is successfully updated!!") }
                        .addOnFailureListener { exception -> Log.w("User Data 업데이트 실패", "Error updating document", exception) }
                }
            } else {
                Log.d("User Data 존재하지 않음", "다큐먼트가 없어!!!!")
                // 최초 로그인 (회원가입)
                val trophies = listOf<Trophy>()
                val apps = listOf<AllowedApp>()

                // TODO : User DataClass 생성 되면 DataClass 맞춰서 생성할 것
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
                    .addOnSuccessListener { Log.d("User Data의 Apps 전송 성공", "App document written!") }
                    .addOnFailureListener { exception -> Log.w("Firestore", "Error adding app document", exception) }

                userRef.set(firstUser)
                    .addOnSuccessListener { Log.d("User Data 전송 성공", "User data is successfully written!") }
                    .addOnFailureListener { exception -> Log.w("User Data 전송 실패", "Error writing document", exception) }

            }
        }
    }
}