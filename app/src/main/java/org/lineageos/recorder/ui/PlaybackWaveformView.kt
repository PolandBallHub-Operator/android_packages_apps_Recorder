package org.lineageos.recorder.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.android.material.color.MaterialColors
import kotlin.math.max

class PlaybackWaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeCap = Paint.Cap.ROUND }
    private var playbackProgress = 0f
    private var amplitudes = FloatArray(BAR_COUNT) { MIN_AMPLITUDE }

    /** Resamples any recording length to the same fixed 72 display bars. */
    fun setAmplitudes(values: FloatArray) {
        if (values.isNotEmpty()) {
            amplitudes = FloatArray(BAR_COUNT) { index ->
                val start = index * values.size / BAR_COUNT
                val end = ((index + 1) * values.size / BAR_COUNT).coerceAtLeast(start + 1)
                var peak = MIN_AMPLITUDE
                for (sample in start until end.coerceAtMost(values.size)) {
                    peak = max(peak, values[sample])
                }
                peak.coerceIn(MIN_AMPLITUDE, 1f)
            }
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val active = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary)
        val inactive = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutlineVariant)
        val step = width.toFloat() / (BAR_COUNT + 1)
        val centerY = height / 2f
        paint.strokeWidth = max(3f, resources.displayMetrics.density * 4f)
        for (index in 0 until BAR_COUNT) {
            val x = step * (index + 1)
            val normalized = index.toFloat() / BAR_COUNT
            val barHeight = (height * 0.44f * amplitudes[index]).coerceAtLeast(6f)
            paint.color = if (normalized <= playbackProgress) active else inactive
            canvas.drawLine(x, centerY - barHeight, x, centerY + barHeight, paint)
        }
    }

    fun setProgress(progress: Float) {
        playbackProgress = progress.coerceIn(0f, 1f)
        invalidate()
    }

    companion object {
        private const val BAR_COUNT = 72
        private const val MIN_AMPLITUDE = 0.04f
    }
}
