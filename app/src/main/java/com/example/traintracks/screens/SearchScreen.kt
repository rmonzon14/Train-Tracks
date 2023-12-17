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
import androidx.compose.ui.tooling.preview.Preview
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
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale


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
                workoutType.forEach { item ->
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
                muscle.forEach { item ->
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
                difficulty.forEach { item ->
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
fun Results(searchResults: List<com.example.traintracks.SearchResult>, snackbarHostState: SnackbarHostState) {
    val currentContext = LocalContext.current

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
                searchResults.forEach { result ->
                    SearchResultItem(result = result, snackbarHostState = snackbarHostState) // Pass snackbarHostState to SearchResultItem
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
fun SearchResultItem(result: com.example.traintracks.SearchResult, snackbarHostState: SnackbarHostState) {
    val auth = Firebase.auth
    val currentUser = auth.currentUser
    var isAddedToDatabase by remember { mutableStateOf(false) }
    var firebaseId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(result) {
        val userId = currentUser?.uid
        if (userId != null) {
            val db = FirebaseDatabase.getInstance().getReference("users/$userId/workouts")
            db.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { dataSnapshot ->
                        val workout = dataSnapshot.getValue(Workout::class.java)
                        if (workout?.name == result.name) {
                            isAddedToDatabase = true
                            firebaseId = dataSnapshot.key // Store the Firebase ID
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
                val iconResId = when (result.type) {
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

                val difficultyIconResId = when (result.difficulty) {
                    difficulties[0] -> R.drawable.easy
                    difficulties[1] -> R.drawable.medium
                    difficulties[2] -> R.drawable.hard
                    else -> R.drawable.emh
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
                                addToFirebaseDatabase(result, snackbarHostState)
                                isAddedToDatabase = true
                            }
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.heart_closed),
                        contentDescription = "Remove from Favourites",
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                val id = firebaseId
                                if (id != null) {
                                    deleteFromFirebaseDatabase(id, snackbarHostState)
                                    isAddedToDatabase = false
                                } else {
                                    // Handle the case where firebaseId is null
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar("Error: Unable to remove workout")
                                    }
                                }
                            }
                    )
                }

            }

            Text(
                text = result.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = result.type.toTitleCase(),
                fontSize = 18.sp,
                color = getTypeColor(result.type),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Difficulty: ${result.difficulty.toTitleCase()}",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Muscle Group: ${result.muscle.toTitleCase()}",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            // Expandable section for Equipment and Instructions
            if (expanded) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Equipment: ${result.equipment}",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Instructions: ${result.instructions}",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

private fun addToFirebaseDatabase(result: SearchResult, snackbarHostState: SnackbarHostState) {
    val auth = Firebase.auth
    val currentUser = auth.currentUser

    if (currentUser != null) {
        val userId = currentUser.uid
        val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/workouts")

        val key = dbRef.push().key ?: return // Generate a new key for new entries

        val workoutData = result.toWorkoutMap(key)
        dbRef.child(key).setValue(workoutData).addOnSuccessListener {
            CoroutineScope(Dispatchers.Main).launch {
                snackbarHostState.showSnackbar("Workout added to favourites")
            }
        }.addOnFailureListener { e ->
            CoroutineScope(Dispatchers.Main).launch {
                snackbarHostState.showSnackbar(e.message ?: "Failed to save workout")
            }
        }
    }
}


private fun deleteFromFirebaseDatabase(firebaseId: String, snackbarHostState: SnackbarHostState) {
    val auth = Firebase.auth
    val currentUser = auth.currentUser

    if (currentUser != null && firebaseId != null) {
        val userId = currentUser.uid
        val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/workouts")

        dbRef.child(firebaseId).removeValue().addOnSuccessListener {
            CoroutineScope(Dispatchers.Main).launch {
                snackbarHostState.showSnackbar("Workout successfully removed")
            }
        }.addOnFailureListener { e ->
            CoroutineScope(Dispatchers.Main).launch {
                snackbarHostState.showSnackbar(e.message ?: "Failed to remove workout")
            }
        }
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

@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Preview
@Composable
fun SearchScreenContent() {
    val snackbarHostState = remember { SnackbarHostState() }
    var searchClicked by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<com.example.traintracks.SearchResult>>(emptyList()) }

    if (searchClicked) {
        Results(searchResults, snackbarHostState) // Pass snackbarHostState to Results
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
    return this.split('_')
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
}

fun SearchResult.toWorkoutMap(id: String): Map<String, Any?> {
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


