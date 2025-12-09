package com.motion.dr_project.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class SpeedometerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var speed = 0f
        set(value) {
            field = value.coerceAtLeast(0f)
            invalidate()
        }

    var maxExpectedSpeed = 3f // m/s, can be adjusted by activity

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height.toFloat() * 0.9f
        val r = min(cx, height * 0.8f) - 12f

        // background arc
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 18f
        paint.color = Color.argb(80, 200, 200, 200)
        val rect = RectF(cx - r, cy - r, cx + r, cy + r)
        canvas.drawArc(rect, 180f, 180f, false, paint)

        // active arc
        val ratio = (speed / maxExpectedSpeed).coerceIn(0f, 1f)
        paint.color = speedColor(ratio)
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawArc(rect, 180f, 180f * ratio, false, paint)

        // needle
        val angle = 180f + 180f * ratio
        val rad = Math.toRadians(angle.toDouble())
        paint.strokeWidth = 6f
        paint.color = Color.WHITE
        val tipX = cx + r * cos(rad).toFloat()
        val tipY = cy + r * sin(rad).toFloat()
        canvas.drawLine(cx, cy, tipX, tipY, paint)

        // center dot
        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, 10f, paint)

        // labels
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 32f
        canvas.drawText("%.2f m/s".format(speed), cx, cy - r - 12f, paint)

        paint.textSize = 20f
        paint.color = Color.LTGRAY
        canvas.drawText("Speedometer", cx, cy + 28f, paint)
    }

    private fun speedColor(r: Float): Int {
        val t = r.coerceIn(0f, 1f)
        val rCol = (0 + 255 * t).toInt()
        val gCol = (200 - 150 * t).toInt()
        val bCol = (80 - 60 * t).toInt().coerceAtLeast(0)
        return Color.rgb(rCol, gCol, bCol)
    }
}
