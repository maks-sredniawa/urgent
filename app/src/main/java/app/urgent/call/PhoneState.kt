package app.urgent.call

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.telecom.TelecomManager

/**
 * `READ_PHONE_STATE` delivers the phone-state broadcast at all: the end of a call, which stops the
 * ring, and on Android 8.x the ringing number too.
 */
val CALL_PERMISSIONS = arrayOf(Manifest.permission.READ_PHONE_STATE)

fun Context.hasCallPermissions(): Boolean =
    CALL_PERMISSIONS.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

/**
 * Android 10 added the call-screening role, through which Urgent hears the ringing number from then
 * on. Android 9 has neither the role nor the number in the broadcast, so there the rule cannot work.
 */
fun repeatedCallsSupported(): Boolean = Build.VERSION.SDK_INT != Build.VERSION_CODES.P

fun needsScreeningRole(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

fun Context.holdsScreeningRole(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        getSystemService(RoleManager::class.java)?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
    } else {
        true
    }

fun Context.hasContactsPermission(): Boolean =
    checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

/** A caller's standing in the contacts. A starred contact is a saved one too. */
enum class CallerContact { NONE, SAVED, STARRED }

/**
 * What the contacts say about a call from [number]. The platform matches the number itself, so
 * `+48 600…` and `600…` are the same phone.
 */
fun Context.callerContact(number: String): CallerContact {
    if (!hasContactsPermission()) return CallerContact.NONE
    return try {
        queryCallerContact(number)
    } catch (e: RuntimeException) {
        // The receiver's coroutine has no catch, so a provider failure here would crash the app.
        CallerContact.NONE
    }
}

/** Lets a provider failure escape; [callerContact] is the version the ring path uses. */
internal fun Context.queryCallerContact(number: String): CallerContact {
    val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
    val columns = arrayOf(ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.STARRED)
    val cursor = contentResolver.query(uri, columns, null, null, null) ?: return CallerContact.NONE
    val starredFlags = cursor.use { found -> buildList { while (found.moveToNext()) add(found.getInt(1) == 1) } }
    return when {
        starredFlags.any { it } -> CallerContact.STARRED
        starredFlags.isNotEmpty() -> CallerContact.SAVED
        else -> CallerContact.NONE
    }
}

/** The live sound settings [shouldRingThrough] decides from. */
@SuppressLint("InlinedApi")
fun Context.soundSettings(): SoundSettings? {
    val audio = getSystemService(AudioManager::class.java) ?: return null
    val notifications = getSystemService(NotificationManager::class.java) ?: return null
    // From Android 15 the consolidated policy is the one in force, with schedules and modes applied;
    // before it, the user's manual policy is the closest the platform will tell an app. Before
    // Android 9 the platform refuses the read without Do Not Disturb access, which Urgent never asks
    // for, so the defaults below stand in for it.
    val policy =
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                notifications.consolidatedNotificationPolicy
            } else {
                notifications.notificationPolicy
            }
        } catch (e: SecurityException) {
            null
        }
    // Before Android 9 priority mode always let alarms through, and the policy had no bit for it.
    val alarmsAlwaysAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.P
    val categories = policy?.priorityCategories ?: NotificationManager.Policy.PRIORITY_CATEGORY_ALARMS
    return SoundSettings(
        ringerMode = audio.ringerMode,
        ringMuted = audio.isStreamMute(AudioManager.STREAM_RING),
        ringVolume = audio.getStreamVolume(AudioManager.STREAM_RING),
        audioMode = audio.mode,
        interruptionFilter = notifications.currentInterruptionFilter,
        priorityCategories =
            if (alarmsAlwaysAllowed) categories or NotificationManager.Policy.PRIORITY_CATEGORY_ALARMS else categories,
        priorityCallSenders = policy?.priorityCallSenders ?: NotificationManager.Policy.PRIORITY_SENDERS_STARRED,
    )
}

/** Whether a call is still ringing or connected — `isRinging` itself is not public API. */
fun Context.isCallStillThere(): Boolean =
    try {
        getSystemService(TelecomManager::class.java)?.isInCall == true
    } catch (e: SecurityException) {
        false
    }
