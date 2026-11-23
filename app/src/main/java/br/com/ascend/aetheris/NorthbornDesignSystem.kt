package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*

/**
 * ASCEND 1.7.5.9 — NORTHBORN (Design System 4.0)
 * Linguagem visual contemporânea de artesanato escandinavo:
 * - Paleta semântica unificada (Escuro mineral / Claro documental / Minimalista).
 * - Escala modular de espaçamento (4 / 8 / 12 / 16 / 24 / 32 dp) e chanfros arquitetônicos simétricos.
 * - 8 materiais autênticos em northborn_materials_atlas.webp aplicados com parcimônia (baixa opacidade, zero ruído).
 * - 5 papéis de superfície (Operacional, Documental, Estratégica, Cerimonial, Informativa) sem caixas redundantes.
 * - Sete papéis de botão, com placas de forja renderizadas e estados nativos.
 * - Divisores geométricos nórdicos e iconografia arquitetônica de traço uniforme.
 */

@Immutable
data class NorthbornPalette(
    val canvas: Color,
    val surfaceBase: Color,
    val surfaceElevated: Color,
    val surfaceDocument: Color,
    val surfaceTinted: Color,
    val inkPrimary: Color,
    val inkSecondary: Color,
    val inkTertiary: Color,
    val accentBronze: Color,
    val accentPaleGold: Color,
    val accentSilver: Color,
    val accentFjord: Color,
    val accentPatina: Color,
    val accentEmber: Color,
    val hairline: Color,
    val hairlineStrong: Color,
    val onAccent: Color,
    val dark: Boolean
) {
    val inkMuted: Color get() = inkTertiary
}

val NorthbornDarkPalette = NorthbornPalette(
    canvas = Color(0xFF111418),          // Deep Fjord Charcoal
    surfaceBase = Color(0xFF181C21),     // Matte Forged Iron
    surfaceElevated = Color(0xFF20252C), // Brushed Slate
    surfaceDocument = Color(0xFF1C1A16), // Aged Smoked Oak / Dark Vellum
    surfaceTinted = Color(0xFF161B22),   // Fjord Night Slate
    inkPrimary = Color(0xFFF1ECE2),      // Unbleached Ivory
    inkSecondary = Color(0xFFA9B2BC),    // Weathered Pewter
    inkTertiary = Color(0xFF7D8692),     // Ash Stone
    accentBronze = Color(0xFFC5A26E),    // Aged Burnished Bronze
    accentPaleGold = Color(0xFFE3C896),  // Pale Nordic Gold (ceremonial)
    accentSilver = Color(0xFFBAC5D1),    // Oxidized Silver
    accentFjord = Color(0xFF86A7C7),     // Boreal Fjord Blue
    accentPatina = Color(0xFF85AB91),    // Verdigris Patina Green
    accentEmber = Color(0xFFCF8074),     // Muted Forge Ember
    hairline = Color(0xFF262B33),        // Quiet architectural seam
    hairlineStrong = Color(0xFF39414D),  // Crisp structural border
    onAccent = Color(0xFF14110D),
    dark = true
)

val NorthbornLightPalette = NorthbornPalette(
    canvas = Color(0xFFF3EEE4),          // Warm Unbleached Linen
    surfaceBase = Color(0xFFFAF7F0),     // Archival Ivory Paper
    surfaceElevated = Color(0xFFEBE4D7), // Pale Carved Oak
    surfaceDocument = Color(0xFFF7F0E2), // Manuscript Vellum
    surfaceTinted = Color(0xFFEAEFF2),   // Cold Mineral Mist
    inkPrimary = Color(0xFF1D2127),      // Iron-Gall Ink
    inkSecondary = Color(0xFF4F5863),    // Weathered Slate Ink
    inkTertiary = Color(0xFF747D88),     // Muted Ash
    accentBronze = Color(0xFF825D2B),    // Deep Burnished Bronze (AAA on linen)
    accentPaleGold = Color(0xFF694B20),  // Dark Ceremonial Bronze
    accentSilver = Color(0xFF505D6B),    // Forged Pewter
    accentFjord = Color(0xFF2B5678),     // Deep Fjord Blue
    accentPatina = Color(0xFF286446),    // Nordic Pine Patina
    accentEmber = Color(0xFF9A4034),     // Iron-Oxide Red
    hairline = Color(0xFFDAD2C3),        // Quiet archival rule
    hairlineStrong = Color(0xFFB9AFA0),  // Crisp linen seam
    onAccent = Color(0xFFFAF7F0),
    dark = false
)

