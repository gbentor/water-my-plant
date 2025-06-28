package com.example.watermyplant.data.api

import com.example.watermyplant.data.model.LoginRequest
import com.example.watermyplant.data.model.RegisterRequest
import com.example.watermyplant.data.model.TokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): TokenResponse
} 