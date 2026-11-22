package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AnsweredQuestionEntity
import com.example.data.model.BookmarkedQuestionEntity
import com.example.data.model.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {

    // ==========================================
    // Reactive Flow Queries (Room Best Practice)
    // ==========================================

    @Query("SELECT * FROM questions ORDER BY stageLevel ASC, id ASC")
    fun observeAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE specialtyId = :specialtyId ORDER BY stageLevel ASC, id ASC")
    fun observeQuestionsByCategory(specialtyId: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE specialtyId = :specialtyId AND academicDegree = :academicDegree ORDER BY stageLevel ASC, id ASC")
    fun observeQuestionsByDegree(specialtyId: String, academicDegree: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE difficultyLevel = :difficultyLevel ORDER BY stageLevel ASC, id ASC")
    fun observeQuestionsByDifficultyOnly(difficultyLevel: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE specialtyId = :specialtyId AND difficultyLevel = :difficultyLevel ORDER BY stageLevel ASC, id ASC")
    fun observeQuestionsByDifficulty(specialtyId: String, difficultyLevel: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE imageResName IS NOT NULL ORDER BY specialtyId, stageLevel")
    fun observeDiagnosticImageQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getQuestionById(id: Long): QuestionEntity?

    // ==========================================
    // Bilingual Search Feature: English + Arabic translations, options, category, stage or ID
    // ==========================================

    @Query(
        """
        SELECT DISTINCT q.* FROM questions q
        LEFT JOIN question_translations qt ON q.id = qt.questionId
        WHERE q.questionEn LIKE '%' || :query || '%'
           OR q.optionA LIKE '%' || :query || '%'
           OR q.optionB LIKE '%' || :query || '%'
           OR q.optionC LIKE '%' || :query || '%'
           OR q.optionD LIKE '%' || :query || '%'
           OR q.subCategory LIKE '%' || :query || '%'
           OR q.clinicalExplanationEn LIKE '%' || :query || '%'
           OR COALESCE(q.scientificReference, '') LIKE '%' || :query || '%'
           OR COALESCE(qt.translatedQuestion, '') LIKE '%' || :query || '%'
           OR COALESCE(qt.translatedOptionA, '') LIKE '%' || :query || '%'
           OR COALESCE(qt.translatedOptionB, '') LIKE '%' || :query || '%'
           OR COALESCE(qt.translatedOptionC, '') LIKE '%' || :query || '%'
           OR COALESCE(qt.translatedOptionD, '') LIKE '%' || :query || '%'
           OR COALESCE(qt.translatedExplanation, '') LIKE '%' || :query || '%'
           OR CAST(q.id AS TEXT) = :query
           OR CAST(q.stageLevel AS TEXT) = :query
        ORDER BY q.stageLevel ASC, q.id ASC
        LIMIT 250
        """
    )
    suspend fun searchQuestions(query: String): List<QuestionEntity>

    @Query(
        """
        SELECT DISTINCT q.* FROM questions q
        LEFT JOIN question_translations qt ON q.id = qt.questionId
        WHERE (:specialtyId IS NULL OR q.specialtyId = :specialtyId)
          AND (:academicDegree IS NULL OR q.academicDegree = :academicDegree)
          AND (
              :query = ''
              OR q.questionEn LIKE '%' || :query || '%'
              OR q.optionA LIKE '%' || :query || '%'
              OR q.optionB LIKE '%' || :query || '%'
              OR q.optionC LIKE '%' || :query || '%'
              OR q.optionD LIKE '%' || :query || '%'
              OR q.subCategory LIKE '%' || :query || '%'
              OR q.clinicalExplanationEn LIKE '%' || :query || '%'
              OR COALESCE(q.scientificReference, '') LIKE '%' || :query || '%'
              OR COALESCE(qt.translatedQuestion, '') LIKE '%' || :query || '%'
              OR COALESCE(qt.translatedOptionA, '') LIKE '%' || :query || '%'
              OR COALESCE(qt.translatedOptionB, '') LIKE '%' || :query || '%'
              OR COALESCE(qt.translatedOptionC, '') LIKE '%' || :query || '%'
              OR COALESCE(qt.translatedOptionD, '') LIKE '%' || :query || '%'
              OR COALESCE(qt.translatedExplanation, '') LIKE '%' || :query || '%'
              OR CAST(q.id AS TEXT) = :query
              OR CAST(q.stageLevel AS TEXT) = :query
          )
        ORDER BY q.stageLevel ASC, q.id ASC
        LIMIT 250
        """
    )
    suspend fun searchQuestionsFiltered(
        query: String,
        specialtyId: String? = null,
        academicDegree: String? = null
    ): List<QuestionEntity>

    // ==========================================
    // Favorites / Bookmarks Feature
    // ==========================================

    @Query("SELECT q.* FROM questions q INNER JOIN bookmarked_questions b ON q.id = b.questionId ORDER BY b.timestamp DESC")
    fun observeBookmarkedQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT questionId FROM bookmarked_questions")
    fun observeBookmarkedIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: BookmarkedQuestionEntity)

    @Query("DELETE FROM bookmarked_questions WHERE questionId = :questionId")
    suspend fun removeBookmark(questionId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_questions WHERE questionId = :questionId)")
    suspend fun isBookmarked(questionId: Long): Boolean

    // ==========================================
    // Mistakes Notebook / قائمة الأخطاء
    // ==========================================

    @Query("SELECT DISTINCT q.* FROM questions q INNER JOIN answered_questions a ON q.id = a.questionId WHERE a.isCorrect = 0 ORDER BY a.timestamp DESC")
    fun observeMistakeQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT COUNT(DISTINCT questionId) FROM answered_questions WHERE isCorrect = 0")
    fun observeMistakeCount(): Flow<Int>

    // ==========================================
    // Absolute Zero-Duplication Engine Queries
    // Strict SQL exclusion via subquery: WHERE id NOT IN (SELECT questionId FROM answered_questions)
    // ==========================================

    @Query("SELECT * FROM questions WHERE specialtyId = :specialtyId AND academicDegree = :academicDegree AND stageLevel = :stageLevel AND id NOT IN (SELECT questionId FROM answered_questions)")
    suspend fun getUnansweredQuestionsForStage(
        specialtyId: String,
        academicDegree: String,
        stageLevel: Int
    ): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE specialtyId = :specialtyId AND stageLevel = :stageLevel AND id NOT IN (SELECT questionId FROM answered_questions)")
    suspend fun getUnansweredQuestionsForStage(
        specialtyId: String,
        stageLevel: Int
    ): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE specialtyId = :specialtyId AND academicDegree = :academicDegree AND id NOT IN (SELECT questionId FROM answered_questions)")
    suspend fun getAllUnansweredQuestionsForDegree(
        specialtyId: String,
        academicDegree: String
    ): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE specialtyId = :specialtyId AND id NOT IN (SELECT questionId FROM answered_questions)")
    suspend fun getAllUnansweredQuestionsForSpecialty(
        specialtyId: String
    ): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE specialtyId = :specialtyId AND academicDegree = :academicDegree AND subCategory = :subCategory AND id NOT IN (SELECT questionId FROM answered_questions)")
    suspend fun getUnansweredQuestionsByTopic(
        specialtyId: String,
        academicDegree: String,
        subCategory: String
    ): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE specialtyId = :specialtyId AND academicDegree = :academicDegree AND subCategory = :subCategory")
    suspend fun getAllQuestionsByTopic(
        specialtyId: String,
        academicDegree: String,
        subCategory: String
    ): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE id NOT IN (SELECT questionId FROM answered_questions) ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomUnansweredQuestions(limit: Int): List<QuestionEntity>

    @Query("SELECT COUNT(*) FROM questions WHERE specialtyId = :specialtyId AND academicDegree = :academicDegree AND id NOT IN (SELECT questionId FROM answered_questions)")
    suspend fun getUnansweredCountForTrack(
        specialtyId: String,
        academicDegree: String
    ): Int

    @Query("SELECT * FROM questions WHERE id IN (:ids)")
    suspend fun getQuestionsByIds(ids: List<Long>): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE specialtyId = :specialtyId AND academicDegree = :academicDegree AND stageLevel = :stageLevel")
    suspend fun getAllQuestionsForStage(
        specialtyId: String,
        academicDegree: String,
        stageLevel: Int
    ): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE specialtyId = :specialtyId AND stageLevel = :stageLevel")
    suspend fun getAllQuestionsForStage(
        specialtyId: String,
        stageLevel: Int
    ): List<QuestionEntity>

    @Query("SELECT questionId FROM answered_questions WHERE specialtyId = :specialtyId AND stageLevel = :stageLevel")
    suspend fun getAnsweredIdsForStage(
        specialtyId: String,
        stageLevel: Int
    ): List<Long>

    @Query("SELECT questionId FROM answered_questions WHERE specialtyId = :specialtyId")
    suspend fun getAllAnsweredIdsForSpecialty(
        specialtyId: String
    ): List<Long>

    @Query("SELECT COUNT(DISTINCT questionId) FROM answered_questions WHERE isCorrect = 1")
    fun observeTotalCorrectAnswersCount(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT questionId) FROM answered_questions")
    fun observeTotalAnsweredCount(): Flow<Int>

    // ==========================================
    // Category & Difficulty Filter Queries
    // ==========================================

    @Query("SELECT * FROM questions WHERE specialtyId = :specialtyId AND difficultyLevel = :difficultyLevel")
    suspend fun getQuestionsByDifficulty(
        specialtyId: String,
        difficultyLevel: String
    ): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE specialtyId = :specialtyId AND imageResName IS NOT NULL")
    suspend fun getQuestionsWithDiagnosticImages(
        specialtyId: String
    ): List<QuestionEntity>

    @Query("SELECT DISTINCT subCategory FROM questions WHERE specialtyId = :specialtyId")
    suspend fun getSubCategoriesByCategory(specialtyId: String): List<String>

    @Query("SELECT * FROM questions WHERE specialtyId = :specialtyId")
    suspend fun getQuestionsByCategoryDirect(specialtyId: String): List<QuestionEntity>

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getTotalQuestionsCount(): Int

    @Query("SELECT COUNT(*) FROM questions WHERE specialtyId = :specialtyId")
    suspend fun getQuestionsCountByCategory(specialtyId: String): Int

    @Query("SELECT COUNT(*) FROM questions WHERE specialtyId = :specialtyId AND academicDegree = :academicDegree")
    suspend fun getQuestionsCountByDegree(specialtyId: String, academicDegree: String): Int

    @Query("SELECT COUNT(*) FROM questions WHERE specialtyId = :specialtyId AND academicDegree = :academicDegree AND stageLevel = :stageLevel")
    suspend fun getQuestionsCountByStage(specialtyId: String, academicDegree: String, stageLevel: Int): Int

    @Query("SELECT COUNT(*) FROM questions WHERE specialtyId = :specialtyId AND difficultyLevel = :difficultyLevel")
    suspend fun getQuestionsCountByDifficulty(specialtyId: String, difficultyLevel: String): Int

    @Query("SELECT LOWER(TRIM(questionEn)) FROM questions")
    suspend fun getAllNormalizedQuestionStems(): List<String>

    @Query(
        """
        SELECT stageLevel AS stageLevel, COUNT(*) AS questionCount
        FROM questions
        WHERE specialtyId = :specialtyId AND academicDegree = :academicDegree
        GROUP BY stageLevel
        ORDER BY stageLevel ASC
        """
    )
    fun observeStageQuestionCounts(
        specialtyId: String,
        academicDegree: String
    ): Flow<List<StageQuestionCountTuple>>

    @Query(
        """
        SELECT academicDegree AS academicDegree, COUNT(*) AS questionCount
        FROM questions
        WHERE specialtyId = :specialtyId
        GROUP BY academicDegree
        """
    )
    fun observeDegreeQuestionCounts(
        specialtyId: String
    ): Flow<List<DegreeQuestionCountTuple>>

    // ==========================================
    // CRUD Operations (suspend functions)
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @androidx.room.Delete
    suspend fun deleteQuestion(question: QuestionEntity)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteQuestionById(id: Long)

    @Query("DELETE FROM questions")
    suspend fun deleteAllQuestions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordAnswer(answeredQuestion: AnsweredQuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordAnswers(answeredQuestions: List<AnsweredQuestionEntity>)

    @Query("DELETE FROM answered_questions WHERE specialtyId = :specialtyId AND stageLevel = :stageLevel")
    suspend fun resetStageAnswers(specialtyId: String, stageLevel: Int)

    @Query("DELETE FROM answered_questions WHERE specialtyId = :specialtyId AND academicDegree = :academicDegree AND stageLevel = :stageLevel")
    suspend fun resetStageAnswers(specialtyId: String, academicDegree: String, stageLevel: Int)

    @Query("DELETE FROM answered_questions WHERE specialtyId = :specialtyId")
    suspend fun resetSpecialtyAnswers(specialtyId: String)

    @Query("DELETE FROM answered_questions WHERE specialtyId = :specialtyId AND academicDegree = :academicDegree")
    suspend fun resetSpecialtyAnswers(specialtyId: String, academicDegree: String)

    // ==========================================
    // Performance Dashboard Analytics Queries
    // ==========================================

    @Query(
        """
        SELECT q.subCategory AS subCategory,
               q.specialtyId AS specialtyId,
               q.academicDegree AS academicDegree,
               COUNT(q.id) AS totalQuestions,
               COUNT(a.questionId) AS answeredCount,
               COALESCE(SUM(CASE WHEN a.isCorrect = 1 THEN 1 ELSE 0 END), 0) AS correctCount
        FROM questions q
        LEFT JOIN answered_questions a ON q.id = a.questionId
        GROUP BY q.specialtyId, q.academicDegree, q.subCategory
        ORDER BY answeredCount DESC, totalQuestions DESC
        """
    )
    fun observeTopicMasteryStats(): Flow<List<TopicMasteryTuple>>
}

data class TopicMasteryTuple(
    val subCategory: String,
    val specialtyId: String,
    val academicDegree: String,
    val totalQuestions: Int,
    val answeredCount: Int,
    val correctCount: Int
)

data class StageQuestionCountTuple(
    val stageLevel: Int,
    val questionCount: Int
)

data class DegreeQuestionCountTuple(
    val academicDegree: String,
    val questionCount: Int
)
