package com.example.watermyplant.data.model

import com.google.gson.annotations.SerializedName
import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID,
    val username: String,
    @SerializedName("is_active")
    val isActive: Boolean
)

data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String
)

data class Plant(
    val id: UUID,
    val name: String,
    val type: String,
    val description: String?,
    @SerializedName("sensor_id")
    val sensorId: String?,
    @SerializedName("last_watered")
    val lastWatered: Instant?,
    @SerializedName("owner_id")
    val ownerId: UUID,
    @SerializedName("created_at")
    val createdAt: Instant
)

data class PlantCreateRequest(
    val name: String,
    val type: String,
    val description: String? = null,
    @SerializedName("last_watered")
    val lastWatered: Instant
)

data class PlantUpdateRequest(
    val name: String? = null,
    val type: String? = null,
    val description: String? = null,
    @SerializedName("last_watered")
    val lastWatered: Instant? = null,
    @SerializedName("sensor_name")
    val sensorName: String? = null
)

data class WateringEvent(
    val id: UUID,
    @SerializedName("plant_id")
    val plantId: UUID,
    @SerializedName("watered_at")
    val wateredAt: Instant,
    @SerializedName("fertilizer_used")
    val fertilizerUsed: Boolean,
    val notes: String?,
    @SerializedName("created_at")
    val createdAt: Instant
)

data class WateringEventCreateRequest(
    @SerializedName("plant_id")
    val plantId: UUID,
    @SerializedName("watered_at")
    val wateredAt: Instant,
    @SerializedName("fertilizer_used")
    val fertilizerUsed: Boolean,
    val notes: String? = null
)

data class WateringEventUpdateRequest(
    @SerializedName("fertilizer_used")
    val fertilizerUsed: Boolean? = null,
    val notes: String? = null
)

data class ErrorResponse(
    val detail: String
)

data class ValidationErrorResponse(
    val detail: List<ValidationError>
)

data class ValidationError(
    val loc: List<String>,
    val msg: String,
    val type: String
)

data class RegistrationRequest(
    val username: String,
    val password: String
)

data class PlantWithLastWatered(
    val plant: Plant,
    val lastWatered: Instant?,
    val currentMoisture: Double?
)

data class RegisterSensorRequest(
    @SerializedName("sensor_hardware_id")
    val sensorId: String,
    @SerializedName("sensor_name")
    val sensorName: String,
    @SerializedName("board_mac_address")
    val boardMacAddress: String,
    @SerializedName("type")
    val sensorType: String = "moisture"
)

data class RegisterBoardRequest(
    @SerializedName("mac_address")
    val macAddress: String,
    @SerializedName("board_name")
    val boardName: String,
)

data class Sensor(
    @SerializedName("sensor_name")
    val sensorName: String,
    @SerializedName("sensor_id")
    val sensorId: String,
    @SerializedName("type")
    val sensorType: String = "moisture",
    @SerializedName("used_by")
    val usedBy: String = "",
)


data class Board(
    @SerializedName("mac_address")
    val macAddress: String,
    @SerializedName("board_name")
    val boardName: String,
)

data class MoistureData(
    @SerializedName("sensor_id")
    val sensorId: String,
    val moisture: List<Double>,
)