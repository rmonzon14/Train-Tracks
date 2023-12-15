package com.example.traintracks.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.traintracks.R
import com.example.traintracks.SearchApiService
import com.example.traintracks.ui.theme.TrainTracksTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchScreen : ComponentActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var db  : DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrainTracksTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SearchScreenContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(
    onSearchClicked: (String, String, String, String) -> Unit
) {
    var workoutName by remember { mutableStateOf("") }
    var workoutType by remember { mutableStateOf("") }
    var muscleGroup by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("") }
    val currentContext = LocalContext.current

    Box (
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.search),
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

        TopAppBar(
            title = {
                Text(text = "Go Back")
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        val intent = Intent(currentContext, MainActivity::class.java)
                        currentContext.startActivity(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        Text(
            text = "Workout Search",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold
        )
        WorkoutNameField(
            value = workoutName,
            onChange = { workoutName = it },
        )
        WorkoutTypeField(
            value = workoutType,
            onChange = { workoutType = it },
        )
        MuscleGroupField(
            value = muscleGroup,
            onChange = { muscleGroup = it },
        )
        DifficultyField(
            value = difficulty,
            onChange = { difficulty = it },
        )
        Button(
            onClick = { onSearchClicked(workoutName, workoutType, muscleGroup, difficulty) },
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
                text = "Search",
                fontSize = 20.sp,
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutNameField(
    value: String,
    onChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val leadingIcon = @Composable {
        Icon(
            Icons.Default.Edit,
            contentDescription = "",
            tint = Color.White
        )
    }

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
        leadingIcon = leadingIcon,
        placeholder = { Text("Enter workout name", color = Color.White) },
        label = { Text("Workout Name (Optional)", color = Color.White) },
        visualTransformation = VisualTransformation.None,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTypeField(
    value: String,
    onChange: (String) -> Unit
) {
    val workoutType = arrayOf(
            "cardio",
            "olympic_weightlifting",
            "plyometrics",
            "powerlifting",
            "strength",
            "stretching",
            "strongman")
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(value) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            TextField(
                value = selectedText,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                label = { Text("Workout Type") },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                workoutType.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            selectedText = item
                            expanded = false
                            onChange(item)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuscleGroupField(
    value: String,
    onChange: (String) -> Unit
) {
    val muscle = arrayOf(
            "abdominals",
            "abductors",
            "adductors",
            "biceps",
            "calves",
            "chest",
            "forearms",
            "glutes",
            "hamstrings",
            "lats",
            "lower_back",
            "middle_back",
            "neck",
            "quadriceps",
            "traps",
            "triceps")
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(value) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            TextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                label = { Text("Muscle Group") },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .verticalScroll(rememberScrollState())

            ) {
                muscle.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            selectedText = item
                            expanded = false
                            onChange(item)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DifficultyField(
    value: String,
    onChange: (String) -> Unit
) {
    val difficulty = arrayOf("Beginner", "Intermediate", "Expert")
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(value) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            TextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                label = { Text("Difficulty") },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                difficulty.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            selectedText = item
                            expanded = false
                            onChange(item)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Results(searchResults: List<com.example.traintracks.SearchResult>) {
    val currentContext = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TopAppBar(
            title = {
                Text(text = "Go Home")
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        val intent = Intent(currentContext, MainActivity::class.java)
                        currentContext.startActivity(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )
        Text(
            text = "Results Screen",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Click on workout items for more details.",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(top=16.dp, bottom = 16.dp)
        )

        if (searchResults.isNotEmpty()) {
            // Display search results
            searchResults.forEach { result ->
                SearchResultItem(result = result)
            }
        } else {
            // Display a message when there are no search results
            Text(
                text = "No results found",
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
@Composable
fun SearchResultItem(result: com.example.traintracks.SearchResult) {
    val auth = Firebase.auth
    val currentUser = auth.currentUser
    var isAddedToDatabase by remember { mutableStateOf(false) }

    LaunchedEffect(result) {
        val userId = currentUser?.uid
        if (userId != null) {
            val db = FirebaseDatabase.getInstance().getReference("users/$userId/workouts")
            db.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.children.any { it.child("name").value == result.name }) {
                        isAddedToDatabase = true
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(color = Color.DarkGray)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val iconResId = when (result.type) {
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
                modifier = Modifier.size(30.dp)
            )
            Text(
                text = result.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = result.type,
                fontSize = 18.sp,
                color = Color.Blue
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Difficulty: ${result.difficulty}",
                fontSize = 18.sp,
                color = Color.Red
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Muscle Group: ${result.muscle}",
                fontSize = 18.sp,
                color = Color.Black
            )

            // Expandable section for Equipment and Instructions
            if (expanded) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Equipment: ${result.equipment}",
                    fontSize = 18.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Instructions: ${result.instructions}",
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!isAddedToDatabase) {
                Button(
                    onClick = {
                        addToFirebaseDatabase(result)
                        isAddedToDatabase = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = "Add to Workouts",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            } else {
                Text(
                    text = "Workout Already Saved",
                    fontSize = 16.sp,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }
    }
}

private fun addToFirebaseDatabase(result: com.example.traintracks.SearchResult) {

    val auth = Firebase.auth
    val currentUser = auth.currentUser

    if (currentUser != null) {
        val userId = currentUser.uid

        // Push a new entry and capture the generated unique key
        val db = FirebaseDatabase.getInstance().getReference("users/$userId/workouts").push()
        val key = db.key ?: return // Return if key is null

        // Include the key in the workout data
        val workoutData = result.toMap(key)

        db.setValue(workoutData)
    }
}

fun com.example.traintracks.SearchResult.toMap(id: String): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "name" to name,
        "type" to type,
        "difficulty" to difficulty,
        "muscle" to muscle,
        "equipment" to equipment,
        "instructions" to instructions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SearchScreenContent() {
    var searchClicked by remember { mutableStateOf(false) }
    val currentContext = LocalContext.current
    var searchResults by remember { mutableStateOf<List<com.example.traintracks.SearchResult>>(emptyList()) }

    if (searchClicked) {
        Results(searchResults)
    } else {
            Search { workoutName, workoutType, muscleGroup, difficulty ->
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.api-ninjas.com/v1/") // Replace with your API base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val searchApiService = retrofit.create(SearchApiService::class.java)


            GlobalScope .launch {
                try {
                    searchResults = searchApiService.searchWorkouts(
                            workoutName,
                            workoutType,
                            muscleGroup,
                            difficulty,
                            "KX79m6HUenAsqfTvt9WydA==ib8FER6nxlcnsxnk")
                    // Handle the search results as needed
                    Log.i("CHECK_POINT", "Params: $workoutName, $workoutType, $muscleGroup, $difficulty  --- onResponse: $searchResults")
                    searchClicked = true

                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.i("CHECK_POINT", "onResponse: ${e.code()}")
                    Log.i("CHECK_POINT", "onResponse: ${e.message()}")
                    Log.i("CHECK_POINT", "onResponse: $errorBody")
                } catch (e: Exception) {
                    // Handle errors
                    Log.i("CHECK_POINT", "onResponse: ${e.message}")
                    Log.i("CHECK_POINT", "onResponse: ${e.cause}")

                    println("Error: ${e.message}")
                }
            }

            println("Workout Name: $workoutName, Workout Type: $workoutType, Muscle Group: $muscleGroup, Difficulty: $difficulty")
            searchClicked = true
        }
    }
}

