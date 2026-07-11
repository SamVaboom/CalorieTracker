# Calorie Streak

Private, offline-first Android calorie tracker.

## Technology

- Kotlin
- Jetpack Compose and Material 3
- Room
- ViewModel, StateFlow, and Coroutines
- Navigation Compose
- DataStore
- WorkManager
- Minimum Android version: Android 10 (API 29)

Package: `com.sam.caloriestreak`

## Open and run

1. Install Android Studio with JDK 17.
2. Clone the repository and check out `codex/native-android-foundation`.
3. Open the repository root in Android Studio.
4. Allow Gradle sync to finish.
5. Select an Android emulator or connected phone.
6. Run the `app` configuration.

## Tests

Run the unit tests from Android Studio, or use:

`./gradlew test`

## Debug APK

Build from Android Studio with **Build > Build APK(s)**, or run:

`./gradlew assembleDebug`

The generated APK is located at:

`app/build/outputs/apk/debug/app-debug.apk`

Install it with:

`adb install -r app/build/outputs/apk/debug/app-debug.apk`

## Current foundation

This first native Android phase includes the Gradle project, Compose navigation, Material 3 UI placeholders, Room foundation, repository contract, ingredient model, Kotlin score calculator, and score unit tests.

The remaining features should be implemented in separate, reviewable phases: ingredient CRUD, recipes, meal logging, dashboard state, streaks/freezes, grocery lists, statistics, notifications, and the Glance widget.
