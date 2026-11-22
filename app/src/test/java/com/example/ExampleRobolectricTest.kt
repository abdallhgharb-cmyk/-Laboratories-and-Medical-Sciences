package com.example

import android.app.Application
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Looper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Scale
import com.example.ClinicalBoardApplication
import com.example.data.db.AppDatabase
import com.example.data.model.AcademicDegree
import com.example.data.model.AnsweredQuestionEntity
import com.example.data.model.QuestionCategory
import com.example.data.model.QuestionEntity
import com.example.data.model.QuestionShuffleUtil
import com.example.data.model.QuestionTranslationEntity
import com.example.data.model.QuizMode
import com.example.data.model.QuizSessionAnswerEntity
import com.example.data.model.QuizSessionEntity
import com.example.data.model.StageGradingPolicy
import com.example.data.model.UserNoteEntity
import com.example.data.remote.ConnectivityObserver
import com.example.data.remote.MedicalTranslationService
import com.example.data.repository.ClinicalAssessmentRepository
import com.example.data.seed.LicensingBankImporter
import com.example.data.seed.NursingBankImporter
import kotlin.random.Random
import com.example.ui.screens.ATLAS_ITEMS
import com.example.ui.viewmodel.AssessmentViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.SyncUiState
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private fun buildTestDb(context: Context): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "test_clinical_competency_${System.nanoTime()}.db"
    )
        .createFromAsset("database/clinical_competency_db.db")
        .addMigrations(
            AppDatabase.MIGRATION_5_6,
            AppDatabase.MIGRATION_6_7,
            AppDatabase.MIGRATION_7_8,
            AppDatabase.MIGRATION_8_9,
            AppDatabase.MIGRATION_9_10
        )
        .allowMainThreadQueries()
        .build()

    private fun buildRepository(db: AppDatabase, context: Context): ClinicalAssessmentRepository =
        ClinicalAssessmentRepository(
            questionDao = db.questionDao(),
            userProgressDao = db.userProgressDao(),
            applicantProfileDao = db.applicantProfileDao(),
            studyMaterialDao = db.studyMaterialDao(),
            userNoteDao = db.userNoteDao(),
            flashcardDao = db.flashcardDao(),
            quizHistoryDao = db.quizHistoryDao(),
            appContext = context,
            questionTranslationDao = db.questionTranslationDao(),
            stageProgressDao = db.stageProgressDao()
        )

    @Test
    fun `app name matches medical board branding`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("المختبرات والعلوم الطبية - د/ عبدالله غارب", appName)
    }

    @Test
    fun `prepackaged room v8 opens cleanly with 9570 questions and supports notes and topic stats`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = buildTestDb(context)

        try {
            val questionDao = db.questionDao()
            val noteDao = db.userNoteDao()

            val totalQuestions = questionDao.getTotalQuestionsCount()
            assertEquals(10070, totalQuestions)

            val bachelorCount = questionDao.getQuestionsCountByDegree(
                QuestionCategory.MEDICAL_LABS,
                AcademicDegree.BACHELOR
            )
            assertTrue("Expected >= 1000 bachelor questions", bachelorCount >= 1000)

            val sampleQuestion = questionDao.getAllQuestionsForStage(
                QuestionCategory.MEDICAL_LABS,
                AcademicDegree.BACHELOR,
                1
            ).first()

            val standaloneId = noteDao.upsertNote(
                UserNoteEntity(
                    questionId = null,
                    specialtyId = "GENERAL",
                    topic = "Quality Control",
                    title = "Westgard 1-3s Rule",
                    content = "Reject analytical run if 1 control exceeds +/- 3 SD."
                )
            )
            val linkedId = noteDao.upsertNote(
                UserNoteEntity(
                    questionId = sampleQuestion.id,
                    specialtyId = sampleQuestion.specialtyId,
                    topic = sampleQuestion.subCategory,
                    title = "High-yield pearl",
                    content = "Remember key diagnostic marker."
                )
            )

            assertTrue(standaloneId > 0)
            assertTrue(linkedId > 0)

            val allNotes = noteDao.observeAllNotes().first()
            assertEquals(2, allNotes.size)
            assertNotNull(noteDao.getFirstNoteForQuestion(sampleQuestion.id))

            questionDao.recordAnswer(
                AnsweredQuestionEntity(
                    questionId = sampleQuestion.id,
                    specialtyId = sampleQuestion.specialtyId,
                    stageLevel = sampleQuestion.stageLevel,
                    isCorrect = true,
                    selectedOptionIndex = sampleQuestion.correctOptionIndex,
                    academicDegree = sampleQuestion.academicDegree
                )
            )

            val stats = questionDao.observeTopicMasteryStats().first()
            val matchedStat = stats.firstOrNull {
                it.subCategory == sampleQuestion.subCategory &&
                    it.specialtyId == sampleQuestion.specialtyId &&
                    it.academicDegree == sampleQuestion.academicDegree
            }
            assertNotNull(matchedStat)
            assertTrue(matchedStat!!.answeredCount >= 1)
            assertTrue(matchedStat.correctCount >= 1)
        } finally {
            db.close()
        }
    }

    @Test
    fun `licensing bank loads all 530 questions across 9 stages with zero duplicates and exact answers`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = buildTestDb(context)

        try {
            val questionDao = db.questionDao()
            assertEquals(10070, questionDao.getTotalQuestionsCount())
            assertEquals(3022, questionDao.getQuestionsCountByCategory(QuestionCategory.MEDICAL_LABS))
            assertEquals(3510, questionDao.getQuestionsCountByCategory(QuestionCategory.NURSING))
            assertEquals(3008, questionDao.getQuestionsCountByCategory(QuestionCategory.PHYSICIAN_ASSISTANT))
            assertEquals(530, questionDao.getQuestionsCountByCategory(QuestionCategory.MED_LAB_LICENSING))

            // Verify every single normalized stem across all 10,070 rows is unique (0 duplicates after merge)
            val allNormalizedStems = questionDao.getAllNormalizedQuestionStems()
            assertEquals(10070, allNormalizedStems.size)
            val duplicateCount = allNormalizedStems.size - allNormalizedStems.toSet().size
            assertEquals("Database must contain zero duplicate stems after merge", 0, duplicateCount)

            // Verify exact JSON correct_answer parity, 4 options, stage allocation, explanations & references
            val rawJson = context.assets.open(LicensingBankImporter.ASSET_FILE_NAME)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            val jsonQuestions = JSONObject(rawJson).getJSONArray("questions")
            assertEquals(530, jsonQuestions.length())

            val expectedAnswerByStem = HashMap<String, String>(530)
            val expectedCategoryByStem = HashMap<String, String>(530)
            val expectedRefByStem = HashMap<String, String>(530)
            val expectedExplByStem = HashMap<String, String>(530)
            for (i in 0 until jsonQuestions.length()) {
                val obj = jsonQuestions.getJSONObject(i)
                val stem = obj.getString("question_en").trim().lowercase()
                expectedAnswerByStem[stem] = obj.getString("correct_answer_en").trim()
                expectedCategoryByStem[stem] = obj.getString("category").trim()
                expectedRefByStem[stem] = obj.optString("reference", "").trim()
                expectedExplByStem[stem] = obj.optString("explanation_en", "").trim()
            }

            val expectedStageCounts = mapOf(
                1 to 30,  // Quality Control & Lab Safety
                2 to 43,  // Urinalysis & Body Fluids
                3 to 52,  // Parasitology
                4 to 90,  // Hematology
                5 to 102, // Clinical Chemistry
                6 to 97,  // Microbiology
                7 to 49,  // Serology & Immunology
                8 to 51,  // Blood Bank
                9 to 16   // Molecular Diagnostics
            )

            val repo = buildRepository(db, context)
            var totalLoadedLicensing = 0
            var explanationsCount = 0
            for ((stage, expectedCount) in expectedStageCounts) {
                val stageQuestions = questionDao.getAllQuestionsForStage(
                    QuestionCategory.MED_LAB_LICENSING,
                    AcademicDegree.BACHELOR,
                    stage
                )
                assertEquals("Unexpected count in licensing stage $stage", expectedCount, stageQuestions.size)
                totalLoadedLicensing += stageQuestions.size

                for (q in stageQuestions) {
                    val options = listOf(q.optionA, q.optionB, q.optionC, q.optionD)
                    assertTrue("All 4 options must be non-blank for Q ${q.id}", options.all { it.isNotBlank() })
                    assertTrue("correctOptionIndex out of range for Q ${q.id}", q.correctOptionIndex in 0..3)
                    assertFalse("Licensing primary question must be English for Q ${q.id}", q.questionEn.any { it in '\u0600'..'\u06FF' })

                    val stem = q.questionEn.trim().lowercase()
                    assertEquals(expectedCategoryByStem[stem], q.subCategory)
                    assertEquals(expectedAnswerByStem[stem], options[q.correctOptionIndex])
                    assertFalse("Reference must be populated for Q ${q.id}", q.scientificReference.isNullOrBlank())
                    assertEquals(expectedRefByStem[stem], q.scientificReference)

                    val expectedExpl = expectedExplByStem[stem].orEmpty()
                    assertEquals(expectedExpl, q.clinicalExplanationEn)
                    if (q.clinicalExplanationEn.isNotBlank()) {
                        explanationsCount++
                    }

                    val arCached = repo.getCachedTranslation(q)
                    assertNotNull("Original Arabic translation must be cached offline for Q ${q.id}", arCached)
                }
            }

            assertEquals(530, totalLoadedLicensing)
            assertEquals(20, explanationsCount)
        } finally {
            db.close()
        }
    }

    @Test
    fun `licensing importer avoids duplicates when merging into populated db and reimports when absent`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = buildTestDb(context)

        try {
            val questionDao = db.questionDao()
            val existingStems = questionDao.getAllNormalizedQuestionStems().toSet()

            // When DB already contains the 530 licensing items, importer must yield 0 items
            val extraToInsert = LicensingBankImporter.loadFromAssets(context, existingStems)
            assertEquals(0, extraToInsert.size)

            // Simulate pre-existing v7 database without MED_LAB_LICENSING rows
            db.openHelper.writableDatabase.execSQL(
                "DELETE FROM questions WHERE specialtyId = '${QuestionCategory.MED_LAB_LICENSING}'"
            )
            assertEquals(9540, questionDao.getTotalQuestionsCount())
            assertEquals(0, questionDao.getQuestionsCountByCategory(QuestionCategory.MED_LAB_LICENSING))

            val repo = buildRepository(db, context)
            repo.initializeDatabaseIfNeeded()

            assertEquals(530, questionDao.getQuestionsCountByCategory(QuestionCategory.MED_LAB_LICENSING))
            assertEquals(10070, questionDao.getTotalQuestionsCount())
            val stemsAfterReimport = questionDao.getAllNormalizedQuestionStems()
            assertEquals(10070, stemsAfterReimport.toSet().size)
        } finally {
            db.close()
        }
    }

    @Test
    fun `licensing track quiz validates answers transitions questions supports replay and integrates with study modes`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = buildTestDb(context)

        try {
            val repo = buildRepository(db, context)
            repo.initializeDatabaseIfNeeded()

            // 1. Flashcards: 9 specialty topics & strict no-repeat until reset
            val topics = repo.getFlashcardTopics(QuestionCategory.MED_LAB_LICENSING, AcademicDegree.BACHELOR)
            assertEquals(9, topics.size)
            val molecularDeck = repo.loadFlashcardDeck(
                QuestionCategory.MED_LAB_LICENSING,
                AcademicDegree.BACHELOR,
                "Molecular Diagnostics"
            )
            assertEquals(16, molecularDeck.totalInScope)
            assertEquals(16, molecularDeck.cards.size)

            // 2. Prometric Timed Exam: 30, 60, 100 unique questions from licensing bank
            for (count in listOf(30, 60, 100)) {
                val pool = repo.getQuestionsForPrometricExam(
                    specialtyId = QuestionCategory.MED_LAB_LICENSING,
                    academicDegree = AcademicDegree.BACHELOR,
                    targetCount = count
                )
                assertEquals(count, pool.questions.size)
                assertEquals(count, pool.questions.map { it.id }.toSet().size)
                assertTrue(pool.questions.all { it.specialtyId == QuestionCategory.MED_LAB_LICENSING })
            }

            // 3. Stage Replay: answer all 16 questions in Stage 9 -> stage still loads 16 questions for replay
            val stage9Questions = repo.getQuestionsForStage(
                QuestionCategory.MED_LAB_LICENSING,
                AcademicDegree.BACHELOR,
                9
            )
            assertEquals(16, stage9Questions.size)
            repo.recordQuestionAnswers(
                stage9Questions.map { q ->
                    AnsweredQuestionEntity(
                        questionId = q.id,
                        specialtyId = q.specialtyId,
                        stageLevel = q.stageLevel,
                        isCorrect = true,
                        selectedOptionIndex = q.correctOptionIndex,
                        academicDegree = q.academicDegree
                    )
                }
            )
            val stage9Replay = repo.getQuestionsForStage(
                QuestionCategory.MED_LAB_LICENSING,
                AcademicDegree.BACHELOR,
                9
            )
            assertEquals(16, stage9Replay.size)

            // 4. Progression caps cleanly at 9 stages for MED_LAB_LICENSING
            repo.updateStageCompletion(
                specialtyId = QuestionCategory.MED_LAB_LICENSING,
                academicDegree = AcademicDegree.BACHELOR,
                completedStage = 9,
                scoreEarned = 2400,
                correctCount = 16,
                totalInRound = 16
            )
            val updatedProgress = repo.getAllProgressDirect()
                .first { it.specialtyId == QuestionCategory.MED_LAB_LICENSING && it.academicDegree == AcademicDegree.BACHELOR }
            assertEquals(9, updatedProgress.highestUnlockedStage)
            assertEquals(9, updatedProgress.completedStagesCount)
            assertEquals(2400, updatedProgress.totalScore)
        } finally {
            db.close()
        }
    }

    private fun waitForMainCondition(timeoutMs: Long = 5000L, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (!condition() && System.currentTimeMillis() < deadline) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(20)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        assertTrue("Timed out waiting for ViewModel condition", condition())
    }

    @Test
    fun `assessment viewModel runs millionaire flow on licensing stage 1 with lifelines and next navigation`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        if (FirebaseApp.getApps(app).isEmpty()) {
            FirebaseApp.initializeApp(app)
        }

        val viewModel = AssessmentViewModel(app)
        if (!viewModel.audioEngine.isMuted.value) {
            viewModel.toggleAudio()
        }

        waitForMainCondition {
            viewModel.allProgress.value.isNotEmpty() || viewModel.searchResults.value.isNotEmpty()
        }

        viewModel.startAssessment(
            specialtyId = QuestionCategory.MED_LAB_LICENSING,
            academicDegree = AcademicDegree.BACHELOR,
            stageLevel = 1
        )
        waitForMainCondition {
            viewModel.sessionState.value.specialtyId == QuestionCategory.MED_LAB_LICENSING &&
                viewModel.sessionState.value.questionQueue.size == 30
        }

        val started = viewModel.sessionState.value
        assertEquals(QuestionCategory.MED_LAB_LICENSING, started.specialtyId)
        assertEquals(30, started.questionQueue.size)
        assertEquals(30, started.questionQueue.map { it.id }.toSet().size)
        assertNotNull(started.currentQuestion)
        assertTrue(viewModel.currentScreen.value is ScreenDestination.Assessment)

        // Test lifelines on Q1
        viewModel.useLifelineHint()
        assertTrue(viewModel.sessionState.value.isHintRevealed)
        viewModel.useLifeline5050()
        assertEquals(2, viewModel.sessionState.value.eliminatedOptionIndices.size)
        viewModel.useLifelineAddTime()
        assertTrue(viewModel.sessionState.value.remainingSeconds > 60)

        // Submit correct answer for Q1
        val q1 = viewModel.sessionState.value.currentQuestion!!
        viewModel.submitAnswer(q1.correctOptionIndex)
        val afterQ1 = viewModel.sessionState.value
        assertTrue(afterQ1.isAnswerSubmitted)
        assertTrue(afterQ1.isAnswerCorrect)
        assertTrue(afterQ1.scoreEarned >= 100)
        assertEquals(5, afterQ1.heartsRemaining)

        // Move to Q2
        viewModel.moveToNextQuestion()
        val atQ2 = viewModel.sessionState.value
        assertEquals(1, atQ2.currentIndex)
        assertFalse(atQ2.isAnswerSubmitted)
        val q2 = atQ2.currentQuestion!!
        assertTrue(q2.id != q1.id)

        // Submit wrong answer for Q2 and check heart deduction
        val wrongIndex = (q2.correctOptionIndex + 1) % 4
        viewModel.submitAnswer(wrongIndex)
        val afterQ2 = viewModel.sessionState.value
        assertTrue(afterQ2.isAnswerSubmitted)
        assertFalse(afterQ2.isAnswerCorrect)
        assertEquals(4, afterQ2.heartsRemaining)

        // Navigate back to ladder stops timer cleanly
        viewModel.navigateBackFromAssessment()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        assertTrue(viewModel.currentScreen.value is ScreenDestination.StageLadder)
    }

    @Test
    fun `flashcard deck strictly prevents repeat until deck reset`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = buildTestDb(context)

        try {
            val repo = buildRepository(db, context)
            val topics = repo.getFlashcardTopics(QuestionCategory.MEDICAL_LABS, AcademicDegree.BACHELOR)
            assertTrue("Expected categorized topics for MED_LAB Bachelor", topics.isNotEmpty())

            val firstTopic = topics.first()
            val initialDeck = repo.loadFlashcardDeck(QuestionCategory.MEDICAL_LABS, AcademicDegree.BACHELOR, firstTopic)
            assertTrue(initialDeck.cards.isNotEmpty())
            assertEquals(initialDeck.totalInScope, initialDeck.unseenCount)

            val studiedCard = initialDeck.cards.first()
            repo.markFlashcardReviewed(studiedCard)

            val reloadedDeck = repo.loadFlashcardDeck(QuestionCategory.MEDICAL_LABS, AcademicDegree.BACHELOR, firstTopic)
            assertEquals(initialDeck.totalInScope - 1, reloadedDeck.unseenCount)
            assertFalse("Reviewed card must not repeat before reset", reloadedDeck.cards.any { it.id == studiedCard.id })

            repo.resetFlashcardDeck(QuestionCategory.MEDICAL_LABS, AcademicDegree.BACHELOR, firstTopic)
            val afterResetDeck = repo.loadFlashcardDeck(QuestionCategory.MEDICAL_LABS, AcademicDegree.BACHELOR, firstTopic)
            assertEquals(initialDeck.totalInScope, afterResetDeck.unseenCount)
            assertTrue("Card should return after deck reset", afterResetDeck.cards.any { it.id == studiedCard.id })
        } finally {
            db.close()
        }
    }

    @Test
    fun `prometric exam pulls exact non-repeating pools 30 60 100 and exam review saves permanently`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = buildTestDb(context)

        try {
            val repo = buildRepository(db, context)

            // 1. Verify exact 30, 60, 100 counts with zero duplicates
            for (count in listOf(30, 60, 100)) {
                val pool = repo.getQuestionsForPrometricExam(
                    specialtyId = QuestionCategory.MEDICAL_LABS,
                    academicDegree = AcademicDegree.BACHELOR,
                    targetCount = count
                )
                assertEquals(count, pool.questions.size)
                assertEquals(count, pool.questions.map { it.id }.toSet().size)
                assertEquals(count, pool.questions.map { it.questionEn.trim().lowercase() }.toSet().size)
            }

            // 2. Mark 30 questions answered, then draw next 30 Prometric questions -> zero overlap!
            val firstExam = repo.getQuestionsForPrometricExam(
                specialtyId = QuestionCategory.NURSING,
                academicDegree = AcademicDegree.BACHELOR,
                targetCount = 30
            )
            val firstIds = firstExam.questions.map { it.id }.toSet()

            repo.recordQuestionAnswers(
                firstExam.questions.mapIndexed { idx, q ->
                    AnsweredQuestionEntity(
                        questionId = q.id,
                        specialtyId = q.specialtyId,
                        stageLevel = q.stageLevel,
                        isCorrect = idx % 2 == 0,
                        selectedOptionIndex = if (idx % 2 == 0) q.correctOptionIndex else (q.correctOptionIndex + 1) % 4,
                        academicDegree = q.academicDegree
                    )
                }
            )

            val secondExam = repo.getQuestionsForPrometricExam(
                specialtyId = QuestionCategory.NURSING,
                academicDegree = AcademicDegree.BACHELOR,
                targetCount = 30
            )
            val secondIds = secondExam.questions.map { it.id }.toSet()
            assertTrue(
                "Second Prometric exam must not repeat previously answered questions",
                firstIds.intersect(secondIds).isEmpty()
            )

            // 3. Save completed quiz session permanently & reload for Exam Review
            val savedSessionId = repo.saveCompletedQuizSession(
                session = QuizSessionEntity(
                    mode = QuizMode.PROMETRIC_TIMED,
                    specialtyId = QuestionCategory.NURSING,
                    academicDegree = AcademicDegree.BACHELOR,
                    stageLevel = 1,
                    topicTitle = "30 min Prometric",
                    durationLimitSeconds = 1800,
                    timeSpentSeconds = 950,
                    totalQuestions = 30,
                    correctCount = 15,
                    scoreEarned = 1800,
                    isPassed = false
                ),
                answers = firstExam.questions.mapIndexed { idx, q ->
                    val chosen = if (idx % 2 == 0) q.correctOptionIndex else (q.correctOptionIndex + 1) % 4
                    QuizSessionAnswerEntity(
                        sessionId = 0L,
                        questionId = q.id,
                        orderIndex = idx,
                        selectedOptionIndex = chosen,
                        isCorrect = chosen == q.correctOptionIndex,
                        wasFlagged = idx == 0 || idx == 5
                    )
                }
            )
            assertTrue(savedSessionId > 0)

            val review = repo.loadQuizSessionForReview(savedSessionId)
            assertNotNull(review)
            assertEquals(30, review!!.items.size)
            assertTrue(review.items[0].wasFlagged)
            assertTrue(review.items[5].wasFlagged)
            assertFalse(review.items[1].wasFlagged)
            assertEquals(firstExam.questions[0].id, review.items[0].question.id)
        } finally {
            db.close()
        }
    }

    @Test
    fun `stage delivers all 102 questions even after partial progress and never repeats intra-stage`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = buildTestDb(context)

        try {
            val repo = buildRepository(db, context)

            // Mark 94 of 102 questions in Licensing Stage 5 (Clinical Chemistry) as already answered
            val fullStage5 = repo.getQuestionsForStage(
                QuestionCategory.MED_LAB_LICENSING,
                AcademicDegree.BACHELOR,
                5
            )
            assertEquals(102, fullStage5.size)

            repo.recordQuestionAnswers(
                fullStage5.take(94).map { q ->
                    AnsweredQuestionEntity(
                        questionId = q.id,
                        specialtyId = q.specialtyId,
                        stageLevel = q.stageLevel,
                        isCorrect = true,
                        selectedOptionIndex = q.correctOptionIndex,
                        academicDegree = q.academicDegree
                    )
                }
            )

            // Resuming Stage 5 must STILL deliver all 102 distinct questions (unseen 8 first + remaining 94)
            val resumedStage5 = repo.getQuestionsForStage(
                QuestionCategory.MED_LAB_LICENSING,
                AcademicDegree.BACHELOR,
                5
            )
            assertEquals(102, resumedStage5.size)
            assertEquals(102, resumedStage5.map { it.id }.toSet().size)

            // Verify MED_LAB Bachelor Stage 2 (102 Qs) also delivers full 102 questions
            val medLabStage2 = repo.getQuestionsForStage(
                QuestionCategory.MEDICAL_LABS,
                AcademicDegree.BACHELOR,
                2
            )
            assertEquals(102, medLabStage2.size)
            assertEquals(102, medLabStage2.map { it.id }.toSet().size)
        } finally {
            db.close()
        }
    }

    @Test
    fun `stage continues past zero hearts and requires 70 percent with 2 stars to unlock`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        if (FirebaseApp.getApps(app).isEmpty()) {
            FirebaseApp.initializeApp(app)
        }

        val viewModel = AssessmentViewModel(app)
        if (!viewModel.audioEngine.isMuted.value) {
            viewModel.toggleAudio()
        }

        waitForMainCondition {
            viewModel.allProgress.value.isNotEmpty() || viewModel.searchResults.value.isNotEmpty()
        }

        // 1. Fail < 70%: Stage 9 (16 Qs) -> 6 wrong + 10 right = 10/16 (62.5%) -> continues past 0 hearts, FAILS (<70%)
        viewModel.startAssessment(
            specialtyId = QuestionCategory.MED_LAB_LICENSING,
            academicDegree = AcademicDegree.BACHELOR,
            stageLevel = 9
        )
        waitForMainCondition {
            viewModel.sessionState.value.specialtyId == QuestionCategory.MED_LAB_LICENSING &&
                viewModel.sessionState.value.stageLevel == 9 &&
                viewModel.sessionState.value.questionQueue.size == 16
        }

        for (i in 0 until 6) {
            val q = viewModel.sessionState.value.currentQuestion!!
            viewModel.submitAnswer((q.correctOptionIndex + 1) % 4)
            assertFalse("Round must continue past 0 hearts at index $i", viewModel.sessionState.value.isRoundComplete)
            viewModel.moveToNextQuestion()
        }
        assertEquals(0, viewModel.sessionState.value.heartsRemaining)

        for (i in 6 until 16) {
            val q = viewModel.sessionState.value.currentQuestion!!
            viewModel.submitAnswer(q.correctOptionIndex)
            viewModel.moveToNextQuestion()
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        assertTrue(viewModel.sessionState.value.isRoundComplete)
        assertFalse("10/16 (62.5% < 70%) must NOT pass stage", viewModel.sessionState.value.isPassed)

        // 2. Pass >= 70%: Replay Stage 9 -> 4 wrong + 12 right = 12/16 (75%) -> PASSES & awards 2 stars
        viewModel.startAssessment(
            specialtyId = QuestionCategory.MED_LAB_LICENSING,
            academicDegree = AcademicDegree.BACHELOR,
            stageLevel = 9
        )
        waitForMainCondition {
            viewModel.sessionState.value.questionQueue.size == 16 &&
                !viewModel.sessionState.value.isRoundComplete
        }
        for (i in 0 until 4) {
            val q = viewModel.sessionState.value.currentQuestion!!
            viewModel.submitAnswer((q.correctOptionIndex + 1) % 4)
            viewModel.moveToNextQuestion()
        }
        for (i in 4 until 16) {
            val q = viewModel.sessionState.value.currentQuestion!!
            viewModel.submitAnswer(q.correctOptionIndex)
            viewModel.moveToNextQuestion()
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        assertTrue(viewModel.sessionState.value.isRoundComplete)
        assertTrue("12/16 (75% >= 70%) must pass stage", viewModel.sessionState.value.isPassed)
        assertEquals(2, StageGradingPolicy.starsForAccuracy(75))
        assertEquals(3, StageGradingPolicy.starsForAccuracy(90))
    }

    @Test
    fun `bidirectional translation detector and Room cache work offline for EN and AR questions`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = buildTestDb(context)

        try {
            val repo = buildRepository(db, context)

            val enQuestion = db.questionDao().getAllQuestionsForStage(
                QuestionCategory.MEDICAL_LABS,
                AcademicDegree.BACHELOR,
                1
            ).first()
            assertEquals(
                MedicalTranslationService.TARGET_ARABIC,
                MedicalTranslationService.detectTargetLanguage(enQuestion)
            )

            // Licensing questions are now English-primary with Arabic cached offline in question_translations
            val licensingQuestion = db.questionDao().getAllQuestionsForStage(
                QuestionCategory.MED_LAB_LICENSING,
                AcademicDegree.BACHELOR,
                1
            ).first()
            assertEquals(
                MedicalTranslationService.TARGET_ARABIC,
                MedicalTranslationService.detectTargetLanguage(licensingQuestion)
            )
            val preCachedArabic = repo.getCachedTranslation(licensingQuestion)
            assertNotNull("Licensing question must have offline Arabic translation cached", preCachedArabic)
            assertTrue(preCachedArabic!!.translatedQuestion.any { it in '\u0600'..'\u06FF' })

            // Verify reverse direction detection (AR -> EN) on an Arabic question stem
            val arabicStemSample = enQuestion.copy(
                questionEn = "ما هو مانع التخثر المستخدم في فحص تعداد الدم الكامل؟"
            )
            assertEquals(
                MedicalTranslationService.TARGET_ENGLISH,
                MedicalTranslationService.detectTargetLanguage(arabicStemSample)
            )

            // Cache new Arabic translation locally in Room and verify offline retrieval
            val arCache = QuestionTranslationEntity(
                questionId = enQuestion.id,
                targetLanguage = MedicalTranslationService.TARGET_ARABIC,
                translatedQuestion = "ترجمة عربية تجريبية للسؤال السريري",
                translatedOptionA = "خيار أ",
                translatedOptionB = "خيار ب",
                translatedOptionC = "خيار ج",
                translatedOptionD = "خيار د",
                translatedExplanation = "شرح علمي مترجم"
            )
            db.questionTranslationDao().upsertTranslation(arCache)
            val fetchedAr = repo.translateAndCacheQuestion(enQuestion)
            assertEquals("ترجمة عربية تجريبية للسؤال السريري", fetchedAr.translatedQuestion)
        } finally {
            db.close()
        }
    }

    @Test
    fun `reviewed 500 nursing questions merge into Diploma Bachelor Master stages with English primary and Arabic cache`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = buildTestDb(context)

        try {
            val questionDao = db.questionDao()
            val repo = buildRepository(db, context)
            assertEquals(3510, questionDao.getQuestionsCountByCategory(QuestionCategory.NURSING))
            assertEquals(1151, questionDao.getQuestionsCountByDegree(QuestionCategory.NURSING, AcademicDegree.DIPLOMA))
            assertEquals(1242, questionDao.getQuestionsCountByDegree(QuestionCategory.NURSING, AcademicDegree.BACHELOR))
            assertEquals(1117, questionDao.getQuestionsCountByDegree(QuestionCategory.NURSING, AcademicDegree.MASTER))

            // Every single one of the 30 Nursing stages (3 degrees x 10 stages) has > 100 questions
            for (degree in AcademicDegree.STANDARD_DEGREES) {
                for (stage in 1..10) {
                    val stageQs = questionDao.getAllQuestionsForStage(QuestionCategory.NURSING, degree, stage)
                    assertTrue("Expected > 100 questions in $degree stage $stage", stageQs.size > 100)
                }
            }

            // Parse JSON ground truth & verify English-primary parity + cached Arabic for all 500 questions
            val rawJson = context.assets.open(NursingBankImporter.ASSET_FILE_NAME)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            val jsonArr = JSONObject(rawJson).getJSONArray("questions")
            assertEquals(500, jsonArr.length())

            val allNursing = questionDao.observeQuestionsByCategory(QuestionCategory.NURSING).first()
            val dbByStem = allNursing.associateBy { it.questionEn.trim().lowercase() }
            val optionIndexCounts = IntArray(4)

            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                val stem = obj.getString("question_en").trim().lowercase()
                val dbRow = dbByStem[stem]
                assertNotNull("Missing merged Nursing English question id=${obj.getInt("id")}", dbRow)
                dbRow!!
                assertFalse("Nursing primary stem must be English for id=${obj.getInt("id")}", dbRow.questionEn.any { it in '\u0600'..'\u06FF' })

                assertEquals(obj.getString("academic_degree"), dbRow.academicDegree)
                assertEquals(obj.getInt("stage_level"), dbRow.stageLevel)
                assertEquals(obj.getString("difficulty_level"), dbRow.difficultyLevel)
                assertEquals(obj.getString("category"), dbRow.subCategory)

                val options = listOf(dbRow.optionA, dbRow.optionB, dbRow.optionC, dbRow.optionD)
                assertTrue(options.all { it.isNotBlank() })
                assertEquals(4, options.toSet().size)
                assertEquals(obj.getString("correct_answer_en").trim(), options[dbRow.correctOptionIndex])
                optionIndexCounts[dbRow.correctOptionIndex]++

                assertTrue("Explanation must be populated for Q id=${obj.getInt("id")}", dbRow.clinicalExplanationEn.isNotBlank())
                assertFalse("Reference must be populated for Q id=${obj.getInt("id")}", dbRow.scientificReference.isNullOrBlank())
                assertEquals(obj.getString("reference").trim(), dbRow.scientificReference)

                val arCached = repo.getCachedTranslation(dbRow)
                assertNotNull("Original Arabic translation must be cached offline for Nursing Q id=${obj.getInt("id")}", arCached)
            }

            // Verify options are well-balanced across A/B/C/D (each > 100)
            for (idx in 0..3) {
                assertTrue("Expected balanced index $idx count > 100, was ${optionIndexCounts[idx]}", optionIndexCounts[idx] > 100)
            }

            // Verify idempotency when DB already has all 3,510 Nursing rows & recovery when 500 rows are missing
            val existingStems = questionDao.getAllNormalizedQuestionStems().toSet()
            assertEquals(0, NursingBankImporter.loadFromAssets(context, existingStems).size)

            db.openHelper.writableDatabase.execSQL(
                "DELETE FROM questions WHERE specialtyId = '${QuestionCategory.NURSING}' AND id > 9570"
            )
            assertEquals(3010, questionDao.getQuestionsCountByCategory(QuestionCategory.NURSING))

            repo.initializeDatabaseIfNeeded()
            assertEquals(3510, questionDao.getQuestionsCountByCategory(QuestionCategory.NURSING))
            assertEquals(10070, questionDao.getTotalQuestionsCount())
        } finally {
            db.close()
        }
    }

    @Test
    fun `laboratory department organizes Diploma Bachelor Master and Consultant levels across 4 tracks`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = buildTestDb(context)

        try {
            val questionDao = db.questionDao()
            assertEquals(4, AcademicDegree.degreesForSpecialty(QuestionCategory.MEDICAL_LABS).size)
            assertEquals(3, AcademicDegree.degreesForSpecialty(QuestionCategory.NURSING).size)
            assertEquals(3, AcademicDegree.degreesForSpecialty(QuestionCategory.PHYSICIAN_ASSISTANT).size)

            val diploma = questionDao.getQuestionsCountByDegree(QuestionCategory.MEDICAL_LABS, AcademicDegree.DIPLOMA)
            val bachelor = questionDao.getQuestionsCountByDegree(QuestionCategory.MEDICAL_LABS, AcademicDegree.BACHELOR)
            val master = questionDao.getQuestionsCountByDegree(QuestionCategory.MEDICAL_LABS, AcademicDegree.MASTER)
            val consultant = questionDao.getQuestionsCountByDegree(QuestionCategory.MEDICAL_LABS, AcademicDegree.CONSULTANT)

            assertEquals(1014, diploma)
            assertEquals(1008, bachelor)
            assertEquals(500, master)
            assertEquals(500, consultant)
            assertEquals(3022, diploma + bachelor + master + consultant)

            for (stage in 1..10) {
                val masterStage = questionDao.getAllQuestionsForStage(QuestionCategory.MEDICAL_LABS, AcademicDegree.MASTER, stage)
                val consultantStage = questionDao.getAllQuestionsForStage(QuestionCategory.MEDICAL_LABS, AcademicDegree.CONSULTANT, stage)
                assertTrue("Expected MASTER stage $stage > 0", masterStage.isNotEmpty())
                assertTrue("Expected CONSULTANT stage $stage > 0", consultantStage.isNotEmpty())
            }
        } finally {
            db.close()
        }
    }

    @Test
    fun `migration 9 to 10 preserves existing user progress and notes while creating Consultant track`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "migration_9_10_${System.nanoTime()}.db"
        val dbFile = context.getDatabasePath(dbName)
        dbFile.parentFile?.mkdirs()

        // Create a real v9 database with existing user progress, answered rows, notes & MASTER stage 6 question
        val sqlite = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        try {
            sqlite.execSQL(
                """
                CREATE TABLE IF NOT EXISTS questions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    specialtyId TEXT NOT NULL,
                    academicDegree TEXT NOT NULL,
                    stageLevel INTEGER NOT NULL,
                    difficultyLevel TEXT NOT NULL,
                    subCategory TEXT NOT NULL,
                    questionEn TEXT NOT NULL,
                    optionA TEXT NOT NULL,
                    optionB TEXT NOT NULL,
                    optionC TEXT NOT NULL,
                    optionD TEXT NOT NULL,
                    correctOptionIndex INTEGER NOT NULL,
                    clinicalExplanationEn TEXT NOT NULL,
                    clinicalHintEn TEXT NOT NULL,
                    imageResName TEXT,
                    diagnosticImageNotes TEXT,
                    scientificReference TEXT
                )
                """.trimIndent()
            )
            sqlite.execSQL("CREATE INDEX IF NOT EXISTS index_questions_specialtyId_academicDegree_stageLevel ON questions(specialtyId, academicDegree, stageLevel)")
            sqlite.execSQL("CREATE INDEX IF NOT EXISTS index_questions_specialtyId_difficultyLevel ON questions(specialtyId, difficultyLevel)")
            sqlite.execSQL("CREATE INDEX IF NOT EXISTS index_questions_imageResName ON questions(imageResName)")
            sqlite.execSQL("CREATE TABLE IF NOT EXISTS answered_questions (questionId INTEGER PRIMARY KEY NOT NULL, specialtyId TEXT NOT NULL, stageLevel INTEGER NOT NULL, isCorrect INTEGER NOT NULL, selectedOptionIndex INTEGER NOT NULL, timestamp INTEGER NOT NULL, academicDegree TEXT NOT NULL DEFAULT 'BACHELOR')")
            sqlite.execSQL("CREATE TABLE IF NOT EXISTS user_progress (specialtyId TEXT NOT NULL, academicDegree TEXT NOT NULL, highestUnlockedStage INTEGER NOT NULL, completedStagesCount INTEGER NOT NULL, totalScore INTEGER NOT NULL, totalCorrectAnswers INTEGER NOT NULL, totalQuestionsAnswered INTEGER NOT NULL, professionalRankEn TEXT NOT NULL, professionalRankAr TEXT NOT NULL, lastAssessmentTimestamp INTEGER NOT NULL, PRIMARY KEY(specialtyId, academicDegree))")
            sqlite.execSQL("CREATE TABLE IF NOT EXISTS user_notes (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, questionId INTEGER, specialtyId TEXT NOT NULL, topic TEXT NOT NULL, title TEXT NOT NULL, content TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
            sqlite.execSQL("CREATE INDEX IF NOT EXISTS index_user_notes_questionId ON user_notes(questionId)")
            sqlite.execSQL("CREATE INDEX IF NOT EXISTS index_user_notes_specialtyId_updatedAt ON user_notes(specialtyId, updatedAt)")
            sqlite.execSQL("CREATE TABLE IF NOT EXISTS applicant_profile (id INTEGER NOT NULL, fullName TEXT NOT NULL, degreeType TEXT NOT NULL, specialty TEXT NOT NULL, age INTEGER NOT NULL, gender TEXT NOT NULL, phoneNumber TEXT NOT NULL, registrationDate INTEGER NOT NULL, PRIMARY KEY(id))")
            sqlite.execSQL("CREATE TABLE IF NOT EXISTS bookmarked_questions (questionId INTEGER NOT NULL, timestamp INTEGER NOT NULL, PRIMARY KEY(questionId))")
            sqlite.execSQL("CREATE TABLE IF NOT EXISTS study_materials (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, specialtyId TEXT NOT NULL, title TEXT NOT NULL, category TEXT NOT NULL, summary TEXT NOT NULL, clinicalContent TEXT NOT NULL, keyPointsJson TEXT NOT NULL, highYieldRating INTEGER NOT NULL, isSavedOffline INTEGER NOT NULL, timestamp INTEGER NOT NULL)")
            sqlite.execSQL("CREATE TABLE IF NOT EXISTS reviewed_flashcards (questionId INTEGER NOT NULL, specialtyId TEXT NOT NULL, academicDegree TEXT NOT NULL, subCategory TEXT NOT NULL, reviewedAt INTEGER NOT NULL, PRIMARY KEY(questionId))")
            sqlite.execSQL("CREATE INDEX IF NOT EXISTS index_reviewed_flashcards_specialtyId_academicDegree_subCategory ON reviewed_flashcards(specialtyId, academicDegree, subCategory)")
            sqlite.execSQL("CREATE TABLE IF NOT EXISTS quiz_sessions (sessionId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, mode TEXT NOT NULL, specialtyId TEXT NOT NULL, academicDegree TEXT NOT NULL, stageLevel INTEGER NOT NULL, topicTitle TEXT, durationLimitSeconds INTEGER NOT NULL, timeSpentSeconds INTEGER NOT NULL, totalQuestions INTEGER NOT NULL, correctCount INTEGER NOT NULL, scoreEarned INTEGER NOT NULL, isPassed INTEGER NOT NULL, completedAt INTEGER NOT NULL)")
            sqlite.execSQL("CREATE INDEX IF NOT EXISTS index_quiz_sessions_completedAt ON quiz_sessions(completedAt)")
            sqlite.execSQL("CREATE INDEX IF NOT EXISTS index_quiz_sessions_mode_completedAt ON quiz_sessions(mode, completedAt)")
            sqlite.execSQL("CREATE TABLE IF NOT EXISTS quiz_session_answers (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, sessionId INTEGER NOT NULL, questionId INTEGER NOT NULL, orderIndex INTEGER NOT NULL, selectedOptionIndex INTEGER NOT NULL, isCorrect INTEGER NOT NULL, wasFlagged INTEGER NOT NULL)")
            sqlite.execSQL("CREATE INDEX IF NOT EXISTS index_quiz_session_answers_sessionId ON quiz_session_answers(sessionId)")
            sqlite.execSQL("CREATE TABLE IF NOT EXISTS question_translations (questionId INTEGER NOT NULL, targetLanguage TEXT NOT NULL, translatedQuestion TEXT NOT NULL, translatedOptionA TEXT NOT NULL, translatedOptionB TEXT NOT NULL, translatedOptionC TEXT NOT NULL, translatedOptionD TEXT NOT NULL, translatedExplanation TEXT NOT NULL, createdAt INTEGER NOT NULL, PRIMARY KEY(questionId, targetLanguage))")

            sqlite.execSQL(
                "INSERT INTO questions (id, specialtyId, academicDegree, stageLevel, difficultyLevel, subCategory, questionEn, optionA, optionB, optionC, optionD, correctOptionIndex, clinicalExplanationEn, clinicalHintEn) " +
                    "VALUES (501, 'MED_LAB', 'MASTER', 6, 'CONSULTANT', 'Immunohematology', 'Rare blood group consultant item?', 'OptA', 'OptB', 'OptC', 'OptD', 2, 'Exp', 'Hint')"
            )
            sqlite.execSQL(
                "INSERT INTO user_progress VALUES ('MED_LAB', 'BACHELOR', 4, 3, 2500, 80, 100, 'Clinical Specialist', 'أخصائي سريري معتمد', 1000)"
            )
            sqlite.execSQL(
                "INSERT INTO answered_questions VALUES (501, 'MED_LAB', 6, 1, 2, 1000, 'MASTER')"
            )
            sqlite.execSQL(
                "INSERT INTO user_notes (questionId, specialtyId, topic, title, content, createdAt, updatedAt) VALUES (501, 'MED_LAB', 'Immunohematology', 'Saved Pearl', 'Keep note intact', 1000, 1000)"
            )
            sqlite.version = 9
        } finally {
            sqlite.close()
        }

        val migratedDb = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8,
                AppDatabase.MIGRATION_8_9,
                AppDatabase.MIGRATION_9_10
            )
            .allowMainThreadQueries()
            .build()
        try {
            val consultantQs = migratedDb.questionDao().getAllQuestionsForStage("MED_LAB", AcademicDegree.CONSULTANT, 2)
            assertEquals(1, consultantQs.size)
            assertEquals("Rare blood group consultant item?", consultantQs.first().questionEn)

            val preservedNote = migratedDb.userNoteDao().getFirstNoteForQuestion(501)
            assertNotNull("User note must survive v9->v10 migration", preservedNote)
            assertEquals("Keep note intact", preservedNote!!.content)

            val preservedProg = migratedDb.userProgressDao().getProgressDirect("MED_LAB", AcademicDegree.BACHELOR)
            assertNotNull(preservedProg)
            assertEquals(4, preservedProg!!.highestUnlockedStage)
            assertEquals(2500, preservedProg.totalScore)

            val backfilledStage1 = migratedDb.stageProgressDao().getStageProgressDirect("MED_LAB", AcademicDegree.BACHELOR, 1)
            assertNotNull(backfilledStage1)
            assertTrue(backfilledStage1!!.isUnlocked)
            assertTrue(backfilledStage1.isPassed)
        } finally {
            migratedDb.close()
            dbFile.delete()
        }
    }

    @Test
    fun `independent option randomization preserves correct answer validation and aligns translations`() {
        val sample = QuestionEntity(
            id = 42L,
            specialtyId = QuestionCategory.MEDICAL_LABS,
            academicDegree = AcademicDegree.BACHELOR,
            stageLevel = 1,
            difficultyLevel = "CLINICAL_RESIDENT",
            subCategory = "Hematology",
            questionEn = "Which stain is standard for peripheral blood smears?",
            optionA = "Ziehl-Neelsen",
            optionB = "Wright-Giemsa",
            optionC = "India ink",
            optionD = "Sudan black only",
            correctOptionIndex = 1,
            clinicalExplanationEn = "Wright-Giemsa is standard.",
            clinicalHintEn = "Romanowsky stain"
        )
        val arTranslation = QuestionTranslationEntity(
            questionId = 42L,
            targetLanguage = MedicalTranslationService.TARGET_ARABIC,
            translatedQuestion = "أي صبغة قياسية لمسحة الدم المحيطي؟",
            translatedOptionA = "تسييل-نيلسن",
            translatedOptionB = "رايت-غيمزا",
            translatedOptionC = "الحبر الصيني",
            translatedOptionD = "سودان بلاك فقط",
            translatedExplanation = "صبغة رايت-غيمزا قياسية."
        )

        val ordersSeen = HashSet<List<Int>>()
        repeat(24) { seed ->
            val shuffled = QuestionShuffleUtil.shuffleOptionsWithMap(sample, Random(seed))
            val opts = listOf(shuffled.question.optionA, shuffled.question.optionB, shuffled.question.optionC, shuffled.question.optionD)
            assertEquals("Wright-Giemsa", opts[shuffled.question.correctOptionIndex])
            assertEquals(4, opts.toSet().size)
            ordersSeen.add(shuffled.displayedToOriginalIndex)

            val aligned = QuestionShuffleUtil.alignTranslationOptions(arTranslation, shuffled.displayedToOriginalIndex)
            val alignedOpts = listOf(aligned.translatedOptionA, aligned.translatedOptionB, aligned.translatedOptionC, aligned.translatedOptionD)
            assertEquals("رايت-غيمزا", alignedOpts[shuffled.question.correctOptionIndex])

            val roundTripped = QuestionShuffleUtil.canonicalizeTranslationOptions(aligned, shuffled.displayedToOriginalIndex)
            assertEquals(arTranslation.translatedOptionA, roundTripped.translatedOptionA)
            assertEquals(arTranslation.translatedOptionB, roundTripped.translatedOptionB)
            assertEquals(arTranslation.translatedOptionC, roundTripped.translatedOptionC)
            assertEquals(arTranslation.translatedOptionD, roundTripped.translatedOptionD)
        }
        assertTrue("Expected distinct option permutations across sessions", ordersSeen.size > 4)
    }

    @Test
    fun `bilingual search matches English terms options Arabic cached translations and department filters offline`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = buildTestDb(context)

        try {
            val repo = buildRepository(db, context)

            // 1. Arabic query matches cached Arabic translation offline
            val arabicMatches = repo.searchQuestions("التنفس", specialtyId = QuestionCategory.NURSING)
            assertTrue("Expected Arabic cached search results in NURSING", arabicMatches.isNotEmpty())
            assertTrue(arabicMatches.all { it.specialtyId == QuestionCategory.NURSING })

            // 2. English query matches English prompt/options
            val englishMatches = repo.searchQuestions("anticoagulant", specialtyId = QuestionCategory.MED_LAB_LICENSING)
            assertTrue("Expected English search results in MED_LAB_LICENSING", englishMatches.isNotEmpty())
            assertTrue(englishMatches.all { it.specialtyId == QuestionCategory.MED_LAB_LICENSING })

            // 3. Department filter restricts results strictly to selected specialty
            val labMatches = repo.searchQuestions("hemoglobin", specialtyId = QuestionCategory.MEDICAL_LABS)
            assertTrue(labMatches.isNotEmpty())
            assertTrue(labMatches.all { it.specialtyId == QuestionCategory.MEDICAL_LABS })
        } finally {
            db.close()
        }
    }

    @Test
    fun `offline mutation queues sync and reconnecting triggers automatic Firestore push banner`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Application>()
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        val vm = AssessmentViewModel(context)
        val fakeOnline = MutableStateFlow(false)
        var pushCalls = 0
        var shouldSucceed = false

        vm.testSignedInOverride = true
        vm.syncExecutorOverrideForTest = {
            pushCalls++
            if (shouldSucceed) Result.success(Unit)
            else Result.failure(IllegalStateException("Simulated transient Firestore timeout"))
        }
        vm.installConnectivityObserverForTest(object : ConnectivityObserver {
            override val isOnline: StateFlow<Boolean> = fakeOnline
        })

        // 1. Offline local progress mutation queues sync and activates banner
        vm.recordOfflineProgressMutationForTest()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        delay(50)
        assertTrue(vm.syncUiState.value is SyncUiState.OfflinePending)
        assertTrue(vm.isSyncBannerVisible.value)

        // 2. Reconnect while push fails -> transitions cleanly to Error state with Retry affordance
        fakeOnline.value = true
        repeat(15) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            delay(30)
            if (vm.syncUiState.value is SyncUiState.Error) return@repeat
        }
        assertEquals(1, pushCalls)
        assertTrue("Expected Error state on failed reconnect push", vm.syncUiState.value is SyncUiState.Error)
        assertTrue(vm.isSyncBannerVisible.value)

        // 3. Go offline again, then restore connectivity with healthy network -> auto-pushes cleanly
        fakeOnline.value = false
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        delay(30)
        shouldSucceed = true
        fakeOnline.value = true
        repeat(20) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            delay(30)
            if (vm.syncUiState.value is SyncUiState.Synced) return@repeat
        }
        assertEquals(2, pushCalls)
        val synced = vm.syncUiState.value as? SyncUiState.Synced
        assertNotNull("Expected Synced state after connection restored", synced)
        assertTrue(synced!!.isReconnectAutoPush)
        assertTrue(vm.isSyncBannerVisible.value)

        // 4. Dismiss action collapses banner immediately
        vm.dismissSyncBanner()
        assertFalse(vm.isSyncBannerVisible.value)
    }

    @Test
    fun `prometric navigation tracks forward and backward slide directions`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Application>()
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        val vm = AssessmentViewModel(context)
        delay(120)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        vm.startPrometricExam()
        repeat(20) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            delay(40)
            if (vm.prometricState.value.questions.isNotEmpty()) return@repeat
        }

        assertTrue(vm.prometricState.value.questions.size >= 3)
        assertEquals(0, vm.prometricState.value.currentIndex)

        vm.nextPrometricQuestion()
        assertEquals(1, vm.prometricState.value.currentIndex)
        assertEquals(1, vm.prometricState.value.navigationDirection)

        vm.jumpToPrometricQuestion(4)
        assertEquals(4, vm.prometricState.value.currentIndex)
        assertEquals(1, vm.prometricState.value.navigationDirection)

        vm.prevPrometricQuestion()
        assertEquals(3, vm.prometricState.value.currentIndex)
        assertEquals(-1, vm.prometricState.value.navigationDirection)

        vm.cancelPrometricExamAndExit()
    }

    @Test
    fun `all 13 diagnostic atlas WebP assets open and validate cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals(13, ATLAS_ITEMS.size)
        for (item in ATLAS_ITEMS) {
            assertTrue("Expected WebP atlas path: ${item.assetPath}", item.assetPath.endsWith(".webp"))
            val bytes = context.assets.open(item.assetPath).use { it.readBytes() }
            assertTrue("WebP file too small for ${item.assetPath}", bytes.size > 1024)
            assertEquals("RIFF", String(bytes, 0, 4, Charsets.US_ASCII))
            assertEquals("WEBP", String(bytes, 8, 4, Charsets.US_ASCII))

            val bitmap = bytes.inputStream().use { stream ->
                BitmapFactory.decodeStream(stream)
            }
            assertNotNull("Failed to decode ${item.assetPath}", bitmap)
            assertTrue(bitmap!!.width > 0 && bitmap.height > 0)
            bitmap.recycle()
        }
    }

    @Test
    fun `coil atlas imageLoader enforces bounded memory and disk cache and loads WebP asset`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val loader = ClinicalBoardApplication.createAtlasImageLoader(context)
        try {
            assertNotNull("MemoryCache must be configured", loader.memoryCache)
            assertTrue("MemoryCache maxSize must be > 0", loader.memoryCache!!.maxSize > 0)
            assertNotNull("DiskCache must be configured", loader.diskCache)
            assertEquals(ClinicalBoardApplication.DISK_CACHE_MAX_BYTES, loader.diskCache!!.maxSize)
            assertEquals(ClinicalBoardApplication.DISK_CACHE_DIR_NAME, loader.diskCache!!.directory.name)

            val request = ImageRequest.Builder(context)
                .data("file:///android_asset/${ATLAS_ITEMS.first().assetPath}")
                .size(512, 400)
                .scale(Scale.FILL)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()

            val resultRef = java.util.concurrent.atomic.AtomicReference<coil.request.ImageResult?>(null)
            val errRef = java.util.concurrent.atomic.AtomicReference<Throwable?>(null)
            val latch = java.util.concurrent.CountDownLatch(1)
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    resultRef.set(loader.execute(request))
                } catch (t: Throwable) {
                    errRef.set(t)
                } finally {
                    latch.countDown()
                }
            }

            val deadline = System.currentTimeMillis() + 15_000L
            while (latch.count > 0 && System.currentTimeMillis() < deadline) {
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                Thread.sleep(20)
            }
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            errRef.get()?.let { throw AssertionError("Coil execute threw", it) }
            val result = resultRef.get()
            assertTrue("Expected Coil SuccessResult for WebP asset within timeout, got $result", result is SuccessResult)
        } finally {
            loader.shutdown()
        }
    }
}
