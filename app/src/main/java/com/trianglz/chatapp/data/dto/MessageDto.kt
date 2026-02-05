package com.trianglz.chatapp.data.dto

import com.trianglz.chatapp.domain.model.MediaItem
import com.trianglz.chatapp.domain.model.Message
import com.trianglz.chatapp.domain.model.MessageStatus
import com.trianglz.chatapp.domain.model.UploadStatus

/**
 * Firebase data transfer object for messages
 */
data class MessageDto(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val mediaItems: List<MediaItemDto> = emptyList(),
    val timestamp: Long = 0L,
    val status: String = "SENT"
)

/**
 * Firebase data transfer object for media items
 * Now stores media ID instead of URL (for Realtime Database storage)
 */
data class MediaItemDto(
    val id: String = "",
    val mediaId: String = "",  // Changed from remoteUrl to mediaId
    val mimeType: String = ""
)

/**
 * Extension function to convert MessageDto to domain Message
 */
fun MessageDto.toDomain(): Message {
    return Message(
        id = id,
        senderId = senderId,
        senderName = senderName,
        content = content,
        mediaItems = mediaItems.map { it.toDomain() },
        timestamp = timestamp,
        status = MessageStatus.valueOf(status)
    )
}

/**
 * Extension function to convert domain Message to MessageDto
 */
fun Message.toDto(): MessageDto {
    return MessageDto(
        id = id,
        senderId = senderId,
        senderName = senderName,
        content = content,
        mediaItems = mediaItems.filter { it.remoteUrl != null }.map { it.toDto() },
        timestamp = timestamp,
        status = status.name
    )
}

/**
 * Extension function to convert MediaItemDto to domain MediaItem
 */
fun MediaItemDto.toDomain(): MediaItem {
    return MediaItem(
        id = id,
        remoteUrl = mediaId,  // Store mediaId in remoteUrl field for compatibility
        mimeType = mimeType,
        uploadStatus = UploadStatus.UPLOADED
    )
}

/**
 * Extension function to convert domain MediaItem to MediaItemDto
 */
fun MediaItem.toDto(): MediaItemDto {
    return MediaItemDto(
        id = id,
        mediaId = remoteUrl ?: "",  // Use remoteUrl as mediaId
        mimeType = mimeType
    )
}