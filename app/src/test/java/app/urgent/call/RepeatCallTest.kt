package app.urgent.call

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RepeatCallTest {
    private val now = 1_000_000_000L
    private val minute = 60_000L
    private val exact: (String, String) -> Boolean = { a, b -> a == b }

    private fun repeat(
        vararg earlier: RecentCall,
        window: Int = 15,
        incoming: String = "600100200",
    ) = repeatCallAgeMillis(incoming, earlier.toList(), now, window, exact) != null

    private fun call(
        minutesAgo: Long,
        number: String = "600100200",
    ) = RecentCall(number, now - minutesAgo * minute)

    @Test fun `a call from the same number inside the window is a repeat`() = assertTrue(repeat(call(3)))

    @Test fun `the window edge still counts`() = assertTrue(repeat(call(15)))

    @Test fun `a call older than the window is not a repeat`() = assertFalse(repeat(call(16)))

    @Test fun `the window is the chosen one`() = assertFalse(repeat(call(6), window = 5))

    @Test fun `a different number is not a repeat`() = assertFalse(repeat(call(3, number = "700100200")))

    @Test fun `a call dated in the future does not count`() = assertFalse(repeat(call(-1)))

    @Test fun `no earlier calls is not a repeat`() = assertFalse(repeat())

    @Test fun `number matching is delegated`() =
        assertTrue(
            repeatCallAgeMillis("+48600100200", listOf(call(3)), now, 15) { a, b ->
                a.takeLast(9) == b.takeLast(9)
            } !=
                null,
        )

    @Test fun `the age is the newest matching call's`() =
        assertEquals(4 * minute, repeatCallAgeMillis("600100200", listOf(call(12), call(4), call(20)), now, 30, exact))

    @Test fun `no repeat has no age`() = assertNull(repeatCallAgeMillis("600100200", listOf(call(31)), now, 30, exact))

    @Test fun `Do Not Disturb sees an earlier call beyond a shorter window`() {
        val earlier = listOf(call(12))
        assertNull(repeatCallAgeMillis("600100200", earlier, now, 5, exact))
        assertEquals(12 * minute, dndRepeatCallAgeMillis("600100200", earlier, now, exact))
    }

    @Test fun `Do Not Disturb ignores a call older than its threshold`() =
        assertNull(dndRepeatCallAgeMillis("600100200", listOf(call(16)), now, exact))

    @Test fun `the record keeps the widest window and drops older or future calls`() =
        assertEquals(listOf(call(30), call(0)), keepRecent(listOf(call(31), call(30), call(0), call(-1)), now))

    @Test fun `an entry survives encoding`() =
        assertEquals(call(3, "+48 600 100 200"), decodeRecentCall(encodeRecentCall(call(3, "+48 600 100 200"))))

    @Test fun `a malformed entry is dropped`() {
        assertNull(decodeRecentCall("not a call"))
        assertNull(decodeRecentCall("123|"))
        assertNull(decodeRecentCall("|600100200"))
    }
}
