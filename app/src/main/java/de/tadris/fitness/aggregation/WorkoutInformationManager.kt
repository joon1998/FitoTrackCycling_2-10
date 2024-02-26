/*
 * Copyright (c) 2022 Jannis Scheibe <jannis@tadris.de>
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
package de.tadris.fitness.aggregation

import android.content.Context
import de.tadris.fitness.aggregation.information.*
import de.tadris.fitness.data.BaseWorkout

class WorkoutInformationManager(context: Context) {

    private val information = mutableListOf<WorkoutInformation>(
        Distance(context),
        Duration(context),
        AverageMotionSpeed(context),
        AverageTotalSpeed(context),
        AveragePace(context),
        TopSpeed(context),
        BurnedEnergy(context),
        EnergyConsumption(context),
        AverageHeartRate(context),
        Repetitions(context),
        WorkoutCount(context),
    )

    fun addInformation(information: WorkoutInformation) {
        this.information.add(information)
    }

    fun getInformation(): List<WorkoutInformation> {
        return information
    }

    fun getAvailableInformationFor(workout: BaseWorkout) =
        information.filter { it.isInformationAvailableFor(workout) }

    fun findById(typeId: String) = information.find { it.id == typeId }

}