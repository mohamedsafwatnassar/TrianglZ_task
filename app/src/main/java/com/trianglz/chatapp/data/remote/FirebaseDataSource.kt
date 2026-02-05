package com.trianglz.chatapp.data.remote

import com.google.firebase.database.*
import com.trianglz.chatapp.data.dto.MessageDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Realtime Database data source
 * Handles all Firebase operations
 */
@Singleton
class FirebaseDataSource @Inject constructor(
    private val database: FirebaseDatabase
) {
    private val messagesRef: DatabaseReference = database.getReference("messages")
    private val typingRef: DatabaseReference = database.getReference("typing")

    companion object {
        private const val CHAT_ROOM = "global_chat"
    }

    /**
     * Observe messages in real-time
     */
    fun observeMessages(limit: Int = 30): Flow<List<MessageDto>> = callbackFlow {
        val query = messagesRef
            .child(CHAT_ROOM)
            .orderByChild("timestamp")
            .limitToLast(limit)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { childSnapshot ->
                    childSnapshot.getValue(MessageDto::class.java)
                }.sortedByDescending { it.timestamp }

                trySend(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        query.addValueEventListener(listener)

        awaitClose { query.removeEventListener(listener) }
    }

    /**
     * Send a message to Firebase
     */
    suspend fun sendMessage(message: MessageDto) {
        messagesRef
            .child(CHAT_ROOM)
            .child(message.id)
            .setValue(message)
            .await()
    }

    /**
     * Update message status
     */
    suspend fun updateMessageStatus(messageId: String, status: String) {
        messagesRef
            .child(CHAT_ROOM)
            .child(messageId)
            .child("status")
            .setValue(status)
            .await()
    }

    /**
     * Delete a message
     */
    suspend fun deleteMessage(messageId: String) {
        messagesRef
            .child(CHAT_ROOM)
            .child(messageId)
            .removeValue()
            .await()
    }

    /**
     * Load older messages for pagination
     */
    suspend fun loadOlderMessages(beforeTimestamp: Long, limit: Int = 30): List<MessageDto> {
        val snapshot = messagesRef
            .child(CHAT_ROOM)
            .orderByChild("timestamp")
            .endBefore(beforeTimestamp.toDouble())
            .limitToLast(limit)
            .get()
            .await()

        return snapshot.children.mapNotNull { childSnapshot ->
            childSnapshot.getValue(MessageDto::class.java)
        }.sortedByDescending { it.timestamp }
    }

    /**
     * Observe typing users
     */
    fun observeTypingUsers(): Flow<Set<String>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val typingUsers = snapshot.children.mapNotNull { childSnapshot ->
                    if (childSnapshot.getValue(Boolean::class.java) == true) {
                        childSnapshot.key
                    } else {
                        null
                    }
                }.toSet()

                trySend(typingUsers)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        typingRef.child(CHAT_ROOM).addValueEventListener(listener)

        awaitClose { typingRef.child(CHAT_ROOM).removeEventListener(listener) }
    }

    /**
     * Update typing status
     */
    suspend fun updateTypingStatus(userId: String, isTyping: Boolean) {
        if (isTyping) {
            typingRef
                .child(CHAT_ROOM)
                .child(userId)
                .setValue(true)
                .await()
        } else {
            typingRef
                .child(CHAT_ROOM)
                .child(userId)
                .removeValue()
                .await()
        }
    }
}