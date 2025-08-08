package com.example.watermyplant.data.repository

import com.example.watermyplant.data.model.*
import com.example.watermyplant.network.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlantRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getAllPlants(): Flow<Result<List<Plant>>> = flow {
        try {
            val response = apiService.getAllPlants()
            if (response.isSuccessful) {
                response.body()?.let { plants ->
                    emit(Result.success(plants))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getPlantById(plantId: String): Flow<Result<Plant>> = flow {
        try {
            val response = apiService.getPlantById(plantId)
            if (response.isSuccessful) {
                response.body()?.let { plant ->
                    emit(Result.success(plant))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun createPlant(request: PlantCreateRequest): Flow<Result<Plant>> = flow {
        try {
            val response = apiService.createPlant(request)
            if (response.isSuccessful) {
                response.body()?.let { plant ->
                    emit(Result.success(plant))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun updatePlant(plantId: String, request: PlantUpdateRequest): Flow<Result<Plant>> = flow {
        try {
            val response = apiService.updatePlant(plantId, request)
            if (response.isSuccessful) {
                response.body()?.let { plant ->
                    emit(Result.success(plant))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun deletePlant(plantId: String): Flow<Result<Unit>> = flow {
        try {
            val response = apiService.deletePlant(plantId)
            if (response.isSuccessful) {
                emit(Result.success(Unit))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun waterPlant(plantId: UUID): Flow<Result<Plant>> = flow {
        try {
            val request = PlantUpdateRequest(
                lastWatered = Instant.now()
            )
            val response = apiService.updatePlant(plantId.toString(), request)
            if (response.isSuccessful) {
                response.body()?.let { plant ->
                    emit(Result.success(plant))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getPlantWateringHistory(plantId: String): Flow<Result<List<WateringEvent>>> = flow {
        try {
            val response = apiService.getPlantWateringHistory(plantId)
            if (response.isSuccessful) {
                response.body()?.let { events ->
                    emit(Result.success(events))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getLastWateringEvent(plantId: String): Flow<Result<WateringEvent?>> = flow {
        try {
            val response = apiService.getLastWateringEvent(plantId)
            when {
                response.isSuccessful -> {
                    response.body()?.let { event ->
                        emit(Result.success(event))
                    } ?: emit(Result.success(null))
                }
                response.code() == 204 -> {
                    // Server explicitly returned 'No Content' — treat as a valid null response
                    emit(Result.success(null))
                }
                else -> {
                    emit(Result.failure(Exception("Error: ${response.code()}")))
                }
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun recordWateringEvent(request: WateringEventCreateRequest): Flow<Result<WateringEvent>> = flow {
        try {
            val response = apiService.recordWateringEvent(request)
            if (response.isSuccessful) {
                response.body()?.let { event ->
                    emit(Result.success(event))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Error: \\${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun updateWateringEvent(eventId: String, request: WateringEventUpdateRequest): Flow<Result<WateringEvent>> = flow {
        try {
            val response = apiService.updateWateringEvent(eventId, request)
            if (response.isSuccessful) {
                response.body()?.let { event ->
                    emit(Result.success(event))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun deleteWateringEvent(eventId: String): Flow<Result<Unit>> = flow {
        try {
            val response = apiService.deleteWateringEvent(eventId)
            if (response.isSuccessful) {
                emit(Result.success(Unit))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
} 