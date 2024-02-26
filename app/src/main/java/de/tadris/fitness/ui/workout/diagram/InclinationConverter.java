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

package de.tadris.fitness.ui.workout.diagram;

import android.content.Context;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.BaseSample;
import de.tadris.fitness.data.BaseWorkout;
import de.tadris.fitness.data.BaseWorkoutData;
import de.tadris.fitness.data.GpsSample;
import de.tadris.fitness.data.WorkoutManager;

public class InclinationConverter extends AbstractSampleConverter {

    float min, max;

    public InclinationConverter(Context context) {
        super(context);
    }

    @Override
    public void onCreate(BaseWorkoutData data) {
        WorkoutManager.calculateInclination(data.castToGpsData().getSamples());
        List<GpsSample> samples = data.castToGpsData().getSamples();
        this.min = samples.stream().min(inclinationComparator).get().tmpInclination;
        this.max = samples.stream().max(inclinationComparator).get().tmpInclination;
    }

    @Override
    public float getValue(BaseSample sample) {
        return ((GpsSample) sample).tmpInclination;
    }

    @Override
    public String getName() {
        return getString(R.string.inclination);
    }

    @Override
    public String getUnit() {
        return "%";
    }

    @Override
    public int getColor() {
        return R.color.diagramInclination;
    }

    @Override
    public float getMinValue(BaseWorkout workout) {
        return min;
    }

    @Override
    public float getMaxValue(BaseWorkout workout) {
        return max;
    }

    public static Comparator<GpsSample> inclinationComparator = (first, second) -> {
        if (first.tmpInclination > second.tmpInclination) {
            return 1;
        } else if (first.tmpInclination < second.tmpInclination) {
            return -1;
        } else {
            return 0;
        }
    };
}
