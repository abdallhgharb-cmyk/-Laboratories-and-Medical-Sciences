package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionTranslationEntity
import com.example.data.remote.MedicalTranslationService
import com.example.ui.theme.ClinicalError
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.viewmodel.QuestionTranslationUiState

@Composable
fun TranslateActionButton(
    questionId: Long,
    targetLanguage: String,
    uiState: QuestionTranslationUiState?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoading = uiState?.isLoading == true
    val isExpanded = uiState?.isExpanded == true && uiState.translation != null
    val isToArabic = targetLanguage == MedicalTranslationService.TARGET_ARABIC

    val buttonLabel = when {
        isLoading -> "جاري الترجمة..."
        isExpanded -> if (isToArabic) "إخفاء الترجمة العربية" else "Hide English Translation"
        else -> if (isToArabic) "ترجمة للعربية" else "Translate to EN"
    }

    OutlinedButton(
        onClick = onClick,
        enabled = !isLoading,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isExpanded) ClinicalTeal else MedicalBluePrimary.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = modifier
            .heightIn(min = 48.dp)
            .testTag("translate_question_btn_$questionId")
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = MedicalBluePrimary,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Translate,
                contentDescription = buttonLabel,
                tint = if (isExpanded) ClinicalTeal else MedicalBluePrimary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = buttonLabel,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isExpanded) ClinicalTeal else MedicalBluePrimary
        )
    }
}

@Composable
fun TranslatedSubTextBlock(
    translatedText: String?,
    label: String? = null,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    accentColor: Color = ClinicalTeal
) {
    val text = translatedText?.trim().orEmpty()
    AnimatedVisibility(
        visible = isVisible && text.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Surface(
            color = accentColor.copy(alpha = 0.08f),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.28f)),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (!label.isNullOrBlank()) {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        textDirection = TextDirection.Content
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun TranslatedOptionInlineText(
    translatedOption: String?,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val text = translatedOption?.trim().orEmpty()
    AnimatedVisibility(
        visible = isVisible && text.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(modifier = modifier.fillMaxWidth()) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = ClinicalTeal.copy(alpha = 0.2f)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textDirection = TextDirection.Content
                ),
                color = ClinicalTeal,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TranslationErrorBanner(
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    val msg = errorMessage?.trim().orEmpty()
    AnimatedVisibility(visible = msg.isNotEmpty()) {
        Surface(
            color = ClinicalError.copy(alpha = 0.1f),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, ClinicalError.copy(alpha = 0.4f)),
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Translation Error",
                    tint = ClinicalError,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = msg,
                    color = ClinicalError,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

fun QuestionTranslationEntity.optionTranslationAt(index: Int): String = when (index) {
    0 -> translatedOptionA
    1 -> translatedOptionB
    2 -> translatedOptionC
    3 -> translatedOptionD
    else -> ""
}
