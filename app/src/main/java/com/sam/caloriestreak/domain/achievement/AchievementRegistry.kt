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

    val all: List<AchievementDefinition> = time.map { it.asAchievement() } + weight
}
