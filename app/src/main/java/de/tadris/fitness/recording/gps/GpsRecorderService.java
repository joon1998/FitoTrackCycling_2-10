
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

package de.tadris.fitness.recording.gps;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.mapsforge.core.model.LatLong;

import de.tadris.fitness.recording.BaseRecorderService;
import de.tadris.fitness.recording.event.LocationChangeEvent;
import de.tadris.fitness.recording.event.PressureChangeEvent;
import de.tadris.fitness.recording.event.WorkoutGPSStateChanged;
import de.tadris.fitness.recording.information.GPSStatus;

/**
 * The recorder service is responsible for receiving data from the system (Location, Pressure, HeartRate)
 * It stays alive even when the UI activity classes are destroyed.
 */
public class GpsRecorderService extends BaseRecorderService {

    /**
     * @param location the location whose geographical coordinates should be converted.
     * @return a new LatLong with the geographical coordinates taken from the given location.
     */
    public static LatLong locationToLatLong(Location location) {
        return new LatLong(location.getLatitude(), location.getLongitude());
    }

    private Sensor mPressureSensor = null;
    private LocationManager mLocationManager = null;

    private static final int LOCATION_INTERVAL = 1000;

    private class LocationChangedListener implements android.location.LocationListener {
        final Location mLastLocation;

        LocationChangedListener(String provider) {
            Log.i(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            EventBus.getDefault().postSticky(new LocationChangeEvent(location));
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i(TAG, "onStatusChanged: " + provider);
        }
    }

    private class PressureListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            EventBus.getDefault().post(new PressureChangeEvent(event.values[0]));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private final PressureListener pressureListener = new PressureListener();

    private final LocationChangedListener gpsListener = new LocationChangedListener(LocationManager.GPS_PROVIDER);

    @Override
    public void onCreate() {
        super.onCreate();
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, 0, gpsListener);
            checkLastKnownLocation();
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        initializePressureSensor();
        if (mSensorManager != null && mPressureSensor != null) {
            Log.i(TAG, "started Pressure Sensor");
            mSensorManager.registerListener(pressureListener, mPressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.i(TAG, "no Pressure Sensor Available");
        }

        EventBus.getDefault().register(this);
    }

    private void initializeLocationManager() {
        Log.i(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void initializePressureSensor() {
        Log.i(TAG, "initializePressureSensor");
        if (mPressureSensor == null) {
            mPressureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        }
    }

    private void checkLastKnownLocation() throws SecurityException {
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            gpsListener.onLocationChanged(location);
        }
    }

    @Subscribe
    public void onGPSStateChange(WorkoutGPSStateChanged event) {
        GPSStatus announcement = new GPSStatus(this);
        if (instance.recorder.isResumed() && announcement.isAnnouncementEnabled()) {
            if (event.oldState == GpsWorkoutRecorder.GpsState.SIGNAL_LOST) { // GPS Signal found
                mTTSController.speak(announcement.getSpokenGPSFound());
            } else if (event.newState == GpsWorkoutRecorder.GpsState.SIGNAL_LOST) {
                mTTSController.speak(announcement.getSpokenGPSLost());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);

        if (mLocationManager != null) {
            mLocationManager.removeUpdates(gpsListener);
        }

        if (mSensorManager != null && mPressureSensor != null) {
            mSensorManager.unregisterListener(pressureListener);
        }
    }
}
