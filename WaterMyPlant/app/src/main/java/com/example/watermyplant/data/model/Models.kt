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
    val lastWatered: Instant? = null
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
    val lastWatered: Instant?
) 