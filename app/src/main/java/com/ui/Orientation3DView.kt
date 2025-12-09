package com.motion.dr_project.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class Orientation3DView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var yawDeg = 0f
        set(value) { field = value; invalidate() }
    var pitchDeg = 0f
        set(value) { field = value; invalidate() }
    var rollDeg = 0f
        set(value) { field = value; invalidate() }

    private val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.WHITE
    }
    private val facePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(80, 0, 229, 255)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 20f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // semi-transparent overlay background
        canvas.drawColor(Color.argb(140, 21, 24, 32))

        val cx = width / 2f
        val cy = height / 2f

        val size = min(width, height) * 0.4f
        val half = size / 2f

        val verts = arrayOf(
            floatArrayOf(-half, -half, -half),
            floatArrayOf(half, -half, -half),
            floatArrayOf(half, half, -half),
            floatArrayOf(-half, half, -half),
            floatArrayOf(-half, -half, half),
            floatArrayOf(half, -half, half),
            floatArrayOf(half, half, half),
            floatArrayOf(-half, half, half)
        )

        val yaw = Math.toRadians(yawDeg.toDouble())
        val pitch = Math.toRadians(pitchDeg.toDouble())
        val roll = Math.toRadians(rollDeg.toDouble())

        val cosY = cos(yaw); val sinY = sin(yaw)
        val cosP = cos(pitch); val sinP = sin(pitch)
        val cosR = cos(roll); val sinR = sin(roll)

        fun rotate(v: FloatArray): FloatArray {
            var x = v[0].toDouble()
            var y = v[1].toDouble()
            var z = v[2].toDouble()

            // Roll (x-axis)
            var y1 = y * cosR - z * sinR
            var z1 = y * sinR + z * cosR
            y = y1; z = z1

            // Pitch (y-axis)
            var x2 = x * cosP + z * sinP
            var z2 = -x * sinP + z * cosP
            x = x2; z = z2

            // Yaw (z-axis)
            var x3 = x * cosY - y * sinY
            var y3 = x * sinY + y * cosY
            x = x3; y = y3

            return floatArrayOf(x.toFloat(), y.toFloat(), z.toFloat())
        }

        val projected = verts.map { v ->
            val r = rotate(v)
            val px = cx + r[0]
            val py = cy + r[1]
            floatArrayOf(px, py)
        }

        val edges = arrayOf(
            intArrayOf(0, 1), intArrayOf(1, 2), intArrayOf(2, 3), intArrayOf(3, 0),
            intArrayOf(4, 5), intArrayOf(5, 6), intArrayOf(6, 7), intArrayOf(7, 4),
            intArrayOf(0, 4), intArrayOf(1, 5), intArrayOf(2, 6), intArrayOf(3, 7)
        )

        // top face fill
        val topFace = Path().apply {
            moveTo(projected[4][0], projected[4][1])
            lineTo(projected[5][0], projected[5][1])
            lineTo(projected[6][0], projected[6][1])
            lineTo(projected[7][0], projected[7][1])
            close()
        }
        canvas.drawPath(topFace, facePaint)

        for (e in edges) {
            val p1 = projected[e[0]]
            val p2 = projected[e[1]]
            canvas.drawLine(p1[0], p1[1], p2[0], p2[1], edgePaint)
        }

        // labels
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Yaw: ${yawDeg.toInt()}°", 16f, height - 56f, textPaint)
        canvas.drawText("Pitch: ${pitchDeg.toInt()}°", 16f, height - 32f, textPaint)
        canvas.drawText("Roll: ${rollDeg.toInt()}°", 16f, height - 8f, textPaint)
    }
}
