package at.ac.fhstp.flashcardapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [FlashcardEntity::class, DeckEntity::class], version = 2)
abstract class FlashcardDatabase : RoomDatabase() {
    abstract fun flashcardDao(): FlashcardDao
    abstract fun deckDao(): DeckDao

    companion object {
        @Volatile
        private var Instance: FlashcardDatabase? = null

        fun getDatabase(context: Context): FlashcardDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, FlashcardDatabase::class.java, "flashcard_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                Instance?.let { database ->
                                    val deckDao = database.deckDao()
                                    val flashcardDao = database.flashcardDao()
                                    val deckId = deckDao.addDeck(DeckEntity(name = "German to English")).toInt()
                                    flashcardDao.addFlashcard(FlashcardEntity(deckId = deckId, front = "Hallo", back = "Hello"))
                                    flashcardDao.addFlashcard(FlashcardEntity(deckId = deckId, front = "Danke", back = "Thank you"))
                                    flashcardDao.addFlashcard(FlashcardEntity(deckId = deckId, front = "Bitte", back = "Please / You're welcome"))
                                    flashcardDao.addFlashcard(FlashcardEntity(deckId = deckId, front = "Guten Morgen", back = "Good morning"))
                                    flashcardDao.addFlashcard(FlashcardEntity(deckId = deckId, front = "Auf Wiedersehen", back = "Goodbye"))
                                }
                            }
                        }
                    })
                    .build()
                    .also { Instance = it }
            }
        }
    }
}