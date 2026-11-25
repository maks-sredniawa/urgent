package app.urgent

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import app.urgent.data.AppPreferences

/**
 * Kotlin note: `by preferencesDataStore(...)` is a property delegate on `Context` — it creates the
 * store once per process and hands the same instance to every caller. Creating a second DataStore
 * over the same file throws, so this must stay the only one.
 */
internal val Context.preferencesStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

internal val Context.recentCallsStore: DataStore<Preferences> by preferencesDataStore(name = "recent_calls")

/**
 * Manual dependency injection — deliberately not Hilt. The whole object graph is this file, and only
 * this file calls a constructor; a class that reaches for a singleton instead stops being testable
 * without a device.
 */
class AppContainer(
    val preferences: AppPreferences,
)
