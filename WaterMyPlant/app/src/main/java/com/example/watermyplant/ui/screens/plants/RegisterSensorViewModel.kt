package com.example.watermyplant.ui.screens.plants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watermyplant.data.model.Board
import com.example.watermyplant.data.model.RegisterBoardRequest
import com.example.watermyplant.data.model.RegisterSensorRequest
import com.example.watermyplant.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterSensorViewModel @Inject constructor(
    private val sensorRepository: SensorRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _boardError = MutableStateFlow<String?>(null)
    val boardError: StateFlow<String?> = _boardError

    private val _addSuccess = MutableStateFlow(false)
    val addSuccess: StateFlow<Boolean> = _addSuccess

    private val _isGettingBoards = MutableStateFlow(true)
    val isGettingBoards: StateFlow<Boolean> = _isGettingBoards
    private val _boardsOptions = MutableStateFlow<List<Board>>(emptyList())
    val boardsOptions: StateFlow<List<Board>> = _boardsOptions

    fun registerSensor(
        name: String,
        sensorId: String,
        type: String,
        boardMacAddress: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val request = RegisterSensorRequest(
                    sensorId = sensorId,
                    sensorName = name,
                    sensorType = type,
                    boardMacAddress= boardMacAddress
                )
                sensorRepository.registerSensor(request)
                    .collect { result ->
                        result.fold(
                            onSuccess = {
                                _addSuccess.value = true
                            },
                            onFailure = { e ->
                                _error.value = e.message ?: "Failed to register sensor"
                            }
                        )
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun registerBoard(
        macAddress: String,
        boardName: String,
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _boardError.value = null

            // Validate MAC address
            if (!isValidMacAddress(macAddress)) {
                _boardError.value = "Invalid MAC address format"
                _isLoading.value = false
                return@launch
            }

            try {
                val request = RegisterBoardRequest(
                    macAddress = macAddress,
                    boardName = boardName
                )
                sensorRepository.registerBoard(request)
                    .collect { result ->
                        result.fold(
                            onSuccess = {
                                _addSuccess.value = true
                            },
                            onFailure = { e ->
                                _boardError.value = e.message ?: "Failed to register board"
                            }
                        )
                    }
            } catch (e: Exception) {
                _boardError.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getRegisteredBoards() {
        viewModelScope.launch {
            _isLoading.value = true
            _boardError.value = null
            try {
                sensorRepository.getRegisteredBoards().collect { result ->
                    result.fold(
                        onSuccess = { sensorList ->
                            val list = sensorList.ifEmpty {
                                listOf(Board("null", "A board must be registered first!"))
                            }
                            _boardsOptions.value = list
                        },
                        onFailure = { e ->
                            _boardError.value = e.message ?: "Failed to load boards"
                        }
                    )
                }
            } catch (e: Exception) {
                _boardError.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
                _isGettingBoards.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    private fun isValidMacAddress(macAddress: String): Boolean {
        // Matches formats like: AA:BB:CC:DD:EE:FF or AA-BB-CC-DD-EE-FF
        val macRegex = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$".toRegex()
        return macRegex.matches(macAddress)
    }
} 