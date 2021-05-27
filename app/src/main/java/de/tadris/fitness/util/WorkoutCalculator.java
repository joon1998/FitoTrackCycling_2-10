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

package de.tadris.fitness.util;

import org.mapsforge.core.model.LatLong;

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.data.WorkoutData;
import de.tadris.fitness.data.WorkoutSample;

public class WorkoutCalculator {

    public static List<Pause> getPausesFromWorkout(WorkoutData data) {
        List<Pause> result = new ArrayList<>();
        List<WorkoutSample> samples = data.getSamples();

        long absoluteTime = data.getWorkout().start;
        long relativeTime = 0;
        boolean lastWasPause = false;

        for (WorkoutSample sample : samples) {
            long absoluteDiff = sample.absoluteTime - absoluteTime;
            long relativeDiff = sample.relativeTime - relativeTime;
            long diff = absoluteDiff - relativeDiff;

            if (diff > 10000) {
                if (lastWasPause) {
                    // Add duration to last pause if there is no sample between detected pauses
                    result.get(result.size() - 1).addDuration(diff);
                } else {
                    result.add(new Pause(absoluteTime, relativeTime, diff, sample.toLatLong()));
                }
                lastWasPause = true;
            } else {
                lastWasPause = false;
            }
            absoluteTime = sample.absoluteTime;
            relativeTime = sample.relativeTime;
        }
        return result;
    }

    public static class Pause {
        public final long absoluteTimeStart;
        public final long relativeTimeStart;
        public long duration;
        public final LatLong location;

        public Pause(long absoluteTimeStart, long relativeTimeStart, long duration, LatLong location) {
            this.absoluteTimeStart = absoluteTimeStart;
            this.relativeTimeStart = relativeTimeStart;
            this.duration = duration;
            this.location = location;
        }

        private void addDuration(long duration) {
            this.duration += duration;
        }

    }

    /**
     * Returns a list of relative times when intervals were triggered
     */
    public static List<Long> getIntervalSetTimesFromWorkout(WorkoutData data) {
        List<Long> result = new ArrayList<>();
        List<WorkoutSample> samples = data.getSamples();

        for (WorkoutSample sample : samples) {
            if (sample.intervalTriggered > 0) {
                result.add(sample.relativeTime);
            }
        }
        return result;
    }

}