package de.tadris.fitness.util.io.general;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import de.tadris.fitness.data.GpsSample;
import de.tadris.fitness.data.GpsWorkout;

public interface IWorkoutExporter {
    void exportWorkout(GpsWorkout workout, List<GpsSample> samples, OutputStream outputStream) throws IOException;

    default void exportWorkout(GpsWorkout workout, List<GpsSample> samples, File file) throws IOException {
        exportWorkout(workout, samples, new FileOutputStream(file));
    }
}
