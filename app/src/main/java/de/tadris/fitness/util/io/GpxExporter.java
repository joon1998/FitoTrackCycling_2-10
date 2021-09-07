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

package de.tadris.fitness.util.io;

import android.annotation.SuppressLint;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import de.tadris.fitness.data.GpsSample;
import de.tadris.fitness.data.GpsWorkout;
import de.tadris.fitness.util.gpx.Gpx;
import de.tadris.fitness.util.gpx.GpxTpxExtension;
import de.tadris.fitness.util.gpx.Metadata;
import de.tadris.fitness.util.gpx.Track;
import de.tadris.fitness.util.gpx.TrackPoint;
import de.tadris.fitness.util.gpx.TrackPointExtensions;
import de.tadris.fitness.util.gpx.TrackSegment;
import de.tadris.fitness.util.io.general.IWorkoutExporter;

import static java.lang.Math.abs;

public class GpxExporter implements IWorkoutExporter {

    @SuppressLint("SimpleDateFormat") // This has nothing to do with localisation
    public final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public GpxExporter() {
    }

    @Override
    public void exportWorkout(GpsWorkout workout, List<GpsSample> samples, OutputStream fileStream) throws IOException {
        XmlMapper mapper = new XmlMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        mapper.writeValue(fileStream, getGpxFromWorkout(workout, samples));
    }

    private Gpx getGpxFromWorkout(GpsWorkout workout, List<GpsSample> samples) {
        Track track = getTrackFromWorkout(workout, samples, 0);
        ArrayList<Track> tracks = new ArrayList<>();
        tracks.add(track);
        Metadata meta = new Metadata(workout.toString(), workout.comment, getDateTime(workout.start));
        return new Gpx("1.0", "FitoTrack", meta, workout.toString(), workout.comment, tracks);
    }

    private Track getTrackFromWorkout(GpsWorkout workout, List<GpsSample> samples, int number) {
        Track track = new Track();
        track.setNumber(number);
        track.setName(workout.toString());
        track.setCmt(workout.comment);
        track.setDesc(workout.comment);
        track.setSrc("FitoTrack");
        track.setType(workout.workoutTypeId);
        track.setTrkseg(new ArrayList<>());

        TrackSegment segment = new TrackSegment();
        ArrayList<TrackPoint> trkpt = new ArrayList<>();

        for (GpsSample sample : samples) {
            trkpt.add(new TrackPoint(sample.lat, sample.lon, sample.elevation,
                    getDateTime(sample.absoluteTime), new TrackPointExtensions(sample.speed, new GpxTpxExtension(sample.heartRate))));
        }
        segment.setTrkpt(trkpt);

        ArrayList<TrackSegment> segments = new ArrayList<>();
        segments.add(segment);
        track.setTrkseg(segments);

        return track;
    }

    private String getDateTime(long time) {
        return getDateTime(new Date(time));
    }

    private String getDateTime(Date date) {
        // Calculate time zone offset
        // Normally we could use the 'X' char in the formatter to specify the timezone but this is only available in Android 7+
        // Since this minSdkVersion is 21 (Android 5) we cannot use it
        long milliseconds = TimeZone.getDefault().getOffset(date.getTime());
        char sign = '+';
        if (milliseconds < 0) {
            sign = '-';
        }
        long hours = abs(TimeUnit.MILLISECONDS.toHours(milliseconds));
        long minutes = abs(TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(hours));

        return String.format(Locale.GERMANY,"%s%c%02d:%02d", formatter.format(date), sign, hours, minutes);
    }
}
