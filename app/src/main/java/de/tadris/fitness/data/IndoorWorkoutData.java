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

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tadris.fitness.Instance;

public class IndoorWorkoutData {

    public static IndoorWorkoutData fromWorkout(Context context, IndoorWorkout workout) {
        AppDatabase database = Instance.getInstance(context).db;
        return new IndoorWorkoutData(workout, Arrays.asList(database.indoorWorkoutDao().getAllSamplesOfWorkout(workout.id)));
    }

    private IndoorWorkout workout;
    private List<IndoorSample> samples;

    public IndoorWorkoutData(IndoorWorkout workout, List<IndoorSample> samples) {
        this.workout = workout;
        this.samples = samples;
    }

    public IndoorWorkout getWorkout() {
        return workout;
    }

    public void setWorkout(IndoorWorkout workout) {
        this.workout = workout;
    }

    public List<IndoorSample> getSamples() {
        return samples;
    }

    public void setSamples(List<IndoorSample> samples) {
        this.samples = samples;
    }

    public BaseWorkoutData castToBaseWorkoutData() {
        return new BaseWorkoutData(workout, new ArrayList<>(samples));
    }
}
