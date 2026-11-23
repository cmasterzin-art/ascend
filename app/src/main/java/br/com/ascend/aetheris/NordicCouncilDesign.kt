package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*

/**
 * ASCEND 1.7.5.1 — Design System 3.2.1 (Nordic Council Refined)
 * Evolução da materialidade nórdica e viking sobre a base 3.2:
 * - 6 famílias de botões metálicos em restored_council_buttons_atlas.webp
 * - 8 marcadores de planejamento em nordic_planning_marks_atlas.webp
 * - 8 molduras, cantoneiras e divisores Urnes/Borre em nordic_strategy_frames_atlas.webp
 * - 6 selos refinados das estratégias de Odin em odin_council_seals_refined_atlas.webp
 * - 6 instrumentos da Forja do Tempo em time_forge_refinement_atlas.webp
 * - Integração contextual das 24 runas históricas do Elder Futhark sem poluir rótulos funcionais.
 */

enum class CouncilMaterial(val col: Int, val row: Int, val label: String) {
    NordicWood(0, 0, "Madeira Nórdica"),
    AgedVellum(1, 0, "Velino Estratégico"),
    ProjectLeather(2, 0, "Couro de Projeto"),
    HammeredBronze(0, 1, "Bronze Martelado"),
    OxidizedSilver(1, 1, "Prata Oxidada"),
    DarkIron(2, 1, "Ferro Escuro")
}

/**
 * 8 marcadores pictóricos nórdicos de planejamento (nordic_planning_marks_atlas.webp, 4×2).
 */
enum class PlanningMark(val col: Int, val row: Int, val label: String, val fallbackRune: HistoricalFutharkRune) {
    CriticalTask(0, 0, "Tarefa crítica", HistoricalFutharkRune.Tiwaz),
    Dependency(1, 0, "Dependência", HistoricalFutharkRune.Gebo),
    Milestone(2, 0, "Marco", HistoricalFutharkRune.Sowilo),
    Deadline(3, 0, "Prazo", HistoricalFutharkRune.Nauthiz),
    Conflict(0, 1, "Conflito", HistoricalFutharkRune.Thurisaz),
    Pinned(1, 1, "Atividade fixada", HistoricalFutharkRune.Isa),
    Recurring(2, 1, "Ocorrência recorrente", HistoricalFutharkRune.Jera),
    PlanCompleted(3, 1, "Plano concluído", HistoricalFutharkRune.Fehu)
}

/**
 * 8 recursos arquitetônicos e molduras nórdicas (nordic_strategy_frames_atlas.webp, 4×2).
 */
enum class StrategyFrameAsset(val col: Int, val row: Int, val label: String) {
    OakFrame(0, 0, "Moldura de Carvalho"),
    LeatherFrame(1, 0, "Moldura de Couro"),
    BronzeCorner(2, 0, "Cantoneira de Bronze"),
    IronCorner(3, 0, "Cantoneira de Ferro"),
    VellumDivider(0, 1, "Divisor de Velino"),
    UrnesDivider(1, 1, "Entrelaçado Urnes"),
    BorreDivider(2, 1, "Entrelaçado Borre"),
    RunicHeaderPlaque(3, 1, "Placa Rúnica de Cabeçalho")
}

@Composable
fun CouncilAtlasCell(
    resId: Int,
    cols: Int,
    rows: Int,
    col: Int,
    row: Int,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    colorFilter: ColorFilter? = null
) {
    val bmp = ImageBitmap.imageResource(resId)
    Canvas(modifier) {
        val cw = bmp.width / cols
        val ch = bmp.height / rows
        val srcOffset = IntOffset(col.coerceIn(0, cols - 1) * cw, row.coerceIn(0, rows - 1) * ch)
        val srcSize = IntSize(cw, ch)
        drawImage(
            image = bmp,
            srcOffset = srcOffset,
            srcSize = srcSize,
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(size.width.toInt().coerceAtLeast(1), size.height.toInt().coerceAtLeast(1)),
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = FilterQuality.Medium
        )
    }
}

/**
 * Marcador pictórico nórdico para planejamento, Agenda 3.0, Gantt e Kanban.
 * Em modo minimalista ou escala muito reduzida (< 14.dp), usa runa histórica Elder Futhark.
 */
