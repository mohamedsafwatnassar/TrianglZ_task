package com.trianglz.chatapp.data.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.trianglz.chatapp.data.dto.MediaItemDto
import com.trianglz.chatapp.data.dto.MessageDto
import com.trianglz.chatapp.data.remote.FirebaseDataSource
import com.trianglz.chatapp.data.remote.FirebaseStorageDataSource
import com.trianglz.chatapp.domain.model.MessageStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker for sending messages reliably in the background
 * Uses WorkManager to ensure messages are sent even if app is closed
 */
@HiltWorker
class SendMessageWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firebaseDataSource: FirebaseDataSource,
    private val storageDataSource: FirebaseStorageDataSource
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_MESSAGE_ID = "message_id"
        const val KEY_SENDER_ID = "sender_id"
        const val KEY_SENDER_NAME = "sender_name"
        const val KEY_CONTENT = "content"
        const val KEY_TIMESTAMP = "timestamp"
        const val KEY_MEDIA_URIS = "media_uris"
        const val KEY_MEDIA_TYPES = "media_types"

        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "message_sending_channel"

        /**
         * Create work request for sending a message
         */
        fun createWorkRequest(
            messageId: String,
            senderId: String,
            senderName: String,
            content: String,
            timestamp: Long,
            mediaUris: List<String> = emptyList(),
            mediaTypes: List<String> = emptyList()
        ): OneTimeWorkRequest {
            val inputData = Data.Builder()
                .putString(KEY_MESSAGE_ID, messageId)
                .putString(KEY_SENDER_ID, senderId)
                .putString(KEY_SENDER_NAME, senderName)
                .putString(KEY_CONTENT, content)
                .putLong(KEY_TIMESTAMP, timestamp)
                .putStringArray(KEY_MEDIA_URIS, mediaUris.toTypedArray())
                .putStringArray(KEY_MEDIA_TYPES, mediaTypes.toTypedArray())
                .build()

            return OneTimeWorkRequestBuilder<SendMessageWorker>()
                .setInputData(inputData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    java.util.concurrent.TimeUnit.MILLISECONDS
                )
                .addTag(messageId)
                .build()
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Set foreground with notification
            setForeground(createForegroundInfo())

            val messageId =
                inputData.getString(KEY_MESSAGE_ID) ?: return@withContext Result.failure()
            val senderId = inputData.getString(KEY_SENDER_ID) ?: return@withContext Result.failure()
            val senderName =
                inputData.getString(KEY_SENDER_NAME) ?: return@withContext Result.failure()
            val content = inputData.getString(KEY_CONTENT) ?: ""
            val timestamp = inputData.getLong(KEY_TIMESTAMP, System.currentTimeMillis())
            val mediaUris = inputData.getStringArray(KEY_MEDIA_URIS)?.toList() ?: emptyList()
            val mediaTypes = inputData.getStringArray(KEY_MEDIA_TYPES)?.toList() ?: emptyList()

            // Upload media files if any
            val uploadedMedia = mutableListOf<MediaItemDto>()
            mediaUris.forEachIndexed { index, uriString ->
                try {
                    val uri = Uri.parse(uriString)
                    val mimeType = mediaTypes.getOrNull(index) ?: "application/octet-stream"

                    // Update notification for each media upload
                    setForeground(createForegroundInfo("Uploading media ${index + 1}/${mediaUris.size}"))

                    // Upload returns mediaId instead of URL
                    val mediaId = storageDataSource.uploadMedia(uri, mimeType)
                    uploadedMedia.add(
                        MediaItemDto(
                            id = java.util.UUID.randomUUID().toString(),
                            mediaId = mediaId,  // Store mediaId instead of remoteUrl
                            mimeType = mimeType
                        )
                    )
                } catch (e: Exception) {
                    // If one media fails, we can still send the message with other media
                    e.printStackTrace()
                }
            }

            // Create and send message
            val message = MessageDto(
                id = messageId,
                senderId = senderId,
                senderName = senderName,
                content = content,
                mediaItems = uploadedMedia,
                timestamp = timestamp,
                status = MessageStatus.SENT.name
            )

            setForeground(createForegroundInfo("Sending message..."))
            firebaseDataSource.sendMessage(message)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()

            // Update message status to failed
            try {
                val messageId = inputData.getString(KEY_MESSAGE_ID)
                if (messageId != null) {
                    firebaseDataSource.updateMessageStatus(messageId, MessageStatus.FAILED.name)
                }
            } catch (updateException: Exception) {
                updateException.printStackTrace()
            }

            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private fun createForegroundInfo(message: String = "Sending message..."): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Sending Message")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_menu_send)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }
}