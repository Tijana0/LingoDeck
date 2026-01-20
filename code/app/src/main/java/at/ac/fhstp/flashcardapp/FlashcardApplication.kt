package at.ac.fhstp.flashcardapp

import android.app.Application
import at.ac.fhstp.flashcardapp.data.FlashcardRepository
import at.ac.fhstp.flashcardapp.db.FlashcardDatabase

class FlashcardApplication : Application() {
    val flashcardRepository by lazy {
        val database = FlashcardDatabase.getDatabase(this)
        FlashcardRepository(database.flashcardDao(), database.deckDao(), database.studySessionDao())
    }
}