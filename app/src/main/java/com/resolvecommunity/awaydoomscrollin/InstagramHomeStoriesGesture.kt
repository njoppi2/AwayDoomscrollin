package com.resolvecommunity.awaydoomscrollin

import kotlin.math.abs

/** Completed gestures only. An upward start cannot become a forwarded swipe. */
internal class InstagramHomeStoriesGesture(private val slop: Float) {
    data class Forward(val x: Float, val y: Float, val endX: Float, val endY: Float, val durationMs: Long)
    private var startX = 0f
    private var startY = 0f
    private var startedAt = 0L
    private var active = false
    private var upward = false

    fun begin(x: Float, y: Float, now: Long) {
        startX = x; startY = y; startedAt = now
        active = true; upward = false
    }
    fun move(x: Float, y: Float) {
        if (active && startY - y > slop && startY - y >= abs(x - startX)) upward = true
    }
    fun cancel() { active = false }
    fun finish(x: Float, y: Float, now: Long): Forward? {
        if (!active) return null
        move(x, y)
        active = false
        if (upward) return null
        val dx = abs(x - startX)
        val dy = abs(y - startY)
        // Ambiguous diagonal motion is consumed. Project allowed swipes to a
        // flat path so even a slightly diagonal horizontal drag cannot move Home.
        val duration = (now - startedAt).coerceIn(60L, 800L)
        if (maxOf(dx, dy) <= slop) return Forward(startX, startY, startX, startY, duration)
        if (dx > slop && dx >= dy * 1.3f) return Forward(startX, startY, x, startY, duration)
        // A downward pull may refresh Home. Ambiguous diagonals remain blocked.
        if (y - startY > slop && dy >= dx * 1.3f) return Forward(startX, startY, startX, y, duration)
        return null
    }
}
