package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object QuizMode {
    const val STAGE_ASSESSMENT = "STAGE_ASSESSMENT"
    const val PROMETRIC_TIMED = "PROMETRIC_TIMED"
    const val TOPIC_PRACTICE = "TOPIC_PRACTICE"
    const val DAILY_CHALLENGE = "DAILY_CHALLENGE"
}

@Entity(
    tableName = "quiz_sessions",
    indices = [
        Index(value = ["completedAt"]),
        Index(value = ["mode", "completedAt"])
    ]
)
data class QuizSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Long = 0L,
    val mode: String,
    val specialtyId: String,
    val academicDegree: String,
    val stageLevel: Int = 1,
    val topicTitle: String? = null,
    val durationLimitSeconds: Int = 0,
    val timeSpentSeconds: Int = 0,
    val totalQuestions: Int,
    val correctCount: Int,
    val scoreEarned: Int,
    val isPassed: Boolean,
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "quiz_session_answers",
    indices = [
        Index(value = ["sessionId"])
    ]
)
data class QuizSessionAnswerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val sessionId: Long,
    val questionId: Long,
    val orderIndex: Int,
    val selectedOptionIndex: Int,
    val isCorrect: Boolean,
    val wasFlagged: Boolean = false
)