val LocalNorthborn = staticCompositionLocalOf { NorthbornDarkPalette }

object NorthbornSpace {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val page = 20.dp
    val touchMin = 48.dp
}

object NorthbornShape {
    val xs = CutCornerShape(3.dp)
    val sm = CutCornerShape(4.dp)
    val md = CutCornerShape(6.dp)
    val lg = CutCornerShape(8.dp)
    val button = CutCornerShape(6.dp)
    val pill = CutCornerShape(5.dp)
}

/**
 * 8 materiais escandinavos autênticos em northborn_materials_atlas.webp (grade 4×2).
 */
enum class NorthbornMaterial(val col: Int, val row: Int, val label: String) {
    NordicAsh(0, 0, "Freixo Nórdico"),
    SmokedOak(1, 0, "Carvalho Defumado"),
    ForgedIron(2, 0, "Ferro Forjado"),
    AgedBronze(3, 0, "Bronze Envelhecido"),
    OxidizedSilver(0, 1, "Prata Oxidada"),
    SaddleLeather(1, 1, "Couro Escandinavo"),
    ArchivalVellum(2, 1, "Velino Arquivístico"),
    VolcanicSlate(3, 1, "Ardósia Vulcânica")
}

/**
 * 5 categorias semânticas de superfície do NORTHBORN 4.0.
 */
enum class NorthbornSurfaceRole {
    Operational,  // Tarefas, formulários, controles diários — ruído mínimo
    Document,     // Biblioteca, Codex, objetivos, crônicas — velino e leitura
    Strategic,    // Mesa de Odin, Caminhos, Agenda — madeira/ardósia e precisão
    Ceremonial,   // Conquistas, perfil heroico, marcos — metal nobre contido
    Informative   // Métricas, alertas e estados — leve, sem caixa pesada
}

/**
 * 7 níveis hierárquicos de botão do NORTHBORN 4.0.
 */
enum class NorthbornButtonRole {
    Primary,
    Secondary,
    Tertiary,
    Contextual,
    Ceremonial,
    Confirm,
    Destructive
}

enum class NorthbornDividerStyle {
    Hairline,
    DiamondKnot,
    DoubleKnot
}

/**
 * Iconografia arquitetônica escandinava de traço uniforme (1.75 dp), inspirada na prancha NORTHBORN.
 */
enum class NorthbornGlyph {
    Journey,
    Habits,
    Missions,
    World,
    Hero,
    Odin,
    TimeForge,
    CouncilNine,
    Agenda,
    Paths,
    Library,
    Settings,
    Back,
    Add,
    Check
}

/**
 * Desenha uma célula do atlas northborn_materials_atlas.webp (4×2) com discrição material.
 */
fun DrawScope.drawNorthbornMaterialGrain(
    atlas: ImageBitmap,
    material: NorthbornMaterial,
    alpha: Float
) {
    if (alpha <= 0f) return
    val cw = atlas.width / 4
    val ch = atlas.height / 2
    drawImage(
        image = atlas,
        srcOffset = IntOffset(material.col * cw, material.row * ch),
        srcSize = IntSize(cw, ch),
        dstOffset = IntOffset.Zero,
        dstSize = IntSize(size.width.toInt().coerceAtLeast(1), size.height.toInt().coerceAtLeast(1)),
        alpha = alpha,
        filterQuality = FilterQuality.Medium
    )
}

/**
 * Superfície estrutural NORTHBORN 4.0 com taxonomia de 5 papéis, materialidade sutil e zero tags automáticas.
 */
