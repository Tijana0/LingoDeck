package at.ac.fhstp.flashcardapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import at.ac.fhstp.flashcardapp.FlashcardApplication
import at.ac.fhstp.flashcardapp.MainActivity
import at.ac.fhstp.flashcardapp.R
import at.ac.fhstp.flashcardapp.logic.NotificationHelper
import kotlinx.coroutines.flow.first

class ReviewReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = (applicationContext as FlashcardApplication).flashcardRepository
        val dueCount = repository.getAllDueFlashcards().first().size

        if (dueCount > 0) {
            NotificationHelper.sendNotification(applicationContext, dueCount)
        }

        return Result.success()
    }
}
