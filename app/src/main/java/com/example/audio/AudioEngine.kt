package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Interactive Audio & Haptics Engine for Clinical Assessment
 * Uses Android SoundPool with preloaded audio clips (R.raw.correct, R.raw.incorrect)
 * with graceful fallback to PCM synthesized audio.
 */
class AudioEngine(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private var soundPool: SoundPool? = null
    private var correctSoundId: Int = 0
    private var incorrectSoundId: Int = 0
    private var soundPoolLoaded = false

    private var focusAmbienceJob: Job? = null
    private var ambienceTrack: AudioTrack? = null

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    init {
        initSoundPool()
    }

    private fun initSoundPool() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(audioAttributes)
                .build()

            soundPool?.setOnLoadCompleteListener { _, _, status ->
                if (status == 0) {
                    soundPoolLoaded = true
                }
            }

            try {
                correctSoundId = soundPool?.load(context, R.raw.correct, 1) ?: 0
                incorrectSoundId = soundPool?.load(context, R.raw.incorrect, 1) ?: 0
            } catch (ignored: Exception) {
                // Will fall back to synthesis
            }
        } catch (e: Exception) {
            soundPool = null
        }
    }

    fun toggleMute(): Boolean {
        _isMuted.value = !_isMuted.value
        if (_isMuted.value) {
            stopFocusAmbience()
        }
        return _isMuted.value
    }

    /**
     * Pleasant ascending celebratory chime for correct answers
     */
    fun playCorrect() {
        vibrateSuccess()
        if (_isMuted.value) return

        if (soundPoolLoaded && correctSoundId != 0) {
            try {
                soundPool?.play(correctSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
                return
            } catch (ignored: Exception) {}
        }

        // Fast fallback: PCM synthesis
        scope.launch {
            playTones(
                listOf(
                    ToneSpec(frequency = 523.25, durationMs = 80, volume = 0.85f),
                    ToneSpec(frequency = 659.25, durationMs = 80, volume = 0.9f),
                    ToneSpec(frequency = 783.99, durationMs = 90, volume = 0.95f),
                    ToneSpec(frequency = 1046.50, durationMs = 240, volume = 1.0f)
                ),
                addHarmonics = true
            )
        }
    }

    /**
     * Alert correction sound for incorrect answers
     */
    fun playIncorrect() {
        vibrateError()
        if (_isMuted.value) return

        if (soundPoolLoaded && incorrectSoundId != 0) {
            try {
                soundPool?.play(incorrectSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
                return
            } catch (ignored: Exception) {}
        }

        // Fast fallback: PCM synthesis
        scope.launch {
            playTones(
                listOf(
                    ToneSpec(frequency = 261.63, durationMs = 120, volume = 0.85f), // C4
                    ToneSpec(frequency = 196.00, durationMs = 180, volume = 0.95f), // G3
                    ToneSpec(frequency = 164.81, durationMs = 220, volume = 0.90f)  // E3
                ),
                addHarmonics = false,
                isBuzz = true
            )
        }
    }

    /**
     * Ticking click for final 10-second countdown
     */
    fun playTick(isUrgent: Boolean = false) {
        if (isUrgent) {
            vibrateTick()
        }
        if (_isMuted.value) return
        scope.launch {
            val freq = if (isUrgent) 1200.0 else 880.0
            val durationMs = if (isUrgent) 35 else 25
            playSingleTone(freq, durationMs, 0.4f)
        }
    }

    /**
     * Lifeline activation shimmer
     */
    fun playLifeline() {
        vibrateTick()
        if (_isMuted.value) return
        scope.launch {
            playTones(
                listOf(
                    ToneSpec(440.0, 50, 0.4f),
                    ToneSpec(554.37, 50, 0.5f),
                    ToneSpec(659.25, 50, 0.6f),
                    ToneSpec(880.0, 100, 0.7f)
                )
            )
        }
    }

    /**
     * Level completion fanfare
     */
    fun playLevelComplete() {
        vibrateSuccess()
        if (_isMuted.value) return
        scope.launch {
            playTones(
                listOf(
                    ToneSpec(523.25, 100, 0.6f),
                    ToneSpec(659.25, 100, 0.6f),
                    ToneSpec(783.99, 100, 0.7f),
                    ToneSpec(1046.50, 260, 0.85f)
                )
            )
        }
    }

    /**
     * Gentle calming focus background ambience drone (low soothing harmonic wave)
     */
    fun startFocusAmbience() {
        if (_isMuted.value || focusAmbienceJob != null) return
        focusAmbienceJob = scope.launch {
            try {
                val sampleRate = 22050
                val durationSeconds = 2
                val numSamples = sampleRate * durationSeconds
                val buffer = ShortArray(numSamples)
                
                // Soothing sub-harmonic 130.81Hz (C3) & 196Hz (G3) with soft sine envelope
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val wave1 = sin(2.0 * Math.PI * 130.81 * t)
                    val wave2 = sin(2.0 * Math.PI * 196.00 * t) * 0.4
                    val combined = (wave1 + wave2) * 0.08 * Short.MAX_VALUE
                    buffer[i] = combined.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                ambienceTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                ambienceTrack?.write(buffer, 0, buffer.size)
                ambienceTrack?.setLoopPoints(0, buffer.size, -1)
                ambienceTrack?.play()

                while (isActive) {
                    delay(1000)
                }
            } catch (e: Exception) {
                // Ignore audio interruption
            } finally {
                try {
                    ambienceTrack?.stop()
                    ambienceTrack?.release()
                } catch (ignored: Exception) {}
                ambienceTrack = null
            }
        }
    }

    fun stopFocusAmbience() {
        focusAmbienceJob?.cancel()
        focusAmbienceJob = null
        try {
            ambienceTrack?.stop()
            ambienceTrack?.release()
        } catch (ignored: Exception) {}
        ambienceTrack = null
    }

    private data class ToneSpec(val frequency: Double, val durationMs: Int, val volume: Float)

    private fun playTones(
        tones: List<ToneSpec>,
        addHarmonics: Boolean = false,
        isBuzz: Boolean = false
    ) {
        try {
            val sampleRate = 44100
            val totalDurationMs = tones.sumOf { it.durationMs }
            val totalSamples = (sampleRate * (totalDurationMs / 1000.0)).toInt()
            val fullBuffer = ShortArray(totalSamples)

            var offset = 0
            for (tone in tones) {
                val samplesCount = (sampleRate * (tone.durationMs / 1000.0)).toInt()
                for (i in 0 until samplesCount) {
                    val t = i.toDouble() / sampleRate
                    // Smooth envelope to avoid clicks
                    val envelope = when {
                        i < 150 -> i / 150.0
                        i > samplesCount - 300 -> (samplesCount - i) / 300.0
                        else -> 1.0
                    }

                    val rawWave = if (isBuzz) {
                        // Square-ish / sawtooth enriched wave for distinct incorrect buzzer feel
                        val fundamental = sin(2.0 * Math.PI * tone.frequency * t)
                        val thirdHarmonic = 0.33 * sin(2.0 * Math.PI * (tone.frequency * 3.0) * t)
                        val fifthHarmonic = 0.20 * sin(2.0 * Math.PI * (tone.frequency * 5.0) * t)
                        (fundamental + thirdHarmonic + fifthHarmonic) * 0.75
                    } else if (addHarmonics) {
                        // Rich pleasant chime with fundamental + bell overtone (octave + fifth)
                        val fundamental = sin(2.0 * Math.PI * tone.frequency * t)
                        val overtone1 = 0.35 * sin(2.0 * Math.PI * (tone.frequency * 2.0) * t)
                        val overtone2 = 0.15 * sin(2.0 * Math.PI * (tone.frequency * 3.0) * t)
                        (fundamental + overtone1 + overtone2) * 0.75
                    } else {
                        sin(2.0 * Math.PI * tone.frequency * t)
                    }

                    val sample = rawWave * tone.volume * envelope * Short.MAX_VALUE
                    if (offset + i < fullBuffer.size) {
                        fullBuffer[offset + i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                }
                offset += samplesCount
            }

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(fullBuffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(fullBuffer, 0, fullBuffer.size)
            track.play()
            
            // Auto release after playback
            scope.launch {
                delay(totalDurationMs.toLong() + 60)
                try {
                    track.stop()
                    track.release()
                } catch (ignored: Exception) {}
            }
        } catch (e: Exception) {
            // Audio generation fallback
        }
    }

    private fun playSingleTone(frequency: Double, durationMs: Int, volume: Float) {
        playTones(listOf(ToneSpec(frequency, durationMs, volume)))
    }

    private fun vibrateSuccess() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(70)
            }
        } catch (ignored: Exception) {}
    }

    private fun vibrateError() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 50, 100), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(120)
            }
        } catch (ignored: Exception) {}
    }

    private fun vibrateTick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (ignored: Exception) {}
    }

    fun release() {
        stopFocusAmbience()
    }
}
