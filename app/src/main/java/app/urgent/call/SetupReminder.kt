package app.urgent.call

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import app.urgent.MainActivity
import app.urgent.R

/**
 * Whether Urgent, switched on, still lacks what it needs to hear a call. An update to 1.2.0 leaves a
 * user in exactly that state: they switched on before the call-screening role existed (ADR 0007).
 */
fun Context.setupMissing(): Boolean =
    setupIncomplete(repeatedCallsSupported(), hasCallPermissions(), holdsScreeningRole())

/** Says so in a notification, because someone updated from Play will not open the app to find out. */
fun Context.showSetupReminder() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    val notifications = getSystemService(NotificationManager::class.java) ?: return
    notifications.createNotificationChannel(
        NotificationChannel(
            SETUP_CHANNEL_ID,
            getString(R.string.channel_setup_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = getString(R.string.channel_setup_description) },
    )
    val open =
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
    notifications.notify(
        SETUP_NOTIFICATION_ID,
        NotificationCompat
            .Builder(this, SETUP_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.home_permissions_missing_title))
            .setContentText(getString(R.string.setup_reminder_body))
            .setStyle(NotificationCompat.BigTextStyle().bigText(getString(R.string.setup_reminder_body)))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build(),
    )
}

fun Context.clearSetupReminder() {
    getSystemService(NotificationManager::class.java)?.cancel(SETUP_NOTIFICATION_ID)
}

private const val SETUP_CHANNEL_ID = "setup"
private const val SETUP_NOTIFICATION_ID = 3
