package app.urgent.call

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.annotation.RequiresApi
import app.urgent.MainApplication
import kotlinx.coroutines.launch

/**
 * How the ringing number reaches Urgent from Android 10, while it holds the call-screening role
 * (ADR 0007). Telecom binds this for every incoming call before the phone rings, and for a saved
 * contact only while Urgent holds `READ_CONTACTS`.
 *
 * It never screens anything out: the answer is always "allow", so the call rings, logs and notifies
 * exactly as it would without Urgent. The answer waits for the decision, though, because Android
 * lets an app start its ringing service while Telecom is bound to it and refuses once it has let go.
 */
@RequiresApi(Build.VERSION_CODES.Q)
class RepeatCallScreeningService : CallScreeningService() {
    override fun onScreenCall(details: Call.Details) {
        val number =
            details.handle
                ?.schemeSpecificPart
                ?.takeIf { details.callDirection == Call.Details.DIRECTION_INCOMING && it.isNotBlank() }
        if (number == null) {
            allow(details)
            return
        }
        val app = application as MainApplication
        app.callScope.launch {
            try {
                app.decideRingThrough(number)?.let { RingThroughService.start(app, it.firstCall) }
            } finally {
                allow(details)
            }
        }
    }

    private fun allow(details: Call.Details) = respondToCall(details, CallResponse.Builder().build())
}
