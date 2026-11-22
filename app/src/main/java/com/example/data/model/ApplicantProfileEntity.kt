package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "applicant_profile")
data class ApplicantProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val fullName: String = "",
    val degreeType: String = "بكالوريوس", // دبلوم, بكالوريوس, ماجستير, دكتوراه
    val specialty: String = "مختبرات طبية", // مختبرات طبية, علوم التمريض, مساعد طبيب, طب عام
    val age: Int = 24,
    val gender: String = "ذكر", // ذكر, أنثى
    val phoneNumber: String = "",
    val registrationDate: Long = System.currentTimeMillis()
)
