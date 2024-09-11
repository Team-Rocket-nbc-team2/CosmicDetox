package com.rocket.cosmic_detox.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object Authentication {

    var currentUser: FirebaseUser? = null // TODO: 나중에 로컬 DB에 저장하는 방식으로 변경
        private set

    fun setCurrentUser(user: FirebaseUser) {
        currentUser = user
    }
}