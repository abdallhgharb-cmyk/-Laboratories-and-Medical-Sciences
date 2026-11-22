package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEngine
import com.example.data.db.AppDatabase
import com.example.data.model.AcademicDegree
import com.example.data.model.AnsweredQuestionEntity
import com.example.data.model.ApplicantProfileEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.QuestionShuffleUtil
import com.example.data.model.QuestionTranslationEntity
import com.example.data.model.QuizMode
import com.example.data.model.QuizSessionAnswerEntity
import com.example.data.model.QuizSessionEntity
import com.example.data.model.StageGradingPolicy
import com.example.data.model.StageProgressEntity
import com.example.data.model.UserNoteEntity
import com.example.data.model.UserProgressEntity
import com.example.data.remote.AuthManager
import com.example.data.remote.CloudSyncRepository
import com.example.data.remote.ConnectivityObserver
import com.example.data.remote.MedicalTranslationService
import com.example.data.remote.NetworkConnectivityMonitor
import com.example.data.repository.ClinicalAssessmentRepository
import com.example.data.repository.ReviewQuestionEntry
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DashboardLanguage {
    EN,
    AR
}

enum class TopicMasteryStatus {
    MASTERED,
    IN_PROGRESS,
    NEEDS_REVIEW,
    UNSTARTED
}

data class TopicMasteryUi(
    val subCategory: String,
    val specialtyId: String,
    val academicDegree: String,
    val totalQuestions: Int,
    val answeredCount: Int,
    val correctCount: Int,
    val accuracyPercent: Int,
    val masteryStatus: TopicMasteryStatus
)

sealed interface CloudAuthUiState {
    data object SignedOut : CloudAuthUiState
    data object SigningIn : CloudAuthUiState
    data class SignedIn(
        val uid: String,
        val displayName: String,
        val email: String,
        val photoUrl: String?
    ) : CloudAuthUiState
    data class Error(val message: String) : CloudAuthUiState
}

sealed interface SyncUiState {
    data object Idle : SyncUiState
    data class OfflinePending(val pendingChangesCount: Int = 1) : SyncUiState
    data class Syncing(val isReconnectAutoPush: Boolean = false) : SyncUiState
    data class Synced(val timestampMs: Long, val isReconnectAutoPush: Boolean = false) : SyncUiState
    data class Error(val message: String) : SyncUiState
}

sealed class ScreenDestination {
    object Home : ScreenDestination()
    object Dashboard : ScreenDestination() // لوحة معلومات التقدم
    object DailyChallenge : ScreenDestination() // التحدي اليومي
    object Search : ScreenDestination() // البحث
    object Favorites : ScreenDestination() // المفضلة
    object Mistakes : ScreenDestination() // قائمة الأخطاء
    object Certificate : ScreenDestination() // شهادة التقييم وترخيص المزاولة
    object Profile : ScreenDestination() // تسجيل بيانات المتقدم
    object Atlas : ScreenDestination() // الأطلس المجهري عالي الدقة
    object Flashcards : ScreenDestination() // البطاقات التعليمية السريعة
    object PrometricSetup : ScreenDestination() // إعداد اختبار برومترك المؤقت
    object PrometricExam : ScreenDestination() // اختبار برومترك المؤقت (30/60/120 دقيقة)
    object ExamHistory : ScreenDestination() // سجل الاختبارات والمراجعة الدائمة
    data class StageLadder(
        val specialtyId: String,
        val academicDegree: String = AcademicDegree.BACHELOR
    ) : ScreenDestination()
    data class Assessment(
        val specialtyId: String,
        val stageLevel: Int = 1,
        val academicDegree: String = AcademicDegree.BACHELOR
    ) : ScreenDestination()
    data class Result(
        val specialtyId: String,
        val stageLevel: Int = 1,
        val isPassed: Boolean = false,
        val academicDegree: String = AcademicDegree.BACHELOR,
        val isPrometricTimed: Boolean = false
    ) : ScreenDestination()
    data class Review(
        val specialtyId: String,
        val stageLevel: Int = 1,
        val academicDegree: String = AcademicDegree.BACHELOR,
        val sessionId: Long? = null,
        val fromHistory: Boolean = false,
        val isPrometricTimed: Boolean = false
    ) : ScreenDestination()
}

data class QuestionTranslationUiState(
    val isLoading: Boolean = false,
    val isExpanded: Boolean = false,
    val translation: QuestionTranslationEntity? = null,
    val errorMessage: String? = null
)

data class RoundAnswerRecord(
    val question: QuestionEntity,
    val selectedOptionIndex: Int, // -1 if timed out
    val isCorrect: Boolean,
    val explanation: String
)

data class AssessmentSessionState(
    val specialtyId: String = "MED_LAB",
    val academicDegree: String = AcademicDegree.BACHELOR,
    val stageLevel: Int = 1,
    val isDailyChallenge: Boolean = false,
    val isTopicPractice: Boolean = false,
    val activeTopicTitle: String? = null,
    val questionQueue: List<QuestionEntity> = emptyList(),
    val currentIndex: Int = 0,
    val currentQuestion: QuestionEntity? = null,
    val selectedOptionIndex: Int? = null,
    val isAnswerSubmitted: Boolean = false,
    val isAnswerCorrect: Boolean = false,
    val remainingSeconds: Int = 60, // ميزة المؤقت 60 ثانية
    val isTimerActive: Boolean = false,
    val heartsRemaining: Int = 5,
    val scoreEarned: Int = 0,
    val eliminatedOptionIndices: Set<Int> = emptySet(),
    val isHintRevealed: Boolean = false,
    val lifeline5050Used: Boolean = false,
    val lifelineHintUsed: Boolean = false,
    val lifelineSwapUsed: Boolean = false,
    val lifelineTimeUsed: Boolean = false,
    val answersHistory: List<RoundAnswerRecord> = emptyList(),
    val isRoundComplete: Boolean = false,
    val isPassed: Boolean = false,
    val enlargedImageResName: String? = null,
    val isPrometricTimed: Boolean = false,
    val savedSessionId: Long? = null,
    val optionPermutations: Map<Long, List<Int>> = emptyMap()
)

class AssessmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ClinicalAssessmentRepository
    private val cloudSyncRepository: CloudSyncRepository
    val audioEngine: AudioEngine

    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Home)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _selectedDegree = MutableStateFlow<String>(AcademicDegree.BACHELOR)
    val selectedDegree: StateFlow<String> = _selectedDegree.asStateFlow()

    private val _sessionState = MutableStateFlow(AssessmentSessionState())
    val sessionState: StateFlow<AssessmentSessionState> = _sessionState.asStateFlow()

    private val _showSupervisorProfile = MutableStateFlow(false)
    val showSupervisorProfile: StateFlow<Boolean> = _showSupervisorProfile.asStateFlow()

    private val _showContactDialog = MutableStateFlow(false)
    val showContactDialog: StateFlow<Boolean> = _showContactDialog.asStateFlow()

    // Dashboard language defaults to English with instant Arabic translation toggle
    private val _dashboardLanguage = MutableStateFlow(DashboardLanguage.EN)
    val dashboardLanguage: StateFlow<DashboardLanguage> = _dashboardLanguage.asStateFlow()

    // Cloud Authentication & Sync state (Optional sign-in, offline-first anytime)
    private val _authUiState = MutableStateFlow<CloudAuthUiState>(CloudAuthUiState.SignedOut)
    val authUiState: StateFlow<CloudAuthUiState> = _authUiState.asStateFlow()

    private val _syncUiState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncUiState: StateFlow<SyncUiState> = _syncUiState.asStateFlow()

    private val _isSyncBannerVisible = MutableStateFlow(false)
    val isSyncBannerVisible: StateFlow<Boolean> = _isSyncBannerVisible.asStateFlow()

    private val syncPrefs = application.getSharedPreferences("cloud_sync_queue_prefs", Context.MODE_PRIVATE)
    private var connectivityObserver: ConnectivityObserver
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchSpecialtyFilter = MutableStateFlow<String?>(null)
    val searchSpecialtyFilter: StateFlow<String?> = _searchSpecialtyFilter.asStateFlow()

    private val _searchDegreeFilter = MutableStateFlow<String?>(null)
    val searchDegreeFilter: StateFlow<String?> = _searchDegreeFilter.asStateFlow()

    private val _searchResults = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val searchResults: StateFlow<List<QuestionEntity>> = _searchResults.asStateFlow()

    private val _searchTranslations = MutableStateFlow<Map<Long, QuestionTranslationEntity>>(emptyMap())
    val searchTranslations: StateFlow<Map<Long, QuestionTranslationEntity>> = _searchTranslations.asStateFlow()

    // Daily Challenge streak
    private val _dailyStreak = MutableStateFlow(3)
    val dailyStreak: StateFlow<Int> = _dailyStreak.asStateFlow()

    // Flashcard Mode, Prometric Timed Exam & Exam Review History state
    private val _flashcardState = MutableStateFlow(FlashcardUiState())
    val flashcardState: StateFlow<FlashcardUiState> = _flashcardState.asStateFlow()

    private val _prometricState = MutableStateFlow(PrometricExamUiState())
    val prometricState: StateFlow<PrometricExamUiState> = _prometricState.asStateFlow()

    private val _loadedReviewState = MutableStateFlow(LoadedReviewUiState())
    val loadedReviewState: StateFlow<LoadedReviewUiState> = _loadedReviewState.asStateFlow()

    private val _translationsState = MutableStateFlow<Map<Long, QuestionTranslationUiState>>(emptyMap())
    val translationsState: StateFlow<Map<Long, QuestionTranslationUiState>> = _translationsState.asStateFlow()

    private var timerJob: Job? = null
    private var prometricTimerJob: Job? = null
    private var syncJob: Job? = null
    private var bannerAutoDismissJob: Job? = null
    private var connectivityCollectorJob: Job? = null
    private var autoSignInAttempted = false
    private var syncedUidThisSession: String? = null
    private var wasOffline = false

    @Volatile
    internal var syncExecutorOverrideForTest: (suspend () -> Result<Unit>)? = null

    @Volatile
    internal var testSignedInOverride: Boolean = false

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ClinicalAssessmentRepository(
            questionDao = db.questionDao(),
            userProgressDao = db.userProgressDao(),
            applicantProfileDao = db.applicantProfileDao(),
            studyMaterialDao = db.studyMaterialDao(),
            userNoteDao = db.userNoteDao(),
            flashcardDao = db.flashcardDao(),
            quizHistoryDao = db.quizHistoryDao(),
            appContext = application.applicationContext,
            questionTranslationDao = db.questionTranslationDao(),
            translationService = MedicalTranslationService(),
            stageProgressDao = db.stageProgressDao()
        )
        cloudSyncRepository = CloudSyncRepository(application)
        connectivityObserver = NetworkConnectivityMonitor(application)
        audioEngine = AudioEngine(application)
        _isOnline.value = connectivityObserver.isOnline.value
        wasOffline = !connectivityObserver.isOnline.value

        viewModelScope.launch {
            repository.initializeDatabaseIfNeeded()
            performSearch("")
        }

        connectivityCollectorJob = viewModelScope.launch {
            connectivityObserver.isOnline.collectLatest { online ->
                handleConnectivityStateChange(online)
            }
        }

        // Observe Firebase Auth state reactively (never attaches Firestore listeners while unauthenticated)
        viewModelScope.launch {
            AuthManager.authStateFlow().collectLatest { user ->
                if (user != null) {
                    _authUiState.value = user.toSignedInUiState()
                    if (syncedUidThisSession != user.uid) {
                        syncedUidThisSession = user.uid
                        if (isOnline.value) {
                            syncBidirectional(user, isReconnectAutoPush = false, showBannerOnSync = hasPendingCloudSync())
                        } else if (hasPendingCloudSync()) {
                            _syncUiState.value = SyncUiState.OfflinePending(getPendingChangesCount())
                            _isSyncBannerVisible.value = true
                        }
                    }
                } else if (!testSignedInOverride) {
                    syncedUidThisSession = null
                    _authUiState.value = CloudAuthUiState.SignedOut
                    _syncUiState.value = SyncUiState.Idle
                    _isSyncBannerVisible.value = false
                }
            }
        }
    }

    val studyMaterials: StateFlow<List<com.example.data.model.StudyMaterialEntity>> = repository.allStudyMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProgress: StateFlow<List<UserProgressEntity>> = repository.getAllProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStageProgress: StateFlow<List<StageProgressEntity>> = repository.observeAllStageProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedQuestions: StateFlow<List<QuestionEntity>> = repository.observeBookmarkedQuestions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedIds: StateFlow<List<Long>> = repository.observeBookmarkedIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mistakeQuestions: StateFlow<List<QuestionEntity>> = repository.observeMistakeQuestions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mistakeCount: StateFlow<Int> = repository.observeMistakeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCorrectCount: StateFlow<Int> = repository.observeTotalCorrectCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalAnsweredCount: StateFlow<Int> = repository.observeTotalAnsweredCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val applicantProfile: StateFlow<ApplicantProfileEntity?> = repository.getApplicantProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allNotes: StateFlow<List<UserNoteEntity>> = repository.observeAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notedQuestionIds: StateFlow<Set<Long>> = repository.observeNoteQuestionIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val quizHistory: StateFlow<List<QuizSessionEntity>> = repository.observeQuizSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topicMasteryStats: StateFlow<List<TopicMasteryUi>> = repository.observeTopicMasteryStats()
        .map { tuples ->
            tuples.map { t ->
                val accuracy = if (t.answeredCount > 0) {
                    ((t.correctCount.toFloat() / t.answeredCount) * 100f).toInt().coerceIn(0, 100)
                } else {
                    0
                }
                val status = when {
                    t.answeredCount == 0 -> TopicMasteryStatus.UNSTARTED
                    t.answeredCount >= 5 && accuracy >= 80 -> TopicMasteryStatus.MASTERED
                    accuracy >= 50 -> TopicMasteryStatus.IN_PROGRESS
                    else -> TopicMasteryStatus.NEEDS_REVIEW
                }
                TopicMasteryUi(
                    subCategory = t.subCategory,
                    specialtyId = t.specialtyId,
                    academicDegree = t.academicDegree,
                    totalQuestions = t.totalQuestions,
                    answeredCount = t.answeredCount,
                    correctCount = t.correctCount,
                    accuracyPercent = accuracy,
                    masteryStatus = status
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateTo(destination: ScreenDestination) {
        if (_currentScreen.value is ScreenDestination.Assessment && destination !is ScreenDestination.Assessment) {
            stopTimer()
            audioEngine.stopFocusAmbience()
        }
        if (_currentScreen.value is ScreenDestination.PrometricExam && destination !is ScreenDestination.PrometricExam) {
            stopPrometricTimer()
            audioEngine.stopFocusAmbience()
        }
        if (destination is ScreenDestination.StageLadder) {
            _selectedDegree.value = destination.academicDegree
        }
        _currentScreen.value = destination
    }

    fun navigateBackFromAssessment() {
        val state = _sessionState.value
        val target = when {
            state.isPrometricTimed -> ScreenDestination.PrometricSetup
            state.isDailyChallenge -> ScreenDestination.DailyChallenge
            state.isTopicPractice -> ScreenDestination.Dashboard
            else -> ScreenDestination.StageLadder(state.specialtyId, state.academicDegree)
        }
        if (hasPendingCloudSync() && isOnline.value && isAuthenticatedForSync()) {
            viewModelScope.launch {
                flushPendingCloudSync(isReconnectAutoPush = false, showBannerOnSync = false)
            }
        }
        navigateTo(target)
    }

    fun observeStageProgress(specialtyId: String, academicDegree: String): Flow<Map<Int, StageProgressEntity>> =
        repository.observeStageProgress(specialtyId, academicDegree)
            .map { list -> list.associateBy { it.stageLevel } }

    fun observeStageQuestionCounts(specialtyId: String, academicDegree: String): Flow<Map<Int, Int>> =
        repository.observeStageQuestionCounts(specialtyId, academicDegree)

    fun observeDegreeQuestionCounts(specialtyId: String): Flow<Map<String, Int>> =
        repository.observeDegreeQuestionCounts(specialtyId)

    // ==========================================
    // Categorized Flashcard Mode (Zero-Duplication)
    // ==========================================
    private fun resolveSupportedDegree(specialtyId: String, requestedDegree: String): String {
        val supported = AcademicDegree.degreesForSpecialty(specialtyId)
        return if (requestedDegree in supported) requestedDegree else AcademicDegree.BACHELOR
    }

    fun openFlashcards(
        specialtyId: String = _flashcardState.value.specialtyId,
        academicDegree: String = _selectedDegree.value
    ) {
        val effectiveDegree = resolveSupportedDegree(specialtyId, academicDegree)
        _flashcardState.update {
            it.copy(
                specialtyId = specialtyId,
                academicDegree = effectiveDegree,
                selectedTopic = null,
                isFlipped = false,
                isLoading = true
            )
        }
        navigateTo(ScreenDestination.Flashcards)
        loadFlashcardDeckInternal(specialtyId, effectiveDegree, null)
    }

    fun updateFlashcardFilter(
        specialtyId: String = _flashcardState.value.specialtyId,
        academicDegree: String = _flashcardState.value.academicDegree,
        topic: String? = _flashcardState.value.selectedTopic,
        resetTopicIfTrackChanged: Boolean = false
    ) {
        val resolvedTopic = if (resetTopicIfTrackChanged) null else topic
        val effectiveDegree = resolveSupportedDegree(specialtyId, academicDegree)
        _flashcardState.update {
            it.copy(
                specialtyId = specialtyId,
                academicDegree = effectiveDegree,
                selectedTopic = resolvedTopic,
                isFlipped = false,
                isLoading = true
            )
        }
        loadFlashcardDeckInternal(specialtyId, effectiveDegree, resolvedTopic)
    }

    private fun loadFlashcardDeckInternal(
        specialtyId: String,
        academicDegree: String,
        topic: String?
    ) {
        viewModelScope.launch {
            val topics = repository.getFlashcardTopics(specialtyId, academicDegree)
            val safeTopic = if (topic != null && topic in topics) topic else null
            val deck = repository.loadFlashcardDeck(specialtyId, academicDegree, safeTopic)
            val (shuffledCards, permutations) = QuestionShuffleUtil.shuffleQuestionList(deck.cards)
            _flashcardState.update {
                it.copy(
                    availableTopics = topics,
                    selectedTopic = safeTopic,
                    deckCards = shuffledCards,
                    currentIndex = 0,
                    isFlipped = false,
                    totalInScope = deck.totalInScope,
                    unseenRemaining = deck.unseenCount,
                    isLoading = false
                )
            }
            preloadOfflineTranslationsForQuestions(shuffledCards, permutations)
        }
    }

    fun flipFlashcard() {
        _flashcardState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun markFlashcardMasteredAndNext() {
        val state = _flashcardState.value
        val card = state.currentCard ?: return
        audioEngine.playCorrect()

        viewModelScope.launch {
            repository.markFlashcardReviewed(card)
            val updatedCards = state.deckCards.filterNot { it.id == card.id }
            val nextIdx = if (updatedCards.isEmpty()) 0 else state.currentIndex.coerceAtMost(updatedCards.lastIndex)
            _flashcardState.update {
                it.copy(
                    deckCards = updatedCards,
                    currentIndex = nextIdx,
                    isFlipped = false,
                    unseenRemaining = (it.unseenRemaining - 1).coerceAtLeast(0)
                )
            }
        }
    }

    fun skipFlashcardToLater() {
        val state = _flashcardState.value
        if (state.deckCards.size <= 1) {
            _flashcardState.update { it.copy(isFlipped = false) }
            return
        }
        val current = state.currentCard ?: return
        val rotated = state.deckCards.toMutableList().apply {
            removeAt(state.currentIndex)
            add(current)
        }
        val safeIdx = state.currentIndex.coerceAtMost(rotated.lastIndex)
        _flashcardState.update {
            it.copy(
                deckCards = rotated,
                currentIndex = safeIdx,
                isFlipped = false
            )
        }
    }

    fun resetFlashcardDeck() {
        val state = _flashcardState.value
        _flashcardState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repository.resetFlashcardDeck(state.specialtyId, state.academicDegree, state.selectedTopic)
            loadFlashcardDeckInternal(state.specialtyId, state.academicDegree, state.selectedTopic)
        }
    }

    // ==========================================
    // Prometric Timed Quiz Mode (30 / 60 / 120 min)
    // ==========================================
    fun openPrometricSetup() {
        val degree = _selectedDegree.value
        val currentSpec = _prometricState.value.specialtyId
        val effectiveDegree = resolveSupportedDegree(currentSpec, degree)
        _prometricState.update { it.copy(academicDegree = effectiveDegree) }
        navigateTo(ScreenDestination.PrometricSetup)
        refreshPrometricPoolCount()
    }

    fun updatePrometricSetupConfig(
        specialtyId: String = _prometricState.value.specialtyId,
        academicDegree: String = _prometricState.value.academicDegree,
        duration: PrometricDurationOption = _prometricState.value.selectedDuration
    ) {
        val effectiveDegree = resolveSupportedDegree(specialtyId, academicDegree)
        _prometricState.update {
            it.copy(
                specialtyId = specialtyId,
                academicDegree = effectiveDegree,
                selectedDuration = duration
            )
        }
        refreshPrometricPoolCount()
    }

    private fun refreshPrometricPoolCount() {
        val state = _prometricState.value
        viewModelScope.launch {
            val count = repository.getUnansweredCountForTrack(state.specialtyId, state.academicDegree)
            _prometricState.update { it.copy(unseenAvailableCount = count) }
        }
    }

    fun startPrometricExam() {
        val setup = _prometricState.value
        _prometricState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val pool = repository.getQuestionsForPrometricExam(
                specialtyId = setup.specialtyId,
                academicDegree = setup.academicDegree,
                targetCount = setup.selectedDuration.questionCount
            )
            if (pool.questions.isEmpty()) {
                _prometricState.update { it.copy(isLoading = false) }
                return@launch
            }

            val (shuffledQuestions, permutations) = QuestionShuffleUtil.shuffleQuestionList(pool.questions)
            _prometricState.update {
                it.copy(
                    isLoading = false,
                    questions = shuffledQuestions,
                    currentIndex = 0,
                    selectedAnswers = emptyMap(),
                    flaggedIndices = emptySet(),
                    remainingSeconds = setup.selectedDuration.totalSeconds,
                    totalDurationSeconds = setup.selectedDuration.totalSeconds,
                    isRunning = true,
                    isSubmitted = false,
                    unseenUsedCount = pool.unseenUsedCount,
                    optionPermutations = permutations
                )
            }
            preloadOfflineTranslationsForQuestions(shuffledQuestions, permutations)
            navigateTo(ScreenDestination.PrometricExam)
            audioEngine.startFocusAmbience()
            startPrometricTimer()
        }
    }

    private fun startPrometricTimer() {
        prometricTimerJob?.cancel()
        prometricTimerJob = viewModelScope.launch {
            while (_prometricState.value.isRunning && _prometricState.value.remainingSeconds > 0) {
                delay(1000L)
                val next = _prometricState.value.remainingSeconds - 1
                _prometricState.update { it.copy(remainingSeconds = next) }
                if (next in 1..10) {
                    audioEngine.playTick(isUrgent = next <= 5)
                }
                if (next <= 0) {
                    submitPrometricExam()
                    break
                }
            }
        }
    }

    private fun stopPrometricTimer() {
        prometricTimerJob?.cancel()
        prometricTimerJob = null
        _prometricState.update { it.copy(isRunning = false) }
    }

    fun selectPrometricOption(optionIndex: Int) {
        val state = _prometricState.value
        if (state.isSubmitted || state.currentQuestion == null) return
        _prometricState.update {
            it.copy(selectedAnswers = it.selectedAnswers + (it.currentIndex to optionIndex))
        }
    }

    fun togglePrometricFlag(index: Int = _prometricState.value.currentIndex) {
        _prometricState.update { state ->
            val updated = if (index in state.flaggedIndices) {
                state.flaggedIndices - index
            } else {
                state.flaggedIndices + index
            }
            state.copy(flaggedIndices = updated)
        }
    }

    fun jumpToPrometricQuestion(index: Int) {
        val state = _prometricState.value
        if (index in state.questions.indices && index != state.currentIndex) {
            val dir = if (index > state.currentIndex) 1 else -1
            _prometricState.update { it.copy(currentIndex = index, navigationDirection = dir) }
        }
    }

    fun nextPrometricQuestion() {
        val state = _prometricState.value
        if (state.currentIndex < state.questions.lastIndex) {
            _prometricState.update {
                it.copy(currentIndex = it.currentIndex + 1, navigationDirection = 1)
            }
        }
    }

    fun prevPrometricQuestion() {
        val state = _prometricState.value
        if (state.currentIndex > 0) {
            _prometricState.update {
                it.copy(currentIndex = it.currentIndex - 1, navigationDirection = -1)
            }
        }
    }

    fun submitPrometricExam() {
        val state = _prometricState.value
        if (state.isSubmitted || state.questions.isEmpty()) return

        stopPrometricTimer()
        audioEngine.stopFocusAmbience()

        val timeSpent = (state.totalDurationSeconds - state.remainingSeconds).coerceAtLeast(0)
        val records = state.questions.mapIndexed { idx, q ->
            val chosen = state.selectedAnswers[idx] ?: -1
            val correct = chosen == q.correctOptionIndex
            RoundAnswerRecord(
                question = q,
                selectedOptionIndex = chosen,
                isCorrect = correct,
                explanation = q.clinicalExplanationEn
            )
        }
        val correctCount = records.count { it.isCorrect }
        val passed = StageGradingPolicy.isStagePassed(correctCount, records.size)
        val scoreEarned = correctCount * 120

        if (passed) {
            audioEngine.playLevelComplete()
        } else {
            audioEngine.playIncorrect()
        }

        _prometricState.update { it.copy(isSubmitted = true, isRunning = false) }
        _sessionState.value = AssessmentSessionState(
            specialtyId = state.specialtyId,
            academicDegree = state.academicDegree,
            stageLevel = 1,
            isDailyChallenge = false,
            isTopicPractice = false,
            isPrometricTimed = true,
            questionQueue = state.questions,
            answersHistory = records,
            scoreEarned = scoreEarned,
            isRoundComplete = true,
            isPassed = passed,
            optionPermutations = state.optionPermutations
        )

        viewModelScope.launch {
            val answeredEntities = state.questions.mapIndexedNotNull { idx, q ->
                val chosen = state.selectedAnswers[idx] ?: return@mapIndexedNotNull null
                val canonicalSelected = state.optionPermutations[q.id]?.getOrNull(chosen) ?: chosen
                AnsweredQuestionEntity(
                    questionId = q.id,
                    specialtyId = q.specialtyId,
                    stageLevel = q.stageLevel,
                    isCorrect = chosen == q.correctOptionIndex,
                    selectedOptionIndex = canonicalSelected,
                    academicDegree = q.academicDegree
                )
            }
            repository.recordQuestionAnswers(answeredEntities)

            val savedId = repository.saveCompletedQuizSession(
                session = QuizSessionEntity(
                    mode = QuizMode.PROMETRIC_TIMED,
                    specialtyId = state.specialtyId,
                    academicDegree = state.academicDegree,
                    stageLevel = 1,
                    topicTitle = "${state.selectedDuration.minutes} min Prometric",
                    durationLimitSeconds = state.totalDurationSeconds,
                    timeSpentSeconds = timeSpent,
                    totalQuestions = records.size,
                    correctCount = correctCount,
                    scoreEarned = scoreEarned,
                    isPassed = passed
                ),
                answers = state.questions.mapIndexed { idx, q ->
                    val chosen = state.selectedAnswers[idx] ?: -1
                    val canonicalSelected = if (chosen in 0..3) {
                        state.optionPermutations[q.id]?.getOrNull(chosen) ?: chosen
                    } else chosen
                    QuizSessionAnswerEntity(
                        sessionId = 0L,
                        questionId = q.id,
                        orderIndex = idx,
                        selectedOptionIndex = canonicalSelected,
                        isCorrect = chosen == q.correctOptionIndex,
                        wasFlagged = idx in state.flaggedIndices
                    )
                }
            )
            _sessionState.update { it.copy(savedSessionId = savedId) }
            refreshPrometricPoolCount()
            markLocalChangeAndPushIfPossible(pushImmediatelyWhenOnline = true)
        }

        _currentScreen.value = ScreenDestination.Result(
            specialtyId = state.specialtyId,
            stageLevel = 1,
            isPassed = passed,
            academicDegree = state.academicDegree,
            isPrometricTimed = true
        )
    }

    fun cancelPrometricExamAndExit() {
        stopPrometricTimer()
        audioEngine.stopFocusAmbience()
        navigateTo(ScreenDestination.PrometricSetup)
    }

    // ==========================================
    // Permanent Exam History & Review Loader
    // ==========================================
    fun openExamHistory() {
        navigateTo(ScreenDestination.ExamHistory)
    }

    fun openSessionReview(sessionId: Long) {
        _loadedReviewState.value = LoadedReviewUiState(isLoading = true)
        navigateTo(
            ScreenDestination.Review(
                specialtyId = "",
                stageLevel = 1,
                sessionId = sessionId,
                fromHistory = true
            )
        )
        viewModelScope.launch {
            val loaded = repository.loadQuizSessionForReview(sessionId)
            _loadedReviewState.value = LoadedReviewUiState(
                session = loaded?.session,
                items = loaded?.items ?: emptyList(),
                isLoading = false
            )
        }
    }

    fun openCurrentRoundReview(specialtyId: String, stageLevel: Int, academicDegree: String) {
        val currentSession = _sessionState.value
        val items = currentSession.answersHistory.map { rec ->
            ReviewQuestionEntry(
                question = rec.question,
                selectedOptionIndex = rec.selectedOptionIndex,
                isCorrect = rec.isCorrect,
                wasFlagged = false
            )
        }
        _loadedReviewState.value = LoadedReviewUiState(
            session = null,
            items = items,
            isLoading = false
        )
        navigateTo(
            ScreenDestination.Review(
                specialtyId = specialtyId,
                stageLevel = stageLevel,
                academicDegree = academicDegree,
                sessionId = currentSession.savedSessionId,
                fromHistory = false,
                isPrometricTimed = currentSession.isPrometricTimed
            )
        )
    }

    fun deleteQuizSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteQuizSession(sessionId)
        }
    }

    fun setSelectedDegree(degree: String) {
        _selectedDegree.value = degree
    }

    fun toggleOrTranslateQuestion(question: QuestionEntity) {
        val existing = _translationsState.value[question.id]
        if (existing?.isLoading == true) return

        val screen = _currentScreen.value
        val isFromHistoryReview = (screen as? ScreenDestination.Review)?.fromHistory == true
        val useSessionPerm = !isFromHistoryReview && screen !is ScreenDestination.Search &&
            screen !is ScreenDestination.Favorites && screen !is ScreenDestination.Mistakes
        val permutation = if (useSessionPerm) {
            _sessionState.value.optionPermutations[question.id]
                ?: _prometricState.value.optionPermutations[question.id]
        } else {
            null
        }

        if (existing?.translation != null) {
            _translationsState.update { currentMap ->
                currentMap + (question.id to existing.copy(
                    isExpanded = !existing.isExpanded,
                    errorMessage = null
                ))
            }
            return
        }

        _translationsState.update { currentMap ->
            currentMap + (question.id to QuestionTranslationUiState(
                isLoading = true,
                isExpanded = false,
                translation = null,
                errorMessage = null
            ))
        }

        viewModelScope.launch {
            try {
                val translated = repository.translateAndCacheQuestion(question, permutation)
                _translationsState.update { currentMap ->
                    currentMap + (question.id to QuestionTranslationUiState(
                        isLoading = false,
                        isExpanded = true,
                        translation = translated,
                        errorMessage = null
                    ))
                }
            } catch (e: TimeoutCancellationException) {
                Log.e("AssessmentViewModel", "Translation timeout for question ${question.id}", e)
                _translationsState.update { currentMap ->
                    currentMap + (question.id to QuestionTranslationUiState(
                        isLoading = false,
                        isExpanded = false,
                        translation = null,
                        errorMessage = "انتهت مهلة الترجمة (60 ثانية). تحقق من الاتصال وأعد المحاولة."
                    ))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("AssessmentViewModel", "Translation failed for question ${question.id}", e)
                _translationsState.update { currentMap ->
                    currentMap + (question.id to QuestionTranslationUiState(
                        isLoading = false,
                        isExpanded = false,
                        translation = null,
                        errorMessage = e.localizedMessage?.takeIf { it.isNotBlank() }
                            ?: "تعذر ترجمة السؤال. تحقق من الاتصال بالإنترنت."
                    ))
                }
            }
        }
    }

    fun toggleDashboardLanguage() {
        _dashboardLanguage.update { current ->
            if (current == DashboardLanguage.EN) DashboardLanguage.AR else DashboardLanguage.EN
        }
    }

    fun toggleAudio() {
        audioEngine.toggleMute()
    }

    fun openSupervisorProfile(show: Boolean) {
        _showSupervisorProfile.value = show
    }

    fun toggleSupervisorProfile(show: Boolean) {
        _showSupervisorProfile.value = show
    }

    fun openContactDialog(show: Boolean) {
        _showContactDialog.value = show
    }

    fun toggleContactDialog(show: Boolean) {
        _showContactDialog.value = show
    }

    fun toggleBookmark(questionId: Long) {
        viewModelScope.launch {
            repository.toggleBookmark(questionId)
        }
    }

    fun saveApplicantProfile(profile: ApplicantProfileEntity) {
        viewModelScope.launch {
            repository.saveApplicantProfile(profile)
            markLocalChangeAndPushIfPossible(pushImmediatelyWhenOnline = true)
        }
    }

    fun saveApplicantProfile(
        fullName: String,
        degreeType: String,
        specialty: String,
        age: Int,
        gender: String,
        phoneNumber: String
    ) {
        viewModelScope.launch {
            repository.saveApplicantProfile(
                ApplicantProfileEntity(
                    id = 1,
                    fullName = fullName,
                    degreeType = degreeType,
                    specialty = specialty,
                    age = age,
                    gender = gender,
                    phoneNumber = phoneNumber
                )
            )
            markLocalChangeAndPushIfPossible(pushImmediatelyWhenOnline = true)
        }
    }

    // ==========================================
    // Offline Student Notes (Question-Linked + Standalone)
    // ==========================================
    fun saveNote(
        id: Long = 0L,
        questionId: Long? = null,
        specialtyId: String = "GENERAL",
        topic: String,
        title: String,
        content: String
    ) {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch {
            repository.saveNote(
                id = id,
                questionId = questionId,
                specialtyId = specialtyId,
                topic = topic.ifBlank { "General" },
                title = title.ifBlank { topic.ifBlank { "Clinical Note" } },
                content = content.ifBlank { title }
            )
            markLocalChangeAndPushIfPossible(pushImmediatelyWhenOnline = true)
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNoteById(noteId)
            val user = Firebase.auth.currentUser
            if (user != null) {
                if (isOnline.value) {
                    val result = cloudSyncRepository.deleteCloudNote(noteId)
                    if (result.isFailure) {
                        recordPendingLocalChange()
                        _syncUiState.value = SyncUiState.Error(result.exceptionOrNull()?.localizedMessage ?: "Delete sync failed")
                        _isSyncBannerVisible.value = true
                    }
                } else {
                    recordPendingLocalChange()
                    _syncUiState.value = SyncUiState.OfflinePending(getPendingChangesCount())
                    _isSyncBannerVisible.value = true
                }
            }
        }
    }

    // ==========================================
    // Optional Google Sign-In & Firestore Sync
    // ==========================================
    fun attemptSilentAutoSignIn(context: Context) {
        if (autoSignInAttempted || Firebase.auth.currentUser != null) return
        autoSignInAttempted = true
        AuthManager.attemptAutoSignIn(
            context = context,
            scope = viewModelScope,
            onAuthSuccess = { user ->
                _authUiState.value = user.toSignedInUiState()
            },
            onUnauthenticated = {
                _authUiState.value = CloudAuthUiState.SignedOut
            }
        )
    }

    fun signInWithGoogle(context: Context) {
        _authUiState.value = CloudAuthUiState.SigningIn
        AuthManager.signInWithGoogleInteractive(
            context = context,
            scope = viewModelScope,
            onAuthSuccess = { user ->
                _authUiState.value = user.toSignedInUiState()
                viewModelScope.launch {
                    syncBidirectional(user)
                }
            },
            onAuthCancelled = {
                _authUiState.value = Firebase.auth.currentUser?.toSignedInUiState() ?: CloudAuthUiState.SignedOut
            },
            onAuthError = { message ->
                _authUiState.value = CloudAuthUiState.Error(message)
            }
        )
    }

    fun syncNow() {
        if (!isAuthenticatedForSync()) return
        if (!isOnline.value) {
            recordPendingLocalChangeIfUnset()
            _syncUiState.value = SyncUiState.OfflinePending(getPendingChangesCount())
            _isSyncBannerVisible.value = true
            return
        }
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            flushPendingCloudSync(isReconnectAutoPush = false, showBannerOnSync = true)
        }
    }

    fun dismissSyncBanner() {
        bannerAutoDismissJob?.cancel()
        _isSyncBannerVisible.value = false
    }

    fun signOut(context: Context) {
        AuthManager.signOut(
            context = context,
            scope = viewModelScope,
            onBeforeSignOut = {
                syncJob?.cancelAndJoin()
                syncJob = null
                bannerAutoDismissJob?.cancel()
                clearPendingLocalChanges()
                _syncUiState.value = SyncUiState.Idle
                _isSyncBannerVisible.value = false
            },
            onSignOutComplete = {
                syncedUidThisSession = null
                _authUiState.value = CloudAuthUiState.SignedOut
                _syncUiState.value = SyncUiState.Idle
                _isSyncBannerVisible.value = false
            }
        )
    }

    suspend fun getFirstNoteForQuestion(questionId: Long): UserNoteEntity? {
        return repository.getFirstNoteForQuestion(questionId)
    }

    private fun isAuthenticatedForSync(): Boolean =
        testSignedInOverride || Firebase.auth.currentUser != null

    private fun hasPendingCloudSync(): Boolean =
        syncPrefs.getBoolean(KEY_HAS_PENDING_SYNC, false)

    private fun getPendingChangesCount(): Int =
        syncPrefs.getInt(KEY_PENDING_CHANGES_COUNT, 0).coerceAtLeast(1)

    private fun recordPendingLocalChange() {
        val nextCount = syncPrefs.getInt(KEY_PENDING_CHANGES_COUNT, 0) + 1
        syncPrefs.edit()
            .putBoolean(KEY_HAS_PENDING_SYNC, true)
            .putInt(KEY_PENDING_CHANGES_COUNT, nextCount)
            .apply()
    }

    private fun recordPendingLocalChangeIfUnset() {
        if (!hasPendingCloudSync()) {
            syncPrefs.edit()
                .putBoolean(KEY_HAS_PENDING_SYNC, true)
                .putInt(KEY_PENDING_CHANGES_COUNT, 1)
                .apply()
        }
    }

    private fun clearPendingLocalChanges() {
        syncPrefs.edit()
            .putBoolean(KEY_HAS_PENDING_SYNC, false)
            .putInt(KEY_PENDING_CHANGES_COUNT, 0)
            .apply()
    }

    private suspend fun handleConnectivityStateChange(online: Boolean) {
        _isOnline.value = online
        if (!online) {
            wasOffline = true
            if (isAuthenticatedForSync() && hasPendingCloudSync()) {
                _syncUiState.value = SyncUiState.OfflinePending(getPendingChangesCount())
                _isSyncBannerVisible.value = true
            }
            return
        }

        val reconnected = wasOffline
        wasOffline = false
        if (isAuthenticatedForSync() && (hasPendingCloudSync() || (reconnected && (_syncUiState.value is SyncUiState.OfflinePending || _syncUiState.value is SyncUiState.Error)))) {
            _isSyncBannerVisible.value = true
            syncJob?.cancel()
            syncJob = viewModelScope.launch {
                flushPendingCloudSync(
                    isReconnectAutoPush = true,
                    showBannerOnSync = true
                )
            }
        }
    }

    private suspend fun markLocalChangeAndPushIfPossible(pushImmediatelyWhenOnline: Boolean) {
        recordPendingLocalChange()
        if (!isAuthenticatedForSync()) return

        if (!isOnline.value) {
            _syncUiState.value = SyncUiState.OfflinePending(getPendingChangesCount())
            _isSyncBannerVisible.value = true
            return
        }

        if (pushImmediatelyWhenOnline) {
            val result = executePushOrOverride()
            if (result.isSuccess) {
                clearPendingLocalChanges()
                _syncUiState.value = SyncUiState.Synced(
                    timestampMs = System.currentTimeMillis(),
                    isReconnectAutoPush = false
                )
            } else if (!isOnline.value) {
                _syncUiState.value = SyncUiState.OfflinePending(getPendingChangesCount())
                _isSyncBannerVisible.value = true
            }
        }
    }

    private suspend fun flushPendingCloudSync(
        isReconnectAutoPush: Boolean,
        showBannerOnSync: Boolean
    ) {
        val override = syncExecutorOverrideForTest
        if (override != null) {
            bannerAutoDismissJob?.cancel()
            _syncUiState.value = SyncUiState.Syncing(isReconnectAutoPush = isReconnectAutoPush)
            if (showBannerOnSync) _isSyncBannerVisible.value = true
            val result = override()
            if (result.isSuccess) {
                clearPendingLocalChanges()
                _syncUiState.value = SyncUiState.Synced(
                    timestampMs = System.currentTimeMillis(),
                    isReconnectAutoPush = isReconnectAutoPush
                )
                if (showBannerOnSync) scheduleBannerAutoDismiss()
            } else {
                _syncUiState.value = SyncUiState.Error(
                    result.exceptionOrNull()?.localizedMessage ?: "Cloud sync unavailable offline"
                )
                if (showBannerOnSync) _isSyncBannerVisible.value = true
            }
            return
        }

        val user = Firebase.auth.currentUser ?: return
        syncBidirectional(
            user = user,
            isReconnectAutoPush = isReconnectAutoPush,
            showBannerOnSync = showBannerOnSync
        )
    }

    private suspend fun executePushOrOverride(): Result<Unit> {
        val override = syncExecutorOverrideForTest
        if (override != null) return override()
        val user = Firebase.auth.currentUser ?: return Result.failure(IllegalStateException("Not signed in"))
        return pushSnapshotToCloud(user)
    }

    private suspend fun syncBidirectional(
        user: FirebaseUser,
        isReconnectAutoPush: Boolean = false,
        showBannerOnSync: Boolean = isReconnectAutoPush
    ) {
        bannerAutoDismissJob?.cancel()
        _syncUiState.value = SyncUiState.Syncing(isReconnectAutoPush = isReconnectAutoPush)
        if (showBannerOnSync) {
            _isSyncBannerVisible.value = true
        }
        val pullResult = cloudSyncRepository.pullUserCloudData()
        if (pullResult.isSuccess) {
            val payload = pullResult.getOrThrow()
            repository.mergeCloudProgress(payload.progressList)
            repository.mergeCloudNotes(payload.notesList)
        }
        val pushResult = pushSnapshotToCloud(user)
        if (pushResult.isSuccess) {
            clearPendingLocalChanges()
            _syncUiState.value = SyncUiState.Synced(
                timestampMs = System.currentTimeMillis(),
                isReconnectAutoPush = isReconnectAutoPush
            )
            if (showBannerOnSync) {
                scheduleBannerAutoDismiss()
            }
        } else {
            val err = pullResult.exceptionOrNull() ?: pushResult.exceptionOrNull()
            _syncUiState.value = SyncUiState.Error(err?.localizedMessage ?: "Cloud sync unavailable offline")
            if (showBannerOnSync) {
                _isSyncBannerVisible.value = true
            }
        }
    }

    private fun scheduleBannerAutoDismiss() {
        bannerAutoDismissJob?.cancel()
        bannerAutoDismissJob = viewModelScope.launch {
            delay(3200L)
            if (_syncUiState.value is SyncUiState.Synced) {
                _isSyncBannerVisible.value = false
            }
        }
    }

    private suspend fun pushSnapshotToCloud(user: FirebaseUser): Result<Unit> {
        val progressList = repository.getAllProgressDirect()
        val notesList = repository.getAllNotesDirect()
        val masteredCount = topicMasteryStats.value.count { it.masteryStatus == TopicMasteryStatus.MASTERED }.toLong()
        val totalScore = progressList.sumOf { it.totalScore }.toLong()
        val totalCorrect = totalCorrectCount.value.toLong()
        val totalAnswered = totalAnsweredCount.value.toLong()
        return cloudSyncRepository.pushFullSnapshot(
            user = user,
            applicantProfile = applicantProfile.value,
            progressList = progressList,
            notesList = notesList,
            totalScore = totalScore,
            totalCorrect = totalCorrect,
            totalAnswered = totalAnswered,
            masteredTopicsCount = masteredCount
        )
    }

    internal fun installConnectivityObserverForTest(observer: ConnectivityObserver) {
        connectivityCollectorJob?.cancel()
        connectivityObserver.unregister()
        connectivityObserver = observer
        _isOnline.value = observer.isOnline.value
        wasOffline = !observer.isOnline.value
        connectivityCollectorJob = viewModelScope.launch {
            observer.isOnline.collectLatest { online ->
                handleConnectivityStateChange(online)
            }
        }
    }

    internal suspend fun recordOfflineProgressMutationForTest() {
        markLocalChangeAndPushIfPossible(pushImmediatelyWhenOnline = false)
    }

    private companion object {
        const val KEY_HAS_PENDING_SYNC = "has_pending_cloud_sync"
        const val KEY_PENDING_CHANGES_COUNT = "pending_cloud_changes_count"
    }

    private fun preloadOfflineTranslationsForQuestions(
        questions: List<QuestionEntity>,
        permutations: Map<Long, List<Int>>
    ) {
        if (questions.isEmpty()) return
        viewModelScope.launch {
            val cachedMap = repository.getCachedTranslationsForIds(questions.map { it.id })
            if (cachedMap.isEmpty()) return@launch
            _translationsState.update { currentMap ->
                val updated = currentMap.toMutableMap()
                for (q in questions) {
                    val cached = cachedMap[q.id] ?: continue
                    val aligned = QuestionShuffleUtil.alignTranslationOptions(cached, permutations[q.id])
                    val prev = updated[q.id]
                    updated[q.id] = QuestionTranslationUiState(
                        isLoading = false,
                        isExpanded = prev?.isExpanded ?: false,
                        translation = aligned,
                        errorMessage = null
                    )
                }
                updated
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        setSearchQuery(query)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        performSearch(query)
    }

    fun setSearchFilters(specialtyId: String?, academicDegree: String? = _searchDegreeFilter.value) {
        _searchSpecialtyFilter.value = specialtyId
        _searchDegreeFilter.value = academicDegree
        performSearch(_searchQuery.value)
    }

    fun performSearch(query: String) {
        val spec = _searchSpecialtyFilter.value
        val deg = _searchDegreeFilter.value
        viewModelScope.launch {
            val results = repository.searchQuestions(query, spec, deg)
            _searchResults.value = results
            if (results.isNotEmpty()) {
                _searchTranslations.value = repository.getCachedTranslationsForIds(results.map { it.id })
            } else {
                _searchTranslations.value = emptyMap()
            }
        }
    }

    // ==========================================
    // Daily Challenge & Topic Practice
    // ==========================================
    fun startDailyChallenge() {
        viewModelScope.launch {
            val rawQuestions = repository.getDailyChallengeQuestions()
            val (questions, permutations) = QuestionShuffleUtil.shuffleQuestionList(rawQuestions)
            val initialQuestion = questions.firstOrNull()

            _sessionState.value = AssessmentSessionState(
                specialtyId = "MED_LAB",
                academicDegree = AcademicDegree.BACHELOR,
                stageLevel = 1,
                isDailyChallenge = true,
                isTopicPractice = false,
                activeTopicTitle = null,
                questionQueue = questions,
                currentIndex = 0,
                currentQuestion = initialQuestion,
                remainingSeconds = 60,
                isTimerActive = true,
                heartsRemaining = 5,
                scoreEarned = 0,
                answersHistory = emptyList(),
                isRoundComplete = false,
                isPassed = false,
                optionPermutations = permutations
            )
            preloadOfflineTranslationsForQuestions(questions, permutations)

            _currentScreen.value = ScreenDestination.Assessment("MED_LAB", 1, AcademicDegree.BACHELOR)
            audioEngine.startFocusAmbience()
            startTimer()
        }
    }

    fun startTopicPractice(
        specialtyId: String,
        subCategory: String,
        academicDegree: String
    ) {
        viewModelScope.launch {
            val rawQuestions = repository.getQuestionsForTopicPractice(specialtyId, subCategory, academicDegree)
            if (rawQuestions.isEmpty()) return@launch
            val (questions, permutations) = QuestionShuffleUtil.shuffleQuestionList(rawQuestions)
            val initialQuestion = questions.firstOrNull()
            _selectedDegree.value = academicDegree

            _sessionState.value = AssessmentSessionState(
                specialtyId = specialtyId,
                academicDegree = academicDegree,
                stageLevel = initialQuestion?.stageLevel ?: 1,
                isDailyChallenge = false,
                isTopicPractice = true,
                activeTopicTitle = subCategory,
                questionQueue = questions,
                currentIndex = 0,
                currentQuestion = initialQuestion,
                remainingSeconds = 60,
                isTimerActive = true,
                heartsRemaining = 5,
                scoreEarned = 0,
                answersHistory = emptyList(),
                isRoundComplete = false,
                isPassed = false,
                optionPermutations = permutations
            )
            preloadOfflineTranslationsForQuestions(questions, permutations)

            _currentScreen.value = ScreenDestination.Assessment(
                specialtyId = specialtyId,
                stageLevel = initialQuestion?.stageLevel ?: 1,
                academicDegree = academicDegree
            )
            audioEngine.startFocusAmbience()
            startTimer()
        }
    }

    /**
     * Absolute Zero-Duplication Engine initialization:
     * 1. Query unanswered questions for this stage from Room.
     * 2. Shuffle questions & independently shuffle options A-D per question & session.
     * 3. Navigate sequentially by currentIndex++.
     */
    fun startAssessment(
        specialtyId: String,
        academicDegree: String = _selectedDegree.value,
        stageLevel: Int = 1
    ) {
        viewModelScope.launch {
            _selectedDegree.value = academicDegree
            val rawQuestions = repository.getQuestionsForStage(specialtyId, academicDegree, stageLevel)
            val (questions, permutations) = QuestionShuffleUtil.shuffleQuestionList(rawQuestions)
            val initialQuestion = questions.firstOrNull()

            _sessionState.value = AssessmentSessionState(
                specialtyId = specialtyId,
                academicDegree = academicDegree,
                stageLevel = stageLevel,
                isDailyChallenge = false,
                isTopicPractice = false,
                activeTopicTitle = null,
                questionQueue = questions,
                currentIndex = 0,
                currentQuestion = initialQuestion,
                remainingSeconds = 60,
                isTimerActive = true,
                heartsRemaining = 5,
                scoreEarned = 0,
                answersHistory = emptyList(),
                isRoundComplete = false,
                isPassed = false,
                optionPermutations = permutations
            )
            preloadOfflineTranslationsForQuestions(questions, permutations)

            _currentScreen.value = ScreenDestination.Assessment(specialtyId, stageLevel, academicDegree)
            audioEngine.startFocusAmbience()
            startTimer()
        }
    }

    fun startAssessment(specialtyId: String, stageLevel: Int) {
        startAssessment(specialtyId, _selectedDegree.value, stageLevel)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_sessionState.value.isTimerActive && _sessionState.value.remainingSeconds > 0) {
                delay(1000L)
                val newSeconds = _sessionState.value.remainingSeconds - 1
                _sessionState.update { it.copy(remainingSeconds = newSeconds) }

                if (newSeconds in 1..10) {
                    audioEngine.playTick(isUrgent = newSeconds <= 5)
                }

                if (newSeconds <= 0) {
                    handleTimeExpired()
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _sessionState.update { it.copy(isTimerActive = false) }
    }

    private fun handleTimeExpired() {
        stopTimer()
        audioEngine.playIncorrect()
        val state = _sessionState.value
        val currentQ = state.currentQuestion ?: return

        val newHearts = (state.heartsRemaining - 1).coerceAtLeast(0)
        val answerRecord = RoundAnswerRecord(
            question = currentQ,
            selectedOptionIndex = -1,
            isCorrect = false,
            explanation = currentQ.clinicalExplanationEn
        )

        viewModelScope.launch {
            repository.recordQuestionAnswer(
                questionId = currentQ.id,
                specialtyId = state.specialtyId,
                academicDegree = state.academicDegree,
                stageLevel = state.stageLevel,
                isCorrect = false,
                selectedOptionIndex = -1
            )
            markLocalChangeAndPushIfPossible(pushImmediatelyWhenOnline = false)
        }

        _sessionState.update {
            it.copy(
                isAnswerSubmitted = true,
                isAnswerCorrect = false,
                selectedOptionIndex = -1,
                heartsRemaining = newHearts,
                answersHistory = it.answersHistory + answerRecord
            )
        }
    }

    fun submitAnswer(optionIndex: Int) {
        val state = _sessionState.value
        if (state.isAnswerSubmitted || state.currentQuestion == null) return

        stopTimer()
        val currentQ = state.currentQuestion
        val isCorrect = optionIndex == currentQ.correctOptionIndex

        if (isCorrect) {
            audioEngine.playCorrect()
        } else {
            audioEngine.playIncorrect()
        }

        val pointGain = if (isCorrect) (100 + state.remainingSeconds * 2) else 0
        val newHearts = if (isCorrect) state.heartsRemaining else (state.heartsRemaining - 1).coerceAtLeast(0)

        val record = RoundAnswerRecord(
            question = currentQ,
            selectedOptionIndex = optionIndex,
            isCorrect = isCorrect,
            explanation = currentQ.clinicalExplanationEn
        )

        val canonicalSelectedIndex = state.optionPermutations[currentQ.id]?.getOrNull(optionIndex) ?: optionIndex

        viewModelScope.launch {
            repository.recordQuestionAnswer(
                questionId = currentQ.id,
                specialtyId = state.specialtyId,
                academicDegree = state.academicDegree,
                stageLevel = state.stageLevel,
                isCorrect = isCorrect,
                selectedOptionIndex = canonicalSelectedIndex
            )
            markLocalChangeAndPushIfPossible(pushImmediatelyWhenOnline = false)
        }

        _sessionState.update {
            it.copy(
                selectedOptionIndex = optionIndex,
                isAnswerSubmitted = true,
                isAnswerCorrect = isCorrect,
                scoreEarned = it.scoreEarned + pointGain,
                heartsRemaining = newHearts,
                answersHistory = it.answersHistory + record
            )
        }
    }

    fun moveToNextQuestion() {
        val state = _sessionState.value
        val nextIndex = state.currentIndex + 1

        if (nextIndex >= state.questionQueue.size) {
            val correctCount = state.answersHistory.count { it.isCorrect }
            val totalCount = state.questionQueue.size
            val passed = StageGradingPolicy.isStagePassed(correctCount, totalCount)
            finishRound(passed = passed)
            return
        }

        val nextQ = state.questionQueue[nextIndex]
        _sessionState.update {
            it.copy(
                currentIndex = nextIndex,
                currentQuestion = nextQ,
                selectedOptionIndex = null,
                isAnswerSubmitted = false,
                isAnswerCorrect = false,
                remainingSeconds = 60,
                isTimerActive = true,
                eliminatedOptionIndices = emptySet(),
                isHintRevealed = false
            )
        }

        startTimer()
    }

    private fun finishRound(passed: Boolean) {
        stopTimer()
        audioEngine.stopFocusAmbience()

        if (passed) {
            audioEngine.playLevelComplete()
        }

        val state = _sessionState.value
        val correctCount = state.answersHistory.count { it.isCorrect }

        viewModelScope.launch {
            if (!state.isDailyChallenge && !state.isTopicPractice && !state.isPrometricTimed &&
                state.answersHistory.isNotEmpty()
            ) {
                repository.updateStageCompletion(
                    specialtyId = state.specialtyId,
                    academicDegree = state.academicDegree,
                    completedStage = state.stageLevel,
                    scoreEarned = state.scoreEarned,
                    correctCount = correctCount,
                    totalInRound = state.answersHistory.size
                )
            }
            if (state.answersHistory.isNotEmpty()) {
                val mode = when {
                    state.isDailyChallenge -> QuizMode.DAILY_CHALLENGE
                    state.isTopicPractice -> QuizMode.TOPIC_PRACTICE
                    else -> QuizMode.STAGE_ASSESSMENT
                }
                val savedId = repository.saveCompletedQuizSession(
                    session = QuizSessionEntity(
                        mode = mode,
                        specialtyId = state.specialtyId,
                        academicDegree = state.academicDegree,
                        stageLevel = state.stageLevel,
                        topicTitle = state.activeTopicTitle,
                        durationLimitSeconds = state.answersHistory.size * 60,
                        timeSpentSeconds = 0,
                        totalQuestions = state.answersHistory.size,
                        correctCount = correctCount,
                        scoreEarned = state.scoreEarned,
                        isPassed = passed
                    ),
                    answers = state.answersHistory.mapIndexed { idx, rec ->
                        val canonicalSelected = if (rec.selectedOptionIndex in 0..3) {
                            state.optionPermutations[rec.question.id]?.getOrNull(rec.selectedOptionIndex)
                                ?: rec.selectedOptionIndex
                        } else rec.selectedOptionIndex
                        QuizSessionAnswerEntity(
                            sessionId = 0L,
                            questionId = rec.question.id,
                            orderIndex = idx,
                            selectedOptionIndex = canonicalSelected,
                            isCorrect = rec.isCorrect,
                            wasFlagged = false
                        )
                    }
                )
                _sessionState.update { it.copy(savedSessionId = savedId) }
            }
            markLocalChangeAndPushIfPossible(pushImmediatelyWhenOnline = true)
        }

        _sessionState.update {
            it.copy(
                isRoundComplete = true,
                isPassed = passed
            )
        }

        _currentScreen.value = ScreenDestination.Result(
            specialtyId = state.specialtyId,
            stageLevel = state.stageLevel,
            isPassed = passed,
            academicDegree = state.academicDegree,
            isPrometricTimed = false
        )
    }

    // ==========================================
    // Clinical Lifelines & Assistance Tools
    // ==========================================

    fun useLifeline5050() {
        val state = _sessionState.value
        if (state.lifeline5050Used || state.isAnswerSubmitted || state.currentQuestion == null) return

        audioEngine.playLifeline()
        val correctIndex = state.currentQuestion.correctOptionIndex
        val wrongOptions = (0..3).filter { it != correctIndex }.shuffled().take(2).toSet()

        _sessionState.update {
            it.copy(
                lifeline5050Used = true,
                eliminatedOptionIndices = wrongOptions
            )
        }
    }

    fun useLifelineHint() {
        val state = _sessionState.value
        if (state.lifelineHintUsed || state.isAnswerSubmitted) return

        audioEngine.playLifeline()
        _sessionState.update {
            it.copy(
                lifelineHintUsed = true,
                isHintRevealed = true
            )
        }
    }

    fun useLifelineSwap() {
        val state = _sessionState.value
        if (state.lifelineSwapUsed || state.isAnswerSubmitted || state.currentQuestion == null) return

        audioEngine.playLifeline()
        viewModelScope.launch {
            val alt = repository.getAlternativeQuestion(
                specialtyId = state.specialtyId,
                academicDegree = state.academicDegree,
                stageLevel = state.stageLevel,
                currentQuestionId = state.currentQuestion.id
            )

            if (alt != null) {
                val shuffledRes = QuestionShuffleUtil.shuffleOptionsWithMap(alt)
                val altShuffled = shuffledRes.question
                val updatedQueue = state.questionQueue.toMutableList()
                if (state.currentIndex in updatedQueue.indices) {
                    updatedQueue[state.currentIndex] = altShuffled
                }
                val updatedPerms = state.optionPermutations + (altShuffled.id to shuffledRes.displayedToOriginalIndex)
                preloadOfflineTranslationsForQuestions(listOf(altShuffled), updatedPerms)

                _sessionState.update {
                    it.copy(
                        lifelineSwapUsed = true,
                        questionQueue = updatedQueue,
                        currentQuestion = altShuffled,
                        selectedOptionIndex = null,
                        isAnswerSubmitted = false,
                        eliminatedOptionIndices = emptySet(),
                        isHintRevealed = false,
                        remainingSeconds = 60,
                        optionPermutations = updatedPerms
                    )
                }
            } else {
                _sessionState.update {
                    it.copy(
                        lifelineSwapUsed = true,
                        remainingSeconds = 60
                    )
                }
            }
        }
    }

    fun useLifelineAddTime() {
        val state = _sessionState.value
        if (state.lifelineTimeUsed || state.isAnswerSubmitted) return

        audioEngine.playLifeline()
        _sessionState.update {
            it.copy(
                lifelineTimeUsed = true,
                remainingSeconds = it.remainingSeconds + 30
            )
        }
    }

    fun setEnlargedImage(imageResName: String?) {
        _sessionState.update { it.copy(enlargedImageResName = imageResName) }
    }

    // Direct support intents
    fun launchPhoneCall(context: Context, phoneNumber: String = "+967773148358") {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun launchWhatsApp(context: Context, phoneNumber: String = "967773148358") {
        try {
            val url = "https://wa.me/$phoneNumber"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun launchFacebook(context: Context) {
        try {
            val url = "https://www.facebook.com/profile.php?id=100009173844663"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun launchEmail(context: Context, email: String = "abdallh773148358@gmail.com") {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, "استشارة وتقييم - برنامج المختبرات والعلوم الطبية")
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        stopPrometricTimer()
        connectivityCollectorJob?.cancel()
        bannerAutoDismissJob?.cancel()
        connectivityObserver.unregister()
        audioEngine.release()
    }

    private fun FirebaseUser.toSignedInUiState(): CloudAuthUiState.SignedIn = CloudAuthUiState.SignedIn(
        uid = uid,
        displayName = displayName?.takeIf { it.isNotBlank() } ?: (email ?: "Clinical Student"),
        email = email ?: "",
        photoUrl = photoUrl?.toString()
    )
}
