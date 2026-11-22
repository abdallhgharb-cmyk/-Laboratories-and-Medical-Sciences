package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserProgressEntity
import com.example.ui.components.ConsultantProfileDialog
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MedicalBlueDark
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.VerifiedGreen
import com.example.ui.viewmodel.AssessmentViewModel
import com.example.ui.viewmodel.ScreenDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: AssessmentViewModel) {
    val context = LocalContext.current
    val progressList by viewModel.allProgress.collectAsState()
    val selectedDegree by viewModel.selectedDegree.collectAsState()
    val isMuted by viewModel.audioEngine.isMuted.collectAsState()
    val showSupervisorProfile by viewModel.showSupervisorProfile.collectAsState()

    val nonLabDegree = if (selectedDegree in com.example.data.model.AcademicDegree.STANDARD_DEGREES) {
        selectedDegree
    } else {
        com.example.data.model.AcademicDegree.BACHELOR
    }
    val labDegree = if (selectedDegree in com.example.data.model.AcademicDegree.MED_LAB_DEGREES) {
        selectedDegree
    } else {
        com.example.data.model.AcademicDegree.BACHELOR
    }

    val medLabProgress = progressList.find { it.specialtyId == "MED_LAB" && it.academicDegree == labDegree }
        ?: UserProgressEntity("MED_LAB", labDegree)
    val licensingProgress = progressList.find {
        it.specialtyId == com.example.data.model.QuestionCategory.MED_LAB_LICENSING &&
            it.academicDegree == com.example.data.model.AcademicDegree.BACHELOR
    } ?: UserProgressEntity(
        com.example.data.model.QuestionCategory.MED_LAB_LICENSING,
        com.example.data.model.AcademicDegree.BACHELOR
    )
    val nursingProgress = progressList.find { it.specialtyId == "NURSING" && it.academicDegree == nonLabDegree }
        ?: UserProgressEntity("NURSING", nonLabDegree)
    val paProgress = progressList.find { it.specialtyId == "PHYSICIAN_ASSISTANT" && it.academicDegree == nonLabDegree }
        ?: UserProgressEntity("PHYSICIAN_ASSISTANT", nonLabDegree)

    val medLabDegreeCounts by viewModel.observeDegreeQuestionCounts("MED_LAB").collectAsState(initial = emptyMap())
    val medLabQuestionTotal = if (medLabDegreeCounts.isNotEmpty()) medLabDegreeCounts.values.sum() else 3022
    val nursingDegreeCounts by viewModel.observeDegreeQuestionCounts("NURSING").collectAsState(initial = emptyMap())
    val nursingQuestionTotal = if (nursingDegreeCounts.isNotEmpty()) nursingDegreeCounts.values.sum() else 3510

    val medLabTotalScore = progressList.filter { it.specialtyId == "MED_LAB" }.sumOf { it.totalScore }
    val nursingTotalScore = progressList.filter { it.specialtyId == "NURSING" }.sumOf { it.totalScore }
    val paTotalScore = progressList.filter { it.specialtyId == "PHYSICIAN_ASSISTANT" }.sumOf { it.totalScore }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "المختبرات والعلوم الطبية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "إشراف د/ عبدالله غارب",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD6E3FF)
                        )
                    }
                },
                actions = {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("home_screen_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Supervisor Card with Verified Badge
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { viewModel.openSupervisorProfile(true) }
                        .testTag("supervisor_hero_card"),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MedicalBluePrimary.copy(alpha = 0.08f),
                                        ClinicalTeal.copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Supervisor Photo with Verified Badge
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Image(
                                    painter = painterResource(id = R.drawable.dr_alghareb),
                                    contentDescription = "د. عبدالله غارب",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape)
                                        .border(2.5.dp, MedicalBluePrimary, CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(VerifiedGreen)
                                        .border(1.5.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified Badge",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "المشرف العام د/ عبدالله غارب",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = VerifiedGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Text(
                                    text = "استشاري وباحث في العلوم الطبية والمخبرية",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ClinicalTeal,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Surface(
                                    color = VerifiedGreen.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "برنامج تقييم وتطوير كفاءة علمية وتأهيل للمزاولة",
                                        color = VerifiedGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Feature Shortcuts Strip
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        HomeShortcutChip(
                            text = "محاكاة برومترك (30/60/120د)",
                            icon = Icons.Default.Timer,
                            color = MedicalBluePrimary,
                            testTag = "home_shortcut_prometric"
                        ) {
                            viewModel.openPrometricSetup()
                        }
                    }
                    item {
                        HomeShortcutChip(
                            text = "البطاقات التعليمية",
                            icon = Icons.Default.Style,
                            color = ClinicalTeal,
                            testTag = "home_shortcut_flashcards"
                        ) {
                            viewModel.openFlashcards()
                        }
                    }
                    item {
                        HomeShortcutChip(
                            text = "سجل ومراجعة الاختبارات",
                            icon = Icons.Default.History,
                            color = Color(0xFF6D28D9),
                            testTag = "home_shortcut_history"
                        ) {
                            viewModel.openExamHistory()
                        }
                    }
                    item {
                        HomeShortcutChip("لوحة التقدم", Icons.Default.QueryStats, ClinicalTeal, "home_shortcut_dashboard") {
                            viewModel.navigateTo(ScreenDestination.Dashboard)
                        }
                    }
                    item {
                        HomeShortcutChip("التحدي اليومي (60ث)", Icons.Default.Today, Color(0xFFFF9800), "home_shortcut_daily") {
                            viewModel.navigateTo(ScreenDestination.DailyChallenge)
                        }
                    }
                    item {
                        HomeShortcutChip("ترخيص المزاولة", Icons.Default.MilitaryTech, GoldAccent, "home_shortcut_license") {
                            viewModel.navigateTo(ScreenDestination.Certificate)
                        }
                    }
                    item {
                        HomeShortcutChip("دفتر الأخطاء", Icons.Default.ErrorOutline, Color(0xFFD32F2F), "home_shortcut_mistakes") {
                            viewModel.navigateTo(ScreenDestination.Mistakes)
                        }
                    }
                    item {
                        HomeShortcutChip("المفضلة", Icons.Default.Bookmark, MedicalBluePrimary, "home_shortcut_favorites") {
                            viewModel.navigateTo(ScreenDestination.Favorites)
                        }
                    }
                }
            }

            // 2. Scientific Board Credentials Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MedicalBlueDark
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "بنك الأسئلة والاعتماد الدولي (Board Standard)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "كافة التحديات والخيارات والأدلة التفسيرية مصاغة باللغة الإنجليزية المعتمدة لاختبارات البورد والبرومتريك (ASCP, Prometric, DHA, MOH) وفق خوارزمية عدم التكرار الصارمة (Zero-Duplication Engine).",
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BadgePill("60s زمن التفكير")
                            BadgePill("30 مستوى سريري")
                            BadgePill("أدوات دعم تشخيصية")
                        }
                    }
                }
            }

            // 3. Full Offline Medical Diagnostic Atlas Card (~150MB)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.navigateTo(ScreenDestination.Atlas) }
                        .testTag("atlas_banner_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ClinicalTeal.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Science,
                                contentDescription = null,
                                tint = ClinicalTeal,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "الأطلس الطبي والتشخيصي المدمج",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = VerifiedGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "100% Offline",
                                        color = VerifiedGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "مسحات دم، طفيليات، صبغات جرام، تخطيط قلب، وأشعة X-Ray بدقة 2048x1600",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Section Header: The 3 Clinical Tracks
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مسارات التقييم والاعتماد السريري",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "30 مستوى لكل تخصص",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClinicalTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Track 1: Medical Laboratories Licensing Bank (530 Qs)
            item {
                SpecialtyCard(
                    titleAr = "بنك مزاولة المهنة - المختبرات الطبية",
                    titleEn = "Medical Lab Licensing Exam (530 Qs)",
                    description = "بنك أسئلة مزاولة المهنة الشامل (530 سؤالاً بالإنجليزية الطبية مع ترجمة عربية مخزنة أوفلاين): 9 مراحل متدرجة حسب تخصصات المختبرات.",
                    icon = Icons.Default.VerifiedUser,
                    accentColor = Color(0xFF0F766E),
                    progress = licensingProgress,
                    maxStages = 9,
                    actionButtonText = "بدء تحدي المزاولة (530 سؤال • 9 مراحل)",
                    onClick = {
                        viewModel.navigateTo(
                            ScreenDestination.StageLadder(
                                com.example.data.model.QuestionCategory.MED_LAB_LICENSING,
                                com.example.data.model.AcademicDegree.BACHELOR
                            )
                        )
                    }
                )
            }

            // Track 2: Medical Laboratories (4 Levels: Diploma, Bachelor, Master, Consultant)
            item {
                val formattedMedLabTotal = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
                    .format(medLabQuestionTotal)
                SpecialtyCard(
                    titleAr = "قسم المختبرات والعلوم الطبية",
                    titleEn = "Medical Laboratory Sciences ($formattedMedLabTotal Qs)",
                    description = "4 مستويات علمية: دبلوم • بكالوريوس • ماجستير • استشاري ($formattedMedLabTotal سؤال مقسمة إلى 40 مستوى سريري متدرج)",
                    icon = Icons.Default.Biotech,
                    accentColor = MedicalBluePrimary,
                    progress = medLabProgress.copy(totalScore = medLabTotalScore),
                    actionButtonText = "عرض المراحل الـ 4 والمستويات ($formattedMedLabTotal سؤال)",
                    onClick = {
                        viewModel.navigateTo(ScreenDestination.StageLadder("MED_LAB", labDegree))
                    }
                )
            }

            // Track 3: Nursing Sciences (3,510 Qs after merging 500 reviewed board questions)
            item {
                val formattedNursingTotal = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
                    .format(nursingQuestionTotal)
                SpecialtyCard(
                    titleAr = "قسم علوم التمريض والرعاية الحرجة",
                    titleEn = "Nursing Sciences ($formattedNursingTotal Qs)",
                    description = "3 درجات علمية: دبلوم • بكالوريوس • ماجستير ($formattedNursingTotal سؤالاً متدرجاً من الأسهل إلى الأصعب مع الشروحات والمراجع العلمية)",
                    icon = Icons.Default.Healing,
                    accentColor = ClinicalTeal,
                    progress = nursingProgress.copy(totalScore = nursingTotalScore),
                    onClick = {
                        viewModel.navigateTo(ScreenDestination.StageLadder("NURSING", nonLabDegree))
                    }
                )
            }

            // Track 4: Physician Assistant & General Medicine
            item {
                SpecialtyCard(
                    titleAr = "قسم مساعد الطبيب والطب العام",
                    titleEn = "Physician Assistant (3,008 Qs)",
                    description = "3 مراحل: دبلوم مساعد طبيب • بكالوريوس مساعد طبيب • ماجستير مساعد طبيب (1000+ سؤال لكل مرحلة مقسمة إلى 10 مستويات)",
                    icon = Icons.Default.LocalHospital,
                    accentColor = Color(0xFFC2185B),
                    progress = paProgress.copy(totalScore = paTotalScore),
                    onClick = {
                        viewModel.navigateTo(ScreenDestination.StageLadder("PHYSICIAN_ASSISTANT", nonLabDegree))
                    }
                )
            }

            // 4. Dedicated Supervisor & Direct Social Links Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .testTag("supervisor_contact_section"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Image(
                                    painter = painterResource(id = R.drawable.dr_alghareb),
                                    contentDescription = "د/ عبدالله غارب",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, MedicalBluePrimary, CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(VerifiedGreen)
                                        .border(1.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "المشرف العام والدعم الفني",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "د/ عبدالله غارب • استشاري علوم طبية",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ClinicalTeal,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Social Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.launchWhatsApp(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("home_whatsapp_btn")
                            ) {
                                Text("واتساب المشرف", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { viewModel.launchFacebook(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("home_facebook_btn")
                            ) {
                                Text("فيسبوك المشرف", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { viewModel.launchPhoneCall(context) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("home_call_btn")
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("اتصال مباشر بالدعم الفني والأكاديمي (+967773148358)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showSupervisorProfile) {
        ConsultantProfileDialog(
            onDismiss = { viewModel.openSupervisorProfile(false) },
            onCallClick = { viewModel.launchPhoneCall(context) },
            onWhatsAppClick = { viewModel.launchWhatsApp(context) },
            onFacebookClick = { viewModel.launchFacebook(context) },
            onEmailClick = { viewModel.launchEmail(context) }
        )
    }

    val showContactDialog by viewModel.showContactDialog.collectAsState()
    if (showContactDialog) {
        ConsultantProfileDialog(
            onDismiss = { viewModel.openContactDialog(false) },
            onCallClick = { viewModel.launchPhoneCall(context) },
            onWhatsAppClick = { viewModel.launchWhatsApp(context) },
            onFacebookClick = { viewModel.launchFacebook(context) },
            onEmailClick = { viewModel.launchEmail(context) }
        )
    }
}

@Composable
private fun SpecialtyCard(
    titleAr: String,
    titleEn: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    progress: UserProgressEntity,
    maxStages: Int = 10,
    actionButtonText: String = "عرض المراحل الـ 3 والمستويات (3000 سؤال)",
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("specialty_card_${progress.specialtyId}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(26.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = titleAr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = titleEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    color = accentColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${progress.totalScore} نقطة",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar and Level Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "المستوى التقييمي: ${progress.highestUnlockedStage.coerceAtMost(maxStages)} / $maxStages",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = progress.professionalRankAr,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { (progress.highestUnlockedStage.toFloat() / maxStages.toFloat()).coerceIn(0.1f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = actionButtonText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun HomeShortcutChip(
    text: String,
    icon: ImageVector,
    color: Color,
    testTag: String = "",
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = text, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun BadgePill(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
