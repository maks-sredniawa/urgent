package app.urgent.call

import app.urgent.data.CallerRules
import app.urgent.data.ContactScope

/** An incoming call Urgent saw ring: the number as the network gave it, and when. */
data class RecentCall(
    val number: String,
    val atMillis: Long,
)

/**
 * How long ago [incoming] last called inside the window, or `null` when the call ringing now is not
 * a repeat. Every call Urgent saw ring counts, answered or not: without the call log there is no
 * telling a declined call from a missed one.
 *
 * [earlier] never holds the call ringing now, because the record is read before this call is added
 * to it. [sameNumber] is passed in because number matching is the platform's job (`+48 600…` and
 * `600…` are the same phone), and the platform's matcher is not callable from a JVM test.
 */
fun repeatCallAgeMillis(
    incoming: String,
    earlier: List<RecentCall>,
    nowMillis: Long,
    windowMinutes: Int,
    sameNumber: (String, String) -> Boolean,
): Long? =
    newestCall(incoming, earlier, nowMillis, windowMinutes * 60_000L, sameNumber)
        ?.let { call -> nowMillis - call.atMillis }

/**
 * How long ago [incoming] last called as Do Not Disturb's repeat-callers rule sees it, or `null` when
 * that rule will not let the call through. The rule has its own threshold, so this can differ from
 * [repeatCallAgeMillis] under the user's window.
 */
fun dndRepeatCallAgeMillis(
    incoming: String,
    earlier: List<RecentCall>,
    nowMillis: Long,
    sameNumber: (String, String) -> Boolean,
): Long? =
    newestCall(incoming, earlier, nowMillis, DND_REPEAT_CALLERS_MILLIS, sameNumber)
        ?.let { call -> nowMillis - call.atMillis }

private fun newestCall(
    incoming: String,
    earlier: List<RecentCall>,
    nowMillis: Long,
    windowMillis: Long,
    sameNumber: (String, String) -> Boolean,
): RecentCall? =
    earlier
        .filter { call -> sameNumber(call.number, incoming) }
        .filter { call -> nowMillis - call.atMillis in 0..windowMillis }
        .maxByOrNull { call -> call.atMillis }

/** The longest any rule looks back: the widest window the user can pick, or Do Not Disturb's. */
val RECENT_CALL_RETENTION_MILLIS = maxOf(30 * 60_000L, DND_REPEAT_CALLERS_MILLIS)

/** [calls] without anything older than [RECENT_CALL_RETENTION_MILLIS] or dated in the future. */
fun keepRecent(
    calls: List<RecentCall>,
    nowMillis: Long,
): List<RecentCall> = calls.filter { call -> nowMillis - call.atMillis in 0..RECENT_CALL_RETENTION_MILLIS }

/** One stored entry, `millis|number`: a number never contains `|`, so the split is unambiguous. */
fun encodeRecentCall(call: RecentCall): String = "${call.atMillis}|${call.number}"

fun decodeRecentCall(entry: String): RecentCall? {
    val millis = entry.substringBefore('|', "").toLongOrNull() ?: return null
    val number = entry.substringAfter('|', "").takeIf { it.isNotEmpty() } ?: return null
    return RecentCall(number, millis)
}

fun isChosenCaller(
    incoming: String,
    chosen: Set<String>,
    sameNumber: (String, String) -> Boolean,
): Boolean = chosen.any { entry -> ChosenCaller.fromEntry(entry).numbers.any { sameNumber(it, incoming) } }

/**
 * Whether a repeated call from [incoming] may ring. Nothing restricts anyone while the chosen list is
 * off or empty and the contacts setting is off. Otherwise a chosen caller may, and so may a contact
 * that [rules] admits: any saved one, or only a starred one.
 */
fun isAllowedCaller(
    incoming: String,
    rules: CallerRules,
    sameNumber: (String, String) -> Boolean,
    contact: CallerContact,
): Boolean {
    val listRestricts = rules.useChosen && rules.chosen.isNotEmpty()
    if (!listRestricts && rules.contactScope == ContactScope.OFF) return true
    if (rules.useChosen && isChosenCaller(incoming, rules.chosen, sameNumber)) return true
    return when (rules.contactScope) {
        ContactScope.OFF -> false
        ContactScope.EVERYONE -> contact != CallerContact.NONE
        ContactScope.STARRED -> contact == CallerContact.STARRED
    }
}
