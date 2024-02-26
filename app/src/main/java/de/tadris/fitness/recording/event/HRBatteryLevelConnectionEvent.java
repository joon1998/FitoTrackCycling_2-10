package de.tadris.fitness.recording.event;

import de.tadris.fitness.recording.component.HeartRateComponent;

public class HRBatteryLevelConnectionEvent {
    public final HeartRateComponent.HeartRateConnectionState state;

    public HRBatteryLevelConnectionEvent(HeartRateComponent.HeartRateConnectionState state) {
        this.state = state;
    }
}
