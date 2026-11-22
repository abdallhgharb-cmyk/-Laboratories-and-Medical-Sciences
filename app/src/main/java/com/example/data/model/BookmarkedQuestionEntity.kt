package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarked_questions")
data class BookmarkedQuestionEntity(
    @PrimaryKey
    val questionId: Long,
    val timestamp: Long = System.currentTimeMillis()
)
