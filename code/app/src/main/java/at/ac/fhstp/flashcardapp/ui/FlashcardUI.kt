package at.ac.fhstp.flashcardapp.ui

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import at.ac.fhstp.flashcardapp.AppViewModelProvider
import at.ac.fhstp.flashcardapp.R
import at.ac.fhstp.flashcardapp.data.Deck
import at.ac.fhstp.flashcardapp.data.Flashcard
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

enum class Routes {
    DeckList,
    DeckDetail,
    Review,
    AddDeck,
    AddFlashcard
}

@Composable
fun FlashcardApp(
    viewModel: FlashcardViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavHostController = rememberNavController()
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        NavHost(
            navController = navController,
            startDestination = Routes.DeckList.name
        ) {
            composable(Routes.DeckList.name) {
                val decks by viewModel.decks.collectAsState()
                DeckListScreen(
                    decks = decks,
                    onDeckClick = { deck ->
                        navController.navigate("${Routes.DeckDetail.name}/${deck.id}")
                    },
                    onAddDeckClick = { navController.navigate(Routes.AddDeck.name) },
                    onDeleteDeckClick = { deck -> viewModel.deleteDeck(deck) }
                )
            }
            composable(
                route = "${Routes.DeckDetail.name}/{deckId}",
                arguments = listOf(navArgument("deckId") { type = NavType.IntType })
            ) { backStackEntry ->
                val deckId = backStackEntry.arguments?.getInt("deckId") ?: 0
                val flashcards by viewModel.getFlashcardsForDeck(deckId).collectAsState(initial = emptyList())
                val dueFlashcards by viewModel.getDueFlashcards(deckId).collectAsState(initial = emptyList())
                val chartData = viewModel.getUpcomingReviewsChartData(flashcards)

                DeckDetailScreen(
                    flashcards = flashcards,
                    dueFlashcardsCount = dueFlashcards.size,
                    chartData = chartData,
                    onStartReviewClick = {
                        navController.navigate("${Routes.Review.name}/$deckId")
                    },
                    onAddFlashcardClick = {
                        navController.navigate("${Routes.AddFlashcard.name}/$deckId")
                    }
                )
            }
            composable(
                route = "${Routes.Review.name}/{deckId}",
                arguments = listOf(navArgument("deckId") { type = NavType.IntType })
            ) { backStackEntry ->
                val deckId = backStackEntry.arguments?.getInt("deckId") ?: 0
                val dueFlashcards by viewModel.getDueFlashcards(deckId).collectAsState(initial = null)
                val decks by viewModel.decks.collectAsState()
                val deck = decks.find { it.id == deckId }

                ReviewScreen(
                    dueFlashcards = dueFlashcards,
                    deck = deck,
                    onAnswer = { flashcard, isCorrect ->
                        viewModel.processAnswer(flashcard, isCorrect)
                    },
                    onReviewComplete = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Routes.AddDeck.name) {
                AddDeckScreen(
                    onSave = { deckName, frontLang, backLang ->
                        viewModel.addDeck(deckName, frontLang, backLang) {
                            navController.popBackStack()
                        }
                    }
                )
            }
            composable(
                route = "${Routes.AddFlashcard.name}/{deckId}",
                arguments = listOf(navArgument("deckId") { type = NavType.IntType })
            ) { backStackEntry ->
                val deckId = backStackEntry.arguments?.getInt("deckId") ?: 0
                AddFlashcardScreen(
                    onSave = { front, back ->
                        viewModel.addFlashcard(deckId, front, back)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun DeckListScreen(
    decks: List<Deck>,
    onDeckClick: (Deck) -> Unit,
    onAddDeckClick: () -> Unit,
    onDeleteDeckClick: (Deck) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddDeckClick) {
                Icon(Icons.Default.Add, contentDescription = "Add deck")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (decks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No decks available")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(decks) { deck ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { onDeckClick(deck) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = deck.name, style = MaterialTheme.typography.titleMedium)
                                IconButton(onClick = { onDeleteDeckClick(deck) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete deck", tint = MaterialTheme.colorScheme.tertiary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeckDetailScreen(
    flashcards: List<Flashcard>,
    dueFlashcardsCount: Int,
    chartData: List<Pair<String, Int>>,
    onStartReviewClick: () -> Unit,
    onAddFlashcardClick: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddFlashcardClick) {
                Icon(Icons.Default.Add, contentDescription = "Add flashcard")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Button(onClick = onStartReviewClick, enabled = dueFlashcardsCount > 0) {
                Text("Start Review ($dueFlashcardsCount due)")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Upcoming Reviews:", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            UpcomingReviewsChart(chartData)
            Spacer(modifier = Modifier.height(16.dp))
            Text("All Cards:", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(flashcards) { flashcard ->
                    val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(flashcard.dueDate))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = flashcard.front, style = MaterialTheme.typography.bodyLarge)
                            Text(text = flashcard.back, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Due: $formattedDate", style = MaterialTheme.typography.bodySmall)
                                Text("Interval: ${flashcard.interval}d", style = MaterialTheme.typography.bodySmall)
                                Text("Ease: ${flashcard.easeFactor}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingReviewsChart(chartData: List<Pair<String, Int>>) {
    val maxValue = chartData.maxOfOrNull { it.second } ?: 1
    val barColor = MaterialTheme.colorScheme.tertiary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        for ((day, count) in chartData) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Canvas(
                    modifier = Modifier
                        .width(20.dp)
                        .height(80.dp)
                ) {
                    val barHeight = (count.toFloat() / maxValue) * size.height
                    drawLine(
                        color = barColor,
                        start = Offset(x = center.x, y = size.height),
                        end = Offset(x = center.x, y = size.height - barHeight),
                        strokeWidth = 20f
                    )
                }
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ReviewScreen(
    dueFlashcards: List<Flashcard>?,
    deck: Deck?,
    onAnswer: (Flashcard, Boolean) -> Unit,
    onReviewComplete: () -> Unit
) {
    var currentCardIndex by remember { mutableStateOf(0) }
    var showBack by remember { mutableStateOf(false) }
    val offset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val threshold = screenWidth * 0.3f

    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    fun speak(text: String, langCode: String) {
        if (isTtsReady) {
            tts?.language = Locale(langCode)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    // Auto-play TTS when card changes or flips
    if (dueFlashcards != null && currentCardIndex < dueFlashcards.size && deck != null) {
        val flashcard = dueFlashcards[currentCardIndex]
        LaunchedEffect(currentCardIndex, showBack, isTtsReady) {
            if (isTtsReady) {
                val textToSpeak = if (showBack) flashcard.back else flashcard.front
                val lang = if (showBack) deck.backLanguage else deck.frontLanguage
                speak(textToSpeak, lang)
            }
        }
    }

    // Handle Empty State (No cards due)
    if (dueFlashcards != null && dueFlashcards.isEmpty()) {
        LaunchedEffect(Unit) {
            onReviewComplete()
        }
    }

    // Handle Completion State
    if (dueFlashcards != null && dueFlashcards.isNotEmpty() && currentCardIndex >= dueFlashcards.size) {
        LaunchedEffect(currentCardIndex) {
            kotlinx.coroutines.delay(500)
            onReviewComplete()
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (dueFlashcards == null) {
                // Loading State
                CircularProgressIndicator()
            } else if (dueFlashcards.isEmpty()) {
                // Empty State
                Text(text = "No cards due.")
            } else {
                // Review State
                if (currentCardIndex < dueFlashcards.size) {
                    val flashcard = dueFlashcards[currentCardIndex]

                    // Background indicators for Swipe
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Right Swipe (Correct) Indicator
                        if (offset.value > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Green.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Correct",
                                    modifier = Modifier.padding(start = 24.dp).size(48.dp),
                                    tint = Color.Green
                                )
                            }
                        }
                        // Left Swipe (Wrong) Indicator
                        if (offset.value < 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Red.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Wrong",
                                    modifier = Modifier.padding(end = 24.dp).size(48.dp),
                                    tint = Color.Red
                                )
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .padding(16.dp)
                                .offset { IntOffset(offset.value.roundToInt(), 0) }
                                .pointerInput(showBack) {
                                    if (showBack) {
                                        detectHorizontalDragGestures(
                                            onDragEnd = {
                                                scope.launch {
                                                    if (offset.value > threshold) {
                                                        // Swipe Right (Correct)
                                                        offset.animateTo(screenWidth)
                                                        onAnswer(flashcard, true)
                                                        showBack = false
                                                        currentCardIndex++
                                                        offset.snapTo(0f)
                                                    } else if (offset.value < -threshold) {
                                                        // Swipe Left (Wrong)
                                                        offset.animateTo(-screenWidth)
                                                        onAnswer(flashcard, false)
                                                        showBack = false
                                                        currentCardIndex++
                                                        offset.snapTo(0f)
                                                    } else {
                                                        offset.animateTo(0f)
                                                    }
                                                }
                                            },
                                            onDragCancel = {
                                                scope.launch { offset.animateTo(0f) }
                                            }
                                        ) { change, dragAmount ->
                                            change.consume()
                                            scope.launch { offset.snapTo(offset.value + dragAmount) }
                                        }
                                    }
                                }
                                .clickable { if (!showBack) showBack = true },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (showBack) flashcard.back else flashcard.front,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                if (deck != null) {
                                    FilledIconButton(
                                        onClick = {
                                            val textToSpeak = if (showBack) flashcard.back else flashcard.front
                                            val lang = if (showBack) deck.backLanguage else deck.frontLanguage
                                            speak(textToSpeak, lang)
                                        },
                                        modifier = Modifier.size(56.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Speak",
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (!showBack) {
                            Text(text = "Tap to show answer", style = MaterialTheme.typography.bodySmall)
                        } else {
                            Text(text = "Swipe Left for Wrong, Right for Correct", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    Text(text = "Review Complete!")
                }
            }
        }
    }
}

@Composable
fun AddDeckScreen(onSave: (String, String, String) -> Unit) {
    var deckName by remember { mutableStateOf("") }
    var frontLang by remember { mutableStateOf("de") }
    var backLang by remember { mutableStateOf("en") }

    val languages = listOf(
        "en" to "English",
        "de" to "German",
        "fr" to "French",
        "es" to "Spanish",
        "it" to "Italian"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Add a new deck", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = deckName,
            onValueChange = { deckName = it },
            label = { Text("Deck Name") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(text = "Languages", style = MaterialTheme.typography.titleMedium)
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Front Language
            var expandedFront by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { expandedFront = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Front: ${languages.find { it.first == frontLang }?.second}",
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                }
                DropdownMenu(expanded = expandedFront, onDismissRequest = { expandedFront = false }) {
                    languages.forEach { (code, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                frontLang = code
                                expandedFront = false
                            }
                        )
                    }
                }
            }

            // Back Language
            var expandedBack by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { expandedBack = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Back: ${languages.find { it.first == backLang }?.second}",
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                }
                DropdownMenu(expanded = expandedBack, onDismissRequest = { expandedBack = false }) {
                    languages.forEach { (code, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                backLang = code
                                expandedBack = false
                            }
                        )
                    }
                }
            }
        }

        Button(
            onClick = { if (deckName.isNotBlank()) onSave(deckName, frontLang, backLang) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}

@Composable
fun AddFlashcardScreen(onSave: (String, String) -> Unit) {
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = stringResource(R.string.add_flashcard), style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = front,
            onValueChange = { front = it },
            label = { Text(stringResource(R.string.flashcard_front)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = back,
            onValueChange = { back = it },
            label = { Text(stringResource(R.string.flashcard_back)) },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { if (front.isNotBlank()) onSave(front, back) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save))
        }
    }
}