package at.ac.fhstp.flashcardapp.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFlashcard(flashcard: FlashcardEntity)

    @Update
    suspend fun updateFlashcard(flashcard: FlashcardEntity)

    @Delete
    suspend fun deleteFlashcard(flashcard: FlashcardEntity)

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId")
    fun getFlashcardsForDeck(deckId: Int): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId")
    suspend fun getFlashcardsForDeckList(deckId: Int): List<FlashcardEntity>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId AND dueDate <= (strftime('%s', 'now') * 1000)")
    fun getDueFlashcards(deckId: Int): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId AND dueDate <= (strftime('%s', 'now') * 1000)")
    suspend fun getDueFlashcardsList(deckId: Int): List<FlashcardEntity>

    @Query("SELECT * FROM flashcards WHERE dueDate <= (strftime('%s', 'now') * 1000)")
    fun getAllDueFlashcards(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards")
    fun getAllFlashcards(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE id = :id LIMIT 1")
    suspend fun getFlashcardById(id: Int): FlashcardEntity?

    @Query("SELECT deckId, COUNT(*) as totalCards, SUM(CASE WHEN dueDate <= (strftime('%s', 'now') * 1000) THEN 1 ELSE 0 END) as dueCards FROM flashcards GROUP BY deckId")
    fun getDeckStats(): Flow<List<DeckStats>>
}