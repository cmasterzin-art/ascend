package br.com.ascend.aetheris

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * 1.7.2 — Design System 3.0 (Nordic Viking Interface) & Renascimento Rúnico.
 *
 * Strictly extracts individual atlas cells before nine-slice scaling so corner rivets,
 * Urnes/Borre end-caps, and L-brackets never deform or bleed adjacent cells.
 * Distinguishes verified historical Elder Futhark vector glyphs from fictional Aetheris seals.
 */

enum class VikingButtonStyle(val cellIndex: Int, val label: String) {
 FerroRunico(0, "Ferro Rúnico"),
 BronzeForjado(1, "Bronze Forjado"),
 CerimonialViking(2, "Cerimonial Viking"),
 Confirmacao(3, "Confirmação"),
 Destrutivo(4, "Destrutivo"),
 CompactoRunico(5, "Compacto Rúnico")
}

/**
 * Verified historical Elder Futhark glyphs rendered via crisp vector chisel geometry.
 * Strictly historical linguistic runes; zero extremist or ideological symbology.
 */
enum class HistoricalFutharkRune(val runeName: String, val unicode: String, val domain: String) {
 Fehu("Fehu", "ᚠ", "Conquista e recompensa"),
 Ansuz("Ansuz", "ᚨ", "Sabedoria e memória"),
 Raido("Raido", "ᚱ", "Jornada e caminho"),
 Kenaz("Kenaz", "ᚲ", "Foco e chama"),
 Gebo("Gebo", "ᚷ", "Aliança e vínculo"),
 Wunjo("Wunjo", "ᚹ", "Descanso e equilíbrio"),
 Isa("Isa", "ᛁ", "Firmeza e disciplina"),
 Berkano("Berkano", "ᛒ", "Hábito e crescimento"),
 Ehwaz("Ehwaz", "ᛖ", "Progresso e expedição"),
 Mannaz("Mannaz", "ᛗ", "Herói e identidade"),
 Laguz("Laguz", "ᛚ", "Fluxo e crônica"),
 Dagaz("Dagaz", "ᛞ", "Renovação e horizonte")
}

/**
 * 24 original fictional seals of Aetheris (aetheris_runes_atlas.webp, 6 columns x 4 rows).
 * Clearly distinct from historical Elder Futhark runes.
 */
enum class AetherisSealKind(val cellIndex: Int, val label: String) {
 CompassStar(0, "Estrela da Bússola"),
 MountainPeaks(1, "Cumes do Norte"),
 OceanWaves(2, "Correntes do Fiorde"),
 ArchedDoorway(3, "Pórtico das Eras"),
 CrescentMoon(4, "Vigília Lunar"),
 PineBough(5, "Ramo de Pinheiro"),
 LanternFlame(6, "Chama da Lanterna"),
 DiamondKnot(7, "Nó do Juramento"),
 TwinStaves(8, "Colunas do Saber"),
 ShieldCrest(9, "Escudo do Guardião"),
 CrossroadsStar(10, "Encruzilhada dos Caminhos"),
 SpiralWhorl(11, "Espiral do Tempo"),
 OpenBook(12, "Códice Aberto"),
 Hourglass(13, "Ampulheta do Foco"),
 TwinChevrons(14, "Avanço da Trilha"),
 RisingSun(15, "Alvorada Boreal"),
 LaurelWreath(16, "Coroa da Consistência"),
 Anchor(17, "Âncora da Calma"),
 StoneTower(18, "Torre da Vigília"),
 HearthCampfire(19, "Fogueira da Jornada"),
 StoneBridge(20, "Ponte de Pedra"),
 NorthernStar(21, "Estrela do Firmamento"),
 OakLeaf(22, "Folha de Carvalho"),
 ConcentricSeal(23, "Selo Concêntrico")
}

/**
 * 16 functional Viking pictorial icons (viking_functional_icons_atlas.webp, 4x4),
 * each paired with a CarvedGlyph vector fallback for minimal mode or small scale (<20.dp).
 */
