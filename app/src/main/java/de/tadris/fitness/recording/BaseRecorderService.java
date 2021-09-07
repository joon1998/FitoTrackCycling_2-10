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

package de.tadris.fitness.recording;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.tadris.fitness.BuildConfig;
import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.Interval;
import de.tadris.fitness.recording.announcement.TTSController;
import de.tadris.fitness.recording.announcement.VoiceAnnouncements;
import de.tadris.fitness.recording.event.HeartRateChangeEvent;
import de.tadris.fitness.recording.event.HeartRateConnectionChangeEvent;
import de.tadris.fitness.recording.gps.GpsRecorderService;
import de.tadris.fitness.recording.gps.GpsWorkoutRecorder;
import de.tadris.fitness.recording.sensors.HRManager;
import de.tadris.fitness.ui.record.RecordWorkoutActivity;
import de.tadris.fitness.util.NotificationHelper;
import no.nordicsemi.android.ble.observer.ConnectionObserver;

public abstract class BaseRecorderService extends Service {

    public static final String TTS_CONTROLLER_ID = "RecorderService";
    protected Date serviceStartTime;

    public static final String TAG = "LocationListener";
    protected static final int NOTIFICATION_ID = 10;

    protected static final int WATCHDOG_INTERVAL = 2_500; // Trigger Watchdog every 2.5 Seconds

    protected PowerManager.WakeLock wakeLock;

    protected SensorManager mSensorManager = null;
    protected Instance instance = null;

    protected TTSController mTTSController;
    protected VoiceAnnouncements announcements;

    protected WatchDogRunner mWatchdogRunner;
    protected Thread mWatchdogThread = null;

    protected HRManager hrManager;
    protected HeartRateListener heartRateListener;

    private class HeartRateListener implements HRManager.HRManagerCallback, ConnectionObserver {
        @Override
        public void onHeartRateMeasure(HeartRateChangeEvent event) {
            EventBus.getDefault().post(event);
        }

        @Override
        public void onDeviceConnecting(@NonNull BluetoothDevice device) {
            publishState(GpsRecorderService.HeartRateConnectionState.CONNECTING);
        }

        @Override
        public void onDeviceConnected(@NonNull BluetoothDevice device) {
            publishState(GpsRecorderService.HeartRateConnectionState.CONNECTED);
        }

        @Override
        public void onDeviceFailedToConnect(@NonNull BluetoothDevice device, int reason) {
            publishState(GpsRecorderService.HeartRateConnectionState.CONNECTION_FAILED);
        }

        @Override
        public void onDeviceReady(@NonNull BluetoothDevice device) {
            publishState(GpsRecorderService.HeartRateConnectionState.CONNECTED);
        }

        @Override
        public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
        }

        @Override
        public void onDeviceDisconnected(@NonNull BluetoothDevice device, int reason) {
            publishState(GpsRecorderService.HeartRateConnectionState.DISCONNECTED);
        }

