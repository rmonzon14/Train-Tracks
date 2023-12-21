package com.example.traintracks.screens

import android.content.Context
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.traintracks.WorkoutLog
import com.google.firebase.database.DatabaseReference
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import java.util.Locale
import java.util.UUID


val auth = Firebase.auth
val currentUser = auth.currentUser
val userId = currentUser?.uid ?: ""

@Composable
fun HomeScreen(navController: NavHostController) {
    val sharedViewModel: SharedViewModel = viewModel()
    var recommendedWorkoutsByMuscleGroup by remember { mutableStateOf<Map<String, List<Workout>>>(mapOf()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val currentContext = LocalContext.current
    var selectedWorkout by remember { mutableStateOf<Workout?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var workoutsByType by remember { mutableStateOf<List<Workout>>(listOf()) }
    var workoutsByMuscle by remember { mutableStateOf<List<Workout>>(listOf()) }
    var workoutsByDifficulty by remember { mutableStateOf<List<Workout>>(listOf()) }
    var randomMuscle by remember { mutableStateOf("") }
    var randomType by remember { mutableStateOf("") }
    var randomDifficulty by remember { mutableStateOf("") }
    val workoutLogs = remember { mutableStateOf<List<WorkoutLog>>(listOf()) }
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.api-ninjas.com/v1/") // Replace with your API's base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    var apiService = retrofit.create(SearchApiService::class.java)

    LaunchedEffect(Unit) {
        initializeDataIfNeeded(currentContext, apiService, sharedViewModel)
        sharedViewModel.initializeData(apiService)
    }

    var difficulties = sharedViewModel.difficulties.observeAsState().value
    var muscles = sharedViewModel.muscles.observeAsState().value
    var types = sharedViewModel.types.observeAsState().value

    // Check if all data is loaded
    val isDataLoaded = muscles?.isNotEmpty() == true &&
            types?.isNotEmpty() == true &&
            difficulties?.isNotEmpty() == true

    LaunchedEffect(isDataLoaded) {
        if (isDataLoaded) {
            // Randomly select muscle, type, and difficulty
            randomMuscle = muscles?.randomOrNull() ?: ""
            randomType = types?.randomOrNull() ?: ""
            randomDifficulty = difficulties?.randomOrNull() ?: ""

            val dataRef = FirebaseDatabase.getInstance().getReference("/data")
            val logsRef = FirebaseDatabase.getInstance().getReference("users/$userId/logs")

            logsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val fetchedLogs = snapshot.children.mapNotNull { it.getValue(WorkoutLog::class.java) }

                    // Filter logs for the last 30 days
                    val logsWithinLast30Days = filterLogsForLast30Days(fetchedLogs)
                    workoutLogs.value = logsWithinLast30Days

                    dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val items = dataSnapshot.children.mapNotNull { it.getValue(Workout::class.java) }

                            findAllLeastEngagedMuscleGroups(calculateMuscleGroupPercentages(workoutLogs.value)).forEach { muscleGroup ->
                                val muscleCount = items.count { it.muscle == muscleGroup }
                                val maxMuscleOffset = if (muscleCount > 10) muscleCount - 10 else 0
                                val randomMuscleOffset = (0..maxMuscleOffset).random()

                                fetchWorkouts(apiService, null, muscleGroup, null, null, randomMuscleOffset) { workouts ->
                                    recommendedWorkoutsByMuscleGroup = recommendedWorkoutsByMuscleGroup.toMutableMap().apply {
                                        put(muscleGroup, workouts)
                                    }
                                }
                            }
                        }

                        override fun onCancelled(dataError: DatabaseError) {
                            Log.e("Firebase", "Failed to fetch data", dataError.toException())
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar("Error: Unable to fetch workout logs")
                    }
                }
            })

            dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val items = snapshot.children.mapNotNull { it.getValue(Workout::class.java) }

                    // Count items for each category
                    val typeCount = items.count { it.type == randomType }
                    val muscleCount = items.count { it.muscle == randomMuscle }
                    val difficultyCount = items.count { it.difficulty == randomDifficulty }

                    // Calculate max offsets
                    val maxTypeOffset = if (typeCount > 10) typeCount - 10 else 0
                    val maxMuscleOffset = if (muscleCount > 10) muscleCount - 10 else 0
                    val maxDifficultyOffset = if (difficultyCount > 10) difficultyCount - 10 else 0



                    // Fetch data for random categories
                    fetchWorkouts(apiService, null, randomMuscle, null, null, (0..maxMuscleOffset).random()) {
                        workoutsByMuscle = it
                        Log.d("CHECK_POINT", "Fetched Muscle Workouts: ${it.size} muscle: $randomMuscle muscles: $muscles")
                    }
                    fetchWorkouts(apiService, null, null, randomType, null, (0..maxTypeOffset).random()) {
                        workoutsByType = it
                        Log.d("CHECK_POINT", "Fetched Type Workouts: ${it.size} type: $randomType types: $types")
                    }
                    fetchWorkouts(apiService, null, null, null, randomDifficulty, (0..maxDifficultyOffset).random()) {
                        workoutsByDifficulty = it
                        Log.d("CHECK_POINT", "Fetched Difficulty Workouts: ${it.size} difficulty: $randomDifficulty difficulties: $difficulties")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CHECK_POINT", "Failed to fetch data", error.toException())
                }
            })
        }
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
            SnackbarHost(hostState = snackbarHostState)
            // Title
            Icon(
                painter = painterResource(id = R.drawable.baseline_fitness_center_24),
                contentDescription = "",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(75.dp)
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
                Text("Recommended Muscle Groups", style = MaterialTheme.typography.headlineSmall)

                Spacer(Modifier.height(16.dp))

                recommendedWorkoutsByMuscleGroup.forEach { (muscleGroup, workouts) ->
                    if (!muscleGroup.isNullOrEmpty()) {
                        Log.d("CHECK_POINT", "%%%muslceGroup%%%$muscleGroup###workouts###: $workouts")
                        Text(muscleGroup.toTitleCase(), style = MaterialTheme.typography.headlineSmall)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(workouts) { workout ->
                                WorkoutCard(workout = workout, difficulties = difficulties, onWorkoutClick = {
                                    selectedWorkout = workout
                                    showDialog = true
                                })
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                @Composable
                fun RandomCategoryRow(categoryName: String, workouts: List<Workout>) {
                    if (workouts.isNotEmpty()) {
                        Text("$categoryName".toTitleCase(), style = MaterialTheme.typography.headlineSmall)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(workouts) { workout ->
                                WorkoutCard(workout, difficulties, onWorkoutClick = {
                                    selectedWorkout = workout
                                    showDialog = true
                                })
                            }
                        }
                    }
                }

                Text("Random Workouts", style = MaterialTheme.typography.headlineSmall)

                RandomCategoryRow("Muscle: $randomMuscle", workoutsByMuscle)
                RandomCategoryRow("Type: $randomType", workoutsByType)
                RandomCategoryRow("Difficulty: $randomDifficulty", workoutsByDifficulty)

                // Workout details dialog
                if (showDialog && selectedWorkout != null && difficulties != null) {
                    WorkoutDetailsDialog(
                        workout = selectedWorkout!!,
                        onClose = { showDialog = false },
                        difficulties = difficulties,
                        snackbarHostState
                    )
                }
            }

        }
    }
}

