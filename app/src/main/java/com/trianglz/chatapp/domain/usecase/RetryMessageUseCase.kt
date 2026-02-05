package com.trianglz.chatapp.domain.usecase

import com.trianglz.chatapp.domain.model.Message
import com.trianglz.chatapp.domain.model.MessageStatus
import com.trianglz.chatapp.domain.repository.MessageRepository
import javax.inject.Inject

/**
 * Use case for retrying a failed message
 */
class RetryMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(message: Message) {
        // Reset status to sending and resend
        val retryMessage = message.copy(
            status = MessageStatus.SENDING,
            timestamp = System.currentTimeMillis()
        )
        messageRepository.sendMessage(retryMessage)
    }
}