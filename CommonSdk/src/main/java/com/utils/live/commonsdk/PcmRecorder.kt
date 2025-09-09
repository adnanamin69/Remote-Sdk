package com.utils.live.commonsdk


import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.util.Log
import com.github.squti.androidwaverecorder.WaveRecorder
import com.sdk.remote.androidremote.DeviceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class PcmAudioRecorder(val context: Context) {

    @Volatile
    private var isRecording = false

    private var waveRecorder: WaveRecorder? = null

    private val lock = ReentrantLock()

    @SuppressLint("MissingPermission")
    suspend fun recordPcmAudio(
        device: DeviceType,
        durationMillis: Long = 6000,
        callback: (ByteArray) -> Unit
    ) = withContext(Dispatchers.IO) {
        val sampleRate1 = if (device == DeviceType.FIRE) 16000 else 8000

        if (isRecording) {
            Log.w("PcmAudioRecorder", "Already recording, stopping previous session.")
            //stopRecording()
            return@withContext // Optional: small buffer delay before restarting
        }

        isRecording = true

        val audioFile = File.createTempFile("audio_temp", ".wav", context.cacheDir)
        val filePath = audioFile.absolutePath

        waveRecorder = WaveRecorder(filePath).apply {

            configureSilenceDetection {
                minAmplitudeThreshold = 500
                bufferDurationInMillis = 1500
                preSilenceDurationInMillis = 1500
            }

            onStateChangeListener = {
                Log.i("MainActivityPairing", "recordPcmAudio: ${it.name}")
            }

            configureWaveSettings {
                sampleRate = sampleRate1
                audioEncoding = AudioFormat.ENCODING_PCM_16BIT
                channels = AudioFormat.CHANNEL_IN_MONO
            }

            val tempBuffer = ByteArrayOutputStream()
            val chunkSize = 19_800

            onAudioChunkCaptured = { dataChunk ->
                if (!dataChunk.isChunkEmptyOrZero()) {
                    lock.withLock {
                        tempBuffer.write(dataChunk)

                        while (tempBuffer.size() >= chunkSize) {
                            val fullChunk = tempBuffer.toByteArray()
                            val toSend = fullChunk.copyOfRange(0, chunkSize)

                            if (!toSend.isChunkEmptyOrZero()) {
                                callback.invoke(toSend)
                                Log.i("Samples", "recordPcmAudio: ${toSend.size}")
                                Log.i("Samples", "recordPcmAudio: ${toSend.contentToString()}")
                            }

                            // Reset and keep remaining
                            tempBuffer.reset()
                            val remaining = fullChunk.copyOfRange(chunkSize, fullChunk.size)
                            tempBuffer.write(remaining)
                        }
                    }
                }
            }

            startRecording()
        }

        delay(durationMillis)

        stopRecording()
    }

    fun stopRecording() {
        if (isRecording) {
            waveRecorder?.stopRecording()
            waveRecorder = null
            isRecording = false
            Log.i("PcmAudioRecorder", "Recording stopped.")
        }
    }

    private fun ByteArray.isChunkEmptyOrZero(): Boolean {
        return all { it == 0.toByte() }
    }
}


