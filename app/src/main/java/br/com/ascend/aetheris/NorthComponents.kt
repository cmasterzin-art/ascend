package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*

@Composable
fun SagaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    secondary: Boolean = false,
    destructive: Boolean = false,
    loading: Boolean = false,
    completed: Boolean = false,
    ceremonial: Boolean = false,
    selected: Boolean = false,
    style: VikingButtonStyle? = null,
    leadingRune: HistoricalFutharkRune? = null
) {
    val role = when {
        destructive || style == VikingButtonStyle.Destrutivo -> NorthbornButtonRole.Destructive
        completed || style == VikingButtonStyle.Confirmacao -> NorthbornButtonRole.Confirm
        ceremonial || style == VikingButtonStyle.CerimonialViking -> NorthbornButtonRole.Ceremonial
        style == VikingButtonStyle.CompactoRunico -> NorthbornButtonRole.Tertiary
        secondary || style == VikingButtonStyle.FerroRunico -> NorthbornButtonRole.Secondary
        else -> NorthbornButtonRole.Primary
    }
    NorthbornButton(
        text = text,
        onClick = onClick,
        role = role,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        completed = completed,
        selected = selected,
        leadingRune = leadingRune
    )
}

/** Five carved class medallions (raster), each with its own motif; vector engraving in minimal mode. */
@Composable
fun ClassEmblem(index: Int, modifier: Modifier = Modifier) {
    Box(modifier.size(36.dp).semantics { contentDescription = "Emblema ${classes[index]}" }) {
        if (LocalAppearance.current.minimal) ClassEngraving(index, Modifier.matchParentSize())
        else AtlasCell(R.drawable.carved_emblems, index, 5, 2, Modifier.matchParentSize())
    }
}

@Composable
private fun ClassEngraving(index: Int, modifier: Modifier) {
    val colors = listOf(Ember, Blue, Green, Blue, Gold)
    val color = colors[index.coerceIn(colors.indices)]
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val p = Path()
        when (index) {
            0, 2 -> {
                p.moveTo(w * .2f, h * .18f); p.lineTo(w * .8f, h * .18f); p.lineTo(w * .74f, h * .65f); p.lineTo(w * .5f, h * .9f); p.lineTo(w * .26f, h * .65f); p.close()
            }
            1 -> {
                p.moveTo(w * .18f, h * .25f); p.lineTo(w * .5f, h * .34f); p.lineTo(w * .82f, h * .25f); p.lineTo(w * .82f, h * .8f); p.lineTo(w * .5f, h * .88f); p.lineTo(w * .18f, h * .8f); p.close()
            }
            3 -> {
                p.moveTo(w * .5f, h * .08f); p.lineTo(w * .85f, h * .82f); p.lineTo(w * .5f, h * .67f); p.lineTo(w * .15f, h * .82f); p.close()
            }
            else -> {
                p.moveTo(w * .24f, h * .18f); p.lineTo(w * .8f, h * .18f); p.lineTo(w * .8f, h * .4f); p.lineTo(w * .58f, h * .4f); p.lineTo(w * .58f, h * .86f); p.lineTo(w * .4f, h * .86f); p.lineTo(w * .4f, h * .4f); p.lineTo(w * .24f, h * .4f); p.close()
            }
        }
        drawPath(p, color.copy(alpha = .12f))
        drawPath(p, color, style = Stroke(1.5.dp.toPx()))
        if (index == 0 || index == 1 || index == 2) {
            drawLine(color, androidx.compose.ui.geometry.Offset(w * .5f, h * .34f), androidx.compose.ui.geometry.Offset(w * .5f, h * .7f), 1.4.dp.toPx())
        }
    }
}

@Composable
fun NarrativePanel(s: GameState, vm: GameViewModel) {
    val nb = LocalNorthborn.current
    nextNarrative(s)?.let { line ->
        NorthbornSurface(role = NorthbornSurfaceRole.Document, accent = nb.accentBronze) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                when {
                    line.speaker.startsWith("Elyra") -> NorthbornIcon(NorthbornGlyph.Habits, nb.accentBronze, size = 24.dp)
                    line.speaker.startsWith("Darian") -> ClassEmblem(4, Modifier.size(30.dp))
                    line.speaker.startsWith("Seren") -> ClassEmblem(2, Modifier.size(30.dp))
                    else -> NorthbornIcon(NorthbornGlyph.Library, nb.accentBronze, size = 24.dp)
                }
                Column(Modifier.weight(1f)) {
                    Eyebrow("Uma voz de Aetheris", nb.accentBronze)
                    Text(line.speaker, style = MaterialTheme.typography.titleMedium, color = nb.inkPrimary)
                }
            }
            NorthbornDivider(NorthbornDividerStyle.Hairline)
            Text("“${line.text}”", color = nb.inkPrimary, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontSize = 15.sp, lineHeight = 24.sp)
            NorthbornButton("Guardar esta memória", { vm.run { vm.repo.rememberNarrative(line.id) } }, role = NorthbornButtonRole.Secondary)
        }
    }
}