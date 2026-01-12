package at.ac.fhstp.flashcardapp.data

import at.ac.fhstp.flashcardapp.db.DeckDao
import at.ac.fhstp.flashcardapp.db.DeckEntity
import at.ac.fhstp.flashcardapp.db.FlashcardEntity
import at.ac.fhstp.flashcardapp.db.FlashcardDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FlashcardRepository(
    private val flashcardDao: FlashcardDao,
    private val deckDao: DeckDao
) {

    // Deck methods
    val allDecks: Flow<List<Deck>> = deckDao.getAllDecks().map { entities ->
        entities.map { entity ->
            Deck(entity.id, entity.name)
        }
    }

    suspend fun addDeck(name: String) {
        val entity = DeckEntity(name = name)
        deckDao.addDeck(entity)
    }

    suspend fun deleteDeck(deck: Deck) {
        val entity = DeckEntity(id = deck.id, name = deck.name)
        deckDao.deleteDeck(entity)
    }

    // Flashcard methods
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

    fun getDueFlashcards(deckId: Int): Flow<List<Flashcard>> {
        return flashcardDao.getDueFlashcards(deckId, System.currentTimeMillis()).map { entities ->
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
}