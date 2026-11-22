package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Constants and helpers for the clinical categories and academic degrees
 */
data class ClinicalDepartmentSpec(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val maxStages: Int = 10,
    val degrees: List<String> = AcademicDegree.STANDARD_DEGREES
)

/**
 * Constants and helpers for the clinical categories and academic degrees.
 * Extensible via [REGISTERED_DEPARTMENTS] for scaling past 10,000+ questions and future departments.
 */
object QuestionCategory {
    const val MEDICAL_LABS = "MED_LAB"
    const val MED_LAB_LICENSING = "MED_LAB_LICENSING"
    const val NURSING = "NURSING"
    const val PHYSICIAN_ASSISTANT = "PHYSICIAN_ASSISTANT"

    val REGISTERED_DEPARTMENTS: List<ClinicalDepartmentSpec> = listOf(
        ClinicalDepartmentSpec(
            id = MED_LAB_LICENSING,
            titleAr = "مزاولة المهنة - المختبرات الطبية",
            titleEn = "Medical Lab Licensing Exam (530 Qs)",
            maxStages = 9,
            degrees = listOf(AcademicDegree.BACHELOR)
        ),
        ClinicalDepartmentSpec(
            id = MEDICAL_LABS,
            titleAr = "المختبرات والعلوم الطبية",
            titleEn = "Medical Laboratory Sciences",
            maxStages = 10,
            degrees = AcademicDegree.MED_LAB_DEGREES
        ),
        ClinicalDepartmentSpec(
            id = NURSING,
            titleAr = "علوم التمريض والرعاية الحرجة",
            titleEn = "Nursing Sciences",
            maxStages = 10,
            degrees = AcademicDegree.STANDARD_DEGREES
        ),
        ClinicalDepartmentSpec(
            id = PHYSICIAN_ASSISTANT,
            titleAr = "مساعد الطبيب والطب العام",
            titleEn = "Physician Assistant",
            maxStages = 10,
            degrees = AcademicDegree.STANDARD_DEGREES
        )
    )

    val ALL_CATEGORIES: List<String> = listOf(
        MEDICAL_LABS,
        MED_LAB_LICENSING,
        NURSING,
        PHYSICIAN_ASSISTANT
    )

    fun specFor(specialtyId: String): ClinicalDepartmentSpec? =
        REGISTERED_DEPARTMENTS.find { it.id == specialtyId }

    fun maxStagesForSpecialty(specialtyId: String): Int =
        specFor(specialtyId)?.maxStages ?: 10

    fun getCategoryTitleAr(category: String): String =
        specFor(category)?.titleAr ?: category

    fun getCategoryTitleEn(category: String): String =
        specFor(category)?.titleEn ?: category
}

object AcademicDegree {
    const val DIPLOMA = "DIPLOMA"
    const val BACHELOR = "BACHELOR"
    const val MASTER = "MASTER"
    const val CONSULTANT = "CONSULTANT"

    val STANDARD_DEGREES = listOf(DIPLOMA, BACHELOR, MASTER)
    val MED_LAB_DEGREES = listOf(DIPLOMA, BACHELOR, MASTER, CONSULTANT)
    val ALL_DEGREES = STANDARD_DEGREES // Kept for existing 3-tier Nursing/PA compatibility

    fun degreesForSpecialty(specialtyId: String): List<String> = when (specialtyId) {
        QuestionCategory.MED_LAB_LICENSING -> listOf(BACHELOR)
        QuestionCategory.MEDICAL_LABS -> MED_LAB_DEGREES
        else -> QuestionCategory.specFor(specialtyId)?.degrees ?: STANDARD_DEGREES
    }

