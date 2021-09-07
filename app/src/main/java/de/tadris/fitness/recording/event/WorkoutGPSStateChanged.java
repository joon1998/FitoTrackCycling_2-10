package de.tadris.fitness.recording.event;

import de.tadris.fitness.recording.gps.GpsWorkoutRecorder;

public class WorkoutGPSStateChanged {

    public final GpsWorkoutRecorder.GpsState oldState;
    public final GpsWorkoutRecorder.GpsState newState;

    public WorkoutGPSStateChanged(GpsWorkoutRecorder.GpsState oldState, GpsWorkoutRecorder.GpsState newState) {
        this.oldState = oldState;
        this.newState = newState;
    }
}
