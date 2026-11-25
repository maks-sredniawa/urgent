package app.urgent.call

/**
 * Whether switched-on Urgent still lacks what it needs to hear a call: the phone permission and the
 * call-screening role. On Android 9 nothing it could be granted would help, so nothing is missing.
 */
fun setupIncomplete(
    supported: Boolean,
    callPermissions: Boolean,
    screeningRole: Boolean,
): Boolean = supported && !(callPermissions && screeningRole)
