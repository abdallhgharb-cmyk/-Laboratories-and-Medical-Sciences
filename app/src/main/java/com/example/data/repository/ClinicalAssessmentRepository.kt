package com.example.data.repository

import android.content.Context
import com.example.data.dao.ApplicantProfileDao
import com.example.data.dao.FlashcardDao
import com.example.data.dao.QuestionDao
import com.example.data.dao.QuestionTranslationDao
import com.example.data.dao.QuizHistoryDao
import com.example.data.dao.StageProgressDao
import com.example.data.dao.StudyMaterialDao
import com.example.data.dao.TopicMasteryTuple
import com.example.data.dao.UserNoteDao
import com.example.data.dao.UserProgressDao
import com.example.data.model.AcademicDegree
import com.example.data.model.AnsweredQuestionEntity
import com.example.data.model.ApplicantProfileEntity
import com.example.data.model.BookmarkedQuestionEntity
import com.example.data.model.QuestionCategory
import com.example.data.model.QuestionEntity
import com.example.data.model.QuestionTranslationEntity
import com.example.data.model.QuizSessionAnswerEntity
import com.example.data.model.QuizSessionEntity
import com.example.data.model.ReviewedFlashcardEntity
import com.example.data.model.StageGradingPolicy
import com.example.data.model.StageProgressEntity
import com.example.data.model.StudyMaterialEntity
import com.example.data.model.UserNoteEntity
import com.example.data.model.UserProgressEntity
import com.example.data.remote.MedicalTranslationService
import com.example.data.seed.BilingualQuestionPair
import com.example.data.seed.LicensingBankImporter
import com.example.data.seed.NursingBankImporter
import com.example.data.seed.StudyMaterialSeedData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class PrometricExamPool(
    val questions: List<QuestionEntity>,
    val unseenUsedCount: Int,
    val totalUnseenAvailableBeforeStart: Int
)

data class FlashcardDeckLoad(
    val cards: List<QuestionEntity>,
    val totalInScope: Int,
    val unseenCount: Int
)

data class ReviewQuestionEntry(
    val question: QuestionEntity,
    val selectedOptionIndex: Int,
    val isCorrect: Boolean,
    val wasFlagged: Boolean
)

data class LoadedQuizReview(
    val session: QuizSessionEntity,
    val items: List<ReviewQuestionEntry>
)