enum class VikingIcon(val cellIndex: Int, val fallback: Glyph, val label: String) {
 Hearth(0, Glyph.Journey, "Jornada"),
 Helmet(1, Glyph.Hero, "Herói"),
 CompassRaven(2, Glyph.World, "Mundo"),
 Codex(3, Glyph.Routines, "Biblioteca"),
 CalendarStone(4, Glyph.Planner, "Agenda"),
 LanternHourglass(5, Glyph.Focus, "Foco"),
 Longship(6, Glyph.Map, "Expedição"),
 ForgeTools(7, Glyph.Sagas, "Caminhos"),
 RunicTablet(8, Glyph.Claims, "Flashcards"),
 RoundShield(9, Glyph.Achievements, "Conquista"),
 QuillScroll(10, Glyph.Chronicles, "Crônica"),
 MountainPeak(11, Glyph.Journey, "Montanhas"),
 SilverRing(12, Glyph.Inventory, "Relíquias"),
 BronzeKey(13, Glyph.Claims, "Desbloqueio"),
 OakTree(14, Glyph.Habits, "Hábitos"),
 KnotGear(15, Glyph.Settings, "Configurações")
}

private val vikingDesaturated = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.12f) })
private val vikingMutedDark = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.28f) })

/**
 * Extracts a single cell (index in columns x rows) from an atlas and draws it with true 9-slice scaling.
 * Never applies 9-slice directly to the entire atlas sheet.
 */
fun DrawScope.drawAtlasCellNineSlice(
 image: ImageBitmap,
 index: Int,
 columns: Int,
 rows: Int,
 capXRatio: Float = .18f,
 capYRatio: Float = .28f,
 alpha: Float = 1f,
 colorFilter: ColorFilter? = null
) {
 val safeCols = columns.coerceAtLeast(1)
 val safeRows = rows.coerceAtLeast(1)
 val safeIdx = index.coerceIn(0, safeCols * safeRows - 1)
 val cw = (image.width / safeCols).coerceAtLeast(4)
 val ch = (image.height / safeRows).coerceAtLeast(4)
 val ox = (safeIdx % safeCols) * cw
 val oy = (safeIdx / safeCols) * ch
 val dw = size.width.toInt().coerceAtLeast(4)
 val dh = size.height.toInt().coerceAtLeast(4)
 val sx = (cw * capXRatio).toInt().coerceIn(1, cw / 3)
 val sy = (ch * capYRatio).toInt().coerceIn(1, ch / 3)
 val dy = (dh * capYRatio).toInt().coerceIn(1, dh / 3)
 val dx = (dy * (sx.toFloat() / sy.coerceAtLeast(1))).toInt().coerceIn(1, dw / 3)
 val sxCoords = intArrayOf(ox, ox + sx, ox + cw - sx, ox + cw)
 val syCoords = intArrayOf(oy, oy + sy, oy + ch - sy, oy + ch)
 val dxCoords = intArrayOf(0, dx, dw - dx, dw)
 val dyCoords = intArrayOf(0, dy, dh - dy, dh)
 for (r in 0..2) {
  for (c in 0..2) {
   val sW = (sxCoords[c + 1] - sxCoords[c]).coerceAtLeast(1)
   val sH = (syCoords[r + 1] - syCoords[r]).coerceAtLeast(1)
   val dW = (dxCoords[c + 1] - dxCoords[c]).coerceAtLeast(1)
   val dH = (dyCoords[r + 1] - dyCoords[r]).coerceAtLeast(1)
   drawImage(
    image = image,
    srcOffset = IntOffset(sxCoords[c], syCoords[r]),
    srcSize = IntSize(sW, sH),
    dstOffset = IntOffset(dxCoords[c], dyCoords[r]),
    dstSize = IntSize(dW, dH),
    alpha = alpha,
    colorFilter = colorFilter,
    filterQuality = FilterQuality.Medium
   )
  }
 }
}

@Composable
fun Modifier.atlasCellNineSlice(
 resource: Int,
 index: Int,
 columns: Int,
 rows: Int,
 capXRatio: Float = .18f,
 capYRatio: Float = .28f,
 alpha: Float = .85f,
 filter: ColorFilter? = null
): Modifier {
 val minimal = LocalAppearance.current.minimal
 val img = if (minimal) null else ImageBitmap.imageResource(resource)
 return drawWithCache {
  onDrawBehind {
   if (img != null) {
    drawAtlasCellNineSlice(img, index, columns, rows, capXRatio, capYRatio, alpha, filter)
   }
  }
 }
}

