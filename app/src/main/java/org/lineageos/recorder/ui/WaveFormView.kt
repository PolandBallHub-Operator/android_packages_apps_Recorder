package org.lineageos.recorder.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.android.material.color.MaterialColors
import org.lineageos.recorder.R
import kotlin.math.max

/** Fixed-density live waveform. The ring buffer always renders exactly 72 bars. */
class WaveFormView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val maxAudioValue: Float
    private val idleAmplitude: Float
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeCap = Paint.Cap.ROUND }
    private val ampLock = Any()
    private val samples = FloatArray(BAR_COUNT) { MIN_AMPLITUDE }
    private var writeIndex = 0
    private var amplitude: Float

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
        val color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary)
        val step = width.toFloat() / (BAR_COUNT + 1)
        val centerY = height / 2f
        paint.color = color
        paint.strokeWidth = max(3f, resources.displayMetrics.density * 4f)
        synchronized(ampLock) {
            for (offset in 0 until BAR_COUNT) {
                val index = (writeIndex + offset) % BAR_COUNT
                val barHeight = (height * 0.44f * samples[index]).coerceAtLeast(6f)
                val x = step * (offset + 1)
                canvas.drawLine(x, centerY - barHeight, x, centerY + barHeight, paint)
            }
        }
    }

    /** Appends one live amplitude sample; bar count and line width never change. */
    fun setAmplitude(amplitude: Int) {
        synchronized(ampLock) {
            this.amplitude = (amplitude / maxAudioValue).coerceIn(0f, idleAmplitude)
            samples[writeIndex] = this.amplitude.coerceIn(MIN_AMPLITUDE, 1f)
            writeIndex = (writeIndex + 1) % BAR_COUNT
        }
        postInvalidateOnAnimation()
    }

    companion object {
        private const val BAR_COUNT = 72
        private const val DEFAULT_MAX_AUDIO_VALUE = 1500
        private const val DEFAULT_AMPLITUDE = 1f
        private const val MIN_AMPLITUDE = 0.04f
    }
}
