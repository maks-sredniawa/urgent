package app.urgent.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import app.urgent.call.RecentCall
import app.urgent.call.decodeRecentCall
import app.urgent.call.encodeRecentCall
import app.urgent.call.keepRecent

/**
 * The incoming calls Urgent saw ring in the last half hour, in place of the call log (ADR 0007).
 *
 * Its own DataStore file, apart from the settings, because it is live state: the backup rules leave
 * this file out, so a restored phone starts with an empty record rather than another phone's calls.
 */
class RecentCalls(
    private val store: DataStore<Preferences>,
) {
    private val calls = stringSetPreferencesKey("calls")

    /**
     * Adds a call ringing now and returns the calls that came before it. One `edit` is one
     * transaction, so two calls arriving together cannot each miss the other.
     */
    suspend fun record(
        number: String,
        nowMillis: Long,
    ): List<RecentCall> {
        var earlier = emptyList<RecentCall>()
        store.edit { stored ->
            earlier = keepRecent(stored[calls].orEmpty().mapNotNull(::decodeRecentCall), nowMillis)
            stored[calls] = (earlier + RecentCall(number, nowMillis)).map(::encodeRecentCall).toSet()
        }
        return earlier
    }

    suspend fun clear() {
        store.edit { it.remove(calls) }
    }
}
