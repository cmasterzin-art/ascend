package br.com.ascend.aetheris

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

private val Hearth = Color(0xFF101318)
private val Embers = Color(0xFFE4B582)
private val Ivory = Color(0xFFF7EADB)
private val Ash = Color(0xFFD3C7B9)

@Composable
internal fun RebirthRule(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier, color = LocalNorthborn.current.hairlineStrong.copy(alpha = .72f))
}

@Composable
internal fun RebirthBoss(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val nb = LocalNorthborn.current
    Box(modifier.clip(CircleShape), contentAlignment = Alignment.Center) {
        if (!LocalAppearance.current.minimal) {
            Image(
                painterResource(R.drawable.oath_shield), null, Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
        } else {
            Box(Modifier.matchParentSize().border(1.dp, nb.hairlineStrong, CircleShape))
        }
        content()
    }
}

@Composable
internal fun RebirthRune(rune: HistoricalFutharkRune, color: Color, modifier: Modifier = Modifier) {
    RebirthBoss(modifier.size(40.dp)) {
        FutharkRuneGlyph(rune, if (LocalAppearance.current.minimal) color else Embers, size = 19.dp)
    }
}

@Composable
internal fun RebirthShieldGauge(value: Float, label: String, diameter: Dp) {
    val nb = LocalNorthborn.current
    val appearance = LocalAppearance.current
    val fraction = value.coerceIn(0f, 1f)
    val sweep by animateFloatAsState(fraction, tween(if (appearance.reducedMotion) 0 else AetherTokens.scene), label = "progresso")
    val accent = if (appearance.minimal) nb.accentBronze else Embers
    RebirthBoss(
        Modifier.size(diameter).semantics { progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f) }
    ) {
        Canvas(Modifier.matchParentSize()) {
            val inset = size.minDimension * .145f
            val bounds = Size(size.width - 2 * inset, size.height - 2 * inset)
            val origin = Offset(inset, inset)
            val stroke = Stroke(1.8.dp.toPx())
            drawArc(accent.copy(alpha = .26f), -90f, 360f, false, origin, bounds, style = stroke)
            drawArc(accent, -90f, 360f * sweep, false, origin, bounds, style = stroke)
        }
        Text(
            label, color = if (appearance.minimal) nb.inkPrimary else Ivory,
            fontFamily = SagaFont, fontSize = if (diameter < 80.dp) 15.sp else 17.sp, maxLines = 1
        )
    }
}

@Composable
internal fun RebirthNotchedTrack(value: Float, accent: Color, track: Color) {
    if (LocalAppearance.current.minimal) {
        Meter(value, accent, trackColor = track)
        return
    }
    val fraction = value.coerceIn(0f, 1f)
    val reduced = LocalAppearance.current.reducedMotion
    val progress by animateFloatAsState(fraction, tween(if (reduced) 0 else AetherTokens.scene), label = "experiência")
    Canvas(Modifier.fillMaxWidth().height(8.dp).semantics {
        progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
    }) {
        val markCount = 8
        val stride = size.width / markCount
        val gap = 3.dp.toPx()
        repeat(markCount) { index ->
            val start = index * stride
            val width = (stride - gap).coerceAtLeast(0f)
            val origin = Offset(start, 0f)
            drawRect(track, origin, Size(width, size.height))
            val filled = (size.width * progress - start).coerceIn(0f, width)
            if (filled > 0f) {
                drawRect(accent, origin, Size(filled, size.height))
                drawLine(Ivory.copy(alpha = .44f), origin, Offset(start + filled, 0f), 1.dp.toPx())
            }
        }
    }
}

