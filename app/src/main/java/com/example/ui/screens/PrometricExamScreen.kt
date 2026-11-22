package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.remote.MedicalTranslationService
import com.example.ui.components.DiagnosticImageZoomDialog
import com.example.ui.components.SubtleQuestionTransition
import com.example.ui.components.TranslateActionButton
import com.example.ui.components.TranslatedOptionInlineText
import com.example.ui.components.TranslatedSubTextBlock
import com.example.ui.components.TranslationErrorBanner
import com.example.ui.components.optionTranslationAt
import com.example.ui.theme.ClinicalError
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MedicalBlueDark
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.VerifiedGreen
import com.example.ui.viewmodel.AssessmentViewModel
import com.example.ui.viewmodel.DashboardLanguage
import com.example.ui.viewmodel.StudyModesStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrometricExamScreen(viewModel: AssessmentViewModel) {
    val state by viewModel.prometricState.collectAsState()
    val lang by viewModel.dashboardLanguage.collectAsState()
    val translationsMap by viewModel.translationsState.collectAsState()
    val isAr = lang == DashboardLanguage.AR

    var showNavigatorDialog by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showSubmitConfirmDialog by remember { mutableStateOf(false) }
    var zoomedImageResName by remember { mutableStateOf<String?>(null) }

    BackHandler { showExitConfirmDialog = true }

    val question = state.currentQuestion
    val isFlagged = state.currentIndex in state.flaggedIndices
    val totalCount = state.questions.size
    val selectedOption = state.selectedAnswers[state.currentIndex]
    val translationUi = question?.let { translationsMap[it.id] }
    val showTranslation = translationUi?.isExpanded == true && translationUi.translation != null
    val translatedEntity = translationUi?.translation
    val targetLang = question?.let { MedicalTranslationService.detectTargetLanguage(it) }
        ?: MedicalTranslationService.TARGET_ARABIC
    val countdownText = StudyModesStrings.formatCountdown(state.remainingSeconds)
    val timerColor = when {
        state.remainingSeconds < 60 -> ClinicalError
        state.remainingSeconds < 300 -> GoldAccent
        else -> Color.White
    }

    val layoutDir = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Countdown Timer",
                                        tint = timerColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = countdownText,
                                        color = timerColor,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        modifier = Modifier.testTag("prometric_countdown_text")
                                    )
                                }
                            }
                            Text(
                                text = "Q ${state.currentIndex + 1} / $totalCount • ${state.answeredCount}/$totalCount",
                                fontSize = 11.sp,
                                color = Color(0xFFD6E3FF)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { showExitConfirmDialog = true },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("prometric_exit_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Exit Exam", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.togglePrometricFlag() },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("prometric_flag_btn")
                        ) {
                            Icon(
                                imageVector = if (isFlagged) Icons.Default.Flag else Icons.Default.OutlinedFlag,
                                contentDescription = "Flag Question",
                                tint = if (isFlagged) GoldAccent else Color.White
                            )
                        }
                        IconButton(
                            onClick = { showNavigatorDialog = true },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("prometric_grid_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Question Navigator",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MedicalBlueDark)
                )
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.prevPrometricQuestion() },
                            enabled = state.currentIndex > 0,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("prometric_prev_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isAr) "السابق" else "Prev", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { showSubmitConfirmDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ClinicalTeal),
                            border = BorderStroke(1.5.dp, ClinicalTeal),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("prometric_finish_btn")
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Submit Exam", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAr) "تسليم (${state.answeredCount}/$totalCount)" else "Submit (${state.answeredCount}/$totalCount)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = {
                                if (state.currentIndex < totalCount - 1) {
                                    viewModel.nextPrometricQuestion()
                                } else {
                                    showSubmitConfirmDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.currentIndex < totalCount - 1) MedicalBluePrimary else VerifiedGreen
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("prometric_next_btn")
                        ) {
                            Text(
                                text = if (state.currentIndex < totalCount - 1) {
                                    if (isAr) "التالي" else "Next"
                                } else {
                                    if (isAr) "إنهاء" else "Finish"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        ) { innerPadding ->
            if (question == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isAr) "لا توجد أسئلة متاحة لهذه الجلسة." else "No questions available for this session.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                val progressFraction = if (totalCount > 0) {
                    (state.answeredCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
                } else 0f

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .testTag("prometric_exam_screen"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Progress + Deferred Grading Notice
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = VerifiedGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    SubtleQuestionTransition(
                        targetKey = Pair(state.currentIndex, question),
                        direction = state.navigationDirection
                    ) { (animatedIdx, animatedQuestion) ->
                        val animatedTranslationUi = translationsMap[animatedQuestion.id]
                        val animatedTranslatedEntity = animatedTranslationUi?.translation
                        val animatedShowTranslation = animatedTranslationUi?.isExpanded == true && animatedTranslatedEntity != null
                        val animatedTargetLang = MedicalTranslationService.detectTargetLanguage(animatedQuestion)
                        val animatedSelectedOption = state.selectedAnswers[animatedIdx]
                        val animatedIsFlagged = animatedIdx in state.flaggedIndices

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = ClinicalTeal.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${animatedQuestion.subCategory} • #${animatedQuestion.id}",
                                    color = ClinicalTeal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            if (animatedIsFlagged) {
                                Surface(
                                    color = GoldAccent.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Flag, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isAr) "مميّز للمراجعة" else "Flagged",
                                            color = Color(0xFF92400E),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                    // Question Vignette Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (animatedQuestion.imageResName != null) {
                                val resId = when (animatedQuestion.imageResName) {
                                    "img_hematology_smear" -> R.drawable.img_hematology_smear
                                    "img_urinalysis_crystals" -> R.drawable.img_urinalysis_crystals
                                    "img_microbiology_gram" -> R.drawable.img_microbiology_gram
                                    else -> R.drawable.img_app_icon
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { zoomedImageResName = animatedQuestion.imageResName }
                                ) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = "Diagnostic Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Zoom", color = Color.White, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }

                            Text(
                                text = animatedQuestion.questionEn,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    textDirection = TextDirection.Content,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 23.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            TranslatedSubTextBlock(
                                translatedText = animatedTranslatedEntity?.translatedQuestion,
                                label = if (animatedTargetLang == MedicalTranslationService.TARGET_ARABIC) {
                                    "الترجمة العربية للسؤال"
                                } else {
                                    "English Question Translation"
                                },
                                isVisible = animatedShowTranslation
                            )

                            TranslationErrorBanner(errorMessage = animatedTranslationUi?.errorMessage)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TranslateActionButton(
                                    questionId = animatedQuestion.id,
                                    targetLanguage = animatedTargetLang,
                                    uiState = animatedTranslationUi,
                                    onClick = { viewModel.toggleOrTranslateQuestion(animatedQuestion) }
                                )
                            }
                        }
                    }

                    // Deferred-feedback Option Cards (NO right/wrong reveal until full submission)
                    val options = listOf(animatedQuestion.optionA, animatedQuestion.optionB, animatedQuestion.optionC, animatedQuestion.optionD)
                    val labels = listOf("A", "B", "C", "D")

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        options.forEachIndexed { idx, optionText ->
                            val isSelected = animatedSelectedOption == idx
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { viewModel.selectPrometricOption(idx) }
                                    .testTag("prometric_option_$idx"),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MedicalBluePrimary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        MedicalBluePrimary.copy(alpha = 0.1f)
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp)
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) MedicalBluePrimary else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = labels[idx],
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = optionText,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                textDirection = TextDirection.Content,
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                lineHeight = 20.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        TranslatedOptionInlineText(
                                            translatedOption = animatedTranslatedEntity?.optionTranslationAt(idx),
                                            isVisible = animatedShowTranslation
                                        )
                                    }
                                }
                            }
                        }
                    }
                        }
                    }

                    // Prometric deferred-grading info pill
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isAr) {
                                "محاكاة برومترك: الإجابات والتفسير العلمي محفوظة حتى تسليم الاختبار بالكامل."
                            } else {
                                "Prometric Mode: Feedback and rationales unlock after full exam submission."
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showNavigatorDialog) {
        Dialog(onDismissRequest = { showNavigatorDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .testTag("prometric_navigator_dialog")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isAr) "شبكة التنقل بين الأسئلة" else "Question Navigator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showNavigatorDialog = false },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close Navigator")
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LegendChip(MedicalBluePrimary, if (isAr) "الحالي" else "Current")
                        LegendChip(VerifiedGreen, if (isAr) "مجاب" else "Answered")
                        LegendChip(GoldAccent, if (isAr) "مميز" else "Flagged")
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 48.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items((0 until totalCount).toList()) { idx ->
                            val isCurrent = idx == state.currentIndex
                            val isAnswered = idx in state.selectedAnswers
                            val isItemFlagged = idx in state.flaggedIndices

                            val bg = when {
                                isCurrent -> MedicalBluePrimary
                                isAnswered -> VerifiedGreen.copy(alpha = 0.16f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            val fg = when {
                                isCurrent -> Color.White
                                isAnswered -> VerifiedGreen
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bg)
                                    .then(
                                        if (isItemFlagged) {
                                            Modifier.border(2.dp, GoldAccent, RoundedCornerShape(10.dp))
                                        } else Modifier
                                    )
                                    .clickable {
                                        viewModel.jumpToPrometricQuestion(idx)
                                        showNavigatorDialog = false
                                    }
                                    .testTag("prometric_nav_cell_$idx"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = fg,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSubmitConfirmDialog) {
        val unansweredCount = (totalCount - state.answeredCount).coerceAtLeast(0)
        AlertDialog(
            onDismissRequest = { showSubmitConfirmDialog = false },
            title = {
                Text(
                    text = if (isAr) "تأكيد تسليم الاختبار؟" else "Submit Prometric Exam?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isAr) {
                        "• الأسئلة المجابة: ${state.answeredCount} / $totalCount\n• غير مجابة: $unansweredCount\n• مميزة للمراجعة (Flagged): ${state.flaggedCount}\n\nسيتم التصحيح وحفظ النتيجة في السجل الدائم."
                    } else {
                        "• Answered: ${state.answeredCount} / $totalCount\n• Unanswered: $unansweredCount\n• Flagged: ${state.flaggedCount}\n\nYour exam will be graded and saved permanently for review."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirmDialog = false
                        viewModel.submitPrometricExam()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen),
                    modifier = Modifier.testTag("prometric_confirm_submit_btn")
                ) {
                    Text(if (isAr) "تسليم الآن" else "Submit Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirmDialog = false }) {
                    Text(if (isAr) "مواصلة الحل" else "Continue Exam")
                }
            }
        )
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = {
                Text(
                    text = if (isAr) "إلغاء جلسة المحاكاة؟" else "Exit Timed Exam?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isAr) {
                        "هل تريد الخروج من اختبار برومترك المؤقت؟ لن يتم احتساب الأسئلة الحالية كاختبار مكتمل."
                    } else {
                        "Are you sure you want to leave the timed exam? Progress in this active session will not be submitted."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        viewModel.cancelPrometricExamAndExit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalError),
                    modifier = Modifier.testTag("prometric_confirm_exit_btn")
                ) {
                    Text(if (isAr) "خروج" else "Exit", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text(if (isAr) "البقاء" else "Stay")
                }
            }
        )
    }

    zoomedImageResName?.let { imgName ->
        DiagnosticImageZoomDialog(imageResName = imgName, onDismiss = { zoomedImageResName = null })
    }
}

@Composable
private fun LegendChip(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