@Composable
fun NorthbornSurface(
    role: NorthbornSurfaceRole = NorthbornSurfaceRole.Operational,
    modifier: Modifier = Modifier,
    material: NorthbornMaterial? = null,
    accent: Color? = null,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = NorthbornSpace.lg,
        vertical = if (role == NorthbornSurfaceRole.Informative) NorthbornSpace.md else NorthbornSpace.lg
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    val nb = LocalNorthborn.current
    val minimal = LocalAppearance.current.minimal
    val resolvedMaterial = material ?: when (role) {
        NorthbornSurfaceRole.Operational -> NorthbornMaterial.ForgedIron
        NorthbornSurfaceRole.Document -> NorthbornMaterial.ArchivalVellum
        NorthbornSurfaceRole.Strategic -> NorthbornMaterial.SmokedOak
        NorthbornSurfaceRole.Ceremonial -> NorthbornMaterial.AgedBronze
        NorthbornSurfaceRole.Informative -> NorthbornMaterial.VolcanicSlate
    }
    val resolvedAccent = accent ?: when (role) {
        NorthbornSurfaceRole.Ceremonial -> nb.accentBronze
        NorthbornSurfaceRole.Strategic -> nb.accentSilver
        NorthbornSurfaceRole.Document -> nb.accentBronze
        NorthbornSurfaceRole.Informative -> nb.accentFjord
        NorthbornSurfaceRole.Operational -> nb.hairlineStrong
    }
    val containerColor = when (role) {
        NorthbornSurfaceRole.Operational -> nb.surfaceBase
        NorthbornSurfaceRole.Document -> nb.surfaceDocument
        NorthbornSurfaceRole.Strategic -> if (nb.dark) nb.surfaceTinted else nb.surfaceBase
        NorthbornSurfaceRole.Ceremonial -> nb.surfaceElevated
        NorthbornSurfaceRole.Informative -> nb.surfaceBase.copy(alpha = if (nb.dark) 0.72f else 0.80f)
    }
    val grainAlpha = when {
        minimal -> 0f
        role == NorthbornSurfaceRole.Informative -> 0f
        role == NorthbornSurfaceRole.Operational -> if (nb.dark) 0.06f else 0.04f
        role == NorthbornSurfaceRole.Document -> if (nb.dark) 0.09f else 0.08f
        role == NorthbornSurfaceRole.Strategic -> if (nb.dark) 0.11f else 0.06f
        else -> if (nb.dark) 0.14f else 0.09f
    }
    val shape = when (role) {
        NorthbornSurfaceRole.Informative -> NorthbornShape.sm
        NorthbornSurfaceRole.Ceremonial -> NorthbornShape.lg
        else -> NorthbornShape.md
    }
    val borderColor = when (role) {
        NorthbornSurfaceRole.Informative -> nb.hairline
        NorthbornSurfaceRole.Ceremonial -> resolvedAccent.copy(alpha = if (nb.dark) 0.42f else 0.46f)
        NorthbornSurfaceRole.Document -> nb.hairlineStrong.copy(alpha = if (nb.dark) 0.56f else 0.78f)
        else -> nb.hairlineStrong.copy(alpha = if (nb.dark) 0.46f else 0.66f)
    }
    val atlas = if (!minimal && grainAlpha > 0f) ImageBitmap.imageResource(R.drawable.northborn_materials_atlas) else null

    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor)
            .drawBehind {
                if (atlas != null) {
                    drawNorthbornMaterialGrain(atlas, resolvedMaterial, grainAlpha)
                }
                val d = 1.dp.toPx()
                // Subtle architectural vertical depth on Strategic/Ceremonial surfaces
                if (!minimal && (role == NorthbornSurfaceRole.Strategic || role == NorthbornSurfaceRole.Ceremonial)) {
                    drawRect(
                        Brush.verticalGradient(
                            0f to (if (nb.dark) Color.White.copy(alpha = 0.03f) else Color.White.copy(alpha = 0.25f)),
                            1f to (if (nb.dark) Color.Black.copy(alpha = 0.14f) else nb.surfaceElevated.copy(alpha = 0.18f))
                        )
                    )
                }
                // Filete sutil de luz arquitetônica no topo
                if (role != NorthbornSurfaceRole.Informative) {
                    drawLine(
                        color = if (nb.dark) Color.White.copy(alpha = 0.075f) else Color.White.copy(alpha = 0.65f),
                        start = Offset(10 * d, d),
                        end = Offset(size.width - 10 * d, d),
                        strokeWidth = d
                    )
                }
                // Lombada discreta à esquerda em superfícies documentais e informativas
                if (role == NorthbornSurfaceRole.Document || role == NorthbornSurfaceRole.Informative) {
                    drawRect(
                        color = resolvedAccent.copy(alpha = if (role == NorthbornSurfaceRole.Document) 0.52f else 0.64f),
                        topLeft = Offset(0f, 8 * d),
                        size = Size(2.5f * d, (size.height - 16 * d).coerceAtLeast(0f))
                    )
                }
                // Detalhe geométrico discreto central no topo em superfícies cerimoniais
                if (role == NorthbornSurfaceRole.Ceremonial && !minimal) {
                    val cx = size.width * 0.5f
                    drawLine(resolvedAccent.copy(alpha = 0.55f), Offset(cx - 26 * d, 1.5f * d), Offset(cx + 26 * d, 1.5f * d), 1.4f * d)
                    val dm = Path().apply {
                        moveTo(cx, 0.5f * d); lineTo(cx + 3 * d, 3.5f * d); lineTo(cx, 6.5f * d); lineTo(cx - 3 * d, 3.5f * d); close()
                    }
                    drawPath(dm, resolvedAccent.copy(alpha = 0.72f))
                }
            }
            .border(1.dp, borderColor, shape)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(NorthbornSpace.md)
    ) {
        CompositionLocalProvider(LocalContentColor provides nb.inkPrimary) {
            content()
        }
    }
}

