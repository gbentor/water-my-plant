package com.example.watermyplant.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watermyplant.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginState {
    object Initial : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            authRepository.login(username, password).collect { result ->
                _loginState.value = when {
                    result.isSuccess -> LoginState.Success
                    result.isFailure -> {
                        val message = result.exceptionOrNull()?.message
                        if (message?.contains("401") == true || message?.contains("400") == true) {
                            LoginState.Error("Incorrect username or password")
                        } else {
                            LoginState.Error(message ?: "An error occurred")
                        }
                    }
                    else -> LoginState.Error("Unknown error occurred")
                }
            }
        }
    }
} 