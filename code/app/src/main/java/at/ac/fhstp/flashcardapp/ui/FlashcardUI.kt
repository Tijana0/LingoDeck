package at.ac.fhstp.flashcardapp.ui

import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
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
    AddFlashcard,
    EditFlashcard
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
                val context = LocalContext.current
                DeckListScreen(
                    decks = decks,
                    onDeckClick = { deck ->
                        navController.navigate("${Routes.DeckDetail.name}/${deck.id}")
                    },
                    onAddDeckClick = { navController.navigate(Routes.AddDeck.name) },
                    onDeleteDeckClick = { deck -> viewModel.deleteDeck(deck) },
                    onImportAnkiClick = { uri, name ->
                        viewModel.importAnkiDeck(context, uri, name)
                    }
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
                        navController.navigate("${Routes.Review.name}/$deckId?practice=false")
                    },
                    onStartPracticeClick = {
                        navController.navigate("${Routes.Review.name}/$deckId?practice=true")
                    },
                    onAddFlashcardClick = {
                        navController.navigate("${Routes.AddFlashcard.name}/$deckId")
                    },
                    onEditFlashcardClick = { flashcard ->
                        navController.navigate("${Routes.EditFlashcard.name}/${flashcard.id}")
                    },
                    onDeleteFlashcardClick = { flashcard ->
                        viewModel.deleteFlashcard(flashcard)
                    }
                )
            }
            composable(
                route = "${Routes.Review.name}/{deckId}?practice={practice}",
                arguments = listOf(
                    navArgument("deckId") { type = NavType.IntType },
                    navArgument("practice") { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val deckId = backStackEntry.arguments?.getInt("deckId") ?: 0
                val isPractice = backStackEntry.arguments?.getBoolean("practice") ?: false
                
                LaunchedEffect(deckId, isPractice) {
                    if (isPractice) {
                        viewModel.startPracticeSession(deckId)
                    } else {
                        viewModel.startReviewSession(deckId)
                    }
                }
                
                val reviewCards by viewModel.reviewCards.collectAsState()
                val decks by viewModel.decks.collectAsState()
                val deck = decks.find { it.id == deckId }

                ReviewScreen(
                    dueFlashcards = reviewCards,
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
            composable(
                route = "${Routes.EditFlashcard.name}/{flashcardId}",
                arguments = listOf(navArgument("flashcardId") { type = NavType.IntType })
            ) { backStackEntry ->
                val flashcardId = backStackEntry.arguments?.getInt("flashcardId") ?: 0
                var flashcard by remember { mutableStateOf<Flashcard?>(null) }

                LaunchedEffect(flashcardId) {
                    flashcard = viewModel.getFlashcardById(flashcardId)
                }

                if (flashcard != null) {
                    EditFlashcardScreen(
                        initialFront = flashcard!!.front,
                        initialBack = flashcard!!.back,
                        onSave = { newFront, newBack ->
                            viewModel.updateFlashcard(flashcard!!, newFront, newBack)
                            navController.popBackStack()
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun DeckListScreen(
    decks: List<Deck>,
    onDeckClick: (Deck) -> Unit,
    onAddDeckClick: () -> Unit,
    onDeleteDeckClick: (Deck) -> Unit,
    onImportAnkiClick: (android.net.Uri, String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deckToDelete by remember { mutableStateOf<Deck?>(null) }
    var showImportNameDialog by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var importName by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            selectedUri = uri
            importName = "Imported Anki Deck"
            showImportNameDialog = true
        }
    }

    if (showImportNameDialog) {
        AlertDialog(
            onDismissRequest = { showImportNameDialog = false },
            title = { Text("Import Anki Deck") },
            text = {
                Column {
                    Text("Enter a name for the new deck:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importName,
                        onValueChange = { importName = it },
                        label = { Text("Deck Name") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (importName.isNotBlank() && selectedUri != null) {
                        onImportAnkiClick(selectedUri!!, importName)
                        showImportNameDialog = false
                    }
                }) { Text("Import") }
            },
            dismissButton = {
                TextButton(onClick = { showImportNameDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteDialog && deckToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Deck") },
            text = { Text("Are you sure you want to delete this deck? All flashcards inside it will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteDeckClick(deckToDelete!!)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                    icon = { Icon(Icons.Default.FileOpen, contentDescription = "Import") },
                    text = { Text("Import Anki") },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                ExtendedFloatingActionButton(
                    onClick = onAddDeckClick,
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    text = { Text("New Deck") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {

            // 🔹 APP TITLE
            Text(
                text = "LingoDeck",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp)
            )

            // 🔹 SUBTITLE
            Text(
                text = "Your language decks for smarter learning",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
            )

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
                                IconButton(onClick = {
                                    deckToDelete = deck
                                    showDeleteDialog = true
                                }) {
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
    onStartPracticeClick: () -> Unit,
    onAddFlashcardClick: () -> Unit,
    onEditFlashcardClick: (Flashcard) -> Unit,
    onDeleteFlashcardClick: (Flashcard) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
        var flashcardToDelete by remember { mutableStateOf<Flashcard?>(null) }
    
        if (showDeleteDialog && flashcardToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Flashcard") },
                text = { Text("Are you sure you want to delete this flashcard?") },
                                        confirmButton = {
                                            TextButton(
                                                onClick = {
                                                    onDeleteFlashcardClick(flashcardToDelete!!)
                                                    showDeleteDialog = false
                                                },
                                                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                                            ) {
                                                Text("Delete")
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(
                                                onClick = { showDeleteDialog = false },
                                                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                                            ) {
                                                Text("Cancel")
                                            }
                                        }            )
        }
    
                Scaffold(
    
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (flashcards.isNotEmpty()) {
                    val isReview = dueFlashcardsCount > 0
                    val count = if (isReview) minOf(dueFlashcardsCount, 20) else minOf(flashcards.size, 20)
                    val label = if (isReview) "Start Study Session ($count)" else "Practice ($count)"
                    
                    ExtendedFloatingActionButton(
                        onClick = if (isReview) onStartReviewClick else onStartPracticeClick,
                        modifier = Modifier.weight(1f),
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                        text = { Text(label) }
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                FloatingActionButton(onClick = onAddFlashcardClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add flashcard")
                }
            }
        }
    
                ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Text(
                        text = "Upcoming Study Sessions:",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        UpcomingReviewsChart(chartData)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "All Cards:",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {                                items(flashcards) { flashcard ->
                                    FlashcardItem(
                                        flashcard = flashcard,
                                        onEditClick = { onEditFlashcardClick(flashcard) },
                                        onDeleteClick = {
                                            flashcardToDelete = flashcard
                                            showDeleteDialog = true
                                        }
                                    )
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

    var sessionCorrect by remember { mutableIntStateOf(0) }
    var sessionIncorrect by remember { mutableIntStateOf(0) }
    var hasProcessedCards by remember { mutableStateOf(false) }

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

    if (dueFlashcards != null && dueFlashcards.isEmpty() && hasProcessedCards) {
        ReviewSummaryScreen(
            correct = sessionCorrect,
            incorrect = sessionIncorrect,
            onBackToDeck = onReviewComplete
        )
        return
    }

    if (dueFlashcards != null && dueFlashcards.isEmpty() && !hasProcessedCards) {
        LaunchedEffect(Unit) {
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
                CircularProgressIndicator()
            } else if (dueFlashcards.isEmpty()) {
                Text(text = "No cards due.")
            } else {
                val currentCount = sessionCorrect + sessionIncorrect + 1
                val totalCount = dueFlashcards.size + sessionCorrect + sessionIncorrect
                Text(
                    text = "$currentCount / $totalCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                )

                val flashcard = dueFlashcards.first()

                if (deck != null) {
                    LaunchedEffect(flashcard, showBack, isTtsReady) {
                        if (isTtsReady) {
                            val textToSpeak = if (showBack) flashcard.back else flashcard.front
                            val lang = if (showBack) deck.backLanguage else deck.frontLanguage
                            speak(textToSpeak, lang)
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
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
                            .drawBehind {
                                val strokeWidth = 8.dp.toPx()
                                val currentOffset = offset.value

                                if (currentOffset <= 0) {
                                    drawLine(
                                        color = Color.Red,
                                        start = Offset(0f, 0f),
                                        end = Offset(0f, size.height),
                                        strokeWidth = strokeWidth
                                    )
                                }

                                if (currentOffset >= 0) {
                                    drawLine(
                                        color = Color.Green,
                                        start = Offset(size.width, 0f),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = strokeWidth
                                    )
                                }
                            }
                            .padding(16.dp)
                            .offset { IntOffset(offset.value.roundToInt(), 0) }
                            .pointerInput(showBack) {
                                if (showBack) {
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            scope.launch {
                                                if (offset.value > threshold) {
                                                    sessionCorrect++
                                                    hasProcessedCards = true
                                                    // Swipe Right (Correct)
                                                    offset.animateTo(screenWidth)
                                                    onAnswer(flashcard, true)
                                                    showBack = false
                                                    offset.snapTo(0f)
                                                } else if (offset.value < -threshold) {
                                                    sessionIncorrect++
                                                    hasProcessedCards = true
                                                    // Swipe Left (Wrong)
                                                    offset.animateTo(-screenWidth)
                                                    onAnswer(flashcard, false)
                                                    showBack = false
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
                            .clickable { showBack = !showBack },
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
                                        Text(
                                            text = if (!showBack) "Tap to show answer" else "Swipe right for correct, left for incorrect,\nTap to flip back",
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }            }
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
        "it" to "Italian",
        "ja" to "Japanese",
        "zh" to "Chinese",
        "ko" to "Korean",
        "ru" to "Russian",
        "pt" to "Portuguese",
        "nl" to "Dutch",
        "tr" to "Turkish",
        "ar" to "Arabic",
        "el" to "Greek",
        "pl" to "Polish",
        "sv" to "Swedish",
        "da" to "Danish",
        "no" to "Norwegian",
        "fi" to "Finnish",
        "hi" to "Hindi",
        "id" to "Indonesian",
        "th" to "Thai",
        "vi" to "Vietnamese"
    ).sortedBy { it.second }

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
            LanguageDropdown(
                label = "Front",
                selectedCode = frontLang,
                onLanguageSelected = { frontLang = it },
                languages = languages,
                modifier = Modifier.weight(1f)
            )
            LanguageDropdown(
                label = "Back",
                selectedCode = backLang,
                onLanguageSelected = { backLang = it },
                languages = languages,
                modifier = Modifier.weight(1f)
            )
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
    FlashcardForm(
        buttonText = stringResource(R.string.save),
        onSave = onSave
    )
}

@Composable
fun EditFlashcardScreen(
    initialFront: String,
    initialBack: String,
    onSave: (String, String) -> Unit
) {
    FlashcardForm(
        initialFront = initialFront,
        initialBack = initialBack,
        buttonText = "Update",
        onSave = onSave
    )
}

@Composable
fun FlashcardItem(
    flashcard: Flashcard,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(flashcard.dueDate))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = flashcard.front, style = MaterialTheme.typography.bodyLarge)
                    Text(text = flashcard.back, style = MaterialTheme.typography.bodyMedium)
                }
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                expanded = false
                                onEditClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                expanded = false
                                onDeleteClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        )
                    }
                }
            }
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

@Composable
fun LanguageDropdown(
    label: String,
    selectedCode: String,
    onLanguageSelected: (String) -> Unit,
    languages: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$label: ${languages.find { it.first == selectedCode }?.second ?: selectedCode}",
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            languages.forEach { (code, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onLanguageSelected(code)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FlashcardForm(
    initialFront: String = "",
    initialBack: String = "",
    buttonText: String,
    onSave: (String, String) -> Unit
) {
    var front by remember { mutableStateOf(initialFront) }
    var back by remember { mutableStateOf(initialBack) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = if (initialFront.isEmpty()) "Add Flashcard" else "Edit Flashcard", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = front,
            onValueChange = { front = it },
            label = { Text("Front") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = back,
            onValueChange = { back = it },
            label = { Text("Back") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { if (front.isNotBlank()) onSave(front, back) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(buttonText)
        }
    }
}

@Composable
fun ReviewSummaryScreen(
    correct: Int,
    incorrect: Int,
    onBackToDeck: () -> Unit
) {
    val total = correct + incorrect
    val percentage = if (total > 0) (correct.toFloat() / total * 100).toInt() else 0

    val message = remember(percentage) {
        val highMessages = listOf(
            "Outstanding work!",
            "You crushed it!",
            "Impressive mastery!",
            "Sharp memory!"
        )
        val mediumMessages = listOf(
            "Good job, keep it up!",
            "Solid progress!",
            "Getting there!",
            "Nice effort!"
        )
        val lowMessages = listOf(
            "Practice makes perfect.",
            "Keep studying, you'll get it!",
            "Don't give up!",
            "Every mistake is a lesson."
        )

        when {
            percentage >= 80 -> highMessages.random()
            percentage >= 50 -> mediumMessages.random()
            else -> lowMessages.random()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Session Complete", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(text = "$percentage%", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
        Text(text = "Accuracy", style = MaterialTheme.typography.labelLarge)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(text = message, style = MaterialTheme.typography.headlineSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$correct", style = MaterialTheme.typography.headlineMedium, color = Color.Green)
                Text(text = "Correct", style = MaterialTheme.typography.bodyMedium)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$incorrect", style = MaterialTheme.typography.headlineMedium, color = Color.Red)
                Text(text = "Incorrect", style = MaterialTheme.typography.bodyMedium)
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        ExtendedFloatingActionButton(
            onClick = onBackToDeck,
            modifier = Modifier.fillMaxWidth(),
            icon = { Icon(Icons.Default.Check, contentDescription = null) },
            text = { Text("Back to Deck") }
        )
    }
}