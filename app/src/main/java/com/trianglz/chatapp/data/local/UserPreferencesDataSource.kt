package com.trianglz.chatapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.trianglz.chatapp.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Local data source using DataStore for user preferences
 */
@Singleton
class UserPreferencesDataSource @Inject constructor(
    private val context: Context
) {
    private object PreferencesKeys {
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
    }

    /**
     * Get current user from preferences
     */
    fun getCurrentUser(): Flow<User?> {
        return context.dataStore.data.map { preferences ->
            val userId = preferences[PreferencesKeys.USER_ID]
            val userName = preferences[PreferencesKeys.USER_NAME]

            if (userId != null && userName != null) {
                User(id = userId, name = userName)
            } else {
                null
            }
        }
    }

    /**
     * Save user to preferences
     */
    suspend fun saveUser(user: User) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = user.id
            preferences[PreferencesKeys.USER_NAME] = user.name
        }
    }

    /**
     * Check if user is setup
     */
    suspend fun isUserSetup(): Boolean {
        var isSetup = false
        context.dataStore.data.collect { preferences ->
            isSetup = preferences[PreferencesKeys.USER_NAME] != null
        }
        return isSetup
    }
}