@Composable
fun WorkoutCard(workout: Workout, difficulties: Array<String>?, onWorkoutClick: (Workout) -> Unit) {
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
                Text(workout.name.toTitleCase(), fontWeight = FontWeight.Bold)
                Text("Type: ${workout.type.toTitleCase()}")
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
    difficulties: Array<String>?,
    snackbarHostState: SnackbarHostState
) {
    var showDeleteMessage by remember { mutableStateOf(false) }
    var isWorkoutSaved by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var savedFirebaseId by remember { mutableStateOf<String?>(null) }
    var isOperationInProgress by remember { mutableStateOf(false) }

    LaunchedEffect(workout) {
        checkIfWorkoutSaved(workout) { isSaved, firebaseId ->
            isWorkoutSaved = isSaved
            savedFirebaseId = firebaseId
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
                                            if (!isOperationInProgress) {
                                                isOperationInProgress = true
                                                if (!isWorkoutSaved) {
                                                    // Add to favorites
                                                    saveWorkoutToFirebase(
                                                        workout,
                                                        userId,
                                                        snackbarHostState,
                                                        onSaved = { newFirebaseId ->
                                                            savedFirebaseId = newFirebaseId
                                                            isWorkoutSaved = true
                                                            isOperationInProgress = false
                                                        },
                                                        onFailure = {
                                                            isOperationInProgress = false
                                                        }
                                                    )
                                                } else {
                                                    // Remove from favorites
                                                    savedFirebaseId?.let { firebaseId ->
                                                        onDeleteWorkout(
                                                            firebaseId,
                                                            userId,
                                                            snackbarHostState
                                                        )
                                                        isWorkoutSaved = false
                                                        isOperationInProgress = false
                                                    }
                                                }
                                            }
                                        }
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.heart_closed),
                                    contentDescription = "Remove from Favourites",
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clickable {
                                            savedFirebaseId?.let { firebaseId ->
                                                onDeleteWorkout(
                                                    firebaseId,
                                                    userId,
                                                    snackbarHostState
                                                )
                                                isWorkoutSaved = false
                                            }
                                        }
                                )
                            }
                        }
                    )
                    Row(
                        modifier = Modifier.padding(start = 20.dp)
                    ){
                        showTypeIcon(workout, 50, MaterialTheme.colorScheme.primary)

                        Spacer(Modifier.height(6.dp))

                        showDifficultyIcon(workout, 50, difficulties = difficulties)
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
                    Text(
                        "${workout.type.toTitleCase()}",
                        color = getTypeColor(workout.type)
                    )
                    Text(
                        "Muscle: ${workout.muscle.toTitleCase()}",
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Difficulty: ${workout.difficulty.toTitleCase()}")
                    Text(
                        "Equipment: ${workout.equipment.toTitleCase()}",
                        color = MaterialTheme.colorScheme.primary
                    )
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

fun saveWorkoutToFirebase(workout: Workout, userId: String, snackbarHostState: SnackbarHostState, onSaved: (String) -> Unit, onFailure: () -> Unit) {
    val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/workouts")

    // Check if the workout is already saved
    dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            var isAlreadySaved = false

            for (dataSnapshot in snapshot.children) {
                val savedWorkout = dataSnapshot.getValue(Workout::class.java)
                if (savedWorkout?.name == workout.name) {
                    isAlreadySaved = true
                    break
                }
            }

            if (!isAlreadySaved) {
                // Save the new workout
                val workoutId = dbRef.push().key ?: return
                dbRef.child(workoutId)
                    .setValue(workout.copy(id = workoutId))
                    .addOnSuccessListener {
                        CoroutineScope(Dispatchers.Main).launch {
                            snackbarHostState.showSnackbar("Workout added to favourites")
                            onSaved(workoutId)
                        }
                    }
                    .addOnFailureListener { e ->
                        CoroutineScope(Dispatchers.Main).launch {
                            snackbarHostState.showSnackbar(e.message ?: "Failed to save workout")
                            onFailure()
                        }
                    }
            } else {
                // Workout is already saved, no need to save again
                onFailure()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Failed to check if workout is already saved", error.toException())
            onFailure()
        }
    })
}



