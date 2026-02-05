package com.trianglz.chatapp.domain.model

import java.util.UUID

/**
 * Domain model representing a chat message
 */
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val senderName: String,
    val content: String,
    val mediaItems: List<MediaItem> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENDING
)

/**
 * Represents the status of a message
 */
enum class MessageStatus {
    SENDING,
    SENT,
    FAILED
}

/**
 * Represents a media item attached to a message
 */
data class MediaItem(
    val id: String = UUID.randomUUID().toString(),
    val localUri: String? = null,
    val remoteUrl: String? = null,
    val mimeType: String,
    val uploadStatus: UploadStatus = UploadStatus.PENDING
)

/**
 * Upload status for media items
 */
enum class UploadStatus {
    PENDING,
    UPLOADING,
    UPLOADED,
    FAILED
}