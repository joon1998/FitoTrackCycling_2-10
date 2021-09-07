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
package de.tadris.fitness.recording.indoor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import de.tadris.fitness.Instance
import de.tadris.fitness.recording.BaseRecorderService
import de.tadris.fitness.recording.indoor.exercise.ExerciseRecognizer
import org.greenrobot.eventbus.EventBus

class IndoorRecorderService : BaseRecorderService(), SensorEventListener {

    private var exerciseRecognizer: ExerciseRecognizer? = null

    override fun onCreate() {
        super.onCreate()
        exerciseRecognizer = ExerciseRecognizer.findByType(Instance.getInstance(this).recorder.workout.workoutTypeId)
        if (exerciseRecognizer != null) {
            Log.d("RecoderService", "Using ${exerciseRecognizer!!.javaClass.simpleName} recognizer")
            exerciseRecognizer!!.start()
            exerciseRecognizer!!.getActivatedSensors().forEach {
                activateSensor(it)
            }
        } else {
            Log.w("RecoderService", "No recognizer found")
        }
    }

    private fun activateSensor(sensorOption: FitoTrackSensorOption) {
        val sensor = mSensorManager?.getDefaultSensor(sensorOption.sensorType)
        if (sensor != null) {
            Log.d("RecoderService", "Activating sensor ${sensorOption.name}")
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSensorManager?.unregisterListener(this)
        exerciseRecognizer?.stop()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        Log.v(
            "RecoderService",
            "Sensorevent ${event.sensor.name} - ${event.values.contentToString()}"
        )
        EventBus.getDefault().post(event)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}