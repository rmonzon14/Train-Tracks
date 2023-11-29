package com.example.traintracks

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SearchApiService {
    @GET("exercises?")
    suspend fun searchWorkouts(
        @Query("name") workoutName: String?,
        @Query("type") workoutType: String,
        @Query("muscle") muscleGroup: String,
        @Query("difficulty") difficulty: String,
        @Header("X-Api-Key") apiKey: String
    ): List<SearchResult>
}