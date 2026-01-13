package at.ac.fhstp.flashcardapp.data

data class Deck(
    val id: Int = 0,
    val name: String,
    val frontLanguage: String = "de",
    val backLanguage: String = "en"
)