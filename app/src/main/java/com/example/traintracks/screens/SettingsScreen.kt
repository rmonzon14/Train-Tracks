package com.example.traintracks.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.traintracks.WorkoutLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.traintracks.R
import com.example.traintracks.SearchApiService
import com.google.android.gms.tasks.Tasks
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.semantics.SemanticsProperties.ImeAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val logsDbRef = FirebaseDatabase.getInstance().getReference("users/$userId/logs")
    val logId = logsDbRef.push().key ?: return
    val notesDbRef = FirebaseDatabase.getInstance().getReference("users/$userId/notes")
    var fetchError by remember { mutableStateOf<String?>(null) }
    var newWorkoutName by remember { mutableStateOf("") }
    var newWorkoutDate by remember { mutableStateOf("") }
    var newWorkoutType by remember { mutableStateOf("") }
    var newWorkoutMuscle by remember { mutableStateOf("") }
    var newWorkoutDuration by remember { mutableStateOf("") }
    var newWorkoutDistance by remember { mutableStateOf("") }
    var newWorkoutSets by remember { mutableStateOf("") }
    var newWorkoutReps by remember { mutableStateOf("") }
    var showNoteField by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    var dropDownTypeText by remember { mutableStateOf("") }
    var expandedTypes by remember { mutableStateOf(false) }
    var expandedWorkoutNames by remember { mutableStateOf(false) }
    var workoutLogs by remember { mutableStateOf<List<WorkoutLog>>(emptyList()) }
    var notesData by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val successMessage = remember { mutableStateOf<String?>(null) }
    var selectedWorkouts by remember { mutableStateOf<Set<String>>(setOf()) }
    var checkedStates by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    val isAnyWorkoutSelected = selectedWorkouts.isNotEmpty()
    var showAddWorkoutDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    val updateWorkoutLogsList: (List<WorkoutLog>) -> Unit = { newList ->
        workoutLogs = newList
    }
    val updateSelectedWorkouts: (Set<String>) -> Unit = { newSet ->
        selectedWorkouts = newSet
    }
    val sharedViewModel: SharedViewModel = viewModel()
    val types by sharedViewModel.types.observeAsState(arrayOf())
    val snackbarHostState = remember { SnackbarHostState() }

    @Composable
    fun CustomAppBar(
        onAddClick: () -> Unit,
        onDeleteClick: () -> Unit,
        isAnyWorkoutSelected: Boolean
    ) {
        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {

                    Text(
                        text = "Workout Session Log",
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
                // Plus icon - always visible
                IconButton(onClick = onAddClick) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add Workout",
                        modifier = Modifier.size(70.dp)
                    )
                }
            }
        )
    }

    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete the selected workouts?") },
            confirmButton = {
                Button(onClick = {
                    val selectedIds = selectedWorkouts
                    deleteSelectedWorkoutLogs(selectedIds, {
                        workoutLogs = it
                    }, successMessage, errorMessage, workoutLogs)
                    showDeleteConfirmationDialog = false
                }) {
                    Text(
                        "Confirm",
                        color = MaterialTheme.colorScheme.background
                    )
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmationDialog = false }) {
                    Text(
                        "Cancel",
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }
        )
    }


    if (showAddWorkoutDialog) {
        // State for workout names fetched from Firebase
        var workoutNames by remember { mutableStateOf<List<String>>(emptyList()) }

        fun fetchAndDisplayWorkoutNames() {
            val dataDbRef = FirebaseDatabase.getInstance().getReference("data")
            dataDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val namesList = mutableListOf<String>()
                    snapshot.children.forEach { child ->
                        val type = child.child("type").getValue(String::class.java)
                        if (type == newWorkoutType) {
                            val name = child.child("name").getValue(String::class.java)
                            name?.let { namesList.add(it) }
                        }
                    }
                    workoutNames = namesList
                }

                override fun onCancelled(error: DatabaseError) {
                    // Log and handle the error
                    Log.e("FirebaseError", "Error fetching workout names: ${error.message}")
                    fetchError = "Error fetching workout names. Please try again."
                }
            })
        }



        // Reset the dialog fields
        fun resetAddWorkoutDialogFields() {
            newWorkoutName = ""
            newWorkoutDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            newWorkoutType = ""
            dropDownTypeText = ""
            newWorkoutDuration = ""
            newWorkoutDistance = ""
            newWorkoutSets = ""
            newWorkoutReps = ""
            noteText = ""
            showNoteField = false
            workoutNames = emptyList()
        }

        resetAddWorkoutDialogFields()

        AlertDialog(
            onDismissRequest = { showAddWorkoutDialog = false },
            title = { Text("Log Workout") },
            text = {
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .requiredHeight(height = 395.dp)
                ) {
                    TextField(value = newWorkoutDate, onValueChange = { newWorkoutDate = it }, label = { Text("Date") })

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dropdown for Workout Type
                    CustomDropDownMenu(
                        selectedText = dropDownTypeText,
                        group = types,
                        onItemSelected = { selected ->
                            dropDownTypeText = selected
                            newWorkoutType = selected
                            // Fetch workout names based on selected type from Firebase
                            fetchAndDisplayWorkoutNames() // Fetch names when type changes

                        },
                        expanded = expandedTypes,
                        onExpandedChange = { expandedTypes = it },
                        label = "Workout Type"
                    )

                    // Workout Name dropdown, shown only when a type is selected
                    if (newWorkoutType.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CustomDropDownMenu(
                            selectedText = newWorkoutName,
                            group = workoutNames.toTypedArray(),
                            onItemSelected = { selected -> newWorkoutName = selected },
                            expanded = expandedWorkoutNames,
                            onExpandedChange = { expandedWorkoutNames = it },
                            label = "Workout Name"
                        )
                    }

                    // Conditional fields based on workout type
                    when (newWorkoutType) {

                        "cardio" -> {
                            newWorkoutSets = ""
                            newWorkoutReps = ""

                            Spacer(modifier = Modifier.height(8.dp))

                            TextField(
                                value = newWorkoutDuration,
                                onValueChange = { newWorkoutDuration = it },
                                label = { Text("Duration (HH:MM:SS)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        var isInputValid = true
                                        val errorMessages = mutableListOf<String>()

                                        // Validate Name
                                        if (newWorkoutName.isBlank()) {
                                            errorMessages.add("Workout name cannot be blank.")
                                            isInputValid = false
                                        }

                                        // Validate Date
                                        val (isDateValid, dateErrorMsg) = isValidDate(newWorkoutDate)
                                        if (!isDateValid) {
                                            errorMessages.add(dateErrorMsg)
                                            isInputValid = false
                                        }

                                        // Validate Duration for certain workout types
                                        if (newWorkoutType in listOf("cardio", "stretching") && !isValidDuration(newWorkoutDuration).first) {
                                            errorMessages.add(isValidDuration(newWorkoutDuration).second)
                                            isInputValid = false
                                        }

                                        // Validate Sets and Reps for certain workout types
                                        if (newWorkoutType in listOf("olympic_weightlifting", "powerlifting", "strongman", "strength", "plyometrics")) {
                                            val (isSetsValid, setsErrorMsg) = isValidIntField("", newWorkoutSets, "Sets")
                                            if (!isSetsValid) {
                                                errorMessages.add(setsErrorMsg)
                                                isInputValid = false
                                            }

                                            val (isRepsValid, repsErrorMsg) = isValidIntField("", newWorkoutReps, "Reps")
                                            if (!isRepsValid) {
                                                errorMessages.add(repsErrorMsg)
                                                isInputValid = false
                                            }
                                        }

                                        // Validate Note
                                        if (showNoteField && noteText.isBlank()) {
                                            errorMessages.add("Note cannot be blank.")
                                            isInputValid = false
                                        }

                                        if (!isInputValid) {
                                            // Display the first error message
                                            errorMessage.value = errorMessages.firstOrNull()
                                            return@KeyboardActions
                                        }

                                        // Proceed to save the workout log, notes, and points
                                        val newWorkoutLog = WorkoutLog(
                                            id = logId,
                                            name = newWorkoutName,
                                            type = newWorkoutType,
                                            muscle = newWorkoutMuscle,
                                            difficulty = "user entry", // Default difficulty
                                            duration = newWorkoutDuration,
                                            distance = newWorkoutDistance.ifBlank { null },
                                            sets = newWorkoutSets,
                                            reps = newWorkoutReps,
                                            date = newWorkoutDate
                                        )

                                        logsDbRef.child(logId).setValue(newWorkoutLog).addOnSuccessListener {
                                            if (showNoteField && noteText.isNotBlank()) {
                                                // Construct the note data with an id field
                                                val noteData = mapOf(
                                                    "id" to logId,
                                                    "note" to noteText
                                                )
                                                notesDbRef.child(logId).setValue(noteData)
                                            }

                                            val pointsData = mapOf(
                                                "id" to logId,
                                                "timestamp" to System.currentTimeMillis(),
                                                "name" to newWorkoutName,
                                                "points" to 25 // Points for the workout
                                            )
                                            val pointsDbRef = FirebaseDatabase.getInstance().getReference("users/$userId/points")
                                            pointsDbRef.child(logId).setValue(pointsData)

                                            // Update the UI and close the dialog
                                            updateWorkoutLogsList(workoutLogs + listOf(newWorkoutLog))
                                            successMessage.value = "Workout added successfully."
                                            showAddWorkoutDialog = false
                                            resetAddWorkoutDialogFields()
                                        }.addOnFailureListener {
                                            errorMessage.value = "Error saving workout: ${it.message}"
                                        }
                                    }
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            TextField(
                                value = newWorkoutDistance,
                                onValueChange = { newWorkoutDistance = it },
                                label = { Text("Distance (km)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        var isInputValid = true
                                        val errorMessages = mutableListOf<String>()

                                        // Validate Name
                                        if (newWorkoutName.isBlank()) {
                                            errorMessages.add("Workout name cannot be blank.")
                                            isInputValid = false
                                        }

                                        // Validate Date
                                        val (isDateValid, dateErrorMsg) = isValidDate(newWorkoutDate)
                                        if (!isDateValid) {
                                            errorMessages.add(dateErrorMsg)
                                            isInputValid = false
                                        }

                                        // Validate Duration for certain workout types
                                        if (newWorkoutType in listOf("cardio", "stretching") && !isValidDuration(newWorkoutDuration).first) {
                                            errorMessages.add(isValidDuration(newWorkoutDuration).second)
                                            isInputValid = false
                                        }

                                        // Validate Sets and Reps for certain workout types
                                        if (newWorkoutType in listOf("olympic_weightlifting", "powerlifting", "strongman", "strength", "plyometrics")) {
                                            val (isSetsValid, setsErrorMsg) = isValidIntField("", newWorkoutSets, "Sets")
                                            if (!isSetsValid) {
                                                errorMessages.add(setsErrorMsg)
                                                isInputValid = false
                                            }

                                            val (isRepsValid, repsErrorMsg) = isValidIntField("", newWorkoutReps, "Reps")
                                            if (!isRepsValid) {
                                                errorMessages.add(repsErrorMsg)
                                                isInputValid = false
                                            }
                                        }

                                        // Validate Note
                                        if (showNoteField && noteText.isBlank()) {
                                            errorMessages.add("Note cannot be blank.")
                                            isInputValid = false
                                        }

                                        if (!isInputValid) {
                                            // Display the first error message
                                            errorMessage.value = errorMessages.firstOrNull()
                                            return@KeyboardActions
                                        }

                                        // Proceed to save the workout log, notes, and points
                                        val newWorkoutLog = WorkoutLog(
                                            id = logId,
                                            name = newWorkoutName,
                                            type = newWorkoutType,
                                            muscle = newWorkoutMuscle,
                                            difficulty = "user entry", // Default difficulty
                                            duration = newWorkoutDuration,
                                            distance = newWorkoutDistance.ifBlank { null },
                                            sets = newWorkoutSets,
                                            reps = newWorkoutReps,
                                            date = newWorkoutDate
                                        )

                                        logsDbRef.child(logId).setValue(newWorkoutLog).addOnSuccessListener {
                                            if (showNoteField && noteText.isNotBlank()) {
                                                // Construct the note data with an id field
                                                val noteData = mapOf(
                                                    "id" to logId,
                                                    "note" to noteText
                                                )
                                                notesDbRef.child(logId).setValue(noteData)
                                            }

                                            val pointsData = mapOf(
                                                "id" to logId,
                                                "timestamp" to System.currentTimeMillis(),
                                                "name" to newWorkoutName,
                                                "points" to 25 // Points for the workout
                                            )
                                            val pointsDbRef = FirebaseDatabase.getInstance().getReference("users/$userId/points")
                                            pointsDbRef.child(logId).setValue(pointsData)

                                            // Update the UI and close the dialog
                                            updateWorkoutLogsList(workoutLogs + listOf(newWorkoutLog))
                                            successMessage.value = "Workout added successfully."
                                            showAddWorkoutDialog = false
                                            resetAddWorkoutDialogFields()
                                        }.addOnFailureListener {
                                            errorMessage.value = "Error saving workout: ${it.message}"
                                        }
                                    }
                                )
                            )
                        }
                        "stretching" -> {
                            newWorkoutDistance = ""
                            newWorkoutSets = ""
                            newWorkoutReps = ""
                            Spacer(modifier = Modifier.height(8.dp))

                            TextField(
                                value = newWorkoutDuration,
                                onValueChange = { newWorkoutDuration = it },
                                label = { Text("Duration (HH:MM:SS)") }
                            )
                        }
                        "olympic_weightlifting", "powerlifting", "strongman", "strength", "plyometrics" -> {
                            newWorkoutDistance = ""
                            newWorkoutDuration = ""
                            Spacer(modifier = Modifier.height(8.dp))

                            TextField(
                                value = newWorkoutSets,
                                onValueChange = { newWorkoutSets = it },
                                label = { Text("Sets") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        var isInputValid = true
                                        val errorMessages = mutableListOf<String>()

                                        // Validate Name
                                        if (newWorkoutName.isBlank()) {
                                            errorMessages.add("Workout name cannot be blank.")
                                            isInputValid = false
                                        }

                                        // Validate Date
                                        val (isDateValid, dateErrorMsg) = isValidDate(newWorkoutDate)
                                        if (!isDateValid) {
                                            errorMessages.add(dateErrorMsg)
                                            isInputValid = false
                                        }

                                        // Validate Duration for certain workout types
                                        if (newWorkoutType in listOf("cardio", "stretching") && !isValidDuration(newWorkoutDuration).first) {
                                            errorMessages.add(isValidDuration(newWorkoutDuration).second)
                                            isInputValid = false
                                        }

                                        // Validate Sets and Reps for certain workout types
                                        if (newWorkoutType in listOf("olympic_weightlifting", "powerlifting", "strongman", "strength", "plyometrics")) {
                                            val (isSetsValid, setsErrorMsg) = isValidIntField("", newWorkoutSets, "Sets")
                                            if (!isSetsValid) {
                                                errorMessages.add(setsErrorMsg)
                                                isInputValid = false
                                            }

                                            val (isRepsValid, repsErrorMsg) = isValidIntField("", newWorkoutReps, "Reps")
                                            if (!isRepsValid) {
                                                errorMessages.add(repsErrorMsg)
                                                isInputValid = false
                                            }
                                        }

                                        // Validate Note
                                        if (showNoteField && noteText.isBlank()) {
                                            errorMessages.add("Note cannot be blank.")
                                            isInputValid = false
                                        }

                                        if (!isInputValid) {
                                            // Display the first error message
                                            errorMessage.value = errorMessages.firstOrNull()
                                            return@KeyboardActions
                                        }

                                        // Proceed to save the workout log, notes, and points
                                        val newWorkoutLog = WorkoutLog(
                                            id = logId,
                                            name = newWorkoutName,
                                            type = newWorkoutType,
                                            muscle = newWorkoutMuscle,
                                            difficulty = "user entry", // Default difficulty
                                            duration = newWorkoutDuration,
                                            distance = newWorkoutDistance.ifBlank { null },
                                            sets = newWorkoutSets,
                                            reps = newWorkoutReps,
                                            date = newWorkoutDate
                                        )

                                        logsDbRef.child(logId).setValue(newWorkoutLog).addOnSuccessListener {
                                            if (showNoteField && noteText.isNotBlank()) {
                                                // Construct the note data with an id field
                                                val noteData = mapOf(
                                                    "id" to logId,
                                                    "note" to noteText
                                                )
                                                notesDbRef.child(logId).setValue(noteData)
                                            }

                                            val pointsData = mapOf(
                                                "id" to logId,
                                                "timestamp" to System.currentTimeMillis(),
                                                "name" to newWorkoutName,
                                                "points" to 25 // Points for the workout
                                            )
                                            val pointsDbRef = FirebaseDatabase.getInstance().getReference("users/$userId/points")
                                            pointsDbRef.child(logId).setValue(pointsData)

                                            // Update the UI and close the dialog
                                            updateWorkoutLogsList(workoutLogs + listOf(newWorkoutLog))
                                            successMessage.value = "Workout added successfully."
                                            showAddWorkoutDialog = false
                                            resetAddWorkoutDialogFields()
                                        }.addOnFailureListener {
                                            errorMessage.value = "Error saving workout: ${it.message}"
                                        }
                                    }
                                )

                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            TextField(
                                value = newWorkoutReps,
                                onValueChange = { newWorkoutReps = it },
                                singleLine = true,
                                label = { Text("Reps") },
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        var isInputValid = true
                                        val errorMessages = mutableListOf<String>()

                                        // Validate Name
                                        if (newWorkoutName.isBlank()) {
                                            errorMessages.add("Workout name cannot be blank.")
                                            isInputValid = false
                                        }

                                        // Validate Date
                                        val (isDateValid, dateErrorMsg) = isValidDate(newWorkoutDate)
                                        if (!isDateValid) {
                                            errorMessages.add(dateErrorMsg)
                                            isInputValid = false
                                        }

                                        // Validate Duration for certain workout types
                                        if (newWorkoutType in listOf("cardio", "stretching") && !isValidDuration(newWorkoutDuration).first) {
                                            errorMessages.add(isValidDuration(newWorkoutDuration).second)
                                            isInputValid = false
                                        }

                                        // Validate Sets and Reps for certain workout types
                                        if (newWorkoutType in listOf("olympic_weightlifting", "powerlifting", "strongman", "strength", "plyometrics")) {
                                            val (isSetsValid, setsErrorMsg) = isValidIntField("", newWorkoutSets, "Sets")
                                            if (!isSetsValid) {
                                                errorMessages.add(setsErrorMsg)
                                                isInputValid = false
                                            }

                                            val (isRepsValid, repsErrorMsg) = isValidIntField("", newWorkoutReps, "Reps")
                                            if (!isRepsValid) {
                                                errorMessages.add(repsErrorMsg)
                                                isInputValid = false
                                            }
                                        }

                                        // Validate Note
                                        if (showNoteField && noteText.isBlank()) {
                                            errorMessages.add("Note cannot be blank.")
                                            isInputValid = false
                                        }

                                        if (!isInputValid) {
                                            // Display the first error message
                                            errorMessage.value = errorMessages.firstOrNull()
                                            return@KeyboardActions
                                        }

                                        // Proceed to save the workout log, notes, and points
                                        val newWorkoutLog = WorkoutLog(
                                            id = logId,
                                            name = newWorkoutName,
                                            type = newWorkoutType,
                                            muscle = newWorkoutMuscle,
                                            difficulty = "user entry", // Default difficulty
                                            duration = newWorkoutDuration,
                                            distance = newWorkoutDistance.ifBlank { null },
                                            sets = newWorkoutSets,
                                            reps = newWorkoutReps,
                                            date = newWorkoutDate
                                        )

                                        logsDbRef.child(logId).setValue(newWorkoutLog).addOnSuccessListener {
                                            if (showNoteField && noteText.isNotBlank()) {
                                                // Construct the note data with an id field
                                                val noteData = mapOf(
                                                    "id" to logId,
                                                    "note" to noteText
                                                )
                                                notesDbRef.child(logId).setValue(noteData)
                                            }

                                            val pointsData = mapOf(
                                                "id" to logId,
                                                "timestamp" to System.currentTimeMillis(),
                                                "name" to newWorkoutName,
                                                "points" to 25 // Points for the workout
                                            )
                                            val pointsDbRef = FirebaseDatabase.getInstance().getReference("users/$userId/points")
                                            pointsDbRef.child(logId).setValue(pointsData)

                                            // Update the UI and close the dialog
                                            updateWorkoutLogsList(workoutLogs + listOf(newWorkoutLog))
                                            successMessage.value = "Workout added successfully."
                                            showAddWorkoutDialog = false
                                            resetAddWorkoutDialogFields()
                                        }.addOnFailureListener {
                                            errorMessage.value = "Error saving workout: ${it.message}"
                                        }
                                    }
                                )
                            )
                        }
                    }

                    // Note field
                    if (showNoteField) {
                        Spacer(modifier = Modifier.height(8.dp))

                        TextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            label = { Text("Note") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = androidx.compose.ui.text.input.ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    var isInputValid = true
                                    val errorMessages = mutableListOf<String>()

                                    // Validate Name
                                    if (newWorkoutName.isBlank()) {
                                        errorMessages.add("Workout name cannot be blank.")
                                        isInputValid = false
                                    }

                                    // Validate Date
                                    val (isDateValid, dateErrorMsg) = isValidDate(newWorkoutDate)
                                    if (!isDateValid) {
                                        errorMessages.add(dateErrorMsg)
                                        isInputValid = false
                                    }

                                    // Validate Duration for certain workout types
                                    if (newWorkoutType in listOf("cardio", "stretching") && !isValidDuration(newWorkoutDuration).first) {
                                        errorMessages.add(isValidDuration(newWorkoutDuration).second)
                                        isInputValid = false
                                    }

                                    // Validate Sets and Reps for certain workout types
                                    if (newWorkoutType in listOf("olympic_weightlifting", "powerlifting", "strongman", "strength", "plyometrics")) {
                                        val (isSetsValid, setsErrorMsg) = isValidIntField("", newWorkoutSets, "Sets")
                                        if (!isSetsValid) {
                                            errorMessages.add(setsErrorMsg)
                                            isInputValid = false
                                        }

                                        val (isRepsValid, repsErrorMsg) = isValidIntField("", newWorkoutReps, "Reps")
                                        if (!isRepsValid) {
                                            errorMessages.add(repsErrorMsg)
                                            isInputValid = false
                                        }
                                    }

                                    // Validate Note
                                    if (showNoteField && noteText.isBlank()) {
                                        errorMessages.add("Note cannot be blank.")
                                        isInputValid = false
                                    }

                                    if (!isInputValid) {
                                        // Display the first error message
                                        errorMessage.value = errorMessages.firstOrNull()
                                        return@KeyboardActions
                                    }

                                    // Proceed to save the workout log, notes, and points
                                    val newWorkoutLog = WorkoutLog(
                                        id = logId,
                                        name = newWorkoutName,
                                        type = newWorkoutType,
                                        muscle = newWorkoutMuscle,
                                        difficulty = "user entry", // Default difficulty
                                        duration = newWorkoutDuration,
                                        distance = newWorkoutDistance.ifBlank { null },
                                        sets = newWorkoutSets,
                                        reps = newWorkoutReps,
                                        date = newWorkoutDate
                                    )

                                    logsDbRef.child(logId).setValue(newWorkoutLog).addOnSuccessListener {
                                        if (showNoteField && noteText.isNotBlank()) {
                                            // Construct the note data with an id field
                                            val noteData = mapOf(
                                                "id" to logId,
                                                "note" to noteText
                                            )
                                            notesDbRef.child(logId).setValue(noteData)
                                        }

                                        val pointsData = mapOf(
                                            "id" to logId,
                                            "timestamp" to System.currentTimeMillis(),
                                            "name" to newWorkoutName,
                                            "points" to 25 // Points for the workout
                                        )
                                        val pointsDbRef = FirebaseDatabase.getInstance().getReference("users/$userId/points")
                                        pointsDbRef.child(logId).setValue(pointsData)

                                        // Update the UI and close the dialog
                                        updateWorkoutLogsList(workoutLogs + listOf(newWorkoutLog))
                                        successMessage.value = "Workout added successfully."
                                        showAddWorkoutDialog = false
                                        resetAddWorkoutDialogFields()
                                    }.addOnFailureListener {
                                        errorMessage.value = "Error saving workout: ${it.message}"
                                    }
                                }
                            )
                        )
                    }else {
                        noteText = ""
                    }

                    // Toggle icon for showing/hiding note field
                    IconToggleButton(
                        checked = showNoteField,
                        onCheckedChange = { showNoteField = !showNoteField }
                    ) {
                        Icon(
                            imageVector = if (showNoteField) Icons.Default.Delete else Icons.Default.Add,
                            contentDescription = if (showNoteField) "Hide Note" else "Add Note",
                            modifier = Modifier.size(20.dp)
                        )
                    }


                }
            },
            confirmButton = {
                Button(onClick = {
                    var isInputValid = true
                    val errorMessages = mutableListOf<String>()

                    // Validate Name
                    if (newWorkoutName.isBlank()) {
                        errorMessages.add("Workout name cannot be blank.")
                        isInputValid = false
                    }

                    // Validate Date
                    val (isDateValid, dateErrorMsg) = isValidDate(newWorkoutDate)
                    if (!isDateValid) {
                        errorMessages.add(dateErrorMsg)
                        isInputValid = false
                    }

                    // Validate Duration for certain workout types
                    if (newWorkoutType in listOf("cardio", "stretching") && !isValidDuration(newWorkoutDuration).first) {
                        errorMessages.add(isValidDuration(newWorkoutDuration).second)
                        isInputValid = false
                    }

                    // Validate Sets and Reps for certain workout types
                    if (newWorkoutType in listOf("olympic_weightlifting", "powerlifting", "strongman", "strength", "plyometrics")) {
                        val (isSetsValid, setsErrorMsg) = isValidIntField("", newWorkoutSets, "Sets")
                        if (!isSetsValid) {
                            errorMessages.add(setsErrorMsg)
                            isInputValid = false
                        }

                        val (isRepsValid, repsErrorMsg) = isValidIntField("", newWorkoutReps, "Reps")
                        if (!isRepsValid) {
                            errorMessages.add(repsErrorMsg)
                            isInputValid = false
                        }
                    }

                    // Validate Note
                    if (showNoteField && noteText.isBlank()) {
                        errorMessages.add("Note cannot be blank.")
                        isInputValid = false
                    }

                    if (!isInputValid) {
                        // Display the first error message
                        errorMessage.value = errorMessages.firstOrNull()
                        return@Button
                    }

                    // Proceed to save the workout log, notes, and points
                    val newWorkoutLog = WorkoutLog(
                        id = logId,
                        name = newWorkoutName,
                        type = newWorkoutType,
                        muscle = newWorkoutMuscle,
                        difficulty = "user entry", // Default difficulty
                        duration = newWorkoutDuration,
                        distance = newWorkoutDistance.ifBlank { null },
                        sets = newWorkoutSets,
                        reps = newWorkoutReps,
                        date = newWorkoutDate
                    )

                    logsDbRef.child(logId).setValue(newWorkoutLog).addOnSuccessListener {
                        if (showNoteField && noteText.isNotBlank()) {
                            // Construct the note data with an id field
                            val noteData = mapOf(
                                "id" to logId,
                                "note" to noteText
                            )
                            notesDbRef.child(logId).setValue(noteData)
                        }

                        val pointsData = mapOf(
                            "id" to logId,
                            "timestamp" to System.currentTimeMillis(),
                            "name" to newWorkoutName,
                            "points" to 25 // Points for the workout
                        )
                        val pointsDbRef = FirebaseDatabase.getInstance().getReference("users/$userId/points")
                        pointsDbRef.child(logId).setValue(pointsData)

                        // Update the UI and close the dialog
                        updateWorkoutLogsList(workoutLogs + listOf(newWorkoutLog))
                        successMessage.value = "Workout added successfully."
                        showAddWorkoutDialog = false
                        resetAddWorkoutDialogFields()
                    }.addOnFailureListener {
                        errorMessage.value = "Error saving workout: ${it.message}"
                    }
                }) {
                    Text(
                        "Save",
                        color = MaterialTheme.colorScheme.background
                    )
                }
            },
            dismissButton = {
                Button(onClick = { showAddWorkoutDialog = false }) {
                    Text(
                        "Cancel",
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }
        )
    }

    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.api-ninjas.com/v1/") // Replace with your API's base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService = retrofit.create(SearchApiService::class.java)

    LaunchedEffect(Unit) {
        logsDbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                sharedViewModel.initializeData(apiService)
                workoutLogs = snapshot.children.mapNotNull { it.getValue(WorkoutLog::class.java) }
                    .asReversed()
                    .sortedByDescending { it.date }
                isLoading = false

            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
                errorMessage.value = error.message
            }
        })

        notesDbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notesMap = mutableMapOf<String, String>()
                snapshot.children.forEach { child ->
                    val noteId = child.child("id").getValue(String::class.java) ?: ""
                    val noteText = child.child("note").getValue(String::class.java) ?: ""
                    notesMap[noteId] = noteText
                }
                notesData = notesMap.toMap()
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
                // Display Error Message
                errorMessage.value = error.message
            }
        })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            //CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Surface(
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                SnackbarHost(hostState = snackbarHostState)
                Column {

                    // Snackbar for displaying errors
                    if (errorMessage.value != null) {
                        successMessage.value = null
                        Snackbar(
                            action = {
                                TextButton(onClick = { errorMessage.value = null }) {
                                    Text("OK")
                                }
                            },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(errorMessage.value!!)
                        }
                    }
                    // Snackbar for displaying success message
                    if (successMessage.value != null) {
                        errorMessage.value = null
                        Snackbar(
                            action = {
                                TextButton(onClick = { successMessage.value = null }) {
                                    Text("OK")
                                }
                            },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(successMessage.value!!)
                        }
                    }

                    CustomAppBar(
                        onAddClick = {
                            showAddWorkoutDialog = true
                                     },
                        onDeleteClick = { showDeleteConfirmationDialog = true },
                        isAnyWorkoutSelected = isAnyWorkoutSelected
                    )

                    LazyColumn(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(workoutLogs) { log ->
                            WorkoutLogCard(
                                log = log,
                                isChecked = checkedStates[log.id] ?: false,
                                onCheckedChange = { isChecked ->
                                    val updatedCheckedStates = checkedStates.toMutableMap().apply {
                                        this[log.id] = isChecked
                                    }
                                    checkedStates = updatedCheckedStates
                                    updateSelectedWorkouts(updatedCheckedStates.filterValues { it }.keys)
                                },
                                noteText = notesData[log.id] ?: "",
                                successMessageState = successMessage,
                                errorMessageState = errorMessage,
                            )
                        }

                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutLogCard(
    log: WorkoutLog,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    noteText: String,
    successMessageState: MutableState<String?>,
    errorMessageState: MutableState<String?>,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showDeleteNoteDialog by remember { mutableStateOf(false) }
    var showEditNoteDialog by remember { mutableStateOf(false) }
    var editNoteText by remember { mutableStateOf(noteText) }
    var editedName by remember { mutableStateOf(log.name) }
    var editedSets by remember(log.sets.isNotBlank()) { mutableStateOf(log.sets) }
    var editedReps by remember(log.reps.isNotBlank()) { mutableStateOf(log.reps) }
    var editedDate by remember { mutableStateOf(log.date) }
    var editedDuration by remember(log.duration.isNotBlank()) { mutableStateOf(log.duration) }
    var editedDistance by remember(log.distance) { mutableStateOf(log.distance ?: "") }
    var noteInputText by remember { mutableStateOf("") }
    val hasNote = noteText.isNotEmpty()



    fun resetForm() {
        // Reset error messages
        errorMessageState.value = null
        successMessageState.value = null

        // Reset form fields to the current log's values
        editedName = log.name
        editedSets = log.sets
        editedReps = log.reps
        editedDate = log.date
        editedDuration = log.duration
        editedDistance = log.distance ?: ""
    }

    fun deleteWorkoutLog(
        logId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
        successMessageState: MutableState<String?>,
        errorMessageState: MutableState<String?>
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val logsDbRef = FirebaseDatabase.getInstance().getReference("users/$userId/logs")
        val pointsDbRef = FirebaseDatabase.getInstance().getReference("users/$userId/points")
        val notesDbRef = FirebaseDatabase.getInstance().getReference("users/$userId/notes")

        val task1 = logsDbRef.child(logId).removeValue()
        val task2 = pointsDbRef.child(logId).removeValue()
        val task3 = notesDbRef.child(logId).removeValue()

        // Use the Firebase Tasks API to handle all deletions concurrently
        Tasks.whenAll(task1, task2, task3).addOnSuccessListener {
            successMessageState.value = "Workout log and related data successfully deleted."
            errorMessageState.value = null
            onSuccess()
        }.addOnFailureListener {
            errorMessageState.value = it.message ?: "An error occurred during deletion"
            successMessageState.value = null
            onFailure(it.message ?: "An unknown error occurred")
        }
    }

    fun updateWorkoutLog(
        updatedLog: WorkoutLog,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
        successMessageState: MutableState<String?>,
        errorMessageState: MutableState<String?>
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().getReference("users/$userId/logs")

        db.child(updatedLog.id).setValue(updatedLog)
            .addOnSuccessListener {
                onSuccess()
                successMessageState.value = "Workout log updated successfully."
                errorMessageState.value = null
            }
            .addOnFailureListener {
                onFailure(it.message ?: "An unknown error occurred")
                errorMessageState.value = it.message
                successMessageState.value = null
            }
    }

    fun validateInputs(): Boolean {
        val (isDateValid, dateValidationMsg) = isValidDate(editedDate)
        // Reset error messages
        errorMessageState.value = null
        successMessageState.value = null

        // Date Validation
        if (!isDateValid) {
            errorMessageState.value = dateValidationMsg
            return false
        }

        // Sets Validation - only if sets were originally provided
        if (log.sets.isNotBlank()) {
            val (isSetsValid, setsValidationMsg) = isValidIntField(log.sets, editedSets, "Sets")
            if (!isSetsValid) {
                errorMessageState.value = setsValidationMsg
                return false
            }
        }

        // Reps Validation - similar to Sets
        if (log.reps.isNotBlank()) {
            val (isRepsValid, repsValidationMsg) = isValidIntField(log.reps, editedReps, "Reps")
            if (!isRepsValid) {
                errorMessageState.value = repsValidationMsg
                return false
            }
        }

        // Duration Validation - only if duration was originally provided
        if (log.duration.isNotBlank()) {
            val (isDurationValid, durationValidationMsg) = isValidDuration(editedDuration)
            if (!isDurationValid) {
                errorMessageState.value = durationValidationMsg
                return false
            }
        }

        // Distance Validation - similar to Duration
        if (log.distance != null && log.distance!!.isNotBlank()) {
            val (isDistanceValid, distanceValidationMsg) = isValidDistance(
                log.distance,
                editedDistance
            )
            if (!isDistanceValid) {
                errorMessageState.value = distanceValidationMsg
                return false
            }
        }

        // Additional checks if required
        // ...

        return true
    }

    fun updateWorkoutLogIfValid() {
        if (validateInputs()) {
            log.date = editedDate
            log.name = editedName
            log.sets = editedSets
            log.reps = editedReps
            log.duration = editedDuration
            log.distance = editedDistance

            updateWorkoutLog(
                log,
                onSuccess = { showEditDialog = false },
                onFailure = { error -> errorMessageState.value = error },
                successMessageState,
                errorMessageState
            )
        }
    }

    fun saveNoteToFirebase(
        logId: String,
        noteText: String,
        successMessageState: MutableState<String?>,
        errorMessageState: MutableState<String?>
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val notesDbRef = FirebaseDatabase.getInstance().getReference("users/$userId/notes")

        val note = mapOf(
            "id" to logId,
            "note" to noteText
        )
        notesDbRef.child(logId).setValue(note).addOnSuccessListener {
            successMessageState.value = "Note added successfully."
        }.addOnFailureListener {
            errorMessageState.value = "Failed to add note: ${it.message}"
        }
    }

    fun deleteNoteFromFirebase(
        logId: String,
        successMessageState: MutableState<String?>,
        errorMessageState: MutableState<String?>
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val notesDbRef = FirebaseDatabase.getInstance().getReference("users/$userId/notes")

        notesDbRef.child(logId).removeValue().addOnSuccessListener {
            successMessageState.value = "Note deleted successfully."
        }.addOnFailureListener {
            errorMessageState.value = "Failed to delete note: ${it.message}"
        }
    }

    fun updateNoteFromFirebase(
        logId: String,
        updatedNoteText: String,
        successMessageState: MutableState<String?>,
        errorMessageState: MutableState<String?>
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val notesDbRef = FirebaseDatabase.getInstance().getReference("users/$userId/notes")

        val updatedNote = mapOf(
            "id" to logId,
            "note" to updatedNoteText
        )
        notesDbRef.child(logId).setValue(updatedNote).addOnSuccessListener {
            successMessageState.value = "Note updated successfully."
        }.addOnFailureListener {
            errorMessageState.value = "Failed to update note: ${it.message}"
        }
    }

    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("Add a Note") },
            text = {
                TextField(
                    value = noteInputText,
                    onValueChange = { if (it.length <= 250) noteInputText = it },
                    label = { Text("Note") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (noteInputText.isNotBlank()) {
                        saveNoteToFirebase(
                            log.id,
                            noteInputText,
                            successMessageState,
                            errorMessageState
                        )
                        showAddNoteDialog = false
                    } else {
                        errorMessageState.value = "Note cannot be empty"
                    }
                }) {
                    Text(
                        text = "Save",
                        color = MaterialTheme.colorScheme.background
                    )
                }
            },
            dismissButton = {
                Button(onClick = { showAddNoteDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }
        )
    }

    // Dialog for deleting a note
    if (showDeleteNoteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteNoteDialog = false },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                Button(onClick = {
                    deleteNoteFromFirebase(
                        log.id,
                        successMessageState,
                        errorMessageState
                    )
                    showDeleteNoteDialog = false
                }) {
                    Text(
                        "Confirm",
                        color = MaterialTheme.colorScheme.background
                    )
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteNoteDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }
        )
    }

    // Dialog for editing a note
    if (showEditNoteDialog) {
        AlertDialog(
            onDismissRequest = { showEditNoteDialog = false },
            title = { Text("Edit Note") },
            text = {
                TextField(
                    value = editNoteText,
                    onValueChange = { if (it.length <= 250) editNoteText = it },
                    label = { Text("Note") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    updateNoteFromFirebase(
                        log.id,
                        editNoteText,
                        successMessageState,
                        errorMessageState
                    )
                    showEditNoteDialog = false
                }) {
                    Text(
                        text = "Save",
                        color = MaterialTheme.colorScheme.background
                    )
                }
            },
            dismissButton = {
                Button(onClick = { showEditNoteDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }
        )
    }



    if (showDeleteDialog) {


        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Workout Log") },
            text = { Text("Are you sure you want to delete this workout log?") },
            confirmButton = {
                Button(onClick = {
                    deleteWorkoutLog(
                        log.id,
                        onSuccess = { showDeleteDialog = false },
                        onFailure = { errorMessage -> errorMessageState.value = errorMessage },
                        successMessageState,
                        errorMessageState
                    )
                }) {
                    Text(
                        "Confirm",
                        color = MaterialTheme.colorScheme.background
                    )
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }
        )
    }

    // Edit dialog
    if (showEditDialog) {

        // Dialog for editing
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(log.name) },
            text = {
                Column {
                    // Display date of logged workout
                    TextField(
                        value = editedDate,
                        onValueChange = { editedDate = it },
                        label = { Text("Date") }
                    )
                    // Conditionally show Sets field
                    if (log.sets.isNotBlank()) {
                        TextField(
                            value = editedSets,
                            onValueChange = { editedSets = it },
                            label = { Text("Sets") }
                        )
                    }

                    // Conditionally show Reps field
                    if (log.reps.isNotBlank()) {
                        TextField(
                            value = editedReps,
                            onValueChange = { editedReps = it },
                            label = { Text("Reps") }
                        )
                    }

                    // Conditionally show Duration field
                    if (log.duration.isNotBlank()) {
                        TextField(
                            value = editedDuration,
                            onValueChange = { editedDuration = it },
                            label = { Text("Duration") }
                        )
                    }

                    // Conditionally show Distance field
                    if (log.distance != null && log.distance!!.isNotBlank()) {
                        TextField(
                            value = editedDistance,
                            onValueChange = { editedDistance = it },
                            label = { Text("Distance") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    // Call validation
                    val isValid = validateInputs()
                    if (isValid) {
                        updateWorkoutLogIfValid()
                    } else {
                        // You can log here or set a breakpoint to verify this branch is reached
                        println("Validation failed: ${errorMessageState.value}")
                    }
                }) {
                    Text(
                        text = "Save",
                        color = MaterialTheme.colorScheme.background
                    )
                }

            },
            dismissButton = {
                Button(onClick = { showEditDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.85f)
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.widthIn(min = 55.dp, max = 55.dp),
                ) {
                    Checkbox(
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary, // Color when the Checkbox is checked
                            uncheckedColor = MaterialTheme.colorScheme.primary, // Color when the Checkbox is unchecked
                            checkmarkColor = MaterialTheme.colorScheme.background  // Color of the checkmark
                        ),
                        checked = isChecked,
                        onCheckedChange = onCheckedChange
                    )
                    val iconResId = when (log.type) {
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
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.size(55.dp)
                    )
                }
                Column(modifier = Modifier.widthIn(min = 225.dp, max = 225.dp)) {
                    // Workout log details
                    Text(
                        text = log.date,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Text(
                        text = log.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (log.sets.isNotBlank()) {
                        Text(
                            text = "Sets: ${log.sets}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (log.reps.isNotBlank()) {
                        Text(
                            text = "Reps: ${log.reps}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (log.duration.isNotBlank()) {
                        Text(
                            text = "Duration: ${log.duration}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    log.distance?.let { distance ->
                        if (distance.isNotBlank()) {
                            Text(
                                text = "Distance: $distance",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (hasNote) {
                        Text(
                            text = noteText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Column {
                    if (!hasNote) {
                        Icon(
                            painter = painterResource(id = R.drawable.add_note),
                            contentDescription = "Add Note",
                            modifier = Modifier
                                .size(30.dp)
                                .clickable { showAddNoteDialog = true }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Workout Log",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                resetForm()
                                showEditDialog = true
                            }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Workout Log",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { showDeleteDialog = true }
                    )

                    if (hasNote) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Icon(
                            painter = painterResource(id = R.drawable.edit_note),
                            contentDescription = "Edit Note",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    editNoteText =
                                        noteText // Set the current note text before opening the dialog
                                    showEditNoteDialog = true
                                }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Icon(
                            painter = painterResource(id = R.drawable.delete_note),
                            contentDescription = "Delete Note",
                            modifier = Modifier
                                .size(26.dp)
                                .clickable { showDeleteNoteDialog = true }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

    }
}
fun deleteSelectedWorkoutLogs(
    selectedIds: Set<String>,
    updateWorkoutLogs: (List<WorkoutLog>) -> Unit,
    successMessage: MutableState<String?>,
    errorMessage: MutableState<String?>,
    workoutLogs: List<WorkoutLog>
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val logsDbRef = FirebaseDatabase.getInstance().getReference("users/$userId/logs")
    val pointsDbRef = FirebaseDatabase.getInstance().getReference("users/$userId/points")
    val notesDbRef = FirebaseDatabase.getInstance().getReference("users/$userId/notes")

    // Track completion of all delete operations
    val deleteTasks = selectedIds.flatMap { logId ->
        listOf(
            logsDbRef.child(logId).removeValue(),
            pointsDbRef.child(logId).removeValue(),
            notesDbRef.child(logId).removeValue()
        )
    }

    // Wait for all delete operations to complete
    Tasks.whenAllSuccess<Void>(deleteTasks).addOnSuccessListener {
        // Once all deletes are successful, update the local state
        val updatedList = workoutLogs.filterNot { it.id in selectedIds }
        updateWorkoutLogs(updatedList)
        successMessage.value = "Selected workout logs successfully deleted."
    }.addOnFailureListener {
        errorMessage.value = it.message ?: "An error occurred during deletion"
    }
}

fun isValidDuration(editedDuration: String): Pair<Boolean, String> {
    // Check if the duration is blank, which is allowed
    if (editedDuration.isBlank()) {
        return Pair(true, "")
    }

    // Regex for HH:MM:SS format
    val regex = "^\\d{2,}:[0-5]\\d:[0-5]\\d$".toRegex()

    // Check if the edited duration matches the regex
    return if (editedDuration.matches(regex)) {
        Pair(true, "")
    } else {
        Pair(false, "Invalid duration format. Please use HH:MM:SS.")
    }
}

fun isValidDate(dateStr: String): Pair<Boolean, String> {
    // Check if the date is blank
    if (dateStr.isBlank()) {
        return Pair(false, "Date cannot be blank")
    }

    // Check if the date matches the strict "YYYY-MM-DD" format
    if (!dateStr.matches("\\d{4}-\\d{2}-\\d{2}".toRegex())) {
        return Pair(false, "Invalid date format. Please use YYYY-MM-DD.")
    }

    // Parse the date and check if it's in the future
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CANADA)
    dateFormat.isLenient = false
    return try {
        val parsedDate = dateFormat.parse(dateStr)
        val today = Calendar.getInstance().time
        if (parsedDate!!.after(today)) {
            Pair(false, "You cannot log workouts in the future unless you are Marty McFly")
        } else {
            Pair(true, "")
        }
    } catch (e: Exception) {
        Pair(false, "Invalid date format. Please use YYYY-MM-DD.")
    }
}

fun isValidIntField(
    originalValue: String,
    editedValue: String,
    fieldName: String
): Pair<Boolean, String> {
    // Check if the field is required but blank
    if (originalValue.isNotBlank() && editedValue.isBlank()) {
        return Pair(false, "$fieldName cannot be blank")
    }
    // Check if the field contains a valid integer
    return try {
        editedValue.toInt()
        Pair(true, "")
    } catch (e: NumberFormatException) {
        Pair(false, "$fieldName must be an integer")
    }
}

fun isValidDistance(
    originalDistance: String?,
    editedDistance: String
): Pair<Boolean, String> {
    if (!originalDistance.isNullOrBlank() && editedDistance.isBlank()) {
        return Pair(false, "Distance cannot be blank")
    }
    return Pair(true, "")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropDownMenu(
    selectedText: String,
    group: Array<String>,
    onItemSelected: (String) -> Unit,
    expanded: Boolean,
    label: String,
    onExpandedChange: (Boolean) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        TextField(
            value = selectedText.toTitleCase(),
            onValueChange = { },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            label = { Text(label, fontSize = 16.sp) },
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            group.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item.toTitleCase(), fontSize = 16.sp) },
                    onClick = {
                        onItemSelected(item)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}
