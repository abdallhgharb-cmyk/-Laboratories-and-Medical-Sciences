package com.example.data.remote

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class UserAccountCloud(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val fullName: String = "",
    val degreeType: String = "",
    val specialty: String = "",
    val totalScore: Long = 0L,
    val totalCorrect: Long = 0L,
    val totalAnswered: Long = 0L,
    val masteredTopicsCount: Long = 0L,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = buildMap {
        put("userId", userId)
        put("displayName", displayName.take(100))
        put("email", email.take(150))
        photoUrl?.takeIf { it.isNotBlank() }?.let { put("photoUrl", it.take(500)) }
        put("fullName", fullName.take(120))
        put("degreeType", degreeType.take(40))
        put("specialty", specialty.take(60))
        put("totalScore", totalScore.coerceAtLeast(0L))
        put("totalCorrect", totalCorrect.coerceAtLeast(0L))
        put("totalAnswered", totalAnswered.coerceAtLeast(0L))
        put("masteredTopicsCount", masteredTopicsCount.coerceAtLeast(0L))
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
    }

    fun toUpdateMap(): Map<String, Any> = buildMap {
        put("displayName", displayName.take(100))
        put("email", email.take(150))
        photoUrl?.takeIf { it.isNotBlank() }?.let { put("photoUrl", it.take(500)) }
        put("fullName", fullName.take(120))
        put("degreeType", degreeType.take(40))
        put("specialty", specialty.take(60))
        put("totalScore", totalScore.coerceAtLeast(0L))
        put("totalCorrect", totalCorrect.coerceAtLeast(0L))
        put("totalAnswered", totalAnswered.coerceAtLeast(0L))
        put("masteredTopicsCount", masteredTopicsCount.coerceAtLeast(0L))
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@Keep
@IgnoreExtraProperties
data class CloudProgressDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val specialtyId: String = "MED_LAB",
    val academicDegree: String = "BACHELOR",
    val highestUnlockedStage: Long = 1L,
    val completedStagesCount: Long = 0L,
    val totalScore: Long = 0L,
    val totalCorrectAnswers: Long = 0L,
    val totalQuestionsAnswered: Long = 0L,
    val professionalRankEn: String = "Junior Specialist",
    val professionalRankAr: String = "أخصائي متدرب",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "specialtyId" to specialtyId,
        "academicDegree" to academicDegree,
        "highestUnlockedStage" to highestUnlockedStage.coerceIn(1L, 10L),
        "completedStagesCount" to completedStagesCount.coerceIn(0L, 10L),
        "totalScore" to totalScore.coerceAtLeast(0L),
        "totalCorrectAnswers" to totalCorrectAnswers.coerceAtLeast(0L),
        "totalQuestionsAnswered" to totalQuestionsAnswered.coerceAtLeast(0L),
        "professionalRankEn" to professionalRankEn.take(100),
        "professionalRankAr" to professionalRankAr.take(100),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    fun toUpdateMap(): Map<String, Any> = mapOf(
        "highestUnlockedStage" to highestUnlockedStage.coerceIn(1L, 10L),
        "completedStagesCount" to completedStagesCount.coerceIn(0L, 10L),
        "totalScore" to totalScore.coerceAtLeast(0L),
        "totalCorrectAnswers" to totalCorrectAnswers.coerceAtLeast(0L),
        "totalQuestionsAnswered" to totalQuestionsAnswered.coerceAtLeast(0L),
        "professionalRankEn" to professionalRankEn.take(100),
        "professionalRankAr" to professionalRankAr.take(100),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class CloudNoteDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val localNoteId: Long = 0L,
    val questionId: Long? = null,
    val specialtyId: String = "GENERAL",
    val topic: String = "General",
    val title: String = "",
    val content: String = "",
    val clientUpdatedAtMs: Long = 0L,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = buildMap {
        put("userId", userId)
        put("localNoteId", localNoteId.coerceAtLeast(0L))
        questionId?.takeIf { it > 0L }?.let { put("questionId", it) }
        put("specialtyId", specialtyId)
        put("topic", topic.ifBlank { "General" }.take(120))
        put("title", title.ifBlank { "Clinical Note" }.take(160))
        put("content", content.ifBlank { " " }.take(4000))
        put("clientUpdatedAtMs", clientUpdatedAtMs.coerceAtLeast(0L))
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
    }

    fun toUpdateMap(): Map<String, Any> = buildMap {
        put("localNoteId", localNoteId.coerceAtLeast(0L))
        questionId?.takeIf { it > 0L }?.let { put("questionId", it) }
        put("specialtyId", specialtyId)
        put("topic", topic.ifBlank { "General" }.take(120))
        put("title", title.ifBlank { "Clinical Note" }.take(160))
        put("content", content.ifBlank { " " }.take(4000))
        put("clientUpdatedAtMs", clientUpdatedAtMs.coerceAtLeast(0L))
        put("updatedAt", FieldValue.serverTimestamp())
    }
}
