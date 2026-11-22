package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ClinicalError
import com.example.ui.theme.ClinicalSuccess
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.ClinicalTealLight
import com.example.ui.theme.ClinicalWarning
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MedicalBlueDark
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.VerifiedGreenLight
import com.example.ui.viewmodel.DashboardLanguage
import com.example.ui.viewmodel.DashboardStrings
import com.example.ui.viewmodel.TopicMasteryUi

@Composable
fun ReadinessGaugeCard(
    accuracyPercent: Int,
    totalScore: Int,
    totalAnswered: Int,
    totalCorrect: Int,
    masteredTopicsCount: Int,
    applicantName: String,
    language: DashboardLanguage
) {
    val targetFraction = (accuracyPercent.coerceIn(0, 100) / 100f)
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 650),
        label = "readiness_sweep"
    )

    val arcColor = when {
        accuracyPercent >= 80 -> VerifiedGreenLight
        accuracyPercent >= 60 -> GoldAccent
        totalAnswered == 0 -> ClinicalTealLight
        else -> ClinicalWarning
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("readiness_gauge_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MedicalBlueDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(MedicalBlueDark, Color(0xFF0A2E5C))
                    )
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            color = ClinicalTeal.copy(alpha = 0.28f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = DashboardStrings.readinessTitle(language),
                                color = Color(0xFF80CBC4),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${DashboardStrings.overallAccuracy(language)}: $accuracyPercent%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = applicantName,
                            color = GoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier.size(88.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(88.dp)) {
                            val strokePx = 9.dp.toPx()
                            val arcSize = Size(size.width - strokePx, size.height - strokePx)
                            val topLeft = Offset(strokePx / 2f, strokePx / 2f)
                            drawArc(
                                color = Color.White.copy(alpha = 0.14f),
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokePx, cap = StrokeCap.Round)
                            )
                            val sweep = 270f * animatedFraction.coerceAtLeast(0.03f)
                            drawArc(
                                color = arcColor,
                                startAngle = 135f,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokePx, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$accuracyPercent%",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = if (language == DashboardLanguage.EN) "Score" else "الدقة",
                                color = Color(0xFFB0BEC5),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    HeroMetricItem(
                        label = DashboardStrings.totalScoreLabel(language),
                        value = "$totalScore",
                        valueColor = GoldAccent
                    )
                    HeroMetricItem(
                        label = DashboardStrings.answeredLabel(language),
                        value = "$totalAnswered",
                        valueColor = Color.White
                    )
                    HeroMetricItem(
                        label = DashboardStrings.correctLabel(language),
                        value = "$totalCorrect",
                        valueColor = VerifiedGreenLight
                    )
                    HeroMetricItem(
                        label = DashboardStrings.topicsMasteredLabel(language),
                        value = "$masteredTopicsCount",
                        valueColor = ClinicalTealLight
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMetricItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = label,
            color = Color(0xFFCFD8DC),
            fontSize = 10.sp
        )
    }
}

@Composable
fun TopicAccuracyBarChartCard(
    topics: List<TopicMasteryUi>,
    language: DashboardLanguage
) {
    val activeTopics = topics
        .filter { it.answeredCount > 0 }
        .sortedByDescending { it.accuracyPercent }
        .take(6)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("topic_accuracy_chart_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Insights,
                        contentDescription = null,
                        tint = MedicalBluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = DashboardStrings.accuracyChartTitle(language),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = DashboardStrings.accuracyChartSubtitle(language),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (activeTopics.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = ClinicalTeal,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = DashboardStrings.noChartDataTip(language),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )
                    }
                }
            } else {
                // Canvas-backed bar chart with dashed 80% mastery threshold guide line
                val gridLineColor = MaterialTheme.colorScheme.outlineVariant
                val targetLineColor = ClinicalTeal

                Box(modifier = Modifier.fillMaxWidth()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(148.dp)
                    ) {
                        val chartBottom = size.height - 6.dp.toPx()
                        val chartTop = 10.dp.toPx()
                        val chartHeight = (chartBottom - chartTop).coerceAtLeast(1f)

                        // Horizontal baseline & 50% reference line
                        drawLine(
                            color = gridLineColor,
                            start = Offset(0f, chartBottom),
                            end = Offset(size.width, chartBottom),
                            strokeWidth = 1.dp.toPx()
                        )
                        val fiftyY = chartBottom - chartHeight * 0.5f
                        drawLine(
                            color = gridLineColor.copy(alpha = 0.5f),
                            start = Offset(0f, fiftyY),
                            end = Offset(size.width, fiftyY),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Dashed 80% Mastery Threshold line
                        val eightyY = chartBottom - chartHeight * 0.8f
                        drawLine(
                            color = targetLineColor,
                            start = Offset(0f, eightyY),
                            end = Offset(size.width, eightyY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )

                        val count = activeTopics.size
                        val slotWidth = size.width / count.toFloat()
                        val barWidth = (slotWidth * 0.52f).coerceIn(18.dp.toPx(), 42.dp.toPx())

                        activeTopics.forEachIndexed { idx, topic ->
                            val centerX = slotWidth * idx + slotWidth / 2f
                            val fraction = (topic.accuracyPercent.coerceIn(0, 100) / 100f).coerceAtLeast(0.05f)
                            val barHeight = chartHeight * fraction
                            val barTop = chartBottom - barHeight
                            val barColor = when {
                                topic.accuracyPercent >= 80 -> ClinicalSuccess
                                topic.accuracyPercent >= 60 -> GoldAccent
                                else -> ClinicalError
                            }
                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(centerX - barWidth / 2f, barTop),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                        }
                    }
                }

                // Exact numeric labels aligned below bars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    activeTopics.forEach { topic ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${topic.accuracyPercent}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    topic.accuracyPercent >= 80 -> ClinicalSuccess
                                    topic.accuracyPercent >= 60 -> GoldAccent
                                    else -> ClinicalError
                                }
                            )
                            Text(
                                text = topic.subCategory,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