@Composable
fun NordicPlanningMark(
    mark: PlanningMark,
    modifier: Modifier = Modifier.size(22.dp),
    alpha: Float = 1f,
    tintFallback: Color = Gold
) {
    val minimal = LocalAppearance.current.minimal
    if (minimal) {
        FutharkRuneGlyph(mark.fallbackRune, tintFallback, modifier, size = 18.dp)
    } else {
        CouncilAtlasCell(
            resId = R.drawable.nordic_planning_marks_atlas,
            cols = 4,
            rows = 2,
            col = mark.col,
            row = mark.row,
            modifier = modifier,
            alpha = alpha
        )
    }
}

/**
 * Recurso arquitetônico ou divisor do atlas nordic_strategy_frames_atlas.webp (4×2).
 */
@Composable
fun StrategyFrameGlyph(
    asset: StrategyFrameAsset,
    modifier: Modifier = Modifier,
    alpha: Float = 1f
) {
    CouncilAtlasCell(
        resId = R.drawable.nordic_strategy_frames_atlas,
        cols = 4,
        rows = 2,
        col = asset.col,
        row = asset.row,
        modifier = modifier,
        alpha = alpha
    )
}

/**
 * Divisor horizontal escandinavo NORTHBORN 4.0: vetores geométricos limpos (sem estiramento raster).
 */
@Composable
fun NordicStrategyDivider(
    style: StrategyFrameAsset = StrategyFrameAsset.UrnesDivider,
    rune: HistoricalFutharkRune? = null,
    accent: Color = LocalNorthborn.current.accentBronze,
    modifier: Modifier = Modifier
) {
    val nbStyle = when (style) {
        StrategyFrameAsset.BorreDivider -> NorthbornDividerStyle.DoubleKnot
        StrategyFrameAsset.VellumDivider -> NorthbornDividerStyle.Hairline
        else -> NorthbornDividerStyle.DiamondKnot
    }
    NorthbornDivider(
        style = nbStyle,
        modifier = modifier,
        color = accent.copy(alpha = 0.65f),
        rune = rune
    )
}

/**
 * Cabeçalho editorial de seção NORTHBORN 4.0: hierarquia limpa sem sobrecarga de maiúsculas ou ícones ruidosos.
 */
@Composable
fun NordicSectionHeader(
    eyebrow: String,
    subtitle: String = "",
    rune: HistoricalFutharkRune? = null,
    mark: PlanningMark? = null,
    accent: Color = LocalNorthborn.current.accentBronze,
    modifier: Modifier = Modifier
) {
    val nb = LocalNorthborn.current
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        when {
            mark != null -> NordicPlanningMark(mark = mark, modifier = Modifier.size(22.dp), tintFallback = accent)
            rune != null -> FutharkRuneGlyph(rune = rune, color = accent.copy(alpha = 0.88f), size = 16.dp)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Eyebrow(eyebrow, accent)
            if (subtitle.isNotBlank()) {
                Text(subtitle, color = nb.inkSecondary, fontSize = 12.5.sp, lineHeight = 18.sp)
            }
        }
    }
}

/**
 * Ação compacta contextual NORTHBORN 4.0 para cartões e linhas (Kanban, Agenda, Conselho dos Nove).
 */
