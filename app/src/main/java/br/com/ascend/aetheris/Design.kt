package br.com.ascend.aetheris

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

data class Appearance(val dark: Boolean = true, val reducedMotion: Boolean = false, val minimal: Boolean = false)
val LocalNarrativeNames = staticCompositionLocalOf { false }
val LocalAppearance = staticCompositionLocalOf { Appearance() }

val Night: Color @Composable get() = LocalNorthborn.current.canvas
val Surface: Color @Composable get() = LocalNorthborn.current.surfaceBase
val CardColor: Color @Composable get() = LocalNorthborn.current.surfaceElevated
val Gold: Color @Composable get() = LocalNorthborn.current.accentBronze
val Blue: Color @Composable get() = LocalNorthborn.current.accentFjord
val Violet: Color @Composable get() = MaterialTheme.colorScheme.tertiary
val Green: Color @Composable get() = LocalNorthborn.current.accentPatina
val Ink: Color @Composable get() = LocalNorthborn.current.inkPrimary
val Muted: Color @Composable get() = LocalNorthborn.current.inkSecondary

@Composable
fun habitColor(index: Int): Color = if (LocalAppearance.current.dark) {
    listOf(Color(0xFFBC9B6A), Color(0xFF84A4C4), Color(0xFFB39EC4), Color(0xFF83A88E), Color(0xFFCC8578))[index.coerceIn(0, 4)]
} else {
    listOf(Color(0xFF856232), Color(0xFF2E587A), Color(0xFF6B4E8A), Color(0xFF296648), Color(0xFF9C4236))[index.coerceIn(0, 4)]
}

@Composable
fun AscendTheme(mode: String = "dark", reducedMotion: Boolean = false, minimal: Boolean = false, content: @Composable () -> Unit) {
    val dark = when (mode) {
        "light" -> false
        "system" -> isSystemInDarkTheme()
        else -> true
    }
    val nb = if (dark) NorthbornDarkPalette else NorthbornLightPalette
    val scheme = if (dark) {
        darkColorScheme(
            primary = nb.accentBronze,
            onPrimary = nb.onAccent,
            secondary = nb.accentFjord,
            tertiary = Color(0xFFB39EC4),
            background = nb.canvas,
            surface = nb.surfaceBase,
            surfaceVariant = nb.surfaceElevated,
            onBackground = nb.inkPrimary,
            onSurface = nb.inkPrimary,
            onSurfaceVariant = nb.inkSecondary,
            outline = nb.hairlineStrong,
            error = nb.accentEmber
        )
    } else {
        lightColorScheme(
            primary = nb.accentBronze,
            onPrimary = nb.onAccent,
            secondary = nb.accentFjord,
            tertiary = Color(0xFF6B4E8A),
            background = nb.canvas,
            surface = nb.surfaceBase,
            surfaceVariant = nb.surfaceElevated,
            onBackground = nb.inkPrimary,
            onSurface = nb.inkPrimary,
            onSurfaceVariant = nb.inkSecondary,
            outline = nb.hairlineStrong,
            error = nb.accentEmber
        )
    }
    CompositionLocalProvider(
        LocalAppearance provides Appearance(dark, reducedMotion, minimal),
        LocalNorthborn provides nb
    ) {
        MaterialTheme(
            colorScheme = scheme,
            shapes = Shapes(
                extraSmall = CutCornerShape(3.dp),
                small = NorthbornShape.sm,
                medium = NorthbornShape.md,
                large = NorthbornShape.lg,
                extraLarge = CutCornerShape(10.dp)
            ),
            typography = Typography(
                // Família Cerimonial / Display
                displaySmall = TextStyle(fontFamily = SagaFont, fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.4.sp),
                headlineLarge = TextStyle(fontFamily = SagaFont, fontSize = 30.sp, lineHeight = 38.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp),
                headlineMedium = TextStyle(fontFamily = SagaFont, fontSize = 24.sp, lineHeight = 31.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.2.sp),
                headlineSmall = TextStyle(fontFamily = SagaFont, fontSize = 20.sp, lineHeight = 27.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.15.sp),
                // Família Editorial
                titleLarge = TextStyle(fontFamily = SagaFont, fontSize = 19.sp, lineHeight = 25.sp, fontWeight = FontWeight.Medium),
                titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 15.5.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
                titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
                // Família Operacional (alta legibilidade)
                bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 15.sp, lineHeight = 23.sp, fontWeight = FontWeight.Normal),
                bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.Normal),
                bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.5.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
                labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
                labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
                labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium)
            ),
            content = content
        )
    }
}

