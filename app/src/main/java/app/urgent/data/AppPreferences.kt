package app.urgent.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Which colour scheme applies, regardless of what the phone is set to. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

/** Which saved contacts may ring a repeated call, as Do Not Disturb's *Calls from* choice does. */
enum class ContactScope {
    OFF,
    EVERYONE,
    STARRED,
}

/**
 * Who may ring a repeated call. [chosen] holds one encoded entry per caller and is ignored, but kept,
 * while [useChosen] is off. [contactScope] admits saved contacts. The defaults let anyone ring.
 * [ringAtOnce] callers, encoded the same way, ring on their first call whatever the other rules say.
 */
data class CallerRules(
    val chosen: Set<String> = emptySet(),
    val useChosen: Boolean = true,
    val contactScope: ContactScope = ContactScope.OFF,
    val ringAtOnce: Set<String> = emptySet(),
)

/**
 * Everything the app remembers. Settings only — there is no database.
 *
 * **DataStore, not SharedPreferences.** SharedPreferences' `commit()` blocks and its `apply()` can
 * still write on the main thread at the wrong moment; DataStore is `suspend`-only and transactional,
 * so there is no API here that can stall a frame.
 *
 * Kotlin note: every read is a `Flow`, not a value. There is no "current value" to ask for
 * synchronously — the first emission arrives after a disk read. That matters at startup: a screen
 * collecting `themeMode` gets `SYSTEM` for one frame and the stored value on the next, which is a
 * visible flash. [themeModeNow] exists for the one caller that must have the answer *before* the
 * first frame, and it is `suspend` because reading a file is.
 *
 * ## Adding a preference
 *
 * A key, a `Flow` that reads it with a default, and a `suspend` setter. Keep the default in the
 * `Flow` rather than writing it on first launch — a default that has been written to disk cannot be
 * changed in a later version without a migration, and a default that has not can.
 */
