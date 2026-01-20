package at.ac.fhstp.flashcardapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {
    @Insert
    suspend fun insertSession(session: StudySessionEntity)

    @Query("SELECT * FROM study_sessions WHERE deckId = :deckId")
    fun getSessionsForDeck(deckId: Int): Flow<List<StudySessionEntity>>
    
    @Query("SELECT AVG(CAST(correct AS FLOAT) / (correct + incorrect) * 100) FROM study_sessions WHERE deckId = :deckId AND (correct + incorrect) > 0")
    fun getAverageAccuracy(deckId: Int): Flow<Float?>

    @Query("SELECT SUM(correct + incorrect) FROM study_sessions WHERE deckId = :deckId AND date >= :startOfDay")
    fun getReviewedCountToday(deckId: Int, startOfDay: Long): Flow<Int?>
}