/** Renders a verified historical Elder Futhark glyph as clean geometric chisel strokes. */
@Composable
fun FutharkRuneGlyph(
 rune: HistoricalFutharkRune,
 color: Color,
 modifier: Modifier = Modifier,
 size: Dp = 18.dp
) {
 val dark = LocalAppearance.current.dark
 val minimal = LocalAppearance.current.minimal
 val shadow = if (dark) Color.Black.copy(alpha = .7f) else Color.White.copy(alpha = .7f)
 Canvas(modifier.size(size)) {
  val u = this.size.minDimension / 24f
  val stroke = Stroke(width = (1.85f * u).coerceAtLeast(1.2f), cap = StrokeCap.Round, join = StrokeJoin.Round)
  fun drawRune(c: Color, dx: Float, dy: Float) {
   fun seg(x1: Float, y1: Float, x2: Float, y2: Float) {
    drawLine(c, Offset(x1 * u + dx, y1 * u + dy), Offset(x2 * u + dx, y2 * u + dy), stroke.width, cap = StrokeCap.Round)
   }
   when (rune) {
    HistoricalFutharkRune.Fehu -> { seg(7f, 3f, 7f, 21f); seg(7f, 11f, 17f, 4f); seg(7f, 16f, 16f, 10f) }
    HistoricalFutharkRune.Ansuz -> { seg(7f, 3f, 7f, 21f); seg(7f, 5f, 17f, 10f); seg(7f, 11f, 16f, 16f) }
    HistoricalFutharkRune.Raido -> { seg(7f, 3f, 7f, 21f); seg(7f, 4f, 16f, 9f); seg(16f, 9f, 7f, 13.5f); seg(10.5f, 12f, 17f, 21f) }
    HistoricalFutharkRune.Kenaz -> { seg(16f, 5f, 7f, 12f); seg(7f, 12f, 16f, 19f) }
    HistoricalFutharkRune.Gebo -> { seg(6f, 4f, 18f, 20f); seg(18f, 4f, 6f, 20f) }
    HistoricalFutharkRune.Wunjo -> { seg(7f, 3f, 7f, 21f); seg(7f, 4f, 17f, 9.5f); seg(17f, 9.5f, 7f, 14.5f) }
    HistoricalFutharkRune.Isa -> { seg(12f, 3f, 12f, 21f) }
    HistoricalFutharkRune.Berkano -> { seg(7f, 3f, 7f, 21f); seg(7f, 4f, 16f, 8f); seg(16f, 8f, 7f, 12f); seg(7f, 12f, 16f, 16.5f); seg(16f, 16.5f, 7f, 20.5f) }
    HistoricalFutharkRune.Ehwaz -> { seg(6f, 3f, 6f, 21f); seg(18f, 3f, 18f, 21f); seg(6f, 7f, 12f, 12f); seg(18f, 7f, 12f, 12f) }
    HistoricalFutharkRune.Mannaz -> { seg(6f, 3f, 6f, 21f); seg(18f, 3f, 18f, 21f); seg(6f, 4f, 18f, 13f); seg(18f, 4f, 6f, 13f) }
    HistoricalFutharkRune.Laguz -> { seg(8f, 3f, 8f, 21f); seg(8f, 4f, 17f, 10f) }
    HistoricalFutharkRune.Dagaz -> { seg(5f, 5f, 5f, 19f); seg(19f, 5f, 19f, 19f); seg(5f, 5f, 19f, 19f); seg(19f, 5f, 5f, 19f) }
   }
  }
  if (!minimal) drawRune(shadow, .6f * u, .8f * u)
  drawRune(color, 0f, 0f)
 }
}

/** Renders one of the 24 original fictional Aetheris medallions (aetheris_runes_atlas.webp, 6x4). */
@Composable
fun AetherisSeal(
 seal: AetherisSealKind,
 modifier: Modifier = Modifier,
 size: Dp = 28.dp,
 alpha: Float = 1f,
 lit: Boolean = true,
 fallbackColor: Color = Gold
) {
 val minimal = LocalAppearance.current.minimal
 if (minimal || size < 16.dp) {
  val rune = HistoricalFutharkRune.entries[seal.cellIndex % HistoricalFutharkRune.entries.size]
  FutharkRuneGlyph(rune, if (lit) fallbackColor else Muted, modifier, size)
 } else {
  AtlasCell(
   resource = R.drawable.aetheris_runes_atlas,
   index = seal.cellIndex,
   columns = 6,
   rows = 4,
   modifier = modifier.size(size),
   alpha = if (lit) alpha else alpha * .58f,
   filter = if (lit) null else vikingDesaturated
  )
 }
}

