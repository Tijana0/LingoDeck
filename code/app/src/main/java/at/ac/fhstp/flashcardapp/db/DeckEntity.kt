package at.ac.fhstp.flashcardapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
