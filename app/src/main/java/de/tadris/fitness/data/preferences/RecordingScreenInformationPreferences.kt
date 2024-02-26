package de.tadris.fitness.data.preferences

import android.content.Context
import android.content.SharedPreferences
import de.tadris.fitness.data.RecordingType
import de.tadris.fitness.data.WorkoutType
import java.lang.Exception

/**
 * Preferences within this class concern the Metrics (Information) shown on the Recording
 * Activities
 */
class RecordingScreenInformationPreferences(private val prefs: SharedPreferences) {

    @Throws(UnknownModeException::class)
    fun getIdOfDisplayedInformation(mode: String, slot: Int) : String {
        val defaultValue = when(mode) {
            RecordingType.INDOOR.id -> this.getDefaultIndoorActivityId(slot)
            RecordingType.GPS.id -> this.getDefaultGpsActivityId(slot)
            else -> throw UnknownModeException()
        }

        val preferenceKey = "information_display_${mode}_${slot}"
        return this.prefs.getString(preferenceKey, defaultValue)!!
    }

    fun setIdOfDisplayedInformation(mode: String, slot: Int, value: String) {
        val preferenceKey = "information_display_${mode}_${slot}"
        this.prefs.edit().putString(preferenceKey, value).apply()
    }

    // TODO: At some point, replace strings with an Enum
    private fun getDefaultIndoorActivityId(slot: Int) : String {
        return when(slot) {
            0 -> "avg_frequency"
            1 -> "energy_burned"
            2 -> "current_intensity"
            3 -> "pause_duration"
            // Should never happen, but even if it would, there is no point in throwing an exception
            // over this
            else -> "pause_duration"
        }
    }

    // TODO: At some point, replace strings with an Enum
    private fun getDefaultGpsActivityId(slot: Int) : String {
        return when(slot) {
            0 -> "distance"
            1 -> "energy_burned"
            2 -> "avgSpeedMotion"
            3 -> "pause_duration"
            // Should never happen, but even if it would, there is no point in throwing an exception
            // over this
            else -> "pause_duration"
        }
    }

    class UnknownModeException : Exception()

}