package com.example.data.service

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class SoundAlertManager(private val context: Context) {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        } catch (e: Exception) {
            Log.e("SoundAlert", "Failed to initialize ToneGenerator", e)
        }
    }

    fun playSniperAlertTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 250)
            triggerHaptic()
        } catch (e: Exception) {
            Log.e("SoundAlert", "Failed to play tone", e)
        }
    }

    fun playSweepTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 180)
            triggerHaptic()
        } catch (e: Exception) {
            Log.e("SoundAlert", "Failed to play sweep tone", e)
        }
    }

    private fun triggerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(80)
                }
            }
        } catch (e: Exception) {
            Log.e("SoundAlert", "Vibration failed", e)
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
