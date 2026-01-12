package at.ac.fhstp.flashcardapp

import android.app.Application
import at.ac.fhstp.flashcardapp.data.FlashcardRepository
import at.ac.fhstp.flashcardapp.db.FlashcardDatabase

class FlashcardApplication : Application() {
    // Lazy initialization of the repository
    val flashcardRepository by lazy {
        val database = FlashcardDatabase.getDatabase(this)
        FlashcardRepository(database.flashcardDao(), database.deckDao())
    }
}