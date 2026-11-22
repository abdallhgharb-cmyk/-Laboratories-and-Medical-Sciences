package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "answered_questions")
data class AnsweredQuestionEntity(
    @PrimaryKey
    val questionId: Long,
    val specialtyId: String,
    val stageLevel: Int,
    val isCorrect: Boolean,
    val selectedOptionIndex: Int,
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "BACHELOR")
    val academicDegree: String = AcademicDegree.BACHELOR
)
