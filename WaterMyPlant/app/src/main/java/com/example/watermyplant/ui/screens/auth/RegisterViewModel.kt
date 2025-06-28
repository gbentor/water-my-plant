package com.example.watermyplant.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watermyplant.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RegisterState {
    object Initial : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Initial)
    val registerState: StateFlow<RegisterState> = _registerState

    fun register(username: String, password: String, confirmPassword: String) {
        when {
            username.isBlank() -> {
                _registerState.value = RegisterState.Error("Username cannot be empty")
                return
            }
            password.isBlank() -> {
                _registerState.value = RegisterState.Error("Password cannot be empty")
                return
            }
            password.length < 8 -> {
                _registerState.value = RegisterState.Error("Password must be at least 8 characters")
                return
            }
            password != confirmPassword -> {
                _registerState.value = RegisterState.Error("Passwords do not match")
                return
            }
        }

        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            authRepository.register(username, password).collect { result ->
                _registerState.value = when {
                    result.isSuccess -> RegisterState.Success
                    result.isFailure -> RegisterState.Error(
                        result.exceptionOrNull()?.message ?: "An error occurred"
                    )
                    else -> RegisterState.Error("Unknown error occurred")
                }
            }
        }
    }
} 