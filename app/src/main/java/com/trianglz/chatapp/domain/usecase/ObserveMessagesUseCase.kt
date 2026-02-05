package com.trianglz.chatapp.domain.usecase

import com.trianglz.chatapp.domain.model.Message
import com.trianglz.chatapp.domain.repository.MessageRepository
import javax.inject.Inject

/**
 * Use case for observing messages
 */
class ObserveMessagesUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    operator fun invoke(limit: Int = 30, lastMessageTimestamp: Long? = null) =
        messageRepository.observeMessages(limit, lastMessageTimestamp)
}