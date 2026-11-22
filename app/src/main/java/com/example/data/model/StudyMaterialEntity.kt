package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_materials")
data class StudyMaterialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val specialtyId: String,
    val title: String,
    val category: String,
    val summary: String,
    val clinicalContent: String,
    val keyPointsJson: String, // High-yield exam bullet points
    val highYieldRating: Int = 5, // 1 to 5 stars
    val isSavedOffline: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
