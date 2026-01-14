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
            Deck(entity.id, entity.name, entity.frontLanguage, entity.backLanguage)
        }
    }

    suspend fun addDeck(name: String, frontLanguage: String = "de", backLanguage: String = "en") {
        val entity = DeckEntity(name = name, frontLanguage = frontLanguage, backLanguage = backLanguage)
        deckDao.addDeck(entity)
    }

    suspend fun deleteDeck(deck: Deck) {
        val entity = DeckEntity(id = deck.id, name = deck.name, frontLanguage = deck.frontLanguage, backLanguage = deck.backLanguage)
        deckDao.deleteDeck(entity)
    }

    suspend fun getDeckById(deckId: Int): Deck? {
        // Since we don't have a direct getDeckById in Dao, we could add it or filter allDecks.
        // For simplicity, let's assume we might need to fetch a single deck's metadata.
        // Let's add it to Dao later if needed. For now we will get it from the list in VM.
        return null 
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
}
