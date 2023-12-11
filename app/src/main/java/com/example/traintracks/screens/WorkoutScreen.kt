package com.example.traintracks.screens

import com.example.traintracks.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource

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

    var workoutList by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var workoutToDeleteIndex by remember { mutableStateOf(-1) }

    db.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            workoutList = snapshot.children.mapNotNull { it.getValue(Workout::class.java) }
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle error
        }
    })

    // Function to delete a workout
    fun deleteWorkout(workoutIndex: Int) {
        val workoutToDelete = workoutList[workoutIndex]
        db.orderByChild("name").equalTo(workoutToDelete.name).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    child.ref.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
            }
        })
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete Workout") },
            text = { Text("Are you sure you want to delete this workout?") },
            confirmButton = {
                Button(onClick = {
                    deleteWorkout(workoutToDeleteIndex)
                    showDialog = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (workoutList.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No workouts found for your profile")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("YourSearchScreenRoute") }) {
                Text("Find Workouts")
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 85.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Saved Workouts",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            itemsIndexed(workoutList) { index, workout ->
                var expanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .padding(8.dp)
                        .widthIn(min = 350.dp, max = 350.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        val iconResId = when (workout.type) {
                            "cardio" -> R.drawable.icon_cardio
                            "olympic_weightlifting" -> R.drawable.icon_olympic_weighlifting
                            "plyometrics" -> R.drawable.icon_plyometrics
                            "powerlifting" -> R.drawable.icon_powerlifting
                            "strength" -> R.drawable.icon_strength
                            "stretching" -> R.drawable.icon_stretching
                            "strongman" -> R.drawable.icon_strongman
                            else -> R.drawable.icon_strongman
                        }
                        Image(
                            painter = painterResource(id = iconResId),
                            contentDescription = "Workout Icon",
                            modifier = Modifier.size(30.dp)
                        )

                        Text(
                            text = "${workout.name}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Type: ${workout.type}",
                            fontSize = 16.sp,
                            color = Color.Blue
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Difficulty: ${workout.difficulty}",
                            fontSize = 16.sp,
                            color = Color.Red
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        if (expanded) {
                            Text(
                                text = "Muscle Group: ${workout.muscle}",
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Equipment: ${workout.equipment}",
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Instructions: ${workout.instructions}",
                                fontSize = 16.sp
                            )
                        }

                        Text(
                            text = "Delete Saved Workout",
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clickable {
                                    workoutToDeleteIndex = index
                                    showDialog = true
                                },
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}



data class Workout(
    val name: String = "",
    val type: String = "",
    val muscle: String = "",
    val equipment: String = "",
    val difficulty: String = "",
    val instructions: String = ""
)