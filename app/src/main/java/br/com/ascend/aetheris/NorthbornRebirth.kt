package br.com.ascend.aetheris

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

private val Hearth = Color(0xFF101318)
private val Embers = Color(0xFFE4B582)
private val Ivory = Color(0xFFF7EADB)
private val Ash = Color(0xFFD3C7B9)

/** Original illustrated settings and materials for the three Northborn pilot screens. */
@Composable
internal fun RebirthFrieze(modifier: Modifier = Modifier) {
    if (LocalAppearance.current.minimal) {
        RebirthRule(modifier)
    } else {
        Image(
            painterResource(R.drawable.rebirth_carved_rail), null,
            modifier.fillMaxWidth().height(15.dp),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
internal fun RebirthRule(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier, color = LocalNorthborn.current.hairlineStrong.copy(alpha = .72f))
}

@Composable
internal fun RebirthRune(rune: HistoricalFutharkRune, color: Color, modifier: Modifier = Modifier) {
    val rim = CutCornerShape(7.dp)
    Box(
        modifier.size(40.dp).clip(rim).background(color.copy(alpha = .08f))
            .border(1.dp, color.copy(alpha = .58f), rim),
        contentAlignment = Alignment.Center
    ) {
        FutharkRuneGlyph(rune, color, size = 21.dp)
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
internal fun RebirthMasthead(title: String, subtitle: String, art: Int, rune: HistoricalFutharkRune) {
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
        RebirthFrieze()
    }
}

@Composable
internal fun RebirthAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = true,
    glyph: NorthbornGlyph? = null,
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
                painterResource(R.drawable.rebirth_oak), null, Modifier.matchParentSize(),
                contentScale = ContentScale.Crop, alpha = .57f
            )
            Box(Modifier.matchParentSize().background(Brush.horizontalGradient(
                listOf(Color(0xFF6B4029).copy(alpha = .68f), Color.Transparent)
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
                NorthbornIcon(glyph, if (textured) Embers else edge, size = 19.dp)
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

@Composable
internal fun RebirthReadingLeaf(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val nb = LocalNorthborn.current
    Box(modifier.fillMaxWidth().background(nb.surfaceDocument)) {
        if (!LocalAppearance.current.minimal) {
            Image(
                painterResource(R.drawable.rebirth_vellum), null, Modifier.matchParentSize(),
                alignment = Alignment.CenterStart, contentScale = ContentScale.FillBounds,
                alpha = if (nb.dark) .23f else .53f
            )
            Box(Modifier.matchParentSize().background(Brush.horizontalGradient(
                0f to Color.Transparent, .12f to nb.surfaceDocument.copy(alpha = .76f),
                .65f to nb.surfaceDocument.copy(alpha = .84f), 1f to nb.surfaceDocument.copy(alpha = .54f)
            )))
        }
        Column(
            Modifier.fillMaxWidth().padding(start = 23.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp), content = content
        )
    }
}
