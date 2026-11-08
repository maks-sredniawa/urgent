package app.urgent.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import app.urgent.MainApplication
import app.urgent.data.ContactScope
import app.urgent.data.isPaused
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Hears every change of call state, and is the app's only way in while it is not open.
 *
 * Declared in the manifest rather than registered from code, so Android starts the process for it.
 * `PHONE_STATE` is one of the few broadcasts still delivered to a manifest receiver since Android 8;
 * on Xiaomi the ROM additionally refuses to start the process for it unless autostart is granted.
 */
class PhoneStateReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        if (intent.getStringExtra(TelephonyManager.EXTRA_STATE) != TelephonyManager.EXTRA_STATE_RINGING) {
            // Answered, declined or hung up: whatever we started ringing has no call left to announce.
            RingThroughService.stop(context)
            return
        }

        // Since Android 9 the broadcast arrives twice per call: once without the number, and once
        // with it for apps holding READ_CALL_LOG. Only the second one can be acted on.
        @Suppress("DEPRECATION")
        val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        if (number.isNullOrBlank()) return

        val app = context.applicationContext as MainApplication

        // `goAsync` keeps the broadcast open after `onReceive` returns, so the preference and
        // call-log reads can run off the main thread. Without it Android considers the receiver
        // finished — and the process killable — the moment this function returns. It is roughly a
        // promise the system waits on, and `finish()` is its resolve; it must be called on every path.
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val preferences = app.preferences
                if (!preferences.ringThrough.first()) return@launch
                if (isPaused(preferences.pausedUntilMillis.first(), System.currentTimeMillis())) return@launch
                // Without the permission the contacts setting cannot apply, so it counts as Off, which admits
                // anyone while the chosen list is off or empty.
                val rules =
                    preferences.callerRules.first().let {
                        if (app.hasContactsPermission()) it else it.copy(contactScope = ContactScope.OFF)
                    }
                val contactsOn = rules.contactScope != ContactScope.OFF
                val atOnce = isChosenCaller(number, rules.ringAtOnce, PhoneNumberUtils::compare)
                // Contacts off: the chosen list decides alone, so a caller it turns away costs no call-log read.
                if (!atOnce &&
                    !contactsOn &&
                    !isAllowedCaller(number, rules, PhoneNumberUtils::compare, CallerContact.NONE)
                ) {
                    return@launch
                }
                // A ring-at-once caller needs no earlier call, but Do Not Disturb's repeat-callers rule
                // may still let this one ring on its own, judged by its threshold rather than the user's.
                val earlierCall =
                    app.earlierCallAgesNow(
                        number,
                        preferences.repeatWindowMinutes.first(),
                        preferences.countDeclinedCalls.first(),
                    )
                if (earlierCall.repeatMillis == null && !atOnce) return@launch
                // Contacts are read only for a call that may ring, and only while they are on.
                val contact = if (contactsOn) app.callerContact(number) else CallerContact.NONE
                if (!atOnce && !isAllowedCaller(number, rules, PhoneNumberUtils::compare, contact)) return@launch
                val sound = app.soundSettings() ?: return@launch
                if (!shouldRingThrough(sound, earlierCall.dndMillis, contact)) return@launch
                // The reads above take a moment; a caller who gave up meanwhile has already sent the
                // idle broadcast, and starting now would ring for a call that no longer exists.
                if (!app.isCallStillThere()) return@launch
                RingThroughService.start(app, firstCall = earlierCall.repeatMillis == null)
            } finally {
                pending.finish()
            }
        }
    }
}
