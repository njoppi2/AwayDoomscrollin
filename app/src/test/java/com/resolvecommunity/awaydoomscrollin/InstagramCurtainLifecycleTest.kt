package com.resolvecommunity.awaydoomscrollin

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.Looper
import android.view.View
import android.view.accessibility.AccessibilityEvent
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@LooperMode(LooperMode.Mode.PAUSED)
class InstagramCurtainLifecycleTest {
    class TestService : AccessibilityService() {
        override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
        override fun onInterrupt() = Unit
    }

    private lateinit var service: TestService
    private lateinit var overlay: InstagramFeedOverlayController

    @Before fun prepare() {
        service = Robolectric.buildService(TestService::class.java).create().get()
        overlay = InstagramFeedOverlayController(service, {})
    }

    @After fun closeWindows() { overlay.hideAll() }

    private fun show(heightDp: Int, screen: InstagramScreen = InstagramScreen.HOME_FEED) {
        val density = service.resources.displayMetrics.density
        val height = (heightDp * density).toInt()
        assertTrue(overlay.show(Rect(0, 0, 1080, height), screen, true))
    }

    private fun view(field: String): View =
        InstagramFeedOverlayController::class.java.getDeclaredField(field).run {
            isAccessible = true
            get(overlay) as View
        }

    private fun nextFrame() { shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(32)) }

    @Test fun shortInitialCurtainHidesCopyImmediately() {
        show(100)
        assertEquals(View.GONE, view("shieldContent").visibility)
    }

    @Test fun shrinkingCurtainHidesCopyInTheSameUpdate() {
        show(500)
        show(100)
        assertEquals(View.GONE, view("shieldContent").visibility)
    }

    @Test fun expandingCurtainRestoresCopyInTheSameUpdate() {
        show(500)
        show(100)
        show(100)
        show(500)
        assertEquals(View.VISIBLE, view("shieldContent").visibility)
    }

    @Test fun identicalUpdatesReuseTheAttachedCurtain() {
        show(500)
        val original = view("overlay")
        repeat(5) { show(500) }
        assertSame(original, view("overlay"))
        assertEquals(Rect(0, 0, 1080, (500 * service.resources.displayMetrics.density).toInt()),
            overlay.visibleCurtain()?.second)
    }

    @Test fun hideAndRestoreCannotDispatchAnOldDrawAction() {
        show(500)
        val original = view("overlay")
        var calls = 0
        overlay.afterCurtainDraw { calls++ }
        original.viewTreeObserver.dispatchOnPreDraw()
        overlay.hide()
        show(500)
        assertSame(original, view("overlay"))
        nextFrame()
        assertEquals(0, calls)
    }

    @Test fun shadeAndResumeCannotDispatchAnOldDrawAction() {
        show(500)
        var calls = 0
        overlay.afterCurtainDraw { calls++ }
        view("overlay").viewTreeObserver.dispatchOnPreDraw()
        overlay.suspendForNotificationShade()
        show(500)
        nextFrame()
        assertEquals(0, calls)
    }

    @Test fun hideBeforePreDrawAlsoCancelsTheOldAction() {
        show(500)
        var calls = 0
        overlay.afterCurtainDraw { calls++ }
        overlay.hide()
        show(500)
        view("overlay").viewTreeObserver.dispatchOnPreDraw()
        nextFrame()
        assertEquals(0, calls)
    }

    @Test fun geometryUpdateOnTheSameSurfacePreservesThePendingAction() {
        show(500)
        var calls = 0
        overlay.afterCurtainDraw { calls++ }
        show(450)
        view("overlay").viewTreeObserver.dispatchOnPreDraw()
        nextFrame()
        assertEquals(1, calls)
    }

    @Test fun surfaceChangeCannotDispatchAnOldDrawAction() {
        show(500)
        var calls = 0
        overlay.afterCurtainDraw { calls++ }
        view("overlay").viewTreeObserver.dispatchOnPreDraw()
        show(500, InstagramScreen.EXPLORE)
        nextFrame()
        assertEquals(0, calls)
    }

    @Test fun currentDrawActionWaitsForFrameAndRunsOnce() {
        show(500)
        var calls = 0
        overlay.afterCurtainDraw { calls++ }
        view("overlay").viewTreeObserver.dispatchOnPreDraw()
        assertEquals(0, calls)
        nextFrame()
        assertEquals(1, calls)
        view("overlay").viewTreeObserver.dispatchOnPreDraw()
        nextFrame()
        assertEquals(1, calls)
    }

    @Test fun verticalSwipesInsideCoveredHomeAreConsumedAndNeverReplayed() {
        var nativeScrolls = 0
        overlay = InstagramFeedOverlayController(service, {}, onProfileSwipeUp = { nativeScrolls++ },
            onEntryGuardVerticalSwipe = { _, _, _ -> nativeScrolls++ })
        show(500)
        val root = view("overlay")
        for ((action, y) in listOf(android.view.MotionEvent.ACTION_DOWN to 400f,
                android.view.MotionEvent.ACTION_MOVE to 100f, android.view.MotionEvent.ACTION_UP to 100f)) {
            val event = android.view.MotionEvent.obtain(0, 0, action, 50f, y, 0)
            try { assertTrue(root.dispatchTouchEvent(event)) } finally { event.recycle() }
        }
        assertEquals(0, nativeScrolls)
    }
    @Test fun storiesGuardConsumesUpwardDragAndSurvivesIdenticalGeometryUpdates() {
        val forwarded = mutableListOf<InstagramHomeStoriesGesture.Forward>()
        val bounds = Rect(0, 100, 1080, 400)
        overlay.updateHomeStoriesGuard(bounds) { forwarded.add(it) }
        val guard = InstagramFeedOverlayController::class.java.getDeclaredField("homeStoriesWindow")
            .apply { isAccessible = true }.get(overlay) as Pair<*, *>
        val view = guard.first as View
        fun touch(action: Int, x: Float, y: Float, at: Long) =
            android.view.MotionEvent.obtain(0L, at, action, x, y, 0).let {
                assertTrue(view.dispatchTouchEvent(it)); it.recycle()
            }
        touch(android.view.MotionEvent.ACTION_DOWN, 100f, 250f, 0)
        overlay.updateHomeStoriesGuard(bounds) { forwarded.add(it) }
        touch(android.view.MotionEvent.ACTION_MOVE, 100f, 100f, 100)
        touch(android.view.MotionEvent.ACTION_UP, 100f, 50f, 200)
        assertTrue(forwarded.isEmpty())
        touch(android.view.MotionEvent.ACTION_DOWN, 800f, 200f, 300)
        touch(android.view.MotionEvent.ACTION_UP, 200f, 210f, 500)
        assertEquals(1, forwarded.size)
        assertEquals(200f, forwarded.single().y)
        overlay.hide()
        assertNull(InstagramFeedOverlayController::class.java.getDeclaredField("homeStoriesWindow")
            .apply { isAccessible = true }.get(overlay))
    }

    private fun movingHomeTouch(endX: Float, endY: Float): List<InstagramHomeStoriesGesture.Forward> {
        val forwarded = mutableListOf<InstagramHomeStoriesGesture.Forward>()
        overlay.updateHomeStoriesGuard(Rect(0, 100, 1080, 400)) { forwarded.add(it) }
        val field = InstagramFeedOverlayController::class.java.getDeclaredField("homeStoriesWindow").apply { isAccessible = true }
        val window = field.get(overlay) as Pair<*, *>
        val guard = window.first as View
        fun touch(action: Int, x: Float, y: Float, at: Long) {
            val event = android.view.MotionEvent.obtain(0L, at, action, x, y, 0)
            guard.dispatchTouchEvent(event); event.recycle()
        }
        touch(android.view.MotionEvent.ACTION_DOWN, 100f, 200f, 0)
        overlay.updateHomeStoriesGuard(Rect(0, 100, 1080, 500)) { forwarded.add(it) }
        assertEquals(Rect(0, 100, 1080, 500), (field.get(overlay) as Pair<*, *>).second)
        touch(android.view.MotionEvent.ACTION_UP, endX, endY, 400)
        return forwarded
    }
    @Test fun verticalDragsStayConsumedWhenNativeLayoutChangesUnderTheFinger() {
        assertTrue(movingHomeTouch(100f, 500f).isEmpty())
        overlay.updateHomeStoriesGuard(null)
        assertTrue(movingHomeTouch(100f, 0f).isEmpty())
    }
    @Test fun movingRowCannotRetargetATapOrSidewaysSwipeToAnotherStory() {
        assertTrue(movingHomeTouch(100f, 200f).isEmpty())
        overlay.updateHomeStoriesGuard(null)
        assertTrue(movingHomeTouch(400f, 200f).isEmpty())
    }

}
