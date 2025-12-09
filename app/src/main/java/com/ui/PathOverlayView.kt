package com.motion.dr_project.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.cos

class PathOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val pathPoints = ArrayList<PointF>(1024)
    private var yawDeg: Float = 0f

    private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.CYAN
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }
    private val carPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00d1b2")
        style = Paint.Style.FILL
    }

    private val carPath = Path()

    fun addSample(position: FloatArray, yawDeg: Float) {
        // scale meters -> pixels (auto from view size)
        val scale = (min(width, height) / 20f).coerceAtLeast(40f) // 10m span by default
        val x = width / 2f + position[0] * scale
        val y = height / 2f - position[1] * scale // +Y up the screen
        pathPoints.add(PointF(x, y))
        this.yawDeg = yawDeg
        if (pathPoints.size > 2000) pathPoints.removeAt(0)
        invalidate()
    }

    fun reset() {
        pathPoints.clear()
        yawDeg = 0f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (pathPoints.size >= 2) {
            val p = Path()
            p.moveTo(pathPoints.first().x, pathPoints.first().y)
            for (i in 1 until pathPoints.size) {
                p.lineTo(pathPoints[i].x, pathPoints[i].y)
            }
            canvas.drawPath(p, pathPaint)
        }
        // draw car at last point
        if (pathPoints.isNotEmpty()) {
            val p = pathPoints.last()
            drawCar(canvas, p.x, p.y, yawDeg)
        }
    }

    private fun drawCar(canvas: Canvas, cx: Float, cy: Float, headingDeg: Float) {
        // MUCH smaller triangle now
        val baseSize = (min(width, height) * 0.015f)  // about 1.5% of min dimension
        val size = baseSize.coerceIn(6f, 14f)

        val rad = Math.toRadians(headingDeg.toDouble())
        // Triangle pointing forward
        val x1 = cx + size * sin(rad).toFloat()
        val y1 = cy - size * cos(rad).toFloat()
        val x2 = cx + size * sin(rad + Math.PI * 2 / 3).toFloat()
        val y2 = cy - size * cos(rad + Math.PI * 2 / 3).toFloat()
        val x3 = cx + size * sin(rad - Math.PI * 2 / 3).toFloat()
        val y3 = cy - size * cos(rad - Math.PI * 2 / 3).toFloat()

        carPath.reset()
        carPath.moveTo(x1, y1)
        carPath.lineTo(x2, y2)
        carPath.lineTo(x3, y3)
        carPath.close()
        canvas.drawPath(carPath, carPaint)
    }

    data class PointF(val x: Float, val y: Float)
}
