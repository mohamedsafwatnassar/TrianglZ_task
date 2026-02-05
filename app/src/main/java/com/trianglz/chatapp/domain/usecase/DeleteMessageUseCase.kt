package com.trianglz.chatapp.domain.usecase

import com.trianglz.chatapp.domain.repository.MessageRepository
import javax.inject.Inject

/**
 * Use case for deleting a message
 */
class DeleteMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(messageId: String) {
        messageRepository.deleteMessage(messageId)
    }
}