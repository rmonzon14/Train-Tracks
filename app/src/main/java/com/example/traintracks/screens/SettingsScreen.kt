package com.example.traintracks.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.traintracks.R
import com.google.android.gms.tasks.Tasks
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun SettingsScreen() {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val logsDbRef = FirebaseDatabase.getInstance().getReference("users/$userId/logs")
    val notesDbRef = FirebaseDatabase.getInstance().getReference("users/$userId/notes")

    var workoutLogs by remember { mutableStateOf<List<WorkoutLog>>(emptyList()) }
    var notesData by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val successMessage = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        logsDbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
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
        if(isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        else{
            Surface(
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column {
                    // Snackbar for displaying errors
                    if (errorMessage.value != null) {
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
                    LazyColumn(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item{ Text(
                            text = "Logged Workouts",
                            modifier = Modifier.padding(bottom = 16.dp, top = 16.dp),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        }
                        items(workoutLogs) { log ->
                            WorkoutLogCard(
                                log = log,
                                noteText = notesData[log.id] ?: "",
                                successMessageState = successMessage,
                                errorMessageState = errorMessage
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
    noteText: String,
    successMessageState: MutableState<String?>,
    errorMessageState: MutableState<String?>
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


    fun deleteWorkoutLog(logId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
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

    fun updateWorkoutLog(updatedLog: WorkoutLog, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
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

    fun isValidIntField(originalValue: String, editedValue: String, fieldName: String): Pair<Boolean, String> {
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

    fun isValidDistance(originalDistance: String?, editedDistance: String): Pair<Boolean, String> {
        if (!originalDistance.isNullOrBlank() && editedDistance.isBlank()) {
            return Pair(false, "Distance cannot be blank")
        }
        return Pair(true, "")
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
            val (isDistanceValid, distanceValidationMsg) = isValidDistance(log.distance, editedDistance)
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

            updateWorkoutLog(log, onSuccess = {
                showEditDialog = false
            }, onFailure = { error ->
                errorMessageState.value = error
                successMessageState.value = null
            })
        }
    }

    fun saveNoteToFirebase(logId: String, noteText: String) {
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

    fun deleteNoteFromFirebase(logId: String, successMessageState: MutableState<String?>, errorMessageState: MutableState<String?>) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val notesDbRef = FirebaseDatabase.getInstance().getReference("users/$userId/notes")

        notesDbRef.child(logId).removeValue().addOnSuccessListener {
            successMessageState.value = "Note deleted successfully."
        }.addOnFailureListener {
            errorMessageState.value = "Failed to delete note: ${it.message}"
        }
    }

    fun updateNoteFromFirebase(logId: String, updatedNoteText: String, successMessageState: MutableState<String?>, errorMessageState: MutableState<String?>) {
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

    fun getDifficultyColor(difficulty: String): Color {
        val darkGreen = Color(0xFF006400)
        val darkOrange = Color(0xFFCC8400)
        val darkRed = Color(0xFFCD5C5C)
        return when (difficulty) {
            "beginner" -> darkGreen
            "intermediate" -> darkOrange
            "expert" -> darkRed
            else -> Color.Gray
        }
    }

    fun getTypeColor(type: String): Color {
        return when (type) {
            "cardio" -> Color(0xFF800000) // Silver
            "olympic_weightlifting" -> Color(0xFFFF8F00) // Amber
            "plyometrics" -> Color(0xFFF124AA) // Magenta
            "powerlifting" -> Color(0xFF1A237E) // Deep Blue
            "strength" -> Color(0xFF9C27B0) // Deep Purple
            "stretching" -> Color(0xFF008B8B) // Teal
            "strongman" -> Color(0xFF6D4C41) // Brown
            else -> Color.Gray
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
                        // Save note to Firebase using noteInputText
                        saveNoteToFirebase(log.id, noteInputText)
                        showAddNoteDialog = false
                    } else {
                        // Show error message
                        errorMessageState.value = "Note cannot be empty"
                    }
                }) {
                    Text(
                        text = "Save",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            },
            dismissButton = {
                Button(onClick = { showAddNoteDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.secondary
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
                    deleteNoteFromFirebase(log.id, successMessageState, errorMessageState)
                    showDeleteNoteDialog = false
                }) {
                    Text(
                        text = "Confirm",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteNoteDialog = false }) {
                    Text(
                       text = "Cancel",
                       color = MaterialTheme.colorScheme.secondary
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
                    updateNoteFromFirebase(log.id, editNoteText, successMessageState, errorMessageState)
                    showEditNoteDialog = false
                }) {
                    Text(
                        text = "Save",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            },
            dismissButton = {
                Button(onClick = { showEditNoteDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.secondary
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
                    deleteWorkoutLog(log.id, onSuccess = {
                        showDeleteDialog = false
                    }, onFailure = {
                        showDeleteDialog = false
                        errorMessageState.value = it
                    })
                }) {
                    Text(
                        text = "Confirm",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.secondary
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
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

            },
            dismissButton = {
                Button(onClick = { showEditDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.widthIn(min = 50.dp, max = 50.dp)){
                    val iconResId = when (log.type) {
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
                }
                Column(modifier = Modifier.widthIn(min = 225.dp, max = 225.dp)){
                    // Workout log details
                    Text(
                        text = log.date,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = log.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = getTypeColor(log.type)
                    )

                    Text(
                        text = log.difficulty,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = getDifficultyColor(log.difficulty)
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

                Column{
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
                                    editNoteText = noteText // Set the current note text before opening the dialog
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
}




