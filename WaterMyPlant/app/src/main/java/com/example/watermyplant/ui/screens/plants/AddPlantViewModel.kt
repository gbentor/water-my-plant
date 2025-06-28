package com.example.watermyplant.ui.screens.plants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watermyplant.data.model.PlantCreateRequest
import com.example.watermyplant.data.repository.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class AddPlantViewModel @Inject constructor(
    private val plantRepository: PlantRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _addSuccess = MutableStateFlow(false)
    val addSuccess: StateFlow<Boolean> = _addSuccess

    fun addPlant(
        name: String,
        type: String,
        description: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val request = PlantCreateRequest(
                    name = name,
                    type = type,
                    description = description,
                    lastWatered = Instant.now()
                )
                plantRepository.createPlant(request)
                    .collect { result ->
                        result.fold(
                            onSuccess = {
                                _addSuccess.value = true
                            },
                            onFailure = { e ->
                                _error.value = e.message ?: "Failed to add plant"
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

    fun clearError() {
        _error.value = null
    }
} 