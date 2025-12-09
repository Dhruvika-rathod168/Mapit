package com.motion.dr_project.core

object Math3 {
    fun norm(v: FloatArray): Float {
        return kotlin.math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2])
    }
}
