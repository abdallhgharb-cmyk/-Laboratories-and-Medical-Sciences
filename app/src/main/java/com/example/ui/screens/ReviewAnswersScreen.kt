package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
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
import com.example.data.repository.ReviewQuestionEntry
import com.example.ui.components.DiagnosticImageZoomDialog
import com.example.ui.components.NoteEditorDialog
import com.example.ui.components.TranslateActionButton
import com.example.ui.components.TranslatedSubTextBlock
import com.example.ui.components.TranslationErrorBanner
import com.example.ui.components.optionTranslationAt
import com.example.ui.theme.ClinicalError
import com.example.ui.theme.ClinicalSuccess
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MedicalBlueDark
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.viewmodel.AssessmentViewModel
import com.example.ui.viewmodel.DashboardLanguage
import com.example.ui.viewmodel.DashboardStrings
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.StudyModesStrings

private enum class ReviewItemFilter { ALL, INCORRECT_ONLY, FLAGGED_ONLY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewAnswersScreen(
    specialtyId: String,
    stageLevel: Int,
    academicDegree: String = AcademicDegree.BACHELOR,
    sessionId: Long? = null,
    fromHistory: Boolean = false,
    isPrometricTimed: Boolean = false,
    viewModel: AssessmentViewModel
) {
    val state by viewModel.sessionState.collectAsState()
    val loadedReview by viewModel.loadedReviewState.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val notedQuestionIds by viewModel.notedQuestionIds.collectAsState()
    val dashboardLanguage by viewModel.dashboardLanguage.collectAsState()
    val translationsMap by viewModel.translationsState.collectAsState()
    val isAr = dashboardLanguage == DashboardLanguage.AR

    var noteTargetQuestion by remember { mutableStateOf<QuestionEntity?>(null) }
    var filterMode by remember { mutableStateOf(ReviewItemFilter.ALL) }

    val navigateBack = {
        if (fromHistory) {
            viewModel.navigateTo(ScreenDestination.ExamHistory)
        } else {
            viewModel.navigateTo(
                ScreenDestination.Result(
                    specialtyId = specialtyId,
                    stageLevel = stageLevel,
                    isPassed = state.isPassed,
                    academicDegree = academicDegree,
                    isPrometricTimed = isPrometricTimed || state.isPrometricTimed
                )
            )
        }
    }

    BackHandler { navigateBack() }

    val allEntries: List<ReviewQuestionEntry> = remember(loadedReview.items, state.answersHistory, sessionId) {
        if (loadedReview.items.isNotEmpty()) {
            loadedReview.items
        } else if (sessionId == null) {
            state.answersHistory.map { rec ->
                ReviewQuestionEntry(
                    question = rec.question,
                    selectedOptionIndex = rec.selectedOptionIndex,
                    isCorrect = rec.isCorrect,
                    wasFlagged = false
                )
            }
        } else {
            emptyList()
        }
    }

    val filteredEntries = remember(allEntries, filterMode) {
        when (filterMode) {
            ReviewItemFilter.ALL -> allEntries
            ReviewItemFilter.INCORRECT_ONLY -> allEntries.filter { !it.isCorrect }
            ReviewItemFilter.FLAGGED_ONLY -> allEntries.filter { it.wasFlagged }
        }
    }

    val correctTotal = allEntries.count { it.isCorrect }
    val wrongTotal = allEntries.count { !it.isCorrect }
    val flaggedTotal = allEntries.count { it.wasFlagged }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isAr) "المراجعة السريرية والأدلة العلمية" else "Exam Review & Explanations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = navigateBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("review_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
        if (loadedReview.isLoading && allEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("review_answers_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Summary Banner
                item {
                    val sessionInfo = loadedReview.session
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MedicalBlueDark)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = sessionInfo?.let {
                                    "${StudyModesStrings.quizModeTitle(it.mode, dashboardLanguage)} • ${DashboardStrings.specialtyName(it.specialtyId, dashboardLanguage)}"
                                } ?: if (isAr) "ملخص مراجعة الإجابات والأدلة" else "Quiz Answers & Clinical Rationales",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isAr) {
                                    "صحيح: $correctTotal/${allEntries.size} • خطأ: $wrongTotal" +
                                        if (flaggedTotal > 0) " • مميزة: $flaggedTotal" else ""
                                } else {
                                    "Correct: $correctTotal/${allEntries.size} • Incorrect: $wrongTotal" +
                                        if (flaggedTotal > 0) " • Flagged: $flaggedTotal" else ""
                                },
                                color = Color(0xFFD6E3FF),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Filter Chips
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = filterMode == ReviewItemFilter.ALL,
                                onClick = { filterMode = ReviewItemFilter.ALL },
                                label = { Text(if (isAr) "الكل (${allEntries.size})" else "All (${allEntries.size})", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MedicalBluePrimary, selectedLabelColor = Color.White),
                                modifier = Modifier.testTag("review_filter_all")
                            )
                        }
                        item {
                            FilterChip(
                                selected = filterMode == ReviewItemFilter.INCORRECT_ONLY,
                                onClick = { filterMode = ReviewItemFilter.INCORRECT_ONLY },
                                label = { Text(if (isAr) "الأخطاء ($wrongTotal)" else "Wrong ($wrongTotal)", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ClinicalError, selectedLabelColor = Color.White),
                                modifier = Modifier.testTag("review_filter_wrong")
                            )
                        }
                        if (flaggedTotal > 0) {
                            item {
                                FilterChip(
                                    selected = filterMode == ReviewItemFilter.FLAGGED_ONLY,
                                    onClick = { filterMode = ReviewItemFilter.FLAGGED_ONLY },
                                    label = { Text(if (isAr) "المميزة ($flaggedTotal)" else "Flagged ($flaggedTotal)", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldAccent, selectedLabelColor = Color.Black),
                                    modifier = Modifier.testTag("review_filter_flagged")
                                )
                            }
                        }
                    }
                }

