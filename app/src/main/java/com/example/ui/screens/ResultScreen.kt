package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ClinicalError
import com.example.ui.theme.ClinicalSuccess
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.ClinicalWarning
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MedicalBlueDark
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.VerifiedGreen
import com.example.data.model.StageGradingPolicy
import com.example.ui.viewmodel.AssessmentViewModel
import com.example.ui.viewmodel.ScreenDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    specialtyId: String,
    stageLevel: Int,
    isPassed: Boolean,
    academicDegree: String = com.example.data.model.AcademicDegree.BACHELOR,
    isPrometricTimed: Boolean = false,
    viewModel: AssessmentViewModel
) {
    val state by viewModel.sessionState.collectAsState()
    val prometricSession = isPrometricTimed || state.isPrometricTimed

    BackHandler {
        val backTarget = when {
            prometricSession -> ScreenDestination.PrometricSetup
            state.isDailyChallenge -> ScreenDestination.DailyChallenge
            state.isTopicPractice -> ScreenDestination.Dashboard
            else -> ScreenDestination.StageLadder(specialtyId, academicDegree)
        }
        viewModel.navigateTo(backTarget)
    }

    val correctCount = state.answersHistory.count { it.isCorrect }
    val totalCount = state.answersHistory.size
    val accuracyPercent = StageGradingPolicy.calculateAccuracyPercent(correctCount, totalCount)
    val earnedStars = StageGradingPolicy.starsForAccuracy(accuracyPercent)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "تقرير الكفاءة السريرية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MedicalBluePrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .testTag("result_screen"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Big Outcome Icon
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPassed) ClinicalSuccess.copy(alpha = 0.15f) else ClinicalError.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.SentimentDissatisfied,
                    contentDescription = null,
                    tint = if (isPassed) ClinicalSuccess else ClinicalError,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Earned Stars Indicator (2 stars at >= 70%, 3 stars at >= 90%)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.testTag("result_stars_row")
            ) {
                repeat(3) { idx ->
                    Icon(
                        imageVector = if (idx < earnedStars) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = if (idx < earnedStars) GoldAccent else Color.Gray.copy(alpha = 0.45f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isPassed) "تم اجتياز المستوى السريري بنجاح" else "يحتاج إلى تدريب ومراجعة إضافية (< 70%)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isPassed) ClinicalSuccess else ClinicalError,
                textAlign = TextAlign.Center
            )

            Text(
                text = "${StageGradingPolicy.gradeLabelAr(accuracyPercent)} • ${StageGradingPolicy.gradeLabelEn(accuracyPercent)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (prometricSession) {
                            "ملخص محاكاة اختبار برومترك المؤقت (Prometric):"
                        } else {
                            "ملخص الأداء في المستوى $stageLevel (الحد المعتمد 70%):"
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(
                            label = "الدقة السريرية",
                            value = "$accuracyPercent%",
                            color = if (isPassed || accuracyPercent >= StageGradingPolicy.PASS_THRESHOLD_PERCENT) ClinicalSuccess else ClinicalWarning
                        )
                        StatItem(
                            label = "الإجابات الصحيحة",
                            value = "$correctCount / $totalCount",
                            color = MedicalBluePrimary
                        )
                        StatItem(
                            label = "النجوم / النقاط",
                            value = "${earnedStars}★ • +${state.scoreEarned}",
                            color = GoldAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Board Certification Note
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalBlueDark)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "يعتمد هذا التقييم خوارزمية استبعاد التكرار الحازمة (Zero-Duplication) لضمان تغطية متكاملة لجميع معايير امتحانات البورد والمزاولة الدولية.",
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = { viewModel.openCurrentRoundReview(specialtyId, stageLevel, academicDegree) },
                colors = ButtonDefaults.buttonColors(containerColor = ClinicalTeal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("review_answers_button")
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "مراجعة الأدلة التفسيرية للأسئلة (Review Explanations)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isPassed && !state.isDailyChallenge && !state.isTopicPractice && !prometricSession &&
                stageLevel < com.example.data.model.QuestionCategory.maxStagesForSpecialty(specialtyId)
            ) {
                Button(
                    onClick = { viewModel.startAssessment(specialtyId, academicDegree, stageLevel + 1) },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("next_stage_button")
                ) {
                    Text(
                        text = "الانتقال للمستوى السريري التالي (${stageLevel + 1})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedButton(
                onClick = {
                    when {
                        prometricSession -> viewModel.openPrometricSetup()
                        state.isDailyChallenge -> viewModel.startDailyChallenge()
                        state.isTopicPractice -> viewModel.startTopicPractice(
                            specialtyId = specialtyId,
                            subCategory = state.activeTopicTitle ?: "",
                            academicDegree = academicDegree
                        )
                        else -> viewModel.startAssessment(specialtyId, academicDegree, stageLevel)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("retry_stage_button")
            ) {
                Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (prometricSession) "إعداد اختبار برومترك جديد" else "إعادة التقييم والممارسة لهذا المستوى",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { viewModel.openExamHistory() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("exam_history_button")
            ) {
                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("سجل الاختبارات السابقة (Exam History)", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { viewModel.navigateTo(ScreenDestination.Home) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("home_button")
            ) {
                Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("العودة للرئيسية", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
