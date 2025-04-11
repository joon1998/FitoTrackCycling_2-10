package de.tadris.fitness.util.calorie

import de.tadris.fitness.data.BaseWorkout
import de.tadris.fitness.data.preferences.UserMeasurements

object CalorieMovingAverageUtil {

    fun calculateMovingAverage(
        calculator: CalorieCalculator,
        measurements: UserMeasurements,
        workouts: List<BaseWorkout>,
        windowSize: Int = 5
    ): Double {
        if (workouts.isEmpty() || windowSize <= 0) return 0.0


        val sortedWorkouts = workouts.sortedByDescending { it.start }


        val recentWorkouts = sortedWorkouts.take(windowSize)


        val totalCalories = recentWorkouts.sumOf {
            calculator.calculateCalories(measurements, it)
        }

        return totalCalories.toDouble() / recentWorkouts.size
    }
}
