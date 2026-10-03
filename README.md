# LingoDeck

LingoDeck is a native Android language-learning app built with **Kotlin** and **Jetpack Compose**. It uses a spaced-repetition system to schedule flashcard reviews based on previous performance, helping learners focus on the cards that are due instead of repeatedly reviewing an entire deck.

## Project context

LingoDeck was developed as a two-person university project during a Creative Code Lab. I was responsible for the **application development** and also contributed to **user testing**, working closely with a teammate focused on UI/UX design.

## Features

- Create and manage language-learning decks
- Add and organize flashcards within each deck
- Review only cards that are currently due
- Reveal answers and grade recall through a simple review flow
- Adjust future review dates using spaced-repetition logic
- Track upcoming reviews and study workload
- Store decks, cards, and review state locally on the device

## Tech stack

- **Kotlin**
- **Jetpack Compose** + Material 3
- **Room** for local persistence
- **Navigation Compose**
- **ViewModel / repository-based data flow**
- **Gradle** + KSP

## How the spaced-repetition logic works

Each flashcard stores a due date, review interval, and ease factor. After a review, the scheduling logic updates those values depending on whether the learner remembered the card.

A successful review increases the interval before the card appears again, while an unsuccessful review shortens the interval so the card returns sooner.

The scheduling logic is kept separate from the UI so it can be changed or extended without tightly coupling it to the Compose screens.

## Architecture

The app is organized into separate layers for UI, data access, persistence, and review logic:

```
Compose UI
   ↓
ViewModel / application state
   ↓
Repository
   ↓
Room DAOs
   ↓
Local database

Review answer
   ↓
Spaced-repetition logic
   ↓
Updated scheduling data
```

The repository maps between the app's domain models and Room entities, keeping database-specific concerns out of the UI layer.

## Project structure

```
code/
├── app/src/main/java/at/ac/fhstp/flashcardapp/
│   ├── data/      # Domain models and repository
│   ├── db/        # Room entities, DAOs, and database
│   ├── logic/     # Spaced-repetition logic
│   └── ui/        # Jetpack Compose screens and components
├── build.gradle.kts
└── settings.gradle.kts
```

## Running the project

1. Clone the repository.
2. Open the `code/` directory in Android Studio.
3. Let Gradle synchronize the project dependencies.
4. Run the app on an Android emulator or device.

The project currently targets Android SDK 36 and supports Android API 26+.

## Team

- **Tijana Mijatović** — development, implementation, user testing
- **Diana Simonicova** — UI/UX design

---

This repository is a university project and is being kept as part of my software-development portfolio.
