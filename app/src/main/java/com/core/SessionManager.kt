package com.motion.dr_project.core

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class PathSample(
    val x: Float,
    val y: Float,
    val tSec: Float
)

data class PathSession(
    val id: Long,
    val name: String,
    val samples: List<PathSample>,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val maxSpeed: Double,
    val avgSpeed: Double
)

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("dr_sessions", Context.MODE_PRIVATE)

    fun saveSession(session: PathSession) {
        val root = JSONObject()
        root.put("id", session.id)
        root.put("name", session.name)
        root.put("distance", session.distanceMeters)
        root.put("duration", session.durationSeconds)
        root.put("maxSpeed", session.maxSpeed)
        root.put("avgSpeed", session.avgSpeed)
        val arr = JSONArray()
        session.samples.forEach {
            val o = JSONObject()
            o.put("x", it.x)
            o.put("y", it.y)
            o.put("t", it.tSec)
            arr.put(o)
        }
        root.put("samples", arr)

        val existingStr = prefs.getString("sessions", "[]") ?: "[]"
        val existing = JSONArray(existingStr)
        existing.put(root)
        prefs.edit().putString("sessions", existing.toString()).apply()
    }

    fun getSessions(): List<PathSession> {
        val existingStr = prefs.getString("sessions", "[]") ?: "[]"
        val arr = JSONArray(existingStr)
        val result = mutableListOf<PathSession>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val samplesArr = o.getJSONArray("samples")
            val samples = mutableListOf<PathSample>()
            for (j in 0 until samplesArr.length()) {
                val so = samplesArr.getJSONObject(j)
                samples.add(
                    PathSample(
                        so.getDouble("x").toFloat(),
                        so.getDouble("y").toFloat(),
                        so.getDouble("t").toFloat()
                    )
                )
            }
            result.add(
                PathSession(
                    id = o.optLong("id", i.toLong()),
                    name = o.optString("name", "Session $i"),
                    samples = samples,
                    distanceMeters = o.optDouble("distance", 0.0),
                    durationSeconds = o.optDouble("duration", 0.0),
                    maxSpeed = o.optDouble("maxSpeed", 0.0),
                    avgSpeed = o.optDouble("avgSpeed", 0.0)
                )
            )
        }
        return result
    }

    fun clearAll() {
        prefs.edit().remove("sessions").apply()
    }
}
