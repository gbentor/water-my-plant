package com.example.watermyplant.ui.screens.plants
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watermyplant.data.model.MoistureData
import com.example.watermyplant.data.model.Plant
import com.example.watermyplant.data.model.Sensor
import com.example.watermyplant.data.model.WateringEvent
import com.example.watermyplant.data.model.WateringEventCreateRequest
import com.example.watermyplant.data.model.WateringEventUpdateRequest
import com.example.watermyplant.data.repository.PlantRepository
import com.example.watermyplant.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

sealed class PlantDetailUiState {
    object Loading : PlantDetailUiState()
    data class Success(
        val plant: Plant,
        val wateringHistory: List<WateringEvent>
    ) : PlantDetailUiState()
    object Error : PlantDetailUiState()
}

@HiltViewModel
class PlantDetailViewModel @Inject constructor(
    private val plantRepository: PlantRepository, private val sensorRepository: SensorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlantDetailUiState>(PlantDetailUiState.Loading)
    val uiState: StateFlow<PlantDetailUiState> = _uiState

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess

    private val _plant = MutableStateFlow<Plant?>(null)
    val plant: StateFlow<Plant?> = _plant

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _waterSuccess = MutableStateFlow(false)
    val waterSuccess: StateFlow<Boolean> = _waterSuccess

    private val _wateringEvents = MutableStateFlow<List<WateringEvent>>(emptyList())
    val wateringEvents: StateFlow<List<WateringEvent>> = _wateringEvents

    @OptIn(ExperimentalCoroutinesApi::class)
    val sensor: StateFlow<Sensor?> = _plant
        .flatMapLatest { plant ->
            val sensorId = plant?.sensorId
            if (sensorId == null) {
                flowOf(null) // If no plant or no sensorId, emit null sensor
            } else {
                // Fetch the sensor. Since getSensorById returns a Flow<Result<Sensor>>,
                // we map it to extract the actual Sensor object.
                sensorRepository.getSensorById(sensorId).map { result ->
                    result.getOrNull()
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val moistureData: StateFlow<MoistureData?> = _plant
        .flatMapLatest { plant ->
            val sensorId = plant?.sensorId
            if (sensorId == null) {
                flowOf(null)
            } else {
                sensorRepository.getSensorMoistureData(sensorId).map { result ->
                    result.getOrNull()
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun loadPlant(plantId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                plantRepository.getPlantById(plantId).collect { result ->
                    result.fold(
                        onSuccess = { _plant.value = it },
                        onFailure = { _error.value = it.message ?: "Failed to load plant" }
                    )
                }

                refreshWateringEvents(plantId)
            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun waterPlant(plantId: String, fertilizerUsed: Boolean, notes: String?) {
        recordWatering(plantId, fertilizerUsed, notes)
    }

    fun recordWatering(plantId: String, fertilizerUsed: Boolean, notes: String?) {
        viewModelScope.launch {
            val now = Instant.now()
            val request = WateringEventCreateRequest(
                plantId = UUID.fromString(plantId),
                wateredAt = now,
                fertilizerUsed = fertilizerUsed,
                notes = notes
            )

            plantRepository.recordWateringEvent(request)
                .catch { e ->
                    _error.value = e.message
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { event ->
                            // Reload plant and watering events so the UI updates
                            viewModelScope.launch {
                                plantRepository.getPlantById(plantId).collect { result ->
                                    result.fold(
                                        onSuccess = { plant ->
                                            _plant.value = plant
                                        },
                                        onFailure = { e ->
                                            _error.value = e.message ?: "Failed to load plant"
                                        }
                                    )
                                }
                                plantRepository.getPlantWateringHistory(plantId).collect { result ->
                                    result.fold(
                                        onSuccess = { events ->
                                            _wateringEvents.value = events
                                        },
                                        onFailure = { e ->
                                            _error.value = e.message ?: "Failed to load watering events"
                                        }
                                    )
                                }
                            }
                        },
                        onFailure = { e ->
                            _error.value = e.message
                        }
                    )
                }
        }
    }

    fun deletePlant(plantId: String) {
        viewModelScope.launch {
            plantRepository.deletePlant(plantId)
                .catch { e ->
                    _error.value = e.message
                }
                .collect { result ->
                    result.fold(
                        onSuccess = {
                            _deleteSuccess.value = true
                        },
                        onFailure = { e ->
                            _error.value = e.message
                        }
                    )
                }
        }
    }

    fun editWateringEvent(eventId: String, fertilizerUsed: Boolean?, notes: String?) {
        viewModelScope.launch {
            val request = WateringEventUpdateRequest(
                fertilizerUsed = fertilizerUsed,
                notes = notes
            )
            plantRepository.updateWateringEvent(eventId, request)
                .catch { e -> _error.value = e.message }
                .collect { result ->
                    result.fold(
                        onSuccess = {
                            // Refresh watering events
                            _plant.value?.id?.toString()?.let { plantId ->
                                refreshWateringEvents(plantId)
                            }
                        },
                        onFailure = { e ->
                            _error.value = e.message
                        }
                    )
                }
        }
    }

    fun deleteWateringEvent(eventId: String) {
        viewModelScope.launch {
            plantRepository.deleteWateringEvent(eventId)
                .catch { e -> _error.value = e.message }
                .collect { result ->
                    result.fold(
                        onSuccess = {
                            // Refresh watering events
                            _plant.value?.id?.toString()?.let { plantId ->
                                refreshWateringEvents(plantId)
                            }
                        },
                        onFailure = { e ->
                            _error.value = e.message
                        }
                    )
                }
        }
    }

    private fun refreshWateringEvents(plantId: String) {
        viewModelScope.launch {
            plantRepository.getPlantWateringHistory(plantId).collect { result ->
                result.fold(
                    onSuccess = { events ->
                        _wateringEvents.value = events
                    },
                    onFailure = { e ->
                        _error.value = e.message ?: "Failed to load watering events"
                    }
                )
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearWaterSuccess() {
        _waterSuccess.value = false
    }
} 