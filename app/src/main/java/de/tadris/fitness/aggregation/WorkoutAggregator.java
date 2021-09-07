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

package de.tadris.fitness.aggregation;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.data.BaseWorkout;

public class WorkoutAggregator {

    private final Context context;
    private final WorkoutFilter filter;
    private final WorkoutInformation information;
    private final AggregationSpan span;

    public WorkoutAggregator(Context context, WorkoutFilter filter, WorkoutInformation information, AggregationSpan span) {
        this.context = context;
        this.filter = filter;
        this.information = information;
        this.span = span;
    }

    public AggregatedWorkoutData aggregate() {
        return new AggregatedWorkoutData(getResults(), span);
    }

    private List<WorkoutInformationResult> getResults() {
        List<WorkoutInformationResult> results = new ArrayList<>();
        List<BaseWorkout> workouts = Instance.getInstance(context).db.getAllWorkouts();
        for (BaseWorkout workout : workouts) {
            if (filter.isAccepted(workout)) {
                if (isAvailableFor(workout)) {
                    results.add(getResultFor(workout));
                }
            }
        }
        return results;
    }

    private boolean isAvailableFor(BaseWorkout workout) {
        return information.isInformationAvailableFor(workout);
    }

    private WorkoutInformationResult getResultFor(BaseWorkout workout) {
        return new WorkoutInformationResult(workout, information.getValueFromWorkout(workout));
    }
}
