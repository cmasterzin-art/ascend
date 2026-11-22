package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*

object AetherTokens {
    val page = NorthbornSpace.page
    val gap = NorthbornSpace.lg
    val inset = NorthbornSpace.lg
    val small = NorthbornSpace.sm
    val touch = NorthbornSpace.touchMin
    val shape = NorthbornShape.md
    const val quick = 140
    const val scene = 220
}

enum class SurfaceKind { Operational, Heroic, Ceremonial, Parchment, Artifact }

/**
 * Superfície unificada sobre NORTHBORN 4.0: elimina etiquetas automáticas repetitivas
 * ("SAGA PESSOAL", "MARCA DE AETHERIS") e excesso de cantoneiras em todos os cartões.
 */
@Composable
fun AetherSurface(kind: SurfaceKind = SurfaceKind.Operational, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val nb = LocalNorthborn.current
    val role = when (kind) {
        SurfaceKind.Operational -> NorthbornSurfaceRole.Operational
        SurfaceKind.Parchment -> NorthbornSurfaceRole.Document
        SurfaceKind.Heroic -> NorthbornSurfaceRole.Strategic
        SurfaceKind.Ceremonial -> NorthbornSurfaceRole.Ceremonial
        SurfaceKind.Artifact -> NorthbornSurfaceRole.Ceremonial
    }
    val mat = when (kind) {
        SurfaceKind.Artifact -> NorthbornMaterial.ForgedIron
        SurfaceKind.Parchment -> NorthbornMaterial.ArchivalVellum
        SurfaceKind.Heroic -> NorthbornMaterial.VolcanicSlate
        SurfaceKind.Ceremonial -> NorthbornMaterial.SmokedOak
        else -> NorthbornMaterial.ForgedIron
    }
    val accent = when (kind) {
        SurfaceKind.Heroic -> nb.accentFjord
        SurfaceKind.Ceremonial -> nb.accentBronze
        SurfaceKind.Parchment -> nb.accentBronze
        SurfaceKind.Artifact -> Violet
        else -> nb.hairlineStrong
    }
    NorthbornSurface(
        role = role,
        modifier = modifier,
        material = mat,
        accent = accent,
        content = content
    )
}

/**
 * Diálogo NORTHBORN 4.0: preserva a semântica de AlertDialog para acessibilidade e Robolectric,
 * com superfície limpa de ferro/velino, divisor geométrico e zero moldura rasterizada borrada.
 */
@Composable
fun AetherDialog(
    onDismissRequest: () -> Unit,
    title: String,
    kind: SurfaceKind = SurfaceKind.Operational,
    seal: SealKind? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val nb = LocalNorthborn.current
    val accent = when (kind) {
        SurfaceKind.Ceremonial -> nb.accentBronze
        SurfaceKind.Heroic -> nb.accentFjord
        SurfaceKind.Artifact -> Violet
        SurfaceKind.Parchment -> nb.accentBronze
        else -> nb.hairlineStrong
    }
    val container = if (kind == SurfaceKind.Parchment) nb.surfaceDocument else nb.surfaceElevated
    val shape = NorthbornShape.lg

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.border(1.dp, accent.copy(alpha = 0.55f), shape),
        shape = shape,
        containerColor = container,
        titleContentColor = nb.inkPrimary,
        textContentColor = nb.inkPrimary,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (seal != null) CeremonialSeal(seal, true, Modifier.size(32.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (kind == SurfaceKind.Ceremonial) nb.accentBronze else nb.inkPrimary
                    )
                }
                NorthbornDivider(NorthbornDividerStyle.DiamondKnot, color = accent.copy(alpha = 0.65f))
            }
        },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = content) },
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
}

@Composable
fun MetricPair(a: String, al: String, b: String, bl: String) {
    val nb = LocalNorthborn.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(a, fontSize = 28.sp, lineHeight = 34.sp, fontFamily = SagaFont, color = nb.inkPrimary)
            Text(al, color = nb.inkSecondary, fontSize = 12.sp, lineHeight = 17.sp)
        }
        Box(Modifier.width(1.dp).height(44.dp).background(nb.hairline))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(b, fontSize = 28.sp, lineHeight = 34.sp, fontFamily = SagaFont, color = nb.inkPrimary)
            Text(bl, color = nb.inkSecondary, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}

@Composable
fun SectionHeader(title: String, sub: String = "") {
    NorthbornEditorialHeader(title = title, subtitle = sub)
}

