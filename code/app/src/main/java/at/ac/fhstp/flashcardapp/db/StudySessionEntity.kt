package at.ac.fhstp.flashcardapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deckId: Int,
    val date: Long,
    val correct: Int,
    val incorrect: Int
)