package com.example.traintracks.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.navigation.NavHostController
import com.example.traintracks.SearchApiService
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.compose.material3.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import java.util.Locale
import java.util.UUID


val auth = Firebase.auth
val currentUser = auth.currentUser
val userId = currentUser?.uid ?: ""
val difficulties = arrayOf("beginner", "intermediate", "expert")
val muscles = arrayOf("abdominals", "abductors", "adductors", "biceps", "calves", "chest", "forearms", "glutes", "hamstrings", "lats", "lower_back", "middle_back", "neck", "quadriceps", "traps", "triceps", null)
val types = arrayOf("cardio", "strength", "stretching", "plyometrics", "powerlifting", "strongman", "olympic_weightlifting")

@Composable
fun HomeScreen(navController: NavHostController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val currentContext = LocalContext.current
    var workouts by remember { mutableStateOf<List<Workout>>(listOf()) }
    var selectedWorkout by remember { mutableStateOf<Workout?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/workouts")
    val logId = dbRef.push().key ?: return
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.api-ninjas.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    var workoutsByType by remember { mutableStateOf<List<Workout>>(listOf()) }
    var workoutsByMuscle by remember { mutableStateOf<List<Workout>>(listOf()) }
    var workoutsByDifficulty by remember { mutableStateOf<List<Workout>>(listOf()) }
    val searchApiService = retrofit.create(SearchApiService::class.java)
    var randomMuscle by remember { mutableStateOf("") }
    var randomType by remember { mutableStateOf("") }
    var randomDifficulty by remember { mutableStateOf("") }


    LaunchedEffect(Unit) {
        randomMuscle = muscles.randomOrNull() ?: ""
        randomType = types.randomOrNull() ?: ""
        randomDifficulty = difficulties.randomOrNull() ?: ""

        // Fetch workouts by random muscle
        fetchWorkouts(searchApiService, randomMuscle, null, null) { apiResults ->
            workoutsByMuscle = apiResults
            Log.d("FetchWorkouts", "Muscle Workouts: $apiResults")
        }

        // Fetch workouts by random type
        fetchWorkouts(searchApiService, null, randomType, null) { apiResults ->
            workoutsByType = apiResults
        }

        // Fetch workouts by random difficulty
        fetchWorkouts(searchApiService, null, null, randomDifficulty) { apiResults ->
            workoutsByDifficulty = apiResults
        }

    }

    if (showDialog && selectedWorkout != null) {
        WorkoutDetailsDialog(
            workout = selectedWorkout!!,
            onClose = { showDialog = false },
            onSaveWorkout = { workout -> saveWorkoutToFirebase(workout, userId, snackbarHostState) },
            snackbarHostState = snackbarHostState
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
            modifier = Modifier.fillMaxSize(),
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

            Icon(
                painter = painterResource(id = R.drawable.baseline_fitness_center_24),
                contentDescription = "",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(105.dp)
            )

            Text(
                text = "TrainTracks",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 50.sp,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
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
                    color = MaterialTheme.colorScheme.background,
                    fontSize = 18.sp
                )
            }

            // Intro Text
            Text(
                text = "Discover:",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold
            )

            val scrollState = rememberScrollState()

            Column(modifier = Modifier.verticalScroll(scrollState)) {

                Text(
                    text = randomMuscle.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                // LazyRow for displaying workouts
                LazyRow(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(workoutsByMuscle) { workout ->
                        WorkoutCard(workout = workout, onWorkoutClick = {
                            selectedWorkout = workout
                            showDialog = true
                        })
                    }
                }

                Text(
                    text = randomType.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                LazyRow(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(workoutsByType) { workout ->
                        WorkoutCard(workout = workout, onWorkoutClick = {
                            selectedWorkout = workout
                            showDialog = true
                        })
                    }
                }

                Text(
                    text = randomDifficulty.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(
                        Locale.getDefault()) else it.toString() },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                LazyRow(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(workoutsByDifficulty) { workout ->
                        WorkoutCard(workout = workout, onWorkoutClick = {
                            selectedWorkout = workout
                            showDialog = true
                        })
                    }
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
        "strength" -> R.drawable.strength
        "stretching" -> R.drawable.icon_stretching
        "strongman" -> R.drawable.icon_strongman
        else -> R.drawable.icon_strongman
    }

    Box(modifier = Modifier.height(200.dp)) {
        Card(
            modifier = Modifier
                .alpha(0.8f)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxHeight()
                .clickable { onWorkoutClick(workout) },
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Display the icon
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = "Workout Icon",
                    modifier = Modifier.size(50.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailsDialog(
    workout: Workout,
    onClose: () -> Unit,
    onSaveWorkout: (Workout) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var showSavedMessage by remember { mutableStateOf(false) }
    var showDeleteMessage by remember { mutableStateOf(false) }
    var isWorkoutSaved by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    LaunchedEffect(workout) {
        // Check if the workout is already saved
        checkIfWorkoutSaved(workout, userId) { isSaved ->
            isWorkoutSaved = isSaved
        }
    }

    SnackbarHost(hostState = snackbarHostState)

    Dialog(onDismissRequest = onClose) {
        Surface(
            modifier = Modifier
                .height(640.dp)
                .fillMaxWidth()
                .alpha(0.92f),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .padding(end = 20.dp, top = 20.dp)
                ) {
                    TopAppBar(
                        title = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {

                                Text(
                                    text = workout.name,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Left,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                            }
                        },
                        actions = {
                            if (!isWorkoutSaved) {
                                Icon(
                                    painter = painterResource(id = R.drawable.heart_open),
                                    contentDescription = "Add to Favourites",
                                    modifier = Modifier
                                        .size(30.dp)
                                        .padding(1.dp)
                                        .clickable {
                                            saveWorkoutToFirebase(
                                                workout,
                                                userId,
                                                snackbarHostState
                                            )
                                            isWorkoutSaved = true
                                        }
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.heart_closed),
                                    contentDescription = "Remove from Favourites",
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clickable {
                                            onDeleteWorkout(workout, userId, snackbarHostState)
                                            isWorkoutSaved = false
                                        }
                                )
                            }
                        }
                    )
                    Row(
                        modifier = Modifier.padding(start = 20.dp)
                    ){
                        val iconResId = when (workout.type) {
                            "cardio" -> R.drawable.icon_cardio
                            "olympic_weightlifting" -> R.drawable.icon_olympic_weighlifting
                            "plyometrics" -> R.drawable.icon_plyometrics
                            "powerlifting" -> R.drawable.icon_powerlifting
                            "strength" -> R.drawable.strength
                            "stretching" -> R.drawable.icon_stretching
                            "strongman" -> R.drawable.icon_strongman
                            else -> R.drawable.icon_strongman
                        }

                        val difficultyIconResId = when (workout.difficulty) {
                            difficulties[0] -> R.drawable.easy
                            difficulties[1] -> R.drawable.medium
                            difficulties[2] -> R.drawable.hard
                            else -> R.drawable.emh
                        }

                        val muscleIconResId = when (workout.difficulty) {
                            muscles[0] -> R.drawable.easy
                            muscles[1] -> R.drawable.medium
                            muscles[2] -> R.drawable.hard
                            else -> R.drawable.emh
                        }

                        Image(
                            painter = painterResource(id = iconResId),
                            contentDescription = "Workout Type Icon",
                            colorFilter = ColorFilter.tint(getTypeColor(workout.type)),
                            modifier = Modifier
                                .size(50.dp)
                        )

                        Spacer(Modifier.height(6.dp))

                        Image(
                            painter = painterResource(difficultyIconResId),
                            contentDescription = "Difficulty Icon",
                            colorFilter = ColorFilter.tint(getDifficultyColor(workout.difficulty)),
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 8.dp)

            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                ){
                    Text("Type: ${workout.type}")
                    Text("Muscle: ${workout.muscle}")
                    Text("Difficulty: ${workout.difficulty}")
                    Text("Equipment: ${workout.equipment}")
                    Text("Instructions: ${workout.instructions}")

                    Spacer(Modifier.height(16.dp))
                }

            }
            Column(
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 4.dp),
            ) {
                // Close Button
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = MaterialTheme.colorScheme.background)
                }

                if (showDeleteMessage) {
                    LaunchedEffect(snackbarMessage) {
                        snackbarHostState.showSnackbar(
                            message = snackbarMessage,
                            duration = SnackbarDuration.Short
                        )
                        showDeleteMessage = false
                    }
                }
            }
            }
        }
    }
}

fun saveWorkoutToFirebase(workout: Workout, userId: String, snackbarHostState: SnackbarHostState) {
    val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/workouts")
    dbRef.child(workout.id ?: dbRef.push().key!!)
        .setValue(workout)
        .addOnSuccessListener {
            CoroutineScope(Dispatchers.Main).launch {
                snackbarHostState.showSnackbar("Workout added to favourites")
            }
        }
        .addOnFailureListener { e ->
            CoroutineScope(Dispatchers.Main).launch {
                snackbarHostState.showSnackbar(e.message ?: "Failed to save workout")
            }
        }
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

fun onDeleteWorkout(workout: Workout, userId: String, snackbarHostState: SnackbarHostState) {
    val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/workouts")
    dbRef.child(workout.id)
        .removeValue()
        .addOnSuccessListener {
            CoroutineScope(Dispatchers.Main).launch {
                snackbarHostState.showSnackbar("Workout successfully removed")
            }
        }
        .addOnFailureListener { e ->
            CoroutineScope(Dispatchers.Main).launch {
                snackbarHostState.showSnackbar(e.message ?: "Failed to remove workout")
            }
        }
}

private fun fetchWorkouts(
    service: SearchApiService,
    muscle: String?,
    type: String?,
    difficulty: String?,
    onSuccess: (List<Workout>) -> Unit
) {
    val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/workouts")
    val logId = dbRef.push().key ?: return
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val apiResults = service.searchWorkouts(
                name = null,
                type = type,
                muscle = muscle,
                difficulty = difficulty,
                apiKey = "KX79m6HUenAsqfTvt9WydA==ib8FER6nxlcnsxnk"
            )

            withContext(Dispatchers.Main) {
                onSuccess(apiResults.map { apiResult ->
                    Workout(
                        id = logId,
                        name = apiResult.name,
                        type = apiResult.type,
                        muscle = apiResult.muscle,
                        difficulty = apiResult.difficulty,
                        equipment = apiResult.equipment,
                        instructions = apiResult.instructions
                    )
                })
            }
        } catch (e: Exception) {
            Log.e("API Error", "Error fetching workouts", e)
        }
    }
}
