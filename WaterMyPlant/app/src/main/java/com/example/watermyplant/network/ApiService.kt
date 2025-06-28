package com.example.watermyplant.network

import com.example.watermyplant.data.model.*
import retrofit2.Response
import retrofit2.http.*

// You might have already created these data classes in your model package
// If so, ensure the imports above point to them correctly.

// For PlantRequestBody, UpdatePlantRequestBody, WateringEventRequestBody, TokenResponse, User, Plant, WateringEvent
// make sure they are correctly defined in your data.model package and imported here.
interface ApiService {

    companion object {
        const val BASE_URL =
            "http:/localhost:8000" // Replace with your actual deployed backend URL
        // Example: "http://10.0.2.2:8000/" if running backend locally and testing on Android Emulator
        // Example: "https://your-app-name.onrender.com/" if deployed
    }

    // --- Authentication ---
    @POST("auth/register")
    suspend fun register(
        @Body request: Map<String, String>
    ): Response<User>

    @FormUrlEncoded
    @POST("auth/token")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<User>

    // --- Plants ---
    @POST("plants")
    suspend fun createPlant(
        @Body request: PlantCreateRequest
    ): Response<Plant>

    @GET("plants")
    suspend fun getAllPlants(): Response<List<Plant>>

    @GET("plants/{plant_id}")
    suspend fun getPlantById(
        @Path("plant_id") plantId: String
    ): Response<Plant>

    @PUT("plants/{plant_id}")
    suspend fun updatePlant(
        @Path("plant_id") plantId: String,
        @Body request: PlantUpdateRequest
    ): Response<Plant>

    @POST("watering")
    suspend fun recordWateringEvent(
        @Body request: WateringEventCreateRequest
    ): Response<WateringEvent>

    @DELETE("plants/{plant_id}")
    suspend fun deletePlant(
        @Path("plant_id") plantId: String
    ): Response<Unit>

    // --- Watering
    @GET("watering/plant/{plant_id}")
    suspend fun getPlantWateringHistory(
        @Path("plant_id") plantId: String
    ): Response<List<WateringEvent>>

    @GET("watering/plant/{plant_id}/last")
    suspend fun getLastWateringEvent(
        @Path("plant_id") plantId: String
    ): Response<WateringEvent>

    @PUT("watering/{event_id}")
    suspend fun updateWateringEvent(
        @Path("event_id") eventId: String,
        @Body request: WateringEventUpdateRequest
    ): Response<WateringEvent>

    @DELETE("watering/{event_id}")
    suspend fun deleteWateringEvent(
        @Path("event_id") eventId: String
    ): Response<Unit>
}