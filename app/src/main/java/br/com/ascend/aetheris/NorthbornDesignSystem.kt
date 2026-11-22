package br.com.ascend.aetheris

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
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
 * - Sistema de Botões 4.0 em 7 níveis hierárquicos, sem adesivos rasterizados sobre Material.
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
    canvas = Color(0xFF121417),          // Deep Fjord Charcoal
    surfaceBase = Color(0xFF181B1F),     // Matte Forged Iron
    surfaceElevated = Color(0xFF20242A), // Brushed Slate
    surfaceDocument = Color(0xFF1C1A16), // Aged Smoked Oak / Dark Vellum
    surfaceTinted = Color(0xFF161A20),   // Fjord Night Slate
    inkPrimary = Color(0xFFEFEAE0),      // Unbleached Ivory
    inkSecondary = Color(0xFFA7B0BA),    // Weathered Pewter
    inkTertiary = Color(0xFF7B838E),     // Ash Stone
    accentBronze = Color(0xFFBC9B6A),    // Aged Burnished Bronze
    accentPaleGold = Color(0xFFD8BE8F),  // Pale Nordic Gold (ceremonial)
    accentSilver = Color(0xFFB7C2CE),    // Oxidized Silver
    accentFjord = Color(0xFF84A4C4),     // Boreal Fjord Blue
    accentPatina = Color(0xFF83A88E),    // Verdigris Patina Green
    accentEmber = Color(0xFFCC7E72),     // Muted Forge Ember
    hairline = Color(0xFF282D34),        // Quiet architectural seam
    hairlineStrong = Color(0xFF3A414C),  // Crisp structural border
    onAccent = Color(0xFF14110D),
    dark = true
)

