package com.rocket.cosmic_detox.domain.repository

import com.google.firebase.auth.FirebaseAuth

interface SignInRepository {
    fun setDataToFireBase()
}