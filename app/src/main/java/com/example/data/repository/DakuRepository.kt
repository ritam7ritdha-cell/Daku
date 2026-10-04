package com.example.data.repository

import com.example.data.db.ChatDao
import com.example.data.db.ChatMessageEntity
import com.example.data.db.ChatSessionEntity
import com.example.data.db.MediaDao
import com.example.data.db.GeneratedMediaEntity
import com.example.data.db.ResearchDao
import com.example.data.db.ResearchNoteEntity
import kotlinx.coroutines.flow.Flow

class DakuRepository(
    private val chatDao: ChatDao,
    private val researchDao: ResearchDao,
    private val mediaDao: MediaDao,
    val creditRepository: CreditRepository? = null
) {
    // Chat Sessions
    fun getChatSessions(suite: String = "general"): Flow<List<ChatSessionEntity>> =
        chatDao.getSessionsBySuite(suite)

    suspend fun getSessionById(sessionId: String): ChatSessionEntity? =
        chatDao.getSessionById(sessionId)

    suspend fun saveChatSession(session: ChatSessionEntity) =
        chatDao.insertOrUpdateSession(session)

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteMessagesBySession(sessionId)
        chatDao.deleteSessionById(sessionId)
    }

    // Chat Messages
    fun getChatMessages(suite: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesBySuite(suite)

    fun getMessagesBySession(sessionId: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesBySession(sessionId)

    fun getBookmarkedMessages(): Flow<List<ChatMessageEntity>> =
        chatDao.getBookmarkedMessages()

    suspend fun saveChatMessage(message: ChatMessageEntity): Long {
        val id = chatDao.insertMessage(message)
        // Update parent session metadata if sessionId is not blank
        val session = chatDao.getSessionById(message.sessionId)
        if (session != null) {
            val count = chatDao.getSessionMessageCount(message.sessionId)
            val preview = if (message.content.length > 60) message.content.take(60) + "..." else message.content
            chatDao.insertOrUpdateSession(
                session.copy(
                    lastModified = System.currentTimeMillis(),
                    previewText = preview,
                    messageCount = count
                )
            )
        }
        return id
    }

    suspend fun deleteMessage(id: Long) =
        chatDao.deleteMessageById(id)

    suspend fun toggleMessageBookmark(id: Long, isBookmarked: Boolean) =
        chatDao.updateBookmark(id, isBookmarked)

    suspend fun clearChat(suite: String) =
        chatDao.clearSuiteMessages(suite)

    // Research Notes
    fun getAllResearchNotes(): Flow<List<ResearchNoteEntity>> =
        researchDao.getAllResearchNotes()

    suspend fun saveResearchNote(note: ResearchNoteEntity): Long =
        researchDao.insertNote(note)

    suspend fun deleteResearchNote(id: Long) =
        researchDao.deleteNoteById(id)

    // Media
    fun getMediaByType(type: String): Flow<List<GeneratedMediaEntity>> =
        mediaDao.getMediaByType(type)

    suspend fun saveMedia(media: GeneratedMediaEntity): Long =
        mediaDao.insertMedia(media)

    suspend fun deleteMedia(id: Long) =
        mediaDao.deleteMediaById(id)
}
