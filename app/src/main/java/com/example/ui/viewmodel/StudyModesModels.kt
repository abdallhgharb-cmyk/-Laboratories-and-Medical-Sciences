package com.example.ui.viewmodel

import com.example.data.model.AcademicDegree
import com.example.data.model.QuestionCategory
import com.example.data.model.QuestionEntity
import com.example.data.model.QuizMode
import com.example.data.model.QuizSessionEntity
import com.example.data.repository.ReviewQuestionEntry

data class FlashcardUiState(
    val specialtyId: String = QuestionCategory.MEDICAL_LABS,
    val academicDegree: String = AcademicDegree.BACHELOR,
    val selectedTopic: String? = null,
    val availableTopics: List<String> = emptyList(),
    val deckCards: List<QuestionEntity> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val totalInScope: Int = 0,
    val unseenRemaining: Int = 0,
    val isLoading: Boolean = false
) {
    val currentCard: QuestionEntity?
        get() = deckCards.getOrNull(currentIndex)
}

enum class PrometricDurationOption(
    val minutes: Int,
    val questionCount: Int
) {
    MIN_30(minutes = 30, questionCount = 30),
    MIN_60(minutes = 60, questionCount = 60),
    MIN_120(minutes = 120, questionCount = 100);

    val totalSeconds: Int
        get() = minutes * 60
}

data class PrometricExamUiState(
    val specialtyId: String = QuestionCategory.MEDICAL_LABS,
    val academicDegree: String = AcademicDegree.BACHELOR,
    val selectedDuration: PrometricDurationOption = PrometricDurationOption.MIN_30,
    val unseenAvailableCount: Int = 0,
    val isLoading: Boolean = false,
    val questions: List<QuestionEntity> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswers: Map<Int, Int> = emptyMap(),
    val flaggedIndices: Set<Int> = emptySet(),
    val remainingSeconds: Int = 0,
    val totalDurationSeconds: Int = 0,
    val isRunning: Boolean = false,
    val isSubmitted: Boolean = false,
    val unseenUsedCount: Int = 0,
    val navigationDirection: Int = 1,
    val optionPermutations: Map<Long, List<Int>> = emptyMap()
) {
    val currentQuestion: QuestionEntity?
        get() = questions.getOrNull(currentIndex)

    val answeredCount: Int
        get() = selectedAnswers.size

    val flaggedCount: Int
        get() = flaggedIndices.size
}

data class LoadedReviewUiState(
    val session: QuizSessionEntity? = null,
    val items: List<ReviewQuestionEntry> = emptyList(),
    val isLoading: Boolean = false
)

object StudyModesStrings {
    fun formatCountdown(totalSeconds: Int): String {
        val safe = totalSeconds.coerceAtLeast(0)
        val hours = safe / 3600
        val minutes = (safe % 3600) / 60
        val seconds = safe % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun quizModeTitle(mode: String, lang: DashboardLanguage): String = when (mode) {
        QuizMode.PROMETRIC_TIMED ->
            if (lang == DashboardLanguage.EN) "Prometric Timed Exam" else "اختبار برومترك المؤقت"
        QuizMode.TOPIC_PRACTICE ->
            if (lang == DashboardLanguage.EN) "Topic Practice" else "تدريب تخصصي"
        QuizMode.DAILY_CHALLENGE ->
            if (lang == DashboardLanguage.EN) "Daily Challenge" else "التحدي اليومي"
        else ->
            if (lang == DashboardLanguage.EN) "Stage Assessment" else "تقييم المستوى"
    }
}
