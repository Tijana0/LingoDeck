package at.ac.fhstp.flashcardapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import at.ac.fhstp.flashcardapp.R

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
