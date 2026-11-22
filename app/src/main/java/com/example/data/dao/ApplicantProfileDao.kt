package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ApplicantProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplicantProfileDao {

    @Query("SELECT * FROM applicant_profile WHERE id = 1")
    fun getProfile(): Flow<ApplicantProfileEntity?>

    @Query("SELECT * FROM applicant_profile WHERE id = 1")
    suspend fun getProfileDirect(): ApplicantProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: ApplicantProfileEntity)
}
