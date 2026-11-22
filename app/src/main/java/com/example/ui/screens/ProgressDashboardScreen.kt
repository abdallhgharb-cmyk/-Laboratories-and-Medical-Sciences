package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AcademicDegree
import com.example.data.model.QuestionCategory
import com.example.data.model.UserNoteEntity
import com.example.data.model.UserProgressEntity
import com.example.ui.components.GoogleSignInCard
import com.example.ui.components.NoteEditorDialog
import com.example.ui.components.ReadinessGaugeCard
import com.example.ui.components.TopicAccuracyBarChartCard
import com.example.ui.theme.ClinicalError
import com.example.ui.theme.ClinicalSuccess
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.ClinicalWarning
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.VerifiedGreen
import com.example.ui.viewmodel.AssessmentViewModel
import com.example.ui.viewmodel.DashboardLanguage
import com.example.ui.viewmodel.DashboardStrings
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.TopicMasteryStatus
import com.example.ui.viewmodel.TopicMasteryUi

private enum class TopicFilter { ALL, MASTERED, REVIEW }
private enum class NotesFilter { ALL, QUESTION_LINKED, STANDALONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressDashboardScreen(viewModel: AssessmentViewModel) {
    val context = LocalContext.current
    val lang by viewModel.dashboardLanguage.collectAsState()
    val progressList by viewModel.allProgress.collectAsState()
    val totalCorrect by viewModel.totalCorrectCount.collectAsState()
    val totalAnswered by viewModel.totalAnsweredCount.collectAsState()
    val mistakesCount by viewModel.mistakeCount.collectAsState()
    val bookmarkedQuestions by viewModel.bookmarkedQuestions.collectAsState()
    val applicantProfile by viewModel.applicantProfile.collectAsState()
    val topicStats by viewModel.topicMasteryStats.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val authUiState by viewModel.authUiState.collectAsState()
    val syncUiState by viewModel.syncUiState.collectAsState()

    var topicFilter by remember { mutableStateOf(TopicFilter.ALL) }
    var showAllTopics by remember { mutableStateOf(false) }
    var degreeTabIndex by remember { mutableStateOf(1) } // 0=Diploma, 1=Bachelor, 2=Master
    var notesFilter by remember { mutableStateOf(NotesFilter.ALL) }
    var notesQuery by remember { mutableStateOf("") }
    var editingNote by remember { mutableStateOf<UserNoteEntity?>(null) }
    var showNewNoteDialog by remember { mutableStateOf(false) }

    BackHandler { viewModel.navigateTo(ScreenDestination.Home) }

    val totalScore = progressList.sumOf { it.totalScore }
    val accuracy = if (totalAnswered > 0) ((totalCorrect.toFloat() / totalAnswered) * 100f).toInt().coerceIn(0, 100) else 0
    val masteredTopicsCount = topicStats.count { it.masteryStatus == TopicMasteryStatus.MASTERED }

    val filteredTopics = remember(topicStats, topicFilter) {
        when (topicFilter) {
            TopicFilter.ALL -> topicStats
            TopicFilter.MASTERED -> topicStats.filter { it.masteryStatus == TopicMasteryStatus.MASTERED }
            TopicFilter.REVIEW -> topicStats.filter {
                it.masteryStatus == TopicMasteryStatus.NEEDS_REVIEW ||
                    it.masteryStatus == TopicMasteryStatus.IN_PROGRESS
            }
        }
    }
    val visibleTopics = if (showAllTopics) filteredTopics else filteredTopics.take(6)

    val filteredNotes = remember(allNotes, notesFilter, notesQuery) {
        allNotes.filter { note ->
            val typeOk = when (notesFilter) {
                NotesFilter.ALL -> true
                NotesFilter.QUESTION_LINKED -> note.questionId != null
                NotesFilter.STANDALONE -> note.questionId == null
            }
            val queryOk = notesQuery.isBlank() ||
                note.title.contains(notesQuery, ignoreCase = true) ||
                note.topic.contains(notesQuery, ignoreCase = true) ||
                note.content.contains(notesQuery, ignoreCase = true)
            typeOk && queryOk
        }
    }

    val degrees = AcademicDegree.MED_LAB_DEGREES
    val selectedDegree = degrees[degreeTabIndex.coerceIn(0, degrees.lastIndex)]