/** A few carved tally marks represent the ratio, with exact counts alongside them. */
@Composable
internal fun DayStaves(done: Int, total: Int, modifier: Modifier = Modifier) {
    if (total <= 0) return
    val nb = LocalNorthborn.current
    val appearance = LocalAppearance.current
    val marks = total.coerceAtMost(5)
    val fraction = (done.toFloat() / total).coerceIn(0f, 1f)
    val progress by animateFloatAsState(
        fraction, tween(if (appearance.reducedMotion) 0 else 230), label = "entalhes da jornada"
    )
    Canvas(modifier.size(width = (marks * 14).dp, height = 27.dp).semantics {
        progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
    }) {
        val width = size.width / marks
        repeat(marks) { index ->
            val x = width * index
            val active = (progress * marks - index).coerceIn(0f, 1f)
            val mark = Path().apply {
                moveTo(x + width * .44f, 1.dp.toPx())
                lineTo(x + width * .69f, size.height * .14f)
                lineTo(x + width * .63f, size.height * .86f)
                lineTo(x + width * .33f, size.height - 1.dp.toPx())
                lineTo(x + width * .39f, size.height * .20f)
                close()
            }
            drawPath(mark, nb.hairlineStrong)
            if (active > 0f) drawPath(mark, nb.accentBronze.copy(alpha = active))
            if (!appearance.minimal && active > 0f) {
                drawLine(nb.accentPaleGold.copy(alpha = .68f * active),
                    Offset(x + width * .48f, size.height * .23f),
                    Offset(x + width * .42f, size.height * .78f), .8.dp.toPx())
            }
        }
    }
}

internal enum class RusticMaterial(val art: Int) {
    Timber(R.drawable.surface_oak), Iron(R.drawable.surface_iron)
}

@Composable
internal fun RusticSurface(
    material: RusticMaterial,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val nb = LocalNorthborn.current
    val engraved = !LocalAppearance.current.minimal
    Box(modifier.fillMaxWidth().background(if (engraved && nb.dark) Hearth else nb.surfaceBase)) {
        if (engraved) {
            Image(
                painterResource(material.art), null, Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds, alpha = if (nb.dark) .93f else .15f
            )
            if (nb.dark) {
                Box(Modifier.matchParentSize().background(Brush.horizontalGradient(
                    0f to Hearth.copy(alpha = .12f), .16f to Hearth.copy(alpha = .40f),
                    .84f to Hearth.copy(alpha = .40f), 1f to Hearth.copy(alpha = .12f)
                )))
            }
        }
        content()
    }
}

internal data class RebirthSceneInk(val title: Color, val body: Color, val accent: Color)

@Composable
internal fun RebirthScene(
    art: Int,
    height: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(RebirthSceneInk) -> Unit
) {
    val nb = LocalNorthborn.current
    val artOn = !LocalAppearance.current.minimal
    val ink = if (artOn) RebirthSceneInk(Ivory, Ash, Embers)
        else RebirthSceneInk(nb.inkPrimary, nb.inkSecondary, nb.accentBronze)
    Box(
        modifier.fillMaxWidth().heightIn(min = if (artOn) height else 0.dp)
            .clipToBounds().background(if (artOn) Hearth else nb.surfaceBase)
    ) {
        if (artOn) {
            val revealed = craftReveal(art, 340)
            Image(
                painterResource(art), null,
                Modifier.matchParentSize().graphicsLayer {
                    val settling = 1f - revealed.value
                    scaleX = 1f + .035f * settling
                    scaleY = scaleX
                    translationY = 3.dp.toPx() * settling
                    alpha = 1f - .2f * settling
                },
                contentScale = ContentScale.Crop
            )
            Box(Modifier.matchParentSize().background(Brush.verticalGradient(
                0f to Hearth.copy(alpha = .53f), .35f to Color.Transparent,
                .58f to Hearth.copy(alpha = .36f), 1f to Hearth
            )))
            Box(Modifier.matchParentSize().background(Brush.horizontalGradient(
                0f to Hearth.copy(alpha = .56f), .46f to Hearth.copy(alpha = .22f), 1f to Color.Transparent
            )))
        }
        content(ink)
    }
}

@Composable
internal fun RebirthMasthead(
    title: String, subtitle: String, art: Int, rune: HistoricalFutharkRune, rail: EngravedRail
) {
    Column {
        RebirthScene(art, 185.dp) { ink ->
            Column(
                Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RebirthRune(rune, ink.accent)
                Text(title, style = MaterialTheme.typography.headlineMedium, color = ink.title)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = ink.body)
            }
        }
        EngravedDivider(rail)
    }
}

