package com.trianglz.chatapp.domain.repository

import com.trianglz.chatapp.domain.model.Message
import com.trianglz.chatapp.domain.model.MessageStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for message operations
 * Follows the Repository pattern for Clean Architecture
 */
interface MessageRepository {
    /**
     * Observe messages in real-time
     * @param limit Number of messages to load initially
     * @param lastMessageTimestamp Timestamp of the last message for pagination
     * @return Flow of message list
     */
    fun observeMessages(
        limit: Int = 30,
        lastMessageTimestamp: Long? = null
    ): Flow<List<Message>>

    /**
     * Send a text message
     * @param message Message to send
     */
    suspend fun sendMessage(message: Message)

    /**
     * Update message status
     * @param messageId ID of the message
     * @param status New status
     */
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus)

    /**
     * Delete a message
     * @param messageId ID of the message to delete
     */
    suspend fun deleteMessage(messageId: String)

    /**
     * Load older messages for pagination
     * @param beforeTimestamp Load messages before this timestamp
     * @param limit Number of messages to load
     * @return List of messages
     */
    suspend fun loadOlderMessages(beforeTimestamp: Long, limit: Int = 30): List<Message>
}