package app.urgent.call

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupTest {
    @Test
    fun `complete once the phone permission and the role are both held`() {
        assertFalse(setupIncomplete(supported = true, callPermissions = true, screeningRole = true))
    }

    @Test
    fun `incomplete without the phone permission`() {
        assertTrue(setupIncomplete(supported = true, callPermissions = false, screeningRole = true))
    }

    @Test
    fun `incomplete without the screening role`() {
        assertTrue(setupIncomplete(supported = true, callPermissions = true, screeningRole = false))
    }

    @Test
    fun `never incomplete where repeated calls are unsupported`() {
        assertFalse(setupIncomplete(supported = false, callPermissions = false, screeningRole = false))
    }
}
