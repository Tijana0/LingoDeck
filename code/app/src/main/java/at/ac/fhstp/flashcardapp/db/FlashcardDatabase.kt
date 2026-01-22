package at.ac.fhstp.flashcardapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [FlashcardEntity::class, DeckEntity::class, StudySessionEntity::class], version = 10)
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
                            db.execSQL("INSERT INTO decks (name, frontLanguage, backLanguage) VALUES ('Toki Pona', 'other', 'en')")
                            
                            // Insert Tutorial Cards
                            val cards = listOf(
                                "toki" to "hello, language, speech",
                                "pona" to "good, simple, friendly",
                                "moku" to "eat, drink, food",
                                "soweli" to "animal, beast",
                                "telo" to "water, fluid",
                                "kili" to "fruit, vegetable",
                                "lili" to "small, little",
                                "suli" to "big, tall, important",
                                "mi" to "I, me, my",
                                "sina" to "you, your"
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