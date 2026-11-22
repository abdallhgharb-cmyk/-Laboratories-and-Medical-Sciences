package com.example.data.model

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "stage_progress",
    primaryKeys = ["specialtyId", "academicDegree", "stageLevel"],
    indices = [
        Index(value = ["specialtyId", "academicDegree"])
    ]
)
data class StageProgressEntity(
    val specialtyId: String,
    val academicDegree: String,
    val stageLevel: Int,
    val bestCorrectCount: Int = 0,
    val bestTotalCount: Int = 0,
    val bestAccuracyPercent: Int = 0,
    val starsEarned: Int = 0, // 0 (<70%), 2 (70%-89% unlock & pass), 3 (>=90% distinction)
    val isUnlocked: Boolean = false,
    val isPassed: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
