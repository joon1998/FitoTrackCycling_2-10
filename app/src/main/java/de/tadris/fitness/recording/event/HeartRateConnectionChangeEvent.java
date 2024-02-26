package de.tadris.fitness.recording.event;

import de.tadris.fitness.recording.component.HeartRateComponent;

public class HeartRateConnectionChangeEvent {

    public final HeartRateComponent.HeartRateConnectionState state;

    public HeartRateConnectionChangeEvent(HeartRateComponent.HeartRateConnectionState state) {
        this.state = state;
    }
}
