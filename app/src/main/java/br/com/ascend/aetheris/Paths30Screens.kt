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
 * ASCEND 1.7.5 — Caminhos 3.0
 * Componentes integrados ao detalhe de cada Caminho:
 * - Dependências finish-to-start (sem ciclos, liberação manual com justificativa registrada)
 * - Caminho Crítico (CPM: ES/EF/LS/LF/folga, data por esforço vs disponibilidade real, premissas)
 * - Gráfico de Gantt interativo em Compose (escala diária/semanal, linha de hoje, marcos, tarefas críticas, toque para inspecionar)
 * - Quadro Kanban 4 colunas na MESMA PathTask (A fazer, Em andamento, Bloqueadas, Concluídas) com botões acessíveis
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
        CouncilMaterialSurface(CouncilMaterial.ProjectLeather) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StrategyControlGlyph(col = 0, row = 0, modifier = Modifier.size(24.dp))
                Column(Modifier.weight(1f)) {
                    Eyebrow("Caminhos 3.0 · Planejamento de Projeto")
                    Text("Gantt interativo, Kanban unificado, caminho crítico e marcos sobre as mesmas tarefas.", color = Muted, fontSize = 12.sp)
                }
            }
            AetherisTabs(tabs, mode, { mode = it })
        }

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
    val dayWidth = if (weeklyScale) 22.dp else 44.dp
    val todayOffsetDays = ChronoUnit.DAYS.between(baseDate, LocalDate.parse(todayDate)).toInt()
    val scroll = rememberScrollState()

    CouncilMaterialSurface(CouncilMaterial.DarkIron) {
        Eyebrow("Cronograma Gantt (${tasks.size} tarefas)")
        ChipRow(listOf("Escala diária", "Escala semanal"), if (weeklyScale) 1 else 0, { weeklyScale = it == 1 })

        if (tasks.isEmpty()) {
            Text("Adicione tarefas neste Caminho para visualizar a linha do tempo Gantt.", color = Muted, fontSize = 13.sp)
        } else {
            val timelineWidth = dayWidth * totalDays
            Column(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scroll)
                    .testTag("gantt_chart_canvas"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cabeçalho de datas
                Row(Modifier.width(timelineWidth), verticalAlignment = Alignment.CenterVertically) {
                    val step = if (weeklyScale) 7 else 1
                    var i = 0
                    while (i < totalDays) {
                        val d = baseDate.plusDays(i.toLong())
                        val isToday = d.toString() == todayDate
                        Box(
                            Modifier.width(dayWidth * minOf(step, totalDays - i)).padding(vertical = 2.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                if (weeklyScale) "S${(i / 7) + 1} (${d.dayOfMonth}/${d.monthValue})" else "${d.dayOfMonth}/${d.monthValue}",
                                color = if (isToday) Gold else Muted,
                                fontSize = 10.sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                        i += step
                    }
                }

                // Marcos no topo do Gantt
                if (milestones.isNotEmpty()) {
                    Box(Modifier.width(timelineWidth).height(26.dp)) {
                        milestones.forEach { m ->
                            val off = ChronoUnit.DAYS.between(baseDate, runCatching { LocalDate.parse(m.targetDate) }.getOrElse { baseDate }).toInt().coerceIn(0, totalDays - 1)
                            Row(
                                Modifier.offset(x = dayWidth * off),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                StrategyControlGlyph(col = 2, row = 0, modifier = Modifier.size(16.dp))
                                Text(m.title, color = if (m.completed) Green else Gold, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }

                // Trilhos de tarefas com linha de hoje e destaque de caminho crítico
                Box(Modifier.width(timelineWidth)) {
                    if (todayOffsetDays in 0 until totalDays) {
                        Box(
                            Modifier
                                .offset(x = dayWidth * todayOffsetDays)
                                .width(2.dp)
                                .height((tasks.size * 38).dp.coerceAtLeast(48.dp))
                                .background(Gold.copy(alpha = 0.65f))
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        tasks.forEach { node ->
                            val stDate = runCatching { LocalDate.parse(node.task.scheduledDate.ifBlank { node.earliestDate }) }.getOrElse { baseDate }
                            val enDate = runCatching { LocalDate.parse(node.task.deadline.ifBlank { node.finishDate }) }.getOrElse { stDate }
                            val startIdx = ChronoUnit.DAYS.between(baseDate, stDate).toInt().coerceIn(0, totalDays - 1)
                            val spanDays = max(1, ChronoUnit.DAYS.between(stDate, if (enDate.isBefore(stDate)) stDate else enDate).toInt() + 1)
                                .coerceAtMost(totalDays - startIdx)
                            val barColor = when {
                                node.task.completed -> Green
                                node.isCritical -> Gold
                                else -> Blue
                            }
                            val shape = CutCornerShape(4.dp)
                            Row(
                                Modifier
                                    .offset(x = dayWidth * startIdx)
                                    .width((dayWidth * spanDays).coerceAtLeast(88.dp))
                                    .height(32.dp)
                                    .clip(shape)
                                    .background(barColor.copy(alpha = 0.24f))
                                    .border(1.dp, barColor.copy(alpha = 0.85f), shape)
                                    .clickable { inspectedNode = node }
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                if (node.isCritical) StrategyControlGlyph(col = 3, row = 0, modifier = Modifier.size(14.dp))
                                Text(
                                    node.task.title,
                                    color = Ink,
                                    fontSize = 11.sp,
                                    fontWeight = if (node.isCritical) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("■ Crítica", color = Gold, fontSize = 11.sp)
                Text("■ Regular", color = Blue, fontSize = 11.sp)
                Text("■ Concluída", color = Green, fontSize = 11.sp)
                Text("| Hoje", color = Gold, fontSize = 11.sp)
            }

            inspectedNode?.let { node ->
                CouncilMaterialSurface(CouncilMaterial.AgedVellum, accent = Gold) {
                    Eyebrow("Tarefa inspecionada no Gantt")
                    Text(node.task.title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        "Estimativa: ${if (node.estimatedMissing) "sem estimativa (ref. ${node.durationMinutes} min)" else "${node.durationMinutes} min"} · " +
                            "Folga: ${node.slackMinutes} min · ${if (node.isCritical) "No caminho crítico" else "Com folga"}",
                        color = Muted,
                        fontSize = 12.sp
                    )
                    Text("Janela projetada: ${displayDate(node.earliestDate)} → ${displayDate(node.finishDate)}", color = Blue, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PathKanbanPanel(path: PersonalPath, s: GameState, vm: GameViewModel) {
    val todayDate = currentDay()
    val tasks = remember(s.pathTasks, path.id) { s.pathTasks.filter { it.pathId == path.id && !it.archived } }
    val tasksById = remember(s.pathTasks) { s.pathTasks.associateBy { it.id } }
    var overrideTarget by remember { mutableStateOf<PathTask?>(null) }
    var overrideReason by remember { mutableStateOf("") }

    val cols = listOf(
        Triple("todo", "A fazer", tasks.filter {
            val st = OdinPlanningEngine.taskWorkflowState(it, tasksById, s.taskDependencies, todayDate)
            st == TaskWorkflowState.AVAILABLE || st == TaskWorkflowState.OVERDUE
        }),
        Triple("doing", "Em andamento", tasks.filter {
            OdinPlanningEngine.taskWorkflowState(it, tasksById, s.taskDependencies, todayDate) == TaskWorkflowState.IN_PROGRESS
        }),
        Triple("blocked", "Bloqueadas", tasks.filter {
            OdinPlanningEngine.taskWorkflowState(it, tasksById, s.taskDependencies, todayDate) == TaskWorkflowState.BLOCKED
        }),
        Triple("done", "Concluídas", tasks.filter { it.completed })
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.testTag("kanban_board")) {
        cols.forEach { (colKey, colTitle, list) ->
            val accent = when (colKey) {
                "doing" -> Blue
                "blocked" -> MaterialTheme.colorScheme.error
                "done" -> Green
                else -> Gold
            }
            CouncilMaterialSurface(CouncilMaterial.DarkIron, accent = accent) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StrategyControlGlyph(col = 1, row = 1, modifier = Modifier.size(18.dp))
                    Eyebrow("$colTitle (${list.size})", accent)
                }
                if (list.isEmpty()) {
                    Text("Sem tarefas nesta coluna.", color = Muted, fontSize = 12.sp)
                } else {
                    list.forEach { t ->
                        val blockers = OdinPlanningEngine.blockingPrerequisites(t.id, tasksById, s.taskDependencies)
                        val shape = CutCornerShape(6.dp)
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .background(Surface.copy(alpha = 0.65f))
                                .border(1.dp, accent.copy(alpha = 0.35f), shape)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(t.title, color = Ink, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            val meta = buildString {
                                if (t.estimateMinutes > 0) append("${t.estimateMinutes} min") else append("sem estimativa")
                                if (t.deadline.isNotBlank()) append(" · prazo ${displayDate(t.deadline)}")
                                if (blockers.isNotEmpty()) append(" · aguarda: ${blockers.joinToString { it.title }}")
                            }
                            Text(meta, color = Muted, fontSize = 11.sp)
                            // Botões acessíveis de movimentação (nunca depende apenas de drag-and-drop!)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                if (colKey != "todo" && colKey != "blocked") {
                                    TextButton({ vm.run { vm.repo.setTaskKanbanColumn(t.id, "todo") } }) {
                                        Text("→ A fazer", color = Gold, fontSize = 12.sp)
                                    }
                                }
                                if (colKey != "doing") {
                                    TextButton({
                                        if (blockers.isNotEmpty()) {
                                            overrideTarget = t
                                            overrideReason = ""
                                        } else {
                                            vm.run { vm.repo.setTaskKanbanColumn(t.id, "doing") }
                                        }
                                    }) {
                                        Text(if (blockers.isNotEmpty()) "Liberar & Iniciar" else "→ Em andamento", color = Blue, fontSize = 12.sp)
                                    }
                                }
                                if (colKey != "done") {
                                    TextButton({ vm.run { vm.repo.setTaskKanbanColumn(t.id, "done") } }) {
                                        Text("→ Concluir", color = Green, fontSize = 12.sp)
                                    }
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
                SagaButton("Confirmar liberação", {
                    val target = t
                    val reason = overrideReason
                    overrideTarget = null
                    vm.run { vm.repo.setTaskKanbanColumn(target.id, "doing", reason) }
                }, enabled = overrideReason.isNotBlank())
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
            Eyebrow("Caminho Crítico (CPM)", if (cpm.deadlineRisk) MaterialTheme.colorScheme.error else Gold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CouncilKpiTile("Esforço crítico", "${cpm.totalCriticalMinutes} min", "Projeção esforço: ${cpm.effortProjectedDate?.let { displayDate(it) } ?: "—"}", Gold, Modifier.weight(1f))
                CouncilKpiTile(
                    "Data real",
                    cpm.availabilityProjectedDate?.let { displayDate(it) } ?: "Indefinida",
                    if (cpm.deadlineRisk) "Risco de atraso no prazo" else "Pela disponibilidade",
                    if (cpm.deadlineRisk) MaterialTheme.colorScheme.error else Green,
                    Modifier.weight(1f)
                )
            }
            if (cpm.criticalSequence.isNotEmpty()) {
                Text("Sequência crítica:", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                cpm.criticalSequence.forEachIndexed { idx, n ->
                    Text("${idx + 1}. ${n.task.title} (${n.durationMinutes} min · folga 0)", color = Ink, fontSize = 13.sp)
                }
            }
            cpm.premises.forEach { p -> Text("• $p", color = Muted, fontSize = 11.sp) }
        }

        // Editor de dependências finish-to-start
        CouncilMaterialSurface(CouncilMaterial.DarkIron) {
            Eyebrow("Dependências (término → início)")
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
                    secondary = true
                )
            }

            if (pathDeps.isEmpty()) {
                Text("Nenhuma dependência cadastrada neste Caminho.", color = Muted, fontSize = 12.sp)
            } else {
                pathDeps.forEach { dep ->
                    val tName = tasksById[dep.taskId]?.title ?: dep.taskId
                    val pName = tasksById[dep.dependsOnTaskId]?.title ?: dep.dependsOnTaskId
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
        Eyebrow("Marcos do Caminho (${milestones.count { it.completed }}/${milestones.size})")
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
            secondary = true
        )

        if (milestones.isEmpty()) {
            Text("Nenhum marco registrado ainda.", color = Muted, fontSize = 12.sp)
        } else {
            milestones.forEach { m ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StrategyControlGlyph(col = 2, row = 0, modifier = Modifier.size(20.dp))
                    Column(Modifier.weight(1f)) {
                        Text(m.title, color = if (m.completed) Green else Ink, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("Alvo: ${displayDate(m.targetDate)}${if (m.completed) " · Concluído" else ""}", color = Muted, fontSize = 11.sp)
                    }
                    TextButton({ vm.run { vm.repo.togglePathMilestone(m.id) } }) {
                        Text(if (m.completed) "Reabrir" else "Concluir", color = Gold, fontSize = 12.sp)
                    }
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
            SagaButton("Criar Caminho", {
                vm.run {
                    val newId = vm.repo.createPathFromTemplate(selected.id, customName.trim())
                    onDismiss()
                    onCreated(newId)
                }
            })
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
