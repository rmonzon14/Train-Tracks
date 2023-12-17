package com.example.traintracks.screens

import android.content.Intent
import android.graphics.drawable.shapes.PathShape
import android.system.Os.close
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.traintracks.WorkoutLog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Random
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ProfileScreen() {
    val auth = Firebase.auth
    val currentUser = auth.currentUser
    val currentContext = LocalContext.current
    val totalPoints = remember { mutableStateOf(0) }
    val level = remember { mutableStateOf(1) }
    val levelMessage = remember { mutableStateOf("Welcome to your fitness journey!") }
    val workoutLogs = remember { mutableStateOf<List<WorkoutLog>>(listOf()) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Function to calculate percentages of workout types
    fun calculateWorkoutTypePercentages(logs: List<WorkoutLog>): Map<String, Double> {
        val totalCount = logs.size.toDouble()
        return logs.groupingBy { it.type }
            .eachCount()
            .mapValues { (it.value / totalCount) * 100 }
    }

    // Function to get a random message for a given workout type
    fun getRandomMessageForWorkoutType(workoutType: String): String {
        val messages = workoutTypeMessages[workoutType] ?: return "Keep up the good work!"
        return messages.random()
    }

    val workoutTypePercentages = calculateWorkoutTypePercentages(workoutLogs.value)
    val pieChartData = workoutTypePercentages.mapValues { it.value.toFloat() }
    val topWorkoutType = workoutTypePercentages.maxByOrNull { it.value }?.key ?: ""

    if (currentUser == null) {
        val intent = Intent(currentContext, LoginScreen::class.java)
        currentContext.startActivity(intent)
        return
    } else {
        val userId = currentUser.uid
        val pointsRef = FirebaseDatabase.getInstance().getReference("users/$userId/points")
        val logsRef = FirebaseDatabase.getInstance().getReference("users/$userId/logs")

        LaunchedEffect(key1 = Unit) {
            logsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val fetchedLogs = snapshot.children.mapNotNull { it.getValue(WorkoutLog::class.java) }
                    workoutLogs.value = fetchedLogs
                    val workoutTypePercentages = calculateWorkoutTypePercentages(fetchedLogs)
                    val topWorkoutType = workoutTypePercentages.maxByOrNull { it.value }?.key ?: ""
                    levelMessage.value = getRandomMessageForWorkoutType(topWorkoutType)
                }

                override fun onCancelled(error: DatabaseError) {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar("Error: Unable to fetch workout logs")
                    }
                }
            })

            pointsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var pointsSum = 0
                    snapshot.children.forEach { child ->
                        val points = child.child("points").getValue(Int::class.java) ?: 0
                        pointsSum += points
                    }
                    totalPoints.value = pointsSum

                    // Calculate level based on points
                    level.value = (pointsSum / 100) + 1
                    levelMessage.value = when (level.value) {
                        1 -> "Off to a great start! Keep pushing yourself!"
                        2 -> "Level up! You're taking this seriously, and it shows."
                        3 -> "Fantastic work! Your dedication is inspiring."
                        4 -> "Level 4 already? You’re on fire!"
                        5 -> "Halfway to the top! Your progress is amazing."
                        6 -> "Unstoppable! Level 6 is just a stepping stone to greater heights."
                        7 -> "Lucky 7! Your commitment is paying off in spades."
                        8 -> "Level 8 achieved! You're an inspiration to many."
                        9 -> "Just one step away from the top! Your journey is awe-inspiring."
                        10 -> "You have reached the highest level! You are as fit as a fiddle!"
                        else -> "Continue your fitness journey!"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar("Error: Unable to fetch workout logs")
                    }
                }
            })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Your Workout Summary", style = MaterialTheme.typography.headlineLarge)

        workoutTypePercentages.forEach { (type, percentage) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$type: ", Modifier.weight(1f))
                LinearProgressIndicator(
                    progress = percentage.toFloat() / 100,
                    modifier = Modifier
                        .height(20.dp)
                        .weight(2f)
                )
                Text("${percentage.toInt()}%")
            }
        }

        Spacer(Modifier.height(12.dp))

        // Display the custom message for the top workout type
        val message = getRandomMessageForWorkoutType(topWorkoutType)
        Text(message, style = MaterialTheme.typography.bodyLarge)

        Spacer(Modifier.height(12.dp))

        Text(
            text = currentUser?.email ?: "User",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(12.dp))

        // Display the total points and level
        Text(
            text = "Total Points: ${totalPoints.value}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Level: ${level.value}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = levelMessage.value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 19.sp
        )

        Spacer(Modifier.height(30.dp))

        Button(
            onClick = {
                auth.signOut()
                val intent = Intent(currentContext, LoginScreen::class.java)
                currentContext.startActivity(intent)
            },
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.background
            )
        ) {
            Icon(Icons.Default.Person, contentDescription = "Logout Icon", tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text(
                "Logout",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.background
            )
        }
    }
    SnackbarHost(hostState = snackbarHostState)
}

val workoutTypeMessages = mapOf(
    "cardio" to listOf("Wow, you are really into cardio, huh?", "Cardio king!", "Running like the wind!", "Your stamina must be through the roof!", "Heart and health first!"),
    "olympic_weightlifting" to listOf("Lifting is your thing!", "Olympian in the making!", "Flex those muscles!", "You're a powerhouse!", "Olympic dreams are made of these."),
    "plyometrics" to listOf("Jumping higher every day!", "Plyometrics powerhouse!", "Leaping towards success!", "Jump around, jump up, jump up and get down!", "Spring into action!"),
    "powerlifting" to listOf("Powerlifting pro!", "Stronger every day!", "Lifting your way to the top!", "Power up!", "You're lifting like a champion!"),
    "strength" to listOf("Strength is your forte!", "Building muscles!", "Muscle up!", "You're stronger than yesterday!", "Strong mind, strong body!"),
    "stretching" to listOf("Flexibility is key!", "Stretch it out!", "Bend, don't break!", "Flexibility for longevity!", "Your flexibility is inspiring!"),
    "strongman" to listOf("Strongman superstar!", "Strength of a titan!", "Carrying the weight of the world with ease!", "Strongman, strong will!", "Your strength knows no bounds!"),
)