package com.trianglz.chatapp.data.repositoryImpl

import com.trianglz.chatapp.data.dto.toDomain
import com.trianglz.chatapp.data.dto.toDto
import com.trianglz.chatapp.data.remote.FirebaseDataSource
import com.trianglz.chatapp.domain.model.Message
import com.trianglz.chatapp.domain.model.MessageStatus
import com.trianglz.chatapp.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of MessageRepository
 * Coordinates between remote and local data sources
 */
@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource
) : MessageRepository {

    override fun observeMessages(limit: Int, lastMessageTimestamp: Long?): Flow<List<Message>> {
        return firebaseDataSource.observeMessages(limit).map { dtoList ->
            dtoList.map { it.toDomain() }
        }
    }

    override suspend fun sendMessage(message: Message) {
        firebaseDataSource.sendMessage(message.toDto())
    }

    override suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {
        firebaseDataSource.updateMessageStatus(messageId, status.name)
    }

    override suspend fun deleteMessage(messageId: String) {
        firebaseDataSource.deleteMessage(messageId)
    }

    override suspend fun loadOlderMessages(beforeTimestamp: Long, limit: Int): List<Message> {
        return firebaseDataSource.loadOlderMessages(beforeTimestamp, limit).map { it.toDomain() }
    }
}