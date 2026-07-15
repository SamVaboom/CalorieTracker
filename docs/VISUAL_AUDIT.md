# Calorie Streak visual audit

Baseline: `codex/achievement-and-ui-fixes`, with functional tests and debug build passing before the redesign work.

## Findings

- The existing theme was dark, but color, shape, typography, spacing, and motion were only partially centralized.
- Most screens used raw Material cards and buttons directly, producing inconsistent surface depth and corner treatment.
- Dashboard hierarchy was functional but visually flat: the score ring, freeze count, metrics, actions, and meal list competed without a strong hero container.
- Navigation used the correct four destinations but lacked app-owned tonal styling and semantic selected-state colors.
- Achievements had earned/locked states, but category accents and popup feedback were missing.
- Settings used always-visible text fields rather than grouped goal controls.
- Statistics, More, Weight, History, and Grocery used repeated card/list patterns that should share reusable components.
- Dialogs inherited Material defaults and needed a consistent dark overlay treatment.
- Several screens still contain local spacing literals; these should progressively move to the app spacing scale.

## Central design tokens

- Background: `#0B0E14`
- Surface: `#121722`
- Elevated surface: `#181E2B`
- Strong elevated surface: `#202738`
- Border: `#2A3345`
- Primary violet: `#A997FF`
- Secondary cyan: `#55D6E8`
- Coral: `#FF8A78`
- Success: `#65D98B`
- Warning: `#F3B85F`
- Error: `#FF6F83`
- Freeze: `#78CCFF`
- Weight: `#5FD4B4`
- Achievement: `#FFD166`

Spacing uses 4, 8, 12, 16, 20, 24, and 32 dp. Shapes use 10, 12, 16, 20, and 28 dp radii. Motion uses 180 ms, 260 ms, and 380 ms durations.

## Files created or changed

### Theme and reusable components

- `ui/theme/Color.kt`
- `ui/theme/Dimensions.kt`
- `ui/theme/Motion.kt`
- `ui/theme/Shape.kt`
- `ui/theme/Type.kt`
- `ui/theme/Theme.kt`
- `ui/components/AppCard.kt`
- `ui/components/AppSectionHeader.kt`

### Application shell and achievement feedback

- `ui/navigation/CalorieStreakNavHost.kt`
- `ui/achievements/AchievementPopupHost.kt`
- `ui/achievements/AchievementsScreen.kt`

### Persistence and state

- `data/local/entity/EarnedAchievementEntity.kt`
- `data/local/entity/AchievementPopupSummaryEntity.kt`
- `data/local/dao/FeatureDao.kt`
- `data/local/database/CalorieStreakDatabase.kt`
- `data/local/database/DatabaseProvider.kt`
- `ui/FeatureViewModel.kt`
- `domain/achievement/AchievementPopupPolicy.kt`

A Room migration is required only for the requested persistent popup-dismissal state. The visual redesign itself does not alter existing stored calorie, weight, recipe, grocery, streak, freeze, or achievement values.
