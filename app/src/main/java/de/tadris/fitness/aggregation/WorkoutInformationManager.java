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

import de.tadris.fitness.aggregation.information.AverageHeartRate;
import de.tadris.fitness.aggregation.information.AverageMotionSpeed;
import de.tadris.fitness.aggregation.information.AveragePace;
import de.tadris.fitness.aggregation.information.AverageTotalSpeed;
import de.tadris.fitness.aggregation.information.BurnedEnergy;
import de.tadris.fitness.aggregation.information.Distance;
import de.tadris.fitness.aggregation.information.Duration;
import de.tadris.fitness.aggregation.information.EnergyConsumption;
import de.tadris.fitness.aggregation.information.Repetitions;
import de.tadris.fitness.aggregation.information.TopSpeed;
import de.tadris.fitness.aggregation.information.WorkoutCount;

public class WorkoutInformationManager {

    private final Context context;
    private final List<WorkoutInformation> information = new ArrayList<>();

    public WorkoutInformationManager(Context context) {
        this.context = context;
        addInformation();
    }

    private void addInformation() {
        information.add(new Distance(context));
        information.add(new Duration(context));
        information.add(new AverageMotionSpeed(context));
        information.add(new AverageTotalSpeed(context));
        information.add(new AveragePace(context));
        information.add(new TopSpeed(context));
        information.add(new BurnedEnergy(context));
        information.add(new EnergyConsumption(context));
        information.add(new AverageHeartRate(context));
        information.add(new Repetitions(context));
        information.add(new WorkoutCount(context));
    }

    public List<WorkoutInformation> getInformation() {
        return information;
    }

}
