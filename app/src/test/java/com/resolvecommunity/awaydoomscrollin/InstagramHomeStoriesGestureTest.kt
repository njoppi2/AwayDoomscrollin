package com.resolvecommunity.awaydoomscrollin

import org.junit.Assert.*
import org.junit.Test

class InstagramHomeStoriesGestureTest {
    @Test fun neitherVerticalDirectionCanBecomeAnAllowedHorizontalSwipe() {
        for (y in listOf(20f, 180f)) {
            val g = InstagramHomeStoriesGesture(12f)
            g.begin(100f, 100f, 0); g.move(102f, y)
            assertNull(g.finish(400f, y, 300))
        }
    }
    @Test fun upwardAndDownwardDragsAreConsumed() {
        for (y in listOf(-200f, 40f, 180f, 400f)) {
            val g = InstagramHomeStoriesGesture(12f)
            g.begin(100f, 100f, 0)
            assertNull(g.finish(100f, y, 300))
        }
    }
    @Test fun horizontalSwipeIsFlatAndForwardsOnlyOnce() {
        val g = InstagramHomeStoriesGesture(12f)
        g.begin(100f, 100f, 10)
        val result = g.finish(400f, 130f, 310)!!
        assertEquals(100f, result.y)
        assertEquals(400f, result.endX)
        assertEquals(300L, result.durationMs)
        assertNull(g.finish(500f, 100f, 400))
    }
    @Test fun diagonalDragIsConsumed() {
        for (y in listOf(10f, 190f)) {
            val g = InstagramHomeStoriesGesture(12f)
            g.begin(100f, 100f, 0)
            assertNull(g.finish(200f, y, 300))
        }
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
    @Test fun blankSpaceCannotOpenAStoryOrForwardASidewaysSwipe() {
        val stories = InstagramGeometrySample(0, 300, 1080, 500)
        assertNull(InstagramHomeStoriesGesture.Forward(100f, 200f, 100f, 80L).forStories(stories))
        assertNull(InstagramHomeStoriesGesture.Forward(100f, 200f, 400f, 300L).forStories(stories))
    }
    @Test fun horizontalReplayStaysInsideTheVerifiedTray() {
        val native = InstagramHomeStoriesGesture.Forward(100f, 350f, 1500f, 300L)
            .forStories(InstagramGeometrySample(0, 300, 1080, 500))!!
        assertEquals(350f, native.y)
        assertEquals(1079f, native.endX)
    }
}
