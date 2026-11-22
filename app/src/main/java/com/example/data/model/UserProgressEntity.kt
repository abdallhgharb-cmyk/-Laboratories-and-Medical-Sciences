package com.example.data.model

import androidx.room.Entity

@Entity(
    tableName = "user_progress",
    primaryKeys = ["specialtyId", "academicDegree"]
)
data class UserProgressEntity(
    val specialtyId: String, // "MED_LAB", "NURSING", "PHYSICIAN_ASSISTANT"
    val academicDegree: String = AcademicDegree.BACHELOR, // "DIPLOMA", "BACHELOR", "MASTER"
    val highestUnlockedStage: Int = 1,
    val completedStagesCount: Int = 0,
    val totalScore: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val totalQuestionsAnswered: Int = 0,
    val professionalRankEn: String = "Junior Trainee",
    val professionalRankAr: String = "متدرب سريري مبتدئ",
    val lastAssessmentTimestamp: Long = System.currentTimeMillis()
)
