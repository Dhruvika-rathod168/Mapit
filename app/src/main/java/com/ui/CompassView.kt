package com.motion.dr_project.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class CompassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var headingDeg = 0f
        set(value) {
            field = ((value % 360) + 360) % 360
            invalidate()
        }

    var speed = 0f
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val r = min(cx, cy) - 12f

        // background dial
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#20252F")
        canvas.drawCircle(cx, cy, r, paint)

        // inner gradient ring
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 12f
            shader = SweepGradient(
                cx,
                cy,
                intArrayOf(
                    Color.parseColor("#00E5FF"),
                    Color.parseColor("#FFB74D"),
                    Color.parseColor("#E53935"),
                    Color.parseColor("#00E5FF")
                ),
                null
            )
        }
        canvas.drawCircle(cx, cy, r - 10f, ringPaint)

        // ticks + cardinal directions
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f
        paint.color = Color.argb(200, 180, 190, 200)
        for (i in 0 until 360 step 15) {
            val rad = Math.toRadians(i.toDouble())
            val inner = if (i % 45 == 0) r - 24 else r - 16
            val x1 = cx + inner * cos(rad).toFloat()
            val y1 = cy - inner * sin(rad).toFloat()
            val x2 = cx + r * cos(rad).toFloat()
            val y2 = cy - r * sin(rad).toFloat()
            canvas.drawLine(x1, y1, x2, y2, paint)
        }

        // cardinal labels
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 28f
        val labels = arrayOf("N", "E", "S", "W")
        val angles = floatArrayOf(0f, 90f, 180f, 270f)
        paint.color = Color.WHITE
        for (i in labels.indices) {
            val a = Math.toRadians(angles[i].toDouble())
            val tx = cx + (r - 40) * sin(a).toFloat()
            val ty = cy - (r - 40) * cos(a).toFloat() + 10f
            canvas.drawText(labels[i], tx, ty, paint)
        }

        // FOV cone ahead (e.g. 60°)
        val fov = 60f
        val start = headingDeg - fov / 2f
        val end = headingDeg + fov / 2f
        val path = Path()
        path.moveTo(cx, cy)
        val step = 5
        for (ang in start.toInt()..end.toInt() step step) {
            val rad = Math.toRadians(ang.toDouble())
            val x = cx + (r - 30) * sin(rad).toFloat()
            val y = cy - (r - 30) * cos(rad).toFloat()
            if (ang == start.toInt()) {
                path.lineTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        paint.color = Color.argb(80, 0, 229, 255)
        canvas.drawPath(path, paint)

        // heading arrow
        paint.color = Color.parseColor("#FF5E6C")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        val a = Math.toRadians(headingDeg.toDouble())
        val tipX = cx + (r - 40) * sin(a).toFloat()
        val tipY = cy - (r - 40) * cos(a).toFloat()
        canvas.drawLine(cx, cy, tipX, tipY, paint)

        // center dot
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawCircle(cx, cy, 6f, paint)

        // labels at bottom
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 26f
        paint.color = Color.WHITE
        canvas.drawText("Heading: ${headingDeg.toInt()}°", 16f, height - 48f, paint)
        canvas.drawText("Speed: %.2f m/s".format(speed), 16f, height - 16f, paint)
    }
}
