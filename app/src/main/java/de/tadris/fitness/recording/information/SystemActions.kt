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
package de.tadris.fitness.recording.information

import android.content.Context
import de.tadris.fitness.R
import de.tadris.fitness.recording.BaseWorkoutRecorder

class SystemActions(context: Context) : RecordingInformation(context) {

    private var lastState: BaseWorkoutRecorder.RecordingState =
        BaseWorkoutRecorder.RecordingState.IDLE

    override val id = "system_actions"
    override val isEnabledByDefault = true
    override fun canBeDisplayed() = false
    override fun getSpokenText(recorder: BaseWorkoutRecorder): String {
        var text = ""
        if (lastState != recorder.state) {
            text = speakState(recorder.state)
        }
        lastState = recorder.state
        return text
    }

    private fun speakState(newState: BaseWorkoutRecorder.RecordingState): String {
        if (lastState == BaseWorkoutRecorder.RecordingState.IDLE) {
            return getString(R.string.workoutStarted)
        }
        return when (newState) {
            BaseWorkoutRecorder.RecordingState.RUNNING -> getString(R.string.workoutResumed)
            BaseWorkoutRecorder.RecordingState.PAUSED -> getString(R.string.workoutPaused)
            BaseWorkoutRecorder.RecordingState.STOPPED -> getString(R.string.workoutStopped)
            else -> ""
        }
    }

    override fun getDisplayedText(recorder: BaseWorkoutRecorder): String {
        return ""
    }

    override fun isPlayedAlways(): Boolean {
        return true
    }

    override fun getTitle(): String {
        return ""
    }
}