/**
 * Divisor geométrico escandinavo (Hairline / DiamondKnot / DoubleKnot) inspirado na prancha NORTHBORN.
 */
@Composable
fun NorthbornDivider(
    style: NorthbornDividerStyle = NorthbornDividerStyle.Hairline,
    modifier: Modifier = Modifier,
    color: Color? = null,
    rune: HistoricalFutharkRune? = null
) {
    val nb = LocalNorthborn.current
    val minimal = LocalAppearance.current.minimal
    val lineColor = color ?: nb.hairlineStrong
    val effectiveStyle = if (minimal) NorthbornDividerStyle.Hairline else style

    if (rune != null && !minimal) {
        Row(
            modifier.fillMaxWidth().height(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, lineColor.copy(alpha = 0.65f)))))
            FutharkRuneGlyph(rune = rune, color = color ?: nb.accentBronze, size = 14.dp)
            Box(Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(lineColor.copy(alpha = 0.65f), Color.Transparent))))
        }
        return
    }

    val height = when (effectiveStyle) {
        NorthbornDividerStyle.Hairline -> 1.dp
        NorthbornDividerStyle.DiamondKnot -> 14.dp
        NorthbornDividerStyle.DoubleKnot -> 16.dp
    }
    Canvas(modifier.fillMaxWidth().height(height)) {
        val w = size.width
        val cy = size.height * 0.5f
        val cx = w * 0.5f
        val d = 1.dp.toPx()
        val stroke = 1.dp.toPx()
        when (effectiveStyle) {
            NorthbornDividerStyle.Hairline -> {
                drawLine(
                    Brush.horizontalGradient(listOf(Color.Transparent, lineColor.copy(alpha = 0.72f), lineColor.copy(alpha = 0.72f), Color.Transparent)),
                    Offset(0f, cy), Offset(w, cy), stroke
                )
            }
            NorthbornDividerStyle.DiamondKnot -> {
                val r = 5f * d
                drawLine(Brush.horizontalGradient(listOf(Color.Transparent, lineColor.copy(alpha = 0.7f)), endX = cx - r - 4 * d), Offset(0f, cy), Offset(cx - r - 4 * d, cy), stroke)
                drawLine(Brush.horizontalGradient(listOf(lineColor.copy(alpha = 0.7f), Color.Transparent), startX = cx + r + 4 * d), Offset(cx + r + 4 * d, cy), Offset(w, cy), stroke)
                val p = Path().apply {
                    moveTo(cx, cy - r); lineTo(cx + r * 1.4f, cy); lineTo(cx, cy + r); lineTo(cx - r * 1.4f, cy); close()
                }
                drawPath(p, lineColor.copy(alpha = 0.85f), style = Stroke(1.1f * d))
                drawCircle(lineColor.copy(alpha = 0.75f), 1.6f * d, Offset(cx, cy))
            }
            NorthbornDividerStyle.DoubleKnot -> {
                val r = 5.5f * d
                val gap = 7f * d
                drawLine(Brush.horizontalGradient(listOf(Color.Transparent, lineColor.copy(alpha = 0.65f)), endX = cx - gap - r - 4 * d), Offset(0f, cy), Offset(cx - gap - r - 4 * d, cy), stroke)
                drawLine(Brush.horizontalGradient(listOf(lineColor.copy(alpha = 0.65f), Color.Transparent), startX = cx + gap + r + 4 * d), Offset(cx + gap + r + 4 * d, cy), Offset(w, cy), stroke)
                listOf(-gap * 0.6f, gap * 0.6f).forEach { dx ->
                    val p = Path().apply {
                        moveTo(cx + dx, cy - r); lineTo(cx + dx + r * 1.15f, cy); lineTo(cx + dx, cy + r); lineTo(cx + dx - r * 1.15f, cy); close()
                    }
                    drawPath(p, lineColor.copy(alpha = 0.82f), style = Stroke(1.1f * d))
                }
            }
        }
    }
}

