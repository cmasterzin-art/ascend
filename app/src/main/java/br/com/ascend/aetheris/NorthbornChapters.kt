package br.com.ascend.aetheris

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.*

/** Materials follow the kind of work the screen supports. */
enum class NorthbornChapter { Daily, Strategy, Archive, World, Forge, Utility }

private val NorthbornChapter.material: Int
    get() = when (this) {
        NorthbornChapter.Daily -> R.drawable.surface_oak
        NorthbornChapter.Strategy -> R.drawable.engraved_iron
        NorthbornChapter.Archive -> R.drawable.surface_vellum
        NorthbornChapter.World -> R.drawable.engraved_iron
        NorthbornChapter.Forge -> R.drawable.surface_iron
        NorthbornChapter.Utility -> R.drawable.engraved_iron
    }

private val NorthbornChapter.scene: Int
    get() = when (this) {
        NorthbornChapter.Daily, NorthbornChapter.Archive -> R.drawable.chapter_archive
        NorthbornChapter.Strategy, NorthbornChapter.Utility -> R.drawable.chapter_strategy
        NorthbornChapter.World -> R.drawable.chapter_world
        NorthbornChapter.Forge -> R.drawable.chapter_forge
    }

private val NorthbornChapter.rail: EngravedRail
    get() = when (this) {
        NorthbornChapter.Daily, NorthbornChapter.World -> EngravedRail.Voyage
        NorthbornChapter.Archive -> EngravedRail.Folio
        else -> EngravedRail.Council
    }

private fun NorthbornChapter.ink(palette: NorthbornPalette): Color = when (this) {
    NorthbornChapter.Strategy, NorthbornChapter.World -> palette.accentFjord
    NorthbornChapter.Utility -> palette.accentSilver
    else -> palette.accentBronze
}

/** Draws a quiet material at the screen edge, keeping list and chart content on their own surfaces. */
@Composable
internal fun Modifier.chapterBackdrop(chapter: NorthbornChapter): Modifier {
    val nb = LocalNorthborn.current
    val textured = !LocalAppearance.current.minimal
    val grain = if (textured) ImageBitmap.imageResource(chapter.material) else null
    val opacity = when {
        !nb.dark -> .08f
        chapter == NorthbornChapter.Archive -> .095f
        chapter == NorthbornChapter.Utility -> .12f
        else -> .22f
    }
    return drawWithCache {
        val floor = Brush.verticalGradient(
            0f to nb.canvas.copy(alpha = .17f),
            .60f to nb.canvas.copy(alpha = .62f),
            1f to nb.canvas
        )
        onDrawBehind {
            drawRect(nb.canvas)
            if (grain != null && size.width >= 1f && size.height >= 1f) {
                val height = minOf(size.height * .79f, 510.dp.toPx()).toInt().coerceAtLeast(1)
                drawImage(grain, dstSize = IntSize(size.width.toInt(), height), alpha = opacity)
                drawRect(floor, size = Size(size.width, height.toFloat()))
            }
        }
    }
}

@Composable
internal fun ChapterHeading(
    title: String,
    subtitle: String = "",
    chapter: NorthbornChapter,
    glyph: Engraving,
    modifier: Modifier = Modifier,
    cover: Boolean = false
) {
    val nb = LocalNorthborn.current
    val minimal = LocalAppearance.current.minimal
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        if (cover && !minimal) {
            RebirthScene(chapter.scene, 116.dp) { ink ->
                Row(
                    Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RebirthBoss(Modifier.size(37.dp)) {
                        EngravedIcon(glyph, ink.accent, size = 19.dp, raised = true)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(title, style = MaterialTheme.typography.headlineSmall, color = ink.title)
                        if (subtitle.isNotBlank()) {
                            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ink.body)
                        }
                    }
                }
            }
            EngravedDivider(chapter.rail)
        } else {
            val accent = chapter.ink(nb)
            Row(
                Modifier.fillMaxWidth().padding(vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EngravedIcon(glyph, accent, size = 24.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, style = MaterialTheme.typography.headlineSmall, color = nb.inkPrimary)
                    if (subtitle.isNotBlank()) {
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = nb.inkSecondary)
                    }
                }
            }
            NorthbornDivider(color = accent.copy(alpha = .52f))
        }
    }
}
