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
    @Test fun sameFingerPositionRefreshesEvenWhenTheTrayMovesBelowIt() {
        val pull = InstagramHomeStoriesGesture.Forward(100f, 200f, 100f, 500f, 400L)
        for (top in listOf(100, 300)) {
            val native = pull.forStories(InstagramGeometrySample(0, top, 1080, top + 200), 1920)!!
            assertEquals(300f, native.endY - native.y)
            assertTrue(native.y >= top)
            assertEquals(native.x, native.endX)
        }
    }
    @Test fun blankRefreshGapCannotOpenAStoryOrForwardASidewaysSwipe() {
        val stories = InstagramGeometrySample(0, 300, 1080, 500)
        assertNull(InstagramHomeStoriesGesture.Forward(100f, 200f, 100f, 200f, 80L).forStories(stories, 1920))
        assertNull(InstagramHomeStoriesGesture.Forward(100f, 200f, 400f, 200f, 300L).forStories(stories, 1920))
    }

    @Test fun refreshStartsBelowFixedToolbarEvenIfTraySettlesDuringTheHandoff() {
        val pull = InstagramHomeStoriesGesture.Forward(100f, 200f, 100f, 500f, 400L)
        val native = pull.forStories(InstagramGeometrySample(0, 482, 1080, 871), 1920, refreshTop = 266)!!
        assertTrue(native.y > 266)
        assertTrue(native.y < 655)
        assertEquals(300f, native.endY - native.y)
    }

}
