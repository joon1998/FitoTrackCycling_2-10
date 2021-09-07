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

package de.tadris.fitness.util;

import android.content.Context;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatDelegate;

import de.tadris.fitness.R;
import de.tadris.fitness.data.WorkoutType;

public class FitoTrackThemes {

    private static final int THEME_SETTING_AUTO = 0;
    private static final int THEME_SETTING_LIGHT = 2;
    private static final int THEME_SETTING_DARK = 1;

    private final Context context;

    public FitoTrackThemes(Context context) {
        this.context = context;
    }

    @StyleRes
    public int getDefaultTheme() {
        return R.style.AppTheme;
    }

    @StyleRes
    public int getWorkoutTypeTheme(WorkoutType type) {
        switch (type.id) {
            case "walking":
            case "running":
            case "treadmill":
                return R.style.Running;
            case "hiking":
                return R.style.Hiking;
            case "cycling":
                return R.style.Bicycling;
            case "skateboarding":
            case "inline_skating":
                return R.style.Skating;
            case "rowing":
                return R.style.Rowing;
            default:
                return R.style.AppTheme;
        }
    }

    public void updateDarkModeSetting() {
        int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        if (getThemeSetting() == THEME_SETTING_LIGHT) {
            mode = AppCompatDelegate.MODE_NIGHT_NO;
        } else if (getThemeSetting() == THEME_SETTING_DARK) {
            mode = AppCompatDelegate.MODE_NIGHT_YES;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public boolean isDarkModeEnabled() {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    private int getThemeSetting() {
        String setting = PreferenceManager.getDefaultSharedPreferences(context).getString("themeSetting", String.valueOf(THEME_SETTING_LIGHT));
        assert setting != null;
        return Integer.parseInt(setting);
    }

}
