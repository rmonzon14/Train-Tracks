package com.example.traintracks

data class WorkoutLog(
    val id: String = "",
    var name: String = "",
    var duration: String = "",
    var distance: String? = null,
    var sets: String = "",
    var reps: String = "",
    val date: String = ""
) {
    // Add a parameterless constructor
    constructor() : this("","", "", null, "", "", "")
}