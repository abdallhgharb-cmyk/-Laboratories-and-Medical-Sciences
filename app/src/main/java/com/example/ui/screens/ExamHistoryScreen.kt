package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuizMode
import com.example.data.model.QuizSessionEntity
import com.example.ui.theme.ClinicalError
import com.example.ui.theme.ClinicalSuccess
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.viewmodel.AssessmentViewModel
import com.example.ui.viewmodel.DashboardLanguage
import com.example.ui.viewmodel.DashboardStrings
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.StudyModesStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class HistoryModeFilter { ALL, PROMETRIC, STAGE, PRACTICE_DAILY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamHistoryScreen(viewModel: AssessmentViewModel) {
    val sessions by viewModel.quizHistory.collectAsState()
    val lang by viewModel.dashboardLanguage.collectAsState()
    val isAr = lang == DashboardLanguage.AR

    var selectedFilter by remember { mutableStateOf(HistoryModeFilter.ALL) }

    BackHandler { viewModel.navigateTo(ScreenDestination.Home) }

    val filteredSessions = remember(sessions, selectedFilter) {
        when (selectedFilter) {
            HistoryModeFilter.ALL -> sessions
            HistoryModeFilter.PROMETRIC -> sessions.filter { it.mode == QuizMode.PROMETRIC_TIMED }
            HistoryModeFilter.STAGE -> sessions.filter { it.mode == QuizMode.STAGE_ASSESSMENT }
            HistoryModeFilter.PRACTICE_DAILY -> sessions.filter {
                it.mode == QuizMode.TOPIC_PRACTICE || it.mode == QuizMode.DAILY_CHALLENGE
            }
        }
    }

    val layoutDir = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isAr) "سجل الاختبارات والمراجعة" else "Exam Review History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isAr) "أرشيف دائم لجميع محاولاتك" else "Permanent Archive of Past Quizzes",
                                fontSize = 11.sp,
                                color = Color(0xFFD6E3FF)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { viewModel.navigateTo(ScreenDestination.Home) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("history_back_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        Surface(
                            color = Color.White.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.toggleDashboardLanguage() }
                                .testTag("history_lang_toggle")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Translate, contentDescription = "Toggle Language", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(DashboardStrings.toggleLabel(lang), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MedicalBluePrimary)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("exam_history_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedFilter == HistoryModeFilter.ALL,
                                onClick = { selectedFilter = HistoryModeFilter.ALL },
                                label = { Text("${DashboardStrings.filterAll(lang)} (${sessions.size})", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MedicalBluePrimary, selectedLabelColor = Color.White),
                                modifier = Modifier.testTag("history_filter_all")
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == HistoryModeFilter.PROMETRIC,
                                onClick = { selectedFilter = HistoryModeFilter.PROMETRIC },
                                label = { Text(if (isAr) "برومترك مؤقت" else "Prometric Timed", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ClinicalTeal, selectedLabelColor = Color.White),
                                modifier = Modifier.testTag("history_filter_prometric")
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == HistoryModeFilter.STAGE,
                                onClick = { selectedFilter = HistoryModeFilter.STAGE },
                                label = { Text(if (isAr) "المستويات" else "Stage Exams", fontSize = 12.sp) },
                                modifier = Modifier.testTag("history_filter_stage")
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == HistoryModeFilter.PRACTICE_DAILY,
                                onClick = { selectedFilter = HistoryModeFilter.PRACTICE_DAILY },
                                label = { Text(if (isAr) "تدريب ويومي" else "Topic & Daily", fontSize = 12.sp) },
                                modifier = Modifier.testTag("history_filter_practice")
                            )
                        }
                    }
                }

                if (filteredSessions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("history_empty_card"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, tint = ClinicalTeal, modifier = Modifier.size(44.dp))
                                Text(
                                    text = if (isAr) "لا توجد اختبارات مسجلة في هذا التصنيف بعد" else "No Saved Quizzes in This Filter Yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = if (isAr) {
                                        "عند إكمالك لأي اختبار مستوى أو محاكاة برومترك مؤقتة سيتم حفظه هنا لمراجعة إجاباتك والتفسيرات العلمية في أي وقت."
                                    } else {
                                        "Completed stage exams and timed Prometric simulations are saved permanently here so you can review your marked answers and rationales anytime."
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                                Button(
                                    onClick = { viewModel.openPrometricSetup() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("history_start_prometric_cta")
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isAr) "بدء اختبار برومترك مؤقت" else "Start Prometric Timed Exam", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    items(filteredSessions, key = { it.sessionId }) { session ->
                        QuizHistorySessionCard(
                            session = session,
                            lang = lang,
                            onReview = { viewModel.openSessionReview(session.sessionId) },
                            onDelete = { viewModel.deleteQuizSession(session.sessionId) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun QuizHistorySessionCard(
    session: QuizSessionEntity,
    lang: DashboardLanguage,
    onReview: () -> Unit,
    onDelete: () -> Unit
) {
    val isAr = lang == DashboardLanguage.AR
    val accuracy = if (session.totalQuestions > 0) {
        ((session.correctCount.toFloat() / session.totalQuestions) * 100f).toInt().coerceIn(0, 100)
    } else 0
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.ENGLISH) }
    val dateLabel = remember(session.completedAt) { dateFormat.format(Date(session.completedAt)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onReview)
            .testTag("history_session_card_${session.sessionId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (session.isPassed) ClinicalSuccess.copy(alpha = 0.14f) else ClinicalError.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (session.isPassed) ClinicalSuccess else ClinicalError,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${StudyModesStrings.quizModeTitle(session.mode, lang)} • $accuracy%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (session.isPassed) ClinicalSuccess else ClinicalError
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("history_delete_btn_${session.sessionId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Session",
                        tint = ClinicalError,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = buildString {
                    append(DashboardStrings.specialtyName(session.specialtyId, lang))
                    append(" • ")
                    append(DashboardStrings.degreeLabel(session.academicDegree, lang))
                    if (!session.topicTitle.isNullOrBlank()) {
                        append(" (${session.topicTitle})")
                    } else if (session.mode == QuizMode.STAGE_ASSESSMENT) {
                        append(if (isAr) " • مستوى ${session.stageLevel}" else " • Stage ${session.stageLevel}")
                    }
                },
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (session.timeSpentSeconds > 0) {
                        "${session.correctCount}/${session.totalQuestions} • ${StudyModesStrings.formatCountdown(session.timeSpentSeconds)}"
                    } else {
                        "${session.correctCount}/${session.totalQuestions} • +${session.scoreEarned} pts"
                    },
                    fontSize = 12.sp,
                    color = ClinicalTeal,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = dateLabel,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isAr) "مراجعة الإجابات والشرح" else "Review Marked Answers & Explanations",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MedicalBluePrimary
                )
            }
        }
    }
}
