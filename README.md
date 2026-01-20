# Flashcard App: LingoDeck

Pages: https://ccl3-ws2025-e53d7a.pages.nwt.fhstp.ac.at

LingoDeck is a native Android flashcard application for language learning, built with Jetpack Compose. The app uses a Spaced Repetition System (SRS) to optimize learning efficiency and supports importing existing decks from Anki.

## Features

### Learning
* **Spaced Repetition:** Smart scheduling that shows you cards right before you're likely to forget them.
* **Practice Mode:** Study any deck at any time, even if no reviews are currently scheduled.
* **Session Limits:** Focused study sessions capped at 20 cards to prevent burnout.
* **Progress Tracking:** Real-time "X / Y" counter during sessions.

### Interactive Review
* **Gesture-Based Interface:** Swipe right for correct, left for incorrect.
* **Anti-Cheat Logic:** Swipe is disabled until the card is flipped to ensure you actually see the answer.
* **Toggle Flip:** Tap cards to flip between front and back as many times as needed.
* **Visual Feedback:** Color-coded borders (Green/Red) that react to your swipe direction.

### Multimedia & Languages
* **Native Text-to-Speech:** Voice output for both sides of the card.
* **Multi-Language Support:** Choose from over 20 languages (including English, German, French, Japanese, Indonesian, and more).
* **Auto-Play:** Optional automatic audio playback when a card is revealed.

### Data Management
* **Anki Import:** Easily import .apkg files. The app automatically unzips and parses the Anki SQLite database.
* **Deck Management:** Organize cards into decks with custom language settings.
* **Edit/Delete:** Full control over your data with intuitive dropdown menus and safety confirmation dialogs.

### Insights & Motivation
* **Review Timeline:** A 7-day chart showing your upcoming workload.
* **Session Summary:** Detailed stats (accuracy %, correct/incorrect counts) and randomized motivational messages after every session.

## Tech Stack

* **Language:** Kotlin
* **UI:** Jetpack Compose (Material 3)
* **Database:** Room (SQLite) for persistence and local storage.
* **Concurrency:** Kotlin Coroutines & Flow for reactive UI updates.
* **Navigation:** Compose Navigation.
* **Audio:** Android TTS (Text-to-Speech) Engine.
* **IO:** ZipInputStream for Anki archive extraction.

## Installation

1.  Clone the repository.
2.  Open the project in Android Studio (Ladybug or newer).
3.  Sync Gradle and run on a physical device or emulator (API 26+).

## Team Members

* Tijana Mijatović
* Diana Simonicova