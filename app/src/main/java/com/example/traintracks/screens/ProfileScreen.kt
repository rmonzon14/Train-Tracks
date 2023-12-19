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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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

    val workoutTypePercentages = calculateWorkoutTypePercentages(workoutLogs.value)
    val topWorkoutType = workoutTypePercentages.maxByOrNull { it.value }?.key ?: ""

    val workoutMusclePercentages = calculateMuscleGroupPercentages(workoutLogs.value)
    val topWorkoutMuscle = workoutMusclePercentages.maxByOrNull { it.value }?.key ?: ""

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

                    // Filter logs for the last 30 days
                    val logsWithinLast30Days = filterLogsForLast30Days(fetchedLogs)

                    workoutLogs.value = logsWithinLast30Days

                    val workoutTypePercentages = calculateWorkoutTypePercentages(logsWithinLast30Days)
                    val workoutMusclePercentages = calculateMuscleGroupPercentages(logsWithinLast30Days)

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
        Text(
            text = currentUser?.email ?: "User",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(12.dp))

        Text("Your Past 30 Days", style = MaterialTheme.typography.headlineLarge)

        Spacer(Modifier.height(12.dp))

        workoutTypePercentages.forEach { (type, percentage) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${type.toTitleCase()}: ", Modifier.weight(1f))
                LinearProgressIndicator(
                    progress = percentage.toFloat() / 100,
                    modifier = Modifier
                        .height(20.dp)
                        .weight(2f)
                )
                Text("${percentage.toInt()}%")
            }
        }

        Spacer(Modifier.height(16.dp))

        workoutMusclePercentages.forEach { (muscle, percentage) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${muscle.toTitleCase()}: ", Modifier.weight(1f))
                LinearProgressIndicator(
                    progress = percentage.toFloat() / 100,
                    modifier = Modifier
                        .height(20.dp)
                        .weight(2f)
                )
                Text("${percentage.toInt()}%")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Display the custom message for the top workout type
        val message = getRandomMessageForWorkoutType(topWorkoutType)
        Text(message, style = MaterialTheme.typography.bodyLarge)




        Spacer(Modifier.height(12.dp))

        // Display message for the least performed workout type
        val muscleGroupPercentages = calculateMuscleGroupPercentages(workoutLogs.value)
        val leastEngagedMuscleGroups = findAllLeastEngagedMuscleGroups(muscleGroupPercentages)
        val muscleGroupsText = leastEngagedMuscleGroups.joinToString(separator = ", ")

        // Display the message for recommended muscle groups
        Text(
            text = "Focus on these muscle groups in future workouts: $muscleGroupsText",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
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
    "cardio" to listOf(
        "Your cardio dedication is unmatched!",
        "Running toward your goals like a champ!",
        "Cardiovascular excellence at its finest!",
        "You're the cardio superstar we admire!",
        "Every step you take is a step closer to success!",
        "Your heart must be as strong as your spirit!",
        "Cardio king/queen in the making!",
        "A true cardio enthusiast in action!",
        "You're setting the pace for everyone else!",
        "Running like the wind, unstoppable!",
        "Your stamina is off the charts!",
        "Cardio champ on the rise!",
        "The road to fitness leads through your footsteps!",
        "Your heart thanks you for the dedication!",
        "Cardio is your playground, and you dominate it!",
        "Every cardio session is a step forward in life!",
        "Healthier with every cardio session!",
        "Cardio conqueror!",
        "Elevating your heart rate and your fitness game!",
        "You breathe fitness and exhale success!",
        "Cardio is your daily meditation!",
        "Pushing your limits, one cardio session at a time!",
        "You've got the heart of a fitness warrior!",
        "Cardio goals? You've already achieved them!",
        "On a journey to cardio excellence!",
        "Cardio routines never looked so good!",
        "Cardio is your secret weapon!",
        "You're the heartbeat of our fitness community!",
        "Cardio enthusiast by day, fitness legend by night!",
        "A master of the cardio craft!",
        "Cardio guru in the making!",
        "Your cardio journey is an inspiration to all!",
        "Running with the purpose of champions!",
        "Cardio is your canvas; sweat is your paint!",
        "Your cardio sessions are pure poetry in motion!",
        "Cardio sessions with you are always a pleasure!",
        "You make cardio look effortless and fun!",
        "Your cardiovascular dedication is contagious!",
        "Cardio is where you shine the brightest!",
        "With every step, you conquer new milestones!",
        "Cardio royalty in action!",
        "You're a cardio role model!",
        "Cardio excellence personified!",
        "Your fitness journey is a masterpiece of cardio!",
        "Breaking a sweat and breaking fitness barriers!",
        "Cardio sessions with you are legendary!",
        "Your heart races for fitness, and so do we!",
        "Cardio commitment that knows no bounds!",
        "Running toward greatness, one stride at a time!",
        "In the world of cardio, you reign supreme!"
    ),
    "olympic_weightlifting" to listOf(
        "Lifting like an Olympian!",
        "The weightlifting world bows to your strength!",
        "You're sculpting your body with iron!",
        "Olympic weightlifting, your domain of excellence!",
        "Strength and grace personified!",
        "Every lift tells a story of determination!",
        "Weightlifting wizardry at its best!",
        "Lifting to new heights of achievement!",
        "The barbell is your instrument, and you're the maestro!",
        "In the realm of weights, you're the undisputed champion!",
        "Lifting heavy and lifting spirits!",
        "You're making weights your playmates!",
        "Weightlifting goals? You've already achieved them!",
        "A true disciple of the iron gospel!",
        "Lifting with precision and power!",
        "You're the Hercules of the modern age!",
        "Lifting the weight of dreams and aspirations!",
        "Olympic weightlifting royalty!",
        "Your lifting sessions inspire awe!",
        "Muscle sculptor extraordinaire!",
        "Lifting weights and lifting spirits!",
        "Your strength journey is poetry in motion!",
        "Weightlifting mastery in action!",
        "Challenging your limits, one lift at a time!",
        "You've got the strength of a thousand warriors!",
        "Lifting your way to legend status!",
        "The iron obeys your command!",
        "Lifting the bar of excellence higher!",
        "Weightlifting wizard with a heart of gold!",
        "Every lift is a step closer to perfection!",
        "Weightlifting, your true calling!",
        "You're a lifting legend in the making!",
        "Lifting heavy, achieving greatness!",
        "In the world of weights, you're a beacon of strength!",
        "Weightlifting goals? You've already surpassed them!",
        "The weight room is your sanctuary!",
        "Weightlifting expertise that inspires us all!",
        "Your lifting sessions are a work of art!",
        "Lifting to new heights of glory!",
        "You're sculpting your body like a master!",
        "Lifting with passion and purpose!",
        "Olympic weightlifting at its finest!",
        "Your strength journey is a masterpiece!",
        "Championing the art of lifting!",
        "Every rep is a testament to your dedication!",
        "Lifting challenges with a smile!",
        "You're redefining strength with every lift!",
        "Weightlifting virtuoso in action!",
        "Lifting heavy, achieving greatness!"
    ),
    "plyometrics" to listOf(
        "Jumping toward success, one leap at a time!",
        "Plyometrics powerhouse in action!",
        "You're the gravity-defying fitness guru!",
        "Leaping toward new horizons of achievement!",
        "Your jumps are a testament to your determination!",
        "Plyometrics pro, soaring to greatness!",
        "Jumping higher, reaching for the stars!",
        "You turn every workout into a high-flying spectacle!",
        "Plyometrics enthusiast with boundless energy!",
        "Your jumps are poetry in motion!",
        "Plyometrics goals? You've already achieved them!",
        "Defying gravity and limitations!",
        "You're a true master of explosive movements!",
        "Leaping to new heights of fitness!",
        "Plyometrics wizardry at its best!",
        "Your jumps inspire awe and admiration!",
        "Elevating your fitness game, one jump at a time!",
        "Plyometrics legend in the making!",
        "You make plyometrics look effortless and fun!",
        "Plyometrics guru with a heart of gold!",
        "Jumping is your superpower!",
        "You're the pulse of our plyometrics community!",
        "Plyometrics wizard with a flair for the dramatic!",
        "Turning workouts into jump-tastic adventures!",
        "Your plyometrics sessions are legendary!",
        "Jumping with grace and precision!",
        "Plyometrics commitment that knows no bounds!",
        "You're leaping toward greatness!",
        "In the world of jumps, you reign supreme!",
        "Plyometrics pro, raising the bar!",
        "Jumping toward fitness and beyond!",
        "Plyometrics goals? You've already smashed them!",
        "The sky's the limit for your jumps!",
        "Plyometrics is your playground; defy gravity!",
        "You're a plyometrics role model!",
        "Plyometrics excellence personified!",
        "Your fitness journey is a symphony of jumps!",
        "Jumping with purpose and passion!",
        "Plyometrics champion in the making!",
        "You're redefining plyometrics with every jump!",
        "Leaping toward new challenges with a smile!",
        "Plyometrics virtuoso at work!",
        "Jumping high, achieving greatness!",
        "Plyometrics expert with a heart of gold!"
    ),
    "powerlifting" to listOf(
        "Powerlifting like a true champion!",
        "Unleashing your inner powerlifting beast!",
        "You're the powerhouse of the weight room!",
        "Lifting heavy and lifting spirits!",
        "Every rep tells a story of strength and resilience!",
        "Powerlifting goals? You've already surpassed them!",
        "A true disciple of the iron temple!",
        "Lifting weights with unmatched determination!",
        "You're sculpting your body with sheer power!",
        "In the realm of strength, you're the undisputed king/queen!",
        "Lifting heavy is your superpower!",
        "You're the Hercules/Hercules-ine of the modern age!",
        "Powerlifting legend in the making!",
        "The iron obeys your every command!",
        "Lifting the weight of dreams and aspirations!",
        "Strength and grace combined!",
        "Powerlifting wizardry at its finest!",
        "Every set is a step closer to strength mastery!",
        "Powerlifting royalty!",
        "Your lifting sessions inspire us all!",
        "Muscle sculptor extraordinaire!",
        "Lifting weights and raising the bar!",
        "Your strength journey is a work of art!",
        "Powerlifting mastery in action!",
        "Challenging your limits, one lift at a time!",
        "You've got the strength of a thousand titans!",
        "Lifting your way to legendary status!",
        "The weight room is your sanctuary!",
        "Strength and determination personified!",
        "Every lift is a testament to your dedication!",
        "Lifting heavy, conquering life!",
        "In the world of weights, you're a beacon of strength!",
        "Powerlifting goals? You've already conquered them!",
        "Your strength journey is poetry in motion!",
        "Lifting to new heights of greatness!",
        "You're sculpting your body with power and precision!",
        "Powerlifting with passion and purpose!",
        "Strength is your domain, and you rule it!",
        "Lifting weights, lifting spirits!",
        "Powerlifting excellence at its best!",
        "Championing the art of strength!",
        "Every rep is a step closer to supremacy!",
        "Lifting challenges with a smile!",
        "You're redefining strength with every set!",
        "Powerlifting virtuoso in action!"
    ),
    "strength" to listOf(
        "Strength is your middle name!",
        "Building muscles and breaking barriers!",
        "You're the architect of your own strength!",
        "Muscle sculptor extraordinaire!",
        "Every rep is a testament to your power!",
        "Strength goals? You've already achieved them!",
        "A true disciple of the iron temple!",
        "Lifting weights with unwavering determination!",
        "You're forging your body with pure strength!",
        "In the realm of power, you're the reigning champion!",
        "Lifting heavy and lifting spirits!",
        "You're the Hercules/Hercules-ine of the modern age!",
        "Strength legend in the making!",
        "The iron responds to your every command!",
        "Building muscles like a true artist!",
        "Strength and grace in perfect harmony!",
        "Your strength journey is a work of art!",
        "Strength mastery in action!",
        "Challenging your limits, one rep at a time!",
        "You possess the strength of a thousand titans!",
        "Lifting your way to legendary status!",
        "The weight room is your sanctuary!",
        "Muscle definition that inspires us all!",
        "Lifting weights and lifting spirits!",
        "Your strength journey is a masterpiece!",
        "Strength goals? You've already conquered them!",
        "Your muscles are a testament to your dedication!",
        "Lifting to new heights of greatness!",
        "You're sculpting your body with precision and power!",
        "Strength with passion and purpose!",
        "Muscle-bound and unstoppable!",
        "Building muscle is your calling!",
        "You're the epitome of strength and determination!",
        "Lifting weights, lifting spirits!",
        "Strength and endurance combined!",
        "Your muscles are a work of art!",
        "Building strength, one rep at a time!",
        "You make strength training look effortless and fun!",
        "Strength is your domain, and you rule it!",
        "Lifting weights, lifting life!",
        "Strength excellence at its best!",
        "Championing the art of muscle building!",
        "Every rep is a step closer to muscular supremacy!",
        "Lifting challenges with a grin!",
        "You're redefining strength, one set at a time!",
        "Strength virtuoso in action!"
    ),
    "stretching" to listOf(
        "Flexibility is your superpower, keep stretching!",
        "Stretch it out and reach for the stars!",
        "Bend, but don't break, like a flexible bamboo!",
        "Flexibility for longevity, embrace the stretch!",
        "Your flexibility is inspiring, keep limber!",
        "Stretch like a graceful dancer, every day!",
        "In the world of flexibility, you're a maestro!",
        "Stretching towards new horizons, one pose at a time!",
        "Limber and lithe, that's you!",
        "Stretching is your secret weapon, unleash it!",
        "Embrace the stretch, embrace the journey!",
        "You're a stretching virtuoso, bend with confidence!",
        "Every stretch is a step toward wellness!",
        "Stretching guru, guiding others to flexibility!",
        "Become one with the stretch, find your inner calm!",
        "Stretching artist, creating masterpieces of flexibility!",
        "Your flexibility is a work of art, frame it!",
        "Stretching is your compass, pointing to balance!",
        "In the realm of stretching, you're royalty!",
        "Elasticity personified, you're unstoppable!",
        "Stretching magician, conjuring wellness with every move!",
        "Flexibility is your friend, embrace it!",
        "Stretching symphony, harmonizing body and soul!",
        "Every stretch tells a story, yours is inspiring!",
        "Stretching poet, composing verses of flexibility!",
        "Elegance and grace, that's your stretching style!",
        "Stretching architect, building a flexible future!",
        "Embrace the challenge, embrace the stretch!",
        "Stretching navigator, charting uncharted waters!",
        "Stretching is the key, unlocking your potential!",
        "Stretching's embrace, like a warm hug for your body!",
        "Stretch like a warrior, conquer stiffness!",
        "In the world of stretching, you're a legend!",
        "Stretching sorcerer, weaving spells of flexibility!",
        "Flexibility guru, leading the way to wellness!",
        "Stretch, rejuvenate, repeat—your mantra!",
        "Each stretch is a step toward vitality!",
        "Stretching alchemist, turning tension into tranquility!",
        "Embrace the stretch, embrace the transformation!",
        "Stretching pioneer, exploring new frontiers!",
        "Stretching's legacy, etched in suppleness!",
        "From stiffness to serenity, one stretch at a time!",
        "Stretching's wisdom, you're the sage!",
        "Rise to the challenge, bend to greatness!",
        "Stretching's odyssey, rewriting your story!",
        "You're a true stretching warrior, fearless and flexible!",
        "Stretching explorer, discovering new limits!",
        "Inhabit the stretch, become the embodiment of flexibility!",
        "Stretching's epic journey, where legends are born!",
        "Rise like a stretcher, shine like a star!",
        "Stretching sage, spreading wellness through flexibility!",
        "Embrace the challenge, embrace the stretch!",
        "Stretching pioneer, leading the way to flexibility!",
        "Stretching's legacy, sculpted by your dedication!",
        "With every stretch, you redefine your potential!"
    ),
    "strongman" to listOf(
        "You're a strongman superstar, lifting the world with ease!",
        "Strength of a titan, unmatched and unbreakable!",
        "Carrying the weight of greatness on your shoulders!",
        "Strongman, strong will, unstoppable spirit!",
        "Your strength knows no bounds, it's a force of nature!",
        "Strong as a bull, fierce as a lion!",
        "Lifting like a true champion, every day!",
        "Strongman of determination, conquering all obstacles!",
        "Your power is awe-inspiring, keep flexing!",
        "Strongman's dedication, forging an iron will!",
        "Embrace the challenge, embrace the strength!",
        "You're a powerhouse, channeling unstoppable energy!",
        "Strongman legend in the making, watch out!",
        "Carving your path to strength, one lift at a time!",
        "Strongman's world, where limits don't exist!",
        "Muscles of steel, heart of a warrior!",
        "Strongman titan, carrying dreams to new heights!",
        "Rising above, lifting beyond the ordinary!",
        "Strongman hero, leading by example!",
        "Strength is your ally, and victory your destiny!",
        "Strongman master, sculpting your future!",
        "Unleash the beast within, embrace the strength!",
        "Strongman voyage, navigating through iron seas!",
        "Strength is your compass, guiding you forward!",
        "Strongman's symphony, every lift is a note of power!",
        "Rising to the challenge, lifting to glory!",
        "Strongman's legacy, etching your mark in iron!",
        "From weights to greatness, one lift at a time!",
        "Strongman's creed, never back down, never quit!",
        "Strength personified, you're the real deal!",
        "Strongman commander, leading the charge!",
        "Lift, conquer, repeat—strongman's mantra!",
        "In the world of strength, you're the king!",
        "Strongman architect, building a powerful you!",
        "Elevate your spirit, elevate your strength!",
        "Strongman's journey, an epic adventure!",
        "Every lift is a step toward immortality!",
        "Strongman virtuoso, mastering the art of power!",
        "Pushing limits, breaking barriers, that's you!",
        "Strongman's odyssey, rewriting the record books!",
        "You're a force to be reckoned with, strongman!",
        "Strongman explorer, uncovering new heights!",
        "Embodying strength, you're the embodiment of power!",
        "Strongman's saga, where legends are born!",
        "Rise like a strongman, shine like a champion!",
        "Strongman sage, imparting wisdom through strength!",
        "Embrace the challenge, embrace the strength!",
        "Strongman pioneer, charting new territories!",
        "Strongman's legacy, written in sweat and steel!",
        "With every lift, you redefine what's possible!"
    )
)

val workoutToMusclesMap = mapOf(
    "cardio" to listOf("calves", "quadriceps"),
    "olympic_weightlifting" to listOf("biceps", "triceps", "back"),
    // Add other mappings here...
)


// Convert log date string to a Date object
fun convertLogDateToDate(logDate: String): Date? {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CANADA)
    return try {
        dateFormat.parse(logDate)
    } catch (e: ParseException) {
        null
    }
}

// Calculate the date 30 days ago
fun getDate30DaysAgo(): Date {
    val calendar = Calendar.getInstance()
    calendar.time = Date()
    calendar.add(Calendar.DAY_OF_MONTH, -30)
    return calendar.time
}

// Filter logs for the last 30 days
fun filterLogsForLast30Days(logs: List<WorkoutLog>): List<WorkoutLog> {
    val thirtyDaysAgo = getDate30DaysAgo()
    return logs.filter { log ->
        val logDate = convertLogDateToDate(log.date)
        logDate != null && logDate >= thirtyDaysAgo
    }
}

fun findAllLeastEngagedMuscleGroups(percentages: Map<String, Double>): List<String> {
    if (percentages.isEmpty()) return listOf("General Workout")

    val minPercentage = percentages.minByOrNull { it.value }?.value ?: return listOf("General Workout")
    return percentages.filter { it.value == minPercentage }.keys.toList()
}

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

fun calculateMuscleGroupPercentages(logs: List<WorkoutLog>): Map<String, Double> {
    val totalCount = logs.size.toDouble()
    return logs.groupingBy { it.muscle }
        .eachCount()
        .mapValues { (it.value / totalCount) * 100 }
}

