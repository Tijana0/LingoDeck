package at.ac.fhstp.flashcardapp.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import at.ac.fhstp.flashcardapp.data.Flashcard

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
                if (flashcards.isNotEmpty()) {
                    val isReview = dueFlashcardsCount > 0
                    val count = if (isReview) minOf(dueFlashcardsCount, 20) else minOf(flashcards.size, 20)
                    val label = if (isReview) "Start Study Session ($count)" else "Practice ($count)"
                    
                    ExtendedFloatingActionButton(
                        onClick = if (isReview) onStartReviewClick else onStartPracticeClick,
                        modifier = Modifier.weight(1f),
                        containerColor = Color(0xFF6D5CFF),
                        contentColor = Color.White,
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                        text = { Text(label) }
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                FloatingActionButton(
                    onClick = onAddFlashcardClick,
                    containerColor = Color(0xFF6D5CFF),
                    contentColor = Color.White
                ) {
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
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                items(flashcards) { flashcard ->
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
    val barColor = Color(0xFF6D5CFF)

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
