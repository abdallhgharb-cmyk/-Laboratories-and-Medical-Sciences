package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ApplicantProfileDao
import com.example.data.dao.FlashcardDao
import com.example.data.dao.QuestionDao
import com.example.data.dao.QuestionTranslationDao
import com.example.data.dao.QuizHistoryDao
import com.example.data.dao.StageProgressDao
import com.example.data.dao.StudyMaterialDao
import com.example.data.dao.UserNoteDao
import com.example.data.dao.UserProgressDao
import com.example.data.model.AnsweredQuestionEntity
import com.example.data.model.ApplicantProfileEntity
import com.example.data.model.BookmarkedQuestionEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.QuestionTranslationEntity
import com.example.data.model.QuizSessionAnswerEntity
import com.example.data.model.QuizSessionEntity
import com.example.data.model.ReviewedFlashcardEntity
import com.example.data.model.StageProgressEntity
import com.example.data.model.StudyMaterialEntity
import com.example.data.model.UserNoteEntity
import com.example.data.model.UserProgressEntity

@Database(
    entities = [
        QuestionEntity::class,
        AnsweredQuestionEntity::class,
        UserProgressEntity::class,
        ApplicantProfileEntity::class,
        BookmarkedQuestionEntity::class,
        StudyMaterialEntity::class,
        UserNoteEntity::class,
        ReviewedFlashcardEntity::class,
        QuizSessionEntity::class,
        QuizSessionAnswerEntity::class,
        QuestionTranslationEntity::class,
        StageProgressEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun questionDao(): QuestionDao
    abstract fun questionTranslationDao(): QuestionTranslationDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun stageProgressDao(): StageProgressDao
    abstract fun applicantProfileDao(): ApplicantProfileDao
    abstract fun studyMaterialDao(): StudyMaterialDao
    abstract fun userNoteDao(): UserNoteDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun quizHistoryDao(): QuizHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE answered_questions ADD COLUMN academicDegree TEXT NOT NULL DEFAULT 'BACHELOR'"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `user_notes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `questionId` INTEGER,
                        `specialtyId` TEXT NOT NULL,
                        `topic` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_user_notes_questionId` ON `user_notes` (`questionId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_user_notes_specialtyId_updatedAt` ON `user_notes` (`specialtyId`, `updatedAt`)"
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `reviewed_flashcards` (
                        `questionId` INTEGER NOT NULL,
                        `specialtyId` TEXT NOT NULL,
                        `academicDegree` TEXT NOT NULL,
                        `subCategory` TEXT NOT NULL,
                        `reviewedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`questionId`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_reviewed_flashcards_specialtyId_academicDegree_subCategory` ON `reviewed_flashcards` (`specialtyId`, `academicDegree`, `subCategory`)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `quiz_sessions` (
                        `sessionId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `mode` TEXT NOT NULL,
                        `specialtyId` TEXT NOT NULL,
                        `academicDegree` TEXT NOT NULL,
                        `stageLevel` INTEGER NOT NULL,
                        `topicTitle` TEXT,
                        `durationLimitSeconds` INTEGER NOT NULL,
                        `timeSpentSeconds` INTEGER NOT NULL,
                        `totalQuestions` INTEGER NOT NULL,
                        `correctCount` INTEGER NOT NULL,
                        `scoreEarned` INTEGER NOT NULL,
                        `isPassed` INTEGER NOT NULL,
                        `completedAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_quiz_sessions_completedAt` ON `quiz_sessions` (`completedAt`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_quiz_sessions_mode_completedAt` ON `quiz_sessions` (`mode`, `completedAt`)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `quiz_session_answers` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sessionId` INTEGER NOT NULL,
                        `questionId` INTEGER NOT NULL,
                        `orderIndex` INTEGER NOT NULL,
                        `selectedOptionIndex` INTEGER NOT NULL,
                        `isCorrect` INTEGER NOT NULL,
                        `wasFlagged` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_quiz_session_answers_sessionId` ON `quiz_session_answers` (`sessionId`)"
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE questions ADD COLUMN scientificReference TEXT")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `question_translations` (
                        `questionId` INTEGER NOT NULL,
                        `targetLanguage` TEXT NOT NULL,
                        `translatedQuestion` TEXT NOT NULL,
                        `translatedOptionA` TEXT NOT NULL,
                        `translatedOptionB` TEXT NOT NULL,
                        `translatedOptionC` TEXT NOT NULL,
                        `translatedOptionD` TEXT NOT NULL,
                        `translatedExplanation` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`questionId`, `targetLanguage`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create per-stage star & grading persistence table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `stage_progress` (
                        `specialtyId` TEXT NOT NULL,
                        `academicDegree` TEXT NOT NULL,
                        `stageLevel` INTEGER NOT NULL,
                        `bestCorrectCount` INTEGER NOT NULL,
                        `bestTotalCount` INTEGER NOT NULL,
                        `bestAccuracyPercent` INTEGER NOT NULL,
                        `starsEarned` INTEGER NOT NULL,
                        `isUnlocked` INTEGER NOT NULL,
                        `isPassed` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`specialtyId`, `academicDegree`, `stageLevel`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_stage_progress_specialtyId_academicDegree` ON `stage_progress` (`specialtyId`, `academicDegree`)"
                )

                // 2. Reorganize Laboratory Department into 4 tiers (Diploma, Bachelor, Master, Consultant)
                // without deleting any user history or creating duplicate questions.
                db.execSQL(
                    """
                    UPDATE `questions`
                    SET `academicDegree` = 'CONSULTANT',
                        `stageLevel` = ((`stageLevel` - 6) * 2) + 1 + (`id` % 2)
                    WHERE `specialtyId` = 'MED_LAB'
                      AND `academicDegree` = 'MASTER'
                      AND (`difficultyLevel` = 'CONSULTANT' OR `stageLevel` >= 6)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    UPDATE `questions`
                    SET `stageLevel` = ((`stageLevel` - 1) * 2) + 1 + (`id` % 2)
                    WHERE `specialtyId` = 'MED_LAB'
                      AND `academicDegree` = 'MASTER'
                      AND `stageLevel` BETWEEN 1 AND 5
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    UPDATE `answered_questions`
                    SET `academicDegree` = 'CONSULTANT'
                    WHERE `questionId` IN (
                        SELECT `id` FROM `questions`
                        WHERE `specialtyId` = 'MED_LAB' AND `academicDegree` = 'CONSULTANT'
                    )
                    """.trimIndent()
                )

                // 3. Ensure MED_LAB CONSULTANT progress row exists
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO `user_progress` (
                        `specialtyId`, `academicDegree`, `highestUnlockedStage`,
                        `completedStagesCount`, `totalScore`, `totalCorrectAnswers`,
                        `totalQuestionsAnswered`, `professionalRankEn`, `professionalRankAr`,
                        `lastAssessmentTimestamp`
                    ) VALUES (
                        'MED_LAB', 'CONSULTANT', 1, 0, 0, 0, 0,
                        'Board Consultant Fellow', 'استشاري مختبرات سريرية متقدم', 0
                    )
                    """.trimIndent()
                )

                // 4. Preserve already-unlocked/completed stages from existing user_progress into stage_progress
                val now = System.currentTimeMillis()
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO `stage_progress` (
                        `specialtyId`, `academicDegree`, `stageLevel`,
                        `bestCorrectCount`, `bestTotalCount`, `bestAccuracyPercent`,
                        `starsEarned`, `isUnlocked`, `isPassed`, `updatedAt`
                    )
                    WITH RECURSIVE stages(lvl) AS (
                        SELECT 1 UNION ALL SELECT lvl + 1 FROM stages WHERE lvl < 10
                    )
                    SELECT
                        up.`specialtyId`,
                        up.`academicDegree`,
                        stages.lvl,
                        0,
                        0,
                        CASE WHEN stages.lvl < up.`highestUnlockedStage` THEN 75 ELSE 0 END,
                        CASE WHEN stages.lvl < up.`highestUnlockedStage` THEN 2 ELSE 0 END,
                        CASE WHEN stages.lvl <= up.`highestUnlockedStage` THEN 1 ELSE 0 END,
                        CASE WHEN stages.lvl < up.`highestUnlockedStage` THEN 1 ELSE 0 END,
                        $now
                    FROM `user_progress` up
                    CROSS JOIN stages
                    WHERE (up.`specialtyId` != 'MED_LAB_LICENSING' OR stages.lvl <= 9)
                      AND stages.lvl <= up.`highestUnlockedStage`
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "clinical_competency_db"
                )
                    .createFromAsset("database/clinical_competency_db.db")
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
