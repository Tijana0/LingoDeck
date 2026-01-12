package at.ac.fhstp.flashcardapp.data

data class Flashcard(
    val id: Int = 0,
    val deckId: Int,
    val front: String,
    val back: String,
    val dueDate: Long,
    val interval: Float,
    val easeFactor: Float
)