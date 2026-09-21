package com.edwin.bekal.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.edwin.bekal.data.dto.AuthSession
import com.edwin.bekal.data.dto.AuthUser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val AUTH_PREFERENCES_NAME = "auth_session"

private val Context.authPreferences: DataStore<Preferences> by preferencesDataStore(
    name = AUTH_PREFERENCES_NAME,
)

@Singleton
class AuthSessionLocalDataSource @Inject constructor(
    @ApplicationContext context: Context
) {

    private val dataStore = context.applicationContext.authPreferences

    // Flow untuk UI / StateManager
    fun observe(): Flow<AuthSession?> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }
        .map(::toSession)

    // Method langsung untuk Interceptor & Authenticator
    suspend fun getAccessToken(): String? = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.ACCESS_TOKEN] }
        .firstOrNull()

    suspend fun getRefreshToken(): String? = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.REFRESH_TOKEN] }
        .firstOrNull()

    suspend fun save(session: AuthSession) {
        dataStore.edit { preferences ->
            preferences[Keys.ACCESS_TOKEN] = session.accessToken
            session.refreshToken?.let { preferences[Keys.REFRESH_TOKEN] = it }
            preferences[Keys.EXPIRES_AT] = session.expiresAtMillis
            session.user?.let {
                preferences[Keys.USER_ID] = session.user.id
                preferences[Keys.USER_NAME] = session.user.name
                preferences[Keys.USER_EMAIL] = session.user.email
                preferences[Keys.USER_TIPE] = session.user.tipe.orEmpty()
                preferences[Keys.USER_ROLES] = session.user.roles.toSet()
            }
        }
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[Keys.ACCESS_TOKEN] = accessToken
            preferences[Keys.REFRESH_TOKEN] = refreshToken
        }
    }

    /**
     * Menghapus sesi login aktif, namun tetap mempertahankan
     * email yang disimpan via 'Remember Me'.
     */
    suspend fun clear() {
        dataStore.edit { preferences ->
            val rememberedEmail = preferences[Keys.REMEMBERED_EMAIL]
            preferences.clear()
            rememberedEmail?.let { preferences[Keys.REMEMBERED_EMAIL] = it }
        }
    }

    // --- Implementasi Remember Me ---

    fun saveRememberedEmail(email: String) {
        runBlocking(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[Keys.REMEMBERED_EMAIL] = email
            }
        }
    }

    fun getRememberedEmail(): String? = runBlocking(Dispatchers.IO) {
        dataStore.data
            .catch { emit(emptyPreferences()) }
            .firstOrNull()
            ?.get(Keys.REMEMBERED_EMAIL)
    }

    fun clearRememberedEmail() {
        runBlocking(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences.remove(Keys.REMEMBERED_EMAIL)
            }
        }
    }

    // ---------------------------------

    private fun toSession(preferences: Preferences): AuthSession? {
        val token = preferences[Keys.ACCESS_TOKEN] ?: return null
        val expiresAt = preferences[Keys.EXPIRES_AT] ?: 0L

        val email = preferences[Keys.USER_EMAIL]

        val user = if (email != null) {
            AuthUser(
                id = preferences[Keys.USER_ID].orEmpty(),
                name = preferences[Keys.USER_NAME] ?: email,
                email = email,
                tipe = preferences[Keys.USER_TIPE]?.takeIf { it.isNotBlank() },
                roles = preferences[Keys.USER_ROLES].orEmpty().toList(),
            )
        } else null

        return AuthSession(
            user = user,
            accessToken = token,
            refreshToken = preferences[Keys.REFRESH_TOKEN],
            expiresAtMillis = expiresAt,
        )
    }

    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_TIPE = stringPreferencesKey("user_tipe")
        val USER_ROLES = stringSetPreferencesKey("user_roles")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val EXPIRES_AT = longPreferencesKey("expires_at")
        val REMEMBERED_EMAIL = stringPreferencesKey("remembered_email")
    }
}