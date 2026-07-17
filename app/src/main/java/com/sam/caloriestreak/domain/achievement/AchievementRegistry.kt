package com.sam.caloriestreak.domain.achievement

object AchievementRegistry {
    val time = listOf(
        ConsecutiveRecordingAchievementDefinition("time_001", "Liftoff", 1, "One down, infinity to go.", sortOrder = 1),
        ConsecutiveRecordingAchievementDefinition("time_003", "All men are created equal", 3, "The Gettysburg battle is over", sortOrder = 3),
        ConsecutiveRecordingAchievementDefinition("time_005", "Iceberg ahead", 5, "The Titanic has hit an iceberg", sortOrder = 5),
        ConsecutiveRecordingAchievementDefinition("time_008", "Houston, We Have a Problem.", 8, "Apollo 11 arrived back on earth", sortOrder = 8),
        ConsecutiveRecordingAchievementDefinition("time_012", "Curtain closed", 12, "The Cannes film festival has ended", sortOrder = 12),
        ConsecutiveRecordingAchievementDefinition("time_014", "Fortnight", 14, "Two weeks of tracking", sortOrder = 14),
        ConsecutiveRecordingAchievementDefinition("time_016", "Hannibal ad portas", 16, "Hannibal has crossed the alps", sortOrder = 16),
        ConsecutiveRecordingAchievementDefinition("time_021", "La ligne d'arrivée", 21, "The Tour de France has ended", sortOrder = 21),
        ConsecutiveRecordingAchievementDefinition("time_026", "The way of money", 26, "Avatar: The Way of Water has earned 2 billion dollars at the box office", sortOrder = 26),
        ConsecutiveRecordingAchievementDefinition("time_030", "Campeones del mundo", 30, "The FIFA World Cup 1978 has ended", sortOrder = 30),
        ConsecutiveRecordingAchievementDefinition("time_036", "A new hope", 36, "Filming of Star Wars: A New Hope has wrapped", sortOrder = 36),
        ConsecutiveRecordingAchievementDefinition("time_045", "Liz Truss", 45, "Liz Truss has left her job as PM of GB", sortOrder = 45),
        ConsecutiveRecordingAchievementDefinition("time_046", "Blitzkrieg", 46, "The Nazis have taken over France", sortOrder = 46),
        ConsecutiveRecordingAchievementDefinition("time_052", "Objection, relevance", 52, "The Johnny Depp v Amber Heard trial is over", sortOrder = 52),
        ConsecutiveRecordingAchievementDefinition("time_056", "Pulp Fiction", 56, "All scenes of Pulp Fiction have been filmed", sortOrder = 56),
        ConsecutiveRecordingAchievementDefinition("time_065", "Cat Mama", 65, "Cat babies have been born", sortOrder = 65),
        ConsecutiveRecordingAchievementDefinition("time_069", "Nice", 69, "Nice", sortOrder = 69),
        ConsecutiveRecordingAchievementDefinition("time_078", "Blue sky", 78, "NATO has stopped air-bombing Yugoslavia", sortOrder = 78),
        ConsecutiveRecordingAchievementDefinition("time_080", "Around the World", 80, "Jules Verne’s balloon has landed back in London", sortOrder = 80),
        ConsecutiveRecordingAchievementDefinition("time_082", "Leaving Elba", 82, "Napoleon has been defeated at Waterloo", sortOrder = 82),
        ConsecutiveRecordingAchievementDefinition("time_088", "Bullet Time", 88, "The first Matrix movie has wrapped filming", sortOrder = 88),
        ConsecutiveRecordingAchievementDefinition("time_096", "Protocol of Peace", 96, "Spain and the USA have signed a cease-fire", sortOrder = 96),
        ConsecutiveRecordingAchievementDefinition("time_140", "The Great Exhibition", 140, "The first world expo has ended", sortOrder = 140),
        ConsecutiveRecordingAchievementDefinition("time_259", "Beat it", 259, "Michael Jackson’s Thriller is no longer top of the charts", sortOrder = 259),
        ConsecutiveRecordingAchievementDefinition("time_274", "Motherhood", 274, "A healthy baby has begun its life", sortOrder = 274),
        ConsecutiveRecordingAchievementDefinition("time_300", "True Spartan", 300, "THIS IS SPARTA!", sortOrder = 300),
        ConsecutiveRecordingAchievementDefinition("time_365", "Full Orbit", 365, "Earth has finished one lap around the sun", sortOrder = 365),
        ConsecutiveRecordingAchievementDefinition("time_420", "Blaze it", 420, "420", sortOrder = 420)
    )

