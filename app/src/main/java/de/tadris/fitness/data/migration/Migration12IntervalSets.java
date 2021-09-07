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

package de.tadris.fitness.data.migration;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.data.AppDatabase;
import de.tadris.fitness.data.GpsSample;
import de.tadris.fitness.data.GpsWorkout;
import de.tadris.fitness.data.GpsWorkoutData;
import de.tadris.fitness.data.Interval;

/**
 * Before release 12 interval data was reconstructed from the interval sets.
 * Because they are editable now, triggered intervals are saved to the samples.
 * This migration calculates the interval times and flags the WorkoutSample.intervalTriggered.
 */
public class Migration12IntervalSets extends Migration {

    private final AppDatabase database;
    private final boolean intervalSetIncludesPauses;

    public Migration12IntervalSets(Context context, MigrationListener listener) {
        super(context, listener);
        Instance instance = Instance.getInstance(context);
        database = instance.db;
        intervalSetIncludesPauses = instance.userPreferences.intervalsIncludePauses();
    }

    @Override
    public void migrate() {
        GpsWorkout[] workouts = database.gpsWorkoutDao().getWorkouts();
        int i = 0;
        for (GpsWorkout workout : workouts) {
            migrateWorkout(workout);
            listener.onProgressUpdate(100 * i / workouts.length);
            i++;
        }
        listener.onProgressUpdate(100);
    }

    public void migrateWorkout(GpsWorkout workout) {
        if (workout.intervalSetUsedId > 0) {
            GpsWorkoutData workoutData = GpsWorkoutData.fromWorkout(context, workout);
            List<GpsSample> samples = new ArrayList<>(workoutData.getSamples());
            for (Pair<Long, Interval> pair : getIntervalSetTimesFromWorkout(workoutData)) {
                long relativeTime = pair.first;
                Interval interval = pair.second;
                while (!samples.isEmpty()) {
                    GpsSample sample = samples.remove(0);
                    if (sample.relativeTime >= relativeTime) {
                        sample.intervalTriggered = interval.id;
                        database.gpsWorkoutDao().updateSample(sample);
                        break;
                    }
                }
            }
        }
    }

    private List<Pair<Long, Interval>> getIntervalSetTimesFromWorkout(GpsWorkoutData data) {
        List<Pair<Long, Interval>> result = new ArrayList<>();
        Interval[] intervals = database.intervalDao().getAllIntervalsOfSet(data.getWorkout().intervalSetUsedId);
        if (intervals == null || intervals.length == 0) {
            return result;
        }
        GpsWorkout workout = data.getWorkout();
        List<GpsSample> samples = data.getSamples();

        int index = 0;
        long time = 0;
        if (intervalSetIncludesPauses) {
            long lastTime = samples.get(0).absoluteTime;
            for (GpsSample sample : samples) {
                if (index >= intervals.length) {
                    index = 0;
                }
                Interval currentInterval = intervals[index];
                time += sample.absoluteTime - lastTime;
                if (time > currentInterval.delayMillis) {
                    time = 0;
                    index++;
                    result.add(new Pair<>(sample.relativeTime, currentInterval));
                }
                lastTime = sample.absoluteTime;
            }
        } else {
            while (time < workout.duration) {
                if (index >= intervals.length) {
                    index = 0;
                }
                Interval interval = intervals[index];

                result.add(new Pair<>(time, interval));

                time += interval.delayMillis;
                index++;
            }
        }
        return result;
    }
}
