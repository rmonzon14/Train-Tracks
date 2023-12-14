package com.example.traintracks.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

@Composable
fun ProfileScreen() {
    val auth = Firebase.auth
    val currentUser = auth.currentUser
    val currentContext = LocalContext.current
    val totalPoints = remember { mutableStateOf(0) }
    val level = remember { mutableStateOf(1) }
    val levelMessage = remember { mutableStateOf("Welcome to your fitness journey!") }

    if (currentUser == null) {
        val intent = Intent(currentContext, LoginScreen::class.java)
        currentContext.startActivity(intent)
    } else {
        val userId = currentUser.uid
        val pointsRef = FirebaseDatabase.getInstance().getReference("users/$userId/points")

        LaunchedEffect(key1 = Unit) {
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
                    // Handle the error if needed
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
        Text(
            text = "${currentUser?.email ?: "User"}",
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
                contentColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(Icons.Default.Person, contentDescription = "Logout Icon")
            Spacer(Modifier.width(8.dp))
            Text("Logout", fontWeight = FontWeight.Bold)
        }
    }
}