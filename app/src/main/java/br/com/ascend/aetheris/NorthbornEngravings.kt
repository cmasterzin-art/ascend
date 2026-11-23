package br.com.ascend.aetheris

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*

internal enum class Engraving {
    Ship, Tally, Banner, Compass, Shield, Council, Forge, Calendar, Folio, Cairns, Focus, Analysis, Add
}

/** Original incised silhouettes on a 24-unit grid, usable on dark iron or light vellum. */
@Composable
internal fun EngravedIcon(
    engraving: Engraving,
    ink: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    raised: Boolean = false
) {
    val shadow = if (LocalNorthborn.current.dark) Color.Black else Color.White
    val iconSize = size
    Canvas(modifier.size(iconSize)) {
        if (raised) scale(iconSize.toPx() / 24f, pivot = Offset.Zero) {
            engrave(engraving, shadow.copy(alpha = .75f), Offset(0f, .9f), 2f)
        }
        scale(iconSize.toPx() / 24f, pivot = Offset.Zero) {
            engrave(engraving, ink, Offset.Zero, 1.45f)
        }
    }
}

private fun DrawScope.engrave(icon: Engraving, ink: Color, offset: Offset, weight: Float) {
    val stroke = Stroke(weight, cap = StrokeCap.Round, join = StrokeJoin.Round)
    fun cut(vararg p: Int, closed: Boolean = false, shaded: Boolean = false) {
        val path = Path().apply {
            moveTo(p[0] + offset.x, p[1] + offset.y)
            for (i in 2 until p.size step 2) lineTo(p[i] + offset.x, p[i + 1] + offset.y)
            if (closed) close()
        }
        if (shaded) drawPath(path, ink.copy(alpha = .19f))
        drawPath(path, ink, style = stroke)
    }
    fun pin(x: Int, y: Int, radius: Float = .75f) {
        drawCircle(ink, radius, Offset(x + offset.x, y + offset.y))
    }
    when (icon) {
        Engraving.Ship -> {
            // Planked hull, sail and carved prow.
            cut(2, 13, 5, 17, 8, 19, 16, 19, 20, 16, 21, 11, 22, 10, 20, 9)
            cut(6, 15, 18, 15)
            cut(12, 3, 12, 14)
            cut(12, 4, 17, 6, 18, 12, 14, 11, 8, 12, 9, 6, closed = true, shaded = true)
            cut(4, 22, 8, 21, 12, 22, 16, 21, 20, 22)
        }
        Engraving.Tally -> {
            cut(7, 3, 11, 5, 11, 21, 7, 19, closed = true, shaded = true)
            cut(14, 6, 18, 4, 18, 18, 14, 20, closed = true)
            cut(7, 8, 10, 7)
            cut(7, 12, 10, 11)
            cut(7, 16, 10, 15)
            cut(14, 11, 17, 10)
            cut(14, 15, 17, 14)
            cut(4, 7, 4, 17)
            cut(21, 7, 21, 17)
        }
        Engraving.Banner -> {
            cut(3, 4, 21, 4)
            pin(3, 4, 1f)
            pin(21, 4, 1f)
            cut(6, 4, 6, 20, 12, 17, 18, 20, 18, 4, closed = true, shaded = true)
            cut(12, 15, 12, 7)
            cut(9, 10, 12, 7, 15, 10)
            pin(8, 16)
            pin(16, 16)
        }
        Engraving.Compass -> {
            cut(12, 2, 15, 9, 22, 12, 15, 15, 12, 22, 9, 15, 2, 12, 9, 9, closed = true)
            cut(12, 2, 12, 12, 22, 12, 15, 9, closed = true, shaded = true)
            cut(12, 22, 12, 12, 2, 12, 9, 15, closed = true, shaded = true)
            pin(5, 5)
            pin(19, 5)
            pin(5, 19)
            pin(19, 19)
        }
        Engraving.Shield -> {
            cut(12, 2, 20, 6, 19, 14, 17, 18, 12, 22, 7, 18, 5, 14, 4, 6, closed = true, shaded = true)
            cut(8, 7, 8, 12, 12, 17, 16, 12, 16, 7, 12, 11, closed = true)
            cut(12, 11, 12, 15)
            pin(12, 4)
            pin(12, 20)
        }
        Engraving.Council -> {
            cut(3, 12, 9, 8, 15, 8, 21, 12, 15, 15, 9, 15, closed = true, shaded = true)
            cut(3, 12, 3, 15, 9, 18, 15, 18, 21, 15, 21, 12)
            cut(7, 17, 7, 21)
            cut(17, 17, 17, 21)
            cut(12, 3, 12, 12)
            cut(9, 6, 12, 3, 15, 6)
        }
        Engraving.Forge -> {
            cut(4, 4, 15, 4, 20, 7, 15, 10, 4, 10, closed = true, shaded = true)
            cut(12, 10, 12, 21, 9, 21, 9, 10)
            cut(18, 13, 20, 14)
            cut(16, 16, 18, 18)
            cut(5, 14, 3, 15)
            pin(7, 7)
        }
        Engraving.Calendar -> {
            cut(3, 6, 6, 4, 18, 4, 21, 6, 21, 19, 18, 21, 6, 21, 3, 19, closed = true, shaded = true)
            cut(3, 9, 21, 9)
            cut(8, 2, 8, 7)
            cut(16, 2, 16, 7)
            pin(7, 13, .95f)
            pin(12, 13, .95f)
            pin(17, 13, .95f)
            pin(7, 17, .95f)
            pin(12, 17, .95f)
            pin(17, 17, .95f)
        }
        Engraving.Folio -> {
            cut(12, 6, 8, 3, 3, 4, 3, 18, 8, 17, 12, 20, 16, 17, 21, 18, 21, 4, 16, 3, closed = true, shaded = true)
            cut(12, 6, 12, 20)
            cut(6, 8, 9, 9)
            cut(6, 12, 9, 13)
            cut(15, 9, 18, 8)
            cut(15, 13, 18, 12)
        }
        Engraving.Cairns -> {
            cut(3, 19, 9, 19, 8, 15, 5, 15, closed = true)
            cut(12, 19, 21, 19, 20, 15, 14, 15, closed = true)
            cut(14, 15, 19, 15, 18, 11, 15, 11, closed = true)
            cut(15, 11, 18, 11, 17, 7, closed = true)
            cut(5, 12, 8, 9, 9, 6, 12, 4)
        }
        Engraving.Focus -> {
            cut(9, 3, 15, 3, 20, 8, 20, 16, 15, 21, 9, 21, 4, 16, 4, 8, closed = true)
            cut(12, 7, 16, 12, 12, 17, 8, 12, closed = true, shaded = true)
            cut(12, 3, 12, 7)
            cut(12, 17, 12, 21)
            pin(12, 12, 1f)
        }
        Engraving.Analysis -> {
            cut(3, 20, 21, 20)
            cut(5, 20, 5, 13, 8, 13, 8, 20, shaded = true)
            cut(11, 20, 11, 9, 14, 9, 14, 20, shaded = true)
            cut(17, 20, 17, 5, 20, 5, 20, 20, shaded = true)
            cut(4, 9, 9, 6, 14, 3)
        }
        Engraving.Add -> {
            cut(12, 3, 20, 7, 20, 17, 12, 21, 4, 17, 4, 7, closed = true)
            cut(12, 7, 12, 17)
            cut(7, 12, 17, 12)
        }
    }
}

