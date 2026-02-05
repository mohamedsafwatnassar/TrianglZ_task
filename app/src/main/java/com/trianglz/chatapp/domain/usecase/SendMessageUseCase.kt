package com.trianglz.chatapp.domain.usecase

import com.trianglz.chatapp.domain.model.Message
import com.trianglz.chatapp.domain.repository.MessageRepository
import javax.inject.Inject

/**
 * Use case for sending a message
 * Encapsulates the business logic for message sending
 */
class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(message: Message) {
        messageRepository.sendMessage(message)
    }
}