    private val weightObjects = listOf(
        1 to "First Step", 2 to "Large Watermelon", 3 to "Newborn baby", 4 to "A cat",
        5 to "Five Down", 6 to "Watermelon", 7 to "Bowling ball", 8 to "Gallon of Paint",
        9 to "Carry-on luggage", 10 to "Double Digits", 11 to "Car battery", 12 to "Mountain Bike",
        13 to "Microwave", 14 to "Large Paint Container", 15 to "Cement Bag", 16 to "Kettlebell",
        17 to "Checked Suitcase", 18 to "Car Wheel", 19 to "Commercial Vacuum"
    )

    val weight = buildList {
        add(AchievementDefinition("weight_first_entry", "First Weigh-In", "Every graph starts with one dot.", AchievementCategory.WEIGHT, threshold = 1.0, sortOrder = 1000))
        add(AchievementDefinition("weight_7_dates", "Data Point", "Record weight on 7 different dates.", AchievementCategory.WEIGHT, threshold = 7.0, sortOrder = 1001))
        add(AchievementDefinition("weight_30_dates", "Trend Watcher", "Record weight on 30 different dates.", AchievementCategory.WEIGHT, threshold = 30.0, sortOrder = 1002))
        weightObjects.forEach { (kilograms, title) ->
            add(AchievementDefinition("weight_loss_${kilograms.toString().padStart(2, '0')}", title, "Your latest weight is at least $kilograms kg below your first recorded weight.", AchievementCategory.WEIGHT, threshold = kilograms.toDouble(), sortOrder = 1100 + kilograms))
        }
        add(AchievementDefinition("weight_halfway", "Halfway There", "Reach 50% of your configured weight-loss goal.", AchievementCategory.WEIGHT, threshold = 50.0, sortOrder = 1201))
        add(AchievementDefinition("weight_goal", "Goal Achieved", "Reach your configured target weight.", AchievementCategory.WEIGHT, sortOrder = 1202))
        add(AchievementDefinition("weight_phoenix", "Phoenix", "Your latest entry is a new lowest recorded weight.", AchievementCategory.WEIGHT, sortOrder = 1203))
    }

    val score = listOf(
        AchievementDefinition("bullseye", "Bullseye", "Reach exactly 100% on a finalized day.", AchievementCategory.SCORE, sortOrder = 3001),
        AchievementDefinition("three_in_a_row", "Three in a Row", "Reach 100% three days in a row.", AchievementCategory.SCORE, threshold = 3.0, sortOrder = 3002),
        AchievementDefinition("perfectionist", "Perfectionist", "Reach 100% seven days in a row.", AchievementCategory.SCORE, threshold = 7.0, sortOrder = 3003),
        AchievementDefinition("master_of_balance", "Master of Balance", "Stay between 95% and 100% for 14 consecutive days.", AchievementCategory.SCORE, threshold = 14.0, sortOrder = 3004)
    )

