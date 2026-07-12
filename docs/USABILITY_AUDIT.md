# Calorie Streak usability audit

Baseline: `main` at merge commit `2b512883f409213d21b797dc9c23f56c7bf642de`.

## Existing architecture

- Native Android app using Kotlin, Jetpack Compose, Material 3, Navigation Compose, Room, ViewModel, StateFlow, Coroutines, DataStore and WorkManager dependencies.
- One activity (`MainActivity`) hosting `CalorieStreakNavHost`.
- One shared `AppViewModel` combines Room flows into `AppUiState`.
- Room database version 2 stores ingredients, recipes, recipe ingredients, meal logs, grocery items and daily logs.
- Pure Kotlin calculators exist for ingredient calories, recipe calories, score interpolation, daily finalization and streak/freeze state.

## Existing screens

- Bottom navigation: Dashboard, Log Food, Recipes and Grocery.
- Additional routes: Ingredients, History and Statistics.
- Dashboard shows calories, score, streaks, freezes and today's meals.
- Log Food supports recipe quick logging and manual calories.
- Recipes supports creation from stored ingredients.
- Ingredients supports adding and deleting ingredients.
- Grocery supports adding recipe ingredients, checking items and clearing the list.
- History lists meals grouped by date and already exposes a basic Delete button.
- Statistics shows 7-day and 30-day averages.

## Existing working behavior to preserve

- Meal calories are stored as snapshots.
- Room flows refresh Dashboard, History and Statistics after database changes.
- `AppViewModel.deleteMeal` removes one selected meal and requests a history rebuild.
- Grocery generation merges identical ingredient/unit combinations.
- The score calculator uses the configured piecewise interpolation curve.
- Completed-day history feeds derived streak and freeze state.

## Defects and risks

1. Dashboard meal rows have no delete action.
2. History deletion has no confirmation or undo.
3. `HistoryRebuilder` returns when no meals remain, leaving stale daily logs.
4. Rebuilding from the new earliest meal can leave stale daily logs before that date.
5. Manual freeze state would be lost by the current delete-and-recreate history rebuild.
6. `StreakCalculator` does not distinguish finalized and in-progress daily records.
7. Lists do not have search and several add actions occupy the top of the screen.
8. History has no graph mode or range/metric filtering.
9. The app uses the default Material theme and has no centralized semantic dark palette.
10. The Dashboard lacks the requested visual hierarchy and manual Freeze Today flow.
11. Ingredient deletion does not currently protect recipe references.
12. Existing tests cover core score/calorie/streak calculations but not search, history ranges, deletion rebuilds, manual freezing or navigation.

## Implementation order on this branch

1. Meal deletion safety and chronological daily-history rebuilding.
2. Shared search behavior and searchable Log Food, Recipes, Ingredients, recipe ingredient picker and Grocery.
3. History List/Graph modes with metric and rolling-range selectors.
4. Recipes/Ingredients navigation and floating add actions.
5. Manual Freeze Today persistence and actual/effective score distinction.
6. Dashboard redesign.
7. Centralized dark Material 3 theme.
8. Tests, README and CI verification.

No destructive Room migration will be introduced. Existing records remain the source of truth.