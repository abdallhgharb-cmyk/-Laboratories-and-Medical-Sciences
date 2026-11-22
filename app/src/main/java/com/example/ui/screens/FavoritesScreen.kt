package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.viewmodel.AssessmentViewModel
import com.example.ui.viewmodel.ScreenDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(viewModel: AssessmentViewModel) {
    val bookmarkedQuestions by viewModel.bookmarkedQuestions.collectAsState()
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
                        text = "الأسئلة المفضلة والمحفوظة (${bookmarkedQuestions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.Home) },
                        modifier = Modifier.testTag("favorites_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MedicalBluePrimary)
            )
        }
    ) { innerPadding ->
        if (bookmarkedQuestions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "قائمة المفضلة فارغة حالياً",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "يمكنك حفظ أي سؤال سريري أثناء التقييم أو من خلال شاشة البحث للرجوع إليه ومراجعته مع التفسير الطبي في أي وقت.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.navigateTo(ScreenDestination.Search) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("البحث في بنك الأسئلة لإضافة مفضلة")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("favorites_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(bookmarkedQuestions) { question ->
                    QuestionSearchItemCard(
                        question = question,
                        isBookmarked = true,
                        onToggleBookmark = { viewModel.toggleBookmark(question.id) },
                        hasNote = question.id in notedQuestionIds,
                        onNoteClick = { noteTargetQuestion = question }
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
