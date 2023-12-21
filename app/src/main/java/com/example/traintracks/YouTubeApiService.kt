package com.example.traintracks

import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {
    @GET("search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("key") apiKey: String
    ): YouTubeSearchResponse
}

data class YouTubeSearchResponse(
    val items: List<YouTubeVideoItem>
)

data class YouTubeVideoItem(
    val id: YouTubeVideoId
)

data class YouTubeVideoId(
    val videoId: String
)