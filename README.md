# Calorie Streak

Private, offline-first Android calorie tracker built with Kotlin, Jetpack Compose, Material 3 and Room.

## Current functionality

- Persistent ingredients with calories and optional protein per shared reference quantity
- Ingredient protein states distinguish unknown (`null`) from explicitly assigned zero (`0.0`)
- Search and filters for all ingredients, protein missing and protein assigned
- Personal recipes built from saved ingredients
- Expandable recipe cards with ingredient quantities, calories, protein and missing-data warnings
- Add and edit recipes, servings, ingredient rows, compatible units and notes
- Automatic ingredient, recipe-total and per-serving calorie and protein calculations
- Historical meal calorie and protein snapshots remain unchanged after ingredient or recipe edits
- Explicit historical protein correction without changing stored calories
- Recipe logging for one serving, half or a full recipe
- Manual calorie entries with optional protein
- Persistent deletion of individual meal logs with confirmation
- Immediate Dashboard, History, Statistics and achievement reconciliation after meal changes
- Piecewise daily score curve, streaks and freezes unchanged by protein tracking
- Searchable Log Food, Recipes, Ingredients, recipe ingredient picker and Grocery quick-add
- Grocery-list generation with identical ingredient/unit merging
- Collapsible History filters with compact active-filter summary
- History List and Graph modes for calories, weight and known protein
- History graph ranges: 7 days, 30 days, 365 days and all time
- Protein History does not convert unknown or missing dates to zero
- Informational Protein Statistics for today, rolling ranges, lifetime totals, highs and ingredient coverage
- Configurable calorie and weight goals; there is deliberately no protein goal
- Persistent weight tracking with list, graphs and statistics
- Persistent achievements, including all informational protein achievements and dynamic No Mystery Macros
- Persistent queued achievement-unlock dialogs with retroactive summary handling
- Dark Material 3 application design system
- Animated Dashboard score ring beginning at 12 o'clock and filling clockwise

## Protein behavior

Protein is informational only and never affects:

- Daily calorie score or target
- Streak continuation
- Freeze progress, earning or use
- Cheat days
- Daily success or failure
- Accountability events

Protein values use these semantics:

- `null`: protein is unknown or not assigned
- `0.0`: protein is explicitly assigned as zero grams
- Positive value: known protein grams

Known protein can still be displayed or qualify for an achievement when another meal on the same day is unknown. Unknown amounts are never assumed and are never silently reconstructed from current recipes.

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
- Protein achievements use the same reconciliation and popup system.
- `No Mystery Macros` is dynamic; other protein achievements remain earned once legitimately achieved.

## Editing and snapshots

Ingredient and recipe definitions are live. Correcting an ingredient's nutrition immediately updates current recipe calculations and future logs.

Meal logs are historical snapshots. They store their own recipe name, portion, calorie and protein values, so later ingredient or recipe edits do not alter old logs. Existing pre-protein meals remain protein-unknown after migration. A user may explicitly recalculate a saved-recipe meal from the current recipe or enter protein for a manual meal; this changes only the historical protein snapshot, not calories.

The legacy ingredient Brand database column remains for non-destructive compatibility but is no longer shown or edited.

## UI design system

The app-owned dark design system is located in `ui/theme` and `ui/components`.

- `Color.kt`: navy-charcoal surfaces, semantic state colors and category accents
- `Type.kt`: accessible number, title, body and label hierarchy
- `Shape.kt`: consistent rounded shape scale
- `Dimensions.kt`: shared spacing and touch-target values
- `Motion.kt`: short, standard and achievement animation durations
- `AppCard`, `AppChartContainer`, `AppEmptyState`, `AppSectionHeader` and `AppStatCard`: reusable screen building blocks

## Architecture

- `data/local`: Room entities, DAOs, database and non-destructive migrations
- `data/settings`: persistent goal and rule settings
- `domain/achievement`: achievement registry, protein evaluation and popup queue policy
- `domain/calculation`: existing calorie, score, history, streak and freeze logic
- `domain/editing`: ingredient/recipe drafts, validation and compatible-unit conversion
- `domain/history`: calorie/weight history ranges and graph points
- `domain/protein`: pure ingredient, recipe, daily, history, statistics and formatting logic
- `domain/search`: normalized case-insensitive search matching
- `ui`: shared ViewModels, state, screens, previews and reusable components

Room schema version 6 adds nullable ingredient protein and historical meal-protein snapshots through a non-destructive 5→6 migration. Schema versions 1 through 6 are committed under `app/schemas` for migration verification. Existing calories, ingredients, recipes, recipe rows, meals, grocery data, daily logs, settings, weights, achievements, streaks, freezes and popup state are preserved.

The complete implementation requirements are committed at `docs/PROTEIN_TRACKING_IMPLEMENTATION_SPEC.txt`; the repository audit and migration rationale are in `docs/PROTEIN_IMPLEMENTATION_AUDIT.md`.

## Build and run

Requirements:

- Android Studio with Android SDK 35
- JDK 17
- Android 10 / API 29 or newer device

The repository includes the Gradle 8.10.2 wrapper.

Linux or macOS:

```text
./gradlew clean
./gradlew testDebugUnitTest kspDebugKotlin kspReleaseKotlin assembleDebug assembleDebugAndroidTest --stacktrace
```

Windows PowerShell:

```text
.\gradlew.bat clean
.\gradlew.bat testDebugUnitTest kspDebugKotlin kspReleaseKotlin assembleDebug assembleDebugAndroidTest --stacktrace
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

GitHub Actions uses the project wrapper, validates every committed Room schema JSON before Gradle runs, preserves the complete Gradle log and fails on the real Gradle exit status.

## Current limitations

- Theme modes are defined in code, but Settings does not yet expose Dark/Light/System selection.
- Notifications, accountability delivery and a Glance homescreen widget are not implemented.
- CI runs unit tests and compiles the app and instrumentation-test APKs. Device instrumentation execution and the real 5→6 upgrade should still be checked on an emulator and the physical phone before merging.

No cloud backend is used. Room and repository boundaries allow backup or synchronization to be added later.