    val calories = listOf(
        AchievementDefinition("fasting_monk", "Fasting Monk", "Eat less than 1000 kcal in one finalized day.", AchievementCategory.CALORIES, sortOrder = 3101),
        AchievementDefinition("enlightened_monk", "Enlightened Monk", "Eat less than 1000 kcal three times within 7 days.", AchievementCategory.CALORIES, sortOrder = 3102),
        AchievementDefinition("air_diet", "Air Diet", "Eat less than 800 kcal in one finalized day.", AchievementCategory.HIDDEN, hidden = true, sortOrder = 3103),
        AchievementDefinition("ghost", "Ghost", "End a finalized day with 0 logged calories.", AchievementCategory.HIDDEN, hidden = true, sortOrder = 3104),
        AchievementDefinition("the_american", "The American", "Eat more than 3000 kcal in one finalized day.", AchievementCategory.CALORIES, sortOrder = 3110),
        AchievementDefinition("thanksgiving", "Thanksgiving", "Eat more than 4000 kcal in one finalized day.", AchievementCategory.CALORIES, sortOrder = 3111),
        AchievementDefinition("bottomless_pit", "Bottomless Pit", "Eat more than 5000 kcal in one finalized day.", AchievementCategory.HIDDEN, hidden = true, sortOrder = 3112),
        AchievementDefinition("cheat_code", "Cheat Code", "Eat more than 2500 kcal on a manually frozen day.", AchievementCategory.CALORIES, sortOrder = 3113),
        AchievementDefinition("testing_limits", "Testing limits", "Finish above the calorie target while keeping a score of at least 80%.", AchievementCategory.CALORIES, sortOrder = 3114),
        AchievementDefinition("loophole", "Loophole", "Finish above the target while staying at 80% or higher for three consecutive days.", AchievementCategory.CALORIES, threshold = 3.0, sortOrder = 3115),
        AchievementDefinition("no_regrets", "No Regrets", "Finish above 2200 kcal without using a freeze.", AchievementCategory.HIDDEN, hidden = true, sortOrder = 3116),
        AchievementDefinition("lucky_seven", "Lucky Seven", "Finish a day with exactly 1666 kcal.", AchievementCategory.HIDDEN, hidden = true, sortOrder = 3117)
    )

    val mealsAndRecipes = listOf(
        AchievementDefinition("picky_eater", "Picky Eater", "Eat the same saved recipe 15 times within 30 days.", AchievementCategory.MEAL_HABITS, threshold = 15.0, sortOrder = 4001),
        AchievementDefinition("creature_of_habit", "Creature of Habit", "Eat the same breakfast recipe 20 times.", AchievementCategory.MEAL_HABITS, threshold = 20.0, sortOrder = 4002),
        AchievementDefinition("meal_prep", "Meal Prep", "Eat the same recipe three consecutive days.", AchievementCategory.MEAL_HABITS, threshold = 3.0, sortOrder = 4003),
        AchievementDefinition("explorer", "Explorer", "Log 25 different recipes.", AchievementCategory.RECIPES, threshold = 25.0, sortOrder = 4010),
        AchievementDefinition("chef", "Chef", "Create 25 recipes.", AchievementCategory.RECIPES, threshold = 25.0, sortOrder = 4011),
        AchievementDefinition("master_chef", "Master Chef", "Create 100 recipes.", AchievementCategory.RECIPES, threshold = 100.0, sortOrder = 4012),
        AchievementDefinition("ingredient_collector", "Ingredient Collector", "Add 100 ingredients.", AchievementCategory.RECIPES, threshold = 100.0, sortOrder = 4013),
        AchievementDefinition("home_cook", "Home Cook", "Log 100 recipe-based meals.", AchievementCategory.RECIPES, threshold = 100.0, sortOrder = 4014),
        AchievementDefinition("freestyler", "Freestyler", "Log 25 manual calorie entries.", AchievementCategory.MEAL_HABITS, threshold = 25.0, sortOrder = 4015)
    )