        private void publishState(GpsRecorderService.HeartRateConnectionState state) {
            EventBus.getDefault().postSticky(new HeartRateConnectionChangeEvent(state));
        }
    }

    private class WatchDogRunner implements Runnable {
        boolean running = true;

        @Override
        public void run() {
            List<Interval> lastList = null;
            running = true;
            try {
                while (running) {
                    while (instance.recorder.handleWatchdog() && running) {
                        updateNotification();
                        // UPDATE INTERVAL LIST IF NEEDED
                        List<Interval> intervalList = instance.recorder.getIntervalList();
                        if (lastList != intervalList) {
                            announcements.applyIntervals(intervalList);
                            lastList = intervalList;
                        }

                        // CHECK FOR ANNOUNCEMENTS
                        announcements.check();
                        Thread.sleep(WATCHDOG_INTERVAL);
                    }
                    Thread.sleep(WATCHDOG_INTERVAL); // Additional Retry Interval
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void stop() {
            running = false;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        serviceStartTime = new Date();
        Notification notification = this.getNotification();

        startForeground(NOTIFICATION_ID, notification);

        acquireWakelock();

        return START_STICKY;
    }

    private String getRecordingStateString() {
        switch (instance.recorder.getState()) {
            case IDLE:
                return getString(R.string.recordingStateIdle);
            case RUNNING:
                return getString(R.string.recordingStateRunning);
            case PAUSED:
                return getString(R.string.recordingStatePaused);
            case STOPPED:
                return getString(R.string.recordingStateStopped);
        }
        return "";
    }

    private Notification getNotification() {
        String contentText = getText(R.string.trackerWaitingMessage).toString();
        if (instance.recorder.getState() != GpsWorkoutRecorder.RecordingState.IDLE) {
            contentText = String.format(Locale.getDefault(), "\n%s\n%s: %s",
                    getRecordingStateString(),
                    getText(R.string.workoutDuration),
                    instance.distanceUnitUtils.getHourMinuteSecondTime(instance.recorder.getDuration()));
        }
        if (BuildConfig.DEBUG && serviceStartTime != null) {
            contentText = String.format("%s\n\nServiceCreateTime: %s",
                    contentText,
                    instance.userDateTimeUtils.formatTime(serviceStartTime));
        }
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getText(R.string.trackerRunning))
                .setContentText(contentText)
                .setStyle(new Notification.BigTextStyle().bigText(contentText))
                .setSmallIcon(R.drawable.notification);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationHelper.createChannels(this);
            builder.setChannelId(NotificationHelper.CHANNEL_WORKOUT);
        }

        Intent recorderActivityIntent = new Intent(this, instance.recorder.getActivityClass());
        recorderActivityIntent.setAction(RecordWorkoutActivity.RESUME_ACTION);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, recorderActivityIntent, 0);
        builder.setContentIntent(pendingIntent);

        return builder.build();
    }

    private void updateNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, getNotification());
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        this.instance = Instance.getInstance(getBaseContext());

        if (mSensorManager == null) {
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        }

        initializeHRManager();

        initializeTTS();

        initializeWatchdog();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");

        // Shutdown Watchdog
        mWatchdogRunner.stop();

        // Shutdown TTS
        mTTSController.destroy();

        hrManager.stop();
        heartRateListener.publishState(HeartRateConnectionState.DISCONNECTED);

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        stopForeground(true);
        super.onDestroy();
    }


    private void initializeHRManager() {
        heartRateListener = new HeartRateListener();
        hrManager = new HRManager(this, heartRateListener);
        hrManager.setConnectionObserver(heartRateListener);
        hrManager.start();
    }

    private void initializeTTS() {
        mTTSController = new TTSController(this.getApplicationContext(), TTS_CONTROLLER_ID);
        announcements = new VoiceAnnouncements(this, instance.recorder, mTTSController, new ArrayList<>());
    }

    private void initializeWatchdog() {
        if (mWatchdogThread == null || !mWatchdogThread.isAlive()) {
            mWatchdogRunner = new WatchDogRunner();
            mWatchdogThread = new Thread(mWatchdogRunner, "WorkoutWatchdog");
        }
        if (!mWatchdogThread.isAlive()) {
            mWatchdogThread.start();
        }
    }

    private void acquireWakelock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "de.tadris.fitotrack:workout_recorder");
        wakeLock.acquire(TimeUnit.HOURS.toMillis(4));
    }

    public enum HeartRateConnectionState {
        DISCONNECTED(R.color.heartRateStateUnavailable, R.drawable.ic_bluetooth),
        CONNECTING(R.color.heartRateStateConnecting, R.drawable.ic_bluetooth_connecting),
        CONNECTED(R.color.heartRateStateAvailable, R.drawable.ic_bluetooth_connected),
        CONNECTION_FAILED(R.color.heartRateStateFailed, R.drawable.ic_bluetooth_off);

        @ColorRes
        public final int colorRes;

        @DrawableRes
        public final int iconRes;

        HeartRateConnectionState(int colorRes, int iconRes) {
            this.colorRes = colorRes;
            this.iconRes = iconRes;
        }
    }

}
