package app.urgent.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import app.urgent.MainApplication
import kotlinx.coroutines.launch

/**
 * Hears every change of call state. It stops the ring once a call is answered or ends, and on
 * Android 8.x it is also how the ringing number arrives.
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

        // Android 9 stopped putting the number here for apps without READ_CALL_LOG, which Urgent no
        // longer asks for (ADR 0007), so from then on the screening service hears the number instead.
        @Suppress("DEPRECATION")
        val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        if (number.isNullOrBlank()) return

        val app = context.applicationContext as MainApplication
        // `goAsync` keeps the broadcast open after `onReceive` returns, so the reads can run off the
        // main thread. Without it Android considers the receiver finished — and the process killable —
        // the moment this function returns. It is roughly a promise the system waits on, and
        // `finish()` is its resolve; it must be called on every path.
        val pending = goAsync()
        app.callScope.launch {
            try {
                val ring = app.decideRingThrough(number) ?: return@launch
                // The reads take a moment; a caller who gave up meanwhile has already sent the idle
                // broadcast, and starting now would ring for a call that no longer exists.
                if (app.isCallStillThere()) RingThroughService.start(app, ring.firstCall)
            } finally {
                pending.finish()
            }
        }
    }
}