    val protein = listOf(
        AchievementDefinition("protein_initiate", "Protein Initiate", "Every journey starts with a first serving.", AchievementCategory.PROTEIN, threshold = 50.0, sortOrder = 8001),
        AchievementDefinition("protein_solid_foundation", "Solid Foundation", "Record at least 75 g of known protein in one day.", AchievementCategory.PROTEIN, threshold = 75.0, sortOrder = 8002),
        AchievementDefinition("protein_triple_digits", "Triple Digits", "Welcome to triple digits.", AchievementCategory.PROTEIN, threshold = 100.0, sortOrder = 8003),
        AchievementDefinition("protein_heavy_lifter", "Heavy Lifter", "Record at least 125 g of known protein in one day.", AchievementCategory.PROTEIN, threshold = 125.0, sortOrder = 8004),
        AchievementDefinition("protein_150_club", "The 150 Club", "Record at least 150 g of known protein in one day.", AchievementCategory.PROTEIN, threshold = 150.0, sortOrder = 8005),
        AchievementDefinition("protein_absolute_unit", "Absolute Unit", "Record at least 200 g of known protein in one day.", AchievementCategory.PROTEIN, hidden = true, threshold = 200.0, sortOrder = 8006),

        AchievementDefinition("protein_snack", "Protein Snack", "Log one meal containing at least 20 g of protein.", AchievementCategory.PROTEIN, threshold = 20.0, sortOrder = 8010),
        AchievementDefinition("protein_power_meal", "Power Meal", "Log one meal containing at least 30 g of protein.", AchievementCategory.PROTEIN, threshold = 30.0, sortOrder = 8011),
        AchievementDefinition("protein_main_course", "Main Course", "Log one meal containing at least 50 g of protein.", AchievementCategory.PROTEIN, threshold = 50.0, sortOrder = 8012),
        AchievementDefinition("protein_bomb", "Protein Bomb", "Log one meal containing at least 75 g of protein.", AchievementCategory.PROTEIN, threshold = 75.0, sortOrder = 8013),

        AchievementDefinition("protein_kilo_club", "Kilo Club", "One kilogram of protein logged.", AchievementCategory.PROTEIN, threshold = 1_000.0, sortOrder = 8020),
        AchievementDefinition("protein_five_kilo_sack", "Five-Kilo Sack", "Record at least 5,000 g of known protein.", AchievementCategory.PROTEIN, threshold = 5_000.0, sortOrder = 8021),
        AchievementDefinition("protein_ten_kilo_plate", "Ten-Kilo Plate", "Record at least 10,000 g of known protein.", AchievementCategory.PROTEIN, threshold = 10_000.0, sortOrder = 8022),
        AchievementDefinition("protein_warehouse", "Protein Warehouse", "Record at least 25,000 g of known protein.", AchievementCategory.PROTEIN, threshold = 25_000.0, sortOrder = 8023),
        AchievementDefinition("protein_industrial_quantities", "Industrial Quantities", "Record at least 50,000 g of known protein.", AchievementCategory.PROTEIN, threshold = 50_000.0, sortOrder = 8024),
        AchievementDefinition("protein_empire", "Protein Empire", "Record at least 100,000 g of known protein.", AchievementCategory.PROTEIN, threshold = 100_000.0, sortOrder = 8025),

        AchievementDefinition("protein_pound_for_pound", "Pound for Pound", "Record at least 1 g of protein per kilogram of body weight in one day.", AchievementCategory.PROTEIN, sortOrder = 8030),
        AchievementDefinition("protein_double_density", "Double Density", "Record at least 2 g of protein per kilogram of body weight in one day.", AchievementCategory.PROTEIN, sortOrder = 8031),
        AchievementDefinition("protein_half_of_yourself", "Half of Yourself", "Log cumulative known protein equal to half your first valid recorded body weight.", AchievementCategory.PROTEIN, sortOrder = 8032),
        AchievementDefinition("protein_eat_yourself", "Eat Yourself", "Log cumulative known protein equal to your first valid recorded body weight.", AchievementCategory.PROTEIN, sortOrder = 8033),

        AchievementDefinition("protein_three_days", "Three Protein Days", "Record at least 100 g on three different calendar days.", AchievementCategory.PROTEIN, threshold = 3.0, sortOrder = 8040),
        AchievementDefinition("protein_week", "Protein Week", "Record at least 100 g on seven different calendar days.", AchievementCategory.PROTEIN, threshold = 7.0, sortOrder = 8041),
        AchievementDefinition("protein_routine", "Protein Routine", "Record at least 100 g on 20 days within a rolling 30-day period.", AchievementCategory.PROTEIN, threshold = 20.0, sortOrder = 8042),
        AchievementDefinition("protein_triple_digit_streak", "Triple-Digit Streak", "Record at least 100 g for seven consecutive calendar days.", AchievementCategory.PROTEIN, threshold = 7.0, sortOrder = 8043),

        AchievementDefinition("protein_detective", "Protein Detective", "Assign protein information to 25 active ingredients.", AchievementCategory.PROTEIN, threshold = 25.0, sortOrder = 8050),
        AchievementDefinition("protein_librarian", "Protein Librarian", "Assign protein information to 100 active ingredients.", AchievementCategory.PROTEIN, threshold = 100.0, sortOrder = 8051),
        AchievementDefinition("protein_no_mystery_macros", "No Mystery Macros", "Every active ingredient has protein assigned, with at least 25 active ingredients.", AchievementCategory.PROTEIN, sortOrder = 8052),
        AchievementDefinition("protein_fully_calculated", "Fully Calculated", "Finish one recorded day where every logged meal has complete protein information.", AchievementCategory.PROTEIN, sortOrder = 8053),
        AchievementDefinition("protein_complete_week", "Complete Week", "Record seven consecutive days where every logged meal has complete protein information.", AchievementCategory.PROTEIN, threshold = 7.0, sortOrder = 8054),

        AchievementDefinition("protein_chef", "Protein Chef", "Create 10 active recipes with at least 20 g of fully calculable protein per serving.", AchievementCategory.PROTEIN, threshold = 10.0, sortOrder = 8060),
        AchievementDefinition("protein_high_protein_menu", "High-Protein Menu", "Create 10 active recipes with at least 30 g of fully calculable protein per serving.", AchievementCategory.PROTEIN, threshold = 10.0, sortOrder = 8061),
        AchievementDefinition("protein_explorer", "Protein Explorer", "Log 10 different saved recipes with at least 20 g of protein per logged serving.", AchievementCategory.PROTEIN, threshold = 10.0, sortOrder = 8062)
    )

