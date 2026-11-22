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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AcademicDegree
import com.example.data.model.UserProgressEntity
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MedicalBlueDark
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.VerifiedGreen
import com.example.ui.viewmodel.AssessmentViewModel
import com.example.ui.viewmodel.ScreenDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StageLadderScreen(
    specialtyId: String,
    initialDegree: String = AcademicDegree.BACHELOR,
    viewModel: AssessmentViewModel
) {
    BackHandler {
        viewModel.navigateTo(ScreenDestination.Home)
    }

    val isLicensingTrack = specialtyId == com.example.data.model.QuestionCategory.MED_LAB_LICENSING
    val degreesList = AcademicDegree.degreesForSpecialty(specialtyId)
    val effectiveInitialDegree = when {
        isLicensingTrack -> AcademicDegree.BACHELOR
        initialDegree in degreesList -> initialDegree
        else -> AcademicDegree.BACHELOR
    }

    LaunchedEffect(specialtyId, effectiveInitialDegree) {
        viewModel.setSelectedDegree(effectiveInitialDegree)
    }

    val selectedDegree by viewModel.selectedDegree.collectAsState()
    val activeDegree = when {
        isLicensingTrack -> AcademicDegree.BACHELOR
        selectedDegree in degreesList -> selectedDegree
        else -> AcademicDegree.BACHELOR
    }
    val progressList by viewModel.allProgress.collectAsState()
    val stageProgressMap by viewModel.observeStageProgress(specialtyId, activeDegree).collectAsState(initial = emptyMap())
    val stageCounts by viewModel.observeStageQuestionCounts(specialtyId, activeDegree).collectAsState(initial = emptyMap())
    val degreeCounts by viewModel.observeDegreeQuestionCounts(specialtyId).collectAsState(initial = emptyMap())
    val userProgress = progressList.find { it.specialtyId == specialtyId && it.academicDegree == activeDegree }
        ?: UserProgressEntity(specialtyId = specialtyId, academicDegree = activeDegree)

    val maxStages = com.example.data.model.QuestionCategory.maxStagesForSpecialty(specialtyId)
    val totalTrackQuestions = if (stageCounts.isNotEmpty()) stageCounts.values.sum() else if (isLicensingTrack) 530 else 1000
    val totalSpecialtyQuestions = if (isLicensingTrack) {
        totalTrackQuestions
    } else if (degreeCounts.isNotEmpty()) {
        degreeCounts.values.sum()
    } else if (specialtyId == com.example.data.model.QuestionCategory.NURSING) {
        3510
    } else {
        3000
    }

    val specialtyNameAr = when (specialtyId) {
        com.example.data.model.QuestionCategory.MED_LAB_LICENSING -> "بنك مزاولة المهنة - المختبرات الطبية"
        "MED_LAB" -> "المختبرات والعلوم الطبية"
        "NURSING" -> "علوم التمريض والرعاية الحرجة"
        else -> "مساعد الطبيب والطب العام"
    }

    val selectedTabIndex = degreesList.indexOf(activeDegree).coerceAtLeast(0)
    val totalStarsEarned = stageProgressMap.values.sumOf { it.starsEarned }
    val totalStarsPossible = maxStages * 3

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isLicensingTrack) "سلم مزاولة المهنة (9 تخصصات)" else "سلم المستويات والتقييم السريري",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        val formattedTotal = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
                            .format(totalSpecialtyQuestions)
                        Text(
                            text = "$specialtyNameAr ($formattedTotal سؤال)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD6E3FF)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.Home) },
                        modifier = Modifier.testTag("ladder_back_button")
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("stage_ladder_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Degree Switcher Tabs (Hidden for unified 530-Q Licensing Bank)
            if (!isLicensingTrack) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = MedicalBluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "المرحلة الأكاديمية / الدرجة العلمية:",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MedicalBlueDark
                                )
                            }

                            TabRow(
                                selectedTabIndex = selectedTabIndex,
                                containerColor = Color(0xFFF1F5F9),
                                contentColor = MedicalBluePrimary,
                                modifier = Modifier.clip(RoundedCornerShape(12.dp))
                            ) {
                                degreesList.forEachIndexed { index, degree ->
                                    val degreeLabel = when (degree) {
                                        AcademicDegree.DIPLOMA -> "دبلوم"
                                        AcademicDegree.BACHELOR -> "بكالوريوس"
                                        AcademicDegree.MASTER -> "ماجستير"
                                        AcademicDegree.CONSULTANT -> "استشاري"
                                        else -> degree
                                    }
                                    val defaultDegreeCount = when {
                                        specialtyId == com.example.data.model.QuestionCategory.MEDICAL_LABS &&
                                            (degree == AcademicDegree.MASTER || degree == AcademicDegree.CONSULTANT) -> 500
                                        else -> 1000
                                    }
                                    val degreeCount = degreeCounts[degree] ?: defaultDegreeCount
                                    Tab(
                                        selected = selectedTabIndex == index,
                                        onClick = { viewModel.setSelectedDegree(degree) },
                                        modifier = Modifier.testTag("degree_tab_$degree"),
                                        text = {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = degreeLabel,
                                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = if (degreesList.size >= 4) 11.sp else 13.sp,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "$degreeCount سؤال",
                                                    fontSize = 9.sp,
                                                    color = if (selectedTabIndex == index) MedicalBluePrimary else Color.Gray,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Header Stats Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MedicalBlueDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isLicensingTrack) {
                                    "المسار المعتمد: امتحان مزاولة المهنة"
                                } else {
                                    "المرحلة الحالية: ${AcademicDegree.getDegreeTitleAr(activeDegree, specialtyId)}"
                                },
                                color = Color(0xFFB0BEC5),
                                fontSize = 12.sp
                            )
                            Text(
                                text = userProgress.professionalRankAr,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "المستوى ${userProgress.highestUnlockedStage.coerceAtMost(maxStages)} من $maxStages • الفتح عند تحقيق 70% فأعلى (نجمتان ★ ★)",
                                color = GoldAccent,
                                fontSize = 11.sp
                            )
                            if (totalStarsEarned > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "النجوم المكتسبة: $totalStarsEarned / $totalStarsPossible ★",
                                    color = Color(0xFFFEF08A),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${userProgress.totalScore}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "النقاط",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // Progressive Levels (9 specialty stages for MED_LAB_LICENSING, 10 levels for others)
            items(maxStages) { index ->
                val stageNumber = index + 1
                val stageProg = stageProgressMap[stageNumber]
                val isUnlocked = (stageNumber <= userProgress.highestUnlockedStage) || (stageProg?.isUnlocked == true)
                val isCompleted = (stageNumber < userProgress.highestUnlockedStage) || (stageProg?.isPassed == true)
                val isCurrent = stageNumber == userProgress.highestUnlockedStage && !isCompleted
                val defaultStageQs = when {
                    isLicensingTrack -> 0
                    specialtyId == "MED_LAB" &&
                        (activeDegree == AcademicDegree.MASTER || activeDegree == AcademicDegree.CONSULTANT) -> 50
                    else -> 100
                }
                val questionsInStage = stageCounts[stageNumber] ?: defaultStageQs
                val earnedStars = stageProg?.starsEarned ?: if (stageNumber < userProgress.highestUnlockedStage) 2 else 0

                val (topicAr, topicEn) = getStageTopicDetails(specialtyId, activeDegree, stageNumber, questionsInStage)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = isUnlocked) {
                            viewModel.startAssessment(specialtyId, activeDegree, stageNumber)
                        }
                        .testTag("stage_card_$stageNumber"),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isCurrent -> MedicalBluePrimary.copy(alpha = 0.08f)
                            isUnlocked -> MaterialTheme.colorScheme.surface
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 2.dp else 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Stage Number Badge
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCompleted -> VerifiedGreen
                                        isCurrent -> MedicalBluePrimary
                                        else -> Color.Gray.copy(alpha = 0.3f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(Icons.Default.Check, contentDescription = "Completed", tint = Color.White)
                            } else if (isUnlocked) {
                                Text(
                                    text = "$stageNumber",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            } else {
                                Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.White.copy(alpha = 0.7f))
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "المستوى التقييمي $stageNumber ($questionsInStage سؤال)",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
                                )
                                if (isCurrent) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = MedicalBluePrimary,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "المستوى الحالي",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = topicAr,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = if (isUnlocked) ClinicalTeal else Color.Gray.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = topicEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }

                        if (isUnlocked) {
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (earnedStars > 0) {
                                        repeat(3) { starIdx ->
                                            Icon(
                                                imageVector = if (starIdx < earnedStars) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = null,
                                                tint = if (starIdx < earnedStars) GoldAccent else Color.Gray.copy(alpha = 0.4f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Action",
                                            tint = MedicalBluePrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                if (stageProg != null && stageProg.bestAccuracyPercent > 0) {
                                    Text(
                                        text = "${stageProg.bestAccuracyPercent}%",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCompleted) VerifiedGreen else ClinicalTeal
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

private fun getStageTopicDetails(
    specialtyId: String,
    degree: String,
    stage: Int,
    questionCount: Int = 100
): Pair<String, String> {
    if (specialtyId == com.example.data.model.QuestionCategory.MED_LAB_LICENSING) {
        return when (stage) {
            1 -> Pair("ضبط الجودة والسلامة المخبرية", "Quality Control & Lab Safety ($questionCount Qs)")
            2 -> Pair("تحليل البول وسوائل الجسم", "Urinalysis & Body Fluids ($questionCount Qs)")
            3 -> Pair("علم الطفيليات التشخيصي", "Diagnostic Parasitology ($questionCount Qs)")
            4 -> Pair("أمراض الدم والتخثر", "Hematology ($questionCount Qs)")
            5 -> Pair("الكيمياء السريرية", "Clinical Chemistry ($questionCount Qs)")
            6 -> Pair("الأحياء الدقيقة الطبية", "Microbiology ($questionCount Qs)")
            7 -> Pair("المصول والمناعة السريرية", "Serology & Immunology ($questionCount Qs)")
            8 -> Pair("بنك الدم ونقل الدم", "Blood Bank ($questionCount Qs)")
            else -> Pair("التشخيص الجزيئي والـ PCR", "Molecular Diagnostics ($questionCount Qs)")
        }
    }
    return when (specialtyId) {
        "MED_LAB" -> when (degree) {
            AcademicDegree.DIPLOMA -> when (stage) {
                1 -> Pair("سحب العينات والأنابيب وموانع التجلط", "Phlebotomy & Anticoagulants (100 Qs)")
                2 -> Pair("أساسيات أمراض الدم وتعداد الخلايا", "Basic Hematology & Cell Counting (100 Qs)")
                3 -> Pair("الفحص الروتيني للبول والبراز والطفيليات", "Urinalysis & Parasitology (100 Qs)")
                4 -> Pair("الكيمياء السريرية الروتينية والسكري", "Basic Clinical Chemistry (100 Qs)")
                5 -> Pair("الأحياء الدقيقة وصبغة غرام وزيل-نلسن", "Microbiology & Stains (100 Qs)")
                6 -> Pair("تحضير وزراعة الأوساط البكتيرية", "Culture Media Preparation (100 Qs)")
                7 -> Pair("فصائل الدم ABO والـ Rh وبنك الدم", "Blood Banking & ABO/Rh (100 Qs)")
                8 -> Pair("المصول والاختبارات السريعة Widal/HBsAg", "Routine Serology & Rapid Tests (100 Qs)")
                9 -> Pair("السلامة البيولوجية وإدارة النفايات والتعقيم", "Biosafety & Sterilization (100 Qs)")
                else -> Pair("معايرة الأجهزة وضبط الجودة المخبرية", "Quality Control & Calibration (100 Qs)")
            }
            AcademicDegree.BACHELOR -> when (stage) {
                1 -> Pair("الإنزيمات السريرية ودلالات الاحتشاء القلبي", "Advanced Enzymology & Biomarkers (100 Qs)")
                2 -> Pair("غازات الدم الشرياني والتوازن الحمضي القاعدي", "Arterial Blood Gases & ABG (100 Qs)")
                3 -> Pair("اعتلالات الهيموغلوبين والثلاسيميا والمنجلي", "Hemoglobinopathies & Smears (100 Qs)")
                4 -> Pair("مسارات التخثر والنزاف وسيولة الدم PT/APTT", "Hemostasis & Coagulation (100 Qs)")
                5 -> Pair("مقاومة المضادات الحيوية وESBL وCRE", "Bacterial Resistance & MIC (100 Qs)")
                6 -> Pair("الفطريات التشخيصية والفيروسات والـ PCR", "Clinical Mycology & Virology (100 Qs)")
                7 -> Pair("طفيليات الملاريا والليشمانيا وعينات الدم", "Vector-borne Parasitology (100 Qs)")
                8 -> Pair("الأجسام المضادة غير المنتظمة وتوافق الدم", "Transfusion & Antibody Panels (100 Qs)")
                9 -> Pair("المناعة الذاتية ANA و Anti-dsDNA و ELISA", "Autoimmune & Flow Cytometry (100 Qs)")
                else -> Pair("دلالات الأورام وهرمونات الغدد الصماء", "Endocrine & Tumor Markers (100 Qs)")
            }
            AcademicDegree.MASTER -> when (stage) {
                1, 2 -> Pair("التشخيص الجزيئي المتقدم و Real-Time PCR", "Molecular Diagnostics & NGS ($questionCount Qs)")
                3, 4 -> Pair("التدفق الخلوي والواسمات المناعية CD", "Flow Cytometry Immunophenotyping ($questionCount Qs)")
                5, 6 -> Pair("إدارة الجودة الشاملة ISO 15189 و Westgard", "Quality Management ISO 15189 ($questionCount Qs)")
                7, 8 -> Pair("سرطانات الدم وخزعات نخاع العظم WHO", "Hematologic Malignancies & Marrow ($questionCount Qs)")
                else -> Pair("أهبة التخثر الوراثية و Factor V Leiden", "Thrombophilia & Special Coag ($questionCount Qs)")
            }
            AcademicDegree.CONSULTANT -> when (stage) {
                1, 2 -> Pair("حل إشكالات فصائل الدم والفصائل النادرة", "Complex Immunohematology Consultant ($questionCount Qs)")
                3, 4 -> Pair("السموم السريرية ومطيافية الكتلة LC-MS", "Clinical Toxicology & LC-MS ($questionCount Qs)")
                5, 6 -> Pair("أخطاء الاستقلاب الوراثية ومسح حديثي الولادة", "Inborn Errors & Metabolic Lab ($questionCount Qs)")
                7, 8 -> Pair("المناعة النسيجية الكيميائية IHC والواسمات", "Immunohistochemistry & HER2 ($questionCount Qs)")
                else -> Pair("القيادة المخبرية المتقدمة ومعايير الاعتماد CAP", "Laboratory Leadership & CAP Board ($questionCount Qs)")
            }
            else -> Pair("المختبرات والعلوم الطبية المتقدمة", "Advanced Laboratory Board ($questionCount Qs)")
        }
        "NURSING" -> when (degree) {
            AcademicDegree.DIPLOMA -> when (stage) {
                1 -> Pair("العلامات الحيوية والتقييم السريري الأولي", "Vital Signs & Patient Assessment ($questionCount Qs)")
                2 -> Pair("مكافحة العدوى والتعقيم وتقنيات PPE", "Infection Control & Asepsis ($questionCount Qs)")
                3 -> Pair("إعطاء الأدوية والحقن العضلي وتحت الجلد", "Medication Administration & Safety ($questionCount Qs)")
                4 -> Pair("العناية بالجروح وقرح الفراش ومقياس برادن", "Wound Care & Braden Scale ($questionCount Qs)")
                5 -> Pair("القسطرة البولية والعناية بالإخراج", "Catheterization & Elimination ($questionCount Qs)")
                6 -> Pair("حساب السوائل الداخلة والخارجة والتغذية", "Fluid Intake/Output & Nutrition ($questionCount Qs)")
                7 -> Pair("الرعاية التمريضية قبل وبعد العمليات الجراحية", "Perioperative Nursing Basics ($questionCount Qs)")
                8 -> Pair("سلامة المرضى والوقاية من السقوط", "Patient Safety & Fall Prevention ($questionCount Qs)")
                9 -> Pair("العلاج بالأكسجين والعناية بالمجرى التنفسي", "Oxygen Therapy & Airway Care ($questionCount Qs)")
                else -> Pair("الإنعاش القلبي الرئوي والإسعافات الأولية BLS", "BLS & Emergency First Aid ($questionCount Qs)")
            }
            AcademicDegree.BACHELOR -> when (stage) {
                1 -> Pair("تمريض الباطني والجراحي: القلب والجهاز التنفسي", "Med-Surg: Cardio & Pulmonary ($questionCount Qs)")
                2 -> Pair("تمريض الباطني: السكري والفشل الكلوي الحاد", "Med-Surg: Endocrine & Renal ($questionCount Qs)")
                3 -> Pair("تمريض العناية المركزة ICU وأجهزة التنفس", "Critical Care & ICU Protocols ($questionCount Qs)")
                4 -> Pair("الحسابات الدوائية المتقدمة والمحاليل الوريدية", "Advanced Drug Calculations ($questionCount Qs)")
                5 -> Pair("تمريض النساء والتوليد ورعاية الولادة و PPH", "Maternal & Obstetric Nursing ($questionCount Qs)")
                6 -> Pair("تمريض الأطفال وحديثي الولادة وسوائل الإماهة", "Pediatric & Neonatal Nursing ($questionCount Qs)")
                7 -> Pair("تمريض الأعصاب والسكتة الدماغية ومقياس GCS", "Neurological Nursing & Stroke ($questionCount Qs)")
                8 -> Pair("تمريض الأورام والاحتياطات الكيماوية", "Oncology & Chemotherapy ($questionCount Qs)")
                9 -> Pair("التمريض النفسي والتواصل العلاجي والصحة النفسية", "Psychiatric & Mental Health ($questionCount Qs)")
                else -> Pair("فرز الطوارئ والإنعاش القلبي المتقدم ACLS", "Emergency Triage & ACLS ($questionCount Qs)")
            }
            else -> when (stage) {
                1 -> Pair("الديناميكا الدموية المتقدمة وقسطرة سوان-غانز", "Advanced Hemodynamics & PAWP ($questionCount Qs)")
                2 -> Pair("استراتيجيات التنفس الصناعي المتقدمة و ARDS", "Advanced Mechanical Ventilation ($questionCount Qs)")
                3 -> Pair("التقييم الصحي المتقدم وتفسير تخطيط القلب", "Advanced Health Assessment ($questionCount Qs)")
                4 -> Pair("العلاج الدوائي المتقدم في فشل الأعضاء المتعدد", "Pharmacotherapy in Multi-Organ Failure ($questionCount Qs)")
                5 -> Pair("إدارة الإصابات والكوارث وفرز الإصابات الجماعية", "Trauma & Disaster Management ($questionCount Qs)")
                6 -> Pair("الممارسة التمريضية القائمة على الأدلة EBP", "Evidence-Based Nursing Practice ($questionCount Qs)")
                7 -> Pair("القيادة التمريضية ومعايير الاعتماد الدولي JCI", "Nursing Leadership & Magnet ($questionCount Qs)")
                8 -> Pair("العناية المركزة المتقدمة لحديثي الولادة NICU", "Advanced Neonatal Intensive Care ($questionCount Qs)")
                9 -> Pair("الرعاية التلطيفية والقرارات الأخلاقية والـ DNR", "Palliative Care & Bioethics ($questionCount Qs)")
                else -> Pair("الممارسة التمريضية المتقدمة واستقلالية القرار", "Advanced Practice Nursing Scope ($questionCount Qs)")
            }
        }
        else -> when (degree) { // PHYSICIAN_ASSISTANT
            AcademicDegree.DIPLOMA -> when (stage) {
                1 -> Pair("الفحص السريري الأولي وأخذ التاريخ المرضي", "Primary Clinical Exam & History (100 Qs)")
                2 -> Pair("الأمراض الجلدية الشائعة في الرعاية الأولية", "Primary Dermatology & Minor Care (100 Qs)")
                3 -> Pair("الجراحة الصغرى وخياطة الجروح والتخدير الموضعي", "Minor Surgery & Suturing (100 Qs)")
                4 -> Pair("تقييم الجهاز الحركي وتثبيت الكسور والجبائر", "Musculoskeletal & Splinting (100 Qs)")
                5 -> Pair("الأمراض المعدية الشائعة في العيادات الأولية", "Common Infectious Diseases (100 Qs)")
                6 -> Pair("علم الأدوية الأساسي والوصفات الدوائية الآمنة", "Essential Pharmacology (100 Qs)")
                7 -> Pair("رعاية العيون والأنف والأذن والحنجرة الأولية", "Primary Ophthalmology & ENT (100 Qs)")
                8 -> Pair("الصحة الإنجابية وتنظيم الأسرة ورعاية الحوامل", "Reproductive Health & Family Med (100 Qs)")
                9 -> Pair("متابعة الأمراض المزمنة (الضغط والسكري)", "Chronic Disease Management (100 Qs)")
                else -> Pair("صحة المجتمع وجدول اللقاحات الوطنية والترصد", "Community Health & Immunization (100 Qs)")
            }
            AcademicDegree.BACHELOR -> when (stage) {
                1 -> Pair("الطب الباطني: أمراض القلب واحتشاء العضلة القلبية", "Cardiology & STEMI Management (100 Qs)")
                2 -> Pair("الطب الباطني: أمراض الصدر والتهاب الرئة و COPD", "Pulmonology & Pneumonia (100 Qs)")
                3 -> Pair("الطب الباطني: الجهاز الهضمي والنزيف الحاد", "Gastroenterology & GI Bleed (100 Qs)")
                4 -> Pair("الطب الباطني: أمراض الكلى واضطراب الشوارد", "Nephrology & Electrolytes (100 Qs)")
                5 -> Pair("الجراحة العامة والبطن الحاد والتهاب الزائدة", "General Surgery: Acute Abdomen (100 Qs)")
                6 -> Pair("طب الطوارئ وأنواع الصدمات والإنعاش السريع", "Emergency Medicine & Shock (100 Qs)")
                7 -> Pair("طب الأطفال: طوارئ الأطفال وتسمم الدم والجفاف", "Acute Pediatric Emergencies (100 Qs)")
                8 -> Pair("طوارئ النساء والتوليد والحمل الهاجر و PPH", "Obstetric & Gynecologic Emergencies (100 Qs)")
                9 -> Pair("طب الأعصاب: السكتة الدماغية والصرع والتهاب السحايا", "Neurology: Stroke & Meningitis (100 Qs)")
                else -> Pair("تفسير تخطيط القلب ECG وأشعة الصدر X-Ray", "Diagnostic ECG & Chest X-Ray (100 Qs)")
            }
            else -> when (stage) {
                1 -> Pair("العناية المركزة المتقدمة وتسمم الدم Sepsis-3", "Advanced Critical Care & Sepsis (100 Qs)")
                2 -> Pair("التشخيص التفريقي المعقد والحالات السريرية النادرة", "Complex Differential Diagnosis (100 Qs)")
                3 -> Pair("طوارئ القلب المتقدمة وتسلخ الشريان الأبهر", "Cardiovascular Emergencies (100 Qs)")
                4 -> Pair("طوارئ الغدد الصماء والحماض الكيتوني DKA", "Endocrine Crises & DKA/HHS (100 Qs)")
                5 -> Pair("علم السموم والترياقات والجرعات الزائدة", "Clinical Toxicology & Antidotes (100 Qs)")
                6 -> Pair("إنعاش الإصابات البليغة المتقدم بروتوكول ATLS", "Advanced Trauma & ATLS (100 Qs)")
                7 -> Pair("الأمراض المعدية المتقدمة وترشيد المضادات الحيوية", "Infectious Diseases Stewardship (100 Qs)")
                8 -> Pair("العناية المركزة العصبية المتقدمة ونزيف الدماغ", "Neurocritical Care & SAH (100 Qs)")
                9 -> Pair("أمراض الروماتيزم والمناعة الذاتية المعقدة SLE", "Rheumatology & Autoimmune (100 Qs)")
                else -> Pair("اتخاذ القرار السريري المتقدم والأخلاقيات الطبية", "Clinical Decision Making & Ethics (100 Qs)")
            }
        }
    }
}
