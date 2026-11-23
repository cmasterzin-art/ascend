package br.com.ascend.aetheris

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

/** Design prototypes scoped to the Journey, Odin and Mímir screens. */
@Composable
private fun rebirthSignal(): Color =
    if (LocalNorthborn.current.dark) Color(0xFFBFD6CB) else Color(0xFF25463F)

@Composable
internal fun RebirthRule(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier, color = LocalNorthborn.current.hairlineStrong.copy(alpha = .72f))
}

@Composable
internal fun RebirthMasthead(title: String, subtitle: String, art: Int, modifier: Modifier = Modifier) {
    val nb = LocalNorthborn.current
    Box(modifier.fillMaxWidth().clip(CutCornerShape(topEnd = 14.dp, bottomStart = 5.dp))) {
        if (!LocalAppearance.current.minimal) {
            Image(painterResource(art), null, Modifier.matchParentSize(), alignment = Alignment.CenterEnd, contentScale = ContentScale.Crop)
            Box(Modifier.matchParentSize().background(Brush.horizontalGradient(
                0f to nb.canvas, .46f to nb.canvas.copy(alpha = .96f), .71f to nb.canvas.copy(alpha = .72f),
                1f to nb.canvas.copy(alpha = .08f)
            )))
        }
        Column(
            Modifier.fillMaxWidth().heightIn(min = 120.dp).padding(start = 12.dp, end = 26.dp, top = 18.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Text(title, color = nb.inkPrimary, style = MaterialTheme.typography.headlineSmall)
            Text(subtitle, color = nb.inkSecondary, style = MaterialTheme.typography.bodyMedium)
        }
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
    val signal = rebirthSignal()
    val foreground = if (!primary) nb.inkPrimary else if (nb.dark) Color(0xFF13241F) else Color(0xFFF4F7F4)
    Button(
        onClick, modifier.heightIn(min = 48.dp), enabled = enabled,
        shape = CutCornerShape(topEnd = 10.dp, bottomStart = 5.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) signal else nb.surfaceElevated,
            contentColor = foreground
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        if (glyph != null) {
            NorthbornIcon(glyph, foreground, size = 17.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(label, fontWeight = FontWeight.SemiBold)
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
                Column(
                    Modifier.heightIn(min = 48.dp).selectable(active, role = Role.Tab, onClick = { onSelect(index) })
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
                ) {
                    Text(label, color = if (active) rebirthSignal() else nb.inkSecondary,
                        style = MaterialTheme.typography.labelLarge, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
                    Box(Modifier.width(18.dp).height(2.dp).background(if (active) rebirthSignal() else Color.Transparent))
                }
            }
        }
        RebirthRule()
    }
}

@Composable
internal fun RebirthReadingLeaf(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val nb = LocalNorthborn.current
    Row(modifier.fillMaxWidth().background(nb.surfaceDocument)) {
        Box(Modifier.width(3.dp).height(32.dp).background(rebirthSignal()))
        Column(Modifier.weight(1f).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
    }
}