val SagaFont = FontFamily(Font(R.font.saga, FontWeight.Medium))

@Composable
fun Eyebrow(text: String, color: Color = LocalNorthborn.current.accentBronze) {
    Text(
        text = text.uppercase(),
        color = color,
        fontSize = 10.5.sp,
        lineHeight = 15.sp,
        letterSpacing = 1.2.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun Title(text: String, sub: String = "") {
    val nb = LocalNorthborn.current
    Column(
        Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text, style = MaterialTheme.typography.headlineMedium, color = nb.inkPrimary)
        if (sub.isNotEmpty()) {
            Text(sub, color = nb.inkSecondary, fontSize = 13.5.sp, lineHeight = 20.sp)
        }
        Box(Modifier.padding(top = 2.dp).width(32.dp).height(1.5.dp).background(nb.accentBronze.copy(alpha = 0.65f)))
    }
}

@Composable
fun Panel(modifier: Modifier = Modifier, accent: Color = LocalNorthborn.current.hairlineStrong, content: @Composable ColumnScope.() -> Unit) {
    NorthbornSurface(
        role = NorthbornSurfaceRole.Operational,
        modifier = modifier,
        accent = accent,
        content = content
    )
}

@Composable
fun Page(chapter: NorthbornChapter = NorthbornChapter.Utility, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize().chapterBackdrop(chapter)) {
        PageColumn(content)
    }
}

@Composable
internal fun EngravedPage(content: @Composable ColumnScope.() -> Unit) {
    EngravedBackdrop {
        PageColumn(content)
    }
}

@Composable
private fun PageColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = NorthbornSpace.page)
            .padding(top = NorthbornSpace.md, bottom = NorthbornSpace.xl),
        verticalArrangement = Arrangement.spacedBy(NorthbornSpace.lg),
        content = content
    )
}

@Composable
fun Meter(value: Float, color: Color = Blue, modifier: Modifier = Modifier, trackColor: Color = LocalNorthborn.current.hairline, slim: Boolean = false) {
    val fraction by animateFloatAsState(
        value.coerceIn(0f, 1f),
        tween(if (LocalAppearance.current.reducedMotion) 0 else AetherTokens.scene),
        label = "progresso"
    )
    val nb = LocalNorthborn.current
    val onParchment = LocalContentColor.current == Sepia
    val track = if (onParchment) Sepia.copy(alpha = 0.22f) else trackColor
    val barHeight = if (slim) 5.dp else 7.dp

    Canvas(
        modifier
            .fillMaxWidth()
            .height(barHeight)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(value.coerceIn(0f, 1f), 0f..1f) }
    ) {
        val w = size.width
        val h = size.height
        val cy = h * 0.5f
        val d = 1.dp.toPx()
        // Trilho mineral limpo sem bitmap ruidoso
        drawRoundRect(
            color = track.copy(alpha = if (nb.dark) 0.9f else 0.85f),
            topLeft = Offset(0f, 0f),
            size = Size(w, h),
            cornerRadius = CornerRadius(h * 0.4f)
        )
        if (fraction > 0f) {
            val fw = (w * fraction).coerceAtLeast(h)
            drawRoundRect(
                color = color,
                topLeft = Offset(0f, 0f),
                size = Size(fw, h),
                cornerRadius = CornerRadius(h * 0.4f)
            )
            // Filete de luz superior sutil
            drawLine(
                Color.White.copy(alpha = if (nb.dark) 0.22f else 0.30f),
                Offset(d, d),
                Offset((fw - d).coerceAtLeast(d), d),
                0.8f * d
            )
        }
        if (!slim) {
            listOf(0.25f, 0.5f, 0.75f).forEach { stop ->
                val x = w * stop
                drawLine(nb.canvas.copy(alpha = 0.45f), Offset(x, 0f), Offset(x, h), d)
            }
        }
    }
}

