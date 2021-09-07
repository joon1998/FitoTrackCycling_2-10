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

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

public abstract class BaseSample {

    @PrimaryKey
    public long id;

    public long absoluteTime;

    public long relativeTime;

    @ColumnInfo(name = "heart_rate")
    public int heartRate = -1; // in bpm

    /**
     * -1 -> No interval was triggered
     * greater than 0 -> Interval with this id was triggered at this sample
     */
    @ColumnInfo(name = "interval_triggered")
    public long intervalTriggered = -1;

}
