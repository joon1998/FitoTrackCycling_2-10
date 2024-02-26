/*
 * Copyright (c) 2023 Jannis Scheibe <jannis@tadris.de>
 *
 * This file is part of FitoTrack
 *
 * FitoTrack is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     FitoTrack is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tadris.fitness.recording.indoor

import android.content.Context
import de.tadris.fitness.Instance
import de.tadris.fitness.data.BaseWorkoutData
import de.tadris.fitness.data.IndoorSample
import de.tadris.fitness.data.IndoorWorkout
import de.tadris.fitness.data.IndoorWorkoutData
import de.tadris.fitness.util.WorkoutCalculator
import de.tadris.fitness.util.calorie.CalorieCalculator

class IndoorWorkoutSaver(private val context: Context, workoutData: IndoorWorkoutData) {

    private val workout: IndoorWorkout = workoutData.workout
    private val samples: List<IndoorSample> = workoutData.samples

    fun save() {
        calculateData()
        insertWorkoutAndSamples()
    }

    private fun calculateData() {
        setIds()
        setStartEnd()
        setRepetitions()
        setFrequencies()
        setMaxAvgFrequency()
        setMaxAvgIntensity()
        setHeartRate()
        setCalories()
    }

    private fun setIds() {
        workout.id = System.nanoTime()
        var i = 0
        for (sample in samples) {
            i++
            sample.id = workout.id + i
            sample.workoutId = workout.id
        }
    }

    private fun setStartEnd() {
        // Recalculate start and end time by cutting start and end pause
        if (samples.isEmpty()) return
        workout.start = samples.first().absoluteTime
        workout.end = samples.last().absoluteEndTime
        workout.pauseDuration = WorkoutCalculator.calculatePauseDuration(getBaseWorkoutData())
        workout.duration = workout.end - workout.start - workout.pauseDuration
    }

    private fun setRepetitions() {
        workout.repetitions = samples.sumOf { it.repetitions }
    }

    private fun setFrequencies() {
        if (samples.size > 2) {
            // Recalculate exact frequency between samples
            var lastTime = samples[0].absoluteTime
            samples.forEach { sample ->
                val timeDiff = sample.absoluteTime - lastTime
                sample.frequency =
                    if (timeDiff > 0) 1000 * sample.repetitions.toDouble() / timeDiff else 0.0
                lastTime = sample.absoluteTime
            }
        }
    }

    private fun setMaxAvgFrequency() {
        workout.avgFrequency = 1000 * workout.repetitions.toDouble() / workout.duration
        workout.maxFrequency = samples.maxOf { it.frequency }
    }

    private fun setMaxAvgIntensity() {
        if (samples.isNotEmpty()) {
            workout.avgIntensity = samples.sumOf { it.intensity } / samples.size
            workout.maxIntensity = samples.maxOf { it.intensity }
        }
    }

    private fun setHeartRate() {
        var heartRateSum = 0
        var maxHeartRate = -1
        for (sample in samples) {
            heartRateSum += sample.heartRate
            if (sample.heartRate > maxHeartRate) {
                maxHeartRate = sample.heartRate
            }
        }
        workout.maxHeartRate = maxHeartRate
        workout.avgHeartRate = heartRateSum / samples.size
    }

    private fun setCalories() {
        val measurements = Instance.getInstance(context).userPreferences.measurements
        workout.calorie = CalorieCalculator(context).calculateCalories(measurements, workout)
    }

    private fun insertWorkoutAndSamples() {
        Instance.getInstance(context).db.indoorWorkoutDao()
            .insertWorkoutAndSamples(workout, samples.toTypedArray())
    }

    private fun getBaseWorkoutData() = BaseWorkoutData(workout, samples)

}