package com.rocket.cosmic_detox.domain.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.rocket.cosmic_detox.data.model.App
import com.rocket.cosmic_detox.presentation.view.fragment.mypage.adapter.Trophy

interface SignInRepository {
    fun setDataToFireBase(auth: FirebaseAuth) {
    }

    fun googleLogin(idToken: String)

    fun kakaoLogin()

    fun xLogin()
}