package com.example.traintracks

data class Workout(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val muscle: String = "",
    val equipment: String = "",
    val difficulty: String = "",
    val instructions: String = "",
    var isSaved: Boolean = false
)