/**
 * Renders a functional Viking pictorial icon (>=20.dp, non-minimal) with automatic
 * CarvedGlyph vector fallback at small scale or in minimal mode.
 */
@Composable
fun VikingFunctionalIcon(
 icon: VikingIcon,
 color: Color,
 modifier: Modifier = Modifier,
 size: Dp = 24.dp,
 active: Boolean = true
) {
 val appearance = LocalAppearance.current
 if (appearance.minimal || size < 20.dp) {
  CarvedGlyph(icon.fallback, color, modifier, size)
 } else {
  AtlasCell(
   resource = R.drawable.viking_functional_icons_atlas,
   index = icon.cellIndex,
   columns = 4,
   rows = 4,
   modifier = modifier.size(size),
   alpha = if (active) (if (appearance.dark) .96f else .92f) else (if (appearance.dark) .66f else .56f),
   filter = if (active) null else vikingMutedDark
  )
 }
}

/**
 * Horizontal Viking divider drawn from viking_knotwork_atlas.webp (2 cols x 4 rows):
 * variant 7 = hammered bronze diamond divider (default); 0 = Urnes; 1 = Borre; 2 = Ringerike; 3 = braided rope.
 */
@Composable
fun VikingKnotDivider(
 modifier: Modifier = Modifier,
 variant: Int = 7,
 alpha: Float = .86f,
 height: Dp = 18.dp
) {
 val appearance = LocalAppearance.current
 val onParchment = androidx.compose.material3.LocalContentColor.current == Sepia
 if (appearance.minimal || onParchment) {
  val lineColor = if (onParchment) Sepia.copy(alpha = .35f) else Gold.copy(alpha = .38f)
  Canvas(modifier.fillMaxWidth().height(10.dp)) {
   val y = size.height / 2f; val x = size.width / 2f; val d = 1.dp.toPx()
   drawLine(lineColor, Offset(0f, y), Offset(x - 7 * d, y), d)
   drawLine(lineColor, Offset(x + 7 * d, y), Offset(size.width, y), d)
   val dm = Path().apply { moveTo(x, y - 3.5f * d); lineTo(x + 3.5f * d, y); lineTo(x, y + 3.5f * d); lineTo(x - 3.5f * d, y); close() }
   drawPath(dm, lineColor, style = Stroke(d))
  }
 } else {
  val cell = variant.coerceIn(0, 7)
  AtlasCell(
   resource = R.drawable.viking_knotwork_atlas,
   index = cell,
   columns = 2,
   rows = 4,
   modifier = modifier.fillMaxWidth().height(height),
   alpha = if (appearance.dark) alpha else alpha * .62f
  )
 }
}

/**
 * Subtle forged Viking corner brackets on Heroic, Ceremonial, and Artifact cards.
 * Uses cell 4 (single iron/bronze TL bracket) or the exact left half of cell 5 (silver TL bracket),
 * rotated 180° for the bottom-right corner so brackets never double or collide with text.
 */
@Composable
fun Modifier.vikingCornerAccents(
 alpha: Float = .48f,
 sizeDp: Dp = 20.dp,
 silver: Boolean = false
): Modifier {
 val appearance = LocalAppearance.current
 val img = if (appearance.minimal) null else ImageBitmap.imageResource(R.drawable.viking_knotwork_atlas)
 val effAlpha = if (appearance.dark) alpha else alpha * .42f
 return drawWithCache {
  onDrawBehind {
   if (img != null) {
    val cw = img.width / 2
    val ch = img.height / 4
    // Cell 4 = (col 0, row 2) single iron/bronze TL bracket; Cell 5 left half = silver TL bracket
    val srcOff = if (silver) IntOffset(cw, 2 * ch) else IntOffset(0, 2 * ch)
    val srcSz = if (silver) IntSize(cw / 2, ch) else IntSize(cw, ch)
    val px = sizeDp.toPx().toInt().coerceAtLeast(8)
    val inset = 3.dp.toPx().toInt()
    drawImage(img, srcOff, srcSz, IntOffset(inset, inset), IntSize(px, px), alpha = effAlpha, filterQuality = FilterQuality.Medium)
    withTransform({
     translate(left = size.width - inset, top = size.height - inset)
     rotate(180f, Offset.Zero)
    }) {
     drawImage(img, srcOff, srcSz, IntOffset.Zero, IntSize(px, px), alpha = effAlpha, filterQuality = FilterQuality.Medium)
    }
   }
  }
 }
}

