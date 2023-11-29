package com.example.traintracks.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.traintracks.ui.theme.TrainTracksTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private lateinit var auth : FirebaseAuth

@Composable
fun ProfileScreen(navController: NavController) {
    var auth = Firebase.auth
    var currentUser = auth.currentUser

    val currentContext = LocalContext.current

    if (currentUser == null) {
        val intent = Intent(currentContext, LoginScreen::class.java)
        currentContext.startActivity(intent)
    }

    Column(
        Modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement =  Arrangement.spacedBy(12.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                auth.signOut()

                val intent = Intent(currentContext, LoginScreen::class.java)
                currentContext.startActivity(intent)
            },
            shape = RoundedCornerShape(5.dp),
            enabled = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.LightGray,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ){
            Text(
                text = "Logout"
            )
        }
    }
}
