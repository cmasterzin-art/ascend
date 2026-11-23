package br.com.ascend.aetheris

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
    val barBg = if (nb.dark) Color(0xFF12151A) else Color(0xFFF8F4EC)
    val claspRes = if (nb.dark) R.drawable.engraved_clasp else R.drawable.engraved_clasp_light

    Column(
        Modifier
            .fillMaxWidth()
            .background(barBg)
            .navigationBarsPadding()
    ) {
        // Architectural sculpted top hairline with subtle centered diamond finial (replaces heavy wool bar)
        Canvas(Modifier.fillMaxWidth().height(if (carved) 5.dp else 1.dp)) {
            val cy = size.height * 0.5f
            val d = 1.dp.toPx()
            val seamColor = if (nb.dark) nb.accentBronze.copy(alpha = 0.44f) else nb.accentBronze.copy(alpha = 0.34f)
            drawLine(
                Brush.horizontalGradient(listOf(nb.hairline.copy(alpha = 0.4f), seamColor, seamColor, nb.hairline.copy(alpha = 0.4f))),
                Offset(0f, cy),
                Offset(size.width, cy),
                d
            )
            if (carved) {
                val cx = size.width * 0.5f
                val r = 2.4f * d
                val diamond = Path().apply {
                    moveTo(cx, cy - r); lineTo(cx + r * 1.45f, cy); lineTo(cx, cy + r); lineTo(cx - r * 1.45f, cy); close()
                }
                drawPath(diamond, activeInk.copy(alpha = if (nb.dark) 0.78f else 0.65f))
            }
        }
        Box(Modifier.fillMaxWidth()) {
            if (carved && nb.dark) {
                Image(
                    painterResource(R.drawable.surface_wool), null, Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop, alpha = .14f
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
                    val labelColor = if (active) (if (nb.dark) nb.inkPrimary else nb.accentBronze) else nb.inkSecondary

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
                                if (press.value > 0f) drawRect(activeInk.copy(alpha = .09f * press.value))
                                if (progress > 0f) {
                                    val d = 1.dp.toPx()
                                    val barW = (size.width * 0.44f * progress).coerceAtLeast(18 * d)
                                    val x0 = (size.width - barW) * 0.5f
                                    // Soft top-centered ambient glow behind the active medallion
                                    drawCircle(
                                        Brush.radialGradient(
                                            listOf(
                                                activeInk.copy(alpha = (if (nb.dark) 0.14f else 0.10f) * progress),
                                                Color.Transparent
                                            ),
                                            center = Offset(size.width * 0.5f, size.height * 0.36f),
                                            radius = size.width * 0.52f
                                        ),
                                        radius = size.width * 0.52f,
                                        center = Offset(size.width * 0.5f, size.height * 0.36f)
                                    )
                                    drawLine(
                                        Brush.horizontalGradient(
                                            listOf(Color.Transparent, activeInk.copy(alpha = progress), activeInk.copy(alpha = progress), Color.Transparent),
                                            startX = x0,
                                            endX = x0 + barW
                                        ),
                                        Offset(x0, size.height - 1.5f * d),
                                        Offset(x0 + barW, size.height - 1.5f * d),
                                        2.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(top = 5.dp, bottom = 7.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Box(
                                Modifier.size(50.dp, 33.dp).graphicsLayer {
                                    if (!motion) {
                                        scaleX = 1f - .05f * press.value
                                        scaleY = scaleX
                                        translationY = 1.dp.toPx() * press.value
                                    }
                                },
                                contentAlignment = Alignment.Center
                            ) {
                                if (carved && progress > 0f) {
                                    Image(
                                        painterResource(claspRes), null,
                                        Modifier.matchParentSize(), contentScale = ContentScale.Fit, alpha = progress
                                    )
                                }
                                val activeGlyphInk = if (nb.dark) Color(0xFFF3D29A) else Color(0xFF745122)
                                EngravedIcon(
                                    routeEngravings[i],
                                    if (active && carved) activeGlyphInk else iconColor,
                                    size = 22.dp, raised = active && carved && nb.dark
                                )
                            }
                            Text(
                                text = mainNames[i],
                                color = labelColor,
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                                letterSpacing = 0.25.sp,
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
 * Abas editoriais NORTHBORN 4.2: tipografia limpa com leve lavagem bronze chanfrada e filete geométrico inferior.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AetherisTabs(values: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val nb = LocalNorthborn.current
    val motion = LocalAppearance.current.reducedMotion
    val minimal = LocalAppearance.current.minimal
    BoxWithConstraints(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        val compact = maxWidth < 360.dp && values.size >= 6
        val hPad = if (compact) 8.dp else 13.dp
        val minH = NorthbornSpace.touchMin
        val fSize = if (compact) 11.5.sp else 13.5.sp
        val tabShape = androidx.compose.foundation.shape.CutCornerShape(topStart = 5.dp, topEnd = 5.dp)
        Column(Modifier.fillMaxWidth()) {
            FlowRow(
                Modifier.fillMaxWidth().selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                values.forEachIndexed { index, label ->
                    val active = index == selected
                    val light by animateFloatAsState(
                        if (active) 1f else 0f,
                        tween(if (motion) 0 else AetherTokens.quick),
                        label = "nb_subtab"
                    )
                    val activeAccent = if (nb.dark) nb.accentPaleGold else nb.accentBronze
                    Box(
                        Modifier
                            .heightIn(min = minH)
                            .clip(tabShape)
                            .background(nb.accentBronze.copy(alpha = (if (nb.dark) 0.13f else 0.11f) * light))
                            .selectable(active, onClick = { onSelect(index) }, role = Role.Tab)
                            .drawBehind {
                                if (light > 0f) {
                                    val d = 1.dp.toPx()
                                    val y = size.height - 1.5f * d
                                    drawLine(
                                        activeAccent.copy(alpha = light),
                                        Offset(size.width * 0.12f, y),
                                        Offset(size.width * 0.88f, y),
                                        2.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                    if (!minimal) {
                                        val cx = size.width * 0.5f
                                        val r = 2.2f * d
                                        val diamond = Path().apply {
                                            moveTo(cx, y - r); lineTo(cx + r * 1.25f, y); lineTo(cx, y + r * 0.7f); lineTo(cx - r * 1.25f, y); close()
                                        }
                                        drawPath(diamond, activeAccent.copy(alpha = light))
                                    }
                                }
                            }
                            .padding(horizontal = hPad),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (active) activeAccent else nb.inkSecondary,
                            fontSize = fSize,
                            lineHeight = (fSize.value + 4).sp,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(nb.hairlineStrong.copy(alpha = 0.65f)))
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
