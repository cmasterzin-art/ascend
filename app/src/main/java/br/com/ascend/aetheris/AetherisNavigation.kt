package br.com.ascend.aetheris

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*

val mainRoutes = listOf("journey", "habits", "missions", "world", "character")
val mainNames = listOf("Jornada", "Hábitos", "Missões", "Mundo", "Herói")
private val mainNorthbornGlyphs = listOf(
    NorthbornGlyph.Journey,
    NorthbornGlyph.Habits,
    NorthbornGlyph.Missions,
    NorthbornGlyph.World,
    NorthbornGlyph.Hero
)

/** Compatibilidade com chamadas existentes que usam NavigationSigil. */
@Composable
fun NavigationSigil(index: Int, color: Color, modifier: Modifier = Modifier) {
    NorthbornIcon(mainNorthbornGlyphs[index.mod(5)], color, modifier, size = 24.dp)
}

/**
 * Navegação Inferior NORTHBORN 4.0:
 * Assinatura arquitetônica em basalto/linho com filete superior preciso, 5 ícones escandinavos
 * de traço uniforme, indicador superior em bronze forjado e ergonomia confortável para uma mão.
 */
@Composable
fun AetherisNavigation(route: String, onSelect: (String) -> Unit) {
    val selected = if (route == "mimir" || route.startsWith("mimir_") || route == "codex_runico" || route.startsWith("region/") || route.startsWith("location/") || route.startsWith("expedition/")) {
        "world"
    } else if (route.startsWith("path/") || route.startsWith("odin") || route.startsWith("council_nine") || route in listOf("paths", "inbox", "pending")) {
        "journey"
    } else if (route.startsWith("time_forge")) {
        "habits"
    } else {
        when (route) {
            "grimoire", "planner", "analysis", "focus_history" -> "habits"
            "settings", "journal", "memories", "portrait", "heraldry" -> "character"
            "weekly" -> "world"
            else -> route
        }
    }
    val nb = LocalNorthborn.current
    val motion = LocalAppearance.current.reducedMotion
    val pilot = route == "journey" || route.startsWith("odin") || route.startsWith("mimir")
    val carved = pilot && !LocalAppearance.current.minimal
    val activeInk = if (carved && nb.dark) Color(0xFFE4B582) else nb.accentBronze
    val barBg = if (carved && nb.dark) Color(0xFF1E1717) else if (nb.dark) Color(0xFF101215) else nb.surfaceBase

    Column(
        Modifier
            .fillMaxWidth()
            .background(barBg)
            .navigationBarsPadding()
    ) {
        if (carved) {
            RebirthBraid()
        } else {
            Box(Modifier.fillMaxWidth().height(1.dp).background(nb.hairlineStrong.copy(alpha = 0.72f)))
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            mainRoutes.forEachIndexed { i, r ->
                val active = selected == r
                val progress by animateFloatAsState(
                    if (active) 1f else 0f,
                    tween(if (motion) 0 else AetherTokens.quick),
                    label = "nb_nav_tab"
                )
                val iconColor = if (active) activeInk else nb.inkSecondary
                val labelColor = if (active) (if (pilot) nb.inkPrimary else if (nb.dark) nb.accentPaleGold else nb.accentBronze) else nb.inkSecondary

                Box(
                    Modifier
                        .weight(1f)
                        .testTag("nav:$r")
                        .heightIn(min = 64.dp)
                        .selectable(active, onClick = { onSelect(r) }, role = Role.Tab)
                        .semantics(mergeDescendants = true) {}
                        .drawBehind {
                            if (progress > 0f) {
                                val d = 1.dp.toPx()
                                val barW = (size.width * 0.42f * progress).coerceAtLeast(18 * d)
                                val x0 = (size.width - barW) * 0.5f
                                if (pilot) drawRect(activeInk.copy(alpha = .10f * progress))
                                drawRect(
                                    color = activeInk.copy(alpha = progress),
                                    topLeft = Offset(x0, 0f),
                                    size = Size(barW, 2.2f * d)
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 9.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) {
                            if (active && carved) {
                                RebirthBoss(Modifier.matchParentSize()) {
                                    NorthbornIcon(mainNorthbornGlyphs[i], Color(0xFFE4B582), size = 16.dp, filled = true)
                                }
                            } else {
                                NorthbornIcon(mainNorthbornGlyphs[i], iconColor, size = 22.dp, filled = active)
                            }
                        }
                        Text(
                            text = mainNames[i],
                            color = labelColor,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                            letterSpacing = 0.2.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Abas editoriais NORTHBORN 4.0: palavras limpas com marcador inferior bronze e zero caixas.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AetherisTabs(values: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val nb = LocalNorthborn.current
    val motion = LocalAppearance.current.reducedMotion
    BoxWithConstraints(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        val compact = maxWidth < 360.dp && values.size >= 6
        val hPad = if (compact) 7.dp else 12.dp
        val minH = if (compact) 40.dp else 46.dp
        val fSize = if (compact) 11.5.sp else 13.5.sp
        Column(Modifier.fillMaxWidth()) {
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                values.forEachIndexed { index, label ->
                    val active = index == selected
                    val light by animateFloatAsState(
                        if (active) 1f else 0f,
                        tween(if (motion) 0 else AetherTokens.quick),
                        label = "nb_subtab"
                    )
                    Box(
                        Modifier
                            .heightIn(min = minH)
                            .selectable(active, onClick = { onSelect(index) }, role = Role.Tab)
                            .drawBehind {
                                if (light > 0f) {
                                    val d = 1.dp.toPx()
                                    val y = size.height - 2 * d
                                    drawLine(
                                        nb.accentBronze.copy(alpha = light),
                                        Offset(size.width * 0.14f, y),
                                        Offset(size.width * 0.86f, y),
                                        2.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                            .padding(horizontal = hPad),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (active) (if (nb.dark) nb.accentPaleGold else nb.accentBronze) else nb.inkSecondary,
                            fontSize = fSize,
                            lineHeight = (fSize.value + 4).sp,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(nb.hairline))
        }
    }
}

/** Faixa de atalhos arquitetônica limpa separada por fios verticais. */
@Composable
fun ShortcutStrip(items: List<Triple<String, Glyph, () -> Unit>>) {
    val nb = LocalNorthborn.current
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val roomy = maxWidth / items.size.coerceAtLeast(1) >= 112.dp
        Column(Modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(nb.hairline))
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                items.forEachIndexed { i, (label, glyph, action) ->
                    if (i > 0) Box(Modifier.width(1.dp).fillMaxHeight().padding(vertical = 12.dp).background(nb.hairline))
                    Row(
                        Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clickable(onClick = action)
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally)
                    ) {
                        if (roomy) CarvedGlyph(glyph, nb.accentBronze, size = 15.dp)
                        Text(
                            label,
                            color = nb.inkPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(nb.hairline))
        }
    }
}
