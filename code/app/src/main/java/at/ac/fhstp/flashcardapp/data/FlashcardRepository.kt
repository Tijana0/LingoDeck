package at.ac.fhstp.flashcardapp.data

import at.ac.fhstp.flashcardapp.db.DeckDao
import at.ac.fhstp.flashcardapp.db.DeckEntity
import at.ac.fhstp.flashcardapp.db.FlashcardEntity
import at.ac.fhstp.flashcardapp.db.FlashcardDao
import at.ac.fhstp.flashcardapp.db.StudySessionDao
import at.ac.fhstp.flashcardapp.db.StudySessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class FlashcardRepository(
    private val flashcardDao: FlashcardDao,
    private val deckDao: DeckDao,
    private val studySessionDao: StudySessionDao
) {

    val allDecks: Flow<List<Deck>> = deckDao.getAllDecks().map { entities ->
        entities.map { entity ->
            Deck(entity.id, entity.name, entity.frontLanguage, entity.backLanguage)
        }
    }

    suspend fun addDeck(name: String, frontLanguage: String = "de", backLanguage: String = "en"): Long {
        val entity = DeckEntity(name = name, frontLanguage = frontLanguage, backLanguage = backLanguage)
        return deckDao.addDeck(entity)
    }

    suspend fun deleteDeck(deck: Deck) {
        val entity = DeckEntity(id = deck.id, name = deck.name, frontLanguage = deck.frontLanguage, backLanguage = deck.backLanguage)
        deckDao.deleteDeck(entity)
    }

    suspend fun updateDeck(deck: Deck) {
        val entity = DeckEntity(id = deck.id, name = deck.name, frontLanguage = deck.frontLanguage, backLanguage = deck.backLanguage)
        deckDao.updateDeck(entity)
    }

    suspend fun getDeckById(deckId: Int): Deck? {
        val entity = deckDao.getDeckById(deckId)
        return entity?.let { Deck(it.id, it.name, it.frontLanguage, it.backLanguage) }
    }

    fun getFlashcardsForDeck(deckId: Int): Flow<List<Flashcard>> {
        return flashcardDao.getFlashcardsForDeck(deckId).map { entities ->
            entities.map { entity ->
                Flashcard(
                    entity.id,
                    entity.deckId,
                    entity.front,
                    entity.back,
                    entity.dueDate,
                    entity.interval,
                    entity.easeFactor
                )
            }
        }
    }

    suspend fun getFlashcardsForDeckList(deckId: Int): List<Flashcard> {
        return flashcardDao.getFlashcardsForDeckList(deckId).map { entity ->
            Flashcard(
                entity.id,
                entity.deckId,
                entity.front,
                entity.back,
                entity.dueDate,
                entity.interval,
                entity.easeFactor
            )
        }
    }

    fun getDueFlashcards(deckId: Int): Flow<List<Flashcard>> {
        return flashcardDao.getDueFlashcards(deckId).map { entities ->
            entities.map { entity ->
                Flashcard(
                    entity.id,
                    entity.deckId,
                    entity.front,
                    entity.back,
                    entity.dueDate,
                    entity.interval,
                    entity.easeFactor
                )
            }
        }
    }

    suspend fun getDueFlashcardsList(deckId: Int): List<Flashcard> {
        return flashcardDao.getDueFlashcardsList(deckId).map { entity ->
            Flashcard(
                entity.id,
                entity.deckId,
                entity.front,
                entity.back,
                entity.dueDate,
                entity.interval,
                entity.easeFactor
            )
        }
    }

    fun getAllDueFlashcards(): Flow<List<Flashcard>> {
        return flashcardDao.getAllDueFlashcards().map { entities ->
            entities.map { entity ->
                Flashcard(
                    entity.id,
                    entity.deckId,
                    entity.front,
                    entity.back,
                    entity.dueDate,
                    entity.interval,
                    entity.easeFactor
                )
            }
        }
    }

    suspend fun addFlashcard(deckId: Int, front: String, back: String) {
        val entity = FlashcardEntity(deckId = deckId, front = front, back = back)
        flashcardDao.addFlashcard(entity)
    }

    suspend fun updateFlashcard(flashcard: Flashcard) {
        val entity = FlashcardEntity(
            id = flashcard.id,
            deckId = flashcard.deckId,
            front = flashcard.front,
            back = flashcard.back,
            dueDate = flashcard.dueDate,
            interval = flashcard.interval,
            easeFactor = flashcard.easeFactor
        )
        flashcardDao.updateFlashcard(entity)
    }

    suspend fun getFlashcardById(id: Int): Flashcard? {
        val entity = flashcardDao.getFlashcardById(id)
        return entity?.let {
            Flashcard(
                it.id,
                it.deckId,
                it.front,
                it.back,
                it.dueDate,
                it.interval,
                it.easeFactor
            )
        }
    }

    suspend fun deleteFlashcard(flashcard: Flashcard) {
        val entity = FlashcardEntity(
            id = flashcard.id,
            deckId = flashcard.deckId,
            front = flashcard.front,
            back = flashcard.back,
            dueDate = flashcard.dueDate,
            interval = flashcard.interval,
            easeFactor = flashcard.easeFactor
        )
        flashcardDao.deleteFlashcard(entity)
    }

    fun getDeckStats(): Flow<List<at.ac.fhstp.flashcardapp.db.DeckStats>> {
        return flashcardDao.getDeckStats()
    }

    suspend fun logSession(deckId: Int, correct: Int, incorrect: Int) {
        val session = StudySessionEntity(
            deckId = deckId,
            date = System.currentTimeMillis(),
            correct = correct,
            incorrect = incorrect
        )
        studySessionDao.insertSession(session)
    }

    fun getAverageAccuracy(deckId: Int): Flow<Float?> {
        return studySessionDao.getAverageAccuracy(deckId)
    }

    suspend fun initialize() {
        if (deckDao.getAllDecks().first().isEmpty()) {
            val deckId = addDeck("Toki Pona", "other", "other").toInt()
            val time = System.currentTimeMillis()
            val cards = listOf(
                "toki" to "hello, language, speech",
                "pona" to "good, simple, friendly",
                "moku" to "eat, drink, food",
                "soweli" to "animal, beast",
                "telo" to "water, fluid",
                "kili" to "fruit, vegetable",
                "lili" to "small, little",
                "suli" to "big, tall, important",
                "mi" to "I, me, my",
                "sina" to "you, your"
            )
            cards.forEach { (front, back) ->
                val entity = FlashcardEntity(deckId = deckId, front = front, back = back, dueDate = time, interval = 1.0f, easeFactor = 2.5f)
                flashcardDao.addFlashcard(entity)
            }
        }
    }

    fun getReviewedCountToday(deckId: Int): Flow<Int> {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return studySessionDao.getReviewedCountToday(deckId, calendar.timeInMillis).map { it ?: 0 }
    }
}