/**
 * Cabeçalho heroico da Jornada (NORTHBORN 4.0): pintura do fiorde dissolvida suavemente no fundo
 * sem caixa artificial, retrato integrado com luz fria discreta e identidade editorial limpa.
 */
@Composable
fun CompactHero(s: GameState, onPortrait: () -> Unit = {}) {
    val p = requireNotNull(s.player)
    val minimal = LocalAppearance.current.minimal
    val nb = LocalNorthborn.current
    val night = nb.canvas
    val cold = if (nb.dark) Color(0xFFB8C7D9) else Color.White

    BoxWithConstraints(
        Modifier.fillMaxWidth().layout { m, c ->
            val bleed = 20.dp.roundToPx()
            val placeable = m.measure(c.copy(minWidth = c.maxWidth + 2 * bleed, maxWidth = c.maxWidth + 2 * bleed))
            layout(c.maxWidth, placeable.height) { placeable.place(-bleed, 0) }
        }
    ) {
        val narrow = maxWidth < 360.dp
        val portrait = if (narrow) 56.dp else 72.dp
        if (!minimal) {
            Image(
                androidx.compose.ui.res.painterResource(R.drawable.nordic_fjord),
                null,
                Modifier.matchParentSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                alignment = Alignment.TopCenter,
                alpha = if (nb.dark) 0.72f else 0.34f
            )
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(
                        0f to night.copy(alpha = 0.72f),
                        0.24f to night.copy(alpha = 0.16f),
                        0.58f to night.copy(alpha = 0.45f),
                        0.86f to night.copy(alpha = 0.92f),
                        1f to night
                    )
                )
            )
            Box(
                Modifier.matchParentSize().background(
                    Brush.horizontalGradient(
                        0f to night.copy(alpha = 0.48f),
                        0.20f to Color.Transparent,
                        0.80f to Color.Transparent,
                        1f to night.copy(alpha = 0.48f)
                    )
                )
            )
        }
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = if (narrow) 64.dp else 74.dp, bottom = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(if (narrow) 12.dp else 16.dp)) {
                Box(
                    Modifier
                        .drawBehind {
                            if (!minimal) {
                                val r = size.maxDimension * 0.85f
                                drawCircle(Brush.radialGradient(listOf(cold.copy(alpha = 0.16f), Color.Transparent), center = Offset(size.width / 2, size.height * 0.4f), radius = r), r, Offset(size.width / 2, size.height * 0.4f))
                            }
                        }
                        .clickable(onClickLabel = "Ampliar retrato", onClick = onPortrait)
                ) {
                    ForgePortrait(s, portrait)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (s.heraldry != null) {
                            HeraldicCrest(s.heraldry, size = if (narrow) 20.dp else 24.dp)
                        }
                        Text(
                            p.name,
                            style = if (narrow) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                            color = nb.inkPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(classes[p.heroClass], color = nb.accentFjord, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                        if (!narrow) {
                            Text("·", color = nb.inkTertiary, fontSize = 12.sp)
                            Text(p.title, color = nb.inkSecondary, fontSize = 12.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    if (narrow) Text(p.title, color = nb.inkSecondary, fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(2.dp))
                    Meter(Progression.current(s.xp).toFloat() / Progression.requirement(s.level), nb.accentFjord, slim = true)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${Progression.current(s.xp)}/${Progression.requirement(s.level)} XP", color = nb.accentFjord, fontSize = if (narrow) 10.5.sp else 11.5.sp, maxLines = 1)
                        Text("${s.coins} ${if (narrow) "frag." else "fragmentos"}", fontSize = if (narrow) 10.5.sp else 11.5.sp, color = nb.inkSecondary, maxLines = 1)
                    }
                }
                Box(Modifier.semantics(mergeDescendants = true) { contentDescription = "Nível ${s.level}" }) {
                    ForgeMedallion(s.level, Modifier.size(if (narrow) 40.dp else 48.dp))
                }
            }
        }
    }
}

/** Progresso diário editorial em linha aberta, sem cartão redundante. */
@Composable
fun DayLedger(done: Int, total: Int, rested: Boolean, relaxed: Boolean, dial: Dp) {
    val nb = LocalNorthborn.current
    val pending = (total - done).coerceAtLeast(0)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        DayDial(if (total == 0) 0f else done.toFloat() / total, if (total == 0) "—" else "${done * 100 / total}%", dial)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Eyebrow(if (rested) "Dia de descanso" else "Jornada de hoje", if (rested) nb.accentPatina else nb.accentBronze)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("$done de $total", style = MaterialTheme.typography.titleLarge, color = nb.inkPrimary)
                Text(
                    "$pending ${if (pending == 1) "pendente" else "pendentes"}",
                    fontSize = 12.5.sp,
                    color = nb.inkSecondary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            if (relaxed) Text("Modo livre · o widget mantém a agenda programada.", fontSize = 11.5.sp, color = nb.inkSecondary)
        }
    }
}

