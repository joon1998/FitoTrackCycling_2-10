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

package de.tadris.fitness.ui.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Switch;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.UserPreferences;
import de.tadris.fitness.model.AutoStartWorkout;
import de.tadris.fitness.recording.announcement.TTSController;
import de.tadris.fitness.recording.event.TTSReadyEvent;
import de.tadris.fitness.ui.dialog.ChooseAutoStartDelayDialog;
import de.tadris.fitness.ui.dialog.ChooseAutoStartModeDialog;
import de.tadris.fitness.ui.dialog.ChooseAutoTimeoutDialog;
import de.tadris.fitness.util.NfcAdapterHelper;
import de.tadris.fitness.util.NumberPickerUtils;

public class RecordingSettingsFragment
        extends FitoTrackSettingFragment
        implements ChooseAutoStartModeDialog.AutoStartModeSelectListener,
        ChooseAutoStartDelayDialog.AutoStartDelaySelectListener,
        ChooseAutoTimeoutDialog.AutoTimeoutSelectListener {

    Instance instance;
    UserPreferences preferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        instance = Instance.getInstance(getContext());
        preferences = Instance.getInstance(requireContext()).userPreferences;

        addPreferencesFromResource(R.xml.preferences_recording);

        // modify NFC option
        if (!NfcAdapterHelper.isNfcPresent(requireContext())) { // disable the NFC option if the device doesn't support NFC
            findPreference("nfcStart").setEnabled(false);
        } else {
            // ask the user to enable NFC in device settings when they want to use it in the app
            // but NFC is globally disabled
            findPreference("nfcStart").setOnPreferenceChangeListener((pref, newValue) -> {
                if ((Boolean) newValue && !NfcAdapterHelper.isNfcEnabled(requireContext())) {
                    NfcAdapterHelper.createNfcEnableDialog(requireContext()).show();
                    return false;   // do NOT use NFC yet, user first needs to enable it in device settings
                }
                return true;
            });
        }

        disableSpeechConfig();
        checkTTS(this::showSpeechConfig);

        findPreference("intervals").setOnPreferenceClickListener(preference -> {
            showIntervalSetManagement();
            return true;
        });

        findPreference("autoStartModeConfig").setOnPreferenceClickListener(preference -> {
            showAutoStartModeConfig();
            return true;
        });

        findPreference("autoStartDelayConfig").setOnPreferenceClickListener(preference -> {
            showAutoStartDelayConfig();
            return true;
        });

        findPreference("autoTimeoutConfig").setOnPreferenceClickListener(preference -> {
            showAutoTimeoutConfig();
            return true;
        });

        findPreference("currentSpeedAverageTimeConfig").setOnPreferenceClickListener(preference -> {
            showCurrentSpeedAverageTimePicker();
            return true;
        });
    }


    private TTSController TTSController;

    private void checkTTS(Runnable onTTSAvailable) {
        TTSController = new TTSController(requireContext());
        EventBus.getDefault().register(new Object() {
            @Subscribe(threadMode = ThreadMode.MAIN)
            public void onTTSReady(TTSReadyEvent e) {
                if (getContext() != null) {
                    if (e.ttsAvailable) {
                        onTTSAvailable.run();
                    }
                    if (TTSController != null) {
                        TTSController.destroy();
                    }
                    EventBus.getDefault().unregister(this);
                }
            }
        });
    }

    private void showSpeechConfig() {
        findPreference("speech").setEnabled(true);
        findPreference("intervals").setEnabled(true);
        findPreference("speech").setSummary(R.string.pref_voice_announcements_summary);
        findPreference("intervals").setSummary(R.string.manageIntervalsSummary);
    }

    private void disableSpeechConfig() {
        findPreference("speech").setEnabled(false);
        findPreference("intervals").setEnabled(false);
        findPreference("speech").setSummary(R.string.ttsNotAvailable);
        findPreference("intervals").setSummary(R.string.ttsNotAvailable);
    }

    private void showIntervalSetManagement() {
        startActivity(new Intent(requireContext(), ManageIntervalSetsActivity.class));
    }

    private void showAutoStartModeConfig() {
        new ChooseAutoStartModeDialog(requireActivity(), this).show();
    }

    private void showAutoStartDelayConfig() {
        int initialDelayS = instance.userPreferences.getAutoStartDelay();
        new ChooseAutoStartDelayDialog(requireActivity(), this,
                (long) initialDelayS * 1_000).show();
    }

    private void showCurrentSpeedAverageTimePicker() {
        final AlertDialog.Builder d = new AlertDialog.Builder(requireActivity());
        final float disabledAlpha = 0.3f;

        d.setTitle(getString(R.string.preferenceCurrentSpeedTime));
        View v = getLayoutInflater().inflate(R.layout.dialog_current_speed, null);
        Switch sw = v.findViewById(R.id.useAverageForCurrentSpeed);

        // number picker: 0-120 seconds, only enabled when sw is checked, transparent if disabled
        NumberPicker np = v.findViewById(R.id.currentSpeedAverageTime);
        sw.setChecked(preferences.getUseAverageForCurrentSpeed());
        np.setEnabled(sw.isChecked());
        np.setAlpha(sw.isChecked() ? 1f : disabledAlpha);
        np.setMaxValue(120);
        np.setMinValue(0);
        np.setFormatter(value -> getResources().getQuantityString(R.plurals.seconds, value, value));
        np.setValue(preferences.getTimeForCurrentSpeed());
        np.setWrapSelectorWheel(false);
        NumberPickerUtils.fixNumberPicker(np);

        sw.setOnCheckedChangeListener((view, isChecked) -> {
            np.setEnabled(isChecked);
            np.setAlpha(isChecked ? 1f : disabledAlpha);
        });

        d.setView(v);

        d.setNegativeButton(R.string.cancel, null);
        d.setPositiveButton(R.string.okay, (dialog, which) -> {
            preferences.setUseAverageForCurrentSpeed(sw.isChecked());
            if (sw.isChecked()) {
                preferences.setTimeForCurrentSpeed(np.getValue());
            }
        });

        d.create().show();
    }

    private void showAutoTimeoutConfig() {
        new ChooseAutoTimeoutDialog(requireActivity(), this).show();
    }

    @Override
    public void onSelectAutoStartMode(AutoStartWorkout.Mode mode) {
        Instance.getInstance(getContext()).userPreferences.setAutoStartMode(mode);
    }

    @Override
    public void onSelectAutoStartDelay(int delayS) {
        Instance.getInstance(getContext()).userPreferences.setAutoStartDelay(delayS);
    }

    @Override
    public void onSelectAutoTimeout(int timeoutM) {
        Instance.getInstance(getContext()).userPreferences.setAutoTimeout(timeoutM);
    }
}
