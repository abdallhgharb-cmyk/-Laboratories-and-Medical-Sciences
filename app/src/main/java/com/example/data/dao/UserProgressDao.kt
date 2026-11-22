package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.UserProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {

    @Query("SELECT * FROM user_progress WHERE specialtyId = :specialtyId AND academicDegree = :academicDegree")
    fun getProgress(specialtyId: String, academicDegree: String): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE specialtyId = :specialtyId LIMIT 1")
    fun getProgress(specialtyId: String): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress")
    fun getAllProgress(): Flow<List<UserProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: UserProgressEntity)

    @Query("SELECT * FROM user_progress WHERE specialtyId = :specialtyId AND academicDegree = :academicDegree")
    suspend fun getProgressDirect(specialtyId: String, academicDegree: String): UserProgressEntity?

    @Query("SELECT * FROM user_progress WHERE specialtyId = :specialtyId LIMIT 1")
    suspend fun getProgressDirect(specialtyId: String): UserProgressEntity?
}
