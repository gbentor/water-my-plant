package com.example.watermyplant.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore
    private var cachedToken: String? = null

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")

        suspend fun getToken(context: Context): String? {
            return context.dataStore.data.map { preferences ->
                preferences[TOKEN_KEY]
            }.first()
        }
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
        cachedToken = token
    }

    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
        cachedToken = null
    }

    fun getTokenFlow(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }

    fun getToken(): String? {
        return cachedToken
    }

    suspend fun initialize() {
        cachedToken = dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }.first()
    }
} 