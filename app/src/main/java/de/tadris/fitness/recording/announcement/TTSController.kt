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
package de.tadris.fitness.recording.announcement

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.app.ActivityCompat
import de.tadris.fitness.recording.BaseWorkoutRecorder
import de.tadris.fitness.recording.event.TTSReadyEvent
import de.tadris.fitness.util.WorkoutLogger
import org.greenrobot.eventbus.EventBus
import java.util.*

class TTSController(private val context: Context, val id: String = DEFAULT_TTS_CONTROLLER_ID) {

    private val textToSpeech = TextToSpeech(context) { status: Int -> ttsReady(status) }

    var isTtsAvailable = false
        private set

    private val currentMode = AnnouncementMode.getCurrentMode(context)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val audioFocusManager = AudioFocusManager(audioManager)
    private val queuedUtterances = HashSet<String>();

    private fun ttsReady(status: Int) {
        isTtsAvailable =
            status == TextToSpeech.SUCCESS && textToSpeech.setLanguage(Locale.getDefault()) >= 0
        if (isTtsAvailable) {
            textToSpeech.setOnUtteranceProgressListener(TextToSpeechListener())
        }
        EventBus.getDefault().post(TTSReadyEvent(isTtsAvailable, id))
    }

    fun speak(recorder: BaseWorkoutRecorder, announcement: Announcement) {
        if (!announcement.isAnnouncementEnabled) {
            return
        }
        val text = announcement.getSpokenText(recorder)
        if (text != null && text != "") {
            speak(text)
        }
    }

    private var speakId = 1
    fun speak(text: String) {
        if (!isTtsAvailable) {
            // Cannot speak
            return
        }
        if (currentMode == AnnouncementMode.HEADPHONES && !isHeadsetOn) {
            // Not allowed to speak
            return
        }
        if (!audioFocusManager.requestFocus()) {
            return
        }
        WorkoutLogger.log("Recorder", "TTS speaks: $text")

        val utteranceId = "announcement" + ++speakId
        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId)
        queuedUtterances.add(utteranceId)
    }

    private val isHeadsetOn get() = audioManager.isWiredHeadsetOn || bluetoothHeadsetConnected

    private val bluetoothHeadsetConnected
        get(): Boolean {
            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            return if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled
                        && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED)
            } else false
        }

    /**
     * Destroys the TTS instance immediately. Ongoing announcements might be aborted.<br></br>
     * Use [.destroyWhenDone] instead, if you don't want to abort ongoing announcements.
     */
    fun destroy() {
        textToSpeech.shutdown()
    }

    /**
     * Waits for the end of an ongoing announcement before the TTS instance is destroyed.<br></br>
     * Use [.destroy] instead, if you don't care about that.
     */
    fun destroyWhenDone() {
        val destroyTimer = Timer("TTS_Destroy")
        destroyTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (!textToSpeech.isSpeaking) {
                    destroy()
                    cancel()
                    destroyTimer.cancel()
                }
            }
        }, 20, 20)
    }

    private inner class TextToSpeechListener : UtteranceProgressListener() {
        override fun onStart(utteranceId: String) {}

        override fun onDone(utteranceId: String) {
            queuedUtterances.remove(utteranceId);

            if (queuedUtterances.isEmpty()) {
                audioFocusManager.abandonFocus()
            }
        }

        override fun onError(utteranceId: String) {}
    }

    companion object {
        const val DEFAULT_TTS_CONTROLLER_ID = "TTSController"
    }
}