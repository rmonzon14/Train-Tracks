package com.example.traintracks

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

class SignupScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrainTracksTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DisplaySignupScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUp(
    onSignUpClicked: (String, String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Box (
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.signup),
            contentDescription = null,

            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    drawRect(
                        color = Color.Black.copy(alpha = 0.8f),
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
        verticalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sign Up",
            color = Color.White,
            fontSize = 55.sp,
            fontWeight = FontWeight.ExtraBold
        )
        SignUpUsernameSection(
            value = username,
            onChange = { username = it },
        )
        SignUpPasswordSection(
            value = password,
            onChange = { password = it },
            submit = { onSignUpClicked(username, password, confirmPassword) },
        )
        SignUpConfirmPasswordSection(
            value = confirmPassword,
            onChange = { confirmPassword = it },
            submit = { onSignUpClicked(username, password, confirmPassword) },
        )
        Button(
            onClick = { onSignUpClicked(username, password, confirmPassword) },
            shape = RoundedCornerShape(5.dp),
            enabled = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.LightGray,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Welcome",
                fontSize = 20.sp,
                color = Color.Black
            )
        }

        AnnotatedClickableTextSignup()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpUsernameSection(
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
        placeholder = { Text("Enter your username", color = Color.White) },
        label = { Text("Username", color = Color.White) },
        visualTransformation = VisualTransformation.None,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(
            onNext = { setFocus.moveFocus(FocusDirection.Down) }
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpPasswordSection(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpConfirmPasswordSection(
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
                Icons.Default.Check,
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
        placeholder = { Text("Confirm your password", color = Color.White) },
        label = { Text("Confirm your Password", color = Color.White) },
        singleLine = true,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
    )
}

@Composable
fun AnnotatedClickableTextSignup() {
    val currentContext = LocalContext.current

    ClickableText(
        text = buildAnnotatedString {
            val fullString = "Already have an account? Login"
            val startIndex = fullString.indexOf("Login")
            val endIndex = startIndex + 5
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
                    color = Color.White,
                    textDecoration = TextDecoration.Underline,
                    fontSize = 18.sp
                ),
                start = startIndex, end = endIndex
            )
        },
        onClick = {
            val intent = Intent(currentContext, LoginScreen::class.java)
            currentContext.startActivity(intent)
        }
    )
}

@Preview
@Composable
fun DisplaySignupScreen() {
    var isSignedUp by remember { mutableStateOf(false) }

    if (isSignedUp) {
        // If successfully signed up, redirect to login page
    } else {
        SignUp { username, password, confirmPassword ->
            // Validation
        }
    }
}