package at.ac.fhstp.flashcardapp.ui

import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.ac.fhstp.flashcardapp.data.Flashcard
import at.ac.fhstp.flashcardapp.ui.theme.*

@Composable
fun DeckDetailScreen(
    deckName: String,
    flashcards: List<Flashcard>,
    dueFlashcardsCount: Int,
    chartData: List<Pair<String, Int>>,
    averageAccuracy: Float?,
    onStartReviewClick: () -> Unit,
    onStartPracticeClick: () -> Unit,
    onAddFlashcardClick: () -> Unit,
    onEditFlashcardClick: (Flashcard) -> Unit,
    onDeleteFlashcardClick: (Flashcard) -> Unit,
    onBackClick: () -> Unit
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
            },
            containerColor = CardBg,
            titleContentColor = TextWhite,
            textContentColor = TextGray
        )
    }

    Scaffold(
        containerColor = BgDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddFlashcardClick,
                containerColor = AccentPurple,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        floatingActionButtonPosition = FabPosition.EndOverlay
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Bar Section
            item {
                TopHeaderSection(deckName, onBackClick)
            }

            // 2. Statistics Row
            item {
                val accuracyText = if (averageAccuracy != null) "${"%.0f".format(averageAccuracy)}%" else "-"
                StatsRow(dueFlashcardsCount, flashcards.size, accuracyText)
            }

            // 3. Action Buttons
            item {
                ActionButtonsRow(
                    onStartReviewClick = onStartReviewClick,
                    onStartPracticeClick = onStartPracticeClick,
                    dueCount = dueFlashcardsCount,
                    totalCount = flashcards.size
                )
            }

            // 4. Review History Chart
            item {
                ReviewHistoryCard(data = chartData)
            }

            // 5. Flashcards List Header
            item {
                Text(
                    text = "Flashcards (${flashcards.size})",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            // 6. Flashcards Items
            items(flashcards) { card ->
                DetailFlashcardItem(
                    card = card,
                    onEditClick = { onEditFlashcardClick(card) },
                    onDeleteClick = {
                        flashcardToDelete = card
                        showDeleteDialog = true
                    }
                )
            }

            // Bottom spacer for scroll
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun TopHeaderSection(deckName: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextWhite
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = deckName,
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Deck Details",
                color = TextGray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun StatsRow(dueCount: Int, totalCount: Int, accuracy: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.CalendarToday,
            value = dueCount.toString(),
            label = "Due",
            color = AccentBlue
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.ShowChart,
            value = totalCount.toString(),
            label = "Total",
            color = AccentPurple
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.TrendingUp,
            value = accuracy,
            label = "Accuracy",
            color = AccentGreen
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = BgDark),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = color, fontSize = 12.sp)
        }
    }
}

@Composable
fun ActionButtonsRow(
    onStartReviewClick: () -> Unit,
    onStartPracticeClick: () -> Unit,
    dueCount: Int,
    totalCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onStartReviewClick,
            enabled = dueCount > 0,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentPurple,
                disabledContainerColor = AccentPurple.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Review ($dueCount)", color = TextWhite)
        }
        
        OutlinedButton(
            onClick = onStartPracticeClick,
            enabled = totalCount > 0,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPurple),
            border = BorderStroke(1.dp, AccentPurple),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Practice")
        }
    }
}

@Composable
fun ReviewHistoryCard(data: List<Pair<String, Int>>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Upcoming Schedule",
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 10.dp)) {
                ReviewChart(data = data)
            }
        }
    }
}

@Composable
fun ReviewChart(data: List<Pair<String, Int>>) {
    if (data.isEmpty()) return
    
    val maxVal = data.maxOfOrNull { it.second }?.toFloat()?.coerceAtLeast(5f) ?: 5f
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val spacing = width / (data.size - 1)
        val bottomMargin = 40f 

        // 1. Draw Grid Lines
        val gridLines = 5
        val stepY = (height - bottomMargin) / (gridLines - 1)
        
        for (i in 0 until gridLines) {
            val y = stepY * i
            drawLine(
                color = GridLineColor,
                start = Offset(30f, y),
                end = Offset(width, y),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
            
            drawContext.canvas.nativeCanvas.drawText(
                (maxVal - (maxVal / (gridLines - 1) * i)).toInt().toString(),
                0f,
                y + 10f,
                Paint().apply {
                    color = android.graphics.Color.parseColor("#8B949E")
                    textSize = 24f
                }
            )
        }

        // Path for the line
        val path = Path()
        
        fun getY(value: Float): Float {
            val drawableHeight = height - bottomMargin
            return drawableHeight - (value / maxVal * drawableHeight)
        }

        data.forEachIndexed { index, point ->
            val x = 50f + (index * ((width - 60f) / (data.size - 1)))
            
            // Draw X Labels
            drawContext.canvas.nativeCanvas.drawText(
                point.first,
                x - 30f,
                height, 
                Paint().apply {
                    color = android.graphics.Color.parseColor("#8B949E")
                    textSize = 24f
                }
            )

            val y = getY(point.second.toFloat())

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                val prevX = 50f + ((index - 1) * ((width - 60f) / (data.size - 1)))
                val prevY = getY(data[index - 1].second.toFloat())
                val conX1 = (prevX + x) / 2f
                path.cubicTo(conX1, prevY, conX1, y, x, y)
            }
        }

        drawPath(
            path = path,
            color = AccentBlue,
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )

        // Draw Dots
        data.forEachIndexed { index, point ->
            val x = 50f + (index * ((width - 60f) / (data.size - 1)))
            val y = getY(point.second.toFloat())

            drawCircle(Color(0xFF1E2330), radius = 10f, center = Offset(x, y))
            drawCircle(AccentBlue, radius = 6f, center = Offset(x, y))
        }
    }
}

@Composable
fun DetailFlashcardItem(
    card: Flashcard,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.front,
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = card.back,
                    color = TextGray,
                    fontSize = 14.sp
                )
            }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TextGray
                    )
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
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            expanded = false
                            onDeleteClick()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                    )
                }
            }
        }
    }
}