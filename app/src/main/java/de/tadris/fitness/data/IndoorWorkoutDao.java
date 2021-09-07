/*
 * Copyright (c) 2021 Jannis Scheibe <jannis@tadris.de>
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

package de.tadris.fitness.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface IndoorWorkoutDao {

    @Query("SELECT * FROM indoor_workout WHERE id = :id")
    IndoorWorkout findById(long id);

    @Query("SELECT * FROM indoor_sample WHERE id = :id")
    IndoorSample findSampleById(long id);

    @Query("SELECT * FROM indoor_sample WHERE workout_id = :workout_id")
    IndoorSample[] getAllSamplesOfWorkout(long workout_id);

    @Query("SELECT * FROM indoor_workout ORDER BY start DESC")
    IndoorWorkout[] getWorkouts();

    @Query("SELECT * FROM indoor_workout ORDER BY start DESC LIMIT 1")
    IndoorWorkout getLastWorkout();

    @Query("SELECT * FROM indoor_workout WHERE workoutType = :type ORDER BY start DESC LIMIT 1")
    IndoorWorkout getLastWorkoutByType(String type);

    @Query("SELECT * FROM indoor_workout ORDER BY start ASC")
    IndoorWorkout[] getAllWorkoutsHistorically();

    @Query("SELECT * FROM indoor_workout WHERE workoutType = :workout_type ORDER BY start ASC ")
    IndoorWorkout[] getWorkoutsHistorically(String workout_type);

    @Query("SELECT * FROM indoor_workout WHERE start = :start")
    IndoorWorkout getWorkoutByStart(long start);

    @Query("SELECT * FROM indoor_workout WHERE id = :id")
    IndoorWorkout getWorkoutById(long id);

    @Query("SELECT * FROM indoor_sample")
    IndoorSample[] getSamples();

    @Insert
    void insertWorkoutAndSamples(IndoorWorkout workout, IndoorSample[] samples);

    @Delete
    void deleteWorkoutAndSamples(IndoorWorkout workout, IndoorSample[] toArray);

    @Insert
    void insertWorkout(IndoorWorkout workout);

    @Delete
    void deleteWorkout(IndoorWorkout workout);

    @Update
    void updateWorkout(IndoorWorkout workout);

    @Insert
    void insertSample(IndoorSample sample);

    @Delete
    void deleteSample(IndoorSample sample);

    @Update
    void updateSamples(IndoorSample[] samples);

    @Update
    void updateSample(IndoorSample sample);
}