@Composable
fun NordicCompactAction(
    text: String,
    onClick: () -> Unit,
    accent: Color = LocalNorthborn.current.accentBronze,
    rune: HistoricalFutharkRune? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val nb = LocalNorthborn.current
    val shape = NorthbornShape.pill
    val bg = when {
        !enabled -> if (nb.dark) Color(0xFF171A1F) else Color(0xFFE7E2D8)
        nb.dark -> nb.surfaceElevated
        else -> nb.surfaceBase
    }
    val fg = if (enabled) nb.inkPrimary else nb.inkTertiary
    val plate = forgedPlate(NorthbornButtonRole.Contextual, enabled)
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val press = craftPress(interaction)
    val edge = when {
        focused -> nb.accentPaleGold
        enabled -> accent.copy(alpha = .48f)
        else -> nb.hairline
    }
    Row(
        modifier
            .heightIn(min = NorthbornSpace.touchMin)
            .widthIn(min = NorthbornSpace.touchMin)
            .clip(shape)
            .background(bg)
            .forgedFinish(plate, press, opacity = .74f)
            .border(if (focused) 2.dp else 1.dp, edge, shape)
            .clickable(
                interactionSource = interaction, indication = LocalIndication.current,
                enabled = enabled, role = Role.Button, onClick = onClick
            )
            .padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
    ) {
        if (rune != null) {
            FutharkRuneGlyph(rune = rune, color = if (enabled) accent else fg, size = 12.dp)
        }
        Text(
            text = text,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Superfície estratégica NORTHBORN 4.0: delega para NorthbornSurface usando os 8 materiais
 * escandinavos foscos de northborn_materials_atlas.webp sem cantoneiras repetidas em todo cartão.
 */
@Composable
fun CouncilMaterialSurface(
    material: CouncilMaterial = CouncilMaterial.NordicWood,
    modifier: Modifier = Modifier,
    accent: Color = LocalNorthborn.current.accentBronze,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    ornamentCorners: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val nbMat = when (material) {
        CouncilMaterial.NordicWood -> NorthbornMaterial.SmokedOak
        CouncilMaterial.AgedVellum -> NorthbornMaterial.ArchivalVellum
        CouncilMaterial.ProjectLeather -> NorthbornMaterial.SaddleLeather
        CouncilMaterial.HammeredBronze -> NorthbornMaterial.AgedBronze
        CouncilMaterial.OxidizedSilver -> NorthbornMaterial.OxidizedSilver
        CouncilMaterial.DarkIron -> NorthbornMaterial.ForgedIron
    }
    val nbRole = when (material) {
        CouncilMaterial.AgedVellum -> NorthbornSurfaceRole.Document
        CouncilMaterial.HammeredBronze -> NorthbornSurfaceRole.Ceremonial
        CouncilMaterial.DarkIron -> NorthbornSurfaceRole.Operational
        else -> NorthbornSurfaceRole.Strategic
    }
    NorthbornSurface(
        role = nbRole,
        modifier = modifier,
        material = nbMat,
        accent = accent,
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun OdinHallHeroBanner(
    title: String,
    subtitle: String,
    activeStrategyName: String,
    activeStrategy: OdinStrategyDef? = null,
    modifier: Modifier = Modifier
) {
    val nb = LocalNorthborn.current
    val minimal = LocalAppearance.current.minimal
    val shape = NorthbornShape.lg
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 152.dp)
            .clip(shape)
            .background(if (nb.dark) Color(0xFF15181E) else Color(0xFF202328))
            .border(1.dp, nb.accentBronze.copy(alpha = 0.52f), shape)
    ) {
        if (!minimal) {
            Image(
                bitmap = ImageBitmap.imageResource(R.drawable.odin_council_hall),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize().alpha(0.68f)
            )
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x66101318),
                            Color(0xDC111419)
                        )
                    )
                )
            )
        }
        Column(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                NorthbornIcon(NorthbornGlyph.Odin, Color(0xFFD8BE8F), size = 15.dp)
                Text(
                    "SALÃO DO CONSELHO",
                    color = Color(0xFFD8BE8F),
                    fontSize = 10.5.sp,
                    letterSpacing = 1.4.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFF3EEE4)
            )
            Text(
                subtitle,
                color = Color(0xFFC8CFD8),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
            Spacer(Modifier.height(2.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(NorthbornShape.sm)
                    .background(Color(0x9912151B))
                    .border(1.dp, Color(0x44D8BE8F), NorthbornShape.sm)
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (activeStrategy != null && !minimal) {
                    OdinStrategyMedal(activeStrategy, Modifier.size(26.dp))
                } else {
                    NorthbornIcon(NorthbornGlyph.Odin, Color(0xFFD8BE8F), size = 16.dp)
                }
                Text(
                    "Estratégia ativa: $activeStrategyName",
                    color = Color(0xFFEAD5AA),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Selo metálico refinado das 6 Estratégias de Odin (odin_council_seals_refined_atlas.webp, 3×2).
 */
@Composable
fun OdinStrategyMedal(strategy: OdinStrategyDef, modifier: Modifier = Modifier.size(48.dp)) {
    val minimal = LocalAppearance.current.minimal
    if (minimal) {
        val rune = when (strategy.id) {
            "guardian" -> HistoricalFutharkRune.Algiz
            "scholar" -> HistoricalFutharkRune.Ansuz
            "berserker" -> HistoricalFutharkRune.Uruz
            "strategist" -> HistoricalFutharkRune.Tiwaz
            "explorer" -> HistoricalFutharkRune.Raido
            else -> HistoricalFutharkRune.Kenaz
        }
        Box(modifier, contentAlignment = Alignment.Center) {
            FutharkRuneGlyph(rune, Gold, size = 22.dp)
        }
    } else {
        CouncilAtlasCell(
            resId = R.drawable.odin_council_seals_refined_atlas,
            cols = 3,
            rows = 2,
            col = strategy.atlasCol,
            row = strategy.atlasRow,
            modifier = modifier
        )
    }
}

@Composable
fun LifeAreaSeal(sealIndex: Int, modifier: Modifier = Modifier.size(44.dp)) {
    val idx = sealIndex.coerceIn(0, 8)
    CouncilAtlasCell(
        resId = R.drawable.nine_council_seals_atlas,
        cols = 3,
        rows = 3,
        col = idx % 3,
        row = idx / 3,
        modifier = modifier
    )
}

/**
 * 6 instrumentos metálicos refinados da Forja do Tempo (time_forge_refinement_atlas.webp, 3×2).
 */
@Composable
fun TimeForgeRefinedPiece(col: Int, row: Int, modifier: Modifier = Modifier.size(168.dp), alpha: Float = 1f) {
    CouncilAtlasCell(
        resId = R.drawable.time_forge_refinement_atlas,
        cols = 3,
        rows = 2,
        col = col,
        row = row,
        modifier = modifier,
        alpha = alpha
    )
}

@Composable
fun TimeForgeInstrument(col: Int, row: Int, modifier: Modifier = Modifier.size(168.dp), alpha: Float = 1f) {
    val mappedCol = if (row == 1) 1 else col.coerceIn(0, 2)
    TimeForgeRefinedPiece(col = mappedCol, row = 0, modifier = modifier, alpha = alpha)
}

@Composable
fun ImmersiveFocusScene(col: Int, row: Int, modifier: Modifier = Modifier, alpha: Float = 0.55f) {
    CouncilAtlasCell(
        resId = R.drawable.nordic_focus_scenes_atlas,
        cols = 2,
        rows = 2,
        col = col,
        row = row,
        modifier = modifier,
        alpha = alpha
    )
}

@Composable
fun RunicCalendarGlyph(col: Int, row: Int, modifier: Modifier = Modifier.size(22.dp)) {
    CouncilAtlasCell(
        resId = R.drawable.runic_calendar_atlas,
        cols = 3,
        rows = 2,
        col = col,
        row = row,
        modifier = modifier
    )
}

@Composable
fun StrategyControlGlyph(col: Int, row: Int, modifier: Modifier = Modifier.size(20.dp)) {
    CouncilAtlasCell(
        resId = R.drawable.nordic_strategy_controls_atlas,
        cols = 4,
        rows = 2,
        col = col,
        row = row,
        modifier = modifier
    )
}

@Composable
fun OdinCouncilOrnament(col: Int, row: Int, modifier: Modifier = Modifier.size(22.dp), alpha: Float = 1f) {
    CouncilAtlasCell(
        resId = R.drawable.odin_council_ornaments_atlas,
        cols = 4,
        rows = 2,
        col = col,
        row = row,
        modifier = modifier,
        alpha = alpha
    )
}

@Composable
fun CouncilDivider(modifier: Modifier = Modifier) {
    NorthbornDivider(NorthbornDividerStyle.DiamondKnot, modifier = modifier)
}

/**
 * Célula informativa de métrica NORTHBORN 4.0: leve, sem caixa pesada, com filete de acento à esquerda.
 */
@Composable
fun CouncilKpiTile(
    label: String,
    value: String,
    detail: String = "",
    accent: Color = LocalNorthborn.current.accentBronze,
    modifier: Modifier = Modifier,
    rune: HistoricalFutharkRune? = null
) {
    val nb = LocalNorthborn.current
    val shape = NorthbornShape.sm
    Column(
        modifier
            .clip(shape)
            .background(nb.surfaceBase)
            .border(1.dp, nb.hairline, shape)
            .drawBehind {
                val d = 1.dp.toPx()
                drawRect(accent.copy(alpha = 0.78f), Offset(0f, 8 * d), androidx.compose.ui.geometry.Size(2.5f * d, (size.height - 16 * d).coerceAtLeast(0f)))
            }
            .padding(start = 13.dp, end = 11.dp, top = 10.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                label.uppercase(),
                color = nb.inkSecondary,
                fontSize = 10.sp,
                letterSpacing = 0.9.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (rune != null) {
                FutharkRuneGlyph(rune = rune, color = accent.copy(alpha = 0.72f), size = 12.dp)
            }
        }
        Text(value, color = accent, fontSize = 21.sp, fontFamily = SagaFont, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (detail.isNotBlank()) Text(detail, color = nb.inkSecondary, fontSize = 11.5.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
