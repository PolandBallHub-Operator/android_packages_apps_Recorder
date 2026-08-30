package org.lineageos.recorder.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.android.material.color.MaterialColors
import org.lineageos.recorder.R
import kotlin.math.abs
import kotlin.math.sin

/** Live waveform rendered with the same bar language as PlaybackWaveformView. */
class WaveFormView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val maxAudioValue: Float
    private val idleAmplitude: Float
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeCap = Paint.Cap.ROUND }
    private val ampLock = Any()
    private var amplitude: Float
    private var phase = 0f

    init {
        val ta = context.resources.obtainAttributes(attrs, R.styleable.WaveFormView)
        maxAudioValue = ta.getInt(
            R.styleable.WaveFormView_maxAudioValue, DEFAULT_MAX_AUDIO_VALUE
        ).toFloat()
        amplitude = ta.getFloat(
            R.styleable.WaveFormView_defaultAmplitude, DEFAULT_AMPLITUDE
        )
        idleAmplitude = amplitude
        ta.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val active = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary)
        val bars = DEFAULT_BAR_COUNT
        val step = width.toFloat() / (bars + 1)
        val centerY = height / 2f
        val currentAmplitude = synchronized(ampLock) { amplitude }.coerceIn(0.04f, 1f)
        paint.strokeWidth = (resources.displayMetrics.density * 4f).coerceAtLeast(3f)
        for (index in 0 until bars) {
            val x = step * (index + 1)
            val normalized = index.toFloat() / bars
            val profile = 0.16f + 0.84f * abs(sin(normalized * Math.PI * 2.7 + phase)).toFloat()
            val barHeight = (height * 0.44f * profile * currentAmplitude).coerceAtLeast(6f)
            paint.color = active
            canvas.drawLine(x, centerY - barHeight, x, centerY + barHeight, paint)
        }
        phase += 0.025f
        postInvalidateOnAnimation()
    }

    fun setAmplitude(amplitude: Int) {
        synchronized(ampLock) {
            this.amplitude = (amplitude / maxAudioValue).coerceIn(0f, idleAmplitude)
        }
    }

    companion object {
        private const val DEFAULT_BAR_COUNT = 72
        private const val DEFAULT_MAX_AUDIO_VALUE = 1500
        private const val DEFAULT_AMPLITUDE = 1f
    }
}
