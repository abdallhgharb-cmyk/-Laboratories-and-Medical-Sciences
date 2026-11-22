package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.R
import com.example.data.model.AcademicDegree
import com.example.data.model.QuestionCategory
import com.example.data.model.QuestionEntity
import com.example.data.remote.MedicalTranslationService
import com.example.ui.components.DiagnosticImageZoomDialog
import com.example.ui.components.NoteEditorDialog
import com.example.ui.components.SubtleQuestionTransition
import com.example.ui.components.TranslateActionButton
import com.example.ui.components.TranslatedSubTextBlock
import com.example.ui.components.TranslationErrorBanner
import com.example.ui.components.optionTranslationAt
import com.example.ui.theme.ClinicalSuccess
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MedicalBlueDark
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.VerifiedGreen
import com.example.ui.viewmodel.AssessmentViewModel
import com.example.ui.viewmodel.DashboardLanguage
import com.example.ui.viewmodel.DashboardStrings
import com.example.ui.viewmodel.QuestionTranslationUiState
import com.example.ui.viewmodel.ScreenDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(viewModel: AssessmentViewModel) {
    val state by viewModel.flashcardState.collectAsState()
    val lang by viewModel.dashboardLanguage.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    val notedQuestionIds by viewModel.notedQuestionIds.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val translationsMap by viewModel.translationsState.collectAsState()

    var noteTargetQuestion by remember { mutableStateOf<QuestionEntity?>(null) }
    var zoomedImage by remember { mutableStateOf<String?>(null) }

    BackHandler { viewModel.navigateTo(ScreenDestination.Home) }

    val isAr = lang == DashboardLanguage.AR
    val layoutDir = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr
    val degrees = AcademicDegree.degreesForSpecialty(state.specialtyId)
    val selectedDegIdx = degrees.indexOf(state.academicDegree).coerceAtLeast(0)

    CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isAr) "البطاقات التعليمية الذكية" else "Clinical Flashcards",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isAr) "مراجعة سريعة بدون تكرار" else "Zero-Duplication Concept Deck",
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
                                .testTag("flashcard_back_btn")
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
                                .testTag("flashcard_lang_toggle")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .testTag("flashcard_screen"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Specialty Selector Row
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(QuestionCategory.ALL_CATEGORIES) { specId ->
                        val selected = state.specialtyId == specId
                        FilterChip(
                            selected = selected,
                            onClick = {
                                viewModel.updateFlashcardFilter(
                                    specialtyId = specId,
                                    resetTopicIfTrackChanged = true
                                )
                            },
                            label = {
                                Text(
                                    text = DashboardStrings.specialtyName(specId, lang),
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MedicalBluePrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("flashcard_spec_$specId")
                        )
                    }
                }

                // Degree Tabs (Hidden for unified 530-Q Licensing Bank)
                if (state.specialtyId != QuestionCategory.MED_LAB_LICENSING) {
                    TabRow(
                        selectedTabIndex = selectedDegIdx,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MedicalBluePrimary,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        degrees.forEachIndexed { idx, deg ->
                            Tab(
                                selected = selectedDegIdx == idx,
                                onClick = {
                                    viewModel.updateFlashcardFilter(
                                        academicDegree = deg,
                                        resetTopicIfTrackChanged = true
                                    )
                                },
                                text = {
                                    Text(
                                        text = DashboardStrings.degreeLabel(deg, lang),
                                        fontWeight = if (selectedDegIdx == idx) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = if (degrees.size >= 4) 11.sp else 12.sp,
                                        maxLines = 1
                                    )
                                },
                                modifier = Modifier.testTag("flashcard_degree_$deg")
                            )
                        }
                    }
                } else {
                    Surface(
                        color = MedicalBluePrimary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isAr) {
                                "بنك مزاولة المهنة الشامل (530 بطاقة تعليمية مصنفة في 9 تخصصات)"
                            } else {
                                "Unified Medical Lab Licensing Deck (530 Cards across 9 Disciplines)"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MedicalBlueDark,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                // Topic Subcategory Filter Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = state.selectedTopic == null,
                            onClick = { viewModel.updateFlashcardFilter(topic = null) },
                            label = { Text(if (isAr) "جميع المواضيع" else "All Topics", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ClinicalTeal,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("flashcard_topic_all")
                        )
                    }
                    items(state.availableTopics) { topic ->
                        FilterChip(
                            selected = state.selectedTopic == topic,
                            onClick = { viewModel.updateFlashcardFilter(topic = topic) },
                            label = { Text(topic, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ClinicalTeal,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("flashcard_topic_$topic")
                        )
                    }
                }

                // Zero-Duplication Deck Progress Header
                val reviewedCount = (state.totalInScope - state.unseenRemaining).coerceAtLeast(0)
                val progressFraction = if (state.totalInScope > 0) {
                    (reviewedCount.toFloat() / state.totalInScope.toFloat()).coerceIn(0f, 1f)
                } else 0f

                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            Column {
                                Text(
                                    text = if (isAr) {
                                        "متبقي بدون تكرار: ${state.unseenRemaining} من ${state.totalInScope} بطاقة"
                                    } else {
                                        "Unseen Remaining: ${state.unseenRemaining} / ${state.totalInScope} Cards"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MedicalBlueDark
                                )
                                Text(
                                    text = if (isAr) "البطاقات المتقنة لا تتكرر حتى إعادة التعيين" else "Reviewed cards never repeat until deck reset",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(
                                onClick = { viewModel.resetFlashcardDeck() },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("flashcard_reset_deck_btn")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isAr) "إعادة" else "Reset", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(CircleShape),
                            color = ClinicalTeal,
                            trackColor = ClinicalTeal.copy(alpha = 0.15f)
                        )
                    }
                }

                // Active Card Area
                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    state.currentCard == null -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("flashcard_deck_complete_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(ClinicalSuccess.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ClinicalSuccess, modifier = Modifier.size(36.dp))
                                }
                                Text(
                                    text = if (isAr) "أتممت جميع بطاقات هذا القسم بدون تكرار!" else "All Cards Reviewed in This Deck!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = if (isAr) {
                                        "يمكنك اختيار موضوع آخر أو إعادة تعيين هذه المجموعة لمراجعتها مجدداً."
                                    } else {
                                        "Select another topic or reset this deck to study the cards again."
                                    },
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { viewModel.resetFlashcardDeck() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("flashcard_empty_reset_btn")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (isAr) "إعادة تعيين المجموعة" else "Reset Deck", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    else -> {
                        val card = state.currentCard!!

                        SubtleQuestionTransition(
                            targetKey = card,
                            direction = 1
                        ) { animatedCard ->
                            val isBookmarked = animatedCard.id in bookmarkedIds
                            val hasNote = animatedCard.id in notedQuestionIds

                            FlipFlashcardSurface(
                                question = animatedCard,
                                isFlipped = state.isFlipped,
                                isBookmarked = isBookmarked,
                                hasNote = hasNote,
                                isAr = isAr,
                                translationUi = translationsMap[animatedCard.id],
                                onFlip = { viewModel.flipFlashcard() },
                                onToggleTranslate = { viewModel.toggleOrTranslateQuestion(animatedCard) },
                                onToggleBookmark = { viewModel.toggleBookmark(animatedCard.id) },
                                onOpenNote = { noteTargetQuestion = animatedCard },
                                onZoomImage = { resName -> zoomedImage = resName }
                            )
                        }

                        // Bottom Deck Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.skipFlashcardToLater() },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("flashcard_skip_btn")
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isAr) "تخطي للاحقاً" else "Review Later", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = { viewModel.markFlashcardMasteredAndNext() },
                                colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1.25f)
                                    .height(50.dp)
                                    .testTag("flashcard_mastered_next_btn")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAr) "أتقنتها • التالي" else "Got It • Next",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
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
            defaultTitle = existingLinkedNote?.title ?: "Flashcard #${q.id}: ${q.subCategory}",
            language = lang,
            onDismiss = { noteTargetQuestion = null },
            onSave = { id, qId, spec, topic, title, content ->
                viewModel.saveNote(id, qId, spec, topic, title, content)
            }
        )
    }

    zoomedImage?.let { imgName ->
        DiagnosticImageZoomDialog(imageResName = imgName, onDismiss = { zoomedImage = null })
    }
}

@Composable
private fun FlipFlashcardSurface(
    question: QuestionEntity,
    isFlipped: Boolean,
    isBookmarked: Boolean,
    hasNote: Boolean,
    isAr: Boolean,
    translationUi: QuestionTranslationUiState?,
    onFlip: () -> Unit,
    onToggleTranslate: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenNote: () -> Unit,
    onZoomImage: (String) -> Unit
) {
    val showTranslation = translationUi?.isExpanded == true && translationUi.translation != null
    val translatedEntity = translationUi?.translation
    val targetLang = MedicalTranslationService.detectTargetLanguage(question)
    val scalePulse by animateFloatAsState(
        targetValue = if (isFlipped) 1f else 0.995f,
        animationSpec = tween(220),
        label = "flashcard_pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 310.dp)
            .graphicsLayer {
                scaleX = scalePulse
                scaleY = scalePulse
            }
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onFlip)
            .testTag("flashcard_flip_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFlipped) Color(0xFFF0FDF9) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isFlipped) {
                        Brush.verticalGradient(listOf(Color(0xFFE6FAF5), Color.White))
                    } else {
                        Brush.verticalGradient(listOf(MedicalBluePrimary.copy(alpha = 0.05f), Color.White))
                    }
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isFlipped) VerifiedGreen.copy(alpha = 0.14f) else MedicalBluePrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Style,
                            contentDescription = null,
                            tint = if (isFlipped) VerifiedGreen else MedicalBluePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${question.subCategory} • #${question.id}",
                            color = if (isFlipped) VerifiedGreen else MedicalBluePrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenNote,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("flashcard_note_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Add Note",
                            tint = if (hasNote) GoldAccent else ClinicalTeal
                        )
                    }
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("flashcard_bookmark_btn")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) GoldAccent else Color.Gray
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TranslateActionButton(
                    questionId = question.id,
                    targetLanguage = targetLang,
                    uiState = translationUi,
                    onClick = onToggleTranslate
                )
            }

            TranslationErrorBanner(errorMessage = translationUi?.errorMessage)

            AnimatedContent(
                targetState = isFlipped,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) },
                label = "flashcard_side_transition"
            ) { showAnswer ->
                if (!showAnswer) {
                    // FRONT: Question & Concept Prompt
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = if (isAr) "السؤال / المفهوم السريري (QUESTION)" else "CONCEPT / CLINICAL QUESTION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ClinicalTeal
                        )

                        if (question.imageResName != null) {
                            val resId = when (question.imageResName) {
                                "img_hematology_smear" -> R.drawable.img_hematology_smear
                                "img_urinalysis_crystals" -> R.drawable.img_urinalysis_crystals
                                "img_microbiology_gram" -> R.drawable.img_microbiology_gram
                                else -> R.drawable.img_app_icon
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onZoomImage(question.imageResName) }
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
                            text = question.questionEn,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 24.sp,
                                textDirection = TextDirection.Content
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        TranslatedSubTextBlock(
                            translatedText = translatedEntity?.translatedQuestion,
                            label = if (targetLang == MedicalTranslationService.TARGET_ARABIC) {
                                "الترجمة العربية للسؤال"
                            } else {
                                "English Translation"
                            },
                            isVisible = showTranslation
                        )

                        if (question.clinicalHintEn.isNotBlank()) {
                            Surface(
                                color = GoldAccent.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Hint: ${question.clinicalHintEn}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            textDirection = TextDirection.Content
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // BACK: Verified Answer + Explanation + Reference
                    val options = listOf(question.optionA, question.optionB, question.optionC, question.optionD)
                    val correctAnswer = options.getOrElse(question.correctOptionIndex) { question.optionA }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = if (isAr) "الإجابة الصحيحة والدليل العلمي (ANSWER)" else "VERIFIED ANSWER & RATIONALE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = VerifiedGreen
                        )

                        Surface(
                            color = ClinicalSuccess.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, ClinicalSuccess, RoundedCornerShape(14.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = correctAnswer,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        textDirection = TextDirection.Content
                                    ),
                                    color = Color(0xFF065F46)
                                )
                                TranslatedSubTextBlock(
                                    translatedText = translatedEntity?.optionTranslationAt(question.correctOptionIndex),
                                    label = if (targetLang == MedicalTranslationService.TARGET_ARABIC) {
                                        "ترجمة الإجابة الصحيحة"
                                    } else {
                                        "Translated Correct Answer"
                                    },
                                    isVisible = showTranslation,
                                    accentColor = VerifiedGreen
                                )
                            }
                        }

                        if (question.clinicalExplanationEn.isNotBlank()) {
                            Text(
                                text = if (isAr) "الشرح العلمي:" else "Clinical Explanation:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MedicalBlueDark
                            )

                            Text(
                                text = question.clinicalExplanationEn,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
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

                        if (!question.scientificReference.isNullOrBlank()) {
                            Surface(
                                color = MedicalBluePrimary.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isAr) {
                                        "المرجع العلمي: ${question.scientificReference}"
                                    } else {
                                        "Reference: ${question.scientificReference}"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textDirection = TextDirection.Content
                                    ),
                                    color = MedicalBluePrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Flip prompt pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Icon(Icons.Default.Flip, contentDescription = null, tint = MedicalBluePrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isFlipped) {
                                if (isAr) "اضغط للعودة للسؤال" else "Tap card to view Question"
                            } else {
                                if (isAr) "اضغط لقلب البطاقة وإظهار الإجابة" else "Tap card to reveal Answer"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MedicalBluePrimary
                        )
                    }
                }
            }
        }
    }
}