    fun getDegreeTitleAr(degree: String, specialty: String): String = when {
        specialty == QuestionCategory.MED_LAB_LICENSING -> "امتحان مزاولة المهنة الشامل"
        degree == DIPLOMA -> when (specialty) {
            QuestionCategory.MEDICAL_LABS -> "فني مختبرات / دبلوم"
            QuestionCategory.NURSING -> "دبلوم تمريض"
            else -> "دبلوم مساعد طبيب"
        }
        degree == BACHELOR -> when (specialty) {
            QuestionCategory.MEDICAL_LABS -> "بكالوريوس مختبرات طبية"
            QuestionCategory.NURSING -> "بكالوريوس تمريض"
            else -> "بكالوريوس مساعد طبيب"
        }
        degree == MASTER -> when (specialty) {
            QuestionCategory.MEDICAL_LABS -> "ماجستير مختبرات سريرية"
            QuestionCategory.NURSING -> "ماجستير تمريض ورعاية حرجة"
            else -> "ماجستير ممارس متقدم"
        }
        degree == CONSULTANT -> when (specialty) {
            QuestionCategory.MEDICAL_LABS -> "استشاري مختبرات سريرية (Consultant)"
            QuestionCategory.NURSING -> "استشاري تمريض ورعاية حرجة"
            else -> "استشاري سريري متقدم"
        }
        else -> degree
    }

    fun getDegreeTitleEn(degree: String): String = when (degree) {
        DIPLOMA -> "Diploma / Technician"
        BACHELOR -> "Bachelor of Science"
        MASTER -> "Master of Science"
        CONSULTANT -> "Consultant / Senior Board"
        else -> degree
    }
}

/**
 * Standardized clinical difficulty levels as per board examination hierarchy
 */
object QuestionDifficulty {
    const val JUNIOR_TRAINEE = "JUNIOR_TRAINEE"       // Levels 1-3
    const val CLINICAL_RESIDENT = "CLINICAL_RESIDENT"   // Levels 4-6
    const val SPECIALIST = "SPECIALIST"                 // Levels 7-8
    const val CONSULTANT = "CONSULTANT"                 // Levels 9-10

    fun fromStageLevel(stageLevel: Int): String = when (stageLevel) {
        in 1..3 -> JUNIOR_TRAINEE
        in 4..6 -> CLINICAL_RESIDENT
        in 7..8 -> SPECIALIST
        else -> CONSULTANT
    }

    fun getDifficultyLabelAr(difficulty: String): String = when (difficulty) {
        JUNIOR_TRAINEE -> "مبتدئ / Junior Trainee"
        CLINICAL_RESIDENT -> "مقيم / Clinical Resident"
        SPECIALIST -> "أخصائي معتمد / Certified Specialist"
        CONSULTANT -> "استشاري / Lab Consultant"
        else -> difficulty
    }
}

@Entity(
    tableName = "questions",
    indices = [
        Index(value = ["specialtyId", "academicDegree", "stageLevel"]),
        Index(value = ["specialtyId", "difficultyLevel"]),
        Index(value = ["imageResName"])
    ]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val specialtyId: String, // QuestionCategory.MEDICAL_LABS, MED_LAB_LICENSING, NURSING, or PHYSICIAN_ASSISTANT
    val academicDegree: String = AcademicDegree.BACHELOR, // DIPLOMA, BACHELOR, MASTER
    val stageLevel: Int, // 1 to 10 (1 to 9 for MED_LAB_LICENSING)
    val difficultyLevel: String = QuestionDifficulty.fromStageLevel(stageLevel), // Difficulty Level
    val subCategory: String, // Sub-discipline (e.g., Hematology, Microbiology, Urinalysis, Critical Care)
    val questionEn: String, // Board / Licensing question prompt
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOptionIndex: Int, // 0..3 (0=A, 1=B, 2=C, 3=D)
    val clinicalExplanationEn: String, // Comprehensive board scientific rationale (optional for licensing bank)
    val clinicalHintEn: String, // Clinical diagnostic hint for lifeline assistance
    val imageResName: String? = null, // Optional diagnostic microscope/clinical image resource name
    val diagnosticImageNotes: String? = null, // Specific clinical image notes
    val scientificReference: String? = null // Authoritative medical textbook/manual reference
)
