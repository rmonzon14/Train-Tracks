package com.example.traintracks

data class WorkoutLog(
    val id: String = "",
    val name: String = "",
    val duration: String = "",
    val distance: String? = null,
    val sets: String = "",
    val reps: String = "",
    val date: String = ""
) {
    // Add a parameterless constructor
    constructor() : this("","", "", null, "", "", "")
}