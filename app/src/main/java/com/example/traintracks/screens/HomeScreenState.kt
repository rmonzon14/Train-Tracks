package com.example.traintracks.screens

// Data class representing the UI state
data class HomeScreenState(
    var difficulties: Array<String> = arrayOf(),
    var muscles: Array<String> = arrayOf(),
    var types: Array<String> = arrayOf(),
    // Add other fields as necessary
)