package com.trianglz.chatapp.domain.usecase

import com.trianglz.chatapp.domain.model.Message
import com.trianglz.chatapp.domain.repository.MessageRepository
import javax.inject.Inject

/**
 * Use case for loading older messages (pagination)
 */
class LoadOlderMessagesUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(beforeTimestamp: Long, limit: Int = 30): List<Message> {
        return messageRepository.loadOlderMessages(beforeTimestamp, limit)
    }
}