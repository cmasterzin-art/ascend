package br.com.ascend.aetheris

import androidx.compose.foundation.*
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
 * Divisor horizontal escandinavo (Urnes bronze, Borre prata ou Velino) com runa histórica opcional ao centro.
 */
@Composable
fun NordicStrategyDivider(
    style: StrategyFrameAsset = StrategyFrameAsset.UrnesDivider,
    rune: HistoricalFutharkRune? = null,
    accent: Color = Gold,
    modifier: Modifier = Modifier
) {
    val minimal = LocalAppearance.current.minimal
    if (minimal) {
        SagaDivider(modifier)
        return
    }
    Row(
        modifier.fillMaxWidth().height(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StrategyFrameGlyph(
            asset = style,
            modifier = Modifier.weight(1f).height(14.dp),
            alpha = 0.82f
        )
        if (rune != null) {
            FutharkRuneGlyph(rune = rune, color = accent, size = 15.dp)
            StrategyFrameGlyph(
                asset = style,
                modifier = Modifier.weight(1f).height(14.dp),
                alpha = 0.82f
            )
        }
    }
}

/**
 * Cabeçalho de seção com hierarquia nórdica: marcador pictórico ou runa histórica contextual + título + subtítulo.
 */
@Composable
fun NordicSectionHeader(
    eyebrow: String,
    subtitle: String = "",
    rune: HistoricalFutharkRune? = null,
    mark: PlanningMark? = null,
    accent: Color = Gold,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        when {
            mark != null -> NordicPlanningMark(mark = mark, modifier = Modifier.size(24.dp), tintFallback = accent)
            rune != null -> FutharkRuneGlyph(rune = rune, color = accent, size = 20.dp)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Eyebrow(eyebrow, accent)
            if (subtitle.isNotBlank()) {
                Text(subtitle, color = Muted, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}

/**
 * Botão compacto tátil com identidade nórdica para ações em cartões (Kanban, Agenda, Conselho dos Nove).
 * Substitui TextButtons soltos mantendo altura mínima acessível e contraste claro.
 */
@Composable
fun NordicCompactAction(
    text: String,
    onClick: () -> Unit,
    accent: Color = Gold,
    rune: HistoricalFutharkRune? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val dark = LocalAppearance.current.dark
    val shape = CutCornerShape(5.dp)
    val bg = when {
        !enabled -> if (dark) Color(0xFF171A1F) else Color(0xFFE0DDD5)
        dark -> Color(0xFF1A1F26)
        else -> Color(0xFFEDE7DC)
    }
    val fg = if (!enabled) Muted.copy(alpha = 0.6f) else accent
    Row(
        modifier
            .heightIn(min = 38.dp)
            .clip(shape)
            .background(bg)
            .border(1.dp, accent.copy(alpha = if (enabled) 0.52f else 0.22f), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (rune != null) {
            FutharkRuneGlyph(rune = rune, color = fg, size = 13.dp)
        }
        Text(
            text = text,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Superfície estrutural DS 3.2.1 com materialidade pictórica diferenciada (madeira, velino, couro,
 * bronze martelado, prata oxidada e ferro escuro), cantoneiras metálicas sutis e contraste AA/AAA.
 */
@Composable
fun CouncilMaterialSurface(
    material: CouncilMaterial = CouncilMaterial.NordicWood,
    modifier: Modifier = Modifier,
    accent: Color = Gold,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    ornamentCorners: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val dark = LocalAppearance.current.dark
    val minimal = LocalAppearance.current.minimal
    val shape = CutCornerShape(topStart = 10.dp, topEnd = 3.dp, bottomEnd = 10.dp, bottomStart = 3.dp)
    val baseFill = when (material) {
        CouncilMaterial.AgedVellum -> if (dark) Color(0xFF1E1B15) else Color(0xFFF4EDDF)
        CouncilMaterial.NordicWood -> if (dark) Color(0xFF181411) else Color(0xFFECE5D8)
        CouncilMaterial.ProjectLeather -> if (dark) Color(0xFF1C1614) else Color(0xFFEDE3DA)
        CouncilMaterial.HammeredBronze -> if (dark) Color(0xFF1F1912) else Color(0xFFF0E6D5)
        CouncilMaterial.OxidizedSilver -> if (dark) Color(0xFF13181E) else Color(0xFFE7ECF1)
        CouncilMaterial.DarkIron -> if (dark) Color(0xFF12151A) else Color(0xFFE5E8EC)
    }
    val borderColor = accent.copy(alpha = if (dark) 0.48f else 0.54f)
    val matBmp = if (!minimal) ImageBitmap.imageResource(R.drawable.nordic_council_materials_atlas) else null
    val frameBmp = if (!minimal && ornamentCorners) ImageBitmap.imageResource(R.drawable.nordic_strategy_frames_atlas) else null
    val cornerCol = when (material) {
        CouncilMaterial.HammeredBronze, CouncilMaterial.NordicWood, CouncilMaterial.AgedVellum -> StrategyFrameAsset.BronzeCorner.col
        else -> StrategyFrameAsset.IronCorner.col
    }

    Box(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(baseFill)
            .drawBehind {
                if (matBmp != null) {
                    val cw = matBmp.width / 3
                    val ch = matBmp.height / 2
                    drawImage(
                        image = matBmp,
                        srcOffset = IntOffset(material.col * cw, material.row * ch),
                        srcSize = IntSize(cw, ch),
                        dstOffset = IntOffset.Zero,
                        dstSize = IntSize(size.width.toInt().coerceAtLeast(1), size.height.toInt().coerceAtLeast(1)),
                        alpha = if (dark) 0.28f else 0.18f
                    )
                    // Véu uniforme garante contraste AA/AAA para todo texto funcional
                    drawRect(baseFill.copy(alpha = if (dark) 0.64f else 0.72f))
                }
                // Linha superior de relevo metálico sutil
                val d = 1.dp.toPx()
                drawLine(
                    color = accent.copy(alpha = if (dark) 0.24f else 0.30f),
                    start = Offset(14 * d, d),
                    end = Offset(size.width - 14 * d, d),
                    strokeWidth = 1.2f * d
                )
                // Cantoneira metálica discreta no topo-esquerdo sem invadir área de texto
                if (frameBmp != null && size.width > 120 * d && size.height > 64 * d) {
                    val fw = frameBmp.width / 4
                    val fh = frameBmp.height / 2
                    val cornerPx = (22 * d).toInt().coerceAtLeast(8)
                    drawImage(
                        image = frameBmp,
                        srcOffset = IntOffset(cornerCol * fw, 0),
                        srcSize = IntSize(fw, fh),
                        dstOffset = IntOffset((3 * d).toInt(), (3 * d).toInt()),
                        dstSize = IntSize(cornerPx, cornerPx),
                        alpha = if (dark) 0.55f else 0.42f,
                        filterQuality = FilterQuality.Medium
                    )
                }
            }
            .border(1.dp, borderColor, shape)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
fun OdinHallHeroBanner(
    title: String,
    subtitle: String,
    activeStrategyName: String,
    activeStrategy: OdinStrategyDef? = null,
    modifier: Modifier = Modifier
) {
    val dark = LocalAppearance.current.dark
    val minimal = LocalAppearance.current.minimal
    val shape = CutCornerShape(topStart = 12.dp, topEnd = 4.dp, bottomEnd = 12.dp, bottomStart = 4.dp)
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 158.dp)
            .clip(shape)
            .background(if (dark) Color(0xFF11151B) else Color(0xFF1A1F27))
            .border(1.2.dp, Gold.copy(alpha = 0.62f), shape)
    ) {
        if (!minimal) {
            Image(
                bitmap = ImageBitmap.imageResource(R.drawable.odin_council_hall),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize().alpha(0.76f)
            )
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x550B0E13),
                            Color(0xD40C1016)
                        )
                    )
                )
            )
        }
        Column(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FutharkRuneGlyph(HistoricalFutharkRune.Ansuz, Gold, size = 15.dp)
                    Text(
                        "SALÃO DO CONSELHO · MESA DE ODIN",
                        color = Gold,
                        fontSize = 11.sp,
                        letterSpacing = 1.3.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2
                    )
                }
                if (!minimal) {
                    StrategyFrameGlyph(StrategyFrameAsset.BronzeCorner, Modifier.size(20.dp), alpha = 0.72f)
                }
            }
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFFF5EFE3)
            )
            Text(
                subtitle,
                color = Color(0xFFD5DAE2),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(2.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(CutCornerShape(5.dp))
                    .background(Color(0x8812161E))
                    .border(1.dp, Gold.copy(alpha = 0.35f), CutCornerShape(5.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (activeStrategy != null && !minimal) {
                    OdinStrategyMedal(activeStrategy, Modifier.size(28.dp))
                } else {
                    FutharkRuneGlyph(HistoricalFutharkRune.Tiwaz, Gold, size = 16.dp)
                }
                Text(
                    "Estratégia ativa: $activeStrategyName",
                    color = Color(0xFFEAD097),
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
fun OdinStrategyMedal(strategy: OdinStrategyDef, modifier: Modifier = Modifier.size(52.dp)) {
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
            FutharkRuneGlyph(rune, Gold, size = 24.dp)
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
fun LifeAreaSeal(sealIndex: Int, modifier: Modifier = Modifier.size(46.dp)) {
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
 * 6 instrumentos metálicos refinados da Forja do Tempo (time_forge_refinement_atlas.webp, 3×2):
 * (0,0) Aro de trabalho bronze · (1,0) Aro de descanso prata oxidada · (2,0) Medalhão de ciclo
 * (0,1) Selo de pausa · (1,1) Selo de conclusão · (2,1) Instrumento cerimonial temporal
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

/** Mantido para compatibilidade com chamadas existentes, redirecionando para os novos aros da 1.7.5.1. */
@Composable
fun TimeForgeInstrument(col: Int, row: Int, modifier: Modifier = Modifier.size(168.dp), alpha: Float = 1f) {
    val mappedCol = if (row == 1) 1 else col.coerceIn(0, 2)
    TimeForgeRefinedPiece(col = mappedCol, row = 0, modifier = modifier, alpha = alpha)
}

@Composable
fun ImmersiveFocusScene(col: Int, row: Int, modifier: Modifier = Modifier, alpha: Float = 0.65f) {
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
fun RunicCalendarGlyph(col: Int, row: Int, modifier: Modifier = Modifier.size(24.dp)) {
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
fun StrategyControlGlyph(col: Int, row: Int, modifier: Modifier = Modifier.size(22.dp)) {
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
fun OdinCouncilOrnament(col: Int, row: Int, modifier: Modifier = Modifier.size(24.dp), alpha: Float = 1f) {
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
    NordicStrategyDivider(style = StrategyFrameAsset.UrnesDivider, modifier = modifier)
}

@Composable
fun CouncilKpiTile(
    label: String,
    value: String,
    detail: String = "",
    accent: Color = Gold,
    modifier: Modifier = Modifier,
    rune: HistoricalFutharkRune? = null
) {
    val dark = LocalAppearance.current.dark
    val shape = CutCornerShape(topStart = 7.dp, topEnd = 2.dp, bottomEnd = 7.dp, bottomStart = 2.dp)
    Column(
        modifier
            .clip(shape)
            .background(if (dark) Color(0xFF151921) else Color(0xFFEEE8DC))
            .border(1.dp, accent.copy(alpha = if (dark) 0.42f else 0.50f), shape)
            .drawBehind {
                val d = 1.dp.toPx()
                drawLine(accent.copy(alpha = 0.72f), Offset(0f, 0f), Offset(size.width, 0f), 2.2f * d)
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                label.uppercase(),
                color = Muted,
                fontSize = 10.sp,
                letterSpacing = 0.8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (rune != null) {
                FutharkRuneGlyph(rune = rune, color = accent.copy(alpha = 0.8f), size = 14.dp)
            }
        }
        Text(value, color = accent, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (detail.isNotBlank()) Text(detail, color = Muted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
