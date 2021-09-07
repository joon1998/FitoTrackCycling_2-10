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

package de.tadris.fitness;

import android.content.Context;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

import de.tadris.fitness.data.AppDatabase;
import de.tadris.fitness.data.GpsSample;
import de.tadris.fitness.data.GpsWorkout;
import de.tadris.fitness.data.UserPreferences;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.recording.BaseWorkoutRecorder;
import de.tadris.fitness.recording.gps.GpsWorkoutRecorder;
import de.tadris.fitness.util.DataManager;
import de.tadris.fitness.util.FitoTrackThemes;
import de.tadris.fitness.util.UserDateTimeUtils;
import de.tadris.fitness.util.unit.DistanceUnitUtils;
import de.tadris.fitness.util.unit.EnergyUnitUtils;

public class Instance {

    private static Instance instance;
    public static Instance getInstance(Context context){
        if (context == null) {
            Log.e("Instance", "no Context Provided");
        }
        if(instance == null){
            instance = new Instance(context);
        }
        return instance;
    }

    public final AppDatabase db;
    public BaseWorkoutRecorder recorder;
    public final UserPreferences userPreferences;
    public final FitoTrackThemes themes;
    public final UserDateTimeUtils userDateTimeUtils;
    public final DistanceUnitUtils distanceUnitUtils;
    public final EnergyUnitUtils energyUnitUtils;

    private Instance(Context context) {
        instance = this;
        userPreferences = new UserPreferences(context);
        themes = new FitoTrackThemes(context);
        userDateTimeUtils = new UserDateTimeUtils(userPreferences);
        distanceUnitUtils = new DistanceUnitUtils(context);
        energyUnitUtils = new EnergyUnitUtils(context);
        db = AppDatabase.provideDatabase(context);

        recorder = restoreRecorder(context);

        startBackgroundClean(context);
    }

    private void startBackgroundClean(Context context) {
        DataManager.cleanFilesASync(context);
    }

    private GpsWorkoutRecorder restoreRecorder(Context context) {
        GpsWorkout lastWorkout = db.gpsWorkoutDao().getLastWorkout();
        if (lastWorkout != null && lastWorkout.end == -1) {
            return restoreRecorder(context, lastWorkout);
        }
        return new GpsWorkoutRecorder(context, WorkoutType.getWorkoutTypeById(context, WorkoutType.WORKOUT_TYPE_ID_OTHER));
    }

    private GpsWorkoutRecorder restoreRecorder(Context context, GpsWorkout workout) {
        List<GpsSample> samples = Arrays.asList(db.gpsWorkoutDao().getAllSamplesOfWorkout(workout.id));
        return new GpsWorkoutRecorder(context, workout, samples);
    }

    public void prepareResume(Context context, GpsWorkout workout) {
        recorder = restoreRecorder(context, workout);
    }
}
