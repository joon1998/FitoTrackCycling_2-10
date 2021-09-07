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

package de.tadris.fitness.recording.information;

import android.content.Context;

import de.tadris.fitness.Instance;
import de.tadris.fitness.data.UserPreferences;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.recording.BaseWorkoutRecorder;

public class InformationDisplay {

    private final WorkoutType.RecordingType mode;
    private final UserPreferences preferences;
    private final InformationManager manager;

    public InformationDisplay(WorkoutType.RecordingType mode, Context context) {
        this.mode = mode;
        this.preferences = Instance.getInstance(context).userPreferences;
        this.manager = new InformationManager(mode, context);
    }

    public DisplaySlot getDisplaySlot(BaseWorkoutRecorder recorder, int slot) {
        String informationId = preferences.getIdOfDisplayedInformation(mode.id, slot);
        RecordingInformation information = manager.getInformationById(informationId);
        if (information != null) {
            return new DisplaySlot(slot, information.getTitle(), information.getDisplayedText(recorder));
        } else {
            return new DisplaySlot(slot, "", "");
        }
    }

    public static class DisplaySlot {

        private int slot;
        private String title;
        private String value;

        public DisplaySlot(int slot, String title, String value) {
            this.slot = slot;
            this.title = title;
            this.value = value;
        }

        public int getSlot() {
            return slot;
        }

        public String getTitle() {
            return title;
        }

        public String getValue() {
            return value;
        }
    }

}
