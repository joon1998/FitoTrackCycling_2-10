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
public interface GpsWorkoutDao {

    @Query("SELECT * FROM workout WHERE id = :id")
    GpsWorkout findById(long id);

    @Query("SELECT * FROM workout_sample WHERE id = :id")
    GpsSample findSampleById(long id);

    @Query("SELECT * FROM workout_sample WHERE workout_id = :workout_id")
    GpsSample[] getAllSamplesOfWorkout(long workout_id);

    @Query("SELECT * FROM workout ORDER BY start DESC")
    GpsWorkout[] getWorkouts();

    @Query("SELECT * FROM workout ORDER BY start DESC LIMIT 1")
    GpsWorkout getLastWorkout();

    @Query("SELECT * FROM workout WHERE workoutType = :type ORDER BY start DESC LIMIT 1")
    GpsWorkout getLastWorkoutByType(String type);

    @Query("SELECT * FROM workout ORDER BY start ASC")
    GpsWorkout[] getAllWorkoutsHistorically();

    @Query("SELECT * FROM workout WHERE workoutType = :workout_type ORDER BY start ASC ")
    GpsWorkout[] getWorkoutsHistorically(String workout_type);

    @Query("SELECT * FROM workout WHERE start = :start")
    GpsWorkout getWorkoutByStart(long start);

    @Query("SELECT * FROM workout WHERE id = :id")
    GpsWorkout getWorkoutById(long id);

    @Query("SELECT * FROM workout_sample")
    GpsSample[] getSamples();

    @Insert
    void insertWorkoutAndSamples(GpsWorkout workout, GpsSample[] samples);

    @Delete
    void deleteWorkoutAndSamples(GpsWorkout workout, GpsSample[] toArray);

    @Insert
    void insertWorkout(GpsWorkout workout);

    @Delete
    void deleteWorkout(GpsWorkout workout);

    @Update
    void updateWorkout(GpsWorkout workout);

    @Insert
    void insertSample(GpsSample sample);

    @Delete
    void deleteSample(GpsSample sample);

    @Update
    void updateSamples(GpsSample[] samples);

    @Update
    void updateSample(GpsSample sample);
}
