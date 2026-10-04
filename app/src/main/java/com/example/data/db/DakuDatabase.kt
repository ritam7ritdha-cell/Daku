package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        ResearchNoteEntity::class,
        GeneratedMediaEntity::class,
        UserCreditsEntity::class,
        CreditCodeEntity::class,
        CreditHistoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class DakuDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun researchDao(): ResearchDao
    abstract fun mediaDao(): MediaDao
    abstract fun creditDao(): CreditDao

    companion object {
        @Volatile
        private var INSTANCE: DakuDatabase? = null

        fun getDatabase(context: Context): DakuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DakuDatabase::class.java,
                    "daku_ai_db"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
