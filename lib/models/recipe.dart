class RecipeIngredient {
  const RecipeIngredient({required this.ingredientId, required this.amount, required this.unit});
  final String ingredientId;
  final double amount;
  final String unit;
}

class Recipe {
  const Recipe({required this.id, required this.name, required this.ingredients, required this.defaultPortions, this.favorite = false});
  final String id;
  final String name;
  final List<RecipeIngredient> ingredients;
  final double defaultPortions;
  final bool favorite;

  Recipe copyWith({String? name, List<RecipeIngredient>? ingredients, double? defaultPortions, bool? favorite}) => Recipe(
        id: id,
        name: name ?? this.name,
        ingredients: ingredients ?? this.ingredients,
        defaultPortions: defaultPortions ?? this.defaultPortions,
        favorite: favorite ?? this.favorite,
      );
}
