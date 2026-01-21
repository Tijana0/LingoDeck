package at.ac.fhstp.flashcardapp.ui

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.ac.fhstp.flashcardapp.data.Deck
import at.ac.fhstp.flashcardapp.data.Flashcard
import com.google.mlkit.nl.languageid.LanguageIdentification
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

// --- Colors ---
val SummaryBgDark = Color(0xFF161B22) 
val SummaryCardBg = Color(0xFF1E2330)
val SuccessGreen = Color(0xFF00C853)
val ErrorRed = Color(0xFFFF5252)
val InfoBlue = Color(0xFF2979FF)
val BrandPurple = Color(0xFF651FFF)
val BrandPurpleLight = Color(0xFF7C4DFF)

@Composable
fun ReviewScreen(
    dueFlashcards: List<Flashcard>?,
    deck: Deck?,
    onAnswer: (Flashcard, Boolean) -> Unit,
    onReviewComplete: () -> Unit,
    onSessionFinished: (Int, Int) -> Unit
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
        LaunchedEffect(Unit) {
            onSessionFinished(sessionCorrect, sessionIncorrect)
        }
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onReviewComplete) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Session",
                            tint = Color.Gray
                        )
                    }

                    Text(
                        text = "$currentCount / $totalCount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                val flashcard = dueFlashcards.first()
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

    Scaffold(
        containerColor = SummaryBgDark,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
            ) {
                Button(
                    onClick = onBackToDeck,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Back to Deck",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            Spacer(modifier = Modifier.height(20.dp))

            // 1. Success Header Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            // 2. Title and Subtitle
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Review Complete!",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Great job on finishing your review",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ResultCard(
                    label = "Total",
                    value = "$total",
                    color = InfoBlue,
                    modifier = Modifier.weight(1f)
                )
                ResultCard(
                    label = "Correct",
                    value = "$correct",
                    color = SuccessGreen,
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
                ResultCard(
                    label = "Wrong",
                    value = "$incorrect",
                    color = ErrorRed,
                    icon = Icons.Default.Close,
                    modifier = Modifier.weight(1f)
                )
            }

            // 4. Accuracy Large Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(BrandPurpleLight, BrandPurple)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Accuracy Rate",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp
                    )
                    Text(
                        text = "$percentage%",
                        color = Color.White,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 5. Motivational Text
            Text(
                text = "🎉 Excellent work! Keep it up!",
                color = SuccessGreen,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ResultCard(
    label: String,
    value: String,
    color: Color,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .border(width = 1.dp, color = color.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(SummaryCardBg)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Label Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = label,
                    color = color,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Bottom Value
            Text(
                text = value,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}