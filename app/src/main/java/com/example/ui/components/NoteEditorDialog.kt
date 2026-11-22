package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.QuestionCategory
import com.example.data.model.UserNoteEntity
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.viewmodel.DashboardLanguage

@Composable
fun NoteEditorDialog(
    existingNote: UserNoteEntity? = null,
    linkedQuestionId: Long? = existingNote?.questionId,
    defaultSpecialtyId: String = existingNote?.specialtyId ?: "GENERAL",
    defaultTopic: String = existingNote?.topic ?: "Clinical Review",
    defaultTitle: String = existingNote?.title ?: "",
    language: DashboardLanguage = DashboardLanguage.EN,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        questionId: Long?,
        specialtyId: String,
        topic: String,
        title: String,
        content: String
    ) -> Unit
) {
    val isAr = language == DashboardLanguage.AR

    var specialtyId by remember(existingNote, defaultSpecialtyId) {
        mutableStateOf(existingNote?.specialtyId ?: defaultSpecialtyId)
    }
    var topic by remember(existingNote, defaultTopic) {
        mutableStateOf(existingNote?.topic ?: defaultTopic)
    }
    var title by remember(existingNote, defaultTitle) {
        mutableStateOf(existingNote?.title ?: defaultTitle)
    }
    var content by remember(existingNote) {
        mutableStateOf(existingNote?.content ?: "")
    }
    var showValidationError by remember { mutableStateOf(false) }

    val specialties = listOf(
        "GENERAL" to (if (isAr) "عام" else "General"),
        QuestionCategory.MEDICAL_LABS to (if (isAr) "المختبرات" else "Med Lab"),
        QuestionCategory.NURSING to (if (isAr) "التمريض" else "Nursing"),
        QuestionCategory.PHYSICIAN_ASSISTANT to (if (isAr) "مساعد طبيب" else "PA / Med")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("note_editor_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = MedicalBluePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                existingNote != null -> if (isAr) "تعديل الملاحظة" else "Edit Study Note"
                                linkedQuestionId != null -> if (isAr) "ملاحظة مرتبطة بالسؤال" else "Question Study Note"
                                else -> if (isAr) "ملاحظة جديدة في الدفتر" else "New Notebook Entry"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("close_note_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (linkedQuestionId != null) {
                    Surface(
                        color = ClinicalTeal.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdded,
                                contentDescription = null,
                                tint = ClinicalTeal,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAr) {
                                    "مرتبطة بالسؤال رقم #$linkedQuestionId"
                                } else {
                                    "Linked to Exam Question #$linkedQuestionId"
                                },
                                color = ClinicalTeal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = if (isAr) "التخصص الطبي:" else "Clinical Track:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(specialties) { (key, label) ->
                            FilterChip(
                                selected = specialtyId == key,
                                onClick = { specialtyId = key },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MedicalBluePrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text(if (isAr) "الموضوع السريري" else "Topic / Discipline") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_topic_input")
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        showValidationError = false
                    },
                    label = { Text(if (isAr) "عنوان الملاحظة" else "Note Title") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_title_input")
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        showValidationError = false
                    },
                    label = { Text(if (isAr) "محتوى الملاحظة الطبية" else "Clinical Pearl / Study Notes") },
                    minLines = 4,
                    maxLines = 8,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_content_input")
                )

                if (showValidationError) {
                    Text(
                        text = if (isAr) {
                            "يرجى إدخال عنوان أو نص للملاحظة قبل الحفظ."
                        } else {
                            "Please enter a note title or study content before saving."
                        },
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text(if (isAr) "إلغاء" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank() && content.isBlank()) {
                                showValidationError = true
                                return@Button
                            }
                            onSave(
                                existingNote?.id ?: 0L,
                                linkedQuestionId,
                                specialtyId,
                                topic.ifBlank { "General" },
                                title.ifBlank { topic.ifBlank { "Clinical Note" } },
                                content.ifBlank { title }
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("save_note_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAr) "حفظ أوفلاين" else "Save Offline",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