class AppPreferences(
    private val store: DataStore<Preferences>,
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val MATERIAL_YOU = booleanPreferencesKey("material_you")
        val RING_THROUGH = booleanPreferencesKey("ring_through")
        val REPEAT_WINDOW_MINUTES = intPreferencesKey("repeat_window_minutes")
        val COUNT_DECLINED_CALLS = booleanPreferencesKey("count_declined_calls")
        val CHOSEN_CALLERS = stringSetPreferencesKey("chosen_callers")
        val USE_CHOSEN_CALLERS = booleanPreferencesKey("use_chosen_callers")
        val CONTACT_SCOPE = stringPreferencesKey("contact_scope")
        val RING_AT_ONCE_CALLERS = stringSetPreferencesKey("ring_at_once_callers")
        val KEEP_ALIVE = booleanPreferencesKey("keep_alive")
        val PAUSED_UNTIL_MILLIS = longPreferencesKey("paused_until_millis")
    }

    /**
     * Light, dark or follow-the-system.
     *
     * Unknown stored values fall back to [ThemeMode.SYSTEM] rather than throwing: a preference file
     * survives a downgrade, and a crash loop at startup is a far worse outcome than the wrong theme.
     * This is the opposite of the rule for enums stored in the *database*, where an unknown value
     * means the schema gate missed something and should be loud.
     */
    val themeMode: Flow<ThemeMode> =
        store.data.map { prefs ->
            prefs[Keys.THEME_MODE]?.let { name ->
                runCatching { enumValueOf<ThemeMode>(name) }.getOrNull()
            } ?: ThemeMode.SYSTEM
        }

    /**
     * Whether to take the wallpaper's palette instead of the app's own (ADR-0006).
     *
     * Defaults **off**. With it on, nothing on Android 12+ reads the generated scheme at all, so the
     * app's identity would be invisible on almost every device that runs it.
     */
    val materialYou: Flow<Boolean> = store.data.map { it[Keys.MATERIAL_YOU] ?: false }

    /**
     * Whether a repeated call should ring through a silenced phone.
     *
     * Separate from the phone and call-log permissions on purpose, and both are checked when a call
     * arrives. The permissions answer "may we", this answers "did they ask us to".
     */
    val ringThrough: Flow<Boolean> = store.data.map { it[Keys.RING_THROUGH] ?: false }

    /**
     * How recent the earlier call must be for this one to count as a repeat. 15 is Android's own
     * figure for Do Not Disturb's repeat callers; an unknown stored value falls back to it.
     */
    val repeatWindowMinutes: Flow<Int> =
        store.data.map { prefs ->
            prefs[Keys.REPEAT_WINDOW_MINUTES]?.takeIf { it in REPEAT_WINDOW_CHOICES } ?: DEFAULT_REPEAT_WINDOW_MINUTES
        }

    /**
     * Whether a call the user declined counts as the earlier call. Defaults **on**: a decline is often
     * a reflex at an unknown number, and the second attempt is the one Urgent exists for.
     */
    val countDeclinedCalls: Flow<Boolean> = store.data.map { it[Keys.COUNT_DECLINED_CALLS] ?: true }

    /**
     * The caller rules, read from one snapshot so the lists, the switch and the contacts setting always
     * agree with each other.
     */
    val callerRules: Flow<CallerRules> =
        store.data.map { prefs ->
            CallerRules(
                chosen = prefs[Keys.CHOSEN_CALLERS] ?: emptySet(),
                useChosen = prefs[Keys.USE_CHOSEN_CALLERS] ?: true,
                contactScope =
                    prefs[Keys.CONTACT_SCOPE]?.let { name ->
                        runCatching { enumValueOf<ContactScope>(name) }.getOrNull()
                    }
                        ?: ContactScope.OFF,
                ringAtOnce = prefs[Keys.RING_AT_ONCE_CALLERS] ?: emptySet(),
            )
        }

    /**
     * Whether the app runs as a foreground service all the time, so the phone-state receiver never
     * waits for Android to start the process again. Off by default: it shows a notification.
     */
    val keepAlive: Flow<Boolean> = store.data.map { it[Keys.KEEP_ALIVE] ?: false }

    /**
     * Whether the keep-alive service should run. It keeps the ringing ready, so it is idle while
     * [ringThrough] is off. Kotlin note: `combine` is RxJS's `combineLatest`, so it re-emits when either switch changes.
     */
    val keepAliveActive: Flow<Boolean> = combine(ringThrough, keepAlive) { ring, keep -> ring && keep }

    /**
     * When the running pause ends, in wall-clock milliseconds; judge it with [isPaused]. A time rather
     * than a switch because Auto Backup carries it to the user's next phone, where a time has already
     * passed and a switch would keep the app silent with nothing on screen to explain why.
     */
    val pausedUntilMillis: Flow<Long> = store.data.map { it[Keys.PAUSED_UNTIL_MILLIS] ?: 0L }

    /**
     * The theme mode, read once, before any Activity exists.
     *
     * The one place a `suspend` read is worth its cost: `MainApplication.onCreate` needs the answer
     * to call `applyThemeMode` *before* the first window is created, and a `Flow` collected in a
     * composition arrives a frame too late — which is a light flash on every cold start for a user
     * who chose dark.
     */
    suspend fun themeModeNow(): ThemeMode = themeMode.first()

    suspend fun setThemeMode(mode: ThemeMode) {
        store.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setMaterialYou(enabled: Boolean) {
        store.edit { it[Keys.MATERIAL_YOU] = enabled }
    }

    suspend fun setRingThrough(enabled: Boolean) {
        store.edit { it[Keys.RING_THROUGH] = enabled }
    }

    suspend fun setRepeatWindowMinutes(minutes: Int) {
        store.edit { it[Keys.REPEAT_WINDOW_MINUTES] = minutes }
    }

    suspend fun setCountDeclinedCalls(enabled: Boolean) {
        store.edit { it[Keys.COUNT_DECLINED_CALLS] = enabled }
    }

    suspend fun setUseChosenCallers(enabled: Boolean) {
        store.edit { it[Keys.USE_CHOSEN_CALLERS] = enabled }
    }

    suspend fun setContactScope(scope: ContactScope) {
        store.edit { it[Keys.CONTACT_SCOPE] = scope.name }
    }

    suspend fun setKeepAlive(enabled: Boolean) {
        store.edit { it[Keys.KEEP_ALIVE] = enabled }
    }

    suspend fun setPausedUntilMillis(millis: Long) {
        store.edit { it[Keys.PAUSED_UNTIL_MILLIS] = millis }
    }

    suspend fun addChosenCaller(entry: String) {
        store.edit { it[Keys.CHOSEN_CALLERS] = (it[Keys.CHOSEN_CALLERS] ?: emptySet()) + entry }
    }

    suspend fun removeChosenCaller(entry: String) {
        store.edit { it[Keys.CHOSEN_CALLERS] = (it[Keys.CHOSEN_CALLERS] ?: emptySet()) - entry }
    }

    suspend fun addRingAtOnceCaller(entry: String) {
        store.edit { it[Keys.RING_AT_ONCE_CALLERS] = (it[Keys.RING_AT_ONCE_CALLERS] ?: emptySet()) + entry }
    }

    suspend fun removeRingAtOnceCaller(entry: String) {
        store.edit { it[Keys.RING_AT_ONCE_CALLERS] = (it[Keys.RING_AT_ONCE_CALLERS] ?: emptySet()) - entry }
    }
}

const val DEFAULT_REPEAT_WINDOW_MINUTES = 15

val REPEAT_WINDOW_CHOICES = listOf(5, 10, 15, 30)

val PAUSE_HOUR_CHOICES = listOf(1, 2, 4, 8)

private const val MILLIS_PER_HOUR = 3_600_000L

fun pauseEndMillis(
    hours: Int,
    nowMillis: Long,
): Long = nowMillis + hours * MILLIS_PER_HOUR

/**
 * Whether a pause is running. An end further away than the longest pause can only come from a clock
 * that moved back, and counts as over: an unwanted ring costs less than a missed urgent call.
 */
fun isPaused(
    pausedUntilMillis: Long,
    nowMillis: Long,
): Boolean = pausedUntilMillis - nowMillis in 1..PAUSE_HOUR_CHOICES.max() * MILLIS_PER_HOUR
