# Ingredient, recipe and freeze-rule audit

## Current storage

- `IngredientEntity` already stores id, name, brand, calories, reference amount/unit, category, favorite, archived, createdAt and updatedAt.
- `RecipeEntity` already stores id, name, description, servings, favorite, archived, createdAt and updatedAt.
- `RecipeItemEntity` already stores recipe/ingredient ids, ingredient-name snapshot, amount, unit and note.
- `MealLogEntity` stores its own recipe-name, portion and calorie values. Historical meals are true snapshots and are not recalculated from recipe or ingredient tables.
- Room schema version 2 already contains all fields required by this update. No schema version change or migration is required.

## Current behavior and gaps

- Ingredients and recipes can be created but not edited.
- Add forms currently omit several existing entity fields.
- Recipe ingredient replacement is not wrapped in one Room transaction.
- Active-only flows hide archived definitions; editing needs an optional archived view while normal selection/logging remains active-only.
- Recipe display stores an ingredient-name snapshot in the relation; UI calculation should resolve the live ingredient name and calories by ingredient id.
- Ingredient deletion currently checks visible recipes rather than all stored recipe relations.
- Meal logs already preserve historical calories and names correctly.
- Freeze earning currently defaults to five qualifying finalized days; the 80% streak and 85% qualification thresholds are separate and already correct.

## Planned changes

1. Add pure editing drafts, validation and unit-compatibility helpers.
2. Add ingredient Add/Edit dialog, archived filtering and persistent update behavior.
3. Add transactional recipe Add/Edit behavior with prefilled ingredient rows, notes, compatible units and unsaved-change confirmation.
4. Resolve live recipe names/calories from ingredient ids while keeping meal snapshots untouched.
5. Centralize the seven-day freeze requirement and update UI/tests/docs.
6. Run Android CI after each major change and verify no Room migration is introduced.
