package de.tadris.fitness.recording.event;

import de.tadris.fitness.recording.gps.GpsRecorderService;

public class HeartRateConnectionChangeEvent {

    public final GpsRecorderService.HeartRateConnectionState state;

    public HeartRateConnectionChangeEvent(GpsRecorderService.HeartRateConnectionState state) {
        this.state = state;
    }
}
