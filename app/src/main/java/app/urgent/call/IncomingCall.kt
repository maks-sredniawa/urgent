package app.urgent.call

import android.telephony.PhoneNumberUtils
import app.urgent.MainApplication
import app.urgent.data.ContactScope
import app.urgent.data.isPaused
import kotlinx.coroutines.flow.first

/** What Urgent does for a call it has decided to ring for. */
data class RingThrough(
    val firstCall: Boolean,
)

/**
 * Records one ringing call and decides whether Urgent rings for it, whichever way its number
 * arrived: the screening service on Android 10+, the phone-state broadcast on Android 8.x. `null`
 * means stay quiet.
 */
suspend fun MainApplication.decideRingThrough(number: String): RingThrough? {
    if (!preferences.ringThrough.first()) return null
    val now = System.currentTimeMillis()
    val earlier = recentCalls.record(number, now)
    if (isPaused(preferences.pausedUntilMillis.first(), now)) return null
    // Without the permission the contacts setting cannot apply, so it counts as Off, which admits
    // anyone while the chosen list is off or empty.
    val rules =
        preferences.callerRules.first().let {
            if (hasContactsPermission()) it else it.copy(contactScope = ContactScope.OFF)
        }
    val contactsOn = rules.contactScope != ContactScope.OFF
    val atOnce = isChosenCaller(number, rules.ringAtOnce, PhoneNumberUtils::compare)
    if (!atOnce &&
        !contactsOn &&
        !isAllowedCaller(number, rules, PhoneNumberUtils::compare, CallerContact.NONE)
    ) {
        return null
    }
    // A ring-at-once caller needs no earlier call, but Do Not Disturb's repeat-callers rule may still
    // let this one ring on its own, judged by its threshold rather than the user's.
    val repeatMillis =
        repeatCallAgeMillis(number, earlier, now, preferences.repeatWindowMinutes.first(), PhoneNumberUtils::compare)
    val dndMillis = dndRepeatCallAgeMillis(number, earlier, now, PhoneNumberUtils::compare)
    if (repeatMillis == null && !atOnce) return null
    // Contacts are read only for a call that may ring, and only while they are on.
    val contact = if (contactsOn) callerContact(number) else CallerContact.NONE
    if (!atOnce && !isAllowedCaller(number, rules, PhoneNumberUtils::compare, contact)) return null
    val sound = soundSettings() ?: return null
    if (!shouldRingThrough(sound, dndMillis, contact)) return null
    return RingThrough(firstCall = repeatMillis == null)
}
