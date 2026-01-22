package at.ac.fhstp.flashcardapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [FlashcardEntity::class, DeckEntity::class, StudySessionEntity::class], version = 5)
abstract class FlashcardDatabase : RoomDatabase() {
    abstract fun flashcardDao(): FlashcardDao
    abstract fun deckDao(): DeckDao
    abstract fun studySessionDao(): StudySessionDao

    companion object {
        @Volatile
        private var Instance: FlashcardDatabase? = null

        fun getDatabase(context: Context): FlashcardDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, FlashcardDatabase::class.java, "flashcard_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            val time = System.currentTimeMillis()
                            
                            // Insert Tutorial Deck
                            db.execSQL("INSERT INTO decks (id, name, frontLanguage, backLanguage) VALUES (1, 'Welcome! \uD83D\uDE80', 'en', 'en')")
                            
                            // Insert Tutorial Cards
                            val cards = listOf(
                                "Swipe Right \uD83D\uDC49" to "To mark as Correct! \u2705",
                                "Swipe Left \uD83D\uDC48" to "To mark as Wrong \u274C",
                                "Tap the card \uD83D\uDC46" to "To flip it! \uD83D\uDD04",
                                "Daily Limit \uD83D\uDCC5" to "20 cards per deck! \uD83D\uDED1",
                                "Enjoy!" to "Have fun learning! \uD83C\uDF89"
                            )
                            
                            cards.forEach { (front, back) ->
                                db.execSQL("INSERT INTO flashcards (deckId, front, back, dueDate, interval, easeFactor) VALUES (1, '$front', '$back', $time, 1.0, 2.5)")
                            }
                        }
                    })
                    .build()
                    .also { Instance = it }
            }
        }
    }
}