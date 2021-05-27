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

package de.tadris.fitness.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.BuildConfig;
import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.UserPreferences;
import de.tadris.fitness.data.migration.Migration;
import de.tadris.fitness.data.migration.Migration12IntervalSets;
import de.tadris.fitness.map.MapManager;
import de.tadris.fitness.recording.WorkoutRecorder;
import de.tadris.fitness.ui.dialog.ProgressDialogController;
import de.tadris.fitness.ui.record.RecordWorkoutActivity;

public class LauncherActivity extends Activity implements Migration.MigrationListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeNoActionbar);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler().postDelayed(this::init, 100);
    }

    private void init() {
        try {
            Instance.getInstance(this); // Initially load instance class
            Instance.getInstance(this).themes.updateDarkModeSetting();
            if (Instance.getInstance(this).userPreferences.getLastVersionCode() < BuildConfig.VERSION_CODE) {
                runMigrations();
            } else {
                Instance.getInstance(this).userPreferences.updateLastVersionCode();
                MapManager.initMapProvider(this);
                start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new AlertDialog.Builder(this)
                    .setTitle(R.string.launchError)
                    .setMessage(e.getMessage())
                    .setPositiveButton(R.string.okay, null)
                    .setOnDismissListener(dialog -> finish())
                    .show();
        }
    }

    ProgressDialogController progressDialog;

    private void runMigrations() {
        UserPreferences preferences = Instance.getInstance(this).userPreferences;
        List<Migration> migrations = new ArrayList<>();
        if (preferences.getLastVersionCode() < 1200) {
            migrations.add(new Migration12IntervalSets(this, this));
        }
        progressDialog = new ProgressDialogController(this, getString(R.string.runningMigrations));
        progressDialog.show();
        new Thread(() -> {
            for (Migration migration : migrations) {
                migration.migrate();
            }
            preferences.updateLastVersionCode();
            runOnUiThread(() -> {
                progressDialog.cancel();
                init();
            });
        }).start();
    }

    @Override
    public void onProgressUpdate(int progress) {
        runOnUiThread(() -> progressDialog.setProgress(progress));
    }

    private void start() {
        WorkoutRecorder recorder = Instance.getInstance(this).recorder;
        if (recorder.getState() == WorkoutRecorder.RecordingState.PAUSED ||
                recorder.getState() == WorkoutRecorder.RecordingState.RUNNING) {
            // Resume to running Workout
            Intent recorderActivityIntent = new Intent(this, RecordWorkoutActivity.class);
            recorderActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            recorderActivityIntent.setAction(RecordWorkoutActivity.RESUME_ACTION);
            startActivity(recorderActivityIntent);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.stay);
        } else {
            // Go to Workout List
            Intent listWorkoutActivityIntent = new Intent(this, ListWorkoutsActivity.class);
            listWorkoutActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(listWorkoutActivityIntent);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.stay);
        }
    }
}
