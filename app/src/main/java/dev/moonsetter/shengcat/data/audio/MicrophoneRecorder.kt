package dev.moonsetter.shengcat.data.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MicrophoneRecorder @Inject constructor(
) {
    private val sampleRate: Int = 44100
    private val frameSize: Int = 2048

    @SuppressLint("MissingPermission")
    fun record(): Flow<FloatArray> = flow {
        val minBuffer = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(minBuffer, frameSize * 2)

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        check(audioRecord.state == AudioRecord.STATE_INITIALIZED) {
            "AudioRecord failed to initialize"
        }

        audioRecord.startRecording()

        try {
            val buffer = ShortArray(frameSize)
            while (currentCoroutineContext().isActive) {
                val read = audioRecord.read(buffer, 0, frameSize)
                if (read > 0) {
                    val floatFrame = FloatArray(read) { i -> buffer[i] / 32768f }
                    emit(floatFrame)
                }
            }
        } finally {
            audioRecord.stop()
            audioRecord.release()
        }
    }.flowOn(Dispatchers.IO)
}