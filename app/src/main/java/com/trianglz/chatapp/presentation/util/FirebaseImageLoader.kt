package com.trianglz.chatapp.presentation.util

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.trianglz.chatapp.data.remote.FirebaseStorageDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Composable function to load image from Firebase Realtime Database
 * Uses media ID to fetch Base64 encoded image data
 */
@Composable
fun rememberFirebaseImage(
    mediaId: String?,
    storageDataSource: FirebaseStorageDataSource
): ImageBitmap? {
    var imageBitmap by remember(mediaId) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(mediaId) {
        if (mediaId != null) {
            withContext(Dispatchers.IO) {
                try {
                    val base64Data = storageDataSource.getMedia(mediaId)
                    if (base64Data != null) {
                        val imageBytes = Base64.decode(base64Data, Base64.NO_WRAP)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        imageBitmap = bitmap?.asImageBitmap()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    return imageBitmap
}

/**
 * Helper class for loading images from Firebase
 */
class FirebaseImageLoader(
    private val storageDataSource: FirebaseStorageDataSource
) {
    suspend fun loadImage(mediaId: String): ImageBitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val base64Data = storageDataSource.getMedia(mediaId)
                if (base64Data != null) {
                    val imageBytes = Base64.decode(base64Data, Base64.NO_WRAP)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    bitmap?.asImageBitmap()
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}