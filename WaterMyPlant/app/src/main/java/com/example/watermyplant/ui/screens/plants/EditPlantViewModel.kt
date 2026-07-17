package com.example.watermyplant.ui.screens.plants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watermyplant.data.model.Plant
import com.example.watermyplant.data.model.PlantUpdateRequest
import com.example.watermyplant.data.model.Sensor
import com.example.watermyplant.data.repository.PlantRepository
import com.example.watermyplant.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditPlantViewModel @Inject constructor(
    private val plantRepository: PlantRepository,
    private val sensorRepository: SensorRepository
) : ViewModel() {

    private val _plant = MutableStateFlow<Plant?>(null)
    val plant: StateFlow<Plant?> = _plant

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _editSuccess = MutableStateFlow(false)
    val editSuccess: StateFlow<Boolean> = _editSuccess

    private val _sensors = MutableStateFlow<List<Sensor>>(emptyList())
    val sensors: StateFlow<List<Sensor>> = _sensors

    fun loadPlant(plantId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
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
                sensorRepository.getAllSensors().collect { result ->
                    result.fold(
                        onSuccess = { sensorList ->
                            // 1. Create your "Remove" item
                            val removeAction = Sensor(sensorName = "Remove Sensor", sensorId = "remove_sensor", sensorType="remove")

                            // 2. Create a new list combining the two
                            _sensors.value = listOf(removeAction) + sensorList
                        },
                        onFailure = { e ->
                            _error.value = e.message ?: "Failed to load sensors"
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

    fun updatePlant(
        plantId: String,
        name: String,
        type: String,
        description: String?,
        sensorName: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val request = PlantUpdateRequest(
                    name = name,
                    type = type,
                    description = description,
                    sensorName = sensorName
                )
                plantRepository.updatePlant(plantId, request)
                    .collect { result ->
                        result.fold(
                            onSuccess = {
                                _editSuccess.value = true
                            },
                            onFailure = { e ->
                                _error.value = e.message ?: "Failed to update plant"
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