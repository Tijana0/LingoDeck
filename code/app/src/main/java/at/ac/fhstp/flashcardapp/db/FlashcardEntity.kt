package at.ac.fhstp.flashcardapp.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val deckId: Int,
    val front: String,
    val back: String,
    val dueDate: Long = System.currentTimeMillis(),
    val interval: Float = 1f,
    val easeFactor: Float = 2.5f
)