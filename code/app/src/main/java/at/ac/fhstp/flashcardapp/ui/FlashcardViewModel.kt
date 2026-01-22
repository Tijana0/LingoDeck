package at.ac.fhstp.flashcardapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.ac.fhstp.flashcardapp.data.Deck
import at.ac.fhstp.flashcardapp.data.Flashcard
import at.ac.fhstp.flashcardapp.data.FlashcardRepository
import at.ac.fhstp.flashcardapp.logic.AnkiImporter
import at.ac.fhstp.flashcardapp.logic.NotificationHelper
import at.ac.fhstp.flashcardapp.logic.SpacedRepetition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DeckUiModel(
    val deck: Deck,
    val totalCards: Int,
    val dueCards: Int
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FlashcardViewModel(private val repository: FlashcardRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.initialize()
        }
    }

    val decksUiState: StateFlow<List<DeckUiModel>> = combine(
        repository.allDecks,
        repository.getDeckStats()
    ) { decks, stats ->
        decks to stats
    }.flatMapLatest { (decks, stats) ->
        if (decks.isEmpty()) {
            kotlinx.coroutines.flow.flowOf(emptyList<DeckUiModel>())
        } else {
            combine(decks.map { deck ->
                repository.getReviewedCountToday(deck.id).map { reviewedCount ->
                    val deckStats = stats.find { it.deckId == deck.id }
                    val dailyLimit = 20
                    val remainingDue = (dailyLimit - reviewedCount).coerceAtLeast(0)
                    val totalDueInDb = deckStats?.dueCards ?: 0

                    DeckUiModel(
                        deck = deck,
                        totalCards = deckStats?.totalCards ?: 0,
                        dueCards = minOf(totalDueInDb, remainingDue)
                    )
                }
            }) { it.toList() }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalDueFlashcardsCount: StateFlow<Int> = decksUiState
        .map { list -> list.sumOf { it.dueCards } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val decks: StateFlow<List<Deck>> = repository.allDecks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _reviewCards = kotlinx.coroutines.flow.MutableStateFlow<List<Flashcard>>(emptyList())
    val reviewCards: StateFlow<List<Flashcard>> = _reviewCards

    fun startReviewSession(deckId: Int) {
        viewModelScope.launch {
            val reviewedToday = repository.getReviewedCountToday(deckId).first()
            val remainingToday = (20 - reviewedToday).coerceAtLeast(0)
            val cards = repository.getDueFlashcardsList(deckId)
            _reviewCards.value = cards.shuffled().take(remainingToday)
        }
    }

    fun startPracticeSession(deckId: Int) {
        viewModelScope.launch {
            val reviewedToday = repository.getReviewedCountToday(deckId).first()
            val remainingToday = (20 - reviewedToday).coerceAtLeast(0)
            val cards = repository.getFlashcardsForDeckList(deckId)
            _reviewCards.value = cards.shuffled().take(remainingToday)
        }
    }

    fun importAnkiDeck(context: android.content.Context, uri: android.net.Uri, deckName: String) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    AnkiImporter(context, repository).importApkg(inputStream, deckName)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addDeck(name: String, frontLanguage: String, backLanguage: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.addDeck(name, frontLanguage, backLanguage)
            onComplete()
        }
    }

    fun updateDeck(deck: Deck, name: String, frontLanguage: String, backLanguage: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateDeck(deck.copy(name = name, frontLanguage = frontLanguage, backLanguage = backLanguage))
            onComplete()
        }
    }
    
    suspend fun getDeckById(deckId: Int): Deck? {
        return repository.getDeckById(deckId)
    }

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch {
            repository.deleteDeck(deck)
        }
    }

    fun getFlashcardsForDeck(deckId: Int): Flow<List<Flashcard>> {
        return repository.getFlashcardsForDeck(deckId)
    }

    fun getDueFlashcards(deckId: Int): Flow<List<Flashcard>> {
        return repository.getDueFlashcards(deckId)
    }

    fun addFlashcard(deckId: Int, front: String, back: String) {
        viewModelScope.launch {
            repository.addFlashcard(deckId, front, back)
        }
    }

    fun updateFlashcard(flashcard: Flashcard, newFront: String, newBack: String) {
        viewModelScope.launch {
            repository.updateFlashcard(flashcard.copy(front = newFront, back = newBack))
        }
    }

    suspend fun getFlashcardById(id: Int): Flashcard? {
        return repository.getFlashcardById(id)
    }

    fun deleteFlashcard(flashcard: Flashcard) {
        viewModelScope.launch {
            repository.deleteFlashcard(flashcard)
        }
    }

    fun getUpcomingReviewsChartData(flashcards: List<Flashcard>): List<Pair<String, Int>> {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val labelFormatter = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
        val calendar = java.util.Calendar.getInstance()

        val groupedCards = flashcards
            .groupBy { formatter.format(java.util.Date(it.dueDate)) }
            .mapValues { it.value.size }

        val result = mutableListOf<Pair<String, Int>>()

        for (i in 0 until 7) {
            val date = calendar.time
            val dateString = formatter.format(date)
            val count = groupedCards[dateString] ?: 0
            val label = labelFormatter.format(date)

            result.add(Pair(label, count))
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        return result
    }

    fun processAnswer(flashcard: Flashcard, isCorrect: Boolean) {
        // Optimistic update: Remove card immediately to update UI
        _reviewCards.value = _reviewCards.value.filter { it.id != flashcard.id }

        viewModelScope.launch {
            val updatedFlashcard = SpacedRepetition.processAnswer(flashcard, isCorrect)
            repository.updateFlashcard(updatedFlashcard)
        }
    }

    fun logSession(deckId: Int, correct: Int, incorrect: Int) {
        viewModelScope.launch {
            repository.logSession(deckId, correct, incorrect)
        }
    }

    fun getDeckAccuracy(deckId: Int): Flow<Float?> {
        return repository.getAverageAccuracy(deckId)
    }

    fun testNotification(context: android.content.Context) {
        NotificationHelper.sendNotification(context, totalDueFlashcardsCount.value)
    }
}
