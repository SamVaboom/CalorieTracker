# Calorie Streak

Private Android calorie tracker built with Kotlin, Jetpack Compose, Material 3 and Room.

## Current prototype

The app supports:

- Persistent ingredients with calories per quantity
- Personal recipes built from saved ingredients
- Automatic recipe calorie totals and calories per serving
- Logging one serving, half, or a full recipe
- Manual calorie entries
- Dashboard totals and the defined score curve
- Meal history with deletion
- Grocery-list generation from recipes with matching-item merging
- Basic 7-day and 30-day statistics
- Chronological completed-day rebuilding for streak and freeze calculations

## Score curve

- 800 kcal or less: 0%
- 1200 kcal: 40%
- 1400 kcal: 80%
- 1650 kcal: 100%
- 1800 kcal: 75%
- 2000 kcal: 20%
- 2200 kcal or more: 0%

Values between points use linear interpolation.

## Build

Open the repository in Android Studio with JDK 17, allow Gradle sync, then run the `app` configuration on an Android 10 or newer device.

Run tests with:

`./gradlew test`

Build a debug APK with:

`./gradlew assembleDebug`

The APK is generated at:

`app/build/outputs/apk/debug/app-debug.apk`

## Prototype limitations

This is an end-to-end local prototype. Editing existing ingredients and recipes, manual cheat-day controls, notifications, accountability delivery, configurable settings, advanced charts, and the Glance homescreen widget still need UI completion and device testing.

No cloud backend is used. Room and the repository boundaries allow backup or synchronization to be added later.
