package com.example.traintracks.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.traintracks.ui.theme.TrainTracksTheme
import androidx.compose.runtime.*
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.example.traintracks.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginScreen : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrainTracksTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DisplayLoginScreen()
                }
            }
        }
    }
}
@Composable
fun Login(
    onLoginClicked: (String, String) -> Unit,

) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }



    val MyCustomFont = FontFamily(
        Font(R.font.crimson)
    )

    Box (
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.login),
            contentDescription = null,

            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    drawRect(
                        color = Color.Black.copy(alpha = 0.6f),
                        blendMode = BlendMode.Darken
                    )
                },
            contentScale = ContentScale.Crop,
        )
    }

    Column(
        Modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement =  Arrangement.spacedBy(12.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Icon(
            painter = painterResource(id = R.drawable.baseline_fitness_center_24),
            contentDescription = "",
            tint = Color.White,
            modifier = Modifier
                .size(105.dp)
        )
        Text (
            text = "TrainTracks",
            color = Color.White,
            fontSize = 55.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = MyCustomFont
        )
        EmailSection(
            value = username,
            onChange = { username = it },
        )
        PasswordSection(
            value = email,
            onChange = { email = it },
            submit = { onLoginClicked(email, email)  },
        )
        Button(
            onClick = { onLoginClicked(username, email) },
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
                text = "Login",
                fontSize = 20.sp,
                color = Color.White
            )
        }

        AnnotatedClickableTextLogin()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailSection(
    value: String,
    onChange: (String) -> Unit
) {
    val setFocus = LocalFocusManager.current

    TextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent,
            textColor = Color.White,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.primary
        ),
        singleLine = true,
        leadingIcon = @Composable {
            Icon(
                Icons.Default.Person,
                contentDescription = "",
                tint = Color.White
            )
        },
        placeholder = { Text("Enter your email", color = Color.White) },
        label = { Text("Email", color = Color.White) },
        visualTransformation = VisualTransformation.None,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(
            onNext = { setFocus.moveFocus(FocusDirection.Down) }
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordSection(
    value: String,
    onChange: (String) -> Unit,
    submit: () -> Unit,
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    TextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent,
            textColor = Color.White,
        ),
        leadingIcon = @Composable {
            Icon(
                Icons.Default.Lock,
                contentDescription = "",
                tint = Color.White
            )
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Password
        ),
        keyboardActions = KeyboardActions(
            onDone = { submit() }
        ),
        placeholder = { Text("Enter your password", color = Color.White) },
        label = { Text("Password", color = Color.White) },
        singleLine = true,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
    )
}

@Composable
fun AnnotatedClickableTextLogin() {
    val currentContext = LocalContext.current

    ClickableText(
        text = buildAnnotatedString {
            val fullString = "Don't have an account? Create one"
            val startIndex = fullString.indexOf("Create one")
            val endIndex = startIndex + 10
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp
                )
            ) {
                append(fullString)
            }
            addStyle(
                style = SpanStyle(
                    textDecoration = TextDecoration.Underline,
                    color = Color.White,
                    fontSize = 18.sp
                ),
                start = startIndex, end = endIndex
            )
        },
        onClick = {
            val intent = Intent(currentContext, SignupScreen::class.java)
            currentContext.startActivity(intent)
        }
    )
}

@Preview
@Composable
fun DisplayLoginScreen() {
    var isLoggedIn by remember { mutableStateOf(false) }

    var auth = Firebase.auth
    var currentUser = auth.currentUser

    val currentContext = LocalContext.current

    if (isLoggedIn || currentUser != null) {
        val intent = Intent(currentContext, MainActivity::class.java)
        currentContext.startActivity(intent)
    } else {
        Login { email, password ->
            when {
                email.isEmpty() -> {
                    Toast.makeText(currentContext, "Empty Email", Toast.LENGTH_SHORT).show()
                }

                password.isEmpty() -> {
                    Toast.makeText(currentContext, "Empty Password", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener() { task ->
                            if (task.isSuccessful) {
                                isLoggedIn = true;
                            } else {
                                Log.i("Check_Point", "signInWithEmail:failure", task.exception)
                            }
                        }
                }
            }
        }
    }
}
