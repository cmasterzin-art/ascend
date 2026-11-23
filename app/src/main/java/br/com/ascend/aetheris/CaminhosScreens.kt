package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import java.time.LocalDate

@Composable
private fun pathStateColor(state: Int): Color = when (state) {
    1 -> Blue
    2 -> Gold
    3 -> Green
    4, 5 -> Muted
    else -> Gold
}

@Composable
private fun priorityColor(priority: Int): Color = when (priority) {
    3 -> MaterialTheme.colorScheme.error
    2 -> Gold
    1 -> Blue
    else -> Muted
}

fun pathDeadlineSummary(path: PersonalPath, refDate: String = today()): String {
    if (path.state == 3 && path.completedDate.isNotBlank()) return "Concluído em ${displayDate(path.completedDate)}"
    val days = pathDeadlineDays(path, refDate) ?: return "Sem prazo"
    return when {
        days < 0 -> "Atrasado há ${-days} ${if (-days == 1L) "dia" else "dias"}"
        days == 0L -> "Prazo hoje"
        else -> "Faltam $days ${if (days == 1L) "dia" else "dias"} · ${displayDate(path.deadline)}"
    }
}

@Composable
private fun openTaskConfirmation(onConfirm: (Set<String>) -> Unit): (Set<String>) -> Unit {
    var pending by remember { mutableStateOf<Set<String>?>(null) }
    pending?.let { ids ->
        AetherDialog(
            onDismissRequest = { pending = null },
            title = "Concluir com tarefas abertas?",
            confirmButton = { SagaButton("Concluir caminho", { pending = null; onConfirm(ids) }) },
            dismissButton = { TextButton({ pending = null }) { Text("Voltar às tarefas") } }
        ) {
            Text("Tarefas ainda abertas: ${ids.size}. Elas serão preservadas e sairão das pendências ativas. Você pode concluí-las antes ou reabrir o caminho depois.")
        }
    }
    return { ids -> if (ids.isEmpty()) onConfirm(ids) else pending = ids }
}

private fun openTaskIds(s: GameState, pathId: String): Set<String> =
    s.pathTasks.filter { it.pathId == pathId && !it.archived && !it.completed }.mapTo(mutableSetOf()) { it.id }

