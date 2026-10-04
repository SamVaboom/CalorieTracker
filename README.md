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
- History List and Graph modes for calories and weight
- History graph metrics: Score % and Calories
- History graph ranges: 7 days, 30 days, 365 days and all time
- Configurable calorie and weight goals
- Persistent weight tracking with list, graphs and statistics
- Persistent achievements, dynamic weight achievements and category filters
- Persistent queued achievement-unlock dialogs with retroactive summary handling
- Dark Material 3 application design system
- Circular animated Dashboard score display with streak and freeze information

## Score curve

- 800 kcal or less: 0%
- 1200 kcal: 40%
- 1400 kcal: 80%
- 1650 kcal: 100%
- 1800 kcal: 75%
- 2000 kcal: 20%
- 2200 kcal or more: 0%

Values between points use linear interpolation. A configured calorie target proportionally scales the calorie anchor points while finalized historical scores retain their stored target snapshot.

## Streak and freeze behavior

- A finalized score of at least 80% keeps the streak.
- A finalized actual score of at least 85% adds freeze progress.
- Seven qualifying days earn one freeze, up to the cap of three.
- Progress does not accumulate invisibly while freeze storage is full.
- Existing numeric progress is preserved across the earlier five-to-seven-day rule migration.
- Freeze Today consumes one available freeze and uses an effective score of 100% for streak presentation.
- Real calories and the real calorie-curve score remain stored and visible in History and graphs.
- A frozen day only qualifies toward another freeze when its actual score is at least 85%.

## Achievement unlock dialogs

New achievement unlocks are shown in a centered dark modal without navigating away from the current screen.

- Live unlocks appear immediately.
- Pending unlocks survive process death and app restarts.
- The X button and Android Back dismiss and persist the popup occurrence.
- Outside taps do not dismiss the dialog.
- Multiple unlocks are shown one at a time in earned order.
- Four or more achievements discovered during initial reconciliation are grouped into one summary dialog.
- Retroactive individual achievements remain new in the Achievements screen.
- Dynamic achievements can create another popup occurrence after being lost and legitimately re-earned.
- Existing pre-dialog achievements are migrated as already dismissed so an update does not replay the entire historical collection.

## Editing and snapshots

Ingredient and recipe definitions are live. Correcting an ingredient's calorie value immediately updates recipes that reference the same ingredient ID, and new meal logs use the corrected recipe total.

Meal logs are historical snapshots. They store their own recipe name, portion and calorie values, so editing or archiving a recipe or ingredient never changes old logs.

Ingredients used by recipes are archived instead of hard-deleted. Archived definitions remain available to existing recipes and can be shown explicitly in the editing screens.

## UI design system

The app-owned dark design system is located in `ui/theme` and `ui/components`.

- `Color.kt`: navy-charcoal surfaces, semantic state colors and category accents
- `Type.kt`: accessible number, title, body and label hierarchy
- `Shape.kt`: consistent rounded shape scale
- `Dimensions.kt`: shared spacing and touch-target values
- `Motion.kt`: short, standard and achievement animation durations
- `AppCard`, `AppChartContainer`, `AppEmptyState`, `AppSectionHeader` and `AppStatCard`: reusable screen building blocks

The default theme remains dark. The custom palette avoids pure-black monotony while keeping low-light use comfortable. Major screens include dark Compose previews, and instrumentation screenshot smoke guards detect accidental bright surfaces.

## Architecture

- `data/local`: Room entities, DAOs, database and migrations
- `data/settings`: persistent goal and rule settings
- `domain/achievement`: achievement registry, evaluation and popup queue policy
- `domain/calculation`: pure calorie, score, history, streak and freeze logic
- `domain/editing`: ingredient/recipe drafts, validation and compatible-unit conversion
- `domain/history`: history ranges and graph point generation
- `domain/search`: normalized case-insensitive search matching
- `ui`: shared ViewModels, state, screens, previews and reusable components

Room schema version 5 adds achievement popup occurrence state and a retroactive-summary table through a non-destructive 4→5 migration. Existing calorie, recipe, ingredient, meal, grocery, daily-log, weight, streak, freeze and achievement data is preserved.

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

Build the app and instrumentation-test APKs with:

```text
gradle assembleDebug assembleDebugAndroidTest
```

The debug app APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

To run on a physical phone, enable Developer options and USB debugging, connect the phone, select it in Android Studio and press Run.

## Current limitations

- Existing meal logs can be deleted but do not yet have a full edit form.
- Theme modes are defined in code, but the Settings UI does not yet expose Dark/Light/System selection.
- Notifications, accountability delivery and a Glance homescreen widget are not implemented.
- CI runs unit tests and compiles both debug APKs; instrumentation UI tests still need an emulator or physical phone to execute.
- Final database-upgrade and accessibility behavior should also be verified on the physical phone before merging.

No cloud backend is used. Room and repository boundaries allow backup or synchronization to be added later.
