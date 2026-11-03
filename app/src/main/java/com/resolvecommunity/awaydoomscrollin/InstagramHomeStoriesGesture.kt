package com.resolvecommunity.awaydoomscrollin

import kotlin.math.abs

/** Completed gestures only. An upward start cannot become a forwarded swipe. */
internal class InstagramHomeStoriesGesture(private val slop: Float) {
    data class Forward(val x: Float, val y: Float, val endX: Float, val endY: Float, val durationMs: Long) {
        fun forStories(stories: InstagramGeometrySample, screenBottom: Int, refreshTop: Int = stories.top): Forward? {
            if (stories.right - stories.left < 2 || stories.bottom - stories.top < 2) return null
            if (endY > y) {
                // A refresh has no point-specific target. Use the fixed toolbar
                // boundary even when the tray shifts beneath a stationary finger.
                val startX = (stories.left + stories.right) / 2f
                if (refreshTop < 0 || refreshTop > stories.top) return null
                // Keep DOWN below the fixed toolbar and above the moving cover.
                // The tray may settle upward during the input-window handoff.
                val startY = refreshTop + maxOf(1, (stories.bottom - stories.top) / 12).toFloat()
                if (startY >= screenBottom - 1) return null
                return copy(x = startX, y = startY, endX = startX,
                    endY = (startY + endY - y).coerceAtMost(screenBottom - 1f))
            }
            // Blank refresh space is not a Story/avatar tap target.
            if (x < stories.left || x >= stories.right || y < stories.top || y >= stories.bottom) return null
            return copy(endX = endX.coerceIn(stories.left + 1f, stories.right - 1f), endY = y)
        }
    }
    private var startX = 0f
    private var startY = 0f
    private var startedAt = 0L
    private var active = false
    val isTracking: Boolean get() = active
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