                if (filteredEntries.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = if (isAr) "لا توجد أسئلة مطابقة لهذا التصنيف." else "No questions match this filter.",
                                modifier = Modifier.padding(18.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    itemsIndexed(filteredEntries, key = { idx, entry -> "${entry.question.id}_$idx" }) { index, item ->
                        val q = item.question
                        val translationUi = translationsMap[q.id]
                        val showTranslation = translationUi?.isExpanded == true && translationUi.translation != null
                        val translatedEntity = translationUi?.translation
                        val targetLang = MedicalTranslationService.detectTargetLanguage(q)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("review_card_${q.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            color = if (item.isCorrect) ClinicalSuccess.copy(alpha = 0.12f) else ClinicalError.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (item.isCorrect) Icons.Default.Check else Icons.Default.Close,
                                                    contentDescription = null,
                                                    tint = if (item.isCorrect) ClinicalSuccess else ClinicalError,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (item.isCorrect) {
                                                        if (isAr) "إجابة صحيحة" else "Correct"
                                                    } else {
                                                        if (isAr) "إجابة خاطئة" else "Incorrect"
                                                    },
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (item.isCorrect) ClinicalSuccess else ClinicalError
                                                )
                                            }
                                        }

                                        if (item.wasFlagged) {
                                            Surface(
                                                color = GoldAccent.copy(alpha = 0.18f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                                ) {
                                                    Icon(Icons.Default.Flag, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = if (isAr) "مميز" else "Flagged",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF92400E)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Text(
                                        text = if (isAr) "السؤال ${index + 1}" else "Q ${index + 1} • #${q.id}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (q.imageResName != null) {
                                    val resId = when (q.imageResName) {
                                        "img_hematology_smear" -> R.drawable.img_hematology_smear
                                        "img_urinalysis_crystals" -> R.drawable.img_urinalysis_crystals
                                        "img_microbiology_gram" -> R.drawable.img_microbiology_gram
                                        else -> R.drawable.img_app_icon
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { viewModel.setEnlargedImage(q.imageResName) }
                                    ) {
                                        Image(
                                            painter = painterResource(id = resId),
                                            contentDescription = "Diagnostic Specimen",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("معاينة مجهرية", color = Color.White, fontSize = 10.sp)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                Text(
                                    text = q.questionEn,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        textDirection = TextDirection.Content
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                TranslatedSubTextBlock(
                                    translatedText = translatedEntity?.translatedQuestion,
                                    label = if (targetLang == MedicalTranslationService.TARGET_ARABIC) {
                                        "الترجمة العربية للسؤال"
                                    } else {
                                        "English Question Translation"
                                    },
                                    isVisible = showTranslation
                                )

                                TranslationErrorBanner(errorMessage = translationUi?.errorMessage)

                                Spacer(modifier = Modifier.height(10.dp))

                                val options = listOf(q.optionA, q.optionB, q.optionC, q.optionD)
                                val correctOptionText = options.getOrElse(q.correctOptionIndex) { "" }
                                val userOptionText = if (item.selectedOptionIndex in options.indices) {
                                    options[item.selectedOptionIndex]
                                } else {
                                    if (isAr) "لم تتم الإجابة / انتهى الوقت" else "Unanswered / Timed out"
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = if (isAr) "إجابتك: $userOptionText" else "Your Answer: $userOptionText",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                textDirection = TextDirection.Content
                                            ),
                                            color = if (item.isCorrect) ClinicalSuccess else ClinicalError
                                        )
                                        if (!item.isCorrect || q.clinicalExplanationEn.isBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (isAr) "الإجابة الصحيحة: $correctOptionText" else "Correct Answer: $correctOptionText",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textDirection = TextDirection.Content
                                                ),
                                                color = ClinicalSuccess
                                            )
                                        }
                                    }
                                }

                                if (showTranslation && translatedEntity != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val allTranslatedOptions = buildString {
                                        val labels = listOf("A", "B", "C", "D")
                                        labels.forEachIndexed { optIdx, lbl ->
                                            appendLine("$lbl) ${options[optIdx]}")
                                            appendLine("   ↳ ${translatedEntity.optionTranslationAt(optIdx)}")
                                        }
                                    }.trimEnd()
                                    TranslatedSubTextBlock(
                                        translatedText = allTranslatedOptions,
                                        label = if (targetLang == MedicalTranslationService.TARGET_ARABIC) {
                                            "ترجمة الخيارات الأربعة"
                                        } else {
                                            "Translated 4 Options"
                                        },
                                        isVisible = true
                                    )
                                }

                                if (q.clinicalExplanationEn.isNotBlank() || !q.scientificReference.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = ClinicalTeal,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            if (q.clinicalExplanationEn.isNotBlank()) {
                                                Text(
                                                    text = if (isAr) "الشرح العلمي (Scientific Rationale):" else "Scientific Rationale:",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = ClinicalTeal
                                                )
                                                Text(
                                                    text = q.clinicalExplanationEn,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 12.sp,
                                                        lineHeight = 18.sp,
                                                        textDirection = TextDirection.Content
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                TranslatedSubTextBlock(
                                                    translatedText = translatedEntity?.translatedExplanation,
                                                    label = if (targetLang == MedicalTranslationService.TARGET_ARABIC) {
                                                        "ترجمة الشرح العلمي"
                                                    } else {
                                                        "Translated Explanation"
                                                    },
                                                    isVisible = showTranslation
                                                )
                                            }
                                            if (!q.scientificReference.isNullOrBlank()) {
                                                Surface(
                                                    color = MedicalBluePrimary.copy(alpha = 0.08f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = if (isAr) "المرجع العلمي: ${q.scientificReference}" else "Reference: ${q.scientificReference}",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            textDirection = TextDirection.Content
                                                        ),
                                                        color = MedicalBlueDark,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val hasNote = q.id in notedQuestionIds
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TranslateActionButton(
                                        questionId = q.id,
                                        targetLanguage = targetLang,
                                        uiState = translationUi,
                                        onClick = { viewModel.toggleOrTranslateQuestion(q) }
                                    )

                                    TextButton(
                                        onClick = { noteTargetQuestion = q },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("review_note_btn_${q.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EditNote,
                                            contentDescription = null,
                                            tint = if (hasNote) GoldAccent else MedicalBluePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (hasNote) "تعديل ملاحظتك (Edit Note)" else "إضافة ملاحظة للسؤال (Add Note)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    state.enlargedImageResName?.let { imgName ->
        DiagnosticImageZoomDialog(
            imageResName = imgName,
            onDismiss = { viewModel.setEnlargedImage(null) }
        )
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
