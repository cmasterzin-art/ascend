package br.com.ascend.aetheris

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
internal fun forgedPlate(role: NorthbornButtonRole, enabled: Boolean): ImageBitmap? {
    if (!enabled || LocalAppearance.current.minimal) return null
    val resource = when (role) {
        NorthbornButtonRole.Primary -> R.drawable.forge_plate_bronze
        NorthbornButtonRole.Ceremonial -> R.drawable.forge_plate_ritual
        NorthbornButtonRole.Confirm -> R.drawable.forge_plate_patina
        NorthbornButtonRole.Destructive -> R.drawable.forge_plate_oxide
        NorthbornButtonRole.Secondary, NorthbornButtonRole.Contextual -> {
            if (LocalNorthborn.current.dark) R.drawable.forge_plate_iron else R.drawable.forge_plate_pewter
        }
        NorthbornButtonRole.Tertiary -> return null
    }
    return ImageBitmap.imageResource(resource)
}

/** Keeps the rendered satin bevel and micro-finials intact across short or multiline labels. */
internal fun Modifier.forgedFinish(
    plate: ImageBitmap?,
    pressure: State<Float>? = null,
    opacity: Float = 1f
): Modifier = drawWithCache {
    val width = size.width.roundToInt()
    val height = size.height.roundToInt()
    val end = minOf(16.dp.roundToPx(), width / 4)
    val lip = minOf(7.dp.roundToPx(), height / 3)
    val x = intArrayOf(0, end, width - end, width)
    val y = intArrayOf(0, lip, height - lip, height)
    val sourceX = plate?.let { intArrayOf(0, 32, it.width - 32, it.width) }
    val sourceY = plate?.let { intArrayOf(0, 14, it.height - 14, it.height) }

    onDrawBehind {
        if (plate != null && sourceX != null && sourceY != null && end > 0 && lip > 0) {
            for (row in 0..2) {
                for (column in 0..2) {
                    drawImage(
                        plate,
                        srcOffset = IntOffset(sourceX[column], sourceY[row]),
                        srcSize = IntSize(sourceX[column + 1] - sourceX[column], sourceY[row + 1] - sourceY[row]),
                        dstOffset = IntOffset(x[column], y[row]),
                        dstSize = IntSize(x[column + 1] - x[column], y[row + 1] - y[row]),
                        alpha = opacity
                    )
                }
            }
        }
        val press = pressure?.value ?: 0f
        if (press > 0f) drawRect(Color.Black.copy(alpha = .15f * press))
    }
}