/** Nine-sliced hollow Viking ornamental frame (cell 6 of viking_knotwork_atlas.webp). */
@Composable
fun Modifier.vikingHollowFrame(alpha: Float = .72f): Modifier =
 atlasCellNineSlice(R.drawable.viking_knotwork_atlas, index = 6, columns = 2, rows = 4, capXRatio = .20f, capYRatio = .26f, alpha = alpha)

/** Confirmation check seal from viking_controls_atlas.webp (cell 6, centered square crop). */
@Composable
fun VikingCheckSeal(size: Dp = 22.dp, modifier: Modifier = Modifier) {
 if (LocalAppearance.current.minimal) {
  FutharkRuneGlyph(HistoricalFutharkRune.Fehu, Green, modifier, size)
 } else {
  val img = ImageBitmap.imageResource(R.drawable.viking_controls_atlas)
  Canvas(modifier.size(size)) {
   val cw = img.width / 2
   val ch = img.height / 4
   val side = minOf(cw, ch)
   val srcX = (cw - side) / 2
   val srcY = 3 * ch + (ch - side) / 2
   drawImage(
    image = img,
    srcOffset = IntOffset(srcX, srcY),
    srcSize = IntSize(side, side),
    dstSize = IntSize(this.size.width.toInt().coerceAtLeast(1), this.size.height.toInt().coerceAtLeast(1)),
    filterQuality = FilterQuality.Medium
   )
  }
 }
}

/** Oval hammered bronze-silver runic nav amulet (cell 7 of viking_controls_atlas.webp). */
@Composable
fun VikingNavAmulet(modifier: Modifier = Modifier, alpha: Float = .9f) {
 if (!LocalAppearance.current.minimal) {
  AtlasCell(R.drawable.viking_controls_atlas, index = 7, columns = 2, rows = 4, modifier = modifier, alpha = alpha)
 }
}

/** Renders a regional pictorial plate (3x3 atlas) for Biblioteca das Eras or Montanhas da Disciplina. */
@Composable
fun RealmAtlasPlate(resource: Int, cellIndex: Int, modifier: Modifier = Modifier, alpha: Float = 1f) {
 AtlasCell(resource = resource, index = cellIndex.coerceIn(0, 8), columns = 3, rows = 3, modifier = modifier, alpha = alpha)
}

/** Renders a collectible relic painting from realm_relics_atlas.webp (2x2). */
@Composable
fun RelicPainting(cellIndex: Int, modifier: Modifier = Modifier, size: Dp = 52.dp, lit: Boolean = true) {
 if (LocalAppearance.current.minimal) {
  AetherisSeal(AetherisSealKind.entries[(cellIndex * 5) % AetherisSealKind.entries.size], modifier, size, lit = lit)
 } else {
  AtlasCell(
   resource = R.drawable.realm_relics_atlas,
   index = cellIndex.coerceIn(0, 3),
   columns = 2,
   rows = 2,
   modifier = modifier.size(size),
   alpha = if (lit) 1f else .48f,
   filter = if (lit) null else vikingDesaturated
  )
 }
}

/** Renders a map/exploration ornament from realm_ornaments_atlas.webp (3x2). */
@Composable
fun RealmOrnament(cellIndex: Int, modifier: Modifier = Modifier, size: Dp = 28.dp, lit: Boolean = true) {
 if (LocalAppearance.current.minimal) {
  CarvedGlyph(Glyph.World, if (lit) Gold else Muted, modifier, size)
 } else {
  AtlasCell(
   resource = R.drawable.realm_ornaments_atlas,
   index = cellIndex.coerceIn(0, 5),
   columns = 3,
   rows = 2,
   modifier = modifier.size(size),
   alpha = if (lit) 1f else .52f,
   filter = if (lit) null else vikingDesaturated
  )
 }
}
