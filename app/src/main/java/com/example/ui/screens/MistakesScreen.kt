package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionEntity
import com.example.ui.components.NoteEditorDialog
import com.example.ui.theme.ClinicalError
import com.example.ui.theme.ClinicalSuccess
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.viewmodel.AssessmentViewModel
import com.example.ui.viewmodel.ScreenDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MistakesScreen(viewModel: AssessmentViewModel) {
    val mistakeQuestions by viewModel.mistakeQuestions.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    val notedQuestionIds by viewModel.notedQuestionIds.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val dashboardLanguage by viewModel.dashboardLanguage.collectAsState()
    var noteTargetQuestion by remember { mutableStateOf<QuestionEntity?>(null) }

    BackHandler {
        viewModel.navigateTo(ScreenDestination.Home)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "دفتر مراجعة الأخطاء السريرية (${mistakeQuestions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.Home) },
                        modifier = Modifier.testTag("mistakes_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = ClinicalError)
            )
        }
    ) { innerPadding ->
        if (mistakeQuestions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ClinicalSuccess,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "سجل الأخطاء نظيف تماماً!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "لم يتم تسجيل أي إجابات غير صحيحة بعد. عند ارتكاب أي خطأ أثناء التقييم السريري، سيتم حفظ السؤال تلقائياً هنا مع التفسير العلمي لإعادة مراجعته وتثبيته.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("mistakes_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ClinicalError.copy(alpha = 0.08f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ClinicalError.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ClinicalError, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "تتضمن هذه القائمة جميع الأسئلة التي تمت الإجابة عليها بشكل غير صحيح مع التفسير السريري لتصحيح المفاهيم وتثبيت المعلومة.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                items(mistakeQuestions) { question ->
                    val isBookmarked = question.id in bookmarkedIds
                    val hasNote = question.id in notedQuestionIds
                    Column {
                        QuestionSearchItemCard(
                            question = question,
                            isBookmarked = isBookmarked,
                            onToggleBookmark = { viewModel.toggleBookmark(question.id) },
                            hasNote = hasNote,
                            onNoteClick = { noteTargetQuestion = question }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                viewModel.startAssessment(
                                    specialtyId = question.specialtyId,
                                    academicDegree = question.academicDegree,
                                    stageLevel = question.stageLevel
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إعادة خوض مرحلة هذا السؤال (المستوى ${question.stageLevel})", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    noteTargetQuestion?.let { q ->
        val existingLinkedNote = allNotes.firstOrNull { it.questionId == q.id }
        NoteEditorDialog(
            existingNote = existingLinkedNote,
            linkedQuestionId = q.id,
            defaultSpecialtyId = q.specialtyId,
            defaultTopic = q.subCategory,
            defaultTitle = existingLinkedNote?.title ?: "Q#${q.id}: ${q.subCategory}",
            language = dashboardLanguage,
            onDismiss = { noteTargetQuestion = null },
            onSave = { id, qId, spec, topic, title, content ->
                viewModel.saveNote(
                    id = id,
                    questionId = qId,
                    specialtyId = spec,
                    topic = topic,
                    title = title,
                    content = content
                )
            }
        )
    }
}
