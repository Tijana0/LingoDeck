package at.ac.fhstp.flashcardapp.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDeck(deck: DeckEntity): Long

    @Delete
    suspend fun deleteDeck(deck: DeckEntity)

    @Query("SELECT * FROM decks")
    fun getAllDecks(): Flow<List<DeckEntity>>
}
