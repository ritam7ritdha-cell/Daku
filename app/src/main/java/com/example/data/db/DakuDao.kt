package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions WHERE suite = :suite ORDER BY lastModified DESC")
    fun getSessionsBySuite(suite: String): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): ChatSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSession(session: ChatSessionEntity)

    @Query("DELETE FROM chat_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSessionById(sessionId: String)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesBySession(sessionId: String)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC, id ASC")
    fun getMessagesBySession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE suite = :suite ORDER BY timestamp ASC, id ASC")
    fun getMessagesBySuite(suite: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessageById(id: Long)

    @Query("UPDATE chat_messages SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmark(id: Long, isBookmarked: Boolean)

    @Query("DELETE FROM chat_messages WHERE suite = :suite")
    suspend fun clearSuiteMessages(suite: String)

    @Query("SELECT COUNT(*) FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun getSessionMessageCount(sessionId: String): Int
}

@Dao
interface ResearchDao {
    @Query("SELECT * FROM research_notes ORDER BY timestamp DESC")
    fun getAllResearchNotes(): Flow<List<ResearchNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: ResearchNoteEntity): Long

    @Query("DELETE FROM research_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}

@Dao
interface MediaDao {
    @Query("SELECT * FROM generated_media WHERE mediaType = :mediaType ORDER BY timestamp DESC")
    fun getMediaByType(mediaType: String): Flow<List<GeneratedMediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: GeneratedMediaEntity): Long

    @Query("DELETE FROM generated_media WHERE id = :id")
    suspend fun deleteMediaById(id: Long)
}

@Dao
interface CreditDao {
    @Query("SELECT * FROM user_credits WHERE userId = :userId LIMIT 1")
    fun getUserCreditsFlow(userId: String): Flow<UserCreditsEntity?>

    @Query("SELECT * FROM user_credits WHERE userId = :userId LIMIT 1")
    suspend fun getUserCredits(userId: String): UserCreditsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserCredits(credits: UserCreditsEntity)

    @Query("SELECT * FROM credit_codes WHERE code = :code LIMIT 1")
    suspend fun getCodeByValue(code: String): CreditCodeEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCode(codeEntity: CreditCodeEntity): Long

    @Query("SELECT * FROM credit_codes ORDER BY createdAt DESC")
    fun getAllCodes(): Flow<List<CreditCodeEntity>>

    @Update
    suspend fun updateCode(codeEntity: CreditCodeEntity)

    @Query("SELECT * FROM credit_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getCreditHistory(userId: String): Flow<List<CreditHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: CreditHistoryEntity): Long
}
