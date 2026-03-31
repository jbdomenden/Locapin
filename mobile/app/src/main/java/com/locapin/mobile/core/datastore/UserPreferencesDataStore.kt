package com.locapin.mobile.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.userPrefs by preferencesDataStore(name = "locapin_user_prefs")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    val hasCompletedOnboarding: Flow<Boolean> = context.userPrefs.data.map {
        it[ONBOARDING_KEY] ?: false
    }

    val authToken: Flow<String?> = context.userPrefs.data.map { it[TOKEN_KEY] }

    val recentSearches: Flow<List<String>> = context.userPrefs.data.map {
        it[RECENT_SEARCHES_KEY]?.split("||")?.filter(String::isNotBlank) ?: emptyList()
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.userPrefs.edit { it[ONBOARDING_KEY] = completed }
    }

    suspend fun setAuthToken(token: String?) {
        context.userPrefs.edit { prefs ->
            if (token.isNullOrBlank()) prefs.remove(TOKEN_KEY) else prefs[TOKEN_KEY] = token
        }
    }

    suspend fun addRecentSearch(query: String) {
        context.userPrefs.edit { prefs ->
            val current = prefs[RECENT_SEARCHES_KEY]
                ?.split("||")
                ?.filter(String::isNotBlank)
                ?.toMutableList()
                ?: mutableListOf()
            current.remove(query)
            current.add(0, query)
            prefs[RECENT_SEARCHES_KEY] = current.take(8).joinToString("||")
        }
    }

    suspend fun clearSession() {
        context.userPrefs.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }

    private companion object {
        val ONBOARDING_KEY: Preferences.Key<Boolean> = booleanPreferencesKey("onboarding_done")
        val TOKEN_KEY: Preferences.Key<String> = stringPreferencesKey("auth_token")
        val RECENT_SEARCHES_KEY: Preferences.Key<String> = stringPreferencesKey("recent_searches")
    }
}
