package com.rocket.cosmic_detox.domain.repository

interface SignInRepository {
    suspend fun setDataToFireBase(): Result<Boolean>
}