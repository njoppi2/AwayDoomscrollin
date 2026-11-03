package com.resolvecommunity.awaydoomscrollin

import kotlin.math.abs

/** Completed taps and horizontal swipes only; vertical movement is never replayed. */
internal class InstagramHomeStoriesGesture(private val slop: Float) {
    // A flat path by construction: there is no destination Y to forward.
    data class Forward(val x: Float, val y: Float, val endX: Float, val durationMs: Long) {
        fun forStories(stories: InstagramGeometrySample): Forward? {
            if (stories.right - stories.left < 2 || stories.bottom - stories.top < 2) return null
            // Blank space below the toolbar is not an avatar tap target.
            if (x < stories.left || x >= stories.right || y < stories.top || y >= stories.bottom) return null
            return copy(endX = endX.coerceIn(stories.left + 1f, stories.right - 1f))
        }
    }
    private var startX = 0f
    private var startY = 0f
    private var startedAt = 0L
    private var active = false
    private var vertical = false

    fun begin(x: Float, y: Float, now: Long) {
        startX = x; startY = y; startedAt = now
        active = true; vertical = false
    }
    fun move(x: Float, y: Float) {
        if (active && abs(y - startY) > slop && abs(y - startY) >= abs(x - startX)) vertical = true
    }
    fun cancel() { active = false }
    fun finish(x: Float, y: Float, now: Long): Forward? {
        if (!active) return null
        move(x, y)
        active = false
        if (vertical) return null
        val dx = abs(x - startX)
        val dy = abs(y - startY)
        val duration = (now - startedAt).coerceIn(60L, 800L)
        if (maxOf(dx, dy) <= slop) return Forward(startX, startY, startX, duration)
        // Consume ambiguous diagonals and flatten permitted sideways motion.
        if (dx > slop && dx >= dy * 1.3f) return Forward(startX, startY, x, duration)
        return null
    }
}
