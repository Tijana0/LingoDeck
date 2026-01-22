package at.ac.fhstp.flashcardapp

import android.app.Application
import at.ac.fhstp.flashcardapp.data.FlashcardRepository
import at.ac.fhstp.flashcardapp.db.FlashcardDatabase
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import at.ac.fhstp.flashcardapp.worker.ReviewReminderWorker
import java.util.concurrent.TimeUnit

class FlashcardApplication : Application() {
    val flashcardRepository by lazy {
        val database = FlashcardDatabase.getDatabase(this)
        FlashcardRepository(database.flashcardDao(), database.deckDao(), database.studySessionDao())
    }

    override fun onCreate() {
        super.onCreate()
        scheduleReminders()
    }

    private fun scheduleReminders() {
        val workRequest = PeriodicWorkRequestBuilder<ReviewReminderWorker>(
            1, TimeUnit.HOURS // Check every hour
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "review_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}