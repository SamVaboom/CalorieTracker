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

    val weight = listOf(
        AchievementDefinition("weight_first", "First Weigh-In", "Every graph starts with one dot.", AchievementCategory.WEIGHT, threshold = 1.0, sortOrder = 1001),
        AchievementDefinition("weight_7_dates", "Data Point", "Record weight on 7 different dates", AchievementCategory.WEIGHT, threshold = 7.0, sortOrder = 1002),
        AchievementDefinition("weight_30_dates", "Trend Watcher", "Record weight on 30 different dates", AchievementCategory.WEIGHT, threshold = 30.0, sortOrder = 1003),
        AchievementDefinition("weight_down_1", "One Down", "Record a weight at least 1 kg below the first entry", AchievementCategory.WEIGHT, threshold = 1.0, sortOrder = 1004),
        AchievementDefinition("weight_down_5", "Five Down", "Record a weight at least 5 kg below the first entry", AchievementCategory.WEIGHT, threshold = 5.0, sortOrder = 1005),
        AchievementDefinition("weight_down_10", "Double Digits", "Record a weight at least 10 kg below the first entry", AchievementCategory.WEIGHT, threshold = 10.0, sortOrder = 1006),
        AchievementDefinition("weight_new_low", "New Low", "Record a new lowest weight after five earlier entries", AchievementCategory.WEIGHT, threshold = 6.0, sortOrder = 1007)
    )

    val mealAndRecipe = listOf(
        AchievementDefinition("picky_eater", "Picky Eater", "Log the same saved recipe 15 times in 30 days", AchievementCategory.MEAL_HABITS, threshold = 15.0, sortOrder = 2001),
        AchievementDefinition("creature_of_habit", "Creature of Habit", "Log the same recipe on 7 dates in 14 days", AchievementCategory.MEAL_HABITS, threshold = 7.0, sortOrder = 2002),
        AchievementDefinition("meal_prepper", "Meal Prepper", "Log the same recipe on 3 consecutive days", AchievementCategory.MEAL_HABITS, threshold = 3.0, sortOrder = 2003),
        AchievementDefinition("explorer", "Explorer", "Log 25 different recipes", AchievementCategory.RECIPES, threshold = 25.0, sortOrder = 2004),
        AchievementDefinition("culinary_tourist", "Culinary Tourist", "Log 50 different recipes", AchievementCategory.RECIPES, threshold = 50.0, sortOrder = 2005),
        AchievementDefinition("home_cook", "Home Cook", "Log 100 recipe-based meals", AchievementCategory.RECIPES, threshold = 100.0, sortOrder = 2006),
        AchievementDefinition("master_chef", "Master Chef", "Create 50 recipes", AchievementCategory.RECIPES, threshold = 50.0, sortOrder = 2007),
        AchievementDefinition("ingredient_collector", "Ingredient Collector", "Create 100 ingredients", AchievementCategory.RECIPES, threshold = 100.0, sortOrder = 2008)
    )

    val calorieAndScore = listOf(
        AchievementDefinition("fasting_monk", "Fasting Monk", "Finalize one day below 1000 kcal", AchievementCategory.CALORIES, sortOrder = 3001),
        AchievementDefinition("enlightened_monk", "Enlightened Monk", "Finalize three days below 1000 kcal in 7 days", AchievementCategory.CALORIES, sortOrder = 3002),
        AchievementDefinition("air_diet", "Air Diet", "Finalize one day below 800 kcal", AchievementCategory.HIDDEN, hidden = true, sortOrder = 3003),
        AchievementDefinition("the_american", "The American", "Finalize one day above 3000 kcal", AchievementCategory.CALORIES, sortOrder = 3004),
        AchievementDefinition("thanksgiving", "Thanksgiving", "Finalize one day above 4000 kcal", AchievementCategory.CALORIES, sortOrder = 3005),
        AchievementDefinition("bottomless_pit", "Bottomless Pit", "Finalize one day above 5000 kcal", AchievementCategory.HIDDEN, hidden = true, sortOrder = 3006),
        AchievementDefinition("bullseye", "Bullseye", "Finish one finalized day at exactly 100%", AchievementCategory.SCORE, sortOrder = 3101),
        AchievementDefinition("double_bullseye", "Double Bullseye", "Finish exactly 100% on two consecutive days", AchievementCategory.SCORE, sortOrder = 3102),
        AchievementDefinition("hat_trick", "Hat Trick", "Finish exactly 100% on three consecutive days", AchievementCategory.SCORE, sortOrder = 3103),
        AchievementDefinition("balanced_week", "Balanced Week", "Finish at 85% or higher for seven consecutive days", AchievementCategory.SCORE, sortOrder = 3104),
        AchievementDefinition("safe_passage", "Safe Passage", "Finish 30 total days at 80% or higher", AchievementCategory.SCORE, threshold = 30.0, sortOrder = 3105)
    )

    val freezes = listOf(
        AchievementDefinition("winter_is_coming", "Winter Is Coming", "Earn the first freeze", AchievementCategory.FREEZES, sortOrder = 4001),
        AchievementDefinition("cold_storage", "Cold Storage", "Reach the configured maximum number of freezes", AchievementCategory.FREEZES, sortOrder = 4002),
        AchievementDefinition("close_call", "Close Call", "Protect a failed day using a freeze", AchievementCategory.FREEZES, sortOrder = 4003),
        AchievementDefinition("ice_king", "Ice King", "Use 10 freezes", AchievementCategory.FREEZES, threshold = 10.0, sortOrder = 4004),
        AchievementDefinition("deep_freeze", "Deep Freeze", "Use 25 freezes", AchievementCategory.FREEZES, threshold = 25.0, sortOrder = 4005),
        AchievementDefinition("seven_good_days", "Seven Good Days", "Earn a freeze after seven qualifying days", AchievementCategory.FREEZES, threshold = 7.0, sortOrder = 4006)
    )

    val all: List<AchievementDefinition> = time.map { it.asAchievement() } + weight + mealAndRecipe + calorieAndScore + freezes
}
