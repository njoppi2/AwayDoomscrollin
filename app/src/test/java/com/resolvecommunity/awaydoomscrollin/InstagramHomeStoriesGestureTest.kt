package com.resolvecommunity.awaydoomscrollin

import org.junit.Assert.*
import org.junit.Test

class InstagramHomeStoriesGestureTest {
    @Test fun upwardStartCannotBeConvertedToAllowedHorizontalSwipe() {
        val g = InstagramHomeStoriesGesture(12f)
        g.begin(100f, 100f, 0); g.move(102f, 20f)
        assertNull(g.finish(400f, 20f, 300))
    }
    @Test fun upwardDragsAreConsumed() {
        for (y in listOf(-200f, 40f)) {
            val g = InstagramHomeStoriesGesture(12f)
            g.begin(100f, 100f, 0)
            assertNull(g.finish(100f, y, 300))
        }
    }
    @Test fun horizontalSwipeHasNoVerticalComponentAndForwardsOnlyOnce() {
        val g = InstagramHomeStoriesGesture(12f)
        g.begin(100f, 100f, 10)
        val result = g.finish(400f, 130f, 310)!!
        assertEquals(100f, result.y)
        assertEquals(400f, result.endX)
        assertEquals(300L, result.durationMs)
        assertNull(g.finish(500f, 100f, 400))
    }
    @Test fun downwardPullIsAllowedWithNoHorizontalComponent() {
        val g = InstagramHomeStoriesGesture(12f)
        g.begin(100f, 100f, 0)
        val result = g.finish(110f, 400f, 300)!!
        assertEquals(result.x, result.endX)
        assertEquals(400f, result.endY)
    }
    @Test fun diagonalDragIsConsumed() {
        val g = InstagramHomeStoriesGesture(12f)
        g.begin(100f, 100f, 0)
        assertNull(g.finish(200f, 190f, 300))
    }
    @Test fun tapAndHeldPressKeepTheirDurationWithoutTurningIntoSwipes() {
        for (duration in listOf(80L, 600L)) {
            val g = InstagramHomeStoriesGesture(12f)
            g.begin(100f, 100f, 10)
            val result = g.finish(105f, 102f, 10 + duration)!!
            assertEquals(result.x, result.endX)
            assertEquals(duration, result.durationMs)
        }
    }
    @Test fun cancelledAndMultitouchGesturesNeverForward() {
        val g = InstagramHomeStoriesGesture(12f)
        g.begin(100f, 100f, 0); g.cancel()
        assertNull(g.finish(400f, 100f, 200))
    }
}