private fun checkIfWorkoutSaved(workout: Workout, callback: (Boolean, String?) -> Unit) {
    val auth = Firebase.auth
    val currentUser = auth.currentUser
    val userId = currentUser?.uid ?: return

    val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/workouts")
    dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            var isSaved = false
            var savedFirebaseId: String? = null
            snapshot.children.forEach { dataSnapshot ->
                val savedWorkout = dataSnapshot.getValue(Workout::class.java)
                if (savedWorkout?.name == workout.name) {
                    isSaved = true
                    savedFirebaseId = dataSnapshot.key
                    return@forEach
                }
            }
            callback(isSaved, savedFirebaseId)
        }
        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Failed to check if workout is saved", error.toException())
        }
    })
}

fun onDeleteWorkout(workoutId: String, userId: String, snackbarHostState: SnackbarHostState) {
    val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/workouts")
    dbRef.child(workoutId)
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


// Add this function in HomeScreen or as a top-level function
fun initializeDataIfNeeded(context: Context, apiService: SearchApiService, sharedViewModel: SharedViewModel) {
    val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    val isInitialized = prefs.getBoolean("isDataInitialized", false)

    if (!isInitialized) {
        sharedViewModel.initializeData(apiService)

            // Save flag in SharedPreferences
            with(prefs.edit()) {
                putBoolean("isDataInitialized", true)
                apply()
            }
    }
}

fun fetchWorkouts(
    service: SearchApiService,
    name: String?,
    muscle: String?,
    type: String?,
    difficulty: String?,
    offset: Int?,
    onSuccess: (List<Workout>) -> Unit
) {
    val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/workouts")
    val logId = dbRef.push().key ?: return
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val apiResults = service.searchWorkouts(
                name = name,
                type = type,
                muscle = muscle,
                difficulty = difficulty,
                apiKey = "KX79m6HUenAsqfTvt9WydA==ib8FER6nxlcnsxnk",
                offset = offset
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

suspend fun fetchAndStoreData(apiService: SearchApiService, dbRef: DatabaseReference) {
    val dataRef = FirebaseDatabase.getInstance().getReference("users/$userId/workouts")
    var offset = 0
    while (true) {
        val workouts = apiService.searchWorkouts(null, null, null, null, offset, "KX79m6HUenAsqfTvt9WydA==ib8FER6nxlcnsxnk")
        if (workouts.isEmpty()) break

        workouts.forEach { workout ->
            val itemId = dataRef.push().key ?: return
            dbRef.child(itemId).setValue(workout)
        }

        offset += 10
    }
}

// This function updates the UI state based on data in SharedViewModel
@Composable
private fun updateUIState(viewModel: SharedViewModel, state: HomeScreenState) {
    // You can update the state based on the data in the ViewModel
    viewModel.difficulties.observeAsState().value?.let {
        state.difficulties = it
    }
    viewModel.muscles.observeAsState().value?.let {
        state.muscles = it
    }
    viewModel.types.observeAsState().value?.let {
        state.types = it
    }
}



