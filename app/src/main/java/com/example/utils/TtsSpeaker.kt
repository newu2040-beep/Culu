package com.example.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

object TtsSpeaker {
    private const val TAG = "TtsSpeaker"
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingTextToSpeak: String? = null

    fun initialize(context: Context, onReady: (() -> Unit)? = null) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isInitialized = true
                    try {
                        val result = tts?.setLanguage(Locale.getDefault())
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            tts?.language = Locale.US
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed setting language", e)
                        tts?.language = Locale.US
                    }

                    // Configure audio attributes for high volume clarity
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                    tts?.setAudioAttributes(audioAttributes)
                    tts?.setPitch(1.05f)
                    tts?.setSpeechRate(0.95f) // Clear, audible cadence

                    pendingTextToSpeak?.let { text ->
                        speakLoud(context, text)
                        pendingTextToSpeak = null
                    }
                    onReady?.invoke()
                } else {
                    Log.e(TAG, "TTS Initialization failed with status: $status")
                }
            }
        } else if (isInitialized) {
            onReady?.invoke()
        }
    }

    fun speakLoud(context: Context, text: String) {
        try {
            // Boost audio volume so user can hear loud and clear
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.let { am ->
                val maxAlarm = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                val currentAlarm = am.getStreamVolume(AudioManager.STREAM_ALARM)
                if (currentAlarm < (maxAlarm * 0.7f).toInt()) {
                    am.setStreamVolume(AudioManager.STREAM_ALARM, (maxAlarm * 0.85f).toInt(), 0)
                }

                val maxMusic = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val currentMusic = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                if (currentMusic < (maxMusic * 0.7f).toInt()) {
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, (maxMusic * 0.85f).toInt(), 0)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not adjust stream volume", e)
        }

        if (!isInitialized || tts == null) {
            pendingTextToSpeak = text
            initialize(context)
            return
        }

        try {
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f) // Maximum gain
                putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_ALARM)
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "CULU_ALERT_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed speaking text", e)
        }
    }

    fun speakReminder(context: Context, title: String, dosage: String) {
        val message = buildString {
            append("Attention! CULU Reminder. Time for ")
            append(title)
            if (dosage.isNotBlank()) {
                append(". ")
                append(dosage)
            }
            append(". Please take care of your wellness now.")
        }
        speakLoud(context, message)
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
    }
}
