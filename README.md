# Calorie Streak

Private, offline-first Android calorie tracker built with Kotlin, Jetpack Compose, Material 3 and Room.

## Current functionality

- Persistent ingredients with calories per reference quantity
- Add and edit ingredients, including brand, category, favorite and archived state
- Personal recipes built from saved ingredients
- Add and edit recipes, servings, ingredient rows, quantities, compatible units and notes
- Transactional recipe ingredient replacement
- Automatic ingredient, recipe-total and per-serving calorie calculations
- Live recipe recalculation after ingredient corrections
- Historical meal snapshots remain unchanged after ingredient or recipe edits
- Recipe logging for one serving, half or a full recipe
- Manual calorie entries
- Persistent deletion of individual meal logs with confirmation
- Immediate Dashboard, History and Statistics updates after meal changes
- Chronological completed-day rebuilding after historical corrections
- Piecewise daily score curve rather than a simple target percentage
- Streaks, freeze earning and automatic freeze foundations
- Manual **Freeze Today** with separate actual and effective score handling
- Searchable Log Food, Recipes, Ingredients, recipe ingredient picker and Grocery quick-add
- Grocery-list generation with identical ingredient/unit merging
- History List and Graph modes
- History graph metrics: Score % and Calories
- History graph ranges: 7 days, 30 days, 365 days and all time
- Basic 7-day and 30-day statistics
- Dark Material 3 interface
- Circular Dashboard score display with streak and freeze information

## Score curve

- 800 kcal or less: 0%
- 1200 kcal: 40%
- 1400 kcal: 80%
- 1650 kcal: 100%
- 1800 kcal: 75%
- 2000 kcal: 20%
- 2200 kcal or more: 0%

Values between points use linear interpolation.

## Streak and freeze behavior

- A finalized score of at least 80% keeps the streak.
- A finalized actual score of at least 85% adds freeze progress.
- Seven qualifying days earn one freeze, up to the current cap of three.
- Existing numeric progress is preserved; for example, progress 4 becomes 4 / 7.
- Freeze Today consumes one available freeze and uses an effective score of 100% for streak presentation.
- Real calories and the real calorie-curve score remain stored and visible in History and graphs.
- A frozen day only qualifies toward another freeze when its actual score is at least 85%.

## Editing and snapshots

Ingredient and recipe definitions are live. Correcting an ingredient's calorie value immediately updates recipes that reference the same ingredient ID, and new meal logs use the corrected recipe total.

Meal logs are historical snapshots. They store their own recipe name, portion and calorie values, so editing or archiving a recipe or ingredient never changes old logs.

Ingredients used by recipes are archived instead of hard-deleted. Archived definitions remain available to existing recipes and can be shown explicitly in the editing screens.

## Architecture

- `data/local`: Room entities, DAOs, database and migrations
- `domain/calculation`: pure calorie, score, history, streak and freeze logic
- `domain/editing`: ingredient/recipe drafts, validation and compatible-unit conversion
- `domain/history`: history ranges and graph point generation
- `domain/search`: normalized case-insensitive search matching
- `ui`: shared ViewModel, state, screens and reusable components

Room remains at schema version 2. All editing fields already existed in the schema, so this update requires no destructive migration and preserves the existing database.

## Build and run

Requirements:

- Android Studio with Android SDK 35
- JDK 17
- Android 10 / API 29 or newer device

Open the repository root in Android Studio, allow Gradle sync to complete, select a connected phone or emulator and run the `app` configuration.

The repository currently does not include a Gradle wrapper. From a terminal with Gradle 8.10.2 installed, run tests with:

```text
gradle test
```

Build a debug APK with:

```text
gradle assembleDebug
```

The APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

To run on a physical phone, enable Developer options and USB debugging, connect the phone, select it in Android Studio and press Run.

## Current limitations

- Existing meal logs can be deleted but do not yet have a full edit form.
- Theme modes are defined in code, but the Settings UI does not yet expose Dark/Light/System selection.
- Notifications, accountability delivery and a Glance homescreen widget are not implemented on this branch.
- CI validates compilation and unit tests; final layout, touch targets and database-upgrade behavior should also be checked on the physical phone before merging.

No cloud backend is used. Room and the repository boundaries allow backup or synchronization to be added later.
