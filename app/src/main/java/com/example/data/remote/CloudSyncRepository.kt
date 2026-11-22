package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.data.model.ApplicantProfileEntity
import com.example.data.model.UserNoteEntity
import com.example.data.model.UserProgressEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

data class CloudPullPayload(
    val progressList: List<UserProgressEntity>,
    val notesList: List<UserNoteEntity>
)

class CloudSyncRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun pullUserCloudData(): Result<CloudPullPayload> {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            return Result.failure(IllegalStateException("Not authenticated"))
        }

        return try {
            val progressPath = "users/$uid/progress"
            val progressSnapshot = db.collection("users").document(uid)
                .collection("progress")
                .get()
                .await()

            val remoteProgress = progressSnapshot.documents.mapNotNull { doc ->
                val obj = doc.toObject(CloudProgressDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?: return@mapNotNull null
                UserProgressEntity(
                    specialtyId = obj.specialtyId,
                    academicDegree = obj.academicDegree,
                    highestUnlockedStage = obj.highestUnlockedStage.toInt().coerceIn(1, 10),
                    completedStagesCount = obj.completedStagesCount.toInt().coerceIn(0, 10),
                    totalScore = obj.totalScore.toInt().coerceAtLeast(0),
                    totalCorrectAnswers = obj.totalCorrectAnswers.toInt().coerceAtLeast(0),
                    totalQuestionsAnswered = obj.totalQuestionsAnswered.toInt().coerceAtLeast(0),
                    professionalRankEn = obj.professionalRankEn,
                    professionalRankAr = obj.professionalRankAr,
                    lastAssessmentTimestamp = obj.updatedAt?.toDate()?.time ?: System.currentTimeMillis()
                )
            }

            val notesPath = "users/$uid/notes"
            val notesSnapshot = db.collection("users").document(uid)
                .collection("notes")
                .get()
                .await()

            val remoteNotes = notesSnapshot.documents.mapNotNull { doc ->
                val obj = doc.toObject(CloudNoteDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?: return@mapNotNull null
                UserNoteEntity(
                    id = obj.localNoteId,
                    questionId = obj.questionId,
                    specialtyId = obj.specialtyId,
                    topic = obj.topic,
                    title = obj.title,
                    content = obj.content,
                    createdAt = obj.createdAt?.toDate()?.time ?: obj.clientUpdatedAtMs,
                    updatedAt = obj.clientUpdatedAtMs.coerceAtLeast(obj.updatedAt?.toDate()?.time ?: 0L)
                )
            }

            Result.success(CloudPullPayload(progressList = remoteProgress, notesList = remoteNotes))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "users/$uid")
            Result.failure(e)
        }
    }

    suspend fun pushFullSnapshot(
        user: FirebaseUser,
        applicantProfile: ApplicantProfileEntity?,
        progressList: List<UserProgressEntity>,
        notesList: List<UserNoteEntity>,
        totalScore: Long,
        totalCorrect: Long,
        totalAnswered: Long,
        masteredTopicsCount: Long
    ): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        check(uid == user.uid) { "Authenticated user mismatch during cloud sync" }

        return try {
            // 1. Upsert /users/{uid} summary document cleanly (preserving createdAt on update)
            val userDocRef = db.collection("users").document(uid)
            val existingUserDoc = userDocRef.get().await()
            val accountModel = UserAccountCloud(
                userId = uid,
                displayName = user.displayName ?: applicantProfile?.fullName ?: "Student",
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString(),
                fullName = applicantProfile?.fullName ?: (user.displayName ?: "Medical Student"),
                degreeType = applicantProfile?.degreeType ?: "BACHELOR",
                specialty = applicantProfile?.specialty ?: "MED_LAB",
                totalScore = totalScore,
                totalCorrect = totalCorrect,
                totalAnswered = totalAnswered,
                masteredTopicsCount = masteredTopicsCount
            )
            if (existingUserDoc.exists()) {
                userDocRef.update(accountModel.toUpdateMap()).await()
            } else {
                userDocRef.set(accountModel.toCreateMap()).await()
            }

            // 2. Upsert per-specialty + per-degree progress docs
            progressList.forEach { local ->
                if (local.specialtyId in ALLOWED_SPECIALTIES && local.academicDegree in ALLOWED_DEGREES) {
                    val docId = "${local.specialtyId}__${local.academicDegree}"
                    val progRef = userDocRef.collection("progress").document(docId)
                    val existingProg = progRef.get().await()
                    val model = CloudProgressDoc(
                        userId = uid,
                        specialtyId = local.specialtyId,
                        academicDegree = local.academicDegree,
                        highestUnlockedStage = local.highestUnlockedStage.toLong(),
                        completedStagesCount = local.completedStagesCount.toLong(),
                        totalScore = local.totalScore.toLong(),
                        totalCorrectAnswers = local.totalCorrectAnswers.toLong(),
                        totalQuestionsAnswered = local.totalQuestionsAnswered.toLong(),
                        professionalRankEn = local.professionalRankEn,
                        professionalRankAr = local.professionalRankAr
                    )
                    if (existingProg.exists()) {
                        progRef.update(model.toUpdateMap()).await()
                    } else {
                        progRef.set(model.toCreateMap()).await()
                    }
                }
            }

            // 3. Upsert local study notes
            notesList.forEach { note ->
                val noteDocId = "note_${note.id}"
                val noteRef = userDocRef.collection("notes").document(noteDocId)
                val existingNote = noteRef.get().await()
                val safeSpecialty = if (note.specialtyId in ALLOWED_NOTE_SPECIALTIES) note.specialtyId else "GENERAL"
                val model = CloudNoteDoc(
                    userId = uid,
                    localNoteId = note.id,
                    questionId = note.questionId,
                    specialtyId = safeSpecialty,
                    topic = note.topic,
                    title = note.title,
                    content = note.content,
                    clientUpdatedAtMs = note.updatedAt
                )
                if (existingNote.exists()) {
                    noteRef.update(model.toUpdateMap()).await()
                } else {
                    noteRef.set(model.toCreateMap()).await()
                }
            }

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$uid")
            Result.failure(e)
        }
    }

    suspend fun deleteCloudNote(localNoteId: Long): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.success(Unit)
        val docPath = "users/$uid/notes/note_$localNoteId"
        return try {
            db.document(docPath).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    private companion object {
        val ALLOWED_SPECIALTIES = setOf("MED_LAB", "MED_LAB_LICENSING", "NURSING", "PHYSICIAN_ASSISTANT")
        val ALLOWED_DEGREES = setOf("DIPLOMA", "BACHELOR", "MASTER")
        val ALLOWED_NOTE_SPECIALTIES = setOf("MED_LAB", "MED_LAB_LICENSING", "NURSING", "PHYSICIAN_ASSISTANT", "GENERAL")
    }
}
