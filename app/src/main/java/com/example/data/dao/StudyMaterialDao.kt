package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.StudyMaterialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyMaterialDao {

    @Query("SELECT * FROM study_materials ORDER BY highYieldRating DESC, id ASC")
    fun observeAllMaterials(): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE specialtyId = :specialtyId ORDER BY highYieldRating DESC, id ASC")
    fun observeMaterialsBySpecialty(specialtyId: String): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE isSavedOffline = 1 ORDER BY highYieldRating DESC")
    fun observeOfflineSavedMaterials(): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE id = :id")
    suspend fun getMaterialById(id: Long): StudyMaterialEntity?

    @Query("SELECT * FROM study_materials WHERE title LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%'")
    suspend fun searchMaterials(query: String): List<StudyMaterialEntity>

    @Query("UPDATE study_materials SET isSavedOffline = :isSaved WHERE id = :id")
    suspend fun updateOfflineStatus(id: Long, isSaved: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterials(materials: List<StudyMaterialEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: StudyMaterialEntity): Long

    @Update
    suspend fun updateMaterial(material: StudyMaterialEntity)

    @Query("DELETE FROM study_materials WHERE id = :id")
    suspend fun deleteMaterialById(id: Long)

    @Query("SELECT COUNT(*) FROM study_materials")
    suspend fun countMaterials(): Int
}