    val layoutDir = if (lang == DashboardLanguage.AR) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = DashboardStrings.screenTitle(lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { viewModel.navigateTo(ScreenDestination.Home) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("dashboard_back_btn")
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
                                .testTag("dashboard_language_toggle")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Translate, contentDescription = "Toggle Language", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = DashboardStrings.toggleLabel(lang),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        IconButton(
                            onClick = { showNewNoteDialog = true },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("dashboard_add_note_btn")
                        ) {
                            Icon(Icons.Default.EditNote, contentDescription = DashboardStrings.addNoteLabel(lang), tint = Color.White)
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
                    .testTag("progress_dashboard_screen"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Optional Google Sign-In & Cloud Backup Card
                item {
                    GoogleSignInCard(
                        authState = authUiState,
                        syncState = syncUiState,
                        language = lang,
                        onSignInClick = { viewModel.signInWithGoogle(context) },
                        onSyncNowClick = { viewModel.syncNow() },
                        onSignOutClick = { viewModel.signOut(context) }
                    )
                }

                // 2. Hero Animated Readiness Gauge & Metrics
                item {
                    ReadinessGaugeCard(
                        accuracyPercent = accuracy,
                        totalScore = totalScore,
                        totalAnswered = totalAnswered,
                        totalCorrect = totalCorrect,
                        masteredTopicsCount = masteredTopicsCount,
                        applicantName = applicantProfile?.fullName ?: "Clinical Candidate",
                        language = lang
                    )
                }

                // 3. Topic Accuracy Bar Chart (Canvas)
                item {
                    TopicAccuracyBarChartCard(topics = topicStats, language = lang)
                }

                // 4. Quick Clinical Tools
                item {
                    Text(
                        text = DashboardStrings.quickToolsTitle(lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DashboardActionCard(
                            title = DashboardStrings.licenseLabel(lang),
                            icon = Icons.Default.MilitaryTech,
                            color = GoldAccent,
                            modifier = Modifier.weight(1f).testTag("dash_tool_license"),
                            onClick = { viewModel.navigateTo(ScreenDestination.Certificate) }
                        )
                        DashboardActionCard(
                            title = DashboardStrings.dailyLabel(lang),
                            icon = Icons.Default.Today,
                            color = ClinicalTeal,
                            modifier = Modifier.weight(1f).testTag("dash_tool_daily"),
                            onClick = { viewModel.navigateTo(ScreenDestination.DailyChallenge) }
                        )
                        DashboardActionCard(
                            title = DashboardStrings.mistakesLabel(lang, mistakesCount),
                            icon = Icons.Default.ErrorOutline,
                            color = ClinicalError,
                            modifier = Modifier.weight(1f).testTag("dash_tool_mistakes"),
                            onClick = { viewModel.navigateTo(ScreenDestination.Mistakes) }
                        )
                        DashboardActionCard(
                            title = DashboardStrings.favoritesLabel(lang, bookmarkedQuestions.size),
                            icon = Icons.Default.Bookmark,
                            color = MedicalBluePrimary,
                            modifier = Modifier.weight(1f).testTag("dash_tool_favorites"),
                            onClick = { viewModel.navigateTo(ScreenDestination.Favorites) }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DashboardActionCard(
                            title = if (lang == DashboardLanguage.EN) "Flashcards" else "البطاقات",
                            icon = Icons.Default.Style,
                            color = ClinicalTeal,
                            modifier = Modifier.weight(1f).testTag("dash_tool_flashcards"),
                            onClick = { viewModel.openFlashcards() }
                        )
                        DashboardActionCard(
                            title = if (lang == DashboardLanguage.EN) "Prometric Timed" else "برومترك المؤقت",
                            icon = Icons.Default.Timer,
                            color = MedicalBluePrimary,
                            modifier = Modifier.weight(1f).testTag("dash_tool_prometric"),
                            onClick = { viewModel.openPrometricSetup() }
                        )
                        DashboardActionCard(
                            title = if (lang == DashboardLanguage.EN) "Exam History" else "سجل المراجعة",
                            icon = Icons.Default.History,
                            color = Color(0xFF6D28D9),
                            modifier = Modifier.weight(1f).testTag("dash_tool_history"),
                            onClick = { viewModel.openExamHistory() }
                        )
                    }
                }

                // 5. Topics Mastered & Practice Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = DashboardStrings.topicsMasteryTitle(lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = topicFilter == TopicFilter.ALL,
                                    onClick = { topicFilter = TopicFilter.ALL },
                                    label = { Text(DashboardStrings.filterAll(lang), fontSize = 12.sp) },
                                    modifier = Modifier.testTag("topic_filter_all")
                                )
                            }
                            item {
                                FilterChip(
                                    selected = topicFilter == TopicFilter.MASTERED,
                                    onClick = { topicFilter = TopicFilter.MASTERED },
                                    label = { Text(DashboardStrings.filterMastered(lang), fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = VerifiedGreen,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("topic_filter_mastered")
                                )
                            }
                            item {
                                FilterChip(
                                    selected = topicFilter == TopicFilter.REVIEW,
                                    onClick = { topicFilter = TopicFilter.REVIEW },
                                    label = { Text(DashboardStrings.filterNeedsReview(lang), fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ClinicalWarning,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("topic_filter_review")
                                )
                            }
                        }
                    }
                }

                if (visibleTopics.isEmpty()) {
                    item {
                        EmptyDashboardCard(
                            text = if (lang == DashboardLanguage.EN) {
                                "No topics match this filter yet. Start a stage or practice round to populate mastery progress."
                            } else {
                                "لا توجد مواضيع مطابقة لهذا التصنيف بعد. ابدأ أي مستوى سريري لتتبع الإتقان."
                            }
                        )
                    }
                } else {
                    items(visibleTopics, key = { "${it.specialtyId}_${it.academicDegree}_${it.subCategory}" }) { item ->
                        TopicMasteryCard(
                            item = item,
                            language = lang,
                            onPracticeClick = {
                                viewModel.startTopicPractice(item.specialtyId, item.subCategory, item.academicDegree)
                            }
                        )
                    }
                    if (filteredTopics.size > 6) {
                        item {
                            TextButton(
                                onClick = { showAllTopics = !showAllTopics },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                            ) {
                                Text(
                                    text = if (showAllTopics) {
                                        if (lang == DashboardLanguage.EN) "Show fewer topics" else "عرض عدد أقل"
                                    } else {
                                        if (lang == DashboardLanguage.EN) "Show all ${filteredTopics.size} topics" else "عرض كافة المواضيع (${filteredTopics.size})"
                                    }
                                )
                            }
                        }
                    }
                }

                // 6. Academic Degree Breakdown (Diploma / Bachelor / Master -> X / 10 levels)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = DashboardStrings.degreeBreakdownTitle(lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        TabRow(
                            selectedTabIndex = degreeTabIndex,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MedicalBluePrimary,
                            modifier = Modifier.clip(RoundedCornerShape(12.dp))
                        ) {
                            degrees.forEachIndexed { idx, deg ->
                                Tab(
                                    selected = degreeTabIndex == idx,
                                    onClick = { degreeTabIndex = idx },
                                    text = {
                                        Text(
                                            text = DashboardStrings.degreeLabel(deg, lang),
                                            fontWeight = if (degreeTabIndex == idx) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    },
                                    modifier = Modifier.testTag("degree_tab_$deg")
                                )
                            }
                        }
                    }
                }

                val degreeBreakdownCategories = when (selectedDegree) {
                    AcademicDegree.BACHELOR -> QuestionCategory.ALL_CATEGORIES
                    AcademicDegree.CONSULTANT -> listOf(QuestionCategory.MEDICAL_LABS)
                    else -> QuestionCategory.ALL_CATEGORIES.filterNot { it == QuestionCategory.MED_LAB_LICENSING }
                }

                items(degreeBreakdownCategories, key = { "deg_${selectedDegree}_$it" }) { specId ->
                    val effectiveDegree = if (specId == QuestionCategory.MED_LAB_LICENSING) {
                        AcademicDegree.BACHELOR
                    } else {
                        selectedDegree
                    }
                    val prog = progressList.find {
                        it.specialtyId == specId && it.academicDegree == effectiveDegree
                    } ?: UserProgressEntity(specialtyId = specId, academicDegree = effectiveDegree)

                    val (icon, tint) = when (specId) {
                        QuestionCategory.MEDICAL_LABS -> Icons.Default.Biotech to MedicalBluePrimary
                        QuestionCategory.MED_LAB_LICENSING -> Icons.Default.VerifiedUser to Color(0xFF0D9488)
                        QuestionCategory.NURSING -> Icons.Default.Healing to ClinicalTeal
                        else -> Icons.Default.LocalHospital to Color(0xFFC2185B)
                    }
                    val maxStages = QuestionCategory.maxStagesForSpecialty(specId)

                    DegreeSpecialtyProgressCard(
                        title = DashboardStrings.specialtyName(specId, lang),
                        rank = if (lang == DashboardLanguage.EN) prog.professionalRankEn else prog.professionalRankAr,
                        highestStage = prog.highestUnlockedStage.coerceAtMost(maxStages),
                        completedStages = prog.completedStagesCount.coerceAtMost(maxStages),
                        maxStages = maxStages,
                        totalScore = prog.totalScore,
                        icon = icon,
                        color = tint,
                        language = lang,
                        onClick = {
                            viewModel.navigateTo(ScreenDestination.StageLadder(specId, effectiveDegree))
                        }
                    )
                }

                // 7. Student Study Notes Section (Question-linked + Standalone Notebook)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = DashboardStrings.notesSectionTitle(lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { showNewNoteDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ClinicalTeal),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(40.dp)
                                    .testTag("new_standalone_note_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(DashboardStrings.addNoteLabel(lang), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = notesFilter == NotesFilter.ALL,
                                    onClick = { notesFilter = NotesFilter.ALL },
                                    label = { Text("${DashboardStrings.filterAll(lang)} (${allNotes.size})", fontSize = 12.sp) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = notesFilter == NotesFilter.QUESTION_LINKED,
                                    onClick = { notesFilter = NotesFilter.QUESTION_LINKED },
                                    label = { Text(DashboardStrings.filterQuestionNotes(lang), fontSize = 12.sp) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = notesFilter == NotesFilter.STANDALONE,
                                    onClick = { notesFilter = NotesFilter.STANDALONE },
                                    label = { Text(DashboardStrings.filterStandaloneNotes(lang), fontSize = 12.sp) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = notesQuery,
                            onValueChange = { notesQuery = it },
                            placeholder = { Text(DashboardStrings.searchNotesHint(lang), fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("notes_search_input")
                        )
                    }
                }

                if (filteredNotes.isEmpty()) {
                    item {
                        EmptyDashboardCard(text = DashboardStrings.emptyNotesTip(lang))
                    }
                } else {
                    items(filteredNotes, key = { "note_${it.id}" }) { note ->
                        StudentNoteCard(
                            note = note,
                            language = lang,
                            onEdit = { editingNote = note },
                            onDelete = { viewModel.deleteNote(note.id) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    if (showNewNoteDialog || editingNote != null) {
        NoteEditorDialog(
            existingNote = editingNote,
            linkedQuestionId = editingNote?.questionId,
            defaultSpecialtyId = editingNote?.specialtyId ?: "GENERAL",
            defaultTopic = editingNote?.topic ?: "Clinical Review",
            defaultTitle = editingNote?.title ?: "",
            language = lang,
            onDismiss = {
                showNewNoteDialog = false
                editingNote = null
            },
            onSave = { id, qId, specId, topic, title, content ->
                viewModel.saveNote(id, qId, specId, topic, title, content)
            }
        )
    }
}

@Composable
private fun DashboardActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TopicMasteryCard(
    item: TopicMasteryUi,
    language: DashboardLanguage,
    onPracticeClick: () -> Unit
) {
    val badgeColor = when (item.masteryStatus) {
        TopicMasteryStatus.MASTERED -> ClinicalSuccess
        TopicMasteryStatus.IN_PROGRESS -> ClinicalTeal
        TopicMasteryStatus.NEEDS_REVIEW -> ClinicalError
        TopicMasteryStatus.UNSTARTED -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("topic_mastery_${item.subCategory}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.subCategory,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${DashboardStrings.specialtyName(item.specialtyId, language)} • ${DashboardStrings.degreeLabel(item.academicDegree, language)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = badgeColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${DashboardStrings.statusLabel(item.masteryStatus, language)} (${item.accuracyPercent}%)",
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (item.accuracyPercent / 100f).coerceIn(0.03f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = badgeColor,
                trackColor = badgeColor.copy(alpha = 0.15f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == DashboardLanguage.EN) {
                        "${item.correctCount}/${item.answeredCount} correct • ${item.totalQuestions} Qs bank"
                    } else {
                        "${item.correctCount}/${item.answeredCount} صحيح • بنك ${item.totalQuestions} سؤال"
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextButton(
                    onClick = onPracticeClick,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(DashboardStrings.practiceTopicBtn(language), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun DegreeSpecialtyProgressCard(
    title: String,
    rank: String,
    highestStage: Int,
    completedStages: Int,
    maxStages: Int,
    totalScore: Int,
    icon: ImageVector,
    color: Color,
    language: DashboardLanguage,
    onClick: () -> Unit
) {
    val safeMaxStages = maxStages.coerceAtLeast(1)
    val clampedHighest = highestStage.coerceIn(1, safeMaxStages)
    val clampedCompleted = completedStages.coerceIn(0, safeMaxStages)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "$rank • $totalScore pts", color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    text = if (language == DashboardLanguage.EN) {
                        "Stage $clampedHighest / $safeMaxStages"
                    } else {
                        "المستوى $clampedHighest / $safeMaxStages ($clampedCompleted مكتمل)"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { (clampedHighest.toFloat() / safeMaxStages.toFloat()).coerceIn(0.05f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = color,
                trackColor = color.copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
private fun StudentNoteCard(
    note: UserNoteEntity,
    language: DashboardLanguage,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_note_card_${note.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = if (note.questionId != null) ClinicalTeal.copy(alpha = 0.14f) else MedicalBluePrimary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (note.questionId != null) "Q #${note.questionId}" else DashboardStrings.filterStandaloneNotes(language),
                            color = if (note.questionId != null) ClinicalTeal else MedicalBluePrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = note.topic,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("edit_note_btn_${note.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Note", tint = MedicalBluePrimary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("delete_note_btn_${note.id}")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Note", tint = ClinicalError, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Text(
                text = note.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = note.content,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyDashboardCard(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp,
            modifier = Modifier.padding(14.dp)
        )
    }
}
