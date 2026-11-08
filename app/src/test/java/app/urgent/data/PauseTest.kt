package app.urgent.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PauseTest {
    private val now = 1_000_000_000L

    @Test
    fun `a pause runs from the moment it is set until its end`() {
        assertTrue(isPaused(pauseEndMillis(1, now), now))
        assertTrue(isPaused(pauseEndMillis(1, now), pauseEndMillis(1, now) - 1))
    }

    @Test
    fun `a pause is over at its end`() {
        assertFalse(isPaused(pauseEndMillis(1, now), pauseEndMillis(1, now)))
    }

    @Test
    fun `the longest pause counts`() {
        assertTrue(isPaused(pauseEndMillis(PAUSE_HOUR_CHOICES.max(), now), now))
    }

    @Test
    fun `no pause was ever set`() {
        assertFalse(isPaused(0L, now))
    }

    @Test
    fun `an end beyond the longest pause means the clock moved back and the app rings`() {
        assertFalse(isPaused(pauseEndMillis(PAUSE_HOUR_CHOICES.max(), now) + 1, now))
    }
}
