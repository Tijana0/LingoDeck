package at.ac.fhstp.flashcardapp.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import at.ac.fhstp.flashcardapp.data.Deck

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
                    containerColor = Color(0xFF9C27B0), //CTA color
                    contentColor = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            //GRADIENT HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF5A4BFF), // blue
                                Color(0xFF8B3DFF)  // purple
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Text(
                        text = "LingoDeck",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Your language decks for smarter learning",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

// CONTENT
            if (decks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No decks available")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(decks) { deck ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .clickable { onDeckClick(deck) },
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            border = BorderStroke(
                                1.6.dp,
                                Color(0xFF5A4BFF).copy(alpha = 0.28f)
                            )

                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFF1E293B),
                                                Color(0xFF0F172A)
                                            )
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {

                                    // LEFT SIDE
                                    Row(verticalAlignment = Alignment.CenterVertically) {

                                        // Icon bubble
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(
                                                            Color(0xFF6D5CFF),
                                                            Color(0xFF8B5CF6)
                                                        )
                                                    ),
                                                    RoundedCornerShape(14.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Book,
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Text(
                                            text = deck.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                    }

                                    // DELETE BUTTON
                                    IconButton(
                                        onClick = {
                                            deckToDelete = deck
                                            showDeleteDialog = true
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete deck",
                                            tint = Color(0xFFFF6B6B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }


        }
    }
}
