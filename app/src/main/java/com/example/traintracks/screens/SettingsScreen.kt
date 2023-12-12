package com.example.traintracks.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardElevation
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.traintracks.WorkoutLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField

@Composable
fun SettingsScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val db = FirebaseDatabase.getInstance().getReference("users/$userId/logs")

    var workoutLogs by remember { mutableStateOf<List<WorkoutLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun refreshWorkoutLogs() {
        isLoading = true
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                workoutLogs = snapshot.children.mapNotNull { it.getValue(WorkoutLog::class.java) }
                    .asReversed()
                    .sortedByDescending { it.date }
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
                errorMessage = error.message
            }
        })
    }

    LaunchedEffect(Unit) {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                workoutLogs = snapshot.children.mapNotNull { it.getValue(WorkoutLog::class.java) }
                    .asReversed()
                    .sortedByDescending { it.date }
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
                // Handle the error
                errorMessage = error.message
            }
        })
    }

    val onDeleteSuccess = {
        // Refresh the workout logs list
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                workoutLogs = snapshot.children.mapNotNull { it.getValue(WorkoutLog::class.java) }
                    .asReversed()
                    .sortedByDescending { it.date }
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
                errorMessage = "Failed to refresh data: ${error.message}"
            }
        })
    }

    val onDeleteFailure = { error: String ->
        // Handle the error case
        errorMessage = error
    }

    // Error message Snackbar
    if (errorMessage != null) {
        Snackbar(
            action = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text(errorMessage!!)
        }
    }

    Surface(
        modifier = Modifier
            .padding(24.dp, bottom = 80.dp)
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item{
                        Text(
                            text = "Logged Workouts",
                            modifier = Modifier.padding(bottom = 16.dp, top = 16.dp),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(workoutLogs) { log ->
                        WorkoutLogCard(
                            log = log,
                            onDeleteSuccess = { refreshWorkoutLogs() },
                            onDeleteFailure = { errorMessage = it },
                            onEditSuccess = { refreshWorkoutLogs() }
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutLogCard(log: WorkoutLog, onDeleteSuccess: () -> Unit, onDeleteFailure: (String) -> Unit, onEditSuccess: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    fun deleteWorkoutLog(logId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().getReference("users/$userId/logs")

        db.child(logId).removeValue().addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onFailure(it.message ?: "An unknown error occurred")
        }
    }

    fun updateWorkoutLog(updatedLog: WorkoutLog, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().getReference("users/$userId/logs")

        db.child(updatedLog.id).setValue(updatedLog)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it.message ?: "An unknown error occurred")
            }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Workout Log") },
            text = { Text("Are you sure you want to delete this workout log?") },
            confirmButton = {
                Button(onClick = {
                    deleteWorkoutLog(log.id, onDeleteSuccess, onDeleteFailure)
                    showDeleteDialog = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit dialog
    if (showEditDialog) {
        var editedName by remember { mutableStateOf(log.name) }
        var editedSets by remember(log.sets.isNotBlank()) { mutableStateOf(log.sets) }
        var editedReps by remember(log.reps.isNotBlank()) { mutableStateOf(log.reps) }
        var editedDuration by remember(log.duration.isNotBlank()) { mutableStateOf(log.duration) }
        var editedDistance by remember(log.distance) { mutableStateOf(log.distance ?: "") }


        // Implement the dialog for editing
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Workout Log") },
            text = {
                Column {
                    // Name field
                    TextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Name") }
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
                    log.name = editedName
                    log.sets = editedSets
                    log.reps = editedReps
                    log.duration = editedDuration
                    log.distance = editedDistance

                    // Call the update function
                    updateWorkoutLog(log,
                        onSuccess = {
                            // Handle success
                            showEditDialog = false
                            onEditSuccess()
                        },
                        onFailure = { error ->
                            // Handle failure.
                            errorMessage = error
                        }
                    )
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
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
                Column {
                    // Workout log details
                    Text(
                        text = "${log.date}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "${log.name}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (log.sets.isNotBlank()) {
                        Text(
                            text = "Sets: ${log.sets}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (log.reps.isNotBlank()) {
                        Text(
                            text = "Reps: ${log.reps}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (log.duration.isNotBlank()) {
                        Text(
                            text = "Duration: ${log.duration}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    log.distance?.let { distance ->
                        if (distance.isNotBlank()) {
                            Text(
                                text = "Distance: $distance",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Column{
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Workout Log",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showEditDialog = true }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Workout Log",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showDeleteDialog = true }
                    )
                }
            }
            errorMessage?.let { msg ->
                Snackbar(
                    action = {
                        TextButton(onClick = { errorMessage = null }) {
                            Text("OK")
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(msg)
                }
            }

            successMessage?.let { msg ->
                Snackbar(
                    action = {
                        TextButton(onClick = { successMessage = null }) {
                            Text("OK")
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(msg)
                }
            }
        }
    }
}

