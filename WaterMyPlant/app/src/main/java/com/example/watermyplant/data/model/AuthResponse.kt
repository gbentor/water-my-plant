package com.example.watermyplant.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String
)

data class TokenResponse(
    val accessToken: String,
    val tokenType: String
)