package com.trianglz.chatapp.domain.repository

import com.trianglz.chatapp.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user operations
 */
interface UserRepository {
    /**
     * Get the current user
     * @return Flow of current user or null if not set
     */
    fun getCurrentUser(): Flow<User?>

    /**
     * Save user information
     * @param user User to save
     */
    suspend fun saveUser(user: User)

    /**
     * Check if user is set up
     * @return true if username exists
     */
    suspend fun isUserSetup(): Boolean

    /**
     * Observe typing status of other users
     * @return Flow of user IDs currently typing
     */
    fun observeTypingUsers(): Flow<Set<String>>

    /**
     * Update typing status for current user
     * @param isTyping Whether the user is currently typing
     */
    suspend fun updateTypingStatus(isTyping: Boolean)
}