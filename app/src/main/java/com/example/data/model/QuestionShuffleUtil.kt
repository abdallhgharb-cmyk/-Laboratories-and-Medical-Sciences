package com.example.data.model

import kotlin.random.Random

/**
 * Holds a session-shuffled view of [QuestionEntity] alongside the index mapping
 * from displayed position (0..3) back to the original database option index (0..3).
 */
data class ShuffledQuestionResult(
    val question: QuestionEntity,
    val displayedToOriginalIndex: List<Int>
)

object QuestionShuffleUtil {

    private val IDENTITY_PERMUTATION = listOf(0, 1, 2, 3)

    /**
     * Produces an independently shuffled option order (A..D) for this question instance
     * while accurately updating [QuestionEntity.correctOptionIndex].
     * Never mutates persisted SQLite rows.
     */
    fun shuffleOptionsWithMap(
        question: QuestionEntity,
        random: Random = Random.Default
    ): ShuffledQuestionResult {
        val originalOptions = listOf(
            question.optionA,
            question.optionB,
            question.optionC,
            question.optionD
        )
        if (question.correctOptionIndex !in 0..3) {
            return ShuffledQuestionResult(question, IDENTITY_PERMUTATION)
        }

        val permutation = listOf(0, 1, 2, 3).shuffled(random)
        val newCorrectIndex = permutation.indexOf(question.correctOptionIndex)

        val shuffledEntity = question.copy(
            optionA = originalOptions[permutation[0]],
            optionB = originalOptions[permutation[1]],
            optionC = originalOptions[permutation[2]],
            optionD = originalOptions[permutation[3]],
            correctOptionIndex = newCorrectIndex
        )
        return ShuffledQuestionResult(
            question = shuffledEntity,
            displayedToOriginalIndex = permutation
        )
    }

    fun shuffleQuestionList(
        questions: List<QuestionEntity>,
        random: Random = Random.Default
    ): Pair<List<QuestionEntity>, Map<Long, List<Int>>> {
        val shuffledList = ArrayList<QuestionEntity>(questions.size)
        val map = HashMap<Long, List<Int>>(questions.size)
        for (q in questions) {
            val res = shuffleOptionsWithMap(q, random)
            shuffledList.add(res.question)
            map[q.id] = res.displayedToOriginalIndex
        }
        return shuffledList to map
    }

    /**
     * Reorders cached DB translation options (which correspond to DB optionA..optionD order)
     * so each displayed letter (A, B, C, D) matches its exact shuffled choice.
     */
    fun alignTranslationOptions(
        translation: QuestionTranslationEntity,
        displayedToOriginalIndex: List<Int>?
    ): QuestionTranslationEntity {
        if (displayedToOriginalIndex == null || displayedToOriginalIndex.size != 4) {
            return translation
        }
        val orig = listOf(
            translation.translatedOptionA,
            translation.translatedOptionB,
            translation.translatedOptionC,
            translation.translatedOptionD
        )
        return translation.copy(
            translatedOptionA = orig.getOrElse(displayedToOriginalIndex[0]) { translation.translatedOptionA },
            translatedOptionB = orig.getOrElse(displayedToOriginalIndex[1]) { translation.translatedOptionB },
            translatedOptionC = orig.getOrElse(displayedToOriginalIndex[2]) { translation.translatedOptionC },
            translatedOptionD = orig.getOrElse(displayedToOriginalIndex[3]) { translation.translatedOptionD }
        )
    }

    /**
     * Converts a newly fetched/live translation generated from a shuffled [QuestionEntity]
     * back into canonical DB option ordering before storing in `question_translations`.
     */
    fun canonicalizeTranslationOptions(
        translation: QuestionTranslationEntity,
        displayedToOriginalIndex: List<Int>?
    ): QuestionTranslationEntity {
        if (displayedToOriginalIndex == null || displayedToOriginalIndex.size != 4) {
            return translation
        }
        val displayed = listOf(
            translation.translatedOptionA,
            translation.translatedOptionB,
            translation.translatedOptionC,
            translation.translatedOptionD
        )
        val canonical = MutableList(4) { "" }
        displayedToOriginalIndex.forEachIndexed { displayedIdx, originalIdx ->
            if (originalIdx in 0..3) {
                canonical[originalIdx] = displayed[displayedIdx]
            }
        }
        return translation.copy(
            translatedOptionA = canonical[0],
            translatedOptionB = canonical[1],
            translatedOptionC = canonical[2],
            translatedOptionD = canonical[3]
        )
    }
}