@Composable
internal fun RebirthAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = true,
    glyph: Engraving? = null,
    enabled: Boolean = true
) {
    val nb = LocalNorthborn.current
    val shape = NorthbornShape.button
    val plate = forgedPlate(if (primary) NorthbornButtonRole.Primary else NorthbornButtonRole.Secondary, enabled)
    val base = if (primary && enabled) Color(0xFF47372A) else nb.surfaceElevated
    val ink = if (primary && enabled) Ivory else nb.inkPrimary
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val press = craftPress(interaction)
    val reduced = LocalAppearance.current.reducedMotion
    val edge = if (primary && enabled) nb.accentBronze else nb.hairlineStrong
    Box(
        modifier.heightIn(min = 48.dp).clip(shape).background(base)
            .forgedFinish(plate, press)
            .then(
                when {
                    focused -> Modifier.border(2.dp, nb.accentPaleGold, shape)
                    plate == null -> Modifier.border(1.dp, edge, shape)
                    else -> Modifier
                }
            ),
        propagateMinConstraints = true
    ) {
        Button(
            onClick, Modifier.heightIn(min = 48.dp), enabled = enabled, shape = shape,
            interactionSource = interaction,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent, contentColor = ink,
                disabledContainerColor = Color.Transparent, disabledContentColor = nb.inkMuted
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp)
        ) {
            Row(
                Modifier.graphicsLayer {
                    if (!reduced) translationY = 1.dp.toPx() * press.value
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (glyph != null) {
                    val iconInk = when {
                        !enabled -> nb.inkMuted
                        primary && enabled -> Embers
                        else -> nb.accentBronze
                    }
                    EngravedIcon(glyph, iconInk, size = 19.dp, raised = plate != null)
                    Spacer(Modifier.width(8.dp))
                }
                Text(label, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RebirthIndex(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val nb = LocalNorthborn.current
    val reduced = LocalAppearance.current.reducedMotion
    Column {
        FlowRow(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            labels.forEachIndexed { index, label ->
                val active = selected == index
                val emphasis by animateFloatAsState(
                    if (active) 1f else 0f, tween(if (reduced) 0 else 165), label = "seleção do índice"
                )
                val shape = CutCornerShape(topEnd = 7.dp)
                Column(
                    Modifier.heightIn(min = 48.dp).clip(shape)
                        .background(nb.accentBronze.copy(alpha = .13f * emphasis))
                        .selectable(active, role = Role.Tab, onClick = { onSelect(index) })
                        .padding(horizontal = 9.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically)
                ) {
                    Text(
                        label, color = if (active) nb.inkPrimary else nb.inkSecondary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                    )
                    Box(Modifier.width(18.dp).height(2.dp).graphicsLayer {
                        alpha = emphasis
                        scaleX = .45f + .55f * emphasis
                    }.background(nb.accentBronze))
                }
            }
        }
        RebirthRule()
    }
}

private val ManuscriptPalette = NorthbornLightPalette.copy(
    surfaceDocument = Color(0xFFF3E4C9),
    inkPrimary = Color(0xFF28201A),
    inkSecondary = Color(0xFF574534),
    inkTertiary = Color(0xFF634E3A),
    accentBronze = Color(0xFF71502D),
    hairlineStrong = Color(0xFF847055)
)

@Composable
internal fun RebirthReadingLeaf(
    modifier: Modifier = Modifier, parchment: Boolean = false, content: @Composable ColumnScope.() -> Unit
) {
    val plain = LocalAppearance.current.minimal
    val documentInk = !plain || parchment
    val palette = if (documentInk) ManuscriptPalette else LocalNorthborn.current
    val leafScheme = if (!documentInk) MaterialTheme.colorScheme else lightColorScheme(
        primary = palette.accentBronze,
        onPrimary = palette.onAccent,
        secondary = palette.accentFjord,
        tertiary = Color(0xFF6F435C),
        onSurface = palette.inkPrimary,
        onSurfaceVariant = palette.inkSecondary,
        surface = palette.surfaceDocument,
        surfaceVariant = palette.surfaceElevated
    )

    Box(modifier.fillMaxWidth().background(palette.surfaceDocument)) {
        if (!plain) {
            Image(
                painterResource(R.drawable.surface_vellum), null, Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Box(Modifier.matchParentSize().background(Brush.horizontalGradient(
                0f to Color.Transparent, .06f to Color.Transparent, .087f to palette.surfaceDocument.copy(alpha = .93f),
                .78f to palette.surfaceDocument.copy(alpha = .78f), 1f to palette.surfaceDocument.copy(alpha = .40f)
            )))
        }
        CompositionLocalProvider(LocalNorthborn provides palette) {
            MaterialTheme(colorScheme = leafScheme) {
                CompositionLocalProvider(LocalContentColor provides palette.inkPrimary) {
                    Column(
                        Modifier.fillMaxWidth().padding(start = 33.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp), content = content
                    )
                }
            }
        }
    }
}
