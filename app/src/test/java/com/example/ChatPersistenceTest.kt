package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.ChatMessageEntity
import com.example.data.db.ChatSessionEntity
import com.example.data.db.DakuDatabase
import com.example.data.repository.DakuRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ChatPersistenceTest {

    private lateinit var database: DakuDatabase
    private lateinit var repository: DakuRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, DakuDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DakuRepository(
            chatDao = database.chatDao(),
            researchDao = database.researchDao(),
            mediaDao = database.mediaDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `test creating and retrieving persistent chat session in Room`() = runBlocking {
        val session = ChatSessionEntity(
            sessionId = "session_test_01",
            suite = "general",
            title = "Quantum Physics Discussion",
            previewText = "Explain quantum entanglement",
            messageCount = 2
        )
        repository.saveChatSession(session)

        val retrievedSession = repository.getSessionById("session_test_01")
        assertNotNull(retrievedSession)
        assertEquals("Quantum Physics Discussion", retrievedSession?.title)
        assertEquals("general", retrievedSession?.suite)

        val sessions = repository.getChatSessions("general").first()
        assertEquals(1, sessions.size)
        assertEquals("session_test_01", sessions[0].sessionId)
    }

    @Test
    fun `test saving chat messages persists them in order across sessions`() = runBlocking {
        val sessionId = "session_user_02"
        val session = ChatSessionEntity(
            sessionId = sessionId,
            suite = "general",
            title = "Product Strategy"
        )
        repository.saveChatSession(session)

        // Session 1: User sends a prompt and receives an AI response
        val userMsg = ChatMessageEntity(
            sessionId = sessionId,
            suite = "general",
            role = "user",
            content = "How do I build a roadmap?",
            modelName = "gemini-3.5-flash",
            timestamp = 1000L
        )
        repository.saveChatMessage(userMsg)

        val aiMsg = ChatMessageEntity(
            sessionId = sessionId,
            suite = "general",
            role = "model",
            content = "Here is a 5-step roadmap guide...",
            modelName = "gemini-3.5-flash",
            timestamp = 2000L
        )
        repository.saveChatMessage(aiMsg)

        // Simulate Session 2 (retrieving all saved messages for this session from Room)
        val messages = repository.getMessagesBySession(sessionId).first()
        assertEquals(2, messages.size)
        assertEquals("How do I build a roadmap?", messages[0].content)
        assertEquals("user", messages[0].role)
        assertEquals("Here is a 5-step roadmap guide...", messages[1].content)
        assertEquals("model", messages[1].role)

        // Check updated session message count
        val updatedSession = repository.getSessionById(sessionId)
        assertEquals(2, updatedSession?.messageCount)
    }

    @Test
    fun `test message bookmarking and individual message deletion`() = runBlocking {
        val sessionId = "session_03"
        val msgId = repository.saveChatMessage(
            ChatMessageEntity(
                sessionId = sessionId,
                suite = "general",
                role = "user",
                content = "Key formula to remember",
                timestamp = 3000L
            )
        )

        // Toggle bookmark
        repository.toggleMessageBookmark(msgId, true)
        val bookmarked = repository.getBookmarkedMessages().first()
        assertEquals(1, bookmarked.size)
        assertTrue(bookmarked[0].isBookmarked)

        // Delete message
        repository.deleteMessage(msgId)
        val remaining = repository.getMessagesBySession(sessionId).first()
        assertEquals(0, remaining.size)
    }

    @Test
    fun `test session deletion removes session and cascaded messages`() = runBlocking {
        val sessionId = "session_to_delete"
        repository.saveChatSession(
            ChatSessionEntity(
                sessionId = sessionId,
                suite = "general",
                title = "Temporary Chat"
            )
        )
        repository.saveChatMessage(
            ChatMessageEntity(
                sessionId = sessionId,
                suite = "general",
                role = "user",
                content = "Temp message"
            )
        )

        assertEquals(1, repository.getMessagesBySession(sessionId).first().size)

        repository.deleteSession(sessionId)

        assertEquals(0, repository.getChatSessions("general").first().size)
        assertEquals(0, repository.getMessagesBySession(sessionId).first().size)
    }
}
