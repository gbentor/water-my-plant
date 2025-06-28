package com.example.watermyplant.data.repository

import com.example.watermyplant.data.TokenManager
import com.example.watermyplant.data.model.AuthResponse
import com.example.watermyplant.data.model.User
import com.example.watermyplant.data.model.RegistrationRequest
import com.example.watermyplant.network.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    val isAuthenticated: Flow<Boolean> = tokenManager.getTokenFlow().map { it != null }

    fun register(username: String, password: String): Flow<Result<User>> = flow {
        try {
            val response = apiService.register(mapOf(
                "username" to username,
                "password" to password
            ))
            if (response.isSuccessful) {
                response.body()?.let { user ->
                    // After successful registration, automatically log in
                    val loginResponse = apiService.login(username, password)
                    if (loginResponse.isSuccessful) {
                        loginResponse.body()?.let { authResponse ->
                            tokenManager.saveToken(authResponse.accessToken)
                            emit(Result.success(user))
                        } ?: emit(Result.failure(Exception("Empty login response body")))
                    } else {
                        emit(Result.failure(Exception("Failed to login after registration")))
                    }
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun login(username: String, password: String): Flow<Result<AuthResponse>> = flow {
        try {
            val response = apiService.login(username, password)
            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    tokenManager.saveToken(authResponse.accessToken)
                    emit(Result.success(authResponse))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getCurrentUser(): Flow<Result<User>> = flow {
        try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful) {
                response.body()?.let { user ->
                    emit(Result.success(user))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun logout() {
        tokenManager.clearToken()
    }
} 