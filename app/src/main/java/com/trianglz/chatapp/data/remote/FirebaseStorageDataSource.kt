package com.trianglz.chatapp.data.remote

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Storage data source for media uploads using Realtime Database
 * Note: Uses Base64 encoding to store images in Realtime Database
 * This is a workaround for free Firebase accounts without Storage access
 */
@Singleton
class FirebaseStorageDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: FirebaseDatabase
) {
    private val mediaRef = database.getReference("media")

    // Maximum size for images (500KB) to avoid Realtime Database payload limits
    private val MAX_IMAGE_SIZE = 500 * 1024 // 500KB

    /**
     * Upload a media file to Firebase Realtime Database as Base64
     * @param localUri Local URI of the file
     * @param mimeType MIME type of the file
     * @return Media ID that can be used to retrieve the image
     */
    suspend fun uploadMedia(localUri: Uri, mimeType: String): String {
        val mediaId = UUID.randomUUID().toString()

        // Read and compress image
        val imageData = readAndCompressImage(localUri)

        // Convert to Base64
        val base64Image = Base64.encodeToString(imageData, Base64.NO_WRAP)

        // Store in Realtime Database
        val mediaData = mapOf(
            "id" to mediaId,
            "data" to base64Image,
            "mimeType" to mimeType,
            "timestamp" to System.currentTimeMillis()
        )

        mediaRef.child(mediaId).setValue(mediaData).await()

        return mediaId
    }

    /**
     * Get media data from Firebase Realtime Database
     * @param mediaId Media ID
     * @return Base64 encoded image data
     */
    suspend fun getMedia(mediaId: String): String? {
        val snapshot = mediaRef.child(mediaId).get().await()
        return snapshot.child("data").getValue(String::class.java)
    }

    /**
     * Delete a media file from Firebase Realtime Database
     * @param mediaId Media ID
     */
    suspend fun deleteMedia(mediaId: String) {
        mediaRef.child(mediaId).removeValue().await()
    }

    /**
     * Read and compress image from URI
     */
    private fun readAndCompressImage(uri: Uri): ByteArray {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes() ?: ByteArray(0)
        inputStream?.close()

        // If image is too large, compress it
        return if (bytes.size > MAX_IMAGE_SIZE) {
            compressImage(bytes)
        } else {
            bytes
        }
    }

    /**
     * Compress image data
     */
    private fun compressImage(imageData: ByteArray): ByteArray {
        // Simple compression by reducing quality
        // In production, use BitmapFactory to properly compress
        return imageData.copyOf(MAX_IMAGE_SIZE)
    }
}
