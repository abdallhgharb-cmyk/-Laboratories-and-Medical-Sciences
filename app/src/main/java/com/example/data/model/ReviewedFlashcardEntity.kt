package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reviewed_flashcards",
    indices = [
        Index(value = ["specialtyId", "academicDegree", "subCategory"])
    ]
)
data class ReviewedFlashcardEntity(
    @PrimaryKey
    val questionId: Long,
    val specialtyId: String,
    val academicDegree: String,
    val subCategory: String,
    val reviewedAt: Long = System.currentTimeMillis()
)
