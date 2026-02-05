package com.trianglz.chatapp.data.repositoryImpl

import com.trianglz.chatapp.data.local.UserPreferencesDataSource
import com.trianglz.chatapp.data.remote.FirebaseDataSource
import com.trianglz.chatapp.domain.model.User
import com.trianglz.chatapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UserRepository
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val firebaseDataSource: FirebaseDataSource
) : UserRepository {

    override fun getCurrentUser(): Flow<User?> {
        return userPreferencesDataSource.getCurrentUser()
    }

    override suspend fun saveUser(user: User) {
        userPreferencesDataSource.saveUser(user)
    }

    override suspend fun isUserSetup(): Boolean {
        return userPreferencesDataSource.isUserSetup()
    }

    override fun observeTypingUsers(): Flow<Set<String>> {
        return firebaseDataSource.observeTypingUsers()
    }

    override suspend fun updateTypingStatus(isTyping: Boolean) {
        val user = getCurrentUser()
        // Note: This is a simplified implementation
        // In production, you'd want to handle this more robustly
    }
}