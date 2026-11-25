package app.urgent.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.urgent.MainApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class KeepAliveRestartReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        val app = context.applicationContext as MainApplication
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (app.preferences.keepAliveActive.first()) KeepAliveService.start(app)
                if (app.preferences.ringThrough.first() && app.setupMissing()) app.showSetupReminder()
            } finally {
                pending.finish()
            }
        }
    }
}