@Composable
private fun pathStateAction(s: GameState, vm: GameViewModel): (String, Int) -> Unit {
    var pathId by remember { mutableStateOf("") }
    val confirm = openTaskConfirmation { ids -> vm.run { vm.repo.setPathState(pathId, 3, ids) } }
    return { id, state ->
        if (state == 3 && s.paths.find { it.id == id }?.state != 3) {
            pathId = id
            confirm(openTaskIds(s, id))
        } else vm.run { vm.repo.setPathState(id, state) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PathsScreen(
    s: GameState,
    vm: GameViewModel,
    onOpenPath: (String) -> Unit,
    onNewPath: () -> Unit,
    onEditPath: (PersonalPath) -> Unit
) {
    val changePathState = pathStateAction(s, vm)
    val day = currentDay()
    var search by rememberSaveable { mutableStateOf("") }
    var stateFilter by rememberSaveable { mutableIntStateOf(0) } // 0=Ativos, 1=Todos, 2..7=pathStates
    var priorityFilter by rememberSaveable { mutableIntStateOf(0) } // 0=Todas, 1..4=priorityLabels
    var categoryFilter by rememberSaveable { mutableStateOf("Todas") }
    var deleteCandidate by remember { mutableStateOf<PersonalPath?>(null) }
    var showTemplates by remember { mutableStateOf(false) }

    val categories = remember(s.paths) {
        listOf("Todas") + s.paths.map { it.category.ifBlank { "Pessoal" } }.distinct().sorted()
    }
    if (categoryFilter !in categories) categoryFilter = "Todas"

    val filtered = remember(s.paths, search, stateFilter, priorityFilter, categoryFilter) {
        s.paths.filter { p ->
            val matchSearch = search.isBlank() ||
                p.name.contains(search, true) ||
                p.category.contains(search, true) ||
                p.description.contains(search, true)
            val matchState = when (stateFilter) {
                0 -> p.state in 0..2
                1 -> true
                else -> p.state == stateFilter - 2
            }
            val matchPriority = priorityFilter == 0 || p.priority == priorityFilter - 1
            val matchCategory = categoryFilter == "Todas" || p.category.ifBlank { "Pessoal" } == categoryFilter
            matchSearch && matchState && matchPriority && matchCategory
        }.sortedWith(compareBy<PersonalPath> { it.state !in 0..2 }.thenBy { it.position }.thenBy { it.name.lowercase() })
    }

    val activeCount = s.paths.count { it.state in 0..2 }
    val openTasksCount = s.pathTasks.count { isTaskActivePending(s, it) && !it.completed }
    val totalFocusMinutes = s.paths.sumOf { pathFocusMinutes(s, it.id) }

    val nb = LocalNorthborn.current
    LazyColumn(
        Modifier.fillMaxSize().chapterBackdrop(NorthbornChapter.Strategy).testTag("paths_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ChapterHeading("Caminhos", "Projetos pessoais com etapas, tarefas, Gantt, Kanban, caminho crítico e marcos.", NorthbornChapter.Strategy, Engraving.Cairns)
        }
        item {
            NorthbornSurface(role = NorthbornSurfaceRole.Informative, accent = nb.accentBronze) {
                MetricPair(
                    activeCount.toString(),
                    if (activeCount == 1) "caminho ativo" else "caminhos ativos",
                    openTasksCount.toString(),
                    if (openTasksCount == 1) "tarefa em aberto" else "tarefas em aberto"
                )
                Text(
                    "${s.paths.count { it.state == 3 }} concluídos · $totalFocusMinutes min de foco real registrado",
                    color = nb.inkSecondary,
                    fontSize = 12.sp
                )
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NorthbornButton("+ Novo caminho", onNewPath, role = NorthbornButtonRole.Primary, modifier = Modifier.weight(1f))
                NorthbornButton("Modelos (6)", { showTemplates = true }, role = NorthbornButtonRole.Secondary, modifier = Modifier.weight(1f))
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FormField(search, { search = it }, "Buscar caminho por nome ou categoria")
                AetherisTabs(
                    listOf("Ativos", "Todos") + pathStates,
                    stateFilter,
                    { stateFilter = it }
                )
                ChipRow(
                    listOf("Todas prioridades") + priorityLabels,
                    priorityFilter,
                    { priorityFilter = it }
                )
                if (categories.size > 2) {
                    ChipRow(
                        categories,
                        categories.indexOf(categoryFilter).coerceAtLeast(0),
                        { categoryFilter = categories[it] }
                    )
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                Empty("Nenhum caminho encontrado com esses filtros. Crie um novo caminho ou ajuste a seleção.")
            }
        }
        items(filtered, key = { it.id }) { p ->
            val prog = pathProgress(s, p.id)
            val stagesCount = s.pathStages.count { it.pathId == p.id }
            val focusMin = pathFocusMinutes(s, p.id)
            val accent = habitColor(p.color.coerceIn(0, 4))
            val sColor = pathStateColor(p.state)
            val kind = when (p.state) {
                3 -> SurfaceKind.Ceremonial
                1 -> SurfaceKind.Heroic
                else -> SurfaceKind.Operational
            }
            AetherSurface(kind, Modifier.testTag("path:${p.id}")) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (p.attribute in 0..4) {
                        AttributeEmblem(p.attribute, Modifier.size(30.dp))
                    } else {
                        Box(
                            Modifier.size(30.dp)
                                .clip(CutCornerShape(4.dp))
                                .background(accent.copy(alpha = .16f))
                                .border(1.dp, accent.copy(alpha = .45f), CutCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(glyphs[p.icon.coerceIn(glyphs.indices)], color = accent, fontSize = 15.sp)
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${p.category} · ${priorityLabels[p.priority.coerceIn(priorityLabels.indices)]}",
                            color = Muted,
                            fontSize = 11.sp
                        )
                    }
                    StatusSeal(pathStates[p.state.coerceIn(pathStates.indices)], sColor)
                }
                Column(
                    Modifier.fillMaxWidth().clickable { onOpenPath(p.id) },
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(p.name, style = MaterialTheme.typography.titleLarge, color = Ink)
                    if (p.description.isNotBlank()) {
                        Text(p.description, color = Muted, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FlowRow(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            if (prog.total == 0) "Sem tarefas cadastradas" else "${prog.done}/${prog.total} tarefas (${prog.percent}%)",
                            color = if (prog.total > 0 && prog.done == prog.total) Green else accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            pathDeadlineSummary(p, day),
                            color = if ((pathDeadlineDays(p, day) ?: 1L) < 0 && p.state in 0..2) MaterialTheme.colorScheme.error else Muted,
                            fontSize = 11.sp
                        )
                    }
                    Meter(prog.fraction, if (p.state == 3) Green else accent, slim = true)
                }
                Text(
                    "$stagesCount ${if (stagesCount == 1) "etapa" else "etapas"} · $focusMin min em foco",
                    color = Muted,
                    fontSize = 12.sp
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SagaButton("Abrir", { onOpenPath(p.id) }, Modifier.weight(1f))
                    SagaButton("Editar", { onEditPath(p) }, Modifier.weight(1f), secondary = true)
                }
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton({ vm.run { vm.repo.duplicatePath(p.id, true) } }) {
                        Text("Duplicar", fontSize = 12.sp)
                    }
                    if (p.state != 3) {
                        TextButton({ changePathState(p.id, 3) }) {
                            Text("Concluir", color = Green, fontSize = 12.sp)
                        }
                    }
                    TextButton({ deleteCandidate = p }) {
                        Text("Excluir", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    deleteCandidate?.let { p ->
        AetherDialog(
            onDismissRequest = { deleteCandidate = null },
            title = "Excluir ${p.name}?",
            confirmButton = {
                SagaButton(
                    "Excluir",
                    {
                        vm.run { vm.repo.deletePath(p.id) }
                        deleteCandidate = null
                    },
                    destructive = true
                )
            },
            dismissButton = { TextButton({ deleteCandidate = null }) { Text("Cancelar") } }
        ) {
            Text("Etapas, tarefas, subtarefas e vínculos deste caminho serão removidos. Sessões de foco históricas preservam o rótulo do trabalho realizado.")
        }
    }
    if (showTemplates) {
        ProjectTemplatesDialog(
            s = s,
            vm = vm,
            onDismiss = { showTemplates = false },
            onCreated = { newId -> onOpenPath(newId) }
        )
    }
}

@Composable
fun PathEditorDialog(
    initial: PersonalPath,
    onDismiss: () -> Unit,
    openTasks: Set<String>,
    onSave: (PersonalPath, Set<String>) -> Unit
) {
    var name by rememberSaveable(initial.id) { mutableStateOf(initial.name) }
    var description by rememberSaveable(initial.id) { mutableStateOf(initial.description) }
    var category by rememberSaveable(initial.id) { mutableStateOf(initial.category) }
    var startDate by rememberSaveable(initial.id) { mutableStateOf(initial.startDate) }
    var deadline by rememberSaveable(initial.id) { mutableStateOf(initial.deadline) }
    var priority by rememberSaveable(initial.id) { mutableIntStateOf(initial.priority) }
    var state by rememberSaveable(initial.id) { mutableIntStateOf(initial.state) }
    var attribute by rememberSaveable(initial.id) { mutableIntStateOf(initial.attribute) }
    var icon by rememberSaveable(initial.id) { mutableIntStateOf(initial.icon) }
    var color by rememberSaveable(initial.id) { mutableIntStateOf(initial.color) }
    var criteria by rememberSaveable(initial.id) { mutableStateOf(initial.criteria) }
    var notes by rememberSaveable(initial.id) { mutableStateOf(initial.notes) }
    var discard by remember { mutableStateOf(false) }

    fun draft() = initial.copy(
        name = name.trim(),
        description = description,
        category = category.trim().ifBlank { "Pessoal" },
        startDate = startDate.trim(),
        deadline = deadline.trim(),
        priority = priority,
        state = state,
        attribute = attribute,
        icon = icon,
        color = color,
        criteria = criteria,
        notes = notes
    )

    val confirmCompletion = openTaskConfirmation { ids -> onSave(draft(), ids) }
    val dirty = draft() != initial
    val close = { if (dirty) discard = true else onDismiss() }

    FullForm(
        if (initial.name.isBlank()) "Novo caminho" else "Editar caminho",
        close,
        { confirmCompletion(if (state == 3 && initial.state != 3) openTasks else emptySet()) }
    ) {
        Panel(accent = habitColor(color.coerceIn(0, 4))) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(glyphs[icon.coerceIn(glyphs.indices)], color = habitColor(color.coerceIn(0, 4)), fontSize = 30.sp)
                Column {
                    Text(name.ifBlank { "Novo projeto pessoal" }, style = MaterialTheme.typography.titleLarge)
                    Text("${pathStates[state]} · ${priorityLabels[priority]}", color = Muted, fontSize = 12.sp)
                }
            }
        }
        FormField(name, { name = it }, "Nome do caminho")
        FormField(description, { description = it }, "Descrição ou propósito", multi = true)
        FormField(category, { category = it }, "Categoria")
        Eyebrow("Estado")
        ChipRow(pathStates, state, { state = it })
        Eyebrow("Prioridade")
        ChipRow(priorityLabels, priority, { priority = it })
        Eyebrow("Atributo simbólico opcional")
        ChipRow(listOf("Nenhum") + attributes, attribute + 1, { attribute = it - 1 })
        FormField(startDate, { startDate = it }, "Início · AAAA-MM-DD")
        FormField(deadline, { deadline = it }, "Prazo opcional · AAAA-MM-DD")
        DeadlinePicker(deadline) { deadline = it }
        Eyebrow("Emblema e cor")
        ChipRow(glyphs, icon, { icon = it })
        ChipRow(listOf("Ouro", "Azul", "Violeta", "Verde", "Coral"), color, { color = it })
        FormField(criteria, { criteria = it }, "Critérios de conclusão (opcional)", multi = true)
        FormField(notes, { notes = it }, "Notas gerais do caminho", multi = true)
        Panel(accent = Blue) {
            Text(
                "Caminhos acompanham entregas reais pelas tarefas vinculadas. Criar, duplicar ou atrasar não inventa progresso nem concede XP extra.",
                color = Muted,
                fontSize = 12.sp
            )
        }
    }

    if (discard) {
        AetherDialog(
            onDismissRequest = { discard = false },
            title = "Descartar alterações?",
            confirmButton = { SagaButton("Descartar", onDismiss, destructive = true) },
            dismissButton = { TextButton({ discard = false }) { Text("Continuar editando") } }
        ) {
            Text("As mudanças deste formulário ainda não foram salvas.")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PathDetailScreen(
    pathId: String,
    s: GameState,
    vm: GameViewModel,
    onBack: () -> Unit,
    onEditPath: (PersonalPath) -> Unit,
    onStartFocus: (pathId: String, taskId: String, label: String) -> Unit,
    onOpenNotebook: (String) -> Unit = {},
    onOpenPage: (String) -> Unit = {}
) {
    val changePathState = pathStateAction(s, vm)
    val day = currentDay()
    val p = s.paths.find { it.id == pathId }
    if (p == null) {
        Page(NorthbornChapter.Strategy) {
            Empty("Caminho não encontrado ou removido.")
            SagaButton("Voltar", onBack)
        }
        return
    }

    val stages = remember(s.pathStages, pathId) {
        s.pathStages.filter { it.pathId == pathId }.sortedBy { it.position }
    }
    val allTasks = remember(s.pathTasks, pathId) {
        s.pathTasks.filter { it.pathId == pathId }.sortedBy { it.position }
    }
    val subtasksByTask = remember(s.pathSubtasks) {
        s.pathSubtasks.groupBy { it.taskId }.mapValues { (_, v) -> v.sortedBy { it.position } }
    }
    val links = remember(s.pathHabitLinks, pathId) {
        s.pathHabitLinks.filter { it.pathId == pathId }
    }
    val linkedNotebooks = remember(s.notebooks, pathId) {
        s.notebooks.filter { !it.archived && it.pathId == pathId }
    }
    val linkedPages = remember(s.knowledgePages, pathId) {
        s.knowledgePages.filter { !it.archived && it.pathId == pathId }
    }
    val prog = pathProgress(s, pathId)
    val focusMinutes = pathFocusMinutes(s, pathId)
    val taskIdsSet = remember(allTasks) { allTasks.mapTo(HashSet()) { it.id } }
    val relatedSessions = remember(s.focusSessions, pathId, taskIdsSet) {
        s.focusSessions.filter { it.pathId == pathId || (it.taskId.isNotBlank() && it.taskId in taskIdsSet) }
            .sortedByDescending { it.endedAt }
    }

    var taskFilter by rememberSaveable(pathId) { mutableIntStateOf(0) } // 0=Abertas, 1=Todas, 2=Concluídas, 3=Atrasadas, 4=Alta prioridade, 5=Arquivadas
    val filteredTasks = remember(allTasks, taskFilter, day) {
        allTasks.filter { t ->
            when (taskFilter) {
                0 -> !t.archived && !t.completed
                1 -> !t.archived
                2 -> !t.archived && t.completed
                3 -> isTaskOverdue(t, day)
                4 -> !t.archived && !t.completed && t.priority >= 2
                else -> t.archived
            }
        }
    }

    var stageEditor by remember { mutableStateOf<PathStage?>(null) }
    var taskEditor by remember { mutableStateOf<PathTask?>(null) }
    var subtaskParent by remember { mutableStateOf<PathTask?>(null) }
    var moveCandidate by remember { mutableStateOf<PathTask?>(null) }
    var deleteTaskCandidate by remember { mutableStateOf<PathTask?>(null) }
    var deleteStageCandidate by remember { mutableStateOf<PathStage?>(null) }
    var deletePathDialog by remember { mutableStateOf(false) }
    var habitLinkDialog by remember { mutableStateOf(false) }
    var stageDialogError by remember { mutableStateOf<String?>(null) }

    val accent = habitColor(p.color.coerceIn(0, 4))

    LazyColumn(
        Modifier.fillMaxSize().chapterBackdrop(NorthbornChapter.Strategy).testTag("path_detail_list"),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AetherSurface(if (p.state == 3) SurfaceKind.Ceremonial else SurfaceKind.Heroic) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (p.attribute in 0..4) AttributeEmblem(p.attribute, Modifier.size(34.dp))
                    Column(Modifier.weight(1f)) {
                        Eyebrow("${p.category} · ${priorityLabels[p.priority.coerceIn(priorityLabels.indices)]}", accent)
                        Text(p.name, style = MaterialTheme.typography.headlineMedium)
                    }
                    StatusSeal(pathStates[p.state.coerceIn(pathStates.indices)], pathStateColor(p.state))
                }
                if (p.description.isNotBlank()) Text(p.description, color = Muted, fontSize = 14.sp, lineHeight = 22.sp)
                MetricPair(
                    if (prog.total == 0) "0/0" else "${prog.done}/${prog.total}",
                    "tarefas concluídas (${prog.percent}%)",
                    "$focusMinutes min",
                    "foco efetivo registrado"
                )
                Meter(prog.fraction, if (p.state == 3) Green else accent)
                Text(
                    "Início: ${displayDate(p.startDate)} · ${pathDeadlineSummary(p, day)}",
                    color = if ((pathDeadlineDays(p, day) ?: 1L) < 0 && p.state in 0..2) MaterialTheme.colorScheme.error else Muted,
                    fontSize = 12.sp
                )
                if (p.criteria.isNotBlank()) {
                    Text("Critério: ${p.criteria}", color = Ink, fontSize = 13.sp)
                }
                Eyebrow("Alterar estado")
                ChipRow(pathStates, p.state, { idx -> changePathState(p.id, idx) })
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Action("Iniciar foco", { onStartFocus(p.id, "", p.name) }, Modifier.weight(1f), enabled = s.focus == null)
                    SagaButton("Editar", { onEditPath(p) }, Modifier.weight(1f), secondary = true)
                }
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton({ vm.run { vm.repo.duplicatePath(p.id, true) } }) { Text("Duplicar caminho") }
                    TextButton({ deletePathDialog = true }) { Text("Excluir caminho", color = MaterialTheme.colorScheme.error) }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { SectionHeader("Etapas", "Divida o caminho em marcos reais.") }
                SagaButton(
                    "+ Etapa",
                    { stageEditor = PathStage(pathId = p.id, name = "", position = stages.size) },
                    secondary = true
                )
            }
        }
        if (stages.isEmpty()) {
            item {
                Text("Nenhuma etapa definida ainda. Você pode criar etapas ou adicionar tarefas diretamente ao caminho.", color = Muted, fontSize = 13.sp)
            }
        }
        items(stages, key = { "stage:${it.id}" }) { st ->
            val sp = stageProgress(s, st.id)
            AetherSurface(SurfaceKind.Operational, Modifier.testTag("stage:${st.id}")) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(st.name, style = MaterialTheme.typography.titleMedium)
                        if (st.targetNote.isNotBlank()) Text(st.targetNote, color = Muted, fontSize = 12.sp)
                    }
                    Text("${sp.done}/${sp.total}", color = if (sp.total > 0 && sp.done == sp.total) Green else Blue, fontSize = 13.sp)
                }
                Meter(sp.fraction, if (sp.total > 0 && sp.done == sp.total) Green else Blue, slim = true)
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton({ taskEditor = PathTask(pathId = p.id, stageId = st.id, title = "", position = allTasks.size) }) {
                        Text("+ Tarefa nesta etapa")
                    }
                    TextButton({ stageEditor = st }) { Text("Editar") }
                    TextButton({ deleteStageCandidate = st }) { Text("Remover", color = MaterialTheme.colorScheme.error) }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) { SectionHeader("Tarefas", "Progresso derivado das entregas concluídas.") }
                    SagaButton(
                        "+ Tarefa",
                        { taskEditor = PathTask(pathId = p.id, title = "", position = allTasks.size) }
                    )
                }
                AetherisTabs(
                    listOf("Abertas", "Todas", "Concluídas", "Atrasadas", "Alta prioridade", "Arquivadas"),
                    taskFilter,
                    { taskFilter = it }
                )
            }
        }
        if (filteredTasks.isEmpty()) {
            item { Empty("Nenhuma tarefa neste filtro.") }
        }
        items(filteredTasks, key = { "task:${it.id}" }) { t ->
            val stageName = stages.find { it.id == t.stageId }?.name
            val subs = subtasksByTask[t.id].orEmpty()
            val overdue = isTaskOverdue(t, day)
            val dueSoon = isTaskDueSoon(t, day)
            val tFocus = taskFocusMinutes(s, t.id)
            val borderColor = when {
                t.completed -> Green.copy(alpha = .5f)
                overdue -> MaterialTheme.colorScheme.error.copy(alpha = .7f)
                dueSoon -> Gold.copy(alpha = .55f)
                else -> ColdIron.copy(alpha = .22f)
            }
            AetherSurface(
                SurfaceKind.Operational,
                Modifier.testTag("task:${t.id}").border(1.dp, borderColor, CutCornerShape(4.dp))
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Checkbox(
                        checked = t.completed,
                        onCheckedChange = { checked -> vm.run { vm.repo.toggleTask(t.id, checked) } },
                        modifier = Modifier.size(48.dp).semantics { contentDescription = "Alternar ${t.title}" },
                        colors = CheckboxDefaults.colors(checkedColor = Green, uncheckedColor = ColdIron)
                    )
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            t.title,
                            style = MaterialTheme.typography.titleMedium,
                            textDecoration = if (t.completed) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (t.completed) Muted else Ink
                        )
                        val meta = buildList {
                            add(priorityLabels[t.priority.coerceIn(priorityLabels.indices)])
                            stageName?.let { add(it) }
                            if (t.scheduledDate.isNotBlank()) add("Agenda: ${displayDate(t.scheduledDate)}${if (t.scheduledTime.isNotBlank()) " ${t.scheduledTime}" else ""}")
                            if (t.deadline.isNotBlank()) add("Prazo: ${displayDate(t.deadline)}") else add("Sem prazo")
                            if (tFocus > 0) add("$tFocus min foco")
                        }.joinToString(" · ")
                        Text(meta, color = if (overdue) MaterialTheme.colorScheme.error else Muted, fontSize = 11.sp)
                    }
                    StatusSeal(
                        when {
                            t.archived -> "Arquivada"
                            t.completed -> "◆ Concluída"
                            overdue -> "Atrasada"
                            dueSoon -> "Próxima"
                            else -> "Aberta"
                        },
                        when {
                            t.completed -> Green
                            overdue -> MaterialTheme.colorScheme.error
                            dueSoon -> Gold
                            else -> Blue
                        }
                    )
                }
                if (t.description.isNotBlank()) {
                    Text(t.description, color = Muted, fontSize = 13.sp)
                }
                if (subs.isNotEmpty()) {
                    Hairline()
                    Text(
                        "Subtarefas (${subs.count { it.done }}/${subs.size})",
                        color = Gold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    subs.forEachIndexed { idx, sub ->
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 44.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Checkbox(
                                checked = sub.done,
                                onCheckedChange = { vm.run { vm.repo.toggleSubtask(sub.id) } },
                                modifier = Modifier.semantics { contentDescription = "Subtarefa ${sub.title}" }
                            )
                            Text(
                                sub.title,
                                Modifier.weight(1f),
                                color = if (sub.done) Muted else Ink,
                                fontSize = 13.sp,
                                textDecoration = if (sub.done) TextDecoration.LineThrough else TextDecoration.None
                            )
                            if (idx > 0) {
                                IconButton({ vm.run { vm.repo.reorderSubtask(sub.id, -1) } }, Modifier.size(44.dp)) {
                                    Icon(Icons.Default.KeyboardArrowUp, "Subir subtarefa", tint = Muted)
                                }
                            }
                            if (idx < subs.lastIndex) {
                                IconButton({ vm.run { vm.repo.reorderSubtask(sub.id, 1) } }, Modifier.size(44.dp)) {
                                    Icon(Icons.Default.KeyboardArrowDown, "Descer subtarefa", tint = Muted)
                                }
                            }
                            IconButton({ vm.run { vm.repo.deleteSubtask(sub.id) } }, Modifier.size(44.dp)) {
                                Icon(Icons.Default.Close, "Remover subtarefa", tint = Muted)
                            }
                        }
                    }
                }
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    TextButton({ subtaskParent = t }) { Text("+ Subtarefa", fontSize = 12.sp) }
                    if (!t.completed && !t.archived) {
                        TextButton({ onStartFocus(p.id, t.id, t.title) }, enabled = s.focus == null) {
                            Text("Foco", color = Violet, fontSize = 12.sp)
                        }
                    }
                    TextButton({ taskEditor = t }) { Text("Editar", fontSize = 12.sp) }
                    TextButton({ moveCandidate = t }) { Text("Mover", fontSize = 12.sp) }
                    TextButton({ vm.run { vm.repo.duplicateTask(t.id) } }) { Text("Duplicar", fontSize = 12.sp) }
                    TextButton({ vm.run { vm.repo.saveTask(t.copy(archived = !t.archived)) } }) {
                        Text(if (t.archived) "Desarquivar" else "Arquivar", fontSize = 12.sp)
                    }
                    TextButton({ deleteTaskCandidate = t }) {
                        Text("Excluir", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }
        }

        item(key = "path_30_advanced") {
            Path30AdvancedSection(path = p, s = s, vm = vm)
        }

        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    SectionHeader("Hábitos vinculados", "Consistência real sem duplicar XP ou moedas.")
                }
                SagaButton("Vincular", { habitLinkDialog = true }, secondary = true)
            }
        }
        if (links.isEmpty()) {
            item { Text("Nenhum hábito vinculado a este caminho.", color = Muted, fontSize = 13.sp) }
        } else {
            items(links, key = { "link:${it.id}" }) { link ->
                val h = s.habits.find { it.id == link.habitId }
                if (h != null) {
                    val st = streak(h, s.records, restDates = s.restDates, plans = s.plans)
                    val doneToday = s.record(h.id, day)?.complete == true
                    AetherSurface {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AttributeEmblem(h.attribute, Modifier.size(28.dp))
                            Column(Modifier.weight(1f)) {
                                Text(h.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Sequência: $st ${if (st == 1) "dia" else "dias"} · ${if (doneToday) "Concluído hoje" else "Pendente hoje"}",
                                    color = if (doneToday) Green else Muted,
                                    fontSize = 12.sp
                                )
                            }
                            TextButton({ vm.run { vm.repo.linkHabitToPath(p.id, h.id, false) } }) {
                                Text("Desvincular", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        if (linkedNotebooks.isNotEmpty() || linkedPages.isNotEmpty()) {
            item {
                AetherSurface(SurfaceKind.Artifact) {
                    Eyebrow("Biblioteca de Mímir vinculada", Gold)
                    linkedNotebooks.forEach { nb ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onOpenNotebook(nb.id) }.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MimirCodexArt(nb.cover, Modifier.size(36.dp))
                            Column(Modifier.weight(1f)) {
                                Text(nb.name, color = Ink, fontSize = 13.sp)
                                Text("Caderno · ${nb.category}", color = Muted, fontSize = 11.sp)
                            }
                            Text("Abrir", color = Gold, fontSize = 12.sp)
                        }
                    }
                    linkedPages.forEach { pg ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onOpenPage(pg.id) }.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("▤ ${pg.title}", color = Blue, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("Ler", color = Gold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (relatedSessions.isNotEmpty() || p.notes.isNotBlank()) {
            item {
                SectionHeader("Foco recente e notas")
                if (p.notes.isNotBlank()) {
                    AetherSurface(SurfaceKind.Parchment) {
                        Eyebrow("Notas do caminho", Sepia)
                        Text(p.notes, color = Sepia, fontSize = 14.sp)
                    }
                }
            }
            items(relatedSessions.take(8), key = { "fs:${it.id}" }) { fs ->
                AetherSurface {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(fs.targetLabel.ifBlank { p.name }, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text("${fs.elapsedSeconds / 60} min", color = Violet, fontSize = 13.sp)
                    }
                    Text("${displayDate(fs.date)} · ${if (fs.status == "completed") "Concluída" else if (fs.status == "cancelled") "Cancelada" else "Encerrada"} · sem XP extra", color = Muted, fontSize = 12.sp)
                }
            }
        }
    }

    stageEditor?.let { st ->
        var stName by remember(st.id) { mutableStateOf(st.name) }
        var stNote by remember(st.id) { mutableStateOf(st.targetNote) }
        stageDialogError = null
        AetherDialog(
            onDismissRequest = { stageEditor = null },
            title = if (st.name.isBlank()) "Nova etapa" else "Editar etapa",
            confirmButton = {
                SagaButton("Salvar", {
                    if (stName.isBlank()) {
                        stageDialogError = "Informe um nome para a etapa."
                    } else {
                        vm.run { vm.repo.saveStage(st.copy(name = stName.trim(), targetNote = stNote)) }
                        stageEditor = null
                    }
                })
            },
            dismissButton = { TextButton({ stageEditor = null }) { Text("Cancelar") } }
        ) {
            stageDialogError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            FormField(stName, { stName = it }, "Nome da etapa")
            FormField(stNote, { stNote = it }, "Meta ou observação opcional", multi = true)
        }
    }

    taskEditor?.let { t ->
        TaskEditorDialog(
            initial = t,
            paths = s.paths,
            stages = s.pathStages,
            onDismiss = { taskEditor = null },
            onSave = { updated ->
                vm.run { vm.repo.saveTask(updated); taskEditor = null }
            }
        )
    }

    subtaskParent?.let { parent ->
        var subTitle by remember(parent.id) { mutableStateOf("") }
        AetherDialog(
            onDismissRequest = { subtaskParent = null },
            title = "Nova subtarefa · ${parent.title}",
            confirmButton = {
                SagaButton("Adicionar", {
                    val pos = (subtasksByTask[parent.id]?.maxOfOrNull { it.position } ?: -1) + 1
                    vm.run { vm.repo.saveSubtask(PathSubtask(taskId = parent.id, title = subTitle, position = pos)) }
                    subtaskParent = null
                }, enabled = subTitle.isNotBlank())
            },
            dismissButton = { TextButton({ subtaskParent = null }) { Text("Cancelar") } }
        ) {
            FormField(subTitle, { subTitle = it }, "Título da subtarefa")
        }
    }

    moveCandidate?.let { t ->
        var targetPathId by remember(t.id) { mutableStateOf(t.pathId) }
        var targetStageId by remember(t.id) { mutableStateOf(t.stageId) }
        val activePaths = s.paths.filter { it.state !in listOf(4, 5) || it.id == t.pathId }
        val targetStages = s.pathStages.filter { it.pathId == targetPathId }.sortedBy { it.position }
        if (targetStageId.isNotBlank() && targetStages.none { it.id == targetStageId }) targetStageId = ""
        AetherDialog(
            onDismissRequest = { moveCandidate = null },
            title = "Mover ${t.title}",
            confirmButton = {
                SagaButton("Mover", {
                    vm.run { vm.repo.moveTask(t.id, targetPathId, targetStageId) }
                    moveCandidate = null
                })
            },
            dismissButton = { TextButton({ moveCandidate = null }) { Text("Cancelar") } }
        ) {
            Eyebrow("Caminho de destino")
            val pathLabels = listOf("Sem caminho (avulsa)") + activePaths.map { it.name }
            val pathIds = listOf("") + activePaths.map { it.id }
            ChipRow(pathLabels, pathIds.indexOf(targetPathId).coerceAtLeast(0), {
                targetPathId = pathIds[it]
                targetStageId = ""
            })
            if (targetStages.isNotEmpty()) {
                Eyebrow("Etapa de destino")
                val stageLabels = listOf("Sem etapa") + targetStages.map { it.name }
                val stageIds = listOf("") + targetStages.map { it.id }
                ChipRow(stageLabels, stageIds.indexOf(targetStageId).coerceAtLeast(0), {
                    targetStageId = stageIds[it]
                })
            }
        }
    }

    if (habitLinkDialog) {
        val linkedIds = links.mapTo(HashSet()) { it.habitId }
        AetherDialog(
            onDismissRequest = { habitLinkDialog = false },
            title = "Hábitos vinculados",
            confirmButton = { SagaButton("Concluir", { habitLinkDialog = false }) }
        ) {
            val available = s.habits.filter { !it.deleted }
            if (available.isEmpty()) {
                Text("Crie hábitos primeiro para associá-los a este caminho.", color = Muted)
            } else {
                Column(Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                    available.forEach { h ->
                        Toggle(h.name, h.id in linkedIds) { enabled ->
                            vm.run { vm.repo.linkHabitToPath(p.id, h.id, enabled) }
                        }
                    }
                }
            }
        }
    }

    deleteTaskCandidate?.let { t ->
        AetherDialog(
            onDismissRequest = { deleteTaskCandidate = null },
            title = "Excluir tarefa?",
            confirmButton = {
                SagaButton("Excluir", {
                    vm.run { vm.repo.deleteTask(t.id) }
                    deleteTaskCandidate = null
                }, destructive = true)
            },
            dismissButton = { TextButton({ deleteTaskCandidate = null }) { Text("Cancelar") } }
        ) {
            Text("“${t.title}” e suas subtarefas serão removidas.")
        }
    }

    deleteStageCandidate?.let { st ->
        AetherDialog(
            onDismissRequest = { deleteStageCandidate = null },
            title = "Remover etapa?",
            confirmButton = {
                SagaButton("Remover", {
                    vm.run { vm.repo.deleteStage(st.id) }
                    deleteStageCandidate = null
                }, destructive = true)
            },
            dismissButton = { TextButton({ deleteStageCandidate = null }) { Text("Cancelar") } }
        ) {
            Text("As tarefas desta etapa serão mantidas no caminho sem vínculo à etapa.")
        }
    }

    if (deletePathDialog) {
        AetherDialog(
            onDismissRequest = { deletePathDialog = false },
            title = "Excluir ${p.name}?",
            confirmButton = {
                SagaButton("Excluir", {
                    vm.run { vm.repo.deletePath(p.id) }
                    deletePathDialog = false
                    onBack()
                }, destructive = true)
            },
            dismissButton = { TextButton({ deletePathDialog = false }) { Text("Cancelar") } }
        ) {
            Text("Todas as etapas, tarefas e subtarefas serão removidas. Sessões de foco históricas preservam seus registros.")
        }
    }
}

@Composable
fun TaskEditorDialog(
    initial: PathTask,
    paths: List<PersonalPath>,
    stages: List<PathStage>,
    onDismiss: () -> Unit,
    onSave: (PathTask) -> Unit
) {
    var title by rememberSaveable(initial.id) { mutableStateOf(initial.title) }
    var desc by rememberSaveable(initial.id) { mutableStateOf(initial.description) }
    var pathId by rememberSaveable(initial.id) { mutableStateOf(initial.pathId) }
    var stageId by rememberSaveable(initial.id) { mutableStateOf(initial.stageId) }
    var priority by rememberSaveable(initial.id) { mutableIntStateOf(initial.priority) }
    var deadline by rememberSaveable(initial.id) { mutableStateOf(initial.deadline) }
    var scheduledDate by rememberSaveable(initial.id) { mutableStateOf(initial.scheduledDate) }
    var scheduledTime by rememberSaveable(initial.id) { mutableStateOf(initial.scheduledTime) }
    var estimate by rememberSaveable(initial.id) { mutableStateOf(if (initial.estimateMinutes > 0) initial.estimateMinutes.toString() else "") }
    var notes by rememberSaveable(initial.id) { mutableStateOf(initial.notes) }
    var discard by remember { mutableStateOf(false) }

    val availableStages = stages.filter { it.pathId == pathId }.sortedBy { it.position }
    if (stageId.isNotBlank() && availableStages.none { it.id == stageId }) stageId = ""

    fun draft() = initial.copy(
        title = title.trim(),
        description = desc,
        pathId = pathId,
        stageId = if (pathId.isBlank()) "" else stageId,
        priority = priority,
        deadline = deadline.trim(),
        scheduledDate = scheduledDate.trim(),
        scheduledTime = scheduledTime.trim(),
        estimateMinutes = estimate.trim().takeIf { it.isNotEmpty() }?.let { it.toIntOrNull() ?: -1 } ?: 0,
        notes = notes
    )

    val dirty = draft() != initial
    val close = { if (dirty) discard = true else onDismiss() }

    FullForm(
        if (initial.title.isBlank()) "Nova tarefa" else "Editar tarefa",
        close,
        { onSave(draft()) }
    ) {
        FormField(title, { title = it }, "Título da tarefa")
        FormField(desc, { desc = it }, "Descrição opcional", multi = true)
        Eyebrow("Prioridade")
        ChipRow(priorityLabels, priority, { priority = it })
        val openPaths = paths.filter { it.state !in listOf(4, 5) || it.id == pathId }
        if (openPaths.isNotEmpty()) {
            Eyebrow("Caminho vinculado")
            val pNames = listOf("Sem caminho (avulsa)") + openPaths.map { it.name }
            val pIds = listOf("") + openPaths.map { it.id }
            ChipRow(pNames, pIds.indexOf(pathId).coerceAtLeast(0), {
                pathId = pIds[it]
                stageId = ""
            })
        }
        if (availableStages.isNotEmpty()) {
            Eyebrow("Etapa")
            val sNames = listOf("Sem etapa") + availableStages.map { it.name }
            val sIds = listOf("") + availableStages.map { it.id }
            ChipRow(sNames, sIds.indexOf(stageId).coerceAtLeast(0), { stageId = sIds[it] })
        }
        FormField(scheduledDate, { scheduledDate = it }, "Data planejada · AAAA-MM-DD")
        FormField(scheduledTime, { scheduledTime = it }, "Horário planejado · HH:mm")
        FormField(deadline, { deadline = it }, "Prazo final · AAAA-MM-DD")
        DeadlinePicker(deadline) { deadline = it }
        FormField(estimate, { estimate = it }, "Estimativa em minutos (opcional)", number = true)
        FormField(notes, { notes = it }, "Notas da tarefa", multi = true)
    }

    if (discard) {
        AetherDialog(
            onDismissRequest = { discard = false },
            title = "Descartar alterações?",
            confirmButton = { SagaButton("Descartar", onDismiss, destructive = true) },
            dismissButton = { TextButton({ discard = false }) { Text("Continuar editando") } }
        ) {
            Text("As mudanças desta tarefa ainda não foram salvas.")
        }
    }
}