@Composable
fun NorthbornButton(
    text: String,
    onClick: () -> Unit,
    role: NorthbornButtonRole = NorthbornButtonRole.Primary,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    completed: Boolean = false,
    selected: Boolean = false,
    leadingRune: HistoricalFutharkRune? = null,
    leadingIcon: NorthbornGlyph? = null
) {
    val nb = LocalNorthborn.current
    val appearance = LocalAppearance.current
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val pressure = craftPress(interaction)

    val effectiveRole = if (completed) NorthbornButtonRole.Confirm else role
    val isCeremonial = effectiveRole == NorthbornButtonRole.Ceremonial
    val isTertiary = effectiveRole == NorthbornButtonRole.Tertiary
    val isContextual = effectiveRole == NorthbornButtonRole.Contextual
    val plate = forgedPlate(effectiveRole, enabled)

    val containerBg = when {
        !enabled -> if (nb.dark) Color(0xFF191D23) else Color(0xFFE7E1D6)
        effectiveRole == NorthbornButtonRole.Primary -> if (nb.dark) Color(0xFF453423) else Color(0xFF564028)
        effectiveRole == NorthbornButtonRole.Ceremonial -> if (nb.dark) Color(0xFF1B1F27) else Color(0xFF21252D)
        effectiveRole == NorthbornButtonRole.Confirm -> if (nb.dark) Color(0xFF203A2E) else Color(0xFF234D38)
        effectiveRole == NorthbornButtonRole.Destructive -> if (nb.dark) Color(0xFF49231F) else Color(0xFF8C3930)
        effectiveRole == NorthbornButtonRole.Secondary -> if (nb.dark) Color(0xFF222730) else Color(0xFFECE5D8)
        effectiveRole == NorthbornButtonRole.Contextual -> if (nb.dark) Color(0xFF1B2028) else Color(0xFFF1EAE0)
        else -> Color.Transparent // Tertiary
    }
    val contentFg = when {
        !enabled -> nb.inkTertiary
        effectiveRole == NorthbornButtonRole.Primary -> Color(0xFFFAF5EA)
        effectiveRole == NorthbornButtonRole.Ceremonial -> Color(0xFFF5E9D1)
        effectiveRole == NorthbornButtonRole.Confirm -> Color(0xFFEFF8F2)
        effectiveRole == NorthbornButtonRole.Destructive -> Color(0xFFFAECE9)
        effectiveRole == NorthbornButtonRole.Secondary -> nb.inkPrimary
        effectiveRole == NorthbornButtonRole.Contextual -> if (selected) nb.accentBronze else nb.inkPrimary
        else -> if (selected) nb.accentBronze else nb.inkSecondary
    }
    val borderColor = when {
        !enabled -> nb.hairlineStrong.copy(alpha = 0.65f)
        focused -> nb.accentPaleGold
        selected -> nb.accentBronze
        effectiveRole == NorthbornButtonRole.Ceremonial -> nb.accentBronze.copy(alpha = 0.82f)
        effectiveRole == NorthbornButtonRole.Primary -> nb.accentBronze.copy(alpha = if (nb.dark) 0.72f else 0.55f)
        effectiveRole == NorthbornButtonRole.Confirm -> nb.accentPatina.copy(alpha = 0.70f)
        effectiveRole == NorthbornButtonRole.Destructive -> nb.accentEmber.copy(alpha = 0.68f)
        effectiveRole == NorthbornButtonRole.Secondary -> nb.hairlineStrong
        effectiveRole == NorthbornButtonRole.Contextual -> nb.hairlineStrong.copy(alpha = 0.85f)
        else -> nb.hairlineStrong.copy(alpha = 0.65f)
    }
    val shape = if (isContextual) NorthbornShape.pill else NorthbornShape.button
    val minHeight = NorthbornSpace.touchMin

    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        interactionSource = interaction,
        shape = shape,
        contentPadding = PaddingValues(
            horizontal = when {
                isCeremonial -> 24.dp
                isContextual -> 14.dp
                isTertiary -> 12.dp
                else -> 20.dp
            },
            vertical = if (isContextual) 8.dp else 11.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = contentFg,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = if (loading && enabled) contentFg else nb.inkTertiary
        ),
        modifier = modifier
            .heightIn(min = minHeight)
            .clip(shape)
            .background(containerBg)
            .forgedFinish(plate, pressure)
            .then(
                if (plate == null || focused || selected) {
                    Modifier.border(if (focused || selected) 2.dp else 1.dp, borderColor, shape)
                } else Modifier
            )
            .semantics {
                if (selected) this.selected = true
                if (loading) stateDescription = "Em andamento"
            }
    ) {
        if (loading) {
            if (appearance.reducedMotion) {
                Text("…", color = contentFg, fontWeight = FontWeight.Bold)
            } else {
                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = contentFg)
            }
            Spacer(Modifier.width(8.dp))
        } else if (leadingIcon != null) {
            NorthbornIcon(leadingIcon, contentFg, Modifier.graphicsLayer {
                if (!appearance.reducedMotion) translationY = 1.dp.toPx() * pressure.value
            }, size = 16.dp)
            Spacer(Modifier.width(8.dp))
        } else if (leadingRune != null) {
            FutharkRuneGlyph(leadingRune, if (enabled) contentFg.copy(alpha = 0.9f) else nb.inkTertiary, size = 13.dp)
            Spacer(Modifier.width(7.dp))
        }
        val display = when {
            completed -> "✓ $text"
            selected && !text.startsWith("◆") -> "◆ $text"
            else -> text
        }
        Text(
            text = display,
            fontSize = if (isContextual) 12.sp else 14.sp,
            fontWeight = if (isCeremonial || effectiveRole == NorthbornButtonRole.Primary) FontWeight.SemiBold else FontWeight.Medium,
            fontFamily = if (isCeremonial) SagaFont else FontFamily.SansSerif,
            letterSpacing = if (isCeremonial) 0.55.sp else 0.15.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Barra superior editorial unificada para todas as sub-rotas (substitui o Row nu de MainActivity.kt).
 */
@Composable
fun NorthbornTopBar(
    title: String,
    onBack: () -> Unit,
    subtitle: String = "",
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    val nb = LocalNorthborn.current
    Column(Modifier.fillMaxWidth().background(nb.canvas)) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(start = 8.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp).semantics { contentDescription = "Voltar" }
            ) {
                NorthbornIcon(NorthbornGlyph.Back, nb.inkPrimary, size = 20.dp)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = nb.inkPrimary,
                    fontFamily = SagaFont,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotBlank()) {
                    Text(subtitle, color = nb.inkSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (trailing != null) {
                Row(verticalAlignment = Alignment.CenterVertically, content = trailing)
            }
        }
        NorthbornDivider(color = nb.accentBronze.copy(alpha = .35f))
    }
}

