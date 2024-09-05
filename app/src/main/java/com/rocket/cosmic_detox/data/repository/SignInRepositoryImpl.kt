package com.rocket.cosmic_detox.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rocket.cosmic_detox.domain.repository.SignInRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SignInRepositoryImpl @Inject constructor(
    private val firestoreDB: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : SignInRepository {
    override suspend fun setDataToFireBase(): Result<Boolean> {
        val authUser = firebaseAuth.currentUser
        val uId = authUser?.uid.toString()

        Log.d("User Data uId>>", "${uId}: 이게 uId야!!!!!!")

        val userRef = firestoreDB.collection("users").document(uId)
        val rankingUserRef = firestoreDB.collection("season").document("season-2024-08")

        return try {
            val document = userRef.get().await() // 유저 문서를 비동기 작업이 완료될 때까지 기다림

            if (document.exists()) {
                Log.d("User Data 존재", "다큐먼트가 있어!!!!")
                val userData = document.data

                // 재로그인
                if (userData != null) {
                    userRef.set(userData).await() // 마찬가지로 비동기 작업이 완료될 때까지 기다림
                    Log.d("User Data 업데이트 성공", "User data is successfully updated!!")
                }
            } else {
                Log.d("User Data 존재하지 않음", "다큐먼트가 없어!!!!")

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

                rankingUserRef.collection("ranking").document(uId).set(firstRankingUser).await()
                Log.d("Ranking User Data 전송 성공", "Ranking user data is successfully written!")

                userRef.set(firstUser).await()
                Log.d("User Data 전송 성공", "User data is successfully written!")
            }

            Result.success(true) // 성공적으로 작업을 마쳤을 때 Result.success()로 성공 결과를 반환
        } catch (e: Exception) {
            Log.e("Firebase Error", "Error: ", e)
            Result.failure(e) // 작업 중 예외가 발생했을 때 Result.failure()로 예외 결과를 반환
        }
    }
}