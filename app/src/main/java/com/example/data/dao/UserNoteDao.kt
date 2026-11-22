package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserNoteDao {

    @Query("SELECT * FROM user_notes ORDER BY updatedAt DESC")
    fun observeAllNotes(): Flow<List<UserNoteEntity>>

    @Query("SELECT * FROM user_notes WHERE questionId IS NOT NULL ORDER BY updatedAt DESC")
    fun observeQuestionLinkedNotes(): Flow<List<UserNoteEntity>>

    @Query("SELECT * FROM user_notes WHERE questionId IS NULL ORDER BY updatedAt DESC")
    fun observeStandaloneNotes(): Flow<List<UserNoteEntity>>

    @Query("SELECT * FROM user_notes WHERE questionId = :questionId ORDER BY updatedAt DESC")
    fun observeNotesForQuestion(questionId: Long): Flow<List<UserNoteEntity>>

    @Query("SELECT DISTINCT questionId FROM user_notes WHERE questionId IS NOT NULL")
    fun observeNoteQuestionIds(): Flow<List<Long>>

    @Query("SELECT * FROM user_notes ORDER BY updatedAt DESC")
    suspend fun getAllNotesDirect(): List<UserNoteEntity>

    @Query("SELECT * FROM user_notes WHERE id = :id")
    suspend fun getNoteById(id: Long): UserNoteEntity?

    @Query("SELECT * FROM user_notes WHERE questionId = :questionId LIMIT 1")
    suspend fun getFirstNoteForQuestion(questionId: Long): UserNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: UserNoteEntity): Long

    @Update
    suspend fun updateNote(note: UserNoteEntity)

    @Query("DELETE FROM user_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}
