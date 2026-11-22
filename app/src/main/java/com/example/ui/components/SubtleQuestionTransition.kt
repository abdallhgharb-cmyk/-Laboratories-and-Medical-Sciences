package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/**
 * Subtle, direction-aware horizontal slide + gentle fade transition used between
 * questions in quiz sessions and cards in flashcard decks.
 *
 * Uses a restrained 12.5% horizontal travel distance paired with a 240ms ease
 * so advancing feels fluid without fatiguing the reader during long sessions.
 */
@Composable
fun <T> SubtleQuestionTransition(
    targetKey: T,
    modifier: Modifier = Modifier,
    direction: Int = 1,
    content: @Composable (T) -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val normalizedDir = if (direction >= 0) 1 else -1
    // Mirror slide vector in RTL so "next" moves in the natural reading progression.
    val slideSign = if (layoutDirection == LayoutDirection.Rtl) -normalizedDir else normalizedDir

    AnimatedContent(
        targetState = targetKey,
        modifier = modifier,
        transitionSpec = {
            val enter = fadeIn(
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            ) + slideInHorizontally(
                animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                initialOffsetX = { fullWidth -> (fullWidth / 8) * slideSign }
            )
            val exit = fadeOut(
                animationSpec = tween(durationMillis = 170, easing = FastOutSlowInEasing)
            ) + slideOutHorizontally(
                animationSpec = tween(durationMillis = 210, easing = FastOutSlowInEasing),
                targetOffsetX = { fullWidth -> -(fullWidth / 8) * slideSign }
            )
            (enter togetherWith exit).using(SizeTransform(clip = false))
        },
        label = "subtle_question_transition"
    ) { stateKey ->
        content(stateKey)
    }
}
