package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_notes",
    indices = [
        Index(value = ["questionId"]),
        Index(value = ["specialtyId", "updatedAt"])
    ]
)
data class UserNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questionId: Long? = null, // null for standalone notebook entries; non-null for question-linked notes
    val specialtyId: String = "GENERAL", // MED_LAB, NURSING, PHYSICIAN_ASSISTANT, GENERAL
    val topic: String = "General",
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
