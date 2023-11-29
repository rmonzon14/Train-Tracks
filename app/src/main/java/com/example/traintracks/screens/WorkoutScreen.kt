package com.example.traintracks.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.traintracks.ui.theme.TrainTracksTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private lateinit var auth : FirebaseAuth
private lateinit var db  : DatabaseReference
@Composable
fun WorkoutScreen(navController: NavController) {
    val auth = Firebase.auth
    val currentUser = auth.currentUser

    if (currentUser != null) {
        val userId = currentUser.uid

        db = FirebaseDatabase.getInstance().getReference("users/$userId")
    }

    // State to hold the list of workouts
    var workoutList by remember { mutableStateOf<List<Workout>>(emptyList()) }

    // Read data from Firebase Realtime Database
    db.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            // Convert the DataSnapshot to a list of Workout objects
            workoutList = snapshot.children.mapNotNull { it.getValue(Workout::class.java) }
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle error
            // You may want to show an error message or log the error
        }
    })

    // Display the list of workouts
    Column(
        Modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Workout Screen",
        )

        // Display each workout in the list
        workoutList.forEach { workout ->

            Text(
                text = "Workout Name: ${workout.name}, Type: ${workout.type}, Difficulty: ${workout.difficulty}",
            )
            // Add more Text elements or compose components based on your workout data structure
        }
    }
}

// Define your Workout data class
data class Workout(
    val name: String = "",
    val type: String = "",
    val difficulty: String = "",
    // Add more fields if necessary
)
