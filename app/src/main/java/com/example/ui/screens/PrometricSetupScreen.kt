package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AcademicDegree
import com.example.data.model.QuestionCategory
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MedicalBlueDark
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.VerifiedGreen
import com.example.ui.viewmodel.AssessmentViewModel
import com.example.ui.viewmodel.DashboardLanguage
import com.example.ui.viewmodel.DashboardStrings
import com.example.ui.viewmodel.PrometricDurationOption
import com.example.ui.viewmodel.ScreenDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrometricSetupScreen(viewModel: AssessmentViewModel) {
    val state by viewModel.prometricState.collectAsState()
    val lang by viewModel.dashboardLanguage.collectAsState()
    val isAr = lang == DashboardLanguage.AR

    BackHandler { viewModel.navigateTo(ScreenDestination.Home) }

    val degrees = AcademicDegree.degreesForSpecialty(state.specialtyId)
    val selectedDegIdx = degrees.indexOf(state.academicDegree).coerceAtLeast(0)
    val layoutDir = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isAr) "محاكاة اختبار برومترك المؤقت" else "Prometric Timed Exam Simulator",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isAr) "ظروف الاختبار الحقيقية بدون تكرار" else "Authentic Timed Conditions • Zero Duplication",
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
                                .testTag("prometric_setup_back_btn")
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
                                .testTag("prometric_setup_lang_toggle")
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
                    .testTag("prometric_setup_screen"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Rules Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MedicalBlueDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAr) "قواعد محاكاة برومترك الرسمية" else "Prometric Exam Rules",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Text(
                            text = if (isAr) {
                                "• عداد زمني تنازلي مستمر (30، 60، أو 120 دقيقة).\n• يتم حجب التصحيح والشرح حتى التسليم النهائي للاختبار.\n• يمكنك التنقل بحرية بين الأسئلة وتمييز الأسئلة للمراجعة (Flag).\n• سحب حصري للأسئلة غير المجابة مسبقاً لمنع التكرار تماماً."
                            } else {
                                "• Continuous global countdown timer (30, 60, or 120 minutes).\n• Answers and rationales remain hidden until final submission.\n• Navigate freely between items and Flag questions for review.\n• Strict Zero-Duplication engine pulls fresh unseen questions first."
                            },
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp,
                            lineHeight = 19.sp
                        )
                        Surface(
                            color = VerifiedGreen.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isAr) {
                                    "الرصيد غير المكرر المتاح في هذا المسار: ${state.unseenAvailableCount} سؤال"
                                } else {
                                    "Unseen Non-Repeated Pool in Track: ${state.unseenAvailableCount} Questions"
                                },
                                color = Color(0xFFA7F3D0),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Specialty Selector
                Text(
                    text = if (isAr) "1. اختر التخصص الطبي:" else "1. Select Clinical Specialty",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuestionCategory.ALL_CATEGORIES.forEach { specId ->
                        val selected = state.specialtyId == specId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { viewModel.updatePrometricSetupConfig(specialtyId = specId) }
                                .testTag("prometric_spec_$specId"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) MedicalBluePrimary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) MedicalBluePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = DashboardStrings.specialtyName(specId, lang),
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = if (selected) MedicalBluePrimary else MaterialTheme.colorScheme.onSurface
                                )
                                if (selected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MedicalBluePrimary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                // Degree Tabs (Hidden for unified 530-Q Licensing Bank)
                if (state.specialtyId != QuestionCategory.MED_LAB_LICENSING) {
                    Text(
                        text = if (isAr) "2. الدرجة العلمية:" else "2. Academic Degree",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    TabRow(
                        selectedTabIndex = selectedDegIdx,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MedicalBluePrimary,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        degrees.forEachIndexed { idx, deg ->
                            Tab(
                                selected = selectedDegIdx == idx,
                                onClick = { viewModel.updatePrometricSetupConfig(academicDegree = deg) },
                                text = {
                                    Text(
                                        text = DashboardStrings.degreeLabel(deg, lang),
                                        fontWeight = if (selectedDegIdx == idx) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = if (degrees.size >= 4) 11.sp else 12.sp,
                                        maxLines = 1
                                    )
                                },
                                modifier = Modifier.testTag("prometric_degree_$deg")
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
                                "2. بنك مزاولة المهنة الموحد للمختبرات الطبية (530 سؤالاً عبر جميع التخصصات الـ 9)"
                            } else {
                                "2. Unified Medical Lab Licensing Pool (530 Questions across 9 Lab Disciplines)"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MedicalBlueDark,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }
                }

                // Duration Presets: 30 / 60 / 120 min
                Text(
                    text = if (isAr) "3. مدة الجلسة وعدد الأسئلة:" else "3. Session Duration & Question Load",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                PrometricDurationOption.entries.forEach { option ->
                    val selected = state.selectedDuration == option
                    val title = when (option) {
                        PrometricDurationOption.MIN_30 ->
                            if (isAr) "جلسة سريعة — 30 دقيقة (30 سؤالاً)" else "Sprint Session — 30 Minutes (30 Questions)"
                        PrometricDurationOption.MIN_60 ->
                            if (isAr) "جلسة قياسية — 60 دقيقة (60 سؤالاً)" else "Standard Session — 60 Minutes (60 Questions)"
                        PrometricDurationOption.MIN_120 ->
                            if (isAr) "محاكاة برومترك الكاملة — 120 دقيقة (100 سؤال)" else "Full Prometric Simulation — 120 Minutes (100 Questions)"
                    }
                    val subtitle = when (option) {
                        PrometricDurationOption.MIN_30 ->
                            if (isAr) "مثالية للمراجعة المكثفة السريعة (1 دقيقة/سؤال)" else "Focused high-yield block (1 min per question)"
                        PrometricDurationOption.MIN_60 ->
                            if (isAr) "توازن احترافي لاختبار السرعة والدقة" else "Balanced pacing drill for exam stamina"
                        PrometricDurationOption.MIN_120 ->
                            if (isAr) "اختبار شامل يحاكي اختبار الهيئة والبرومترك الحقيقي" else "Full licensing board endurance simulation"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.updatePrometricSetupConfig(duration = option) }
                            .testTag("prometric_duration_${option.minutes}"),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) ClinicalTeal else MaterialTheme.colorScheme.outlineVariant
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) ClinicalTeal.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (selected) ClinicalTeal else Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (selected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ClinicalTeal, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = { viewModel.startPrometricExam() },
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_prometric_exam_btn")
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAr) {
                                "بدء الاختبار (${state.selectedDuration.minutes} دقيقة / ${state.selectedDuration.questionCount} سؤال)"
                            } else {
                                "Start Timed Exam (${state.selectedDuration.minutes} min / ${state.selectedDuration.questionCount} Qs)"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
