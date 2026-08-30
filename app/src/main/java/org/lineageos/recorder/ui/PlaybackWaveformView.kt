/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.recorder.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.android.material.color.MaterialColors
import kotlin.math.abs
import kotlin.math.sin

class PlaybackWaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val barCount = 42
    private var playbackProgress = 0f

    init {
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 5f * resources.displayMetrics.density
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val active = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary)
        val inactive = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutlineVariant)
        val centerY = height / 2f
        val step = width.toFloat() / (barCount + 1)
        for (index in 0 until barCount) {
            val x = step * (index + 1)
            val normalized = index.toFloat() / barCount
            val envelope = 0.18f + 0.82f * abs(sin(normalized * Math.PI * 2.7)).toFloat()
            val height = (this.height * 0.34f * envelope).coerceAtLeast(10f)
            paint.color = if (normalized <= playbackProgress) active else inactive
            canvas.drawLine(x, centerY - height, x, centerY + height, paint)
        }
    }

    fun setProgress(progress: Float) {
        playbackProgress = progress.coerceIn(0f, 1f)
        invalidate()
    }
}
