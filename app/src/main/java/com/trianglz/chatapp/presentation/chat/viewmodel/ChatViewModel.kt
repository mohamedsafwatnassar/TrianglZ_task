package com.trianglz.chatapp.presentation.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.trianglz.chatapp.data.worker.SendMessageWorker
import com.trianglz.chatapp.domain.model.MediaItem
import com.trianglz.chatapp.domain.model.Message
import com.trianglz.chatapp.domain.model.MessageStatus
import com.trianglz.chatapp.domain.model.User
import com.trianglz.chatapp.domain.repository.UserRepository
import com.trianglz.chatapp.domain.usecase.DeleteMessageUseCase
import com.trianglz.chatapp.domain.usecase.LoadOlderMessagesUseCase
import com.trianglz.chatapp.domain.usecase.ObserveMessagesUseCase
import com.trianglz.chatapp.domain.usecase.RetryMessageUseCase
import com.trianglz.chatapp.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for chat room screen
 * Follows MVI pattern with unidirectional data flow
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val observeMessagesUseCase: ObserveMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val loadOlderMessagesUseCase: LoadOlderMessagesUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val retryMessageUseCase: RetryMessageUseCase,
    private val userRepository: UserRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var currentUser: User? = null

    init {
        observeCurrentUser()
        observeMessages()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser().collect { user ->
                currentUser = user
                _state.update { it.copy(currentUser = user) }
            }
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            observeMessagesUseCase()
                .catch { e ->
                    _state.update { it.copy(error = "Failed to load messages: ${e.message}") }
                }
                .collect { messages ->
                    _state.update {
                        it.copy(
                            messages = messages,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.MessageTextChanged -> {
                _state.update { it.copy(messageText = event.text) }
            }
            is ChatEvent.MediaSelected -> {
                val currentMedia = _state.value.selectedMedia.toMutableList()
                currentMedia.addAll(event.mediaItems)
                _state.update { it.copy(selectedMedia = currentMedia) }
            }
            is ChatEvent.MediaRemoved -> {
                val currentMedia = _state.value.selectedMedia.toMutableList()
                currentMedia.removeAt(event.index)
                _state.update { it.copy(selectedMedia = currentMedia) }
            }
            is ChatEvent.SendMessage -> {
                sendMessage()
            }
            is ChatEvent.RetryMessage -> {
                retryMessage(event.message)
            }
            is ChatEvent.DeleteMessage -> {
                deleteMessage(event.messageId)
            }
            is ChatEvent.LoadOlderMessages -> {
                loadOlderMessages()
            }
            is ChatEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun sendMessage() {
        val user = currentUser ?: return
        val text = _state.value.messageText.trim()
        val mediaItems = _state.value.selectedMedia

        if (text.isEmpty() && mediaItems.isEmpty()) return

        viewModelScope.launch {
            try {
                val message = Message(
                    senderId = user.id,
                    senderName = user.name,
                    content = text,
                    mediaItems = mediaItems,
                    status = MessageStatus.SENDING
                )

                // Queue the work using WorkManager
                val workRequest = SendMessageWorker.createWorkRequest(
                    messageId = message.id,
                    senderId = message.senderId,
                    senderName = message.senderName,
                    content = message.content,
                    timestamp = message.timestamp,
                    mediaUris = mediaItems.mapNotNull { it.localUri },
                    mediaTypes = mediaItems.map { it.mimeType }
                )

                workManager.enqueue(workRequest)

                // Clear input
                _state.update {
                    it.copy(
                        messageText = "",
                        selectedMedia = emptyList()
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to send message: ${e.message}") }
            }
        }
    }

    private fun retryMessage(message: Message) {
        viewModelScope.launch {
            try {
                retryMessageUseCase(message)
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to retry message: ${e.message}") }
            }
        }
    }

    private fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            try {
                deleteMessageUseCase(messageId)
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to delete message: ${e.message}") }
            }
        }
    }

    private fun loadOlderMessages() {
        val messages = _state.value.messages
        if (messages.isEmpty() || _state.value.isLoadingMore || _state.value.hasReachedEnd) return

        val oldestTimestamp = messages.lastOrNull()?.timestamp ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }

            try {
                val olderMessages = loadOlderMessagesUseCase(oldestTimestamp)

                if (olderMessages.isEmpty()) {
                    _state.update { it.copy(hasReachedEnd = true, isLoadingMore = false) }
                } else {
                    _state.update { it.copy(isLoadingMore = false) }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoadingMore = false,
                        error = "Failed to load older messages: ${e.message}"
                    )
                }
            }
        }
    }
}

/**
 * State for chat screen
 */
data class ChatState(
    val currentUser: User? = null,
    val messages: List<Message> = emptyList(),
    val messageText: String = "",
    val selectedMedia: List<MediaItem> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasReachedEnd: Boolean = false,
    val error: String? = null,
    val typingUsers: Set<String> = emptySet()
)

/**
 * Events that can be triggered on chat screen
 */
sealed class ChatEvent {
    data class MessageTextChanged(val text: String) : ChatEvent()
    data class MediaSelected(val mediaItems: List<MediaItem>) : ChatEvent()
    data class MediaRemoved(val index: Int) : ChatEvent()
    data object SendMessage : ChatEvent()
    data class RetryMessage(val message: Message) : ChatEvent()
    data class DeleteMessage(val messageId: String) : ChatEvent()
    data object LoadOlderMessages : ChatEvent()
    data object ClearError : ChatEvent()
}