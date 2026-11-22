package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionCategory
import com.example.data.model.QuestionEntity
import com.example.data.model.QuestionTranslationEntity
import com.example.ui.components.NoteEditorDialog
import com.example.ui.theme.ClinicalSuccess
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.viewmodel.AssessmentViewModel
import com.example.ui.viewmodel.ScreenDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchQuestionsScreen(viewModel: AssessmentViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchSpecialtyFilter by viewModel.searchSpecialtyFilter.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchTranslations by viewModel.searchTranslations.collectAsState()
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
                        text = "البحث الثنائي اللغة في بنك الأسئلة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.Home) },
                        modifier = Modifier.testTag("search_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                .testTag("search_questions_screen")
        ) {
            // Search Input Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("ابحث بالإنجليزية أو العربية (السؤال، الخيارات، الموضوع، الرقم)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input_field")
                )
            }

            // Department Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = searchSpecialtyFilter == null,
                        onClick = { viewModel.setSearchFilters(null, null) },
                        label = { Text("الكل", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MedicalBluePrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("search_filter_all")
                    )
                }
                items(QuestionCategory.REGISTERED_DEPARTMENTS) { dept ->
                    val isSelected = searchSpecialtyFilter == dept.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSearchFilters(dept.id, null) },
                        label = { Text(dept.titleAr, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ClinicalTeal,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("search_filter_${dept.id}")
                    )
                }
            }

            // Results Counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "نتائج البحث: ${searchResults.size} سؤال",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "EN / AR • مع الأدلة التفسيرية المعتمدة",
                    fontSize = 11.sp,
                    color = ClinicalTeal
                )
            }

            // Results List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(searchResults, key = { it.id }) { question ->
                    val isBookmarked = question.id in bookmarkedIds
                    val hasNote = question.id in notedQuestionIds
                    QuestionSearchItemCard(
                        question = question,
                        isBookmarked = isBookmarked,
                        onToggleBookmark = { viewModel.toggleBookmark(question.id) },
                        hasNote = hasNote,
                        onNoteClick = { noteTargetQuestion = question },
                        cachedTranslation = searchTranslations[question.id]
                    )
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

@Composable
fun QuestionSearchItemCard(
    question: QuestionEntity,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    hasNote: Boolean = false,
    onNoteClick: (() -> Unit)? = null,
    cachedTranslation: QuestionTranslationEntity? = null
) {
    var isExplanationExpanded by remember { mutableStateOf(false) }
    var isTranslationExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .testTag("question_item_${question.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Category, Stage, ID, Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = MedicalBluePrimary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "السؤال #${question.id} • المرحلة ${question.stageLevel}",
                            color = MedicalBluePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = ClinicalTeal.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = question.subCategory,
                            color = ClinicalTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (cachedTranslation != null) {
                        IconButton(
                            onClick = { isTranslationExpanded = !isTranslationExpanded },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Toggle Cached Translation",
                                tint = if (isTranslationExpanded) MedicalBluePrimary else ClinicalTeal
                            )
                        }
                    }

                    if (onNoteClick != null) {
                        IconButton(
                            onClick = onNoteClick,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = "Question Note",
                                tint = if (hasNote) GoldAccent else ClinicalTeal
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) GoldAccent else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Question Text
            Text(
                text = question.questionEn,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp,
                    textDirection = TextDirection.Content
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            AnimatedVisibility(visible = isTranslationExpanded && cachedTranslation != null) {
                cachedTranslation?.let { tr ->
                    Surface(
                        color = ClinicalTeal.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = tr.translatedQuestion,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    textDirection = TextDirection.Rtl
                                ),
                                color = MedicalBluePrimary
                            )
                            if (tr.translatedExplanation.isNotBlank()) {
                                Text(
                                    text = tr.translatedExplanation,
                                    style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Rtl),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Options summary
            val options = listOf(
                Pair("A", question.optionA),
                Pair("B", question.optionB),
                Pair("C", question.optionC),
                Pair("D", question.optionD)
            )

            options.forEachIndexed { idx, pair ->
                val isCorrect = idx == question.correctOptionIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${pair.first})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isCorrect) ClinicalSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = pair.second,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                            textDirection = TextDirection.Content
                        ),
                        color = if (isCorrect) ClinicalSuccess else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (question.clinicalExplanationEn.isNotBlank() || !question.scientificReference.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                // Medical Explanation / Reference Toggle Button
                Surface(
                    color = ClinicalTeal.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExplanationExpanded = !isExplanationExpanded }
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = ClinicalTeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isExplanationExpanded) {
                                "إخفاء التفسير والمرجع العلمي"
                            } else if (question.clinicalExplanationEn.isNotBlank()) {
                                "عرض التفسير والمرجع العلمي (Explanation & Ref)"
                            } else {
                                "عرض المرجع العلمي المعتمد (Scientific Reference)"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ClinicalTeal
                        )
                    }
                }

                AnimatedVisibility(visible = isExplanationExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (question.clinicalExplanationEn.isNotBlank()) {
                            Text(
                                text = "التفسير العلمي السريري (Scientific Board Rationale):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MedicalBluePrimary
                            )
                            Text(
                                text = question.clinicalExplanationEn,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    textDirection = TextDirection.Content
                                ),
                                color = Color(0xFF1E293B)
                            )
                        }
                        if (!question.scientificReference.isNullOrBlank()) {
                            Text(
                                text = "المرجع العلمي: ${question.scientificReference}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textDirection = TextDirection.Content
                                ),
                                color = MedicalBluePrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
