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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.traintracks.R
import com.example.traintracks.SearchApiService
import com.example.traintracks.SearchResult
import com.example.traintracks.Workout
import com.example.traintracks.ui.theme.TrainTracksTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel

class SearchScreen : ComponentActivity() {
    private lateinit var sharedViewModel: SharedViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TrainTracksTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Initialize SharedViewModel
                    val sharedViewModel: SharedViewModel = viewModel()

                    // Initialize data in SharedViewModel
                    val retrofit = Retrofit.Builder()
                        .baseUrl("https://api.api-ninjas.com/v1/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                    val apiService = retrofit.create(SearchApiService::class.java)

                    // Initialize data in SharedViewModel
                    sharedViewModel.initializeData(apiService)

                    SearchScreenContent(sharedViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(
    sharedViewModel: SharedViewModel,
    onSearchClicked: (String, String, String, String) -> Unit
) {
    var workoutName by remember { mutableStateOf("") }
    var workoutType by remember { mutableStateOf("") }
    var muscleGroup by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("") }

    //Dropdown Data
    val difficulties = sharedViewModel.difficulties.observeAsState().value
    val types = sharedViewModel.types.observeAsState().value
    val muscles = sharedViewModel.muscles.observeAsState().value

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
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TopAppBar(
            modifier = Modifier.alpha(0.7f),
            title = {
                Text(
                    text = "Workout Search",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )
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

        WorkoutNameField(
            value = workoutName,
            onChange = { workoutName = it },
        )
        WorkoutTypeField(
            value = workoutType,
            onChange = { workoutType = it },
            types = types ?: arrayOf()
        )
        MuscleGroupField(
            value = muscleGroup,
            onChange = { muscleGroup = it },
            muscles = muscles ?: arrayOf()
        )
        DifficultyField(
            value = difficulty,
            onChange = { difficulty = it },
            difficulties = difficulties ?: arrayOf()
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
                color = MaterialTheme.colorScheme.background
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
    onChange: (String) -> Unit,
    types: Array<String>?
) {
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
                value = selectedText.toTitleCase(),
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
                types?.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = item.toTitleCase()) },
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
    onChange: (String) -> Unit,
    muscles: Array<String>?
) {
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
                value = selectedText.toTitleCase(),
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
                muscles?.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = item.toTitleCase()) },
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
    onChange: (String) -> Unit,
    difficulties: Array<String>?
) {
    //val difficulty = arrayOf("Beginner", "Intermediate", "Expert")
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
                value = selectedText.toTitleCase(),
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
                difficulties?.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = item.toTitleCase()) },
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
fun Results(sharedViewModel: SharedViewModel, searchResults: List<Workout>, snackbarHostState: SnackbarHostState, difficulties: Array<String>?, muscles: Array<String>?, types: Array<String>?) {
    val currentContext = LocalContext.current
    val difficulties = sharedViewModel.difficulties.observeAsState().value
    val muscles = sharedViewModel.muscles.observeAsState().value
    val types = sharedViewModel.types.observeAsState().value
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column{
            TopAppBar(
                title = {
                    Row{
                        Text(
                            text = "Search Results",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            val intent = Intent(currentContext, SearchScreen::class.java)
                            currentContext.startActivity(intent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(35.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val intent = Intent(currentContext, MainActivity::class.java)
                            currentContext.startActivity(intent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Back",
                            modifier = Modifier.size(35.dp)
                        )
                    }
                }
            )

        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Tap workouts for more details.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top=16.dp, bottom = 16.dp)
            )

            if (searchResults.isNotEmpty()) {
                // Display search results
                searchResults.forEach { workout ->
                    SearchResultItem(workout, snackbarHostState = snackbarHostState, difficulties, muscles, types) // Pass snackbarHostState to SearchResultItem
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
}
@Composable
fun SearchResultItem(workout: Workout, snackbarHostState: SnackbarHostState, difficulties: Array<String>?, types: Array<String>?, muscles: Array<String>?) {
    val auth = Firebase.auth
    val currentUser = auth.currentUser
    var isAddedToDatabase by remember { mutableStateOf(false) }
    var firebaseId by remember { mutableStateOf<String?>(null) }
    var isOperationInProgress by remember { mutableStateOf(false) }

    LaunchedEffect(workout) {
        val userId = currentUser?.uid
        if (userId != null) {
            val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/workouts")
            dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { dataSnapshot ->
                        val savedWorkout = dataSnapshot.getValue(Workout::class.java)
                        if (savedWorkout?.name == workout.name) {
                            isAddedToDatabase = true
                            firebaseId = dataSnapshot.key
                            return
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                val iconResId = when (workout.type) {
                    "cardio" -> R.drawable.icon_cardio
                    "olympic_weightlifting" -> R.drawable.icon_olympic_weighlifting
                    "plyometrics" -> R.drawable.icon_plyometrics
                    "powerlifting" -> R.drawable.icon_powerlifting
                    "strength" -> R.drawable.muscle_bicep
                    "stretching" -> R.drawable.icon_stretching
                    "strongman" -> R.drawable.icon_strongman
                    else -> R.drawable.icon_strongman
                }

                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = "Workout Icon",
                    modifier = Modifier.size(48.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary)
                )

                Spacer(modifier = Modifier.width(6.dp))

                val difficultyIconResId = when (workout.difficulty.toLowerCase(Locale.getDefault())) {
                    "easy" -> R.drawable.easy
                    "intermediate" -> R.drawable.medium
                    "expert" -> R.drawable.hard
                    else -> R.drawable.emh // Default icon if no match is found
                }

                Image(
                    painter = painterResource(id = difficultyIconResId),
                    contentDescription = "Difficulty Icon",
                    modifier = Modifier.size(52.dp)
                )

                // Spacer with weight will push the remaining items to the end of the Row
                Spacer(modifier = Modifier.weight(1f))

                // Heart icon
                if (!isAddedToDatabase) {
                    Icon(
                        painter = painterResource(id = R.drawable.heart_open),
                        contentDescription = "Add to Favourites",
                        modifier = Modifier
                            .size(30.dp)
                            .padding(1.dp)
                            .clickable {
                                isAddedToDatabase = true // Optimistically update the UI
                                saveWorkoutToFirebase(
                                    workout,
                                    userId,
                                    snackbarHostState,
                                    onSaved = { newFirebaseId ->
                                        firebaseId = newFirebaseId // Update the ID
                                    },
                                    onFailure = {
                                        isAddedToDatabase = false // Revert if failed
                                    }
                                )
                            }
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.heart_closed),
                        contentDescription = "Remove from Favourites",
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                if (!isOperationInProgress) {
                                    isOperationInProgress = true
                                    if (!isAddedToDatabase) {
                                        // Add to favorites
                                        saveWorkoutToFirebase(
                                            workout,
                                            userId,
                                            snackbarHostState,
                                            onSaved = { newFirebaseId ->
                                                firebaseId = newFirebaseId
                                                isAddedToDatabase= true
                                                isOperationInProgress = false
                                            },
                                            onFailure = {
                                                isOperationInProgress = false
                                            }
                                        )
                                    } else {
                                        // Remove from favorites
                                        firebaseId?.let { firebaseId ->
                                            onDeleteWorkout(
                                                firebaseId,
                                                userId,
                                                snackbarHostState
                                            )
                                            isAddedToDatabase = false
                                            isOperationInProgress = false
                                        }
                                    }
                                }
                            }
                    )
                }

            }

            Text(
                text = workout.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = workout.type.toTitleCase(),
                fontSize = 18.sp,
                color = getTypeColor(workout.type),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Difficulty: ${workout.difficulty.toTitleCase()}",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Muscle Group: ${workout.muscle.toTitleCase()}",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            // Expandable section for Equipment and Instructions
            if (expanded) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Equipment: ${workout.equipment}",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Instructions: ${workout.instructions}",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
fun SearchScreenContent(sharedViewModel: SharedViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    var searchClicked by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Workout>>(emptyList()) }
    val difficulties = sharedViewModel.difficulties.observeAsState().value
    val types = sharedViewModel.difficulties.observeAsState().value
    val muscles = sharedViewModel.difficulties.observeAsState().value

    if (searchClicked) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // TopAppBar and other UI components
            Results(sharedViewModel ,searchResults, snackbarHostState, difficulties, muscles, types)

            if (searchResults.isNotEmpty()) {
                searchResults.forEach { workout ->
                    SearchResultItem(workout, snackbarHostState, difficulties, muscles, types)
                }
            } else {
                Text(
                    text = "No results found",
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } else {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.api-ninjas.com/v1/") // Replace with your API's base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var apiService = retrofit.create(SearchApiService::class.java)
        Search(sharedViewModel, onSearchClicked = { workoutName, workoutType, muscleGroup, difficulty ->
            fetchWorkouts(service = apiService, name = workoutName, muscle = muscleGroup, type = workoutType, difficulty = difficulty, offset = 0) { apiResults ->
                searchResults = apiResults
                searchClicked = true
            }
        })
    }

    SnackbarHost(hostState = snackbarHostState)
}

@Composable
fun getDifficultyColor(difficulty: String): Color {
    val darkGreen = Color(0xFF006400)
    val darkOrange = Color(0xFFCC8400)
    val darkRed = Color(0xFFCD5C5C)
    return when (difficulty) {
        "beginner" -> darkGreen
        "intermediate" -> darkOrange
        "expert" -> darkRed
        else -> MaterialTheme.colorScheme.secondary
    }
}

@Composable
fun getTypeColor(type: String): Color {
    return when (type) {
        "cardio" -> Color(0xFFC91212)
        "olympic_weightlifting" -> Color(0xFFFF8F00)
        "plyometrics" -> Color(0xFFF124AA)
        "powerlifting" -> Color(0xFF1D28A2)
        "strength" -> Color(0xFF9C27B0)
        "stretching" -> Color(0xFF008B8B)
        "strongman" -> Color(0xFF9B6857)
        else -> MaterialTheme.colorScheme.secondary
    }
}

fun String.toTitleCase(): String {
    return this.replace('_', ' ')
        .split(' ')
        .joinToString(" ") { word ->
            word.lowercase(Locale.getDefault()).replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }
}
