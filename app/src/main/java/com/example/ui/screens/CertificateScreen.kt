package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MedicalBlueDark
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.VerifiedGreen
import com.example.ui.viewmodel.AssessmentViewModel
import com.example.ui.viewmodel.ScreenDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateScreen(viewModel: AssessmentViewModel) {
    val context = LocalContext.current
    val profile by viewModel.applicantProfile.collectAsState()
    val progressList by viewModel.allProgress.collectAsState()
    val totalCorrect by viewModel.totalCorrectCount.collectAsState()
    val totalAnswered by viewModel.totalAnsweredCount.collectAsState()

    BackHandler {
        viewModel.navigateTo(ScreenDestination.Home)
    }

    val applicantName = profile?.fullName?.ifBlank { "د. عبدالحكيم محمد الشرعبي" } ?: "د. متقدم مزاولة المهنة"
    val degree = profile?.degreeType ?: "بكالوريوس"
    val specialty = profile?.specialty ?: "مختبرات طبية"

    val accuracy = if (totalAnswered > 0) ((totalCorrect.toFloat() / totalAnswered) * 100).toInt() else 85
    val ratingTitle = when {
        accuracy >= 90 -> "استشاري متميز بدرجة امتياز مع مرتبة الشرف | Consultant with High Distinction"
        accuracy >= 80 -> "أخصائي سريري معتمد بدرجة جيد جداً | Certified Specialist (Very Good)"
        accuracy >= 70 -> "ممارس سريري مؤهل بدرجة جيد | Qualified Clinical Practitioner"
        else -> "متدرب سريري مجتاز لمتطلبات التقييم | Passed Clinical Trainee"
    }

    val serialNumber = "MED-LIC-YE-${((applicantName.hashCode() and 0x7FFFFFFF) % 90000) + 10000}"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "شهادة تقييم وترخيص مزاولة المهنة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.Home) },
                        modifier = Modifier.testTag("cert_back_btn")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("certificate_screen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // The Framed Royal Certificate Document
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, GoldAccent, RoundedCornerShape(18.dp))
                    .testTag("certificate_document_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFDFE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Document Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "الجمهورية اليمنية",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalBluePrimary
                            )
                            Text(
                                text = "المجلس الطبي الأعلى والتقييم السريري",
                                fontSize = 10.sp,
                                color = Color(0xFF455A64)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(36.dp)
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Republic of Yemen",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalBluePrimary
                            )
                            Text(
                                text = "Medical Board Council",
                                fontSize = 10.sp,
                                color = Color(0xFF455A64)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = GoldAccent.copy(alpha = 0.5f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "ترخيص مزاولة المهنة وشهادة كفاءة سريرية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MedicalBlueDark,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "CLINICAL PRACTICE LICENSE & BOARD CERTIFICATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "يشهد المشرف العام للبرنامج الأكاديمي والتقييم السريري بأن المتقدم:",
                        fontSize = 12.sp,
                        color = Color(0xFF37474F)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = applicantName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MedicalBluePrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "حامل مؤهل ($degree) في تخصص ($specialty)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF263238)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "قد اجتاز بنجاح معايير التقييم النظري والعملي والتحليل التشخيصي المجهري وفق معايير امتحانات البورد والبرومتريك الدولية بمعدل دقة سريرية بلغت ($accuracy%)، وبناءً عليه مُنح هذا الترخيص بتقدير:",
                        fontSize = 11.sp,
                        color = Color(0xFF455A64),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = VerifiedGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VerifiedGreen)
                    ) {
                        Text(
                            text = ratingTitle,
                            color = VerifiedGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Serial Number and Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "رقم الاعتماد:", fontSize = 10.sp, color = Color.Gray)
                            Text(text = serialNumber, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF101B2B))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "تاريخ الإصدار:", fontSize = 10.sp, color = Color.Gray)
                            Text(text = "2026/10/02 م", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF101B2B))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFCFD8DC), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Signatures & Official Stamp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Official Stamp
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .border(2.dp, VerifiedGreen, CircleShape)
                                .background(VerifiedGreen.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(24.dp))
                                Text("معتمد رسمي", fontSize = 8.sp, color = VerifiedGreen, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Supervisor Signature & Photo
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(id = R.drawable.dr_alghareb),
                                contentDescription = "المشرف العام",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, MedicalBluePrimary, CircleShape)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "د. عبدالله غارب",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalBluePrimary
                            )
                            Text(
                                text = "المشرف العام واستشاري العلوم الطبية",
                                fontSize = 9.sp,
                                color = Color(0xFF546E7A)
                            )
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "تم بفضل الله حصول $applicantName ($degree في $specialty) على ترخيص مزاولة المهنة وشهادة الكفاءة السريرية بتقدير ($ratingTitle) برقم اعتماد ($serialNumber) تحت إشراف د/ عبدالله غارب."
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة شهادة الترخيص"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("share_cert_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاركة الوثيقة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.navigateTo(ScreenDestination.Profile) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("edit_profile_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تعديل البيانات", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
