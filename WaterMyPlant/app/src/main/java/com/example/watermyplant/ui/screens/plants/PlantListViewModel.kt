package com.example.watermyplant.ui.screens.plants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watermyplant.data.model.Plant
import com.example.watermyplant.data.model.PlantWithLastWatered
import com.example.watermyplant.data.repository.AuthRepository
import com.example.watermyplant.data.repository.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

sealed class PlantListUiState {
    object Loading : PlantListUiState()
    data class Success(val plants: List<PlantWithLastWatered>) : PlantListUiState()
    object Error : PlantListUiState()
}

@HiltViewModel
class PlantListViewModel @Inject constructor(
    private val plantRepository: PlantRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlantListUiState>(PlantListUiState.Loading)
    val uiState: StateFlow<PlantListUiState> = _uiState

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadPlants() {
        viewModelScope.launch {
            _uiState.value = PlantListUiState.Loading
            _error.value = null
            try {
                plantRepository.getAllPlants().collect { result ->
                    result.onSuccess { plants ->
                        val plantsWithLastWatered = mutableListOf<PlantWithLastWatered>()
                        plants.forEach { plant ->
                            plantRepository.getLastWateringEvent(plant.id.toString()).collect { eventResult ->
                                eventResult.onSuccess { event ->
                                    val lastWatered = event?.wateredAt ?: plant.lastWatered ?: Instant.EPOCH
                                    plantsWithLastWatered.add(PlantWithLastWatered(plant, lastWatered))
                                }.onFailure { exception ->
                                    // If no watering event, use plant's lastWatered
                                    plantsWithLastWatered.add(PlantWithLastWatered(plant, plant.lastWatered))
                                }
                            }
                        }
                        _uiState.value = PlantListUiState.Success(plantsWithLastWatered)
                    }.onFailure { exception ->
                        _uiState.value = PlantListUiState.Error
                        _error.value = exception.message ?: "Failed to load plants"
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PlantListUiState.Error
                _error.value = e.message ?: "An unexpected error occurred"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun clearError() {
        _error.value = null
    }
} 