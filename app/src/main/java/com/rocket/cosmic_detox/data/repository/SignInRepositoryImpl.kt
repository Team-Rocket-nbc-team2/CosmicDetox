package com.rocket.cosmic_detox.data.repository

import com.rocket.cosmic_detox.domain.repository.SignInRepository
import javax.inject.Inject

class SignInRepositoryImpl @Inject constructor(

): SignInRepository {
    override fun googleLogin() {

    }

    override fun kakaoLogin() {
//        TODO("Not yet implemented")
    }

    override fun xLogin() {
//        TODO("Not yet implemented")
    }
}