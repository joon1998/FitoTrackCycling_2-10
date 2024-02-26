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

package de.tadris.fitness.ui.workout;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.preferences.UserMeasurements;
import de.tadris.fitness.ui.workout.diagram.FrequencyConverter;
import de.tadris.fitness.ui.workout.diagram.HeartRateConverter;
import de.tadris.fitness.ui.workout.diagram.IntensityConverter;
import de.tadris.fitness.util.DialogUtils;
import de.tadris.fitness.util.WorkoutCalculator;
import de.tadris.fitness.util.unit.UnitUtils;

public class ShowIndoorWorkoutActivity extends IndoorWorkoutActivity implements DialogUtils.WorkoutDeleter {

    TextView commentView;
    List<WorkoutCalculator.Set> sets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initBeforeContent();

        setContentView(R.layout.activity_show_workout);
        initRoot();

        initAfterContent();

        sets = WorkoutCalculator.getSetsFromWorkout(getIndoorWorkoutData());

        commentView = addText("", true);
        commentView.setOnClickListener(v -> openEditCommentDialog());
        updateCommentText();

        addTitle(getString(R.string.workoutTime));
        addKeyValue(getString(R.string.workoutDate), getDate());
        addKeyValue(getString(R.string.workoutDuration), distanceUnitUtils.getHourMinuteSecondTime(workout.duration),
                getString(R.string.workoutPauseDuration), distanceUnitUtils.getHourMinuteSecondTime(workout.pauseDuration));
        addKeyValue(getString(R.string.workoutStartTime), Instance.getInstance(this).userDateTimeUtils.formatTime(new Date(workout.start)),
                getString(R.string.workoutEndTime), Instance.getInstance(this).userDateTimeUtils.formatTime(new Date(workout.end)));

        addKeyValue(getString(R.string.workoutRepetitions), String.valueOf(workout.repetitions), getString(R.string.workoutSets), String.valueOf(sets.size()));

        if (workout.hasEstimatedDistance()) {
            int estimatedDistance = (int) Math.round(workout.estimateDistance(UserMeasurements.from(this)));
            double estimatedSpeed = workout.estimateSpeed(UserMeasurements.from(this));
            addKeyValue(getString(R.string.approxSymbol) + getString(R.string.workoutDistance), distanceUnitUtils.getDistance(estimatedDistance),
                    getString(R.string.approxSymbol) + getString(R.string.workoutSpeed), distanceUnitUtils.getSpeed(estimatedSpeed));
        }

        addTitle(getString(R.string.workoutFrequency));

        if (hasSamples()) {
            addKeyValue(getString(R.string.workoutAvgFrequencyShort), UnitUtils.round(workout.avgFrequency, 2) + " " + UnitUtils.unitHertzShort,
                    getString(R.string.workoutMaxFrequency), UnitUtils.round(workout.maxFrequency, 2) + " " + UnitUtils.unitHertzShort);

            addDiagram(new FrequencyConverter(this));
        } else {
            addKeyValue(getString(R.string.workoutAvgFrequencyShort), UnitUtils.round(workout.avgFrequency, 2) + " " + UnitUtils.unitHertzShort);
        }

        if (workout.hasHeartRateData()) {
            addTitle(getString(R.string.workoutHeartRate));
            addKeyValue(getString(R.string.workoutAvgHeartRate), workout.avgHeartRate + " " + getString(R.string.unitHeartBeatsPerMinute),
                    getString(R.string.workoutMaxHeartRate), workout.maxHeartRate + " " + getString(R.string.unitHeartBeatsPerMinute));

            addDiagram(new HeartRateConverter(this));
        }

        addTitle(getString(R.string.workoutBurnedEnergy));
        addKeyValue(getString(R.string.workoutTotalEnergy), energyUnitUtils.getEnergy(workout.calorie),
                getString(R.string.workoutEnergyConsumption), energyUnitUtils.getRelativeEnergy((double) workout.calorie / ((double) workout.duration / 1000 / 60)));

        if (hasSamples() && workout.hasIntensityValues()) {
            addTitle(getString(R.string.workoutIntensity));

            addKeyValue(getString(R.string.workoutAvgIntensityShort), UnitUtils.round(workout.avgIntensity, 2),
                    getString(R.string.workoutMaxIntensity), UnitUtils.round(workout.maxIntensity, 2));

            addDiagram(new IntensityConverter(this));
        }
    }

    private void openEditCommentDialog() {
        final EditText editText = new EditText(this);
        editText.setText(workout.comment);
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        requestKeyboard(editText);
        new AlertDialog.Builder(this)
                .setTitle(R.string.enterComment)
                .setPositiveButton(R.string.okay, (dialog, which) -> changeComment(editText.getText().toString()))
                .setView(editText).create().show();
    }

    private void changeComment(String comment) {
        workout.comment = comment;
        Instance.getInstance(this).db.indoorWorkoutDao().updateWorkout(workout);
        updateCommentText();
    }

    private void updateCommentText() {
        String str = "";
        if (workout.edited) {
            str += getString(R.string.workoutEdited);
        }
        if (workout.comment != null && workout.comment.length() > 0) {
            if (str.length() > 0) {
                str += "\n";
            }
            str += getString(R.string.comment) + ": " + workout.comment;
        }
        if (str.length() == 0) {
            str = getString(R.string.noComment);
        }
        commentView.setText(str);
    }

    private String getDate() {
        return Instance.getInstance(this).userDateTimeUtils.formatDate(new Date(workout.start));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.show_indoor_workout_menu, menu);
        return true;
    }

    public void deleteWorkout() {
        Instance.getInstance(this).db.indoorWorkoutDao().deleteWorkout(workout);
        finish();
    }

    private void showDeleteDialog() {
        DialogUtils.showDeleteWorkoutDialog(this, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.actionDeleteWorkout) {
            showDeleteDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initRoot() {
        root = findViewById(R.id.showWorkoutRoot);
    }
}
