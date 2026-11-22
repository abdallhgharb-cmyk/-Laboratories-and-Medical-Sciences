package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.QuestionTranslationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionTranslationDao {

    @Query("SELECT * FROM question_translations WHERE questionId = :questionId AND targetLanguage = :targetLanguage LIMIT 1")
    suspend fun getTranslation(questionId: Long, targetLanguage: String): QuestionTranslationEntity?

    @Query("SELECT * FROM question_translations WHERE questionId IN (:questionIds) AND targetLanguage = :targetLanguage")
    suspend fun getTranslationsByIds(questionIds: List<Long>, targetLanguage: String): List<QuestionTranslationEntity>

    @Query("SELECT * FROM question_translations")
    fun observeAllTranslations(): Flow<List<QuestionTranslationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTranslation(translation: QuestionTranslationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTranslations(translations: List<QuestionTranslationEntity>)

    @Query("SELECT COUNT(*) FROM question_translations")
    suspend fun countTranslations(): Int

    @Query("SELECT COUNT(*) FROM question_translations WHERE targetLanguage = :targetLanguage")
    suspend fun countTranslationsByLanguage(targetLanguage: String): Int
}
