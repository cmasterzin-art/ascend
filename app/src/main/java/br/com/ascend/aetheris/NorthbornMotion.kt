package br.com.ascend.aetheris

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import kotlin.math.roundToInt

internal fun craftScreenEnter(reduced: Boolean, back: Boolean = false): EnterTransition {
    if (reduced) return EnterTransition.None
    val direction = if (back) -1 else 1
    return fadeIn(tween(210)) + slideInVertically(tween(230, easing = LinearOutSlowInEasing)) {
        (it * .018f).roundToInt() * direction
    }
}

internal fun craftSectionEnter(reduced: Boolean): EnterTransition =
    if (reduced) EnterTransition.None
    else fadeIn(tween(170)) + expandVertically(animationSpec = tween(220, easing = FastOutSlowInEasing), expandFrom = Alignment.Top)

internal fun craftSectionExit(reduced: Boolean): ExitTransition =
    if (reduced) ExitTransition.None
    else fadeOut(tween(100)) + shrinkVertically(animationSpec = tween(140), shrinkTowards = Alignment.Top)

/** Moves ornament and ink while the size of the underlying touch target stays constant. */
@Composable
internal fun craftPress(source: MutableInteractionSource): State<Float> {
    val pressed by source.collectIsPressedAsState()
    val reduced = LocalAppearance.current.reducedMotion
    return animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = if (reduced) snap() else tween(if (pressed) 90 else 150, easing = FastOutSlowInEasing),
        label = "pressão no controle"
    )
}

/** A short reveal only when the content changes, with no animation job in reduced-motion mode. */
@Composable
internal fun craftReveal(contentKey: Any, durationMs: Int): State<Float> {
    val reduced = LocalAppearance.current.reducedMotion
    val reveal = remember(contentKey, reduced) { Animatable(if (reduced) 1f else 0f) }
    if (!reduced) {
        LaunchedEffect(reveal) { reveal.animateTo(1f, tween(durationMs, easing = LinearOutSlowInEasing)) }
    }
    return reveal.asState()
}
