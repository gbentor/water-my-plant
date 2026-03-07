package com.example.watermyplant.ui.screens.plants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watermyplant.data.model.PlantWithLastWatered
import com.example.watermyplant.data.repository.AuthRepository
import com.example.watermyplant.data.repository.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
            try {
                plantRepository.getAllPlants().collect { result ->
                    result.onSuccess { plants ->
                        coroutineScope {
                            // Launch async requests in parallel for each plant
                            val deferredResults = plants.map { plant ->
                                async {
                                    val eventResult =
                                        plantRepository.getLastWateringEvent(plant.id.toString())
                                            .first()
                                    val lastWatered =
                                        eventResult.getOrNull()?.wateredAt ?: plant.lastWatered
                                    PlantWithLastWatered(
                                        plant = plant,
                                        lastWatered = lastWatered
                                    )
                                }
                            }

                            // Await all parallel jobs and build the final list
                            val plantsWithLastWatered = deferredResults.awaitAll()

                            _uiState.value = PlantListUiState.Success(plantsWithLastWatered)
                        }
                    }.onFailure { exception ->
                        _uiState.value = PlantListUiState.Error
                        _error.value = exception.message ?: "Failed to load plants"
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PlantListUiState.Error
                _error.value = e.message ?: "Unknown error"
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