internal enum class EngravedRail(val art: Int) {
    Voyage(R.drawable.engraved_wool), Council(R.drawable.engraved_oak), Folio(R.drawable.engraved_spine)
}

/** End fittings stay proportional while the center stretches to the available width. */
@Composable
internal fun EngravedDivider(rail: EngravedRail, modifier: Modifier = Modifier) {
    val nb = LocalNorthborn.current
    if (LocalAppearance.current.minimal) {
        RebirthRule(modifier)
        return
    }
    val bitmap = ImageBitmap.imageResource(rail.art)
    Canvas(modifier.fillMaxWidth().height(if (rail == EngravedRail.Voyage) 11.dp else 15.dp)) {
        val width = size.width.toInt()
        val height = size.height.toInt()
        val srcEdge = bitmap.width / 6
        val dstEdge = (height * 2).coerceAtMost(width / 3)
        if (dstEdge <= 0 || height <= 0) return@Canvas
        fun slice(from: Int, srcWidth: Int, to: Int, dstWidth: Int) {
            drawImage(
                bitmap, srcOffset = IntOffset(from, 0), srcSize = IntSize(srcWidth, bitmap.height),
                dstOffset = IntOffset(to, 0), dstSize = IntSize(dstWidth, height)
            )
        }
        slice(0, srcEdge, 0, dstEdge)
        slice(srcEdge, bitmap.width - srcEdge * 2, dstEdge, width - dstEdge * 2)
        slice(bitmap.width - srcEdge, srcEdge, width - dstEdge, dstEdge)
        if (!nb.dark) drawRect(nb.accentPaleGold.copy(alpha = .07f))
    }
}

@Composable
internal fun EngravedBackdrop(content: @Composable BoxScope.() -> Unit) {
    val nb = LocalNorthborn.current
    Box(Modifier.fillMaxSize().background(nb.canvas)) {
        if (!LocalAppearance.current.minimal) {
            Image(
                painterResource(R.drawable.engraved_iron), null, Modifier.matchParentSize(),
                alignment = Alignment.TopCenter, contentScale = ContentScale.Crop,
                alpha = if (nb.dark) .42f else .10f
            )
        }
        content()
    }
}
