package at.ac.fhstp.flashcardapp.ui

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import at.ac.fhstp.flashcardapp.data.Deck
import at.ac.fhstp.flashcardapp.data.Flashcard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

import com.google.mlkit.nl.languageid.LanguageIdentification

@Composable
fun ReviewScreen(
    dueFlashcards: List<Flashcard>?,
    deck: Deck?,
    onAnswer: (Flashcard, Boolean) -> Unit,
    onReviewComplete: () -> Unit
) {
    var showBack by remember { mutableStateOf(false) }
    var hasFlipped by remember { mutableStateOf(false) }
    val offset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val threshold = screenWidth * 0.3f

    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    val languageIdentifier = remember { LanguageIdentification.getClient() }

    var sessionCorrect by remember { mutableIntStateOf(0) }
    var sessionIncorrect by remember { mutableIntStateOf(0) }
    var hasProcessedCards by remember { mutableStateOf(false) }
    var initialTotal by remember { mutableIntStateOf(0) }

    LaunchedEffect(dueFlashcards) {
        if (initialTotal == 0 && dueFlashcards != null && dueFlashcards.isNotEmpty()) {
            initialTotal = dueFlashcards.size
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

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
            languageIdentifier.close()
        }
    }

    fun speak(text: String, fallbackLangCode: String) {
        if (fallbackLangCode == "other") return

        if (isTtsReady && text.isNotBlank()) {
            languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener { languageCode ->
                    val locale = if (languageCode == "und") Locale(fallbackLangCode) else Locale(languageCode)
                    tts?.language = locale
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                }
                .addOnFailureListener {
                    tts?.language = Locale(fallbackLangCode)
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                }
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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
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
                val totalCount = if (initialTotal > 0) initialTotal else dueFlashcards.size + sessionCorrect + sessionIncorrect
                val currentCount = minOf(sessionCorrect + sessionIncorrect + 1, totalCount)
                Text(
                    text = "$currentCount / $totalCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                )

                val flashcard = dueFlashcards.first()

                LaunchedEffect(flashcard) {
                    showBack = false
                    hasFlipped = false
                }

                if (deck != null) {
                    val currentLang = if (showBack) deck.backLanguage else deck.frontLanguage
                    if (currentLang != "other") {
                        LaunchedEffect(flashcard, showBack, isTtsReady) {
                            if (isTtsReady) {
                                val textToSpeak = if (showBack) flashcard.back else flashcard.front
                                speak(textToSpeak, currentLang)
                            }
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
                            .pointerInput(hasFlipped) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        scope.launch {
                                            if (!hasFlipped) {
                                                snackbarHostState.currentSnackbarData?.dismiss()
                                                val snackJob = launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Please flip the card first to reveal the answer!",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                                delay(1500) // ~3x shorter than default Short (4s)
                                                snackJob.cancel()
                                                offset.animateTo(0f)
                                                return@launch
                                            }

                                            if (offset.value > threshold) {
                                                sessionCorrect++
                                                hasProcessedCards = true
                                                // Swipe Right (Correct)
                                                offset.animateTo(screenWidth)
                                                onAnswer(flashcard, true)
                                                // State reset handled by LaunchedEffect(flashcard)
                                                offset.snapTo(0f)
                                            } else if (offset.value < -threshold) {
                                                sessionIncorrect++
                                                hasProcessedCards = true
                                                // Swipe Left (Wrong)
                                                offset.animateTo(-screenWidth)
                                                onAnswer(flashcard, false)
                                                // State reset handled by LaunchedEffect(flashcard)
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
                                    if (hasFlipped) {
                                        scope.launch { offset.snapTo(offset.value + dragAmount) }
                                    }
                                }
                            }
                            .clickable { 
                                showBack = !showBack
                                if (showBack) hasFlipped = true 
                            },
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
                                val currentLang = if (showBack) deck.backLanguage else deck.frontLanguage
                                if (currentLang != "other") {
                                    FilledIconButton(
                                        onClick = {
                                            val textToSpeak = if (showBack) flashcard.back else flashcard.front
                                            speak(textToSpeak, currentLang)
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
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (!showBack) "Tap to show answer" else "Swipe right for correct, left for incorrect,\nTap to flip back",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
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
