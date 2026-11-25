package app.urgent

import android.app.Application
import app.urgent.data.AppPreferences
import app.urgent.data.RecentCalls
import app.urgent.data.ThemeMode
import app.urgent.theme.applyThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking

/**
 * Holds the one [AppContainer] for the process.
 *
 * The phone-state receiver reaches the preferences through here too: a broadcast can start the
 * process with no Activity at all, and this is the only object guaranteed to exist by then.
 */
class MainApplication : Application() {
    lateinit var preferences: AppPreferences
        private set

    lateinit var recentCalls: RecentCalls
        private set

    /**
     * Outlives any one broadcast or binding, so a call's checks can finish after the screening
     * service has answered Telecom. `SupervisorJob` keeps one failed call from cancelling the next.
     */
    val callScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * The theme mode as it was on disk at process start, so the very first composition paints in the
     * right palette rather than flashing light and repainting.
     */
    var startupThemeMode: ThemeMode = ThemeMode.SYSTEM
        private set

    val container: AppContainer by lazy { AppContainer(preferences) }

    override fun onCreate() {
        super.onCreate()

        preferences = AppPreferences(preferencesStore)
        recentCalls = RecentCalls(recentCallsStore)

        // A blocking read, deliberately and exactly once. Everything else about DataStore is a Flow,
        // but this value has to be known before the first window is created — a Flow collected in a
        // composition arrives a frame later, which is a visible light flash for a user who chose
        // dark.
        startupThemeMode = runBlocking { preferences.themeModeNow() }
        applyThemeMode(startupThemeMode)
    }
}
