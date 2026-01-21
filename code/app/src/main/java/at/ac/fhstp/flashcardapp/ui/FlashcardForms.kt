package at.ac.fhstp.flashcardapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import at.ac.fhstp.flashcardapp.R

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert

@Composable
fun AddDeckScreen(
    onSave: (String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    DeckForm(
        title = "Add a new deck",
        buttonText = "Save",
        onSave = onSave,
        onCancel = onCancel
    )
}

@Composable
fun EditDeckScreen(
    initialName: String,
    initialFront: String,
    initialBack: String,
    onSave: (String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    DeckForm(
        title = "Edit deck",
        buttonText = "Update",
        initialName = initialName,
        initialFront = initialFront,
        initialBack = initialBack,
        onSave = onSave,
        onCancel = onCancel
    )
}

@Composable
fun DeckForm(
    title: String,
    buttonText: String,
    initialName: String = "",
    initialFront: String = "de",
    initialBack: String = "en",
    onSave: (String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var deckName by remember { mutableStateOf(initialName) }
    var frontLang by remember { mutableStateOf(initialFront) }
    var backLang by remember { mutableStateOf(initialBack) }

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
    ).sortedBy { it.second } + ("other" to "Other (No Audio)")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = deckName,
            onValueChange = { deckName = it },
            label = { Text("Deck Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = { if (deckName.isNotBlank()) onSave(deckName, frontLang, backLang) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D5CFF))
            ) {
                Text(buttonText)
            }
        }
    }
}

@Composable
fun AddFlashcardScreen(
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit
) {
    FlashcardForm(
        buttonText = stringResource(R.string.save),
        onSave = onSave,
        onCancel = onCancel
    )
}

@Composable
fun EditFlashcardScreen(
    initialFront: String,
    initialBack: String,
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit
) {
    FlashcardForm(
        initialFront = initialFront,
        initialBack = initialBack,
        buttonText = "Update",
        onSave = onSave,
        onCancel = onCancel
    )
}

@Composable
fun FlashcardForm(
    initialFront: String = "",
    initialBack: String = "",
    buttonText: String,
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit
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
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = {
                    val temp = front
                    front = back
                    back = temp
                }
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Swap",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        OutlinedTextField(
            value = back,
            onValueChange = { back = it },
            label = { Text("Back") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = { if (front.isNotBlank()) onSave(front, back) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D5CFF))
            ) {
                Text(buttonText)
            }
        }
    }
}