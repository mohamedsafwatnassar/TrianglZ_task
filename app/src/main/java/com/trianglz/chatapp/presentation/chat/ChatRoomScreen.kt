package com.trianglz.chatapp.presentation.chat

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trianglz.chatapp.domain.model.MediaItem
import com.trianglz.chatapp.domain.model.Message
import com.trianglz.chatapp.presentation.chat.viewmodel.ChatEvent
import com.trianglz.chatapp.presentation.chat.viewmodel.ChatViewModel

/**
 * Chat room screen
 * Main screen showing the message list and input
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current

    // Media picker
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val mediaItems = uris.map { uri ->
                MediaItem(
                    localUri = uri.toString(),
                    mimeType =  getMimeType(context = context ,uri) ?: "image/*"
                )
            }
            viewModel.onEvent(ChatEvent.MediaSelected(mediaItems))
        }
    }

    // Show error snackbar
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.onEvent(ChatEvent.ClearError)
        }
    }

    // Auto-scroll to bottom on new messages
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty() && listState.firstVisibleItemIndex < 3) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat Room") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            MessageInputBar(
                messageText = state.messageText,
                selectedMedia = state.selectedMedia,
                onMessageTextChanged = { viewModel.onEvent(ChatEvent.MessageTextChanged(it)) },
                onSendClick = { viewModel.onEvent(ChatEvent.SendMessage) },
                onMediaClick = {
                    mediaPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                onMediaRemoved = { index ->
                    viewModel.onEvent(ChatEvent.MediaRemoved(index))
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading && state.messages.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                MessageList(
                    messages = state.messages,
                    currentUserId = state.currentUser?.id,
                    listState = listState,
                    isLoadingMore = state.isLoadingMore,
                    hasReachedEnd = state.hasReachedEnd,
                    onLoadMore = { viewModel.onEvent(ChatEvent.LoadOlderMessages) },
                    onRetryMessage = { message ->
                        viewModel.onEvent(ChatEvent.RetryMessage(message))
                    },
                    onDeleteMessage = { messageId ->
                        viewModel.onEvent(ChatEvent.DeleteMessage(messageId))
                    }
                )
            }
        }
    }
}

/**
 * Helper function to get MIME type from URI
 */
private fun getMimeType(context: Context, uri: Uri): String? {
    return context.contentResolver.getType(uri)
}
/*@Composable
private fun getMimeType(uri: Uri): String? {
    val contentResolver = context.current.contentResolver
    return contentResolver.getType(uri)
}*/

/**
 * Message list component
 */
@Composable
private fun MessageList(
    messages: List<Message>,
    currentUserId: String?,
    listState: LazyListState,
    isLoadingMore: Boolean,
    hasReachedEnd: Boolean,
    onLoadMore: () -> Unit,
    onRetryMessage: (Message) -> Unit,
    onDeleteMessage: (String) -> Unit
) {
    LazyColumn(
        state = listState,
        reverseLayout = true,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(messages, key = { it.id }) { message ->
            MessageItem(
                message = message,
                isOwnMessage = message.senderId == currentUserId,
                onRetryClick = { onRetryMessage(message) },
                onDeleteClick = { onDeleteMessage(message.id) }
            )
        }

        // Load more indicator
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            }
        }

        // Trigger load more
        if (!isLoadingMore && !hasReachedEnd && messages.isNotEmpty()) {
            item {
                LaunchedEffect(Unit) {
                    onLoadMore()
                }
            }
        }

        // End of messages indicator
        if (hasReachedEnd && messages.isNotEmpty()) {
            item {
                Text(
                    text = "No more messages",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}