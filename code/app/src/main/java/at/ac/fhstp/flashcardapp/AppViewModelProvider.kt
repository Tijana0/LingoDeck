package at.ac.fhstp.flashcardapp

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import at.ac.fhstp.flashcardapp.ui.FlashcardViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            FlashcardViewModel(flashcardApplication().flashcardRepository)
        }
    }
}

fun CreationExtras.flashcardApplication(): FlashcardApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FlashcardApplication)