/**
 * Cabeçalho editorial de seção sem caixa ao redor (Composição Editorial — Princípio III).
 */
@Composable
fun NorthbornEditorialHeader(
    title: String,
    subtitle: String = "",
    kicker: String = "",
    accent: Color? = null,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    val nb = LocalNorthborn.current
    val kColor = accent ?: nb.accentBronze
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            if (kicker.isNotBlank()) {
                Text(
                    text = kicker.uppercase(),
                    color = kColor,
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = nb.inkPrimary
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    color = nb.inkSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }
        if (action != null) {
            Spacer(Modifier.width(12.dp))
            action()
        }
    }
}

/** Shared application icons use the same engraving geometry as the bottom navigation. */
@Composable
fun NorthbornIcon(
    glyph: NorthbornGlyph,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    filled: Boolean = false
) {
    val engraving = when (glyph) {
        NorthbornGlyph.Journey -> Engraving.Ship
        NorthbornGlyph.Habits -> Engraving.Tally
        NorthbornGlyph.Missions -> Engraving.Banner
        NorthbornGlyph.World -> Engraving.Compass
        NorthbornGlyph.Hero -> Engraving.Shield
        NorthbornGlyph.Odin, NorthbornGlyph.CouncilNine -> Engraving.Council
        NorthbornGlyph.TimeForge -> Engraving.Forge
        NorthbornGlyph.Agenda -> Engraving.Calendar
        NorthbornGlyph.Paths -> Engraving.Cairns
        NorthbornGlyph.Library -> Engraving.Folio
        NorthbornGlyph.Settings -> Engraving.Settings
        NorthbornGlyph.Back -> Engraving.Back
        NorthbornGlyph.Add -> Engraving.Add
        NorthbornGlyph.Check -> Engraving.Check
    }
    EngravedIcon(engraving, color, modifier, size, raised = filled && !LocalAppearance.current.minimal)
}
