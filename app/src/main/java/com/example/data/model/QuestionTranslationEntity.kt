package com.example.data.model

import androidx.room.Entity

@Entity(
    tableName = "question_translations",
    primaryKeys = ["questionId", "targetLanguage"]
)
data class QuestionTranslationEntity(
    val questionId: Long,
    val targetLanguage: String, // "ar" or "en"
    val translatedQuestion: String,
    val translatedOptionA: String,
    val translatedOptionB: String,
    val translatedOptionC: String,
    val translatedOptionD: String,
    val translatedExplanation: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
