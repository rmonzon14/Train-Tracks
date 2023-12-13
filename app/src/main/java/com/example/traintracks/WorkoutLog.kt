package com.example.traintracks

data class WorkoutLog(
    val id: String = "",
    var name: String = "",
    var type: String = "",
    var difficulty: String = "",
    var duration: String = "",
    var distance: String? = null,
    var sets: String = "",
    var reps: String = "",
    var date: String = ""
) {
    // Add a parameterless constructor
    constructor() : this("","","","", "", null, "", "", "")
}