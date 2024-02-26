/*
 * Copyright (c) 2022 Jannis Scheibe <jannis@tadris.de>
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

package de.tadris.fitness.recording.indoor;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.data.BaseWorkout;
import de.tadris.fitness.data.IndoorSample;
import de.tadris.fitness.data.IndoorWorkout;
import de.tadris.fitness.data.IndoorWorkoutData;
import de.tadris.fitness.data.RecordingType;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.data.preferences.UserMeasurements;
import de.tadris.fitness.recording.BaseWorkoutRecorder;
import de.tadris.fitness.recording.indoor.exercise.ExerciseRecognizer;
import de.tadris.fitness.ui.record.RecordIndoorWorkoutActivity;
import de.tadris.fitness.ui.record.RecordWorkoutActivity;
import de.tadris.fitness.util.WorkoutLogger;
import de.tadris.fitness.util.calorie.CalorieCalculator;

public class IndoorWorkoutRecorder extends BaseWorkoutRecorder {

    WorkoutType type;
    IndoorWorkout workout;
    IndoorSample lastCompletedSample;
    IndoorSample currentSample;
    final List<IndoorSample> samples = new ArrayList<>();
    private boolean saved = false;

    private int repetitions = 0;

    public IndoorWorkoutRecorder(Context context, WorkoutType workoutType) {
        super(context);
        this.type = workoutType;
        this.workout = new IndoorWorkout();
        this.workout.edited = false;
        this.workout.setWorkoutType(workoutType);
    }

    @Override
    public int getSampleSize() {
        return samples.size();
    }

    @Override
    protected void onWatchdog() {

    }

    @Override
    protected boolean autoPausePossible() {
        return true;
    }

    @Override
    protected void onStart() {
        workout.id = System.nanoTime();
        workout.start = System.currentTimeMillis();
        //Init Workout To Be able to Save
        workout.end = -1;
    }

    @Override
    protected void onStop() {
        workout.end = System.currentTimeMillis();
        workout.duration = time;
        workout.pauseDuration = pauseTime;
    }

    @Subscribe
    public void onRepetitionRecognized(ExerciseRecognizer.RepetitionRecognizedEvent event) {
        lastSampleTime = System.currentTimeMillis();

        boolean acceptSamples = useAutoPause ? isPausedOrResumed() : isResumed();
        if (acceptSamples && event.getTimestamp() > workout.start) {
            WorkoutLogger.log("Recorder", "repetition recognized with intensity " + event.getIntensity());
            if (currentSample != null && currentSample.repetitions < type.minDistance && event.getTimestamp() - currentSample.absoluteTime < PAUSE_TIME) {
                addToExistingSample(event);
            } else {
                addNewSample(event);
            }
            repetitions++;
        }
    }

    private void addToExistingSample(ExerciseRecognizer.RepetitionRecognizedEvent event) {
        currentSample.intensity = (currentSample.repetitions * currentSample.intensity + event.getIntensity()) / (currentSample.repetitions + 1);
        currentSample.absoluteEndTime = event.getTimestamp();
        currentSample.repetitions++;
    }

    private void addNewSample(ExerciseRecognizer.RepetitionRecognizedEvent event) {
        if (currentSample != null) {
            // lastSample will not be changed further so we broadcast it
            EventBus.getDefault().post(currentSample);
            lastCompletedSample = currentSample;
        }

        IndoorSample sample = new IndoorSample();
        sample.absoluteTime = event.getTimestamp();
        sample.absoluteEndTime = event.getTimestamp();
        sample.repetitions = 1;
        sample.relativeTime = event.getTimestamp() - startTime - getPauseDuration();
        sample.intensity = event.getIntensity();
        sample.heartRate = lastHeartRate;
        sample.intervalTriggered = lastTriggeredInterval;
        lastTriggeredInterval = -1;
        samples.add(sample);
        currentSample = sample;
    }

    public int getRepetitionsTotal() {
        return repetitions;
    }

    public double getAverageFrequency() {
        return (double) repetitions / Math.max(1, (double) getDuration() / 1000);
    }

    public double getCurrentFrequency() {
        if (lastCompletedSample != null && currentSample != null) {
            int repetitions = lastCompletedSample.repetitions + currentSample.repetitions;
            long time = currentSample.absoluteEndTime - lastCompletedSample.absoluteTime;
            return repetitions / ((double) time / 1000);
        } else {
            return 0;
        }
    }

    public double getCurrentIntensity() {
        if (currentSample != null) {
            return currentSample.intensity;
        } else {
            return 0;
        }
    }

    private IndoorWorkoutData getWorkoutData() {
        return new IndoorWorkoutData(workout, samples);
    }

    @Override
    public void save() {
        new IndoorWorkoutSaver(context, getWorkoutData()).save();
        saved = true;
    }

    @Override
    public boolean isSaved() {
        return saved;
    }

    @Override
    public void discard() {
    }

    @Override
    public BaseWorkout getWorkout() {
        return workout;
    }

    @Override
    public int getCalories() {
        workout.duration = getDuration();
        return new CalorieCalculator(context).calculateCalories(UserMeasurements.from(context), workout);
    }

    public List<IndoorSample> getSamples() {
        return new ArrayList<>(samples);
    }

    @Override
    public Class<? extends RecordWorkoutActivity> getActivityClass() {
        return RecordIndoorWorkoutActivity.class;
    }

    @Override
    public RecordingType getRecordingType() {
        return RecordingType.INDOOR;
    }
}
