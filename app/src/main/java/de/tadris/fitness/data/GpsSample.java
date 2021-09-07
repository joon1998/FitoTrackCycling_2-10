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
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.mapsforge.core.model.LatLong;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "workout_sample",
        foreignKeys = @ForeignKey(
                entity = GpsWorkout.class,
                parentColumns = "id",
                childColumns = "workout_id",
                onDelete = CASCADE))
@JsonIgnoreProperties(ignoreUnknown = true)
public class GpsSample extends BaseSample {

    @ColumnInfo(name = "workout_id", index = true)
    public long workoutId;

    public double lat;

    public double lon;

    /**
     * Elevation over the WGS84 ellipsoid in meters
     */
    public double elevation;

    /**
     * Elevation over the media sea level in meters
     * This value should be displayed to the user.
     */
    @ColumnInfo(name = "elevation_msl")
    public double elevationMSL = 0;

    public double speed;

    public float pressure;

    @JsonIgnore
    @Ignore
    public double tmpRoundedSpeed;

    @JsonIgnore
    @Ignore
    public double tmpElevation;

    @JsonIgnore
    @Ignore
    public float tmpInclination;

    public LatLong toLatLong(){
        return new LatLong(lat, lon);
    }


}
