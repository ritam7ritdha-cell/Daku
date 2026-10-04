package com.example.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_sessions",
    indices = [Index(value = ["suite"]), Index(value = ["lastModified"])]
)
data class ChatSessionEntity(
    @PrimaryKey val sessionId: String,
    val suite: String = "general", // "general", "research", "talking"
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val previewText: String = "",
    val messageCount: Int = 0
)

@Entity(
    tableName = "chat_messages",
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["suite"]),
        Index(value = ["timestamp"])
    ]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String = "default_session",
    val suite: String = "general", // "general", "research", "talking"
    val role: String,  // "user", "model"
    val content: String,
    val modelName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val thinkingText: String? = null,
    val isBookmarked: Boolean = false
)

@Entity(tableName = "research_notes")
data class ResearchNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val sourceContent: String,
    val summary: String,
    val keyConcepts: String, // Bullet points or JSON
    val mockQuestions: String, // Quiz questions or JSON
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "generated_media")
data class GeneratedMediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mediaType: String, // "image", "video", "audio"
    val prompt: String,
    val mediaPathOrUrl: String,
    val stylePreset: String = "",
    val resolution: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_credits")
data class UserCreditsEntity(
    @PrimaryKey val userId: String = "default_user",
    val balance: Int = 50,
    val welcomeClaimed: Boolean = true
)

@Entity(
    tableName = "credit_codes",
    indices = [androidx.room.Index(value = ["code"], unique = true)]
)
data class CreditCodeEntity(
    @PrimaryKey(autoGenerate = true) val codeId: Long = 0,
    val code: String, // 10-digit numeric code
    val codeHash: String, // SHA-256 hash
    val creditAmount: Int = 50,
    val status: String = "UNUSED", // "UNUSED", "USED"
    val createdBy: String = "ADMIN",
    val createdAt: Long = System.currentTimeMillis(),
    val redeemedBy: String? = null,
    val redeemedAt: Long? = null
)

@Entity(tableName = "credit_history")
data class CreditHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "default_user",
    val action: String, // e.g. "+50 Credits Claimed", "-5 Image Generation"
    val amount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val balanceAfter: Int,
    val transactionId: String = ""
)
