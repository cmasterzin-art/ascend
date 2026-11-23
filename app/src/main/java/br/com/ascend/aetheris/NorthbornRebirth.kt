package br.com.ascend.aetheris

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

@Composable
internal fun RebirthTimber(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val nb = LocalNorthborn.current
    val engraved = !LocalAppearance.current.minimal
    Box(modifier.fillMaxWidth().background(if (engraved && nb.dark) Hearth else nb.surfaceBase)) {
        if (engraved) {
            Image(
                painterResource(R.drawable.oath_wood), null, Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds, alpha = if (nb.dark) .56f else .10f
            )
            if (nb.dark) {
                Box(Modifier.matchParentSize().background(Brush.horizontalGradient(
                    0f to Hearth.copy(alpha = .2f), .17f to Hearth.copy(alpha = .64f),
                    .84f to Hearth.copy(alpha = .64f), 1f to Hearth.copy(alpha = .18f)
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
            .background(if (artOn) Hearth else nb.surfaceBase)
    ) {
        if (artOn) {
            Image(painterResource(art), null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
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
    val shape = CutCornerShape(8.dp)
    val textured = primary && enabled && !LocalAppearance.current.minimal
    val base = if (textured) Color(0xFF3C2821) else nb.surfaceElevated
    val ink = if (textured) Ivory else nb.inkPrimary
    val edge = if (primary && enabled) nb.accentBronze else nb.hairlineStrong
    Box(
        modifier.heightIn(min = 48.dp).clip(shape).background(base).border(1.dp, edge, shape),
        propagateMinConstraints = true
    ) {
        if (textured) {
            Image(
                painterResource(R.drawable.oath_wood), null, Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds, alpha = .73f
            )
            Box(Modifier.matchParentSize().background(Brush.horizontalGradient(
                listOf(Color(0xFF9A4D34).copy(alpha = .42f), Color.Transparent)
            )))
        }
        Button(
            onClick, Modifier.heightIn(min = 48.dp), enabled = enabled, shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent, contentColor = ink,
                disabledContainerColor = Color.Transparent, disabledContentColor = nb.inkMuted
            ),
            contentPadding = PaddingValues(horizontal = 13.dp, vertical = 11.dp)
        ) {
            if (glyph != null) {
                EngravedIcon(glyph, if (textured) Embers else edge, size = 19.dp, raised = textured)
                Spacer(Modifier.width(8.dp))
            }
            Text(label, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RebirthIndex(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val nb = LocalNorthborn.current
    Column {
        FlowRow(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            labels.forEachIndexed { index, label ->
                val active = selected == index
                val shape = CutCornerShape(topEnd = 7.dp)
                Column(
                    Modifier.heightIn(min = 48.dp).clip(shape)
                        .background(if (active) nb.accentBronze.copy(alpha = .13f) else Color.Transparent)
                        .selectable(active, role = Role.Tab, onClick = { onSelect(index) })
                        .padding(horizontal = 9.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically)
                ) {
                    Text(
                        label, color = if (active) nb.inkPrimary else nb.inkSecondary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                    )
                    Box(Modifier.width(18.dp).height(2.dp).background(if (active) nb.accentBronze else Color.Transparent))
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
internal fun RebirthReadingLeaf(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val plain = LocalAppearance.current.minimal
    val palette = if (plain) LocalNorthborn.current else ManuscriptPalette
    val scheme = MaterialTheme.colorScheme
    val leafScheme = if (plain) scheme else lightColorScheme(
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
                painterResource(R.drawable.oath_manuscript), null, Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Box(Modifier.matchParentSize().background(Brush.horizontalGradient(
                0f to Color.Transparent, .06f to Color.Transparent, .085f to palette.surfaceDocument.copy(alpha = .92f),
                .83f to palette.surfaceDocument.copy(alpha = .82f), 1f to palette.surfaceDocument.copy(alpha = .38f)
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
