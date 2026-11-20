package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import java.time.LocalDate

/**
 * ASCEND 1.7.5 — Conselho dos Nove (Áreas da Vida e Objetivos Pessoais)
 * 9 áreas opcionais personalizáveis com selos metálicos de nine_council_seals_atlas.webp,
 * além de Objetivos Pessoais trimestrais e anuais vinculados a Caminhos e Hábitos reais,
 * sem criar economia paralela no RPG.
 */

@Composable
fun CouncilOfNineScreen(
    s: GameState,
    vm: GameViewModel,
    initialTab: Int = 0
) {
    var tab by rememberSaveable(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    var showArchived by rememberSaveable { mutableStateOf(false) }
    var editingArea by remember { mutableStateOf<LifeArea?>(null) }
    var editingGoal by remember { mutableStateOf<StrategicGoal?>(null) }
    var horizonFilter by rememberSaveable { mutableIntStateOf(0) } // 0=Todos, 1=Trimestrais, 2=Anuais

    val todayDate = currentDay()
    val weekStart = remember(todayDate) { weekOf(todayDate) }
    val weekEnd = remember(weekStart) { LocalDate.parse(weekStart).plusDays(6).toString() }

    val areas = remember(s.lifeAreas, showArchived) {
        s.lifeAreas.filter { showArchived || !it.archived }.sortedBy { it.position }
    }
    val goals = remember(s.strategicGoals, horizonFilter, showArchived) {
        s.strategicGoals.filter { g ->
            (showArchived || !g.archived) && when (horizonFilter) {
                1 -> g.horizon == "quarterly"
                2 -> g.horizon == "annual"
                else -> true
            }
        }
    }

    LazyColumn(
        Modifier.fillMaxSize().testTag("council_nine_list"),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Title("Conselho dos Nove", "Áreas da vida opcionais e objetivos pessoais trimestrais e anuais com progresso verificável.")
        }
        item {
            AetherisTabs(listOf("Áreas da Vida (${areas.size})", "Objetivos (${goals.size})"), tab, { tab = it })
        }

        if (tab == 0) {
            item {
                CouncilMaterialSurface(CouncilMaterial.NordicWood) {
                    Text(
                        "Todas as áreas são opcionais: hábitos, caminhos, cadernos e compromissos sem área continuam funcionando normalmente.",
                        color = Muted,
                        fontSize = 12.sp
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (s.lifeAreas.isEmpty()) {
                            SagaButton("Ativar as 9 áreas padrão", { vm.run { vm.repo.seedDefaultLifeAreas() } }, Modifier.weight(1f))
                        }
                        SagaButton("+ Nova área", {
                            editingArea = LifeArea(name = "", sealIndex = s.lifeAreas.size % 9, position = s.lifeAreas.size)
                        }, Modifier.weight(1f), secondary = s.lifeAreas.isEmpty())
                    }
                    Toggle("Exibir áreas arquivadas", showArchived, { showArchived = it })
                }
            }
            items(areas, key = { it.id }) { area ->
                val weekMins = remember(s, area.id, weekStart, weekEnd) {
                    areaAllocatedMinutes(area.id, s, weekStart, weekEnd)
                }
                val linkedHabits = s.habits.count { !it.deleted && !it.archived && it.areaId == area.id }
                val linkedPaths = s.paths.count { it.state !in 3..5 && it.areaId == area.id }
                val linkedGoals = s.strategicGoals.count { !it.archived && it.areaId == area.id }

                CouncilMaterialSurface(CouncilMaterial.DarkIron, accent = if (area.archived) Muted else Gold) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LifeAreaSeal(area.sealIndex, Modifier.size(48.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(area.name + if (area.archived) " (arquivada)" else "", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            if (area.description.isNotBlank()) Text(area.description, color = Muted, fontSize = 12.sp)
                            Text(
                                "Semana: $weekMins / ${area.weeklyTargetMinutes} min · $linkedPaths caminhos · $linkedHabits hábitos · $linkedGoals metas",
                                color = Blue,
                                fontSize = 11.sp
                            )
                        }
                    }
                    if (area.weeklyTargetMinutes > 0) {
                        Meter((weekMins.toFloat() / area.weeklyTargetMinutes.toFloat()).coerceIn(0f, 1f), Gold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton({ editingArea = area }) { Text("Editar", color = Gold, fontSize = 12.sp) }
                        TextButton({ vm.run { vm.repo.reorderLifeArea(area.id, -1) } }) { Text("↑", color = Muted) }
                        TextButton({ vm.run { vm.repo.reorderLifeArea(area.id, 1) } }) { Text("↓", color = Muted) }
                        TextButton({ vm.run { vm.repo.archiveLifeArea(area.id, !area.archived) } }) {
                            Text(if (area.archived) "Reativar" else "Arquivar", color = Muted, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            item {
                CouncilMaterialSurface(CouncilMaterial.AgedVellum) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OdinCouncilOrnament(col = 1, row = 1, modifier = Modifier.size(28.dp))
                        Column(Modifier.weight(1f)) {
                            Eyebrow("Objetivos pessoais")
                            Text("Medidos apenas pelo avanço real dos Caminhos e Hábitos vinculados — zero economia paralela no RPG.", color = Muted, fontSize = 12.sp)
                        }
                    }
                    ChipRow(listOf("Todos", "Trimestrais", "Anuais"), horizonFilter, { horizonFilter = it })
                    SagaButton("+ Novo objetivo pessoal", {
                        val yr = LocalDate.parse(todayDate).year
                        editingGoal = StrategicGoal(title = "", horizon = "quarterly", periodLabel = "$yr · T${((LocalDate.parse(todayDate).monthValue - 1) / 3) + 1}")
                    }, Modifier.fillMaxWidth())
                }
            }
            if (goals.isEmpty()) {
                item {
                    CouncilMaterialSurface(CouncilMaterial.DarkIron) {
                        Text("Nenhum objetivo pessoal cadastrado ainda. Crie objetivos trimestrais ou anuais e vincule seus Caminhos e Hábitos.", color = Muted, fontSize = 13.sp)
                    }
                }
            } else {
                items(goals, key = { it.id }) { goal ->
                    val (pct, detail) = remember(goal, s, todayDate) { strategicGoalProgress(goal, s, todayDate) }
                    val area = s.lifeAreas.find { it.id == goal.areaId }
                    CouncilMaterialSurface(CouncilMaterial.AgedVellum, accent = if (pct >= 100) Green else Gold) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (area != null) {
                                LifeAreaSeal(area.sealIndex, Modifier.size(36.dp))
                            } else {
                                OdinCouncilOrnament(col = 1, row = 1, modifier = Modifier.size(32.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(goal.title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(
                                    "${if (goal.horizon == "annual") "Anual" else "Trimestral"}" +
                                        "${if (goal.periodLabel.isNotBlank()) " · ${goal.periodLabel}" else ""}" +
                                        "${if (area != null) " · ${area.name}" else ""}",
                                    color = Gold,
                                    fontSize = 11.sp
                                )
                            }
                            Text("$pct%", color = if (pct >= 100) Green else Gold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        if (goal.description.isNotBlank()) Text(goal.description, color = Muted, fontSize = 12.sp)
                        Meter((pct / 100f).coerceIn(0f, 1f), if (pct >= 100) Green else Gold)
                        Text(detail, color = Muted, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton({ editingGoal = goal }) { Text("Editar", color = Gold, fontSize = 12.sp) }
                            TextButton({ vm.run { vm.repo.archiveStrategicGoal(goal.id, !goal.archived) } }) {
                                Text(if (goal.archived) "Reativar" else "Arquivar", color = Muted, fontSize = 12.sp)
                            }
                            TextButton({ vm.run { vm.repo.deleteStrategicGoal(goal.id) } }) {
                                Text("Excluir", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo de edição de Área da Vida
    editingArea?.let { a ->
        var name by remember(a) { mutableStateOf(a.name) }
        var desc by remember(a) { mutableStateOf(a.description) }
        var sealIdx by remember(a) { mutableIntStateOf(a.sealIndex) }
        var targetMin by remember(a) { mutableStateOf(a.weeklyTargetMinutes.toString()) }
        AetherDialog(
            onDismissRequest = { editingArea = null },
            title = if (a.name.isBlank()) "Nova área da vida" else "Editar área",
            confirmButton = {
                SagaButton("Salvar", {
                    val saved = a.copy(
                        name = name.trim(),
                        description = desc.trim(),
                        sealIndex = sealIdx,
                        weeklyTargetMinutes = (targetMin.toIntOrNull() ?: 120).coerceIn(0, 10080)
                    )
                    editingArea = null
                    vm.run { vm.repo.saveLifeArea(saved) }
                }, enabled = name.isNotBlank())
            },
            dismissButton = { TextButton({ editingArea = null }) { Text("Cancelar") } }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FormField(name, { name = it }, "Nome da área")
                FormField(desc, { desc = it }, "Descrição")
                FormField(targetMin, { targetMin = it }, "Meta semanal (minutos)")
                Text("Selo metálico", color = Muted, fontSize = 12.sp)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (0..8).forEach { idx ->
                        val shape = CutCornerShape(6.dp)
                        Box(
                            Modifier
                                .size(48.dp)
                                .clip(shape)
                                .border(if (sealIdx == idx) 2.dp else 1.dp, if (sealIdx == idx) Gold else ColdIron.copy(alpha = 0.4f), shape)
                                .clickable { sealIdx = idx }
                                .padding(4.dp)
                        ) {
                            LifeAreaSeal(idx, Modifier.matchParentSize())
                        }
                    }
                }
            }
        }
    }

    // Diálogo de edição de Objetivo Pessoal
    editingGoal?.let { g ->
        var title by remember(g) { mutableStateOf(g.title) }
        var desc by remember(g) { mutableStateOf(g.description) }
        var horizon by remember(g) { mutableStateOf(g.horizon) }
        var period by remember(g) { mutableStateOf(g.periodLabel) }
        var areaId by remember(g) { mutableStateOf(g.areaId) }
        var linkedPaths by remember(g) { mutableStateOf(g.linkedPathIds.split(',').filter { it.isNotBlank() }.toSet()) }
        var linkedHabits by remember(g) { mutableStateOf(g.linkedHabitIds.split(',').filter { it.isNotBlank() }.toSet()) }

        AetherDialog(
            onDismissRequest = { editingGoal = null },
            title = if (g.title.isBlank()) "Novo objetivo pessoal" else "Editar objetivo",
            confirmButton = {
                SagaButton("Salvar", {
                    val saved = g.copy(
                        title = title.trim(),
                        description = desc.trim(),
                        horizon = horizon,
                        periodLabel = period.trim(),
                        areaId = areaId,
                        linkedPathIds = linkedPaths.joinToString(","),
                        linkedHabitIds = linkedHabits.joinToString(",")
                    )
                    editingGoal = null
                    vm.run { vm.repo.saveStrategicGoal(saved) }
                }, enabled = title.isNotBlank())
            },
            dismissButton = { TextButton({ editingGoal = null }) { Text("Cancelar") } }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FormField(title, { title = it }, "Título do objetivo")
                FormField(desc, { desc = it }, "Descrição")
                ChipRow(listOf("Trimestral", "Anual"), if (horizon == "annual") 1 else 0, { horizon = if (it == 1) "annual" else "quarterly" })
                FormField(period, { period = it }, "Período (ex.: 2026 · T2 ou 2026)")
                if (s.lifeAreas.isNotEmpty()) {
                    val activeAreas = s.lifeAreas.filter { !it.archived }
                    val names = listOf("Sem área") + activeAreas.map { it.name }
                    val ids = listOf("") + activeAreas.map { it.id }
                    Text("Área do Conselho", color = Muted, fontSize = 12.sp)
                    ChipRow(names, ids.indexOf(areaId).coerceAtLeast(0), { areaId = ids[it] })
                }
                if (s.paths.isNotEmpty()) {
                    Text("Vincular Caminhos:", color = Muted, fontSize = 12.sp)
                    s.paths.filter { it.state !in 4..5 }.take(6).forEach { p ->
                        val sel = p.id in linkedPaths
                        Toggle(p.name, sel, { linkedPaths = if (it) linkedPaths + p.id else linkedPaths - p.id })
                    }
                }
                if (s.habits.any { !it.deleted && !it.archived }) {
                    Text("Vincular Hábitos:", color = Muted, fontSize = 12.sp)
                    s.habits.filter { !it.deleted && !it.archived }.take(6).forEach { h ->
                        val sel = h.id in linkedHabits
                        Toggle(h.name, sel, { linkedHabits = if (it) linkedHabits + h.id else linkedHabits - h.id })
                    }
                }
            }
        }
    }
}
