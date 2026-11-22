package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

/**
 * ASCEND 1.7.5.1 — Caminhos 3.0 (Refinamento Visual DS 3.2.1)
 * Componentes integrados ao detalhe de cada Caminho:
 * - Dependências finish-to-start (sem ciclos, liberação manual com justificativa registrada)
 * - Caminho Crítico (CPM: ES/EF/LS/LF/folga, data por esforço vs disponibilidade real, premissas)
 * - Gráfico de Gantt interativo em Compose com trilhos metálicos, títulos completos legíveis,
 *   marcadores nórdicos de caminho crítico/dependência/marcos, linha de hoje e toque para inspecionar
 * - Quadro Kanban 4 colunas na MESMA PathTask com identidade material distinta e botões táteis acessíveis
 * - Marcos (PathMilestone) e 6 Modelos Reutilizáveis de Projeto
 */

@Composable
fun Path30AdvancedSection(
    path: PersonalPath,
    s: GameState,
    vm: GameViewModel,
    initialMode: Int = 0
) {
    var mode by rememberSaveable(path.id, initialMode) { mutableIntStateOf(initialMode.coerceIn(0, 3)) }
    val tabs = listOf("Gantt", "Kanban", "Crítico & Deps", "Marcos")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().testTag("path_30_panel")) {
        NorthbornDivider(NorthbornDividerStyle.DiamondKnot)
        NorthbornEditorialHeader(
            title = "Caminhos 3.0 · Planejamento de Projeto",
            kicker = "Cronograma & Fluxo",
            subtitle = "Gantt interativo em trilhos metálicos, Kanban unificado, caminho crítico e marcos sobre as mesmas tarefas."
        )
        AetherisTabs(tabs, mode, { mode = it })

        when (mode) {
            0 -> PathGanttPanel(path = path, s = s)
            1 -> PathKanbanPanel(path = path, s = s, vm = vm)
            2 -> PathCriticalAndDepsPanel(path = path, s = s, vm = vm)
            3 -> PathMilestonesPanel(path = path, s = s, vm = vm)
        }
    }
}

