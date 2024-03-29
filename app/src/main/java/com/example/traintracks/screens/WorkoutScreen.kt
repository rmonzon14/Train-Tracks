package com.example.traintracks.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import com.example.traintracks.R.drawable.*
import com.example.traintracks.Workout
import com.example.traintracks.WorkoutLog
import com.google.firebase.auth.auth
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.traintracks.R
import com.example.traintracks.SearchApiService
import com.example.traintracks.YouTubeApiService
import com.google.android.gms.common.api.internal.BackgroundDetector.initialize
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private lateinit var db: DatabaseReference

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WorkoutScreen() {
    val auth = Firebase.auth
    val currentUser = auth.currentUser
    val currentContext = LocalContext.current

    if (currentUser != null) {
        val userId = currentUser.uid
        db = FirebaseDatabase.getInstance().getReference("users/$userId/workouts")
    }

    var showYouTubeDialog by remember { mutableStateOf(false) }
    var youTubeVideoId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var workoutList by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var workoutToDeleteIndex by remember { mutableStateOf(-1) }
    var showFullScreenDialog by remember { mutableStateOf(false) }
    var selectedWorkout by remember { mutableStateOf<Workout?>(null) }
    var showLogWorkoutDialog by remember { mutableStateOf(false) }
    var workoutName by remember { mutableStateOf("") }
    var workoutDuration by remember { mutableStateOf("") }
    var workoutDistance by remember { mutableStateOf("") }
    var workoutSets by remember { mutableStateOf("") }
    var workoutReps by remember { mutableStateOf("") }
    var workoutDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var showConfirmationMessage by remember { mutableStateOf(false) }
    var showErrorSnackbar by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var selectedWorkouts by remember { mutableStateOf<Set<String>>(setOf()) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    val sharedViewModel: SharedViewModel = viewModel()
    val difficulties = sharedViewModel.difficulties.observeAsState().value
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.api-ninjas.com/v1/") // Replace with your API's base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService = retrofit.create(SearchApiService::class.java)

    // LaunchedEffect to initialize data
    LaunchedEffect(Unit) {
        sharedViewModel.initializeData(apiService)
    }


    db.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            workoutList = snapshot.children.mapNotNull { it.getValue(Workout::class.java) }.asReversed()
            isLoading = false
        }

        override fun onCancelled(error: DatabaseError) {
            errorMessage = "Failed to load workouts: ${error.message}"
            showErrorSnackbar = true
            isLoading = false
        }
    })

    @Composable
    fun CustomAppBar(
        onDeleteClick: () -> Unit,
        isAnyWorkoutSelected: Boolean
    ) {
        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Text(
                        text = "Favourite Workouts",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                    },
            navigationIcon = {
                if (isAnyWorkoutSelected) {
                    IconButton(
                        onClick = onDeleteClick
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete Selected Workouts",
                            modifier = Modifier.size(70.dp)
                        )
                    }
                }
            },
            actions = {
            }
        )
    }


    fun isValidDuration(duration: String): Boolean {
        // Regex to check if duration is in the format HH:MM:SS, allowing hours to exceed 23
        val regex = "^\\d{2,}:[0-5]\\d:[0-5]\\d$".toRegex()
        return duration.matches(regex)
    }

    fun isFormValid(workout: Workout): Boolean {
        showErrorSnackbar = false
        showConfirmationMessage = false
        if (workoutDate.isBlank() || !workoutDate.matches("\\d{4}-\\d{2}-\\d{2}".toRegex())) {
            errorMessage = "Enter a valid date in format YYYY-MM-DD"
        } else if (workout.type in listOf("cardio", "stretching") && workoutDuration.isBlank()) {
            errorMessage = "Duration is required for this workout type"
        } else if (workout.type in listOf("cardio", "stretching") && !isValidDuration(workoutDuration)) {
            errorMessage = "Enter a valid duration in format HH:MM:SS"
        } else if (workout.type in listOf("olympic_weightlifting", "plyometrics", "powerlifting", "strength", "strongman") && workoutSets.isBlank()) {
            errorMessage = "Sets are required for this workout type"
        } else if (workout.type in listOf("olympic_weightlifting", "plyometrics", "powerlifting", "strength", "strongman") && workoutSets.toIntOrNull() == null) {
            errorMessage = "Sets must be an integer"
        } else if (workout.type in listOf("olympic_weightlifting", "plyometrics", "powerlifting", "strength", "strongman") && workoutReps.isBlank()) {
            errorMessage = "Reps are required for this workout type"
        } else if (workout.type in listOf("olympic_weightlifting", "plyometrics", "powerlifting", "strength", "strongman") && workoutReps.toIntOrNull() == null) {
            errorMessage = "Reps must be an integer"
        } else {
            // Additional check for future date
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.isLenient = false
            errorMessage = try {
                val parsedDate = dateFormat.parse(workoutDate)
                val today = Calendar.getInstance().time
                if (parsedDate!!.after(today)) {
                    "You cannot log workouts in the future unless you are Marty McFly"
                } else {
                    return true
                }
            } catch (e: Exception) {
                "Enter a valid date in format YYYY-MM-DD"
            }
        }
        showErrorSnackbar = true
        return false
    }


    fun saveWorkoutLog(workout: Workout) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val logRef = FirebaseDatabase.getInstance().getReference("users/$userId/logs")
        val pointsRef = FirebaseDatabase.getInstance().getReference("users/$userId/points")
        val logId = logRef.push().key ?: return

        val workoutLog = WorkoutLog(
            id = logId,
            name = workout.name,
            type = workout.type,
            muscle = workout.muscle,
            difficulty = workout.difficulty,
            duration = workoutDuration,
            distance = workoutDistance,
            sets = workoutSets,
            reps = workoutReps,
            date = workoutDate
        )

        if (!isFormValid(workout)) return

        logRef.child(logId).setValue(workoutLog).addOnSuccessListener {
            // Save points data after successfully saving workout log
            val pointsData = mapOf(
                "id" to logId,
                "timestamp" to System.currentTimeMillis(),
                "name" to workout.name,
                "points" to 25 // Each logged workout gives 25 points
            )
            pointsRef.child(logId).setValue(pointsData)

            // Reset UI state and show confirmation
            showLogWorkoutDialog = false
            showConfirmationMessage = true
            showErrorSnackbar = false
            workoutName = ""
            workoutDuration = ""
            workoutDistance = ""
            workoutSets = ""
            workoutReps = ""
            workoutDate = LocalDate.now().toString()
        }.addOnFailureListener {
            errorMessage = "Failed to save log: ${it.message}"
            showErrorSnackbar = true
        }
    }

    fun deleteWorkout(workoutIndex: Int) {
        val workoutToDelete = workoutList[workoutIndex]
        db.orderByChild("name").equalTo(workoutToDelete.name).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    child.ref.removeValue()
                }
                showFullScreenDialog = false
            }

            override fun onCancelled(error: DatabaseError) {
                errorMessage = "Failed to delete workout: ${error.message}"
                showErrorSnackbar = true
            }
        })
    }

    // AlertDialog for deleting a workout
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
                    Text(
                        "Confirm",
                        color = MaterialTheme.colorScheme.surface
                    )
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(
                        "Cancel",
                        color = MaterialTheme.colorScheme.surface
                    )
                }
            }
        )
    }

    // Delete confirmation doalog for selected items.
    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text(
                "Confirm Deletion",
                color = MaterialTheme.colorScheme.primary
            )

                    },
            text = { Text("Are you sure you want to delete the selected workouts?") },
            confirmButton = {
                Button(onClick = {
                    // Deletion logic for selected workouts
                    selectedWorkouts.forEach { workoutId ->
                        db.child(workoutId).removeValue().addOnSuccessListener {
                            // Update the UI here if necessary
                        }.addOnFailureListener {
                            // Handle any errors
                            errorMessage = "Error deleting workout: ${it.message}"
                        }
                    }
                    // After deletion, refresh the workout list
                    db.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            workoutList = snapshot.children.mapNotNull { it.getValue(Workout::class.java) }.asReversed()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error
                        }
                    })
                    // Reset states after deletion
                    selectedWorkouts = setOf()
                    showDeleteConfirmationDialog = false
                    showConfirmationMessage = true  // Set the confirmation message to true
                }) {
                    Text(
                        "Confirm",
                        color = MaterialTheme.colorScheme.surface
                    )
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmationDialog = false }) {
                    Text(
                        "Cancel",
                        color = MaterialTheme.colorScheme.surface
                    )
                }
            }
        )
    }


    // Full-screen dialog for displaying selected workout details
    if (showFullScreenDialog && selectedWorkout != null) {
        Dialog(
            onDismissRequest = { showFullScreenDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    selectedWorkout?.let { workout ->
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ){
                        if (showConfirmationMessage) {
                            showErrorSnackbar = false
                            Snackbar(
                                action = {
                                    TextButton(onClick = {
                                        showConfirmationMessage = false
                                    }) {
                                        Text("OK")
                                    }
                                },
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text("Workout logged successfully")
                            }
                        }

                        if (showErrorSnackbar) {
                            showConfirmationMessage = false
                            Snackbar(
                                action = {
                                    TextButton(onClick = { showErrorSnackbar = false }) {
                                        Text("OK")
                                    }
                                },
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(errorMessage)
                            }
                        }
                        TopAppBar(
                            title = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {

                                    Text(
                                        text = workout.name.toTitleCase(),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        textAlign = TextAlign.Center
                                    )

                                }
                            },
                            navigationIcon = {
                                showTypeIcon(workout, 60, MaterialTheme.colorScheme.primary)
                            },
                            actions = {
                                IconButton(
                                    onClick = {
                                        showFullScreenDialog = false
                                        showConfirmationMessage = false
                                        showErrorSnackbar = false
                                    },
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                }
                            },
                            modifier = Modifier
                                .padding(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                    ){
                        val scrollState = rememberScrollState()

                            Column(
                                modifier = Modifier
                                    .verticalScroll(scrollState)
                                    .weight(1f),
                            ){
                                Row{
                                    showDifficultyIcon(workout, difficulties = difficulties, size = 50)

                                    showEquipmentIcon(workout, 40)

                                    // YouTube icon below the checkbox
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_youtube), // replace with your YouTube icon resource
                                        contentDescription = "Watch Video",
                                        modifier = Modifier
                                            .size(35.dp)
                                            .padding(top = 8.dp)
                                            .clickable {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    val videoId = searchYouTube(workout.name, "AIzaSyB_O7HAR-kuiAHEZf6VLNuOTWRi442Yza4")
                                                    withContext(Dispatchers.Main) {
                                                        if (videoId.isNotEmpty()) {
                                                            youTubeVideoId = videoId
                                                            showYouTubeDialog = true
                                                        } else {
                                                            // Handle case where no video is found
                                                        }
                                                    }
                                                }
                                            }
                                    )

                                    if (showYouTubeDialog) {
                                        YouTubeDialog(videoId = youTubeVideoId, onDismiss = { showYouTubeDialog = false })
                                    }
                                }


                                Text(
                                    text = "${workout.type.toTitleCase()}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = getTypeColor(workout.type)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Difficulty: ${workout.difficulty.toTitleCase()}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Muscle Group: ${workout.muscle.toTitleCase()}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Equipment: ${workout.equipment.toTitleCase()}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Instructions: ${workout.instructions}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ){

                                Icon(
                                    painter = painterResource(id = R.drawable.delete_note),
                                    contentDescription = "Delete Saved Workout",
                                    modifier = Modifier
                                        .size(50.dp)
                                        .padding(5.dp)
                                        .clickable {
                                            workoutToDeleteIndex = workoutList.indexOf(workout)
                                            showDialog = true
                                        },
                                    tint = MaterialTheme.colorScheme.error,
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Icon(
                                    painter = painterResource(id = R.drawable.workout),
                                    contentDescription = "Log Workout",
                                    modifier = Modifier
                                        .size(50.dp)
                                        .padding(5.dp)
                                        .clickable {
                                            showLogWorkoutDialog = true
                                        },
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }

                            if (showLogWorkoutDialog) {
                                Dialog(onDismissRequest = { showLogWorkoutDialog = false }) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                width = 2.dp,
                                                color = MaterialTheme.colorScheme.secondary,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .alpha(0.96f),
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.secondary,
                                    ) {
                                        Column(modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth()) {
                                            if (workout.type in listOf("cardio", "stretching")) {
                                                OutlinedTextField(
                                                    value = workoutDuration,
                                                    onValueChange = { workoutDuration = it },
                                                    label = { Text("Duration (hh:mm:ss)") }
                                                )
                                                if (workout.type == "cardio") {
                                                    OutlinedTextField(
                                                        value = workoutDistance,
                                                        onValueChange = { workoutDistance = it },
                                                        label = { Text("Distance (optional)") }
                                                    )
                                                }
                                            } else if (workout.type in listOf(
                                                    "olympic_weightlifting",
                                                    "plyometrics",
                                                    "powerlifting",
                                                    "strength",
                                                    "strongman"
                                                )
                                            ) {
                                                OutlinedTextField(
                                                    value = workoutSets,
                                                    onValueChange = { workoutSets = it },
                                                    label = { Text("Sets") }


                                                )
                                                OutlinedTextField(
                                                    value = workoutReps,
                                                    onValueChange = { workoutReps = it },
                                                    label = { Text("Reps") }
                                                )
                                            }
                                            OutlinedTextField(
                                                value = workoutDate,
                                                onValueChange = { workoutDate = it },
                                                label = { Text("Date Completed") }
                                            )
                                            Row {
                                                Button(onClick = { saveWorkoutLog(workout) }) {
                                                    Text(
                                                        "Save",
                                                        color = MaterialTheme.colorScheme.surface
                                                    )
                                                }
                                                Button(onClick = { showLogWorkoutDialog = false }) {
                                                    Text(
                                                        "Cancel",
                                                        color = MaterialTheme.colorScheme.surface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()){
        if(isLoading){
            //CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column{
                CustomAppBar(
                    onDeleteClick = { showDeleteConfirmationDialog = true },
                    isAnyWorkoutSelected = selectedWorkouts.isNotEmpty()
                )
                if (showConfirmationMessage && selectedWorkouts.isNotEmpty()) {
                    Snackbar(
                        action = {
                            TextButton(onClick = {
                                showConfirmationMessage = false
                            }) {
                                Text("OK")
                            }
                        },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Workout deleted successfully")
                    }
                } else if (showConfirmationMessage && selectedWorkouts.isEmpty()) {
                    Snackbar(
                        action = {
                            TextButton(onClick = {
                                showConfirmationMessage = false
                            }) {
                                Text("OK")
                            }
                        },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Selected Workouts Deleted Successfully")
                    }
                }

                if (showErrorSnackbar) {
                    showConfirmationMessage = false
                    Snackbar(
                        action = {
                            TextButton(onClick = { showErrorSnackbar = false }) {
                                Text("OK")
                            }
                        },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(errorMessage)
                    }
                }
                // Main content of the WorkoutScreen
                if (workoutList.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("No workouts found for your profile")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val intent = Intent(currentContext, SearchScreen::class.java)
                                currentContext.startActivity(intent)
                            },
                            shape = RoundedCornerShape(5.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Find Workouts")
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(top = 24.dp, bottom = 85.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        itemsIndexed(workoutList) { _, workout ->
                            Card(
                                modifier = Modifier
                                    .clickable {
                                        selectedWorkout = workout
                                        showConfirmationMessage = false
                                        showErrorSnackbar = false
                                        showFullScreenDialog = true
                                    }
                                    .widthIn(min = 390.dp, max = 390.dp)
                                    .alpha(0.85f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)){
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ){
                                        Column(modifier = Modifier.width(60.dp)) {

                                            showTypeIcon(workout, 60, MaterialTheme.colorScheme.primary)

                                        }
                                        Column(modifier = Modifier.width(245.dp)) {

                                            Text(
                                                text = "${workout.name.toTitleCase() }",
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = "${workout.muscle.toTitleCase() }",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Row{

                                                showDifficultyIcon(workout, difficulties = difficulties,size =50)

                                                showEquipmentIcon(workout, 40)

                                            }
                                        }
                                        Column {
                                            Checkbox(
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = getDifficultyColor(workout.difficulty), // Color when the Checkbox is checked
                                                    uncheckedColor = MaterialTheme.colorScheme.primary, // Color when the Checkbox is unchecked
                                                    checkmarkColor = MaterialTheme.colorScheme.background // Color of the checkmark
                                                ),
                                                checked = selectedWorkouts.contains(workout.id),
                                                onCheckedChange = { isChecked ->
                                                    val updatedSelection = selectedWorkouts.toMutableSet()
                                                    if (isChecked) {
                                                        updatedSelection.add(workout.id)
                                                    } else {
                                                        updatedSelection.remove(workout.id)
                                                    }
                                                    selectedWorkouts = updatedSelection
                                                }
                                            )

                                            // YouTube icon below the checkbox
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_youtube), // replace with your YouTube icon resource
                                                contentDescription = "Watch Video",
                                                modifier = Modifier
                                                    .size(35.dp)
                                                    .padding(top = 8.dp)
                                                    .clickable {
                                                        CoroutineScope(Dispatchers.IO).launch {
                                                            val videoId = searchYouTube(workout.name, "AIzaSyB_O7HAR-kuiAHEZf6VLNuOTWRi442Yza4")
                                                            withContext(Dispatchers.Main) {
                                                                if (videoId.isNotEmpty()) {
                                                                    youTubeVideoId = videoId
                                                                    showYouTubeDialog = true
                                                                } else {
                                                                    // Handle case where no video is found
                                                                }
                                                            }
                                                        }
                                                    }
                                            )

                                            if (showYouTubeDialog) {
                                                YouTubeDialog(videoId = youTubeVideoId, onDismiss = { showYouTubeDialog = false })
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun showEquipmentIcon(workout: Workout, size: Int) {
    val equipmentIconResId: Int? = when (workout.equipment) {
        "machine" -> equipment_machine
        "kettlebells" -> equipment_kettlebell
        "dumbbell" -> equipment_dumbbell
        "barbell" -> equipment_barbell
        else -> null
    }
    if (equipmentIconResId != null) {
        Image(
            painter = painterResource(equipmentIconResId),
            contentDescription = "Equipment Icon",
            colorFilter = ColorFilter.tint(
                MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.size(50.dp)
        )
    } else {
        val workoutName = workout.name
        val iconResId = when {
            workoutName?.contains("treadmill", ignoreCase = true) == true -> equipment_treadmill
            workoutName?.contains("bicycling", ignoreCase = true) == true -> equipment_bicycle
            workoutName?.contains("skating", ignoreCase = true) == true -> equipment_skating
            workoutName?.contains("Jumping rope", ignoreCase = true) == true -> equipment_jumprope
            workoutName?.contains("Rickshaw", ignoreCase = true) == true -> equipment_rickshaw
            workoutName?.contains("Plate", ignoreCase = true) == true -> equipment_weightplate
            // Add more cases for other string matches here
            else -> null // Set iconResId to null when there's no match
        }

        if (iconResId != null) {
            Image(
                painter = painterResource(iconResId),
                contentDescription = "Equipment Icon",
                colorFilter = ColorFilter.tint(
                    MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.size(size.dp)
            )
        } else {
            // Handle the case where there's no matching icon based on workout.name
            // Example:
            // Text("No Icon")
        }
    }
}

@Composable
fun showTypeIcon(workout: Workout, size: Int, color: Color) {
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
    Image(
        painter = painterResource(id = iconResId),
        contentDescription = "Workout Icon",
        colorFilter = ColorFilter.tint(
            color
        ),
        modifier = Modifier
            .size(size.dp)
    )
}

@Composable
fun showDifficultyIcon(workout: Workout, size: Int, difficulties: Array<String>?) {
    val difficultyIconResId = when (workout.difficulty) {
        difficulties?.getOrNull(0) -> R.drawable.easy
        difficulties?.getOrNull(1) -> R.drawable.medium
        difficulties?.getOrNull(2) -> R.drawable.hard
        else -> R.drawable.emh
    }

    Image(
        painter = painterResource(difficultyIconResId),
        contentDescription = "Difficulty Icon",
        modifier = Modifier.size(size.dp)
    )
}

@Composable
fun YouTubeDialog(videoId: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        // Request audio focus
        if (requestAudioFocus(context)) {
            AndroidView(factory = {
                YouTubePlayerView(it).apply {
                    addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            youTubePlayer.loadVideo(videoId, 0f)
                        }
                    })
                }
            })
        }
    }
}

suspend fun searchYouTube(workoutName: String, apiKey: String): String {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/youtube/v3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val youTubeApiService = retrofit.create(YouTubeApiService::class.java)
    val response = youTubeApiService.searchVideos(query = workoutName, apiKey = apiKey)

    return response.items.firstOrNull()?.id?.videoId ?: ""
}

fun requestAudioFocus(context: Context): Boolean {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val focusRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener { }
            .build()
        audioManager.requestAudioFocus(focusRequest)
    } else {
        @Suppress("DEPRECATION")
        audioManager.requestAudioFocus({ }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
    }
    return focusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
}


