package com.example.traintracks.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.traintracks.R
import com.example.traintracks.Workout
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.example.traintracks.SearchApiService
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val auth = Firebase.auth
val currentUser = auth.currentUser
val userId = currentUser?.uid ?: ""

@Composable
fun HomeScreen(navController: NavHostController) {
    val currentContext = LocalContext.current
    var workouts by remember { mutableStateOf<List<Workout>>(listOf()) }
    var selectedWorkout by remember { mutableStateOf<Workout?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/workouts")
    val logId = dbRef.push().key ?: return
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.api-ninjas.com/v1/") // Replace with your API base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val searchApiService = retrofit.create(SearchApiService::class.java)


    LaunchedEffect(Unit) {
        val muscles = arrayOf("abdominals", "abductors", "adductors", "biceps", "calves", "chest", "forearms", "glutes", "hamstrings", "lats", "lower_back", "middle_back", "neck", "quadriceps", "traps", "triceps", null)
        val muscle = muscles.random()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiResults = searchApiService.searchWorkouts(
                    name = null,
                    type = null,
                    muscle = muscle,
                    difficulty = null,
                    apiKey = "KX79m6HUenAsqfTvt9WydA==ib8FER6nxlcnsxnk"
                )
                withContext(Dispatchers.Main) {
                    workouts = apiResults.map { apiResult ->
                        Workout(
                            id = logId,
                            name = apiResult.name,
                            type = apiResult.type,
                            muscle = apiResult.muscle,
                            difficulty = apiResult.difficulty,
                            equipment = apiResult.equipment,
                            instructions = apiResult.instructions
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("API Error", "Error fetching workouts", e)
            }
        }
    }

    if (showDialog && selectedWorkout != null) {
        WorkoutDetailsDialog(
            workout = selectedWorkout!!,
            onClose = { showDialog = false },
            onSaveWorkout = { workout ->
                saveWorkoutToFirebase(workout, userId)
                // You may update other states or perform additional actions here if needed
            }
        )
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.login),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Welcome!",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 50.sp,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
            )

            // Intro Text
            Text(
                text = "Discover and track your workouts.",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )

            // Button
            Button(
                onClick = {
                    val intent = Intent(currentContext, SearchScreen::class.java)
                    currentContext.startActivity(intent)
                },
                shape = RoundedCornerShape(5.dp),
                enabled = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(
                    text = "Find a new Workout",
                    color = Color.White
                )
            }
            // LazyRow for displaying workouts
            LazyRow(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(workouts) { workout ->
                    WorkoutCard(workout = workout, onWorkoutClick = {
                        selectedWorkout = workout
                        showDialog = true
                    })
                }
            }

        }
    }
}

@Composable
fun WorkoutCard(workout: Workout, onWorkoutClick: (Workout) -> Unit) {
    // Determine the icon resource ID based on the workout type
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

    Box(modifier = Modifier.height(200.dp)) {
        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxHeight()
                .clickable { onWorkoutClick(workout) },
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Display the icon
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = "Workout Icon",
                    modifier = Modifier.size(50.dp)
                )

                // Content of the card
                Text(workout.name, fontWeight = FontWeight.Bold)
                Text("Type: ${workout.type}")
                Text("Muscle: ${workout.muscle}")
                Text("Difficulty: ${workout.difficulty}")
                // Add more workout details as needed
            }
        }
    }
}
@Composable
fun WorkoutDetailsDialog(
    workout: Workout,
    onClose: () -> Unit,
    onSaveWorkout: (Workout) -> Unit
) {
    var showSavedMessage by remember { mutableStateOf(false) }
    var isWorkoutSaved by remember { mutableStateOf(false) }

    LaunchedEffect(workout) {
        // Check if the workout is already saved
        checkIfWorkoutSaved(workout, userId) { isSaved ->
            isWorkoutSaved = isSaved
        }
    }

    Dialog(onDismissRequest = onClose) {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            shape = RoundedCornerShape(8.dp)
        ) {
            val scrollState = rememberScrollState()
            Column(modifier = Modifier.padding(20.dp).verticalScroll(scrollState),) {
                // Workout Icon
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
                    modifier = Modifier.size(50.dp)
                )

                // Workout Details
                Text(workout.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Type: ${workout.type}")
                Text("Muscle: ${workout.muscle}")
                Text("Difficulty: ${workout.difficulty}")
                Text("Equipment: ${workout.equipment}")
                Text("Instructions: ${workout.instructions}")

                Spacer(Modifier.height(16.dp))

                // Save Workout Button
                if (!isWorkoutSaved) {
                    Button(
                        onClick = {
                            onSaveWorkout(workout)
                            isWorkoutSaved = true
                            showSavedMessage = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add to Workouts", color = Color.White)
                    }
                } else if (showSavedMessage) {
                    Text("Workout Already Saved", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(Modifier.height(16.dp))

                // Close Button
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}


fun Workout.toMap(id: String): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "name" to name,
        "type" to type,
        "difficulty" to difficulty,
        "muscle" to muscle,
        "equipment" to equipment,
        "instructions" to instructions
    )
}


fun saveWorkoutToFirebase(workout: Workout, userId: String) {
    val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/workouts").push()
    dbRef.setValue(workout)
}

fun checkIfWorkoutSaved(workout: Workout, userId: String, onResult: (Boolean) -> Unit) {
    val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/workouts")
    dbRef.orderByChild("name").equalTo(workout.name)
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onResult(snapshot.children.any { it.getValue(Workout::class.java)?.name == workout.name })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
}