val NorthbornLightPalette = NorthbornPalette(
    canvas = Color(0xFFF2EDE3),          // Warm Unbleached Linen
    surfaceBase = Color(0xFFFAF6EE),     // Archival Ivory Paper
    surfaceElevated = Color(0xFFEAE3D5), // Pale Carved Oak
    surfaceDocument = Color(0xFFF6EFE0), // Manuscript Vellum
    surfaceTinted = Color(0xFFE8ECEF),   // Cold Mineral Mist
    inkPrimary = Color(0xFF1E2228),      // Iron-Gall Ink
    inkSecondary = Color(0xFF515A65),    // Weathered Slate Ink
    inkTertiary = Color(0xFF77808A),     // Muted Ash
    accentBronze = Color(0xFF856232),    // Deep Burnished Bronze (AAA on linen)
    accentPaleGold = Color(0xFF6B4D21),  // Dark Ceremonial Bronze
    accentSilver = Color(0xFF525E6C),    // Forged Pewter
    accentFjord = Color(0xFF2E587A),     // Deep Fjord Blue
    accentPatina = Color(0xFF296648),    // Nordic Pine Patina
    accentEmber = Color(0xFF9C4236),     // Iron-Oxide Red
    hairline = Color(0xFFD9D1C2),        // Quiet archival rule
    hairlineStrong = Color(0xFFB8AEA0),  // Crisp linen seam
    onAccent = Color(0xFFFAF6EE),
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
        NorthbornSurfaceRole.Ceremonial -> resolvedAccent.copy(alpha = if (nb.dark) 0.44f else 0.50f)
        NorthbornSurfaceRole.Document -> nb.hairlineStrong.copy(alpha = if (nb.dark) 0.65f else 0.85f)
        else -> nb.hairlineStrong.copy(alpha = if (nb.dark) 0.52f else 0.72f)
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
                // Filete sutil de luz arquitetônica no topo
                if (role != NorthbornSurfaceRole.Informative) {
                    drawLine(
                        color = if (nb.dark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.55f),
                        start = Offset(10 * d, d),
                        end = Offset(size.width - 10 * d, d),
                        strokeWidth = d
                    )
                }
                // Lombada discreta à esquerda em superfícies documentais e informativas
                if (role == NorthbornSurfaceRole.Document || role == NorthbornSurfaceRole.Informative) {
                    drawRect(
                        color = resolvedAccent.copy(alpha = if (role == NorthbornSurfaceRole.Document) 0.55f else 0.68f),
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

/**
 * Botão arquitetônico NORTHBORN 4.0 em 7 níveis hierárquicos.
 * Substitui adesivos rasterizados por placas chanfradas limpas com grão mineral discreto,
 * relevo arquitetônico e contraste AA/AAA nos temas escuro, claro e minimalista.
 */
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
    val pressed by interaction.collectIsPressedAsState()
    val focused by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) 0.985f else 1f, tween(if (appearance.reducedMotion) 0 else 80), label = "nb_btn_scale")

    val effectiveRole = if (completed) NorthbornButtonRole.Confirm else role
    val isCeremonial = effectiveRole == NorthbornButtonRole.Ceremonial
    val isTertiary = effectiveRole == NorthbornButtonRole.Tertiary
    val isContextual = effectiveRole == NorthbornButtonRole.Contextual

    val containerBg = when {
        !enabled -> if (nb.dark) Color(0xFF1A1D22) else Color(0xFFE5DFD4)
        effectiveRole == NorthbornButtonRole.Primary -> if (nb.dark) Color(0xFF5D4B34) else Color(0xFF6B4F2A)
        effectiveRole == NorthbornButtonRole.Ceremonial -> if (nb.dark) Color(0xFF1F1C17) else Color(0xFF28221B)
        effectiveRole == NorthbornButtonRole.Confirm -> if (nb.dark) Color(0xFF263E31) else Color(0xFF2B6448)
        effectiveRole == NorthbornButtonRole.Destructive -> if (nb.dark) Color(0xFF4B2622) else Color(0xFF963D32)
        effectiveRole == NorthbornButtonRole.Secondary -> if (nb.dark) Color(0xFF232830) else Color(0xFFE6DFD2)
        effectiveRole == NorthbornButtonRole.Contextual -> if (nb.dark) Color(0xFF1B1F26) else Color(0xFFEEE8DC)
        else -> Color.Transparent // Tertiary
    }
    val contentFg = when {
        !enabled -> nb.inkTertiary
        effectiveRole == NorthbornButtonRole.Primary -> Color(0xFFF8F3E8)
        effectiveRole == NorthbornButtonRole.Ceremonial -> Color(0xFFF3E6CE)
        effectiveRole == NorthbornButtonRole.Confirm -> Color(0xFFEFF7F1)
        effectiveRole == NorthbornButtonRole.Destructive -> Color(0xFFF9EAE8)
        effectiveRole == NorthbornButtonRole.Secondary -> nb.inkPrimary
        effectiveRole == NorthbornButtonRole.Contextual -> if (selected) nb.accentBronze else nb.inkPrimary
        else -> if (selected) nb.accentBronze else nb.inkSecondary
    }
    val borderColor = when {
        !enabled -> nb.hairline
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
    val material = when (effectiveRole) {
        NorthbornButtonRole.Primary, NorthbornButtonRole.Ceremonial -> NorthbornMaterial.AgedBronze
        NorthbornButtonRole.Secondary -> NorthbornMaterial.ForgedIron
        NorthbornButtonRole.Confirm -> NorthbornMaterial.VolcanicSlate
        else -> null
    }
    val grainAlpha = when {
        appearance.minimal || !enabled || isTertiary || isContextual -> 0f
        isCeremonial -> 0.14f
        effectiveRole == NorthbornButtonRole.Primary -> 0.11f
        effectiveRole == NorthbornButtonRole.Secondary -> if (nb.dark) 0.08f else 0.04f
        else -> 0.06f
    }
    val atlas = if (material != null && grainAlpha > 0f) ImageBitmap.imageResource(R.drawable.northborn_materials_atlas) else null
    val shape = if (isContextual) NorthbornShape.pill else NorthbornShape.button
    val minHeight = if (isContextual) 42.dp else NorthbornSpace.touchMin

    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        interactionSource = interaction,
        shape = shape,
        contentPadding = PaddingValues(
            horizontal = when {
                isCeremonial -> 22.dp
                isContextual -> 12.dp
                isTertiary -> 12.dp
                else -> 16.dp
            },
            vertical = if (isContextual) 8.dp else 11.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = contentFg,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = nb.inkTertiary
        ),
        modifier = modifier
            .heightIn(min = minHeight)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(containerBg)
            .drawWithCache {
                onDrawBehind {
                    if (atlas != null && material != null) {
                        drawNorthbornMaterialGrain(atlas, material, grainAlpha)
                    }
                    val d = 1.dp.toPx()
                    val w = size.width
                    val h = size.height
                    if (!isTertiary && enabled) {
                        // Sutil brilho arquitetônico superior e sombra inferior (chanfro real sem neon)
                        drawLine(Color.White.copy(alpha = if (nb.dark) 0.13f else 0.22f), Offset(6 * d, d), Offset(w - 6 * d, d), d)
                        drawLine(Color.Black.copy(alpha = if (nb.dark) 0.28f else 0.12f), Offset(6 * d, h - d), Offset(w - 6 * d, h - d), d)
                    }
                    if (isCeremonial && enabled && !appearance.minimal) {
                        // Filete interno bronze + 2 pequenos losangos nas extremidades
                        drawRect(nb.accentBronze.copy(alpha = 0.28f), topLeft = Offset(3.5f * d, 3.5f * d), size = Size(w - 7 * d, h - 7 * d), style = Stroke(0.8f * d))
                        listOf(11f * d, w - 11f * d).forEach { rx ->
                            val dm = Path().apply {
                                moveTo(rx, h * 0.5f - 2.6f * d); lineTo(rx + 2.6f * d, h * 0.5f); lineTo(rx, h * 0.5f + 2.6f * d); lineTo(rx - 2.6f * d, h * 0.5f); close()
                            }
                            drawPath(dm, nb.accentPaleGold.copy(alpha = 0.78f))
                        }
                    }
                    if (selected && enabled) {
                        drawLine(nb.accentBronze, Offset(12 * d, h - 2 * d), Offset(w - 12 * d, h - 2 * d), 1.8f * d)
                    }
                    if (pressed && enabled) {
                        drawRect(Color.Black.copy(alpha = 0.14f))
                    }
                }
            }
            .border(if (focused || selected || isCeremonial) 1.25.dp else 1.dp, borderColor, shape)
            .semantics { if (selected) this.selected = true }
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = contentFg)
            Spacer(Modifier.width(8.dp))
        } else if (leadingIcon != null) {
            NorthbornIcon(leadingIcon, contentFg, size = 16.dp)
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
            letterSpacing = if (isCeremonial) 0.5.sp else 0.15.sp,
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
                .heightIn(min = 54.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                    maxLines = 1,
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
        NorthbornDivider(NorthbornDividerStyle.Hairline)
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
        verticalAlignment = Alignment.Bottom,
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

/**
 * Ícones arquitetônicos escandinavos de traço uniforme (1.75 dp), legíveis de 16 dp a 28 dp em ambos os temas.
 */
@Composable
fun NorthbornIcon(
    glyph: NorthbornGlyph,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    filled: Boolean = false
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val s = Stroke(width = (w * 0.082f).coerceAtLeast(1.4.dp.toPx()), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val thin = Stroke(width = (w * 0.062f).coerceAtLeast(1.1.dp.toPx()), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val fill = color.copy(alpha = if (filled) 0.16f else 0f)
        when (glyph) {
            NorthbornGlyph.Journey -> {
                // Estrela polar / bússola do Norte em losango arquitetônico
                val p = Path().apply {
                    moveTo(w * .5f, h * .10f); lineTo(w * .63f, h * .37f); lineTo(w * .90f, h * .5f)
                    lineTo(w * .63f, h * .63f); lineTo(w * .5f, h * .90f); lineTo(w * .37f, h * .63f)
                    lineTo(w * .10f, h * .5f); lineTo(w * .37f, h * .37f); close()
                }
                if (filled) drawPath(p, fill)
                drawPath(p, color, style = s)
                drawCircle(color, w * .08f, Offset(w * .5f, h * .5f))
            }
            NorthbornGlyph.Habits -> {
                // Folha de carvalho nórdico / eixo de constância
                val leaf = Path().apply {
                    moveTo(w * .5f, h * .12f)
                    lineTo(w * .78f, h * .38f); lineTo(w * .72f, h * .66f); lineTo(w * .5f, h * .80f)
                    lineTo(w * .28f, h * .66f); lineTo(w * .22f, h * .38f); close()
                }
                if (filled) drawPath(leaf, fill)
                drawPath(leaf, color, style = s)
                drawLine(color, Offset(w * .5f, h * .28f), Offset(w * .5f, h * .90f), s.width, StrokeCap.Round)
                drawLine(color, Offset(w * .5f, h * .48f), Offset(w * .65f, h * .38f), thin.width, StrokeCap.Round)
                drawLine(color, Offset(w * .5f, h * .60f), Offset(w * .35f, h * .50f), thin.width, StrokeCap.Round)
            }
            NorthbornGlyph.Missions -> {
                // Pergaminho selado / contrato de juramento
                val body = Path().apply {
                    moveTo(w * .22f, h * .16f); lineTo(w * .78f, h * .16f); lineTo(w * .78f, h * .84f); lineTo(w * .22f, h * .84f); close()
                }
                if (filled) drawPath(body, fill)
                drawPath(body, color, style = s)
                drawLine(color, Offset(w * .34f, h * .36f), Offset(w * .66f, h * .36f), thin.width, StrokeCap.Round)
                drawLine(color, Offset(w * .34f, h * .52f), Offset(w * .60f, h * .52f), thin.width, StrokeCap.Round)
                val seal = Path().apply {
                    moveTo(w * .58f, h * .64f); lineTo(w * .66f, h * .71f); lineTo(w * .58f, h * .78f); lineTo(w * .50f, h * .71f); close()
                }
                drawPath(seal, color, style = thin)
            }
            NorthbornGlyph.World -> {
                // Atlas cartográfico dobrado em três folhas (como na prancha NORTHBORN)
                val map = Path().apply {
                    moveTo(w * .14f, h * .24f); lineTo(w * .38f, h * .16f); lineTo(w * .62f, h * .24f); lineTo(w * .86f, h * .16f)
                    lineTo(w * .86f, h * .76f); lineTo(w * .62f, h * .84f); lineTo(w * .38f, h * .76f); lineTo(w * .14f, h * .84f); close()
                }
                if (filled) drawPath(map, fill)
                drawPath(map, color, style = s)
                drawLine(color, Offset(w * .38f, h * .16f), Offset(w * .38f, h * .76f), thin.width)
                drawLine(color, Offset(w * .62f, h * .24f), Offset(w * .62f, h * .84f), thin.width)
            }
            NorthbornGlyph.Hero -> {
                // Elmo escandinavo Gjermundbu minimalista
                val dome = Path().apply {
                    moveTo(w * .20f, h * .58f)
                    lineTo(w * .24f, h * .28f); lineTo(w * .50f, h * .14f); lineTo(w * .76f, h * .28f)
                    lineTo(w * .80f, h * .58f); lineTo(w * .64f, h * .82f); lineTo(w * .50f, h * .86f); lineTo(w * .36f, h * .82f); close()
                }
                if (filled) drawPath(dome, fill)
                drawPath(dome, color, style = s)
                drawLine(color, Offset(w * .50f, h * .16f), Offset(w * .50f, h * .66f), thin.width, StrokeCap.Round)
                drawLine(color, Offset(w * .24f, h * .52f), Offset(w * .76f, h * .52f), thin.width, StrokeCap.Round)
            }
            NorthbornGlyph.Odin -> {
                // Gungnir / eixo de decisão estratégica com asas geométricas
                drawLine(color, Offset(w * .5f, h * .12f), Offset(w * .5f, h * .88f), s.width, StrokeCap.Round)
                val tip = Path().apply { moveTo(w * .5f, h * .10f); lineTo(w * .62f, h * .30f); lineTo(w * .38f, h * .30f); close() }
                if (filled) drawPath(tip, fill)
                drawPath(tip, color, style = thin)
                drawLine(color, Offset(w * .18f, h * .50f), Offset(w * .50f, h * .38f), s.width, StrokeCap.Round)
                drawLine(color, Offset(w * .82f, h * .50f), Offset(w * .50f, h * .38f), s.width, StrokeCap.Round)
                drawCircle(color, w * .08f, Offset(w * .5f, h * .66f), style = thin)
            }
            NorthbornGlyph.TimeForge -> {
                // Aro temporal / ampulheta astrolábio
                drawCircle(color, w * .38f, Offset(w * .5f, h * .5f), style = s)
                val glass = Path().apply {
                    moveTo(w * .36f, h * .30f); lineTo(w * .64f, h * .30f)
                    lineTo(w * .36f, h * .70f); lineTo(w * .64f, h * .70f); close()
                }
                drawPath(glass, color, style = thin)
            }
            NorthbornGlyph.CouncilNine -> {
                // Triângulo duplo / selo dos nove domínios
                val up = Path().apply { moveTo(w * .5f, h * .14f); lineTo(w * .84f, h * .72f); lineTo(w * .16f, h * .72f); close() }
                val dn = Path().apply { moveTo(w * .5f, h * .86f); lineTo(w * .84f, h * .28f); lineTo(w * .16f, h * .28f); close() }
                drawPath(up, color, style = thin)
                drawPath(dn, color, style = thin)
                drawCircle(color, w * .07f, Offset(w * .5f, h * .5f))
            }
            NorthbornGlyph.Agenda -> {
                drawRoundRect(color, Offset(w * .16f, h * .22f), Size(w * .68f, h * .64f), CornerRadius(2 * s.width), style = s)
                drawLine(color, Offset(w * .16f, h * .40f), Offset(w * .84f, h * .40f), thin.width)
                drawLine(color, Offset(w * .34f, h * .12f), Offset(w * .34f, h * .28f), s.width, StrokeCap.Round)
                drawLine(color, Offset(w * .66f, h * .12f), Offset(w * .66f, h * .28f), s.width, StrokeCap.Round)
            }
            NorthbornGlyph.Paths -> {
                // Trilha de marcos conectados
                drawCircle(color, w * .10f, Offset(w * .24f, h * .74f), style = s)
                drawCircle(color, w * .10f, Offset(w * .52f, h * .48f), style = s)
                drawCircle(color, w * .10f, Offset(w * .78f, h * .24f), style = s)
                drawLine(color, Offset(w * .31f, h * .67f), Offset(w * .45f, h * .55f), thin.width, StrokeCap.Round)
                drawLine(color, Offset(w * .59f, h * .41f), Offset(w * .71f, h * .31f), thin.width, StrokeCap.Round)
            }
            NorthbornGlyph.Library -> {
                val book = Path().apply {
                    moveTo(w * .14f, h * .22f); lineTo(w * .50f, h * .28f); lineTo(w * .86f, h * .22f)
                    lineTo(w * .86f, h * .78f); lineTo(w * .50f, h * .84f); lineTo(w * .14f, h * .78f); close()
                }
                if (filled) drawPath(book, fill)
                drawPath(book, color, style = s)
                drawLine(color, Offset(w * .50f, h * .28f), Offset(w * .50f, h * .84f), thin.width)
            }
            NorthbornGlyph.Settings -> {
                drawCircle(color, w * .22f, Offset(w * .5f, h * .5f), style = s)
                for (i in 0 until 6) {
                    val a = Math.toRadians(i * 60.0)
                    val r1 = w * .28f; val r2 = w * .40f
                    drawLine(
                        color,
                        Offset(w * .5f + (r1 * kotlin.math.cos(a)).toFloat(), h * .5f + (r1 * kotlin.math.sin(a)).toFloat()),
                        Offset(w * .5f + (r2 * kotlin.math.cos(a)).toFloat(), h * .5f + (r2 * kotlin.math.sin(a)).toFloat()),
                        s.width, StrokeCap.Round
                    )
                }
            }
            NorthbornGlyph.Back -> {
                drawLine(color, Offset(w * .76f, h * .5f), Offset(w * .24f, h * .5f), s.width, StrokeCap.Round)
                drawLine(color, Offset(w * .46f, h * .26f), Offset(w * .22f, h * .5f), s.width, StrokeCap.Round)
                drawLine(color, Offset(w * .46f, h * .74f), Offset(w * .22f, h * .5f), s.width, StrokeCap.Round)
            }
            NorthbornGlyph.Add -> {
                drawLine(color, Offset(w * .5f, h * .20f), Offset(w * .5f, h * .80f), s.width, StrokeCap.Round)
                drawLine(color, Offset(w * .20f, h * .5f), Offset(w * .80f, h * .5f), s.width, StrokeCap.Round)
            }
            NorthbornGlyph.Check -> {
                drawLine(color, Offset(w * .20f, h * .52f), Offset(w * .42f, h * .74f), s.width, StrokeCap.Round)
                drawLine(color, Offset(w * .42f, h * .74f), Offset(w * .82f, h * .28f), s.width, StrokeCap.Round)
            }
        }
    }
}