@Composable
fun Stat(value: String, label: String, color: Color = Gold) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, color = color, fontSize = 24.sp, lineHeight = 28.sp, fontFamily = SagaFont)
        Text(label, color = Muted, fontSize = 11.sp, letterSpacing = 0.4.sp)
    }
}

@Composable
fun Action(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    completed: Boolean = false,
    ceremonial: Boolean = false,
    secondary: Boolean = false,
    selected: Boolean = false,
    style: VikingButtonStyle? = null,
    leadingRune: HistoricalFutharkRune? = null
) {
    SagaButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        completed = completed,
        ceremonial = ceremonial,
        secondary = secondary,
        selected = selected,
        style = style,
        leadingRune = leadingRune
    )
}

@Composable
fun Empty(text: String) {
    val nb = LocalNorthborn.current
    NorthbornSurface(role = NorthbornSurfaceRole.Informative, accent = nb.accentSilver) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EngravedIcon(Engraving.Cairns, nb.accentSilver, size = 22.dp)
            Text(text, color = nb.inkSecondary, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ChipRow(values: List<String>, selected: Int, onSelect: (Int) -> Unit, scrollable: Boolean = true) {
    val nb = LocalNorthborn.current
    Row(
        if (scrollable) Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()) else Modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        values.forEachIndexed { i, v ->
            val isSel = selected == i
            val shape = NorthbornShape.pill
            val bg = when {
                isSel && nb.dark -> nb.accentBronze.copy(alpha = 0.18f)
                isSel -> nb.accentBronze.copy(alpha = 0.14f)
                nb.dark -> nb.surfaceBase
                else -> nb.surfaceBase
            }
            val border = if (isSel) nb.accentBronze else nb.hairlineStrong.copy(alpha = 0.72f)
            val fg = if (isSel) (if (nb.dark) nb.accentPaleGold else nb.accentBronze) else nb.inkSecondary
            Box(
                Modifier
                    .heightIn(min = NorthbornSpace.touchMin)
                    .clip(shape)
                    .background(bg)
                    .border(if (isSel) 1.2.dp else 1.dp, border, shape)
                    .semantics(mergeDescendants = true) {
                        this.selected = isSel
                        role = Role.Tab
                    }
                    .clickable { onSelect(i) }
                    .padding(horizontal = 13.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = v,
                    color = fg,
                    fontSize = 12.5.sp,
                    fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun FormField(value: String, onChange: (String) -> Unit, label: String, number: Boolean = false, multi: Boolean = false, modifier: Modifier = Modifier) {
    val nb = LocalNorthborn.current
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label, fontSize = 13.sp) },
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = nb.inkPrimary),
        singleLine = !multi,
        minLines = if (multi) 3 else 1,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = if (number) androidx.compose.ui.text.input.KeyboardType.Number else androidx.compose.ui.text.input.KeyboardType.Text
        ),
        shape = NorthbornShape.md,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = nb.accentBronze,
            unfocusedBorderColor = nb.hairlineStrong,
            disabledBorderColor = nb.hairline,
            focusedLabelColor = nb.accentBronze,
            unfocusedLabelColor = nb.inkSecondary,
            focusedContainerColor = if (nb.dark) nb.surfaceElevated else nb.surfaceBase,
            unfocusedContainerColor = if (nb.dark) nb.surfaceBase else nb.surfaceBase.copy(alpha = 0.85f),
            cursorColor = nb.accentBronze,
            focusedTextColor = nb.inkPrimary,
            unfocusedTextColor = nb.inkPrimary
        )
    )
}

@Composable
fun Toggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val nb = LocalNorthborn.current
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = NorthbornSpace.touchMin)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, Modifier.weight(1f).padding(end = 12.dp), fontSize = 14.sp, lineHeight = 20.sp, color = nb.inkPrimary)
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            modifier = Modifier.semantics { contentDescription = label },
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (nb.dark) nb.inkPrimary else nb.surfaceBase,
                checkedTrackColor = nb.accentBronze,
                checkedBorderColor = nb.accentBronze,
                uncheckedThumbColor = nb.inkSecondary,
                uncheckedTrackColor = nb.surfaceElevated,
                uncheckedBorderColor = nb.hairlineStrong
            )
        )
    }
}