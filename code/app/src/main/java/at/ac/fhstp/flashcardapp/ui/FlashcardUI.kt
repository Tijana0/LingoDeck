package at.ac.fhstp.flashcardapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import at.ac.fhstp.flashcardapp.AppViewModelProvider
import at.ac.fhstp.flashcardapp.data.Flashcard

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
