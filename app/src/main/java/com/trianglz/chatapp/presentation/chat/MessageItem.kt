package com.trianglz.chatapp.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.trianglz.chatapp.domain.model.Message
import com.trianglz.chatapp.domain.model.MessageStatus
import com.trianglz.chatapp.presentation.util.DateTimeFormatter

/**
 * Individual message item component
 */
@Composable
fun MessageItem(
    message: Message,
    isOwnMessage: Boolean,
    onRetryClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        if (!isOwnMessage) {
            UserAvatar(
                userName = message.senderName,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
        ) {
            if (!isOwnMessage) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp, start = 12.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = if (isOwnMessage) 16.dp else 4.dp,
                    topEnd = if (isOwnMessage) 4.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = if (isOwnMessage) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Media items
                    if (message.mediaItems.isNotEmpty()) {
                        message.mediaItems.forEach { mediaItem ->
                            // For local URIs (during upload), use AsyncImage
                            // For Firebase media IDs (after upload), show placeholder
                            // Note: In production, implement proper Firebase image loading
                            if (mediaItem.localUri != null) {
                                AsyncImage(
                                    model = mediaItem.localUri,
                                    contentDescription = "Message media",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .padding(bottom = if (message.content.isNotEmpty()) 8.dp else 0.dp),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Placeholder for Firebase-stored images
                                // In production, use FirebaseImageLoader
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(bottom = if (message.content.isNotEmpty()) 8.dp else 0.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "📷 Image",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Text content
                    if (message.content.isNotEmpty()) {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isOwnMessage) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatTimestamp(message.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOwnMessage) {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            }
                        )

                        // Status indicator for own messages
                        if (isOwnMessage) {
                            when (message.status) {
                                MessageStatus.SENDING -> {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Sending",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                                MessageStatus.SENT -> {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Sent",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                MessageStatus.FAILED -> {
                                    IconButton(
                                        onClick = onRetryClick,
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Failed - Tap to retry",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isOwnMessage) {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

/**
 * User avatar component
 */
@Composable
fun UserAvatar(
    userName: String,
    modifier: Modifier = Modifier
) {
    val initials = userName.trim()
        .split(" ")
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .take(2)
        .joinToString("")
        .ifEmpty { userName.take(2).uppercase() }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

/**
 * Format timestamp to readable format
 */
private fun formatTimestamp(timestamp: Long): String {
    return DateTimeFormatter.formatMessageTimestamp(timestamp)
}