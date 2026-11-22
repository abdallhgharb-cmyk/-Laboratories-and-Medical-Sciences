package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Filter2
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AcademicDegree
import com.example.data.model.QuestionEntity
import com.example.data.remote.MedicalTranslationService
import com.example.ui.components.DiagnosticImageZoomDialog
import com.example.ui.components.NoteEditorDialog
import com.example.ui.components.SubtleQuestionTransition
import com.example.ui.components.TranslateActionButton
import com.example.ui.components.TranslatedOptionInlineText
import com.example.ui.components.TranslatedSubTextBlock
import com.example.ui.components.TranslationErrorBanner
import com.example.ui.components.optionTranslationAt
import com.example.ui.theme.ClinicalError
import com.example.ui.theme.ClinicalSuccess
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.ClinicalWarning
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.viewmodel.AssessmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentScreen(
    specialtyId: String,
    stageLevel: Int,
    academicDegree: String = AcademicDegree.BACHELOR,
    viewModel: AssessmentViewModel
) {
    val state by viewModel.sessionState.collectAsState()
    val isMuted by viewModel.audioEngine.isMuted.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    val notedQuestionIds by viewModel.notedQuestionIds.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val dashboardLanguage by viewModel.dashboardLanguage.collectAsState()
    val translationsMap by viewModel.translationsState.collectAsState()

    var showQuestionNoteDialog by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.navigateBackFromAssessment()
    }

    val currentQ = state.currentQuestion
    val isCurrentBookmarked = currentQ != null && currentQ.id in bookmarkedIds
    val hasCurrentNote = currentQ != null && currentQ.id in notedQuestionIds
    val translationUi = currentQ?.let { translationsMap[it.id] }
    val showTranslation = translationUi?.isExpanded == true && translationUi.translation != null
    val translatedEntity = translationUi?.translation
    val targetLang = currentQ?.let { MedicalTranslationService.detectTargetLanguage(it) }
        ?: MedicalTranslationService.TARGET_ARABIC

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when {
                                state.isDailyChallenge -> "التحدي السريري اليومي"
                                state.isTopicPractice -> state.activeTopicTitle ?: "تدريب سريري"
                                else -> "التقييم السريري | المستوى $stageLevel"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        Text(
                            text = "السؤال ${state.currentIndex + 1} من ${state.questionQueue.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD6E3FF)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBackFromAssessment() },
                        modifier = Modifier.testTag("assessment_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (currentQ != null) {
                        IconButton(
                            onClick = { viewModel.toggleBookmark(currentQ.id) },
                            modifier = Modifier.testTag("assessment_bookmark_button")
                        ) {
                            Icon(
                                imageVector = if (isCurrentBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark Question",
                                tint = if (isCurrentBookmarked) GoldAccent else Color.White
                            )
                        }
                        IconButton(
                            onClick = { showQuestionNoteDialog = true },
                            modifier = Modifier.testTag("assessment_note_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = "Question Note",
                                tint = if (hasCurrentNote) GoldAccent else Color.White
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.toggleAudio() },
                        modifier = Modifier.testTag("sound_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MedicalBluePrimary
                )
            )
        }
    ) { innerPadding ->
        if (currentQ == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val totalQuestions = state.questionQueue.size.coerceAtLeast(1)
            val answeredOrCurrent = (state.currentIndex + if (state.isAnswerSubmitted) 1 else 0)
                .coerceIn(0, totalQuestions)
            val rawProgress = answeredOrCurrent.toFloat() / totalQuestions.toFloat()
            val animatedProgress by animateFloatAsState(
                targetValue = rawProgress,
                animationSpec = tween(durationMillis = 320),
                label = "assessment_progress"
            )
            val progressPercent = (rawProgress * 100f).toInt().coerceIn(0, 100)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تقدم الاختبار: ${state.currentIndex + 1} / $totalQuestions",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalBluePrimary
                            )
                            Text(
                                text = "$progressPercent%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ClinicalTeal
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(CircleShape)
                                .testTag("assessment_progress_bar"),
                            color = ClinicalTeal,
                            trackColor = MedicalBluePrimary.copy(alpha = 0.14f)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("assessment_screen_content"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Top HUD: Timer + Hearts + Score
                    item {
                        HUDBar(
                            remainingSeconds = state.remainingSeconds,
                            heartsRemaining = state.heartsRemaining,
                            score = state.scoreEarned
                        )
                    }

                    if (state.heartsRemaining == 0) {
                        item {
                            Surface(
                                color = ClinicalWarning.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ClinicalWarning.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("zero_hearts_continue_banner")
                            ) {
                                Text(
                                    text = "نفدت القلوب الإضافية — واصل إكمال باقي الأسئلة! يتم فتح المرحلة التالية عند تحقيق 70% فأعلى ونيل نجمتين.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                // 2. The 4 Clinical Lifelines
                item {
                    ClinicalLifelinesRow(
                        lifeline5050Used = state.lifeline5050Used,
                        lifelineHintUsed = state.lifelineHintUsed,
                        lifelineSwapUsed = state.lifelineSwapUsed,
                        lifelineTimeUsed = state.lifelineTimeUsed,
                        isAnswerSubmitted = state.isAnswerSubmitted,
                        onUse5050 = { viewModel.useLifeline5050() },
                        onUseHint = { viewModel.useLifelineHint() },
                        onUseSwap = { viewModel.useLifelineSwap() },
                        onUseAddTime = { viewModel.useLifelineAddTime() }
                    )
                }

                // 3–7. Animated Active Question & Options Container
                item(key = "animated_question_container") {
                    SubtleQuestionTransition(
                        targetKey = currentQ,
                        direction = 1
                    ) { animatedQ ->
                        val qTranslationUi = translationsMap[animatedQ.id]
                        val qTranslatedEntity = qTranslationUi?.translation
                        val qShowTranslation = qTranslationUi?.isExpanded == true && qTranslatedEntity != null
                        val qTargetLang = MedicalTranslationService.detectTargetLanguage(animatedQ)
                        val isSameQuestionState = animatedQ.id == currentQ.id

                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // 3. Clinical Diagnostic Clue (Hint Banner)
                            if (isSameQuestionState && state.isHintRevealed) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.15f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, GoldAccent, RoundedCornerShape(12.dp))
                                        .testTag("hint_banner")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = GoldAccent)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "التلميح السريري التشخيصي (Clinical Diagnostic Clue):",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = animatedQ.clinicalHintEn,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 13.sp,
                                                    textDirection = TextDirection.Content
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            // 4. Contextual Diagnostic Image (Microscopy)
                            if (animatedQ.imageResName != null) {
                                DiagnosticImageCard(
                                    imageResName = animatedQ.imageResName,
                                    onZoomClick = { viewModel.setEnlargedImage(animatedQ.imageResName) }
                                )
                            }

                            // 5. Board Exam Question Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Surface(
                                            color = ClinicalTeal.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(Icons.Default.Science, contentDescription = null, tint = ClinicalTeal, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = animatedQ.subCategory,
                                                    color = ClinicalTeal,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        Surface(
                                            color = MedicalBluePrimary.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = if (animatedQ.specialtyId == com.example.data.model.QuestionCategory.MED_LAB_LICENSING) {
                                                    "مزاولة المهنة • Licensing"
                                                } else {
                                                    "Board Prometric Standard"
                                                },
                                                color = MedicalBluePrimary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = animatedQ.questionEn,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 23.sp,
                                            textDirection = TextDirection.Content
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    TranslatedSubTextBlock(
                                        translatedText = qTranslatedEntity?.translatedQuestion,
                                        label = if (qTargetLang == MedicalTranslationService.TARGET_ARABIC) {
                                            "الترجمة العربية للسؤال"
                                        } else {
                                            "English Question Translation"
                                        },
                                        isVisible = qShowTranslation,
                                        modifier = Modifier.testTag("translated_question_block_${animatedQ.id}")
                                    )

                                    TranslationErrorBanner(errorMessage = qTranslationUi?.errorMessage)

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TranslateActionButton(
                                            questionId = animatedQ.id,
                                            targetLanguage = qTargetLang,
                                            uiState = qTranslationUi,
                                            onClick = { viewModel.toggleOrTranslateQuestion(animatedQ) }
                                        )
                                    }
                                }
                            }

                            // 6. Options List (A, B, C, D)
                            val options = listOf(
                                Pair(0, animatedQ.optionA),
                                Pair(1, animatedQ.optionB),
                                Pair(2, animatedQ.optionC),
                                Pair(3, animatedQ.optionD)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                options.forEach { (index, text) ->
                                    val isEliminated = isSameQuestionState && (index in state.eliminatedOptionIndices)
                                    val isSelected = isSameQuestionState && (state.selectedOptionIndex == index)
                                    val isCorrectChoice = index == animatedQ.correctOptionIndex

                                    OptionCard(
                                        letter = ('A' + index).toString(),
                                        text = text,
                                        translatedText = qTranslatedEntity?.optionTranslationAt(index),
                                        showTranslation = qShowTranslation,
                                        isEliminated = isEliminated,
                                        isSelected = isSelected,
                                        isSubmitted = isSameQuestionState && state.isAnswerSubmitted,
                                        isCorrect = isCorrectChoice,
                                        onClick = {
                                            if (isSameQuestionState && !state.isAnswerSubmitted && !isEliminated) {
                                                viewModel.submitAnswer(index)
                                            }
                                        }
                                    )
                                }
                            }

                            // 7. Scientific Board Explanation Card
                            if (isSameQuestionState && state.isAnswerSubmitted) {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn() + slideInVertically()
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("explanation_card"),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (state.isAnswerCorrect) {
                                                ClinicalSuccess.copy(alpha = 0.08f)
                                            } else {
                                                ClinicalError.copy(alpha = 0.08f)
                                            }
                                        ),
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(
                                                if (state.isAnswerCorrect) ClinicalSuccess else ClinicalError
                                            )
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (state.isAnswerCorrect) Icons.Default.CheckCircle else Icons.Default.Info,
                                                    contentDescription = null,
                                                    tint = if (state.isAnswerCorrect) ClinicalSuccess else ClinicalError,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = if (state.isAnswerCorrect) "إجابة صحيحة | Correct Evaluation" else "إجابة غير صحيحة | Correct Rationale",
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (state.isAnswerCorrect) ClinicalSuccess else ClinicalError,
                                                    fontSize = 14.sp
                                                )
                                            }

                                            val correctOptionText = listOf(
                                                animatedQ.optionA,
                                                animatedQ.optionB,
                                                animatedQ.optionC,
                                                animatedQ.optionD
                                            ).getOrElse(animatedQ.correctOptionIndex) { "" }

                                            if (!state.isAnswerCorrect || animatedQ.clinicalExplanationEn.isBlank()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Surface(
                                                    color = ClinicalSuccess.copy(alpha = 0.12f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "الإجابة الصحيحة المعتمدة (${('A' + animatedQ.correctOptionIndex)}): $correctOptionText",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            textDirection = TextDirection.Content
                                                        ),
                                                        color = ClinicalSuccess,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                                    )
                                                }
                                            }

                                            if (animatedQ.clinicalExplanationEn.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "الشرح العلمي:",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = ClinicalTeal
                                                )
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = animatedQ.clinicalExplanationEn,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        lineHeight = 20.sp,
                                                        textDirection = TextDirection.Content
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                TranslatedSubTextBlock(
                                                    translatedText = qTranslatedEntity?.translatedExplanation,
                                                    label = if (qTargetLang == MedicalTranslationService.TARGET_ARABIC) {
                                                        "ترجمة الشرح العلمي"
                                                    } else {
                                                        "Translated Explanation"
                                                    },
                                                    isVisible = qShowTranslation,
                                                    modifier = Modifier.testTag("translated_explanation_block_${animatedQ.id}")
                                                )
                                            }

                                            if (!animatedQ.scientificReference.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Surface(
                                                    color = MedicalBluePrimary.copy(alpha = 0.08f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("reference_card")
                                                ) {
                                                    Text(
                                                        text = "المرجع العلمي (Reference): ${animatedQ.scientificReference}",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = FontWeight.SemiBold,
                                                            textDirection = TextDirection.Content
                                                        ),
                                                        color = MedicalBluePrimary,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(14.dp))

                                            Button(
                                                onClick = { viewModel.moveToNextQuestion() },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (state.isAnswerCorrect) ClinicalSuccess else MedicalBluePrimary
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(46.dp)
                                                    .testTag("next_question_button")
                                            ) {
                                                Text(
                                                    text = if (state.currentIndex + 1 >= state.questionQueue.size) "عرض نتائج التقييم النهائي" else "السؤال التالي",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Zoom Dialog for Diagnostic Image
    state.enlargedImageResName?.let { imgName ->
        DiagnosticImageZoomDialog(
            imageResName = imgName,
            onDismiss = { viewModel.setEnlargedImage(null) }
        )
    }

    // Question-Linked Note Editor Dialog
    if (showQuestionNoteDialog && currentQ != null) {
        val existingLinkedNote = remember(allNotes, currentQ.id) {
            allNotes.firstOrNull { it.questionId == currentQ.id }
        }
        NoteEditorDialog(
            existingNote = existingLinkedNote,
            linkedQuestionId = currentQ.id,
            defaultSpecialtyId = currentQ.specialtyId,
            defaultTopic = currentQ.subCategory,
            defaultTitle = existingLinkedNote?.title ?: "Q#${currentQ.id}: ${currentQ.subCategory}",
            language = dashboardLanguage,
            onDismiss = { showQuestionNoteDialog = false },
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

@Composable
private fun HUDBar(
    remainingSeconds: Int,
    heartsRemaining: Int,
    score: Int
) {
    val timerColor = when {
        remainingSeconds <= 10 -> ClinicalError
        remainingSeconds <= 25 -> ClinicalWarning
        else -> MedicalBluePrimary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Hearts / Clinical Endurance
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { i ->
                    val isAlive = i < heartsRemaining
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Heart",
                        tint = if (isAlive) ClinicalError else Color.Gray.copy(alpha = 0.3f),
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 2.dp)
                    )
                }
            }

            // Countdown Timer Pill
            Surface(
                color = timerColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, timerColor)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Timer",
                        tint = timerColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${remainingSeconds}s",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = timerColor
                    )
                }
            }

            // Score Pill
            Surface(
                color = GoldAccent.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "$score نقطة",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ClinicalLifelinesRow(
    lifeline5050Used: Boolean,
    lifelineHintUsed: Boolean,
    lifelineSwapUsed: Boolean,
    lifelineTimeUsed: Boolean,
    isAnswerSubmitted: Boolean,
    onUse5050: () -> Unit,
    onUseHint: () -> Unit,
    onUseSwap: () -> Unit,
    onUseAddTime: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LifelineButton(
            title = "50:50",
            subtitle = "حذف خطأين",
            icon = Icons.Default.Filter2,
            isUsed = lifeline5050Used,
            isEnabled = !lifeline5050Used && !isAnswerSubmitted,
            modifier = Modifier.weight(1f),
            onClick = onUse5050
        )
        LifelineButton(
            title = "تلميح",
            subtitle = "خيط سريري",
            icon = Icons.Default.Lightbulb,
            isUsed = lifelineHintUsed,
            isEnabled = !lifelineHintUsed && !isAnswerSubmitted,
            modifier = Modifier.weight(1f),
            onClick = onUseHint
        )
        LifelineButton(
            title = "استبدال",
            subtitle = "سؤال بديل",
            icon = Icons.Default.Autorenew,
            isUsed = lifelineSwapUsed,
            isEnabled = !lifelineSwapUsed && !isAnswerSubmitted,
            modifier = Modifier.weight(1f),
            onClick = onUseSwap
        )
        LifelineButton(
            title = "+30ث",
            subtitle = "تمديد وقت",
            icon = Icons.Default.MoreTime,
            isUsed = lifelineTimeUsed,
            isEnabled = !lifelineTimeUsed && !isAnswerSubmitted,
            modifier = Modifier.weight(1f),
            onClick = onUseAddTime
        )
    }
}

@Composable
private fun LifelineButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isUsed: Boolean,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = isEnabled, onClick = onClick)
            .testTag("lifeline_${title}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isUsed) {
                Color.Gray.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isEnabled) 2.dp else 0.dp),
        border = if (isEnabled) androidx.compose.foundation.BorderStroke(1.dp, MedicalBluePrimary.copy(alpha = 0.3f)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isUsed) Color.Gray else MedicalBluePrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUsed) Color.Gray else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = if (isUsed) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DiagnosticImageCard(
    imageResName: String,
    onZoomClick: () -> Unit
) {
    val resId = when (imageResName) {
        "img_hematology_smear" -> R.drawable.img_hematology_smear
        "img_urinalysis_crystals" -> R.drawable.img_urinalysis_crystals
        "img_microbiology_gram" -> R.drawable.img_microbiology_gram
        else -> R.drawable.img_app_icon
    }

    val label = when (imageResName) {
        "img_hematology_smear" -> "مسحة دم محيطية مجهرية | Peripheral Blood Smear (100x)"
        "img_urinalysis_crystals" -> "راسب بول مجهري | Urine Sediment Microscopy (40x)"
        "img_microbiology_gram" -> "صبغة جرام مجهرية | Gram Stain Microscopy (100x)"
        else -> "الفحص التشخيصي المجهري"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onZoomClick)
            .testTag("diagnostic_image_card"),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Zoom Pill Overlay
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "تكبير ملء الشاشة (Pinch to Zoom)",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun OptionCard(
    letter: String,
    text: String,
    translatedText: String? = null,
    showTranslation: Boolean = false,
    isEliminated: Boolean,
    isSelected: Boolean,
    isSubmitted: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isEliminated -> Color.Gray.copy(alpha = 0.12f)
        isSubmitted && isCorrect -> ClinicalSuccess.copy(alpha = 0.15f)
        isSubmitted && isSelected && !isCorrect -> ClinicalError.copy(alpha = 0.15f)
        isSelected -> MedicalBluePrimary.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isEliminated -> Color.Transparent
        isSubmitted && isCorrect -> ClinicalSuccess
        isSubmitted && isSelected && !isCorrect -> ClinicalError
        isSelected -> MedicalBluePrimary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val textColor = when {
        isEliminated -> Color.Gray.copy(alpha = 0.4f)
        isSubmitted && isCorrect -> ClinicalSuccess
        isSubmitted && isSelected && !isCorrect -> ClinicalError
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !isSubmitted && !isEliminated, onClick = onClick)
            .testTag("option_$letter"),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isEliminated -> Color.Gray.copy(alpha = 0.2f)
                            isSubmitted && isCorrect -> ClinicalSuccess
                            isSubmitted && isSelected && !isCorrect -> ClinicalError
                            isSelected -> MedicalBluePrimary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSubmitted && isCorrect) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                } else if (isSubmitted && isSelected && !isCorrect) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text(
                        text = letter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected || (isSubmitted && isCorrect)) FontWeight.Bold else FontWeight.Normal,
                        lineHeight = 20.sp,
                        textDirection = TextDirection.Content
                    ),
                    color = textColor
                )
                TranslatedOptionInlineText(
                    translatedOption = translatedText,
                    isVisible = showTranslation && !isEliminated
                )
            }
        }
    }
}
