package de.tadris.fitness.recording.event;

import android.bluetooth.BluetoothDevice;

public class HRBatteryLevelChangeEvent {
    public final BluetoothDevice device;
    public final int batteryLevel;

    public HRBatteryLevelChangeEvent(BluetoothDevice device, int batteryLevel) {
        this.device = device;
        this.batteryLevel = batteryLevel;
    }
}
