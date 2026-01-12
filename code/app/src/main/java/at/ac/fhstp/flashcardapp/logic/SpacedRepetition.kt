package at.ac.fhstp.flashcardapp.logic

import at.ac.fhstp.flashcardapp.data.Flashcard
import kotlin.math.roundToInt

object SpacedRepetition {

    fun processAnswer(flashcard: Flashcard, isCorrect: Boolean): Flashcard {
        if (isCorrect) {
            val newInterval = when (flashcard.interval) {
                1f -> 6f
                else -> flashcard.interval * flashcard.easeFactor
            }
            val newEaseFactor = (flashcard.easeFactor + 0.1f).coerceAtMost(5.0f)
            return flashcard.copy(
                interval = newInterval,
                easeFactor = newEaseFactor,
                dueDate = System.currentTimeMillis() + (newInterval * 24 * 60 * 60 * 1000).toLong()
            )
        } else {
            return flashcard.copy(
                interval = 1f,
                easeFactor = (flashcard.easeFactor - 0.2f).coerceAtLeast(1.3f),
                dueDate = System.currentTimeMillis() + (1 * 24 * 60 * 60 * 1000)
            )
        }
    }
}
