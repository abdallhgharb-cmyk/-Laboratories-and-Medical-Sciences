package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.StageProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StageProgressDao {

    @Query("SELECT * FROM stage_progress WHERE specialtyId = :specialtyId AND academicDegree = :academicDegree ORDER BY stageLevel ASC")
    fun observeStageProgress(specialtyId: String, academicDegree: String): Flow<List<StageProgressEntity>>

    @Query("SELECT * FROM stage_progress")
    fun observeAllStageProgress(): Flow<List<StageProgressEntity>>

    @Query("SELECT * FROM stage_progress WHERE specialtyId = :specialtyId AND academicDegree = :academicDegree AND stageLevel = :stageLevel LIMIT 1")
    suspend fun getStageProgressDirect(
        specialtyId: String,
        academicDegree: String,
        stageLevel: Int
    ): StageProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStageProgress(stageProgress: StageProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<StageProgressEntity>)

    @Query("DELETE FROM stage_progress WHERE specialtyId = :specialtyId AND academicDegree = :academicDegree")
    suspend fun resetTrackStageProgress(specialtyId: String, academicDegree: String)

    @Query("SELECT COALESCE(SUM(starsEarned), 0) FROM stage_progress WHERE specialtyId = :specialtyId AND academicDegree = :academicDegree")
    suspend fun getTotalStarsForTrack(specialtyId: String, academicDegree: String): Int
}
