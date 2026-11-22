package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ClinicalError
import com.example.ui.theme.ClinicalSuccess
import com.example.ui.theme.ClinicalWarning
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.viewmodel.CloudAuthUiState
import com.example.ui.viewmodel.DashboardLanguage
import com.example.ui.viewmodel.SyncUiState

@Composable
fun GlobalSyncStatusBanner(
    authState: CloudAuthUiState,
    syncState: SyncUiState,
    visible: Boolean,
    language: DashboardLanguage,
    onRetryClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldShow = visible &&
        authState is CloudAuthUiState.SignedIn &&
        syncState !is SyncUiState.Idle

    AnimatedVisibility(
        visible = shouldShow,
        enter = expandVertically(animationSpec = tween(220)) + fadeIn(animationSpec = tween(220)),
        exit = shrinkVertically(animationSpec = tween(220)) + fadeOut(animationSpec = tween(180)),
        modifier = modifier.fillMaxWidth()
    ) {
        val isAr = language == DashboardLanguage.AR

        val targetBg: Color
        val targetFg: Color
        val message: String
        val statusTag: String

        when (syncState) {
            is SyncUiState.OfflinePending -> {
                targetBg = ClinicalWarning.copy(alpha = 0.14f)
                targetFg = Color(0xFFB45309)
                statusTag = "sync_banner_offline_pending"
                val countNote = if (syncState.pendingChangesCount > 1) " (${syncState.pendingChangesCount})" else ""
                message = if (isAr) {
                    "غير متصل — تم حفظ التقدم محلياً وسيُرفع إلى Firestore عند عودة الاتصال$countNote"
                } else {
                    "Offline — local progress saved$countNote & queued for Firestore"
                }
            }
            is SyncUiState.Syncing -> {
                targetBg = MedicalBluePrimary.copy(alpha = 0.12f)
                targetFg = MedicalBluePrimary
                statusTag = if (syncState.isReconnectAutoPush) "sync_banner_reconnecting" else "sync_banner_syncing"
                message = if (syncState.isReconnectAutoPush) {
                    if (isAr) {
                        "عاد الاتصال — جاري دفع بيانات التقدم إلى Firestore..."
                    } else {
                        "Connection restored — pushing local progress to Firestore..."
                    }
                } else {
                    if (isAr) {
                        "جاري مزامنة التقدم والملاحظات مع Firestore..."
                    } else {
                        "Syncing progress & notes with Firestore..."
                    }
                }
            }
            is SyncUiState.Synced -> {
                targetBg = ClinicalSuccess.copy(alpha = 0.13f)
                targetFg = ClinicalSuccess
                statusTag = "sync_banner_synced"
                message = if (syncState.isReconnectAutoPush) {
                    if (isAr) {
                        "تمت استعادة الاتصال وحفظ التقدم في Firestore بنجاح"
                    } else {
                        "Reconnected — local progress pushed to Firestore"
                    }
                } else {
                    if (isAr) {
                        "تمت مزامنة التقدم مع Firestore بنجاح"
                    } else {
                        "Progress synced to Firestore"
                    }
                }
            }
            is SyncUiState.Error -> {
                targetBg = ClinicalError.copy(alpha = 0.12f)
                targetFg = ClinicalError
                statusTag = "sync_banner_error"
                message = if (isAr) {
                    "تعذرت المزامنة مع Firestore — البيانات محفوظة محلياً"
                } else {
                    "Firestore sync paused — local progress safely saved"
                }
            }
            is SyncUiState.Idle -> {
                targetBg = Color.Transparent
                targetFg = MedicalBluePrimary
                statusTag = "sync_banner_idle"
                message = ""
            }
        }

        val animatedBg by animateColorAsState(targetValue = targetBg, animationSpec = tween(220), label = "sync_bg")
        val animatedFg by animateColorAsState(targetValue = targetFg, animationSpec = tween(220), label = "sync_fg")

        Surface(
            color = animatedBg,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, animatedFg.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("global_sync_status_banner")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                    .testTag(statusTag),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (syncState) {
                        is SyncUiState.Syncing -> {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = animatedFg,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        is SyncUiState.OfflinePending -> {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Offline Queued",
                                tint = animatedFg,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        is SyncUiState.Synced -> {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Synced to Firestore",
                                tint = animatedFg,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        is SyncUiState.Error -> {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Sync Error",
                                tint = animatedFg,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        is SyncUiState.Idle -> {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = animatedFg,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textDirection = TextDirection.Content
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (syncState is SyncUiState.Error) {
                        TextButton(
                            onClick = onRetryClick,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("global_sync_retry_button")
                        ) {
                            Text(
                                text = if (isAr) "إعادة" else "Retry",
                                color = animatedFg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    if (syncState !is SyncUiState.Syncing) {
                        IconButton(
                            onClick = onDismissClick,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("global_sync_dismiss_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss sync status",
                                tint = animatedFg,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
