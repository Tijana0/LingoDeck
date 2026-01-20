package at.ac.fhstp.flashcardapp.db

data class DeckStats(
    val deckId: Int,
    val totalCards: Int,
    val dueCards: Int
)
