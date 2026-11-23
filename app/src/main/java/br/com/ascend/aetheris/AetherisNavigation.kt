package br.com.ascend.aetheris

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*

val mainRoutes = listOf("journey", "habits", "missions", "world", "character")
val mainNames = listOf("Jornada", "Hábitos", "Missões", "Mundo", "Herói")
private val routeEngravings = listOf(
    Engraving.Ship, Engraving.Tally, Engraving.Banner, Engraving.Compass, Engraving.Shield
)

/** Compatibilidade com chamadas existentes que usam NavigationSigil. */
@Composable
fun NavigationSigil(index: Int, color: Color, modifier: Modifier = Modifier) {
    EngravedIcon(routeEngravings[index.mod(routeEngravings.size)], color, modifier)
}

/**
 * Five engraved destinations with a forged clasp for the selected one.
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
    val carved = !LocalAppearance.current.minimal
    val activeInk = if (nb.dark) nb.accentPaleGold else nb.accentBronze
    val barBg = if (nb.dark) Color(0xFF121317) else nb.surfaceBase

    Column(
        Modifier
            .fillMaxWidth()
            .background(barBg)
            .navigationBarsPadding()
    ) {
        EngravedDivider(EngravedRail.Voyage)
        Box(Modifier.fillMaxWidth()) {
            if (carved && nb.dark) {
                Image(
                    painterResource(R.drawable.surface_wool), null, Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop, alpha = .28f
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                mainRoutes.forEachIndexed { i, r ->
                    val active = selected == r
                    val interaction = remember { MutableInteractionSource() }
                    val press = craftPress(interaction)
                    val progress by animateFloatAsState(
                        if (active) 1f else 0f,
                        tween(if (motion) 0 else AetherTokens.quick),
                        label = "nb_nav_tab"
                    )
                    val iconColor = if (active) activeInk else nb.inkSecondary
                    val labelColor = if (active) nb.inkPrimary else nb.inkSecondary

                    Box(
                        Modifier
                            .weight(1f)
                            .testTag("nav:$r")
                            .heightIn(min = 64.dp)
                            .selectable(
                                selected = active, interactionSource = interaction, indication = LocalIndication.current,
                                onClick = { onSelect(r) }, role = Role.Tab
                            )
                            .semantics(mergeDescendants = true) {}
                            .drawBehind {
                                if (press.value > 0f) drawRect(activeInk.copy(alpha = .11f * press.value))
                                if (progress > 0f) {
                                    val d = 1.dp.toPx()
                                    val barW = (size.width * 0.42f * progress).coerceAtLeast(18 * d)
                                    val x0 = (size.width - barW) * 0.5f
                                    drawRect(activeInk.copy(alpha = .08f * progress))
                                    drawRect(
                                        color = activeInk.copy(alpha = progress),
                                        topLeft = Offset(x0, size.height - 2 * d),
                                        size = Size(barW, 2 * d)
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(top = 5.dp, bottom = 7.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                Modifier.size(49.dp, 34.dp).graphicsLayer {
                                    if (!motion) {
                                        scaleX = 1f - .055f * press.value
                                        scaleY = scaleX
                                        translationY = 1.dp.toPx() * press.value
                                    }
                                },
                                contentAlignment = Alignment.Center
                            ) {
                                if (carved && progress > 0f) {
                                    Image(
                                        painterResource(R.drawable.engraved_clasp), null,
                                        Modifier.matchParentSize(), contentScale = ContentScale.Fit, alpha = progress
                                    )
                                }
                                EngravedIcon(
                                    routeEngravings[i],
                                    if (active && carved) Color(0xFFF1C68D) else iconColor,
                                    size = 23.dp, raised = active && carved
                                )
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
