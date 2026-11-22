package com.example.data.seed

import android.content.Context
import com.example.data.model.AcademicDegree
import com.example.data.model.QuestionCategory
import com.example.data.model.QuestionDifficulty
import com.example.data.model.QuestionEntity
import com.example.data.model.QuestionTranslationEntity
import com.example.data.remote.MedicalTranslationService
import org.json.JSONObject

/**
 * Deterministic importer for the 500-question clinically reviewed Nursing bank
 * (nursing_questions_1_to_500_reviewed.json) with English primary text and cached Arabic translation.
 */
object NursingBankImporter {

    const val ASSET_FILE_NAME = "nursing_questions_1_to_500_reviewed.json"
    const val EXPECTED_TOTAL_NURSING_AFTER_MERGE = 3510

    fun loadFromAssets(
        context: Context,
        existingNormalizedStems: Set<String> = emptySet()
    ): List<QuestionEntity> {
        val rawJson = context.assets.open(ASSET_FILE_NAME).bufferedReader(Charsets.UTF_8).use { it.readText() }
        return parseJson(rawJson, existingNormalizedStems)
    }

    fun loadBilingualFromAssets(context: Context): List<BilingualQuestionPair> {
        val rawJson = context.assets.open(ASSET_FILE_NAME).bufferedReader(Charsets.UTF_8).use { it.readText() }
        return parseBilingualJson(rawJson)
    }

    fun parseJson(
        rawJson: String,
        existingNormalizedStems: Set<String> = emptySet()
    ): List<QuestionEntity> {
        return parseBilingualJson(rawJson, existingNormalizedStems).map { it.question }
    }

    fun parseBilingualJson(
        rawJson: String,
        existingNormalizedStems: Set<String> = emptySet()
    ): List<BilingualQuestionPair> {
        val root = JSONObject(rawJson)
        val arr = root.getJSONArray("questions")
        val seenStems = HashSet<String>(existingNormalizedStems)
        val result = ArrayList<BilingualQuestionPair>(arr.length())

        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val questionAr = obj.getString("question").trim()
            val questionEn = obj.optString("question_en", "").trim().ifBlank { questionAr }
            val normalizedEn = questionEn.lowercase()
            if (questionEn.isBlank() || !seenStems.add(normalizedEn)) {
                continue
            }

            val category = obj.optString("category", "Fundamentals of Nursing").trim()
            val academicDegree = obj.optString("academic_degree", AcademicDegree.BACHELOR).trim()
            val stageLevel = obj.optInt("stage_level", 1).coerceIn(1, 10)
            val difficultyLevel = obj.optString(
                "difficulty_level",
                QuestionDifficulty.fromStageLevel(stageLevel)
            ).trim()

            val optionsArJson = obj.getJSONArray("options")
            if (optionsArJson.length() != 4) continue
            val arA = optionsArJson.getString(0).trim()
            val arB = optionsArJson.getString(1).trim()
            val arC = optionsArJson.getString(2).trim()
            val arD = optionsArJson.getString(3).trim()

            val optionsEnJson = obj.optJSONArray("options_en")
            val optA: String
            val optB: String
            val optC: String
            val optD: String
            if (optionsEnJson != null && optionsEnJson.length() == 4) {
                optA = optionsEnJson.getString(0).trim()
                optB = optionsEnJson.getString(1).trim()
                optC = optionsEnJson.getString(2).trim()
                optD = optionsEnJson.getString(3).trim()
            } else {
                optA = arA; optB = arB; optC = arC; optD = arD
            }

            val correctAnswerAr = obj.getString("correct_answer").trim()
            val correctAnswerEn = obj.optString("correct_answer_en", "").trim()
            var correctIdx = if (correctAnswerEn.isNotBlank()) {
                listOf(optA, optB, optC, optD).indexOfFirst { it == correctAnswerEn }
            } else {
                -1
            }
            if (correctIdx !in 0..3) {
                correctIdx = listOf(arA, arB, arC, arD).indexOfFirst { it == correctAnswerAr }
            }
            if (correctIdx !in 0..3) continue

            val explanationAr = obj.optString("explanation", "").trim()
            val explanationEn = obj.optString("explanation_en", "").trim().ifBlank { explanationAr }
            val reference = obj.optString("reference", "").trim().ifBlank { null }
            val hintEn = buildString {
                append("Category: ")
                append(category)
                if (!reference.isNullOrBlank()) {
                    append(" • Ref: ")
                    append(reference)
                }
            }

            val entity = QuestionEntity(
                specialtyId = QuestionCategory.NURSING,
                academicDegree = academicDegree,
                stageLevel = stageLevel,
                difficultyLevel = difficultyLevel,
                subCategory = category,
                questionEn = questionEn,
                optionA = optA,
                optionB = optB,
                optionC = optC,
                optionD = optD,
                correctOptionIndex = correctIdx,
                clinicalExplanationEn = explanationEn,
                clinicalHintEn = hintEn,
                imageResName = null,
                diagnosticImageNotes = null,
                scientificReference = reference
            )

            val arTranslation = if (questionAr.isNotBlank() && questionAr != questionEn) {
                QuestionTranslationEntity(
                    questionId = 0L,
                    targetLanguage = MedicalTranslationService.TARGET_ARABIC,
                    translatedQuestion = questionAr,
                    translatedOptionA = arA,
                    translatedOptionB = arB,
                    translatedOptionC = arC,
                    translatedOptionD = arD,
                    translatedExplanation = explanationAr
                )
            } else {
                null
            }

            result.add(
                BilingualQuestionPair(
                    question = entity,
                    arabicStemNormalized = questionAr.lowercase(),
                    arabicTranslation = arTranslation
                )
            )
        }
        return result
    }
}
