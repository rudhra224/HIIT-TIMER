package com.example.service

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class AudioHapticManager(private val context: Context) {

    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
    } catch (e: Exception) {
        null
    }

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun playCountdownBeep(audioEnabled: Boolean = true, hapticsEnabled: Boolean = true) {
        if (audioEnabled) {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        }
        if (hapticsEnabled) {
            vibrate(50)
        }
    }

    fun playWorkStartSound(audioEnabled: Boolean = true, hapticsEnabled: Boolean = true) {
        if (audioEnabled) {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 400)
        }
        if (hapticsEnabled) {
            vibrate(200)
        }
    }

    fun playRestStartSound(audioEnabled: Boolean = true, hapticsEnabled: Boolean = true) {
        if (audioEnabled) {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 300)
        }
        if (hapticsEnabled) {
            vibrate(120)
        }
    }

    fun playWorkoutFinishSound(audioEnabled: Boolean = true, hapticsEnabled: Boolean = true) {
        if (audioEnabled) {
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 800)
        }
        if (hapticsEnabled) {
            vibratePattern(longArrayOf(0, 100, 100, 200, 100, 300))
        }
    }

    private fun vibrate(durationMs: Long) {
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(durationMs)
            }
        }
    }

    private fun vibratePattern(pattern: LongArray) {
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(pattern, -1)
            }
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
