package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.QuestionEntity
import com.example.data.model.ReviewedFlashcardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {

    @Query("SELECT questionId FROM reviewed_flashcards")
    fun observeReviewedFlashcardIds(): Flow<List<Long>>

    @Query(
        """
        SELECT DISTINCT subCategory
        FROM questions
        WHERE specialtyId = :specialtyId AND academicDegree = :academicDegree
        ORDER BY subCategory ASC
        """
    )
    suspend fun getDistinctSubCategories(
        specialtyId: String,
        academicDegree: String
    ): List<String>

    @Query(
        """
        SELECT * FROM questions
        WHERE specialtyId = :specialtyId
          AND academicDegree = :academicDegree
          AND (:subCategory IS NULL OR subCategory = :subCategory)
          AND id NOT IN (SELECT questionId FROM reviewed_flashcards)
        ORDER BY stageLevel ASC, id ASC
        """
    )
    suspend fun getUnseenFlashcards(
        specialtyId: String,
        academicDegree: String,
        subCategory: String?
    ): List<QuestionEntity>

    @Query(
        """
        SELECT COUNT(*) FROM questions
        WHERE specialtyId = :specialtyId
          AND academicDegree = :academicDegree
          AND (:subCategory IS NULL OR subCategory = :subCategory)
        """
    )
    suspend fun getTotalFlashcardsInScope(
        specialtyId: String,
        academicDegree: String,
        subCategory: String?
    ): Int

    @Query(
        """
        SELECT COUNT(*) FROM questions
        WHERE specialtyId = :specialtyId
          AND academicDegree = :academicDegree
          AND (:subCategory IS NULL OR subCategory = :subCategory)
          AND id NOT IN (SELECT questionId FROM reviewed_flashcards)
        """
    )
    suspend fun getUnseenFlashcardsCount(
        specialtyId: String,
        academicDegree: String,
        subCategory: String?
    ): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markFlashcardReviewed(entity: ReviewedFlashcardEntity)

    @Query("DELETE FROM reviewed_flashcards WHERE specialtyId = :specialtyId AND academicDegree = :academicDegree")
    suspend fun resetDeck(specialtyId: String, academicDegree: String)

    @Query(
        """
        DELETE FROM reviewed_flashcards
        WHERE specialtyId = :specialtyId
          AND academicDegree = :academicDegree
          AND subCategory = :subCategory
        """
    )
    suspend fun resetDeckByTopic(
        specialtyId: String,
        academicDegree: String,
        subCategory: String
    )
}