class ClinicalAssessmentRepository(
    private val questionDao: QuestionDao,
    private val userProgressDao: UserProgressDao,
    private val applicantProfileDao: ApplicantProfileDao,
    private val studyMaterialDao: StudyMaterialDao,
    private val userNoteDao: UserNoteDao,
    private val flashcardDao: FlashcardDao,
    private val quizHistoryDao: QuizHistoryDao,
    private val appContext: Context? = null,
    private val questionTranslationDao: QuestionTranslationDao? = null,
    private val translationService: MedicalTranslationService? = null,
    private val stageProgressDao: StageProgressDao? = null
) {

    val allStudyMaterials: Flow<List<StudyMaterialEntity>> = studyMaterialDao.observeAllMaterials()
    val offlineSavedMaterials: Flow<List<StudyMaterialEntity>> = studyMaterialDao.observeOfflineSavedMaterials()

    suspend fun getStudyMaterialsForSpecialty(specialtyId: String): Flow<List<StudyMaterialEntity>> {
        return studyMaterialDao.observeMaterialsBySpecialty(specialtyId)
    }

    suspend fun toggleStudyMaterialOffline(id: Long, isSaved: Boolean) = withContext(Dispatchers.IO) {
        studyMaterialDao.updateOfflineStatus(id, isSaved)
    }

    suspend fun searchStudyMaterials(query: String): List<StudyMaterialEntity> = withContext(Dispatchers.IO) {
        studyMaterialDao.searchMaterials(query)
    }

    suspend fun initializeDatabaseIfNeeded() = withContext(Dispatchers.IO) {
        if (studyMaterialDao.countMaterials() == 0) {
            studyMaterialDao.insertMaterials(StudyMaterialSeedData.getInitialMaterials())
        }

        if (appContext != null && questionDao.getQuestionsCountByCategory(QuestionCategory.MED_LAB_LICENSING) == 0) {
            val existingStems = questionDao.getAllNormalizedQuestionStems().toSet()
            val licensingPairs = LicensingBankImporter.parseBilingualJson(
                appContext.assets.open(LicensingBankImporter.ASSET_FILE_NAME)
                    .bufferedReader(Charsets.UTF_8).use { it.readText() },
                existingStems
            )
            insertBilingualPairs(licensingPairs)
        }

        if (appContext != null &&
            questionDao.getQuestionsCountByCategory(QuestionCategory.NURSING) < NursingBankImporter.EXPECTED_TOTAL_NURSING_AFTER_MERGE
        ) {
            val existingStems = questionDao.getAllNormalizedQuestionStems().toSet()
            val nursingPairs = NursingBankImporter.parseBilingualJson(
                appContext.assets.open(NursingBankImporter.ASSET_FILE_NAME)
                    .bufferedReader(Charsets.UTF_8).use { it.readText() },
                existingStems
            )
            insertBilingualPairs(nursingPairs)
        }

        // Upgrade existing on-device Arabic rows to English-primary + cached Arabic translation in-place
        if (appContext != null) {
            syncEnglishPrimaryAndArabicCacheForSpecialty(
                specialtyId = QuestionCategory.MED_LAB_LICENSING,
                pairs = LicensingBankImporter.loadBilingualFromAssets(appContext)
            )
            syncEnglishPrimaryAndArabicCacheForSpecialty(
                specialtyId = QuestionCategory.NURSING,
                pairs = NursingBankImporter.loadBilingualFromAssets(appContext)
            )
        }

        QuestionCategory.REGISTERED_DEPARTMENTS.forEach { spec ->
            spec.degrees.forEach { degree ->
                val existing = userProgressDao.getProgressDirect(spec.id, degree)
                if (existing == null) {
                    val (rankEn, rankAr) = calculateRank(1, degree, spec.id)
                    userProgressDao.saveProgress(
                        UserProgressEntity(
                            specialtyId = spec.id,
                            academicDegree = degree,
                            highestUnlockedStage = 1,
                            completedStagesCount = 0,
                            totalScore = 0,
                            totalCorrectAnswers = 0,
                            totalQuestionsAnswered = 0,
                            professionalRankEn = rankEn,
                            professionalRankAr = rankAr
                        )
                    )
                }
                if (stageProgressDao != null &&
                    stageProgressDao.getStageProgressDirect(spec.id, degree, 1) == null
                ) {
                    stageProgressDao.upsertStageProgress(
                        StageProgressEntity(
                            specialtyId = spec.id,
                            academicDegree = degree,
                            stageLevel = 1,
                            isUnlocked = true
                        )
                    )
                }
            }
        }

        if (applicantProfileDao.getProfileDirect() == null) {
            applicantProfileDao.saveProfile(
                ApplicantProfileEntity(
                    id = 1,
                    fullName = "د. متقدم مزاولة المهنة",
                    degreeType = "بكالوريوس",
                    specialty = "مختبرات طبية",
                    age = 25,
                    gender = "ذكر",
                    phoneNumber = "+967773148358"
                )
            )
        }
    }

    private suspend fun insertBilingualPairs(pairs: List<BilingualQuestionPair>) {
        if (pairs.isEmpty()) return
        pairs.chunked(200).forEach { chunk ->
            questionDao.insertQuestions(chunk.map { it.question })
        }
        val translationDao = questionTranslationDao ?: return
        val stemToId = questionDao.getQuestionsByCategoryDirect(pairs.first().question.specialtyId)
            .associate { it.questionEn.trim().lowercase() to it.id }
        val translations = pairs.mapNotNull { pair ->
            val qId = stemToId[pair.question.questionEn.trim().lowercase()] ?: return@mapNotNull null
            pair.arabicTranslation?.copy(questionId = qId)
        }
        if (translations.isNotEmpty()) {
            translations.chunked(200).forEach { batch ->
                translationDao.upsertTranslations(batch)
            }
        }
    }

    private suspend fun syncEnglishPrimaryAndArabicCacheForSpecialty(
        specialtyId: String,
        pairs: List<BilingualQuestionPair>
    ) {
        if (pairs.isEmpty()) return
        val existingRows = questionDao.getQuestionsByCategoryDirect(specialtyId)
        if (existingRows.isEmpty()) return

        val byArabicStem = pairs.associateBy { it.arabicStemNormalized }
        val byEnglishStem = pairs.associateBy { it.question.questionEn.trim().lowercase() }

        val updatedQuestions = mutableListOf<QuestionEntity>()
        val translationsToCache = mutableListOf<QuestionTranslationEntity>()
        val existingTranslationsCount = questionTranslationDao?.countTranslationsByLanguage(
            MedicalTranslationService.TARGET_ARABIC
        ) ?: 0

        // Fast skip if all rows in this specialty already use English stems and Arabic cache exists
        val hasArabicStemInDb = existingRows.any { containsArabic(it.questionEn) }
        if (!hasArabicStemInDb && existingTranslationsCount >= pairs.size) {
            return
        }

        for (row in existingRows) {
            val norm = row.questionEn.trim().lowercase()
            val matchedPair = byArabicStem[norm] ?: byEnglishStem[norm] ?: continue
            if (containsArabic(row.questionEn) && !containsArabic(matchedPair.question.questionEn)) {
                updatedQuestions.add(
                    row.copy(
                        questionEn = matchedPair.question.questionEn,
                        optionA = matchedPair.question.optionA,
                        optionB = matchedPair.question.optionB,
                        optionC = matchedPair.question.optionC,
                        optionD = matchedPair.question.optionD,
                        correctOptionIndex = matchedPair.question.correctOptionIndex,
                        clinicalExplanationEn = matchedPair.question.clinicalExplanationEn,
                        clinicalHintEn = matchedPair.question.clinicalHintEn,
                        scientificReference = matchedPair.question.scientificReference ?: row.scientificReference
                    )
                )
            }
            matchedPair.arabicTranslation?.let { ar ->
                translationsToCache.add(ar.copy(questionId = row.id))
            }
        }

        if (updatedQuestions.isNotEmpty()) {
            updatedQuestions.chunked(200).forEach { batch ->
                questionDao.insertQuestions(batch)
            }
        }
        if (translationsToCache.isNotEmpty() && questionTranslationDao != null) {
            translationsToCache.chunked(200).forEach { batch ->
                questionTranslationDao.upsertTranslations(batch)
            }
        }
    }

    private fun containsArabic(text: String): Boolean =
        text.any { it in '\u0600'..'\u06FF' }

    private fun deduplicateQuestions(candidates: List<QuestionEntity>): List<QuestionEntity> {
        val seenIds = HashSet<Long>(candidates.size)
        val seenStems = HashSet<String>(candidates.size)
        return candidates.filter { q ->
            val normalizedStem = q.questionEn.trim().lowercase()
            seenIds.add(q.id) && seenStems.add(normalizedStem)
        }
    }

    suspend fun getQuestionsForStage(
        specialtyId: String,
        academicDegree: String = AcademicDegree.BACHELOR,
        stageLevel: Int
    ): List<QuestionEntity> = withContext(Dispatchers.IO) {
        val allInStage = deduplicateQuestions(
            questionDao.getAllQuestionsForStage(specialtyId, academicDegree, stageLevel)
        )
        val unanswered = deduplicateQuestions(
            questionDao.getUnansweredQuestionsForStage(specialtyId, academicDegree, stageLevel)
        )
        if (unanswered.isEmpty()) {
            allInStage.shuffled()
        } else {
            val unansweredIds = unanswered.map { it.id }.toSet()
            val answeredInStage = allInStage.filter { it.id !in unansweredIds }.shuffled()
            unanswered.shuffled() + answeredInStage
        }
    }

    suspend fun getCachedTranslation(question: QuestionEntity): QuestionTranslationEntity? =
        withContext(Dispatchers.IO) {
            val targetLang = MedicalTranslationService.detectTargetLanguage(question)
            questionTranslationDao?.getTranslation(question.id, targetLang)
        }

    suspend fun getCachedTranslationsForIds(
        questionIds: List<Long>,
        targetLanguage: String = MedicalTranslationService.TARGET_ARABIC
    ): Map<Long, QuestionTranslationEntity> = withContext(Dispatchers.IO) {
        if (questionIds.isEmpty() || questionTranslationDao == null) return@withContext emptyMap()
        questionIds.distinct()
            .chunked(500)
            .flatMap { chunk -> questionTranslationDao.getTranslationsByIds(chunk, targetLanguage) }
            .associateBy { it.questionId }
    }

    suspend fun translateAndCacheQuestion(question: QuestionEntity): QuestionTranslationEntity =
        withContext(Dispatchers.IO) {
            val targetLang = MedicalTranslationService.detectTargetLanguage(question)
            val cached = questionTranslationDao?.getTranslation(question.id, targetLang)
            if (cached != null) return@withContext cached

            try {
                val service = translationService ?: MedicalTranslationService()
                val translated = service.translateQuestion(question)
                questionTranslationDao?.upsertTranslation(translated)
                translated
            } catch (e: Exception) {
                android.util.Log.e("ClinicalRepo", "Translation or App Check failed, returning offline fallback", e)
                val fallback = QuestionTranslationEntity(
                    questionId = question.id,
                    targetLanguage = targetLang,
                    translatedQuestion = question.questionEn,
                    translatedOptionA = question.optionA,
                    translatedOptionB = question.optionB,
                    translatedOptionC = question.optionC,
                    translatedOptionD = question.optionD,
                    translatedExplanation = if (question.clinicalExplanationEn.isNotBlank()) {
                        question.clinicalExplanationEn
                    } else {
                        "تعذر الاتصال بخدمة الذكاء الاصطناعي (App Check). تم عرض النصوص الأساسية محلياً بكفاءة كاملة."
                    }
                )
                questionTranslationDao?.upsertTranslation(fallback)
                fallback
            }
        }

    suspend fun translateAndCacheQuestion(
        question: QuestionEntity,
        displayedToOriginalIndex: List<Int>?
    ): QuestionTranslationEntity = withContext(Dispatchers.IO) {
        val targetLang = MedicalTranslationService.detectTargetLanguage(question)
        val cached = questionTranslationDao?.getTranslation(question.id, targetLang)
        if (cached != null) {
            return@withContext com.example.data.model.QuestionShuffleUtil.alignTranslationOptions(
                cached,
                displayedToOriginalIndex
            )
        }

        try {
            val service = translationService ?: MedicalTranslationService()
            val translatedDisplayed = service.translateQuestion(question)
            val canonicalForDb = com.example.data.model.QuestionShuffleUtil.canonicalizeTranslationOptions(
                translatedDisplayed,
                displayedToOriginalIndex
            )
            questionTranslationDao?.upsertTranslation(canonicalForDb)
            translatedDisplayed
        } catch (e: Exception) {
            android.util.Log.e("ClinicalRepo", "Translation or App Check failed in shuffled view, returning offline fallback", e)
            val fallback = QuestionTranslationEntity(
                questionId = question.id,
                targetLanguage = targetLang,
                translatedQuestion = question.questionEn,
                translatedOptionA = question.optionA,
                translatedOptionB = question.optionB,
                translatedOptionC = question.optionC,
                translatedOptionD = question.optionD,
                translatedExplanation = if (question.clinicalExplanationEn.isNotBlank()) {
                    question.clinicalExplanationEn
                } else {
                    "تعذر الاتصال بخدمة الذكاء الاصطناعي (App Check). تم عرض النصوص الأساسية محلياً بكفاءة كاملة."
                }
            )
            val canonicalFallback = com.example.data.model.QuestionShuffleUtil.canonicalizeTranslationOptions(
                fallback,
                displayedToOriginalIndex
            )
            questionTranslationDao?.upsertTranslation(canonicalFallback)
            if (displayedToOriginalIndex != null) {
                com.example.data.model.QuestionShuffleUtil.alignTranslationOptions(fallback, displayedToOriginalIndex)
            } else {
                fallback
            }
        }
    }

    suspend fun getQuestionsForStage(specialtyId: String, stageLevel: Int): List<QuestionEntity> {
        return getQuestionsForStage(specialtyId, AcademicDegree.BACHELOR, stageLevel)
    }

    suspend fun getQuestionsForTopicPractice(
        specialtyId: String,
        subCategory: String,
        academicDegree: String? = null
    ): List<QuestionEntity> = withContext(Dispatchers.IO) {
        val targetDegree = academicDegree ?: AcademicDegree.BACHELOR
        val unseen = deduplicateQuestions(
            questionDao.getUnansweredQuestionsByTopic(specialtyId, targetDegree, subCategory)
        )
        val pool = if (unseen.isNotEmpty()) {
            unseen
        } else {
            val allInTopic = questionDao.getAllQuestionsByTopic(specialtyId, targetDegree, subCategory)
            if (allInTopic.isNotEmpty()) deduplicateQuestions(allInTopic)
            else deduplicateQuestions(
                questionDao.searchQuestions(subCategory).filter { q ->
                    q.specialtyId == specialtyId &&
                        q.subCategory == subCategory &&
                        (academicDegree == null || q.academicDegree == academicDegree)
                }
            )
        }
        pool.shuffled().take(25)
    }

    suspend fun getDailyChallengeQuestions(): List<QuestionEntity> = withContext(Dispatchers.IO) {
        val unseenPool = deduplicateQuestions(questionDao.getRandomUnansweredQuestions(60))
        if (unseenPool.size >= 5) {
            val pickedMedLab = unseenPool.filter { it.specialtyId == "MED_LAB" }.take(2)
            val pickedNursing = unseenPool.filter { it.specialtyId == "NURSING" }.take(2)
            val pickedPa = unseenPool.filter { it.specialtyId == "PHYSICIAN_ASSISTANT" }.take(1)
            val combined = deduplicateQuestions(pickedMedLab + pickedNursing + pickedPa)
            if (combined.size >= 5) return@withContext combined.shuffled()
            return@withContext deduplicateQuestions(combined + unseenPool).take(5).shuffled()
        }

        val medLab = questionDao.getAllQuestionsForStage("MED_LAB", AcademicDegree.BACHELOR, 1)
        val nursing = questionDao.getAllQuestionsForStage("NURSING", AcademicDegree.BACHELOR, 1)
        val pa = questionDao.getAllQuestionsForStage("PHYSICIAN_ASSISTANT", AcademicDegree.BACHELOR, 1)
        deduplicateQuestions(medLab.shuffled().take(2) + nursing.shuffled().take(2) + pa.shuffled().take(1)).shuffled()
    }

    suspend fun getUnansweredCountForTrack(specialtyId: String, academicDegree: String): Int =
        withContext(Dispatchers.IO) {
            questionDao.getUnansweredCountForTrack(specialtyId, academicDegree)
        }

    suspend fun getQuestionsForPrometricExam(
        specialtyId: String,
        academicDegree: String,
        targetCount: Int
    ): PrometricExamPool = withContext(Dispatchers.IO) {
        val primaryUnseen = deduplicateQuestions(
            questionDao.getAllUnansweredQuestionsForDegree(specialtyId, academicDegree).shuffled()
        )
        val selected = mutableListOf<QuestionEntity>()
        val usedIds = HashSet<Long>()
        val usedStems = HashSet<String>()

        fun appendDistinct(source: List<QuestionEntity>) {
            for (q in source) {
                if (selected.size >= targetCount) break
                val stem = q.questionEn.trim().lowercase()
                if (usedIds.add(q.id) && usedStems.add(stem)) {
                    selected.add(q)
                }
            }
        }

        appendDistinct(primaryUnseen)

        // If the selected degree track has fewer unseen items than targetCount, pull unseen from sibling degrees in same specialty
        if (selected.size < targetCount) {
            val siblingUnseen = questionDao.getAllUnansweredQuestionsForSpecialty(specialtyId).shuffled()
            appendDistinct(siblingUnseen)
        }

        val unseenCountUsed = selected.size

        val maxStage = QuestionCategory.maxStagesForSpecialty(specialtyId)
        // Only if the entire specialty's unseen bank has been exhausted, supplement without repeating inside the exam
        if (selected.size < targetCount) {
            val fallback = (1..maxStage).flatMap { stage ->
                questionDao.getAllQuestionsForStage(specialtyId, academicDegree, stage)
            }.shuffled()
            appendDistinct(fallback)
        }

        PrometricExamPool(
            questions = selected,
            unseenUsedCount = unseenCountUsed,
            totalUnseenAvailableBeforeStart = primaryUnseen.size
        )
    }

    suspend fun searchQuestions(
        query: String,
        specialtyId: String? = null,
        academicDegree: String? = null
    ): List<QuestionEntity> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank() && specialtyId == null && academicDegree == null) {
            questionDao.getAllQuestionsForStage("MED_LAB", AcademicDegree.BACHELOR, 1)
        } else {
            deduplicateQuestions(
                questionDao.searchQuestionsFiltered(
                    query = cleanQuery,
                    specialtyId = specialtyId,
                    academicDegree = academicDegree
                )
            )
        }
    }

    // ==========================================
    // Bookmarks / Favorites Feature
    // ==========================================
    fun observeBookmarkedQuestions(): Flow<List<QuestionEntity>> = questionDao.observeBookmarkedQuestions()
    fun observeBookmarkedIds(): Flow<List<Long>> = questionDao.observeBookmarkedIds()

    suspend fun toggleBookmark(questionId: Long) = withContext(Dispatchers.IO) {
        val isSaved = questionDao.isBookmarked(questionId)
        if (isSaved) {
            questionDao.removeBookmark(questionId)
        } else {
            questionDao.addBookmark(BookmarkedQuestionEntity(questionId = questionId))
        }
    }

    // ==========================================
    // Mistakes & Analytics Features
    // ==========================================
    fun observeMistakeQuestions(): Flow<List<QuestionEntity>> = questionDao.observeMistakeQuestions()
    fun observeMistakeCount(): Flow<Int> = questionDao.observeMistakeCount()
    fun observeTotalCorrectCount(): Flow<Int> = questionDao.observeTotalCorrectAnswersCount()
    fun observeTotalAnsweredCount(): Flow<Int> = questionDao.observeTotalAnsweredCount()
    fun observeTopicMasteryStats(): Flow<List<TopicMasteryTuple>> = questionDao.observeTopicMasteryStats()

    // ==========================================
    // Offline Student Notes (Question-Linked + Standalone)
    // ==========================================
    fun observeAllNotes(): Flow<List<UserNoteEntity>> = userNoteDao.observeAllNotes()
    fun observeNoteQuestionIds(): Flow<List<Long>> = userNoteDao.observeNoteQuestionIds()
    fun observeNotesForQuestion(questionId: Long): Flow<List<UserNoteEntity>> =
        userNoteDao.observeNotesForQuestion(questionId)

    suspend fun getAllNotesDirect(): List<UserNoteEntity> = withContext(Dispatchers.IO) {
        userNoteDao.getAllNotesDirect()
    }

    suspend fun getFirstNoteForQuestion(questionId: Long): UserNoteEntity? = withContext(Dispatchers.IO) {
        userNoteDao.getFirstNoteForQuestion(questionId)
    }

    suspend fun saveNote(
        id: Long = 0,
        questionId: Long? = null,
        specialtyId: String = "GENERAL",
        topic: String,
        title: String,
        content: String,
        createdAt: Long? = null,
        updatedAt: Long = System.currentTimeMillis()
    ): UserNoteEntity = withContext(Dispatchers.IO) {
        val existing = if (id > 0L) userNoteDao.getNoteById(id) else null
        val entity = UserNoteEntity(
            id = existing?.id ?: id,
            questionId = questionId,
            specialtyId = specialtyId,
            topic = topic.ifBlank { "General" },
            title = title.trim(),
            content = content.trim(),
            createdAt = existing?.createdAt ?: createdAt ?: updatedAt,
            updatedAt = updatedAt
        )
        val newId = userNoteDao.upsertNote(entity)
        entity.copy(id = if (entity.id > 0L) entity.id else newId)
    }

    suspend fun deleteNoteById(noteId: Long) = withContext(Dispatchers.IO) {
        userNoteDao.deleteNoteById(noteId)
    }

    suspend fun mergeCloudNotes(cloudNotes: List<UserNoteEntity>) = withContext(Dispatchers.IO) {
        val existingNotes = userNoteDao.getAllNotesDirect()
        cloudNotes.forEach { remote ->
            val bySameId = if (remote.id > 0L) existingNotes.find { it.id == remote.id } else null
            val byContentMatch = existingNotes.find {
                it.questionId == remote.questionId &&
                    it.title.equals(remote.title, ignoreCase = true) &&
                    it.specialtyId == remote.specialtyId
            }
            val matched = when {
                bySameId != null && (bySameId.questionId == remote.questionId && bySameId.title == remote.title) -> bySameId
                byContentMatch != null -> byContentMatch
                else -> null
            }
            if (matched == null) {
                val safeId = if (bySameId == null && remote.id > 0L) remote.id else 0L
                userNoteDao.upsertNote(remote.copy(id = safeId))
            } else if (remote.updatedAt > matched.updatedAt) {
                userNoteDao.upsertNote(
                    matched.copy(
                        topic = remote.topic,
                        title = remote.title,
                        content = remote.content,
                        updatedAt = remote.updatedAt
                    )
                )
            }
        }
    }

    // ==========================================
    // Applicant Profile & Licensing
    // ==========================================
    fun getApplicantProfile(): Flow<ApplicantProfileEntity?> = applicantProfileDao.getProfile()

    suspend fun saveApplicantProfile(profile: ApplicantProfileEntity) = withContext(Dispatchers.IO) {
        applicantProfileDao.saveProfile(profile)
    }

    suspend fun getAlternativeQuestion(
        specialtyId: String,
        academicDegree: String,
        stageLevel: Int,
        currentQuestionId: Long
    ): QuestionEntity? = withContext(Dispatchers.IO) {
        val unseenInStage = questionDao.getUnansweredQuestionsForStage(specialtyId, academicDegree, stageLevel)
        val altUnseen = unseenInStage.firstOrNull { it.id != currentQuestionId }
        if (altUnseen != null) return@withContext altUnseen

        val stageQuestions = questionDao.getAllQuestionsForStage(specialtyId, academicDegree, stageLevel)
        val altInStage = stageQuestions.firstOrNull { it.id != currentQuestionId }
        if (altInStage != null) return@withContext altInStage

        val maxStage = QuestionCategory.maxStagesForSpecialty(specialtyId)
        val neighborStage = when {
            stageLevel > 1 -> stageLevel - 1
            stageLevel < maxStage -> stageLevel + 1
            else -> stageLevel
        }
        questionDao.getAllQuestionsForStage(specialtyId, academicDegree, neighborStage)
            .firstOrNull { it.id != currentQuestionId }
    }

    suspend fun recordQuestionAnswer(
        questionId: Long,
        specialtyId: String,
        academicDegree: String = AcademicDegree.BACHELOR,
        stageLevel: Int,
        isCorrect: Boolean,
        selectedOptionIndex: Int
    ) = withContext(Dispatchers.IO) {
        questionDao.recordAnswer(
            AnsweredQuestionEntity(
                questionId = questionId,
                specialtyId = specialtyId,
                stageLevel = stageLevel,
                isCorrect = isCorrect,
                selectedOptionIndex = selectedOptionIndex,
                academicDegree = academicDegree
            )
        )
    }

    suspend fun recordQuestionAnswers(answers: List<AnsweredQuestionEntity>) = withContext(Dispatchers.IO) {
        if (answers.isNotEmpty()) {
            questionDao.recordAnswers(answers)
        }
    }

    // ==========================================
    // Categorized Flashcard Mode (Zero-Duplication)
    // ==========================================
    suspend fun getFlashcardTopics(specialtyId: String, academicDegree: String): List<String> =
        withContext(Dispatchers.IO) {
            flashcardDao.getDistinctSubCategories(specialtyId, academicDegree)
        }

    suspend fun loadFlashcardDeck(
        specialtyId: String,
        academicDegree: String,
        subCategoryFilter: String?
    ): FlashcardDeckLoad = withContext(Dispatchers.IO) {
        val unseen = deduplicateQuestions(
            flashcardDao.getUnseenFlashcards(specialtyId, academicDegree, subCategoryFilter)
        ).shuffled()
        val total = flashcardDao.getTotalFlashcardsInScope(specialtyId, academicDegree, subCategoryFilter)
        val unseenCount = flashcardDao.getUnseenFlashcardsCount(specialtyId, academicDegree, subCategoryFilter)
        FlashcardDeckLoad(
            cards = unseen,
            totalInScope = total,
            unseenCount = unseenCount
        )
    }

    suspend fun markFlashcardReviewed(question: QuestionEntity) = withContext(Dispatchers.IO) {
        flashcardDao.markFlashcardReviewed(
            ReviewedFlashcardEntity(
                questionId = question.id,
                specialtyId = question.specialtyId,
                academicDegree = question.academicDegree,
                subCategory = question.subCategory
            )
        )
    }

    suspend fun resetFlashcardDeck(
        specialtyId: String,
        academicDegree: String,
        subCategoryFilter: String?
    ) = withContext(Dispatchers.IO) {
        if (subCategoryFilter.isNullOrBlank()) {
            flashcardDao.resetDeck(specialtyId, academicDegree)
        } else {
            flashcardDao.resetDeckByTopic(specialtyId, academicDegree, subCategoryFilter)
        }
    }

    // ==========================================
    // Permanent Exam History & Review
    // ==========================================
    fun observeQuizSessions(): Flow<List<QuizSessionEntity>> = quizHistoryDao.observeAllSessions()

    suspend fun saveCompletedQuizSession(
        session: QuizSessionEntity,
        answers: List<QuizSessionAnswerEntity>
    ): Long = withContext(Dispatchers.IO) {
        quizHistoryDao.saveCompletedQuiz(session, answers)
    }

    suspend fun loadQuizSessionForReview(sessionId: Long): LoadedQuizReview? = withContext(Dispatchers.IO) {
        val session = quizHistoryDao.getSessionById(sessionId) ?: return@withContext null
        val answerRows = quizHistoryDao.getAnswersForSession(sessionId)
        if (answerRows.isEmpty()) return@withContext LoadedQuizReview(session, emptyList())

        val questionMap = answerRows
            .map { it.questionId }
            .distinct()
            .chunked(500)
            .flatMap { chunk -> questionDao.getQuestionsByIds(chunk) }
            .associateBy { it.id }

        val entries = answerRows.mapNotNull { ans ->
            val q = questionMap[ans.questionId] ?: return@mapNotNull null
            ReviewQuestionEntry(
                question = q,
                selectedOptionIndex = ans.selectedOptionIndex,
                isCorrect = ans.isCorrect,
                wasFlagged = ans.wasFlagged
            )
        }
        LoadedQuizReview(session = session, items = entries)
    }

    suspend fun deleteQuizSession(sessionId: Long) = withContext(Dispatchers.IO) {
        quizHistoryDao.deleteSessionWithAnswers(sessionId)
    }

    fun getProgress(specialtyId: String, academicDegree: String): Flow<UserProgressEntity?> {
        return userProgressDao.getProgress(specialtyId, academicDegree)
    }

    fun getProgress(specialtyId: String): Flow<UserProgressEntity?> {
        return userProgressDao.getProgress(specialtyId)
    }

    fun getAllProgress(): Flow<List<UserProgressEntity>> {
        return userProgressDao.getAllProgress()
    }

    fun observeStageProgress(
        specialtyId: String,
        academicDegree: String
    ): Flow<List<StageProgressEntity>> =
        stageProgressDao?.observeStageProgress(specialtyId, academicDegree) ?: flowOf(emptyList())

    fun observeAllStageProgress(): Flow<List<StageProgressEntity>> =
        stageProgressDao?.observeAllStageProgress() ?: flowOf(emptyList())

    fun observeStageQuestionCounts(
        specialtyId: String,
        academicDegree: String
    ): Flow<Map<Int, Int>> = questionDao.observeStageQuestionCounts(specialtyId, academicDegree)
        .map { list -> list.associate { it.stageLevel to it.questionCount } }

    fun observeDegreeQuestionCounts(
        specialtyId: String
    ): Flow<Map<String, Int>> = questionDao.observeDegreeQuestionCounts(specialtyId)
        .map { list -> list.associate { it.academicDegree to it.questionCount } }

    suspend fun getAllProgressDirect(): List<UserProgressEntity> = withContext(Dispatchers.IO) {
        val list = mutableListOf<UserProgressEntity>()
        QuestionCategory.REGISTERED_DEPARTMENTS.forEach { spec ->
            spec.degrees.forEach { d ->
                userProgressDao.getProgressDirect(spec.id, d)?.let { list.add(it) }
            }
        }
        list
    }

    suspend fun mergeCloudProgress(cloudProgressList: List<UserProgressEntity>) = withContext(Dispatchers.IO) {
        cloudProgressList.forEach { remote ->
            val local = userProgressDao.getProgressDirect(remote.specialtyId, remote.academicDegree)
            if (local == null) {
                userProgressDao.saveProgress(remote)
            } else {
                val mergedHighest = maxOf(local.highestUnlockedStage, remote.highestUnlockedStage)
                val (rankEn, rankAr) = calculateRank(mergedHighest, remote.academicDegree, remote.specialtyId)
                userProgressDao.saveProgress(
                    local.copy(
                        highestUnlockedStage = mergedHighest,
                        completedStagesCount = maxOf(local.completedStagesCount, remote.completedStagesCount),
                        totalScore = maxOf(local.totalScore, remote.totalScore),
                        totalCorrectAnswers = maxOf(local.totalCorrectAnswers, remote.totalCorrectAnswers),
                        totalQuestionsAnswered = maxOf(local.totalQuestionsAnswered, remote.totalQuestionsAnswered),
                        professionalRankEn = rankEn,
                        professionalRankAr = rankAr,
                        lastAssessmentTimestamp = maxOf(local.lastAssessmentTimestamp, remote.lastAssessmentTimestamp)
                    )
                )
            }
        }
    }

    /**
     * Evaluates and persists stage completion / attempt under the uniform 70% pass & 2-star policy.
     */
    suspend fun updateStageCompletion(
        specialtyId: String,
        academicDegree: String,
        completedStage: Int,
        scoreEarned: Int,
        correctCount: Int,
        totalInRound: Int
    ) = withContext(Dispatchers.IO) {
        val maxStages = QuestionCategory.maxStagesForSpecialty(specialtyId)
        val accuracy = StageGradingPolicy.calculateAccuracyPercent(correctCount, totalInRound)
        val passed = StageGradingPolicy.isStagePassed(correctCount, totalInRound)
        val earnedStars = StageGradingPolicy.starsForAccuracy(accuracy)

        val existingStage = stageProgressDao?.getStageProgressDirect(specialtyId, academicDegree, completedStage)
        val bestAccuracy = maxOf(existingStage?.bestAccuracyPercent ?: 0, accuracy)
        val bestStars = maxOf(existingStage?.starsEarned ?: 0, earnedStars)
        val bestCorrect = maxOf(existingStage?.bestCorrectCount ?: 0, correctCount)
        val bestTotal = if (accuracy >= (existingStage?.bestAccuracyPercent ?: 0)) {
            totalInRound
        } else {
            existingStage?.bestTotalCount ?: totalInRound
        }

        stageProgressDao?.upsertStageProgress(
            StageProgressEntity(
                specialtyId = specialtyId,
                academicDegree = academicDegree,
                stageLevel = completedStage,
                bestCorrectCount = bestCorrect,
                bestTotalCount = bestTotal,
                bestAccuracyPercent = bestAccuracy,
                starsEarned = bestStars,
                isUnlocked = true,
                isPassed = (existingStage?.isPassed == true) || passed,
                updatedAt = System.currentTimeMillis()
            )
        )

        val nextStage = (completedStage + 1).coerceAtMost(maxStages)
        if (passed && completedStage < maxStages) {
            val existingNext = stageProgressDao?.getStageProgressDirect(specialtyId, academicDegree, nextStage)
            stageProgressDao?.upsertStageProgress(
                (existingNext ?: StageProgressEntity(
                    specialtyId = specialtyId,
                    academicDegree = academicDegree,
                    stageLevel = nextStage
                )).copy(
                    isUnlocked = true,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        val existing = userProgressDao.getProgressDirect(specialtyId, academicDegree) ?: UserProgressEntity(
            specialtyId = specialtyId,
            academicDegree = academicDegree
        )
        val newHighest = if (passed) {
            maxOf(existing.highestUnlockedStage, nextStage)
        } else {
            existing.highestUnlockedStage
        }
        val newCompleted = if (passed) {
            maxOf(existing.completedStagesCount, minOf(maxStages, completedStage))
        } else {
            existing.completedStagesCount
        }
        val newScore = existing.totalScore + scoreEarned
        val newCorrect = existing.totalCorrectAnswers + correctCount
        val newTotal = existing.totalQuestionsAnswered + totalInRound

        val (rankEn, rankAr) = calculateRank(newHighest, academicDegree, specialtyId)

        val updated = existing.copy(
            highestUnlockedStage = newHighest,
            completedStagesCount = newCompleted,
            totalScore = newScore,
            totalCorrectAnswers = newCorrect,
            totalQuestionsAnswered = newTotal,
            professionalRankEn = rankEn,
            professionalRankAr = rankAr,
            lastAssessmentTimestamp = System.currentTimeMillis()
        )
        userProgressDao.saveProgress(updated)
    }

    suspend fun resetStageAnswers(specialtyId: String, academicDegree: String, stageLevel: Int) =
        withContext(Dispatchers.IO) {
            questionDao.resetStageAnswers(specialtyId, academicDegree, stageLevel)
        }

    suspend fun resetSpecialtyProgress(specialtyId: String, academicDegree: String) = withContext(Dispatchers.IO) {
        questionDao.resetSpecialtyAnswers(specialtyId, academicDegree)
        stageProgressDao?.resetTrackStageProgress(specialtyId, academicDegree)
        stageProgressDao?.upsertStageProgress(
            StageProgressEntity(
                specialtyId = specialtyId,
                academicDegree = academicDegree,
                stageLevel = 1,
                isUnlocked = true
            )
        )
        val (rankEn, rankAr) = calculateRank(1, academicDegree, specialtyId)
        userProgressDao.saveProgress(
            UserProgressEntity(
                specialtyId = specialtyId,
                academicDegree = academicDegree,
                highestUnlockedStage = 1,
                completedStagesCount = 0,
                totalScore = 0,
                totalCorrectAnswers = 0,
                totalQuestionsAnswered = 0,
                professionalRankEn = rankEn,
                professionalRankAr = rankAr
            )
        )
    }

    private fun calculateRank(
        highestStage: Int,
        degree: String,
        specialtyId: String = QuestionCategory.MEDICAL_LABS
    ): Pair<String, String> {
        if (specialtyId == QuestionCategory.MED_LAB_LICENSING) {
            return when (highestStage) {
                in 1..3 -> Pair("Licensing Candidate", "مرشح مزاولة المهنة")
                in 4..7 -> Pair("Qualified Lab Specialist", "أخصائي مختبرات مؤهل للمزاولة")
                else -> Pair("Licensed Medical Lab Expert", "خبير معتمد لامتحان مزاولة المهنة")
            }
        }
        return when (degree) {
            AcademicDegree.DIPLOMA -> when (highestStage) {
                in 1..3 -> Pair("Trainee Technician", "فني متدرب")
                in 4..7 -> Pair("Certified Technician", "فني سريري معتمد")
                else -> Pair("Senior Technician", "فني أول معتمد")
            }
            AcademicDegree.BACHELOR -> when (highestStage) {
                in 1..3 -> Pair("Junior Specialist", "أخصائي متدرب")
                in 4..7 -> Pair("Clinical Specialist", "أخصائي سريري معتمد")
                else -> Pair("Senior Specialist", "أخصائي أول / استشاري مساعد")
            }
            AcademicDegree.MASTER -> when (highestStage) {
                in 1..3 -> Pair("Master Resident", "ماجستير سريري متدرب")
                in 4..7 -> Pair("Senior Master Specialist", "أخصائي ماجستير معتمد")
                else -> Pair("Lead Laboratory Scientist", "كبير أخصائيي المختبر والماجستير")
            }
            AcademicDegree.CONSULTANT -> when (highestStage) {
                in 1..3 -> Pair("Board Consultant Fellow", "زميل استشاري مختبرات")
                in 4..7 -> Pair("Clinical Lab Consultant", "استشاري مختبرات سريرية معتمد")
                else -> Pair("Chief Board Consultant", "كبير الاستشاريين ومدير الجودة CAP")
            }
            else -> when (highestStage) {
                in 1..3 -> Pair("Advanced Practitioner", "ممارس متقدم")
                in 4..7 -> Pair("Clinical Consultant", "استشاري سريري معتمد")
                else -> Pair("Master Board Consultant", "استشاري بورد أول وموجه سريري")
            }
        }
    }
}
