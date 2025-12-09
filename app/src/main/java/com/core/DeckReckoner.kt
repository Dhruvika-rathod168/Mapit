package com.motion.dr_project.core

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class DeadReckoner(private val context: Context) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelDevice = FloatArray(3)
    private val linearWorld = FloatArray(3)
    private val velocityWorld = FloatArray(3)
    private val positionWorld = FloatArray(3)
    private var rotationMatrix = FloatArray(9) { i -> if (i % 4 == 0) 1f else 0f }
    private var hasRotation = false
    private var lastTimestamp = 0L
    private var running = false
    private var gravityWorldZ = SensorManager.GRAVITY_EARTH
    private val alpha = 0.9f            
    private val motionThreshold = 0.08f 
    private val zeroVelocityThreshold = 0.05f 
    private var bodyMountedMode = true

    data class DrState(
        val position: FloatArray,       
        val speed: Float,               
        val yawDeg: Float,              
        val pitchDeg: Float,
        val rollDeg: Float,
        val rawAccelDevice: FloatArray, 
        val linearAccelWorld: FloatArray 
    )

    var onUpdate: ((DrState) -> Unit)? = null

    fun setBodyMountedMode(enabled: Boolean) {
        bodyMountedMode = enabled
    }

    fun start() {
        reset()
        running = true

        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (accelSensor == null || rotationSensor == null) {
            android.util.Log.e("DeadReckoner", "Required sensors not available!")
            running = false
            return
        }

        sensorManager.registerListener(
            this,
            accelSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
        sensorManager.registerListener(
            this,
            rotationSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    fun pause() {
        running = false
        sensorManager.unregisterListener(this)
    }

    fun reset() {
        accelDevice.fill(0f)
        linearWorld.fill(0f)
        velocityWorld.fill(0f)
        positionWorld.fill(0f)
        rotationMatrix = FloatArray(9) { i -> if (i % 4 == 0) 1f else 0f }
        hasRotation = false
        lastTimestamp = 0L
        gravityWorldZ = SensorManager.GRAVITY_EARTH
    }

    fun release() = pause()

    override fun onSensorChanged(event: SensorEvent?) {
        if (!running || event == null) return

        when (event.sensor.type) {

            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                hasRotation = true
            }

            Sensor.TYPE_ACCELEROMETER -> {
                if (!hasRotation) return

                val now = event.timestamp
                if (lastTimestamp == 0L) {
                    lastTimestamp = now
                    return
                }

                var dt = (now - lastTimestamp) / 1_000_000_000f
                lastTimestamp = now

                if (dt <= 0f || dt > 0.3f) {
                    dt = 0.02f 
                }

                accelDevice[0] = event.values[0]
                accelDevice[1] = event.values[1]
                accelDevice[2] = event.values[2]

                val ax = accelDevice[0]
                val ay = accelDevice[1]
                val az = accelDevice[2]

                val worldX =
                    rotationMatrix[0] * ax + rotationMatrix[1] * ay + rotationMatrix[2] * az
                val worldY =
                    rotationMatrix[3] * ax + rotationMatrix[4] * ay + rotationMatrix[5] * az
                val worldZ =
                    rotationMatrix[6] * ax + rotationMatrix[7] * ay + rotationMatrix[8] * az

                gravityWorldZ = alpha * gravityWorldZ + (1f - alpha) * worldZ

                linearWorld[0] = worldX
                linearWorld[1] = worldY
                linearWorld[2] = worldZ - gravityWorldZ

                if (bodyMountedMode) {
                    linearWorld[2] = 0f
                }
                
                for (i in 0..2) {
                    linearWorld[i] *= 1.2f
                }

                val accelNorm = sqrt(
                    linearWorld[0] * linearWorld[0] +
                            linearWorld[1] * linearWorld[1] +
                            linearWorld[2] * linearWorld[2]
                )

                if (accelNorm < motionThreshold) {
                    velocityWorld.fill(0f)
                    emitState()
                    return
                }

                for (i in 0..2) {
                    velocityWorld[i] += linearWorld[i] * dt
                    positionWorld[i] += velocityWorld[i] * dt
                }

                val speed = sqrt(
                    velocityWorld[0] * velocityWorld[0] +
                            velocityWorld[1] * velocityWorld[1] +
                            velocityWorld[2] * velocityWorld[2]
                )

                if (speed < zeroVelocityThreshold) {
                    velocityWorld.fill(0f)
                }

                emitState()
            }
        }
    }

    private fun emitState() {
        val orientation = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientation)

        var yaw = Math.toDegrees(orientation[0].toDouble()).toFloat()
        val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
        val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()

        if (yaw < 0) yaw += 360f

        val speed = sqrt(
            velocityWorld[0] * velocityWorld[0] +
                    velocityWorld[1] * velocityWorld[1] +
                    velocityWorld[2] * velocityWorld[2]
        )

        val state = DrState(
            position = positionWorld.clone(),
            speed = speed,
            yawDeg = yaw,
            pitchDeg = pitch,
            rollDeg = roll,
            rawAccelDevice = accelDevice.clone(),
            linearAccelWorld = linearWorld.clone()
        )

        onUpdate?.invoke(state)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}
