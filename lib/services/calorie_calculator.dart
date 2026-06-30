import '../models/ingredient.dart';
import '../models/recipe.dart';

class CalorieCalculator {
  const CalorieCalculator();

  double recipeTotal(Recipe recipe, List<Ingredient> ingredients) {
    var total = 0.0;
    for (final item in recipe.ingredients) {
      final ingredient = ingredients.where((i) => i.id == item.ingredientId).firstOrNull;
      if (ingredient != null) total += ingredient.caloriesFor(item.amount);
    }
    return total;
  }

  double perPortion(Recipe recipe, List<Ingredient> ingredients) => recipe.defaultPortions <= 0 ? recipeTotal(recipe, ingredients) : recipeTotal(recipe, ingredients) / recipe.defaultPortions;

  double forFraction(Recipe recipe, List<Ingredient> ingredients, double fraction) => recipeTotal(recipe, ingredients) * fraction;
}

extension FirstOrNull<T> on Iterable<T> {
  T? get firstOrNull => isEmpty ? null : first;
}
