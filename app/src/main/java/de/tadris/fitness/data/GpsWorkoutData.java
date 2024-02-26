/*
 * Copyright (c) 2020 Jannis Scheibe <jannis@tadris.de>
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

import android.content.Context;

import java.util.Arrays;
import java.util.List;

import de.tadris.fitness.Instance;

public class GpsWorkoutData {

    public static GpsWorkoutData fromWorkout(Context context, GpsWorkout workout) {
        AppDatabase database = Instance.getInstance(context).db;
        return fromWorkout(database.gpsWorkoutDao(), workout);
    }

    public static GpsWorkoutData fromWorkout(GpsWorkoutDao workoutDao, GpsWorkout workout) {
        return new GpsWorkoutData(workout, Arrays.asList(workoutDao.getAllSamplesOfWorkout(workout.id)));
    }

    private GpsWorkout workout;
    private List<GpsSample> samples;

    public GpsWorkoutData(GpsWorkout workout, List<GpsSample> samples) {
        this.workout = workout;
        this.samples = samples;
    }

    public GpsWorkout getWorkout() {
        return workout;
    }

    public void setWorkout(GpsWorkout workout) {
        this.workout = workout;
    }

    public List<GpsSample> getSamples() {
        return samples;
    }

    public void setSamples(List<GpsSample> samples) {
        this.samples = samples;
    }
}