/** Contrato editorial do códice (NORTHBORN 4.0): leitura limpa em velino ou carvalho, sem rótulos automáticos duplicados. */
@Composable
fun MissionCard2(q: Quest, s: GameState, vm: GameViewModel, category: Int, value: Long, claimed: Boolean, onEdit: () -> Unit) {
    val nb = LocalNorthborn.current
    val grand = category == 1 || category == 2
    val role = if (category == 0) NorthbornSurfaceRole.Document else NorthbornSurfaceRole.Ceremonial
    val accent = if (claimed) nb.accentPatina else if (category == 0) nb.accentBronze else nb.accentPaleGold

    NorthbornSurface(role = role, accent = accent) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            CeremonialSeal(
                if (claimed) SealKind.Reward else listOf(SealKind.Mission, SealKind.Oath, SealKind.Saga, SealKind.Mission)[category],
                claimed || value >= q.target,
                Modifier.size(if (grand) 44.dp else 34.dp)
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Eyebrow(listOf("Contrato diário", "Juramento semanal", "Contrato épico", "Desafio ritual")[category], accent)
                Text(q.name, color = nb.inkPrimary, style = if (grand) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge)
            }
        }
        if (category == 3) {
            BossPortrait()
            Text("Vida: ${(100 * (1 - value.toFloat() / q.target)).coerceIn(0f, 100f).toInt()} / 100", color = nb.inkSecondary)
        }
        Text(q.description, color = nb.inkSecondary, fontSize = 14.sp, lineHeight = 22.sp)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${value.coerceAtMost(q.target.toLong())} / ${q.target}", fontSize = 21.sp, fontFamily = SagaFont, color = nb.inkPrimary)
                    Text(listOf("conclusões", "minutos", "XP de hábitos", "páginas")[q.metric], color = nb.inkSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 2.dp))
                }
                StatusSeal(
                    if (claimed) "Resgatado" else if (value >= q.target) "Disponível" else "Em andamento",
                    if (claimed) nb.accentPatina else nb.accentFjord
                )
            }
            Meter(value.toFloat() / q.target, if (claimed) nb.accentPatina else nb.accentFjord, slim = !grand)
        }
        val related = s.habits.filter {
            !it.deleted && (q.habitIds.isBlank() || it.id in q.habitIds.split(',')) &&
                (q.attribute < 0 || it.attribute == q.attribute) &&
                (q.metric != 1 || it.type == 2) &&
                (q.metric != 3 || it.unit.contains("página", true))
        }
        Text("Contribuem: " + related.joinToString { it.name }.ifBlank { "Nenhum hábito compatível" }, color = nb.inkSecondary, fontSize = 12.sp, lineHeight = 18.sp)
        if (q.deadline.isNotBlank()) Text("Prazo: ${displayDate(q.deadline)}", color = nb.inkSecondary, fontSize = 12.sp)
        NorthbornDivider(NorthbornDividerStyle.Hairline)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Column {
                Text("${q.xp} XP", fontSize = if (grand) 20.sp else 17.sp, lineHeight = 22.sp, fontFamily = SagaFont, color = nb.inkPrimary)
                Text("recompensa final", color = nb.inkSecondary, fontSize = 11.sp)
            }
            Column {
                Text("${q.coins}", fontSize = if (grand) 20.sp else 17.sp, lineHeight = 22.sp, fontFamily = SagaFont, color = nb.inkPrimary)
                Text("fragmentos", color = nb.inkSecondary, fontSize = 11.sp)
            }
        }
        if (q.special.isNotBlank()) Text("Troféu: ${q.special}", color = nb.accentBronze, fontSize = 13.sp)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NorthbornButton(
                text = if (claimed) "Recompensa resgatada" else "Resgatar recompensa",
                onClick = { vm.run { vm.repo.claim(q) } },
                role = if (!claimed && value >= q.target) NorthbornButtonRole.Ceremonial else NorthbornButtonRole.Primary,
                modifier = Modifier.weight(1f),
                enabled = !claimed && value >= q.target,
                completed = claimed
            )
            NorthbornButton("Editar missão", onEdit, role = NorthbornButtonRole.Tertiary)
        }
    }
}