@Composable
fun PathGanttPanel(path: PersonalPath, s: GameState) {
    val todayDate = currentDay()
    var weeklyScale by rememberSaveable(path.id) { mutableStateOf(false) }
    var inspectedNode by remember { mutableStateOf<CriticalPathNode?>(null) }

    val cpm = remember(path, s, todayDate) { OdinPlanningEngine.computeCriticalPath(path, s, todayDate) }
    val milestones = remember(s.pathMilestones, path.id) {
        s.pathMilestones.filter { it.pathId == path.id }.sortedBy { it.targetDate }
    }
    val tasksById = remember(s.pathTasks) { s.pathTasks.associateBy { it.id } }
    val tasks = cpm.nodes
    val baseDate = remember(path.startDate, todayDate, tasks) {
        val starts = buildList {
            runCatching { LocalDate.parse(todayDate) }.getOrNull()?.let { add(it) }
            tasks.forEach { n ->
                runCatching { LocalDate.parse(n.task.scheduledDate.ifBlank { n.earliestDate }) }.getOrNull()?.let { add(it) }
            }
        }
        starts.minOrNull() ?: LocalDate.now()
    }
    val endDate = remember(path.deadline, baseDate, tasks, milestones) {
        val ends = buildList {
            add(baseDate.plusDays(13))
            if (path.deadline.isNotBlank()) runCatching { LocalDate.parse(path.deadline) }.getOrNull()?.let { add(it) }
            tasks.forEach { n ->
                runCatching { LocalDate.parse(n.finishDate) }.getOrNull()?.let { add(it) }
                if (n.task.deadline.isNotBlank()) runCatching { LocalDate.parse(n.task.deadline) }.getOrNull()?.let { add(it) }
            }
            milestones.forEach { m -> runCatching { LocalDate.parse(m.targetDate) }.getOrNull()?.let { add(it) } }
        }
        val maxEnd = ends.maxOrNull() ?: baseDate.plusDays(13)
        val span = ChronoUnit.DAYS.between(baseDate, maxEnd).coerceIn(7, 45)
        baseDate.plusDays(span)
    }

    val totalDays = (ChronoUnit.DAYS.between(baseDate, endDate).toInt() + 1).coerceIn(7, 46)
    val dayWidth = if (weeklyScale) 26.dp else 48.dp
    val todayOffsetDays = ChronoUnit.DAYS.between(baseDate, LocalDate.parse(todayDate)).toInt()
    val scroll = rememberScrollState()
    val nb = LocalNorthborn.current

    NorthbornSurface(role = NorthbornSurfaceRole.Strategic, material = NorthbornMaterial.VolcanicSlate) {
        NordicSectionHeader(
            eyebrow = "Cronograma Gantt (${tasks.size} tarefas)",
            subtitle = "Toque em qualquer trilho para consultar detalhes, folga e pré-requisitos.",
            rune = HistoricalFutharkRune.Raido
        )
        ChipRow(listOf("Escala diária", "Escala semanal"), if (weeklyScale) 1 else 0, { weeklyScale = it == 1 })

        if (tasks.isEmpty()) {
            Text("Adicione tarefas neste Caminho para visualizar a linha do tempo Gantt.", color = nb.inkMuted, fontSize = 13.sp)
        } else {
            val timelineWidth = dayWidth * totalDays
            val rowHeight = 56.dp
            Column(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scroll)
                    .testTag("gantt_chart_canvas"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cabeçalho de datas arquitetônico
                Row(
                    Modifier
                        .width(timelineWidth)
                        .clip(NorthbornShape.xs)
                        .background(nb.surfaceElevated)
                        .border(1.dp, nb.hairline, NorthbornShape.xs)
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val step = if (weeklyScale) 7 else 1
                    var i = 0
                    while (i < totalDays) {
                        val d = baseDate.plusDays(i.toLong())
                        val isToday = d.toString() == todayDate
                        Box(
                            Modifier.width(dayWidth * minOf(step, totalDays - i)).padding(horizontal = 3.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                if (weeklyScale) "S${(i / 7) + 1} (${d.dayOfMonth}/${d.monthValue})" else "${d.dayOfMonth}/${d.monthValue}",
                                color = if (isToday) nb.accentBronze else nb.inkMuted,
                                fontSize = 10.sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                        i += step
                    }
                }

                // Marcos no topo do Gantt com obelisco rúnico
                if (milestones.isNotEmpty()) {
                    Box(Modifier.width(timelineWidth).height(26.dp)) {
                        milestones.forEach { m ->
                            val off = ChronoUnit.DAYS.between(baseDate, runCatching { LocalDate.parse(m.targetDate) }.getOrElse { baseDate }).toInt().coerceIn(0, totalDays - 1)
                            Row(
                                Modifier
                                    .offset(x = dayWidth * off)
                                    .clip(CutCornerShape(4.dp))
                                    .background(if (m.completed) Green.copy(alpha = 0.16f) else Gold.copy(alpha = 0.14f))
                                    .border(1.dp, (if (m.completed) Green else Gold).copy(alpha = 0.5f), CutCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                NordicPlanningMark(PlanningMark.Milestone, Modifier.size(14.dp), tintFallback = if (m.completed) Green else Gold)
                                Text(m.title, color = if (m.completed) Green else Gold, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            }
                        }
                    }
                }

                // Trilhos metálicos com linha de hoje, título completo legível acima da barra e destaque de caminho crítico
                Box(Modifier.width(timelineWidth)) {
                    if (todayOffsetDays in 0 until totalDays) {
                        Box(
                            Modifier
                                .offset(x = dayWidth * todayOffsetDays)
                                .width(2.dp)
                                .height((tasks.size * (rowHeight.value + 8f)).dp.coerceAtLeast(56.dp))
                                .background(Gold.copy(alpha = 0.72f))
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        tasks.forEach { node ->
                            val stDate = runCatching { LocalDate.parse(node.task.scheduledDate.ifBlank { node.earliestDate }) }.getOrElse { baseDate }
                            val enDate = runCatching { LocalDate.parse(node.task.deadline.ifBlank { node.finishDate }) }.getOrElse { stDate }
                            val startIdx = ChronoUnit.DAYS.between(baseDate, stDate).toInt().coerceIn(0, totalDays - 1)
                            val spanDays = max(1, ChronoUnit.DAYS.between(stDate, if (enDate.isBefore(stDate)) stDate else enDate).toInt() + 1)
                                .coerceAtMost(totalDays - startIdx)
                            val hasDeps = s.taskDependencies.any { !it.overridden && it.taskId == node.task.id }
                            val barColor = when {
                                node.task.completed -> Green
                                node.isCritical -> Gold
                                else -> Blue
                            }
                            val isSelected = inspectedNode?.task?.id == node.task.id
                            val shape = CutCornerShape(5.dp)

                            Column(
                                Modifier
                                    .width(timelineWidth)
                                    .height(rowHeight)
                                    .clickable { inspectedNode = node },
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                // Rótulo completo legível (sem truncamento agressivo em barras estreitas)
                                Row(
                                    Modifier.offset(x = (dayWidth * startIdx).coerceAtMost((timelineWidth - 180.dp).coerceAtLeast(0.dp))),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    NordicPlanningMark(
                                        mark = when {
                                            node.task.completed -> PlanningMark.PlanCompleted
                                            node.isCritical -> PlanningMark.CriticalTask
                                            hasDeps -> PlanningMark.Dependency
                                            else -> PlanningMark.Deadline
                                        },
                                        modifier = Modifier.size(14.dp),
                                        tintFallback = barColor
                                    )
                                    Text(
                                        text = node.task.title,
                                        color = if (node.isCritical) Gold else Ink,
                                        fontSize = 11.sp,
                                        fontWeight = if (node.isCritical || isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                    val fullStatus = when {
                                        node.task.completed -> "Concluída"
                                        node.isCritical -> "Crítica"
                                        else -> "Folga ${node.slackMinutes}m"
                                    }
                                    Text(
                                        text = "${node.durationMinutes}m · $fullStatus${if (hasDeps) " · dep" else ""}",
                                        color = Muted,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                }
                                // Trilho metálico do Gantt
                                Box(
                                    Modifier
                                        .width(timelineWidth)
                                        .height(26.dp)
                                        .clip(NorthbornShape.xs)
                                        .background(nb.canvas)
                                        .border(0.5.dp, nb.hairline, NorthbornShape.xs)
                                ) {
                                    val barWidthDp = (dayWidth * spanDays).coerceAtLeast(48.dp)
                                    val innerLabel = when {
                                        barWidthDp >= 78.dp && node.task.completed -> "Concluída"
                                        barWidthDp >= 78.dp && node.isCritical -> "Crítica"
                                        barWidthDp >= 78.dp -> "Folga ${node.slackMinutes}m"
                                        node.task.completed -> "✓"
                                        node.isCritical -> "◆"
                                        else -> "+${node.slackMinutes}m"
                                    }
                                    Row(
                                        Modifier
                                            .offset(x = dayWidth * startIdx)
                                            .width(barWidthDp)
                                            .fillMaxHeight()
                                            .clip(shape)
                                            .background(barColor.copy(alpha = if (isSelected) 0.36f else 0.24f))
                                            .border(if (isSelected || node.isCritical) 1.5.dp else 1.dp, barColor.copy(alpha = 0.88f), shape)
                                            .padding(horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = if (barWidthDp >= 78.dp) Arrangement.Start else Arrangement.Center
                                    ) {
                                        Text(
                                            text = innerLabel,
                                            color = nb.inkPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            NorthbornDivider(NorthbornDividerStyle.Hairline)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.horizontalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    NordicPlanningMark(PlanningMark.CriticalTask, Modifier.size(14.dp), tintFallback = nb.accentBronze)
                    Text("Crítica", color = nb.accentBronze, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    NordicPlanningMark(PlanningMark.Dependency, Modifier.size(14.dp), tintFallback = nb.accentFjord)
                    Text("Regular / Dep.", color = nb.accentFjord, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    NordicPlanningMark(PlanningMark.PlanCompleted, Modifier.size(14.dp), tintFallback = nb.accentPatina)
                    Text("Concluída", color = nb.accentPatina, fontSize = 11.sp)
                }
                Text("| Hoje", color = nb.accentBronze, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            inspectedNode?.let { node ->
                val prereqTitles = s.taskDependencies
                    .filter { !it.overridden && it.taskId == node.task.id }
                    .mapNotNull { tasksById[it.dependsOnTaskId]?.title }
                NorthbornSurface(role = NorthbornSurfaceRole.Document, accent = nb.accentBronze) {
                    NordicSectionHeader("Tarefa inspecionada no Gantt", mark = if (node.isCritical) PlanningMark.CriticalTask else PlanningMark.Deadline)
                    Text(node.task.title, color = nb.inkPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(
                        "Estimativa: ${if (node.estimatedMissing) "sem estimativa (ref. ${node.durationMinutes} min)" else "${node.durationMinutes} min"} · " +
                            "Folga: ${node.slackMinutes} min · ${if (node.isCritical) "No caminho crítico" else "Com folga"}",
                        color = nb.inkSecondary,
                        fontSize = 12.sp
                    )
                    Text("Janela projetada: ${displayDate(node.earliestDate)} → ${displayDate(node.finishDate)}", color = nb.accentFjord, fontSize = 12.sp)
                    if (prereqTitles.isNotEmpty()) {
                        Text("Pré-requisitos: ${prereqTitles.joinToString(", ")}", color = nb.accentBronze, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PathKanbanPanel(path: PersonalPath, s: GameState, vm: GameViewModel) {
    val nb = LocalNorthborn.current
    val todayDate = currentDay()
    val tasks = remember(s.pathTasks, path.id) { s.pathTasks.filter { it.pathId == path.id && !it.archived } }
    val tasksById = remember(s.pathTasks) { s.pathTasks.associateBy { it.id } }
    var overrideTarget by remember { mutableStateOf<PathTask?>(null) }
    var overrideReason by remember { mutableStateOf("") }

    data class KanbanColumnSpec(
        val key: String,
        val title: String,
        val list: List<PathTask>,
        val accent: Color,
        val mark: PlanningMark,
        val rune: HistoricalFutharkRune
    )

    val cols = listOf(
        KanbanColumnSpec(
            key = "todo",
            title = "A fazer",
            list = tasks.filter {
                val st = OdinPlanningEngine.taskWorkflowState(it, tasksById, s.taskDependencies, todayDate)
                st == TaskWorkflowState.AVAILABLE || st == TaskWorkflowState.OVERDUE
            },
            accent = nb.accentBronze,
            mark = PlanningMark.Deadline,
            rune = HistoricalFutharkRune.Raido
        ),
        KanbanColumnSpec(
            key = "doing",
            title = "Em andamento",
            list = tasks.filter {
                OdinPlanningEngine.taskWorkflowState(it, tasksById, s.taskDependencies, todayDate) == TaskWorkflowState.IN_PROGRESS
            },
            accent = nb.accentFjord,
            mark = PlanningMark.CriticalTask,
            rune = HistoricalFutharkRune.Kenaz
        ),
        KanbanColumnSpec(
            key = "blocked",
            title = "Bloqueadas",
            list = tasks.filter {
                OdinPlanningEngine.taskWorkflowState(it, tasksById, s.taskDependencies, todayDate) == TaskWorkflowState.BLOCKED
            },
            accent = nb.accentEmber,
            mark = PlanningMark.Dependency,
            rune = HistoricalFutharkRune.Isa
        ),
        KanbanColumnSpec(
            key = "done",
            title = "Concluídas",
            list = tasks.filter { it.completed },
            accent = nb.accentPatina,
            mark = PlanningMark.PlanCompleted,
            rune = HistoricalFutharkRune.Jera
        )
    )

    // Colunas Kanban arquitetônicas abertas (sem caixa-em-caixa)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.testTag("kanban_board")) {
        cols.forEachIndexed { cIdx, col ->
            if (cIdx > 0) NorthbornDivider(NorthbornDividerStyle.Hairline)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.width(3.dp).height(18.dp).background(col.accent))
                    NordicPlanningMark(col.mark, Modifier.size(18.dp), tintFallback = col.accent)
                    Eyebrow("${col.title} (${col.list.size})", col.accent)
                    Spacer(Modifier.weight(1f))
                    FutharkRuneGlyph(col.rune, col.accent.copy(alpha = 0.72f), size = 14.dp)
                }
                if (col.list.isEmpty()) {
                    Text("Sem tarefas nesta coluna.", color = nb.inkMuted, fontSize = 12.sp)
                } else {
                    col.list.forEach { t ->
                        val blockers = OdinPlanningEngine.blockingPrerequisites(t.id, tasksById, s.taskDependencies)
                        val priorityLabel = priorityLabels[t.priority.coerceIn(priorityLabels.indices)]
                        NorthbornSurface(
                            role = NorthbornSurfaceRole.Operational,
                            accent = col.accent
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                Text(
                                    t.title,
                                    color = nb.inkPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                StatusSeal(priorityLabel, if (t.priority >= 2) nb.accentBronze else nb.inkMuted)
                            }
                            val meta = buildString {
                                if (t.estimateMinutes > 0) append("${t.estimateMinutes} min") else append("sem estimativa")
                                if (t.deadline.isNotBlank()) append(" · prazo ${displayDate(t.deadline)}")
                            }
                            Text(meta, color = nb.inkSecondary, fontSize = 11.5.sp)
                            if (blockers.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    NordicPlanningMark(PlanningMark.Dependency, Modifier.size(14.dp), tintFallback = nb.accentEmber)
                                    Text(
                                        "Aguarda: ${blockers.joinToString { it.title }}",
                                        color = nb.accentEmber,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            // Botões táteis e acessíveis de movimentação (nunca depende apenas de drag-and-drop!)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                if (col.key != "todo" && col.key != "blocked") {
                                    NordicCompactAction(
                                        text = "→ A fazer",
                                        onClick = { vm.run { vm.repo.setTaskKanbanColumn(t.id, "todo") } },
                                        accent = nb.accentBronze
                                    )
                                }
                                if (col.key != "doing") {
                                    NordicCompactAction(
                                        text = if (blockers.isNotEmpty()) "Liberar & Iniciar" else "→ Em andamento",
                                        onClick = {
                                            if (blockers.isNotEmpty()) {
                                                overrideTarget = t
                                                overrideReason = ""
                                            } else {
                                                vm.run { vm.repo.setTaskKanbanColumn(t.id, "doing") }
                                            }
                                        },
                                        accent = nb.accentFjord,
                                        rune = if (blockers.isNotEmpty()) HistoricalFutharkRune.Thurisaz else HistoricalFutharkRune.Kenaz
                                    )
                                }
                                if (col.key != "done") {
                                    NordicCompactAction(
                                        text = "→ Concluir",
                                        onClick = { vm.run { vm.repo.setTaskKanbanColumn(t.id, "done") } },
                                        accent = nb.accentPatina,
                                        rune = HistoricalFutharkRune.Fehu
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    overrideTarget?.let { t ->
        AetherDialog(
            onDismissRequest = { overrideTarget = null },
            title = "Liberar dependência manualmente?",
            confirmButton = {
                SagaButton(
                    "Confirmar liberação",
                    {
                        val target = t
                        val reason = overrideReason
                        overrideTarget = null
                        vm.run { vm.repo.setTaskKanbanColumn(target.id, "doing", reason) }
                    },
                    enabled = overrideReason.isNotBlank(),
                    style = VikingButtonStyle.Confirmacao
                )
            },
            dismissButton = { TextButton({ overrideTarget = null }) { Text("Cancelar") } }
        ) {
            Text("A tarefa \"${t.title}\" possui pré-requisitos abertos. Registre a justificativa para liberar o início manual:", fontSize = 13.sp, color = Muted)
            FormField(overrideReason, { overrideReason = it }, "Motivo da liberação manual")
        }
    }
}

@Composable
fun PathCriticalAndDepsPanel(path: PersonalPath, s: GameState, vm: GameViewModel) {
    val todayDate = currentDay()
    val tasks = remember(s.pathTasks, path.id) { s.pathTasks.filter { it.pathId == path.id && !it.archived } }
    val tasksById = remember(tasks) { tasks.associateBy { it.id } }
    val taskIds = remember(tasks) { tasks.map { it.id }.toSet() }
    val pathDeps = remember(s.taskDependencies, taskIds) {
        s.taskDependencies.filter { it.taskId in taskIds && it.dependsOnTaskId in taskIds }
    }
    val cpm = remember(path, s, todayDate) { OdinPlanningEngine.computeCriticalPath(path, s, todayDate) }

    var depTaskIdx by remember(tasks) { mutableIntStateOf(0) }
    var preTaskIdx by remember(tasks) { mutableIntStateOf(if (tasks.size > 1) 1 else 0) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Caminho Crítico (CPM)
        CouncilMaterialSurface(CouncilMaterial.HammeredBronze, accent = if (cpm.deadlineRisk) MaterialTheme.colorScheme.error else Gold) {
            NordicSectionHeader(
                eyebrow = "Caminho Crítico (CPM)",
                mark = if (cpm.deadlineRisk) PlanningMark.Conflict else PlanningMark.CriticalTask,
                accent = if (cpm.deadlineRisk) MaterialTheme.colorScheme.error else Gold
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CouncilKpiTile("Esforço crítico", "${cpm.totalCriticalMinutes} min", "Projeção esforço: ${cpm.effortProjectedDate?.let { displayDate(it) } ?: "—"}", Gold, Modifier.weight(1f), rune = HistoricalFutharkRune.Tiwaz)
                CouncilKpiTile(
                    "Data real",
                    cpm.availabilityProjectedDate?.let { displayDate(it) } ?: "Indefinida",
                    if (cpm.deadlineRisk) "Risco de atraso no prazo" else "Pela disponibilidade",
                    if (cpm.deadlineRisk) MaterialTheme.colorScheme.error else Green,
                    Modifier.weight(1f),
                    rune = HistoricalFutharkRune.Dagaz
                )
            }
            if (cpm.criticalSequence.isNotEmpty()) {
                NordicStrategyDivider(StrategyFrameAsset.UrnesDivider)
                Text("Sequência crítica:", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                cpm.criticalSequence.forEachIndexed { idx, n ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        NordicPlanningMark(PlanningMark.CriticalTask, Modifier.size(15.dp))
                        Text("${idx + 1}. ${n.task.title} (${n.durationMinutes} min · folga 0)", color = Ink, fontSize = 13.sp)
                    }
                }
            }
            cpm.premises.forEach { p -> Text("• $p", color = Muted, fontSize = 11.sp) }
        }

        // Editor de dependências finish-to-start
        CouncilMaterialSurface(CouncilMaterial.DarkIron) {
            NordicSectionHeader("Dependências (término → início)", mark = PlanningMark.Dependency, accent = Blue)
            if (tasks.size < 2) {
                Text("Crie ao menos duas tarefas neste Caminho para estabelecer dependências.", color = Muted, fontSize = 12.sp)
            } else {
                Text("Tarefa dependente:", color = Muted, fontSize = 12.sp)
                ChipRow(tasks.map { it.title.take(18) }, depTaskIdx.coerceIn(tasks.indices), { depTaskIdx = it })
                Text("Depende do término de:", color = Muted, fontSize = 12.sp)
                ChipRow(tasks.map { it.title.take(18) }, preTaskIdx.coerceIn(tasks.indices), { preTaskIdx = it })
                SagaButton(
                    "+ Vincular dependência",
                    {
                        val depTask = tasks[depTaskIdx.coerceIn(tasks.indices)]
                        val preTask = tasks[preTaskIdx.coerceIn(tasks.indices)]
                        vm.run { vm.repo.addTaskDependency(depTask.id, preTask.id) }
                    },
                    Modifier.fillMaxWidth(),
                    style = VikingButtonStyle.FerroRunico,
                    leadingRune = HistoricalFutharkRune.Gebo
                )
            }

            if (pathDeps.isEmpty()) {
                Text("Nenhuma dependência cadastrada neste Caminho.", color = Muted, fontSize = 12.sp)
            } else {
                pathDeps.forEach { dep ->
                    val tName = tasksById[dep.taskId]?.title ?: dep.taskId
                    val pName = tasksById[dep.dependsOnTaskId]?.title ?: dep.dependsOnTaskId
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NordicPlanningMark(PlanningMark.Dependency, Modifier.size(16.dp), tintFallback = Blue)
                        Column(Modifier.weight(1f)) {
                            Text("$pName  →  $tName", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            if (dep.overridden) {
                                Text("Liberada em ${displayDate(dep.overrideDate)}: ${dep.overrideReason}", color = Gold, fontSize = 11.sp)
                            }
                        }
                        TextButton({ vm.run { vm.repo.removeTaskDependency(dep.id) } }) {
                            Text("Remover", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PathMilestonesPanel(path: PersonalPath, s: GameState, vm: GameViewModel) {
    val milestones = remember(s.pathMilestones, path.id) {
        s.pathMilestones.filter { it.pathId == path.id }.sortedBy { it.targetDate }
    }
    var title by rememberSaveable(path.id) { mutableStateOf("") }
    var targetDate by rememberSaveable(path.id) { mutableStateOf(path.deadline.ifBlank { today() }) }

    CouncilMaterialSurface(CouncilMaterial.AgedVellum) {
        NordicSectionHeader("Marcos do Caminho (${milestones.count { it.completed }}/${milestones.size})", mark = PlanningMark.Milestone)
        FormField(title, { title = it }, "Título do marco (ex.: Protótipo validado)")
        FormField(targetDate, { targetDate = it }, "Data-alvo (AAAA-MM-DD)")
        SagaButton(
            "+ Adicionar marco",
            {
                vm.run {
                    vm.repo.savePathMilestone(
                        PathMilestone(
                            pathId = path.id,
                            title = title.trim(),
                            targetDate = targetDate.trim(),
                            position = milestones.size
                        )
                    )
                    title = ""
                }
            },
            Modifier.fillMaxWidth(),
            style = VikingButtonStyle.BronzeForjado,
            leadingRune = HistoricalFutharkRune.Sowilo
        )

        if (milestones.isEmpty()) {
            Text("Nenhum marco registrado ainda.", color = Muted, fontSize = 12.sp)
        } else {
            milestones.forEach { m ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NordicPlanningMark(if (m.completed) PlanningMark.PlanCompleted else PlanningMark.Milestone, Modifier.size(22.dp), tintFallback = if (m.completed) Green else Gold)
                    Column(Modifier.weight(1f)) {
                        Text(m.title, color = if (m.completed) Green else Ink, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("Alvo: ${displayDate(m.targetDate)}${if (m.completed) " · Concluído" else ""}", color = Muted, fontSize = 11.sp)
                    }
                    NordicCompactAction(
                        text = if (m.completed) "Reabrir" else "Concluir",
                        onClick = { vm.run { vm.repo.togglePathMilestone(m.id) } },
                        accent = if (m.completed) Gold else Green
                    )
                    TextButton({ vm.run { vm.repo.deletePathMilestone(m.id) } }) {
                        Text("×", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectTemplatesDialog(
    s: GameState,
    vm: GameViewModel,
    onDismiss: () -> Unit,
    onCreated: (String) -> Unit
) {
    var selectedId by remember { mutableStateOf(projectTemplates.first().id) }
    var customName by remember { mutableStateOf("") }
    val selected = projectTemplates.first { it.id == selectedId }

    AetherDialog(
        onDismissRequest = onDismiss,
        title = "6 Modelos de Projeto",
        confirmButton = {
            SagaButton(
                "Criar Caminho",
                {
                    vm.run {
                        val newId = vm.repo.createPathFromTemplate(selected.id, customName.trim())
                        onDismiss()
                        onCreated(newId)
                    }
                },
                style = VikingButtonStyle.CerimonialViking,
                leadingRune = HistoricalFutharkRune.Raido
            )
        },
        dismissButton = { TextButton(onDismiss) { Text("Cancelar") } }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Modelos estruturam etapas, tarefas, dependências e marcos editáveis sem copiar histórico anterior.", color = Muted, fontSize = 12.sp)
            ChipRow(
                projectTemplates.map { it.name },
                projectTemplates.indexOfFirst { it.id == selectedId }.coerceAtLeast(0),
                { selectedId = projectTemplates[it].id }
            )
            Text(selected.summary, color = Ink, fontSize = 13.sp)
            Text(
                "${selected.stages.size} etapas · ${selected.tasks.size} tarefas · ${selected.milestones.size} marcos · horizonte de ${selected.durationDays} dias",
                color = Gold,
                fontSize = 12.sp
            )
            FormField(customName, { customName = it }, "Nome personalizado (opcional)")
        }
    }
}
