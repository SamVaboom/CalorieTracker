# Protein tracking and Room CI audit

## Phase 0 findings

- The project was already a native Kotlin / Jetpack Compose / Room Android application.
- Root build versions at the start of this work were AGP 8.7.3, Kotlin 2.0.21 and KSP 2.0.21-1.0.28.
- Room runtime and compiler were 2.6.1.
- `exportSchema = true` was enabled, but `app/schemas` was not committed on `main`.
- Schema output used a manual KSP argument rather than the official Room Gradle plugin.
- The repository did not contain its Gradle wrapper.
- GitHub Actions installed an independent Gradle executable and filtered failure output.

## Phase 0 repair

- Applied `androidx.room` 2.6.1 in the root and app Gradle scripts.
- Replaced the manual KSP schema argument with `room { schemaDirectory(...) }`.
- Added the Gradle 8.10.2 wrapper and changed CI to use `./gradlew`.
- Generated Room schema versions 1 through 5 from their actual historical source revisions.
- Generated schema 6 from the protein migration.
- Added JSON, zero-byte, filename/version and contiguous-history validation before Gradle runs.
- Added `MigrationTestHelper` coverage using the committed schema assets.
- CI runs debug unit tests, debug KSP, release KSP and compiles both debug APK variants while retaining the full build log.

## Existing architecture

### Score ring

`ScoreRing` uses a custom Compose `Canvas`, `drawArc`, a sweep gradient and `animateFloatAsState`. The old visible start angle was at 12 o'clock, but the sweep-gradient coordinate system still began at 3 o'clock. The corrected implementation rotates only the drawing coordinate system by -90 degrees and draws both track and progress from zero within that rotated scope.

### History

History was a column containing a permanently expanded filter card followed by a nested list or graph. The replacement uses a compact summary card, `AnimatedVisibility` and `animateContentSize`. Expanded controls remain in normal document layout above content and cannot overlay the graph.

### Recipes

Recipe entities and recipe-item rows were already observed together and converted into `RecipeSummary` values in `AppViewModel`. Protein and ingredient-calorie details are calculated once in that same state transformation. Expandable recipe cards therefore do not execute Room queries during recomposition.

### Meal snapshots

Meal logs already stored calorie snapshots independently of current ingredient and recipe definitions. Protein uses the same model: a nullable known-grams snapshot plus completeness metadata. Editing ingredients or recipes does not mutate old meal rows.

### Achievements

The achievement engine maintains a stable registry, evaluates current database evidence, stores earned records in Room and drives a persistent one-at-a-time popup queue. Protein definitions use the same registry, reconciliation and popup infrastructure. Permanent achievements remain earned; `No Mystery Macros` is dynamic and can relock/re-earn.

## Protein data semantics

- `null`: protein is unknown or not assigned.
- `0.0`: protein was explicitly assigned as zero grams.
- Positive value: known protein grams.

Unknown values are never silently converted to zero.

## Room migration 5 to 6

The non-destructive migration adds:

- `ingredients.proteinPerReferenceAmount REAL NULL`
- `meal_logs.proteinGramsSnapshot REAL NULL`
- `meal_logs.proteinDataComplete INTEGER NOT NULL DEFAULT 0`
- `meal_logs.missingProteinItemCount INTEGER NOT NULL DEFAULT 0`

The legacy ingredient Brand column remains in the database to avoid an unnecessary table rebuild, but it is no longer displayed or edited. Existing calories, recipes, recipe rows, meals, grocery data, daily logs, weights, achievements, streak/freeze state and popup state remain unchanged.

## Separation from calorie behavior

Protein calculators and achievement evaluation live in separate pure Kotlin classes. Protein data is not passed into score, streak, freeze, cheat-day or accountability calculations and is not shown as a Dashboard goal or success state.
