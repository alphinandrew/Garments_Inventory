# Garment Inventory List Extractor

An offline-first Android app for tracking garment stock by category and size — built with Kotlin and Jetpack Compose.

It's designed for a small clothing/textile shop that needs a fast, no-account, no-internet way to keep tabs on how many units of each size are in stock per garment category, see how stock has changed over time, and get a daily reminder to keep it up to date.

## Features

- **Category & size-based catalog** — organize garments by category (e.g. Girls Wear, Night Sets, Frocks/Feeding, Tops & Kurtis, Pants & Bottoms, Nighties), sub-style, item code/SKU, and notes, with support for standard alpha sizes, numeric kids sizes, isolated pant-waist sizes (34–40), and handwritten/custom size additions
- **Per-size stock tracking** — increment/decrement or manually type a stock count for each size, with running totals per garment and across the whole inventory
- **Search & filter** — filter by category and search across name, sub-style, size, notes, and item code
- **Stock history** — every add/edit/delete/stock change is logged; browse a day-by-day history view and view a stock trend chart per garment/size
- **PDF export** — export the full inventory, today's activity, or any single day's history as a PDF
- **Daily reminder notification** — configurable title and time-of-day reminder to prompt a stock update
- **Light / Dark / System theme** — full theme support, including theme-aware category and size tag colors
- **Undo delete** — quickly restore an accidentally deleted garment
- **Local & offline** — all data lives in an on-device Room (SQLite) database; no account, sign-in, or network connection required

## Tech Stack

- [Kotlin](https://kotlinlang.org/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose) + Material 3
- [Room](https://developer.android.com/training/data-storage/room) (SQLite) for persistence, with versioned migrations
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) for the scheduled daily reminder
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- Kotlin Coroutines & Flow, MVVM architecture

## Project Structure

```
app/src/main/java/com/example/
├── data/                     # Room entities, DAOs, database, repository
├── ui/
│   ├── components/           # Reusable Compose UI (dialogs, cards, sheets)
│   ├── theme/                # Color palette, typography, theme tokens
│   ├── GarmentInventoryScreen.kt
│   ├── GarmentInventoryViewModel.kt
│   └── HistoryScreen.kt
├── MainActivity.kt
└── StockReminderWorker.kt    # Daily notification worker
```

## Getting Started

**Prerequisites:** [Android Studio](https://developer.android.com/studio) (recent stable), JDK 11+, an emulator or device running Android 7.0 (API 24) or higher.

1. Clone the repository
   ```bash
   git clone <your-repo-url>
   ```
2. Open the project folder in Android Studio and let it sync Gradle
3. Run the app on an emulator or physical device

No API keys, backend, or account setup is required — the app works entirely offline.

## Screenshots

_Add screenshots of the inventory list, stock detail sheet, history view, and stock chart here._

## License

No license has been added yet. If you'd like to open-source this project, add a `LICENSE` file (e.g. MIT or Apache-2.0) to the repo root.