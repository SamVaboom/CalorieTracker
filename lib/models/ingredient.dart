class Ingredient {
  const Ingredient({required this.id, required this.name, required this.calories, required this.referenceAmount, required this.unit});
  final String id;
  final String name;
  final double calories;
  final double referenceAmount;
  final String unit;
  double caloriesFor(double amount) => referenceAmount <= 0 ? 0 : calories * amount / referenceAmount;
}
