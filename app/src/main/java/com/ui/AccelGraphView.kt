package com.motion.dr_project.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class AccelGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val maxSamples = 200
    private val rawX = ArrayList<Float>(maxSamples)
    private val rawY = ArrayList<Float>(maxSamples)
    private val rawZ = ArrayList<Float>(maxSamples)

    private val filtX = ArrayList<Float>(maxSamples)
    private val filtY = ArrayList<Float>(maxSamples)
    private val filtZ = ArrayList<Float>(maxSamples)

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(160, 80, 80, 80)
        strokeWidth = 1.5f
        style = Paint.Style.STROKE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        textSize = 18f
    }
    private val rawPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val filtPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }

    fun addSample(raw: FloatArray, filt: FloatArray) {
        addToSeries(rawX, raw[0])
        addToSeries(rawY, raw[1])
        addToSeries(rawZ, raw[2])
        addToSeries(filtX, filt[0])
        addToSeries(filtY, filt[1])
        addToSeries(filtZ, filt[2])
        invalidate()
    }

    private fun addToSeries(list: MutableList<Float>, v: Float) {
        list.add(v)
        if (list.size > maxSamples) list.removeAt(0)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // semi-transparent background so map is visible behind
        canvas.drawColor(Color.argb(140, 17, 20, 28))

        val w = width.toFloat()
        val h = height.toFloat()
        val padding = 16f
        val plotLeft = padding
        val plotRight = w - padding
        val plotTop = padding
        val plotBottom = h - padding

        canvas.drawRect(plotLeft, plotTop, plotRight, plotBottom, axisPaint)

        val all = (rawX + rawY + rawZ + filtX + filtY + filtZ)
        if (all.isEmpty()) {
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("Accel filter visualizer", w / 2f, h / 2f, textPaint)
            return
        }

        val minV = all.minOrNull() ?: -1f
        val maxV = all.maxOrNull() ?: 1f
        val span = (maxV - minV).coerceAtLeast(0.5f)

        fun pathFrom(list: List<Float>): Path {
            val p = Path()
            if (list.isEmpty()) return p
            val n = list.size
            for ((i, v) in list.withIndex()) {
                val x = plotLeft + (plotRight - plotLeft) * (i / (n - 1f).coerceAtLeast(1f))
                val norm = (v - minV) / span
                val y = plotBottom - (plotBottom - plotTop) * norm
                if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
            }
            return p
        }

        // raw xyz
        rawPaint.color = Color.argb(140, 244, 67, 54)
        canvas.drawPath(pathFrom(rawX), rawPaint)
        rawPaint.color = Color.argb(140, 33, 150, 243)
        canvas.drawPath(pathFrom(rawY), rawPaint)
        rawPaint.color = Color.argb(140, 76, 175, 80)
        canvas.drawPath(pathFrom(rawZ), rawPaint)

        // filtered x (you can extend to y,z similarly if you want)
        filtPaint.color = Color.argb(220, 255, 193, 7)
        canvas.drawPath(pathFrom(filtX), filtPaint)

        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Raw vs filtered accel (x,y,z)", padding, padding + 14f, textPaint)
    }
}
