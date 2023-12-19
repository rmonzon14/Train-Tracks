package com.example.traintracks

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SearchApiService {
    @GET("exercises?")
    suspend fun searchWorkouts(
        @Query("name") name: String?,
        @Query("type") type: String?,
        @Query("muscle") muscle: String?,
        @Query("difficulty") difficulty: String?,
        @Query("offset") offset: Int?,
        @Header("X-Api-Key") apiKey: String
    ): List<SearchResult>
}