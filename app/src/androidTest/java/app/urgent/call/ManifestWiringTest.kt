package app.urgent.call

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.telecom.CallScreeningService
import android.telephony.TelephonyManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * What Android reads off the installed manifest. Nothing in Kotlin references the receiver — the OS
 * finds it by its intent filter — so a renamed class or a dropped filter compiles cleanly and leaves
 * the app deaf to every call.
 */
@RunWith(AndroidJUnit4::class)
class ManifestWiringTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun the_receiver_hears_phone_state() {
        val receivers =
            context.packageManager.queryBroadcastReceivers(
                Intent(TelephonyManager.ACTION_PHONE_STATE_CHANGED).setPackage(context.packageName),
                0,
            )
        val names = receivers.map { it.activityInfo.name }
        assertTrue("PHONE_STATE reaches $names", PhoneStateReceiver::class.java.name in names)
        assertTrue("the telephony process is another app", receivers.single().activityInfo.exported)
    }

    @Test
    fun telecom_finds_the_screening_service_and_only_telecom_may_bind_it() {
        val services =
            context.packageManager.queryIntentServices(
                Intent(CallScreeningService.SERVICE_INTERFACE).setPackage(context.packageName),
                0,
            )
        val service = services.single().serviceInfo
        assertEquals(RepeatCallScreeningService::class.java.name, service.name)
        assertTrue("Telecom is another process", service.exported)
        assertEquals(Manifest.permission.BIND_SCREENING_SERVICE, service.permission)
    }

    @Test
    fun the_call_log_permission_is_not_requested() {
        val requested =
            context.packageManager
                .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
                .requestedPermissions
                .orEmpty()
        assertFalse(Manifest.permission.READ_CALL_LOG in requested)
    }

    @Test
    fun the_ringing_service_is_a_media_playback_foreground_service() {
        val info =
            context.packageManager.getServiceInfo(
                ComponentName(context, RingThroughService::class.java),
                PackageManager.GET_META_DATA,
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            assertEquals(ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK, info.foregroundServiceType)
        }
    }

    @Test
    fun the_keep_alive_service_outlives_the_app_leaving_recents() {
        val info =
            context.packageManager.getServiceInfo(
                ComponentName(context, KeepAliveService::class.java),
                0,
            )
        assertEquals(0, info.flags and ServiceInfo.FLAG_STOP_WITH_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            assertEquals(ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE, info.foregroundServiceType)
        }
    }

    @Test
    fun the_boot_receiver_is_not_exported_and_hears_boot_and_update() {
        val receiver = ComponentName(context, KeepAliveRestartReceiver::class.java)
        assertFalse(context.packageManager.getReceiverInfo(receiver, 0).exported)
        for (action in listOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED)) {
            val names =
                context.packageManager
                    .queryBroadcastReceivers(Intent(action).setPackage(context.packageName), 0)
                    .map { it.activityInfo.name }
            assertTrue("$action reaches $names", KeepAliveRestartReceiver::class.java.name in names)
        }
    }

    @Test
    fun the_boot_permission_is_requested() {
        val requested =
            context.packageManager
                .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
                .requestedPermissions
                .orEmpty()
        assertTrue(Manifest.permission.RECEIVE_BOOT_COMPLETED in requested)
    }
}
