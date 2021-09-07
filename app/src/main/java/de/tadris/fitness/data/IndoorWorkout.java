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

import androidx.room.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import de.tadris.fitness.Instance;

@Entity(tableName = "indoor_workout")
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndoorWorkout extends BaseWorkout {

    public int repetitions;

    /**
     * Average frequency in hertz
     */
    public double avgFrequency;

    public double maxFrequency;

    public double maxIntensity;

    public double avgIntensity;

    public boolean hasIntensityValues() {
        return avgIntensity > 0;
    }

    public boolean hasEstimatedDistance() {
        return workoutTypeId.equals("treadmill");
    }

    public double estimateDistance(Context context) {
        float stepLength = Instance.getInstance(context).userPreferences.getStepLength();
        return repetitions * stepLength;
    }

    public double estimateSpeed(Context context) {
        return estimateDistance(context) / (duration / 1000d);
    }

}
