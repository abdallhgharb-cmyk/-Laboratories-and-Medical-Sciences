package com.example.data.remote

import android.util.Log
import com.example.data.model.QuestionEntity
import com.example.data.model.QuestionTranslationEntity
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "MedicalTranslation"
private const val GEMINI_MODEL_NAME = "gemini-3.8-flash"

class MedicalTranslationService {

    private val translationSchema = Schema.obj(
        properties = mapOf(
            "translatedQuestion" to Schema.string(description = "Accurate medical translation of the question prompt"),
            "translatedOptionA" to Schema.string(description = "Accurate medical translation of option A"),
            "translatedOptionB" to Schema.string(description = "Accurate medical translation of option B"),
            "translatedOptionC" to Schema.string(description = "Accurate medical translation of option C"),
            "translatedOptionD" to Schema.string(description = "Accurate medical translation of option D"),
            "translatedExplanation" to Schema.string(description = "Accurate medical translation of the clinical explanation, or empty string if original explanation is blank")
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = GEMINI_MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = translationSchema
            }
        )
    }

    suspend fun translateQuestion(question: QuestionEntity): QuestionTranslationEntity {
        val targetLang = detectTargetLanguage(question)
        val prompt = buildTranslationPrompt(question, targetLang)

        try {
            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val rawJson = response.text?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Empty translation response from Gemini.")

            val json = JSONObject(rawJson)
            return QuestionTranslationEntity(
                questionId = question.id,
                targetLanguage = targetLang,
                translatedQuestion = json.optString("translatedQuestion", "").ifBlank { question.questionEn },
                translatedOptionA = json.optString("translatedOptionA", "").ifBlank { question.optionA },
                translatedOptionB = json.optString("translatedOptionB", "").ifBlank { question.optionB },
                translatedOptionC = json.optString("translatedOptionC", "").ifBlank { question.optionC },
                translatedOptionD = json.optString("translatedOptionD", "").ifBlank { question.optionD },
                translatedExplanation = if (question.clinicalExplanationEn.isBlank()) {
                    ""
                } else {
                    json.optString("translatedExplanation", "").ifBlank { question.clinicalExplanationEn }
                }
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Translation timed out for question ${question.id}", e)
            throw IllegalStateException("انتهت مهلة الترجمة الطبية (60 ثانية). يرجى المحاولة مرة أخرى.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Medical translation failed for question ${question.id}: ${e.javaClass.simpleName}: ${e.message}", e)
            val msg = e.message.orEmpty().lowercase()
            if (msg.contains("app check") || msg.contains("appcheck") || msg.contains("attestation") || msg.contains("recaptcha")) {
                throw IllegalStateException(
                    "تعذر التحقق الأمني لـ Firebase App Check حالياً. تأكد من اتصال الشبكة أو حدّث التطبيق للنسخة الموثّقة.",
                    e
                )
            }
            throw e
        }
    }

    private fun buildTranslationPrompt(question: QuestionEntity, targetLanguage: String): String {
        val directionInstruction = if (targetLanguage == TARGET_ARABIC) {
            "Translate this clinical medical board exam question from English to clear, formal Modern Standard Arabic medical terminology. Keep internationally standard abbreviations (e.g., CBC, MCV, PT, PTT, PCR, ELISA, HbA1c, AST, ALT) alongside their Arabic meaning where clinically helpful."
        } else {
            "Translate this clinical medical laboratory licensing question from Arabic to clear, concise international board-exam English medical terminology."
        }

        return buildString {
            appendLine(directionInstruction)
            appendLine("Preserve clinical accuracy, numerical lab values, units, and exact option correspondence (A -> A, B -> B, C -> C, D -> D).")
            if (question.clinicalExplanationEn.isBlank()) {
                appendLine("The explanation input is empty, so return an empty string \"\" for translatedExplanation.")
            }
            appendLine()
            appendLine("Specialty: ${question.subCategory}")
            appendLine("Question: ${question.questionEn}")
            appendLine("Option A: ${question.optionA}")
            appendLine("Option B: ${question.optionB}")
            appendLine("Option C: ${question.optionC}")
            appendLine("Option D: ${question.optionD}")
            appendLine("Explanation: ${question.clinicalExplanationEn}")
        }
    }

    companion object {
        const val TARGET_ARABIC = "ar"
        const val TARGET_ENGLISH = "en"

        fun detectTargetLanguage(question: QuestionEntity): String {
            val sample = question.questionEn
            val arabicCount = sample.count { it in '\u0600'..'\u06FF' }
            val latinCount = sample.count { it in 'A'..'Z' || it in 'a'..'z' }
            return if (arabicCount >= latinCount || arabicCount >= 8) {
                TARGET_ENGLISH
            } else {
                TARGET_ARABIC
            }
        }
    }
}
