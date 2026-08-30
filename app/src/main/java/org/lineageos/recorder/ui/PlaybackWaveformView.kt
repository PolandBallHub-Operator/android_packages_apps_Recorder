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
    private var amplitudes = FloatArray(0)

    fun setAmplitudes(values: FloatArray) {
        amplitudes = values.copyOf()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val active = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary)
        val inactive = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutlineVariant)
        val bars = if (amplitudes.isNotEmpty()) amplitudes else FloatArray(DEFAULT_BAR_COUNT) { 0.12f }
        val step = width.toFloat() / (bars.size + 1)
        val centerY = height / 2f
        paint.strokeWidth = max(3f, resources.displayMetrics.density * 4f)
        bars.forEachIndexed { index, amplitude ->
            val x = step * (index + 1)
            val normalized = index.toFloat() / bars.size
            val barHeight = (height * 0.44f * amplitude.coerceIn(0.04f, 1f)).coerceAtLeast(6f)
            paint.color = if (normalized <= playbackProgress) active else inactive
            canvas.drawLine(x, centerY - barHeight, x, centerY + barHeight, paint)
        }
    }

    fun setProgress(progress: Float) {
        playbackProgress = progress.coerceIn(0f, 1f)
        invalidate()
    }

    companion object {
        private const val DEFAULT_BAR_COUNT = 72
    }
}
