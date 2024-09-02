package com.rocket.cosmic_detox.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.rocket.cosmic_detox.data.model.App
import com.rocket.cosmic_detox.domain.repository.SignInRepository
import com.rocket.cosmic_detox.presentation.view.fragment.mypage.adapter.Trophy
import javax.inject.Inject

class SignInRepositoryImpl @Inject constructor(
    private val firestoreDB: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : SignInRepository {
    override fun setDataToFireBase() {
        val user = firebaseAuth.currentUser
        val uId = user?.uid.toString()

        val userRef = firestoreDB.collection("users").document(uId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // 기존 유저
//                val userData = document.toObject(User::class.java)
                Log.d("기존 유저 정보 업데이트", "Loaded user data userData ${document}")
            } else {
                // 최초 로그인
                // TODO : User DataClass 생성 되면 DataClass 맞춰서 생성할 것
                // ex) val firstUser = User(uid = uid, name = user.displayName, email = user.email)
                val firstUser = hashMapOf(
                    "uID" to uId,
                    "name" to user?.displayName.toString(),
                    "dailyTime" to 0L,
                    "totalTime" to 0L,
                    "totalDay" to 0,
                    "isWithdrawn" to false,
                    // TODO: 아래 collection에 정보 넣는 거 하기
                    "trophies" to listOf<Trophy>(),
                    "apps" to listOf<App>(),
                )

                userRef.set(firstUser)
                    .addOnSuccessListener { Log.d("User Data 전송 성공", "User data is successfully written!") }
                    .addOnFailureListener { exception -> Log.w("User Data 전송 실패", "Error writing document", exception) }
            }
        }
    }
}