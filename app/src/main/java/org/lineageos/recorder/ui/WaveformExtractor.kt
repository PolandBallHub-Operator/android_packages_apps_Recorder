package org.lineageos.recorder.ui

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import kotlin.math.abs
import kotlin.math.sqrt

/** Decodes an audio Uri and reduces its PCM samples to normalized waveform peaks. */
object WaveformExtractor {
    fun extract(context: Context, uri: Uri, samplesPerPeak: Int = 2048): FloatArray {
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null
        return try {
            extractor.setDataSource(context, uri, null)
            var format: MediaFormat? = null
            for (index in 0 until extractor.trackCount) {
                val candidate = extractor.getTrackFormat(index)
                if (candidate.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                    extractor.selectTrack(index)
                    format = candidate
                    break
                }
            }
            val selected = format ?: return FloatArray(0)
            val mime = selected.getString(MediaFormat.KEY_MIME) ?: return FloatArray(0)
            codec = MediaCodec.createDecoderByType(mime).apply {
                configure(selected, null, null, 0)
                start()
            }

            val peaks = ArrayList<Float>()
            val info = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            var sampleCount = 0
            var peak = 0f
            var energy = 0.0

            while (!outputDone) {
                if (!inputDone) {
                    val inputIndex = codec.dequeueInputBuffer(TIMEOUT_US)
                    if (inputIndex >= 0) {
                        val input = codec.getInputBuffer(inputIndex)
                        val size = input?.let { extractor.readSampleData(it, 0) } ?: -1
                        if (size < 0) {
                            codec.queueInputBuffer(inputIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            codec.queueInputBuffer(inputIndex, 0, size, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                val outputIndex = codec.dequeueOutputBuffer(info, TIMEOUT_US)
                when {
                    outputIndex >= 0 -> {
                        val output = codec.getOutputBuffer(outputIndex)
                        if (output != null && info.size > 0) {
                            output.position(info.offset)
                            output.limit(info.offset + info.size)
                            while (output.remaining() >= 2) {
                                val value = output.short.toInt() / 32768f
                                peak = maxOf(peak, abs(value))
                                energy += value * value
                                sampleCount++
                                if (sampleCount >= samplesPerPeak) {
                                    val rms = sqrt(energy / sampleCount).toFloat()
                                    peaks.add(maxOf(peak, rms).coerceIn(0.04f, 1f))
                                    sampleCount = 0
                                    peak = 0f
                                    energy = 0.0
                                }
                            }
                        }
                        outputDone = info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
                        codec.releaseOutputBuffer(outputIndex, false)
                    }
                    outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> Unit
                }
            }
            if (sampleCount > 0) {
                peaks.add(maxOf(peak, sqrt(energy / sampleCount).toFloat()).coerceIn(0.04f, 1f))
            }
            peaks.toFloatArray()
        } finally {
            runCatching { codec?.stop() }
            runCatching { codec?.release() }
            extractor.release()
        }
    }

    private const val TIMEOUT_US = 10_000L
}
