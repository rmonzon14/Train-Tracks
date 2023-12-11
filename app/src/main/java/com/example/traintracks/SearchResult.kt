package com.example.traintracks

data class SearchResult(
    // Define fields here based on the API response
    val name: String,
    val type: String,
    val muscle: String,
    val equipment: String,
    val difficulty: String,
    val instructions: String
)
