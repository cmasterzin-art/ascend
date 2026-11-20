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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

/**
 * ASCEND 1.7.5 — Design System 3.2 (Nordic Council)
 * Diversidade controlada de superfícies nórdicas (madeira esculpida para estratégia,
 * velino para objetivos/documentos, couro para projetos, bronze/ferro para controles,
 * prata oxidada para foco) usando os novos atlas WebP da 1.7.5 com alto contraste.
 */

enum class CouncilMaterial(val col: Int, val row: Int, val label: String) {
    NordicWood(0, 0, "Madeira Nórdica"),
    AgedVellum(1, 0, "Velino Estratégico"),
    ProjectLeather(2, 0, "Couro de Projeto"),
    HammeredBronze(0, 1, "Bronze Martelado"),
    OxidizedSilver(1, 1, "Prata Oxidada"),
    DarkIron(2, 1, "Ferro Escuro")
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
            colorFilter = colorFilter
        )
    }
}

/**
 * Superfície estrutural DS 3.2 com materialidade pictórica sutil e contraste garantido em tema escuro e claro.
 */
@Composable
fun CouncilMaterialSurface(
    material: CouncilMaterial = CouncilMaterial.NordicWood,
    modifier: Modifier = Modifier,
    accent: Color = Gold,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val dark = LocalAppearance.current.dark
    val minimal = LocalAppearance.current.minimal
    val shape = CutCornerShape(topStart = 10.dp, topEnd = 3.dp, bottomEnd = 10.dp, bottomStart = 3.dp)
    val baseFill = when (material) {
        CouncilMaterial.AgedVellum -> if (dark) Color(0xFF1D1A15) else Color(0xFFF3ECDD)
        CouncilMaterial.NordicWood -> if (dark) Color(0xFF171412) else Color(0xFFEBE4D8)
        CouncilMaterial.ProjectLeather -> if (dark) Color(0xFF1A1514) else Color(0xFFECE3DB)
        CouncilMaterial.HammeredBronze -> if (dark) Color(0xFF1D1813) else Color(0xFFEFE6D6)
        CouncilMaterial.OxidizedSilver -> if (dark) Color(0xFF14181D) else Color(0xFFE6EBF0)
        CouncilMaterial.DarkIron -> if (dark) Color(0xFF121519) else Color(0xFFE4E7EB)
    }
    val borderColor = accent.copy(alpha = if (dark) 0.44f else 0.50f)
    val bmp = if (!minimal) ImageBitmap.imageResource(R.drawable.nordic_council_materials_atlas) else null

    Box(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(baseFill)
            .drawBehind {
                if (bmp != null) {
                    val cw = bmp.width / 3
                    val ch = bmp.height / 2
                    drawImage(
                        image = bmp,
                        srcOffset = IntOffset(material.col * cw, material.row * ch),
                        srcSize = IntSize(cw, ch),
                        dstOffset = IntOffset.Zero,
                        dstSize = IntSize(size.width.toInt().coerceAtLeast(1), size.height.toInt().coerceAtLeast(1)),
                        alpha = if (dark) 0.22f else 0.16f
                    )
                    // Véu uniforme garante contraste AA/AAA para todo texto funcional
                    drawRect(baseFill.copy(alpha = if (dark) 0.68f else 0.72f))
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
    modifier: Modifier = Modifier
) {
    val dark = LocalAppearance.current.dark
    val minimal = LocalAppearance.current.minimal
    val shape = CutCornerShape(topStart = 12.dp, topEnd = 4.dp, bottomEnd = 12.dp, bottomStart = 4.dp)
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 152.dp)
            .clip(shape)
            .background(if (dark) Color(0xFF11151B) else Color(0xFF1B2028))
            .border(1.dp, Gold.copy(alpha = 0.55f), shape)
    ) {
        if (!minimal) {
            Image(
                bitmap = ImageBitmap.imageResource(R.drawable.odin_council_hall),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize().alpha(0.72f)
            )
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x660B0E13),
                            Color(0xCC0D1016)
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
                Text(
                    "SALÃO DO CONSELHO · MESA DE ODIN",
                    color = Gold,
                    fontSize = 11.sp,
                    letterSpacing = 1.4.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (!minimal) {
                    OdinCouncilOrnament(col = 2, row = 1, modifier = Modifier.size(28.dp))
                }
            }
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFFF3ECE1)
            )
            Text(
                subtitle,
                color = Color(0xFFD0D5DD),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OdinCouncilOrnament(col = 0, row = 1, modifier = Modifier.size(20.dp))
                Text(
                    "Estratégia ativa: $activeStrategyName",
                    color = Color(0xFFE7C582),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun OdinStrategyMedal(strategy: OdinStrategyDef, modifier: Modifier = Modifier.size(56.dp)) {
    val shape = CutCornerShape(6.dp)
    Box(
        modifier
            .clip(shape)
            .border(1.dp, Gold.copy(alpha = 0.55f), shape)
    ) {
        CouncilAtlasCell(
            resId = R.drawable.odin_strategies_atlas,
            cols = 3,
            rows = 2,
            col = strategy.atlasCol,
            row = strategy.atlasRow,
            modifier = Modifier.matchParentSize()
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

@Composable
fun TimeForgeInstrument(col: Int, row: Int, modifier: Modifier = Modifier.size(160.dp), alpha: Float = 1f) {
    CouncilAtlasCell(
        resId = R.drawable.time_forge_instruments_atlas,
        cols = 2,
        rows = 2,
        col = col,
        row = row,
        modifier = modifier,
        alpha = alpha
    )
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
    val minimal = LocalAppearance.current.minimal
    if (minimal) {
        SagaDivider()
    } else {
        Box(modifier.fillMaxWidth().height(16.dp), contentAlignment = Alignment.Center) {
            OdinCouncilOrnament(col = 1, row = 0, modifier = Modifier.fillMaxWidth(0.75f).height(14.dp), alpha = 0.85f)
        }
    }
}

@Composable
fun CouncilKpiTile(label: String, value: String, detail: String = "", accent: Color = Gold, modifier: Modifier = Modifier) {
    val dark = LocalAppearance.current.dark
    val shape = CutCornerShape(6.dp)
    Column(
        modifier
            .clip(shape)
            .background(if (dark) Color(0xFF151920) else Color(0xFFECE6DA))
            .border(1.dp, accent.copy(alpha = 0.35f), shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label.uppercase(), color = Muted, fontSize = 10.sp, letterSpacing = 0.8.sp, maxLines = 1)
        Text(value, color = accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        if (detail.isNotBlank()) Text(detail, color = Muted, fontSize = 11.sp, maxLines = 2)
    }
}
