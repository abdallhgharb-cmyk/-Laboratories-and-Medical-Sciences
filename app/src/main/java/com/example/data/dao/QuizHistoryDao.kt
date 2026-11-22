package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.QuizSessionAnswerEntity
import com.example.data.model.QuizSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizHistoryDao {

    @Query("SELECT * FROM quiz_sessions ORDER BY completedAt DESC")
    fun observeAllSessions(): Flow<List<QuizSessionEntity>>

    @Query("SELECT * FROM quiz_sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: Long): QuizSessionEntity?

    @Query("SELECT * FROM quiz_session_answers WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    suspend fun getAnswersForSession(sessionId: Long): List<QuizSessionAnswerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: QuizSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionAnswers(answers: List<QuizSessionAnswerEntity>)

    @Transaction
    suspend fun saveCompletedQuiz(
        session: QuizSessionEntity,
        answers: List<QuizSessionAnswerEntity>
    ): Long {
        val newSessionId = insertSession(session)
        if (answers.isNotEmpty()) {
            insertSessionAnswers(answers.map { it.copy(sessionId = newSessionId) })
        }
        return newSessionId
    }

    @Query("DELETE FROM quiz_session_answers WHERE sessionId = :sessionId")
    suspend fun deleteAnswersForSession(sessionId: Long)

    @Query("DELETE FROM quiz_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    @Transaction
    suspend fun deleteSessionWithAnswers(sessionId: Long) {
        deleteAnswersForSession(sessionId)
        deleteSessionById(sessionId)
    }
}