    val grocery = listOf(
        AchievementDefinition("shopping_spree", "Shopping Spree", "Generate 10 grocery lists.", AchievementCategory.GROCERY, threshold = 10.0, sortOrder = 5001),
        AchievementDefinition("organized", "Organized", "Check off every item in a grocery list.", AchievementCategory.GROCERY, sortOrder = 5002)
    )

    val freezes = listOf(
        AchievementDefinition("winter_is_coming", "Winter Is Coming", "Earn your first freeze.", AchievementCategory.FREEZES, sortOrder = 6001),
        AchievementDefinition("cold_storage", "Cold Storage", "Store the maximum of 3 freezes.", AchievementCategory.FREEZES, sortOrder = 6002),
        AchievementDefinition("close_call", "Close Call", "Save a failed day using a freeze.", AchievementCategory.FREEZES, sortOrder = 6003),
        AchievementDefinition("ice_king", "Ice King", "Use 10 freezes.", AchievementCategory.FREEZES, threshold = 10.0, sortOrder = 6004),
        AchievementDefinition("yolo", "YOLO", "Use your last remaining freeze.", AchievementCategory.HIDDEN, hidden = true, sortOrder = 6005)
    )

    val funny = listOf(
        AchievementDefinition("oops", "Oops", "Log the exact same meal twice within one minute.", AchievementCategory.HIDDEN, hidden = true, sortOrder = 7001),
        AchievementDefinition("night_owl", "Night Owl", "Log food between midnight and 4 AM.", AchievementCategory.HIDDEN, hidden = true, sortOrder = 7002),
        AchievementDefinition("breakfast_champion", "Breakfast Champion", "Log breakfast before 7 AM seven days in a row.", AchievementCategory.HIDDEN, hidden = true, sortOrder = 7003),
        AchievementDefinition("last_minute", "Last Minute", "Log the day's final meal between 23:50 and midnight.", AchievementCategory.HIDDEN, hidden = true, sortOrder = 7004),
        AchievementDefinition("deja_vu", "Déjà Vu", "Log the exact same full day of meals as the previous day.", AchievementCategory.HIDDEN, hidden = true, sortOrder = 7005),
        AchievementDefinition("touch_grass", "Touch Grass", "Open the app on 30 consecutive calendar days.", AchievementCategory.HIDDEN, hidden = true, threshold = 30.0, sortOrder = 7006)
    )

    val all: List<AchievementDefinition> =
        time.map { it.asAchievement() } + weight + score + calories + mealsAndRecipes + protein + grocery + freezes + funny

    val revocableWeightIds: Set<String> = weight.map { it.id }.toSet()
    val revocableProteinIds: Set<String> = setOf("protein_no_mystery_macros")
}
