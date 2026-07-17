package com.example.watermyplant.data.repository

import com.example.watermyplant.data.model.*
import com.example.watermyplant.network.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getAllSensors(): Flow<Result<List<Sensor>>> = flow {
        try {
            val response = apiService.getAllAvailableSensors()
            if (response.isSuccessful) {
                response.body()?.let { sensors ->
                    emit(Result.success(sensors))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getSensorById(sensorId: String): Flow<Result<Sensor>> = flow {
        try {
            val response = apiService.getSensorById(sensorId)
            if (response.isSuccessful) {
                response.body()?.let { sensor ->
                    emit(Result.success(sensor))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getSensorMoistureData(sensorId: String): Flow<Result<MoistureData>> = flow {
        try {
            val response = apiService.getSensorMoistureData(sensorId)
            if (response.isSuccessful) {
                response.body()?.let { data ->
                    emit(Result.success(data))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun registerSensor(request: RegisterSensorRequest): Flow<Result<Unit>> = flow {
        try {
            val response = apiService.registerSensorEvent(request)
            if (response.isSuccessful) {
                emit(Result.success(Unit))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun registerBoard(request: RegisterBoardRequest): Flow<Result<Unit>> = flow {
        try {
            val response = apiService.registerBoardEvent(request)
            if (response.isSuccessful) {
                emit(Result.success(Unit))
            } else {
                // Extract error message from response
                val errorMessage = try {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        errorResponse.message
                    } else {
                        "Unknown error occurred"
                    }
                } catch (e: Exception) {
                    "Error: ${response.code()} - ${response.message()}"
                }
                emit(Result.failure(Exception(errorMessage)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getRegisteredBoards(): Flow<Result<List<Board>>> = flow {
        try {
            val response = apiService.getAllAvailableBoards()
            if (response.isSuccessful) {
                response.body()?.let { boards ->
                    emit(Result.success(boards))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}

// Add this data class (make sure it's defined at the class level, not inside a function)
data class ErrorResponse(
    val message: String
)