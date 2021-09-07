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

import android.content.Context;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.data.BaseWorkout;
import de.tadris.fitness.data.Interval;
import de.tadris.fitness.data.IntervalSet;
import de.tadris.fitness.data.UserPreferences;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.recording.event.HeartRateChangeEvent;
import de.tadris.fitness.recording.event.HeartRateConnectionChangeEvent;
import de.tadris.fitness.recording.event.WorkoutAutoStopEvent;
import de.tadris.fitness.recording.gps.GpsRecorderService;
import de.tadris.fitness.recording.gps.GpsWorkoutRecorder;
import de.tadris.fitness.ui.record.RecordWorkoutActivity;

public abstract class BaseWorkoutRecorder {

    protected static final int PAUSE_TIME = 10_000; // 10 Seconds
    private static final int AUTO_TIMEOUT_MULTIPLIER = 1_000 * 60; // minutes to ms


    protected final Context context;
    protected boolean useAutoPause;
    protected long autoTimeoutMs;

    protected GpsWorkoutRecorder.RecordingState state;
    protected long startTime = 0;
    protected long time = 0;
    protected long pauseTime = 0;
    protected long lastResume;
    protected long lastPause = 0;
    protected long lastSampleTime = 0;

    protected List<Interval> intervalList;

    protected int lastHeartRate = -1;

    // Temporarily saved the last interval that was triggered.
    // It will be added to the next recorded sample.
    protected long lastTriggeredInterval = -1;

    public BaseWorkoutRecorder(Context context) {
        this.context = context;
        this.state = RecordingState.IDLE;

        EventBus.getDefault().register(this);
        UserPreferences prefs = Instance.getInstance(context).userPreferences;
        this.useAutoPause = prefs.getUseAutoPause();
        this.autoTimeoutMs = prefs.getAutoTimeout() * AUTO_TIMEOUT_MULTIPLIER;
    }

    public void start() {
        if (state == RecordingState.IDLE) {
            Log.i("Recorder", "Start");
            startTime = System.currentTimeMillis();
            onStart();
            resume();
        } else if (state == RecordingState.PAUSED) {
            resume();
        } else if (state != RecordingState.RUNNING) {
            throw new IllegalStateException("Cannot start or resume recording. state = " + state);
        }
    }

    /**
     * Handles the Record Watchdog, for GPS Check, Pause Detection and Auto Timeout
     *
     * @return is still active workout
     */
    public boolean handleWatchdog() {
        if (isActive()) {
            onWatchdog();
            if (hasRecordedSomething()) {
                long timeDiff = System.currentTimeMillis() - lastSampleTime;
                if (autoTimeoutMs > 0 && timeDiff > autoTimeoutMs) {
                    if (isActive()) {
                        stop("Auto timeout, timediff: " + timeDiff);
                        save();
                        EventBus.getDefault().post(new WorkoutAutoStopEvent());
                    }
                } else if (useAutoPause) {
                    if (timeDiff > PAUSE_TIME) {
                        if (state == RecordingState.RUNNING && autoPausePossible()) {
                            pause();
                        }
                    } else {
                        if (state == RecordingState.PAUSED) {
                            resume();
                        }
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public void resume() {
        Log.i("Recorder", "Resume");
        state = RecordingState.RUNNING;
        lastResume = System.currentTimeMillis();
        if (lastPause != 0) {
            pauseTime += System.currentTimeMillis() - lastPause;
        }
    }

    public void pause() {
        if (state == RecordingState.RUNNING) {
            Log.i("Recorder", "Pause");
            state = RecordingState.PAUSED;
            time += System.currentTimeMillis() - lastResume;
            lastPause = System.currentTimeMillis();
        }
    }

    public void stop(String reason) {
        Log.i("Recorder", "Stopping workout, reason: " + reason);
        if (state == RecordingState.PAUSED) {
            resume();
        }
        pause();
        onStop();
        state = RecordingState.STOPPED;
        EventBus.getDefault().unregister(this);
    }

    public abstract boolean hasRecordedSomething();

    protected abstract void onWatchdog();

    protected abstract boolean autoPausePossible();

    protected abstract void onStart();

    protected abstract void onStop();

    public abstract void save();

    public abstract boolean isSaved();

    public void setComment(String comment) {
        getWorkout().comment = comment;
    }

    public abstract void discard();

    public abstract BaseWorkout getWorkout();

    public abstract int getCalories();

    public void setUsedIntervalSet(IntervalSet set) {
        getWorkout().intervalSetUsedId = set.id;
    }

    public abstract Class<? extends RecordWorkoutActivity> getActivityClass();

    public abstract WorkoutType.RecordingType getRecordingType();

    public void onIntervalWasTriggered(Interval interval) {
        lastTriggeredInterval = interval.id;
    }

    public long getTimeSinceStart() {
        if (startTime != 0) {
            return System.currentTimeMillis() - startTime;
        } else {
            return 0;
        }
    }

    public long getPauseDuration() {
        if (state == RecordingState.PAUSED) {
            return pauseTime + (System.currentTimeMillis() - lastPause);
        } else {
            return pauseTime;
        }
    }

    public long getDuration() {
        if (state == RecordingState.RUNNING) {
            return time + (System.currentTimeMillis() - lastResume);
        } else {
            return time;
        }
    }

    @Subscribe
    public void onHeartRateChange(HeartRateChangeEvent event) {
        lastHeartRate = event.heartRate;
    }

    @Subscribe
    public void onHeartRateConnectionChange(HeartRateConnectionChangeEvent event) {
        if (event.state != GpsRecorderService.HeartRateConnectionState.CONNECTED) {
            // If heart rate sensor currently not available
            lastHeartRate = -1;
        }
    }

    public RecordingState getState() {
        return state;
    }

    public void setIntervalList(List<Interval> intervalList) {
        this.intervalList = intervalList;
    }

    public List<Interval> getIntervalList() {
        return intervalList;
    }

    public int getCurrentHeartRate() {
        return lastHeartRate;
    }

    public boolean isAutoPauseEnabled() {
        return useAutoPause;
    }

    public boolean isActive() {
        return state == GpsWorkoutRecorder.RecordingState.IDLE || state == GpsWorkoutRecorder.RecordingState.RUNNING || state == GpsWorkoutRecorder.RecordingState.PAUSED;
    }

    public boolean isPausedOrResumed() {
        return isPaused() || isResumed();
    }

    public boolean isResumed() {
        return state == GpsWorkoutRecorder.RecordingState.RUNNING;
    }

    public boolean isPaused() {
        return state == RecordingState.PAUSED;
    }

    public enum RecordingState {
        IDLE, RUNNING, PAUSED, STOPPED
    }

}
