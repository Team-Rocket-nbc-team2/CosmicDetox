package com.rocket.cosmic_detox.util

import com.google.firebase.auth.FirebaseAuth

object Authentication {

    val currentUser = FirebaseAuth.getInstance().currentUser

    fun getAuthPlatform(): String {
        return currentUser?.providerData?.get(1)?.providerId.toString()
    }
}