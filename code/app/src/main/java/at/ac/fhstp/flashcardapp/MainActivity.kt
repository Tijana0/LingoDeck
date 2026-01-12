package at.ac.fhstp.flashcardapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import at.ac.fhstp.flashcardapp.ui.FlashcardApp
import at.ac.fhstp.flashcardapp.ui.theme.FlashcardAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlashcardAppTheme {
                FlashcardApp()
            }
        }
    }
}