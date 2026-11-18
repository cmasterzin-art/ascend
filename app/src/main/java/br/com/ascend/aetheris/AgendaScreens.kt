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
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlannerScreen(
    s: GameState,
    vm: GameViewModel,
    onRecord: (Habit, HabitRecord?) -> Unit,
    onOpenPath: (String) -> Unit = {},
    onInbox: () -> Unit = {},
    onPending: () -> Unit = {},
    onStartPathFocus: (pathId: String, taskId: String, label: String) -> Unit = { _, _, _ -> }
) {
    val current = currentDay()
    var mode by rememberSaveable { mutableIntStateOf(1) } // 0=Dia, 1=Semana (default preserves existing planner flow), 2=Mês
    var week by rememberSaveable { mutableStateOf(weekOf(today())) }
    var selected by rememberSaveable { mutableStateOf(today()) }
    var editingHabit by remember { mutableStateOf<Habit?>(null) }
    var blockEditor by remember { mutableStateOf<TimeBlock?>(null) }
    var eventEditor by remember { mutableStateOf<AgendaEvent?>(null) }
    var deleteBlock by remember { mutableStateOf<TimeBlock?>(null) }
    var deleteEvent by remember { mutableStateOf<AgendaEvent?>(null) }

    val start = LocalDate.parse(week)
    val days = (0..6).map { start.plusDays(it.toLong()).toString() }
    val daySummary = remember(s, selected) { agendaDay(s, selected) }
    val blockFocus = remember(s.focusSessions, s.timeBlocks) { timeBlockFocus(s) }
    val due = daySummary.habits
    val done = due.count { s.record(it.id, selected)?.complete == true }
    val dayShape = CutCornerShape(topStart = 6.dp, topEnd = 2.dp, bottomEnd = 6.dp, bottomStart = 2.dp)
    val inboxCount = s.inboxItems.count { !it.archived }
    val pendingCount = s.pathTasks.count { isTaskActivePending(s, it) && !it.completed }

    LazyColumn(
        Modifier.fillMaxSize().testTag("planner_list"),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Title("Planejador Semanal", "Agenda integrada de hábitos, tarefas, blocos e compromissos.")
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AetherisTabs(listOf("Dia", "Semana", "Mês"), mode, { mode = it })
                ShortcutStrip(
                    listOf(
                        Triple("Entrada ($inboxCount)", Glyph.Chronicles, onInbox),
                        Triple("Pendências ($pendingCount)", Glyph.Claims, onPending)
                    )
                )
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    if (maxWidth < 330.dp) {
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SagaButton(
                                "+ Bloco de tempo",
                                { blockEditor = TimeBlock(title = "", date = selected) },
                                Modifier.fillMaxWidth(),
                                secondary = true
                            )
                            SagaButton(
                                "+ Compromisso",
                                { eventEditor = AgendaEvent(title = "", date = selected) },
                                Modifier.fillMaxWidth(),
                                secondary = true
                            )
                        }
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SagaButton(
                                "+ Bloco de tempo",
                                { blockEditor = TimeBlock(title = "", date = selected) },
                                Modifier.weight(1f),
                                secondary = true
                            )
                            SagaButton(
                                "+ Compromisso",
                                { eventEditor = AgendaEvent(title = "", date = selected) },
                                Modifier.weight(1f),
                                secondary = true
                            )
                        }
                    }
                }
            }
        }

        when (mode) {
            0 -> {
                item {
                    val selDate = LocalDate.parse(selected)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SagaButton(
                            "‹",
                            {
                                val prev = selDate.minusDays(1).toString()
                                selected = prev
                                week = weekOf(prev)
                            },
                            Modifier.weight(1f).semantics { contentDescription = "Dia anterior" },
                            secondary = true
                        )
                        SagaButton("Hoje", { selected = current; week = weekOf(current) }, Modifier.weight(1f), secondary = true)
                        SagaButton(
                            "›",
                            {
                                val next = selDate.plusDays(1).toString()
                                selected = next
                                week = weekOf(next)
                            },
                            Modifier.weight(1f).semantics { contentDescription = "Próximo dia" },
                            secondary = true
                        )
                    }
                }
            }
            1 -> {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SagaButton(
                            "‹",
                            { week = start.minusWeeks(1).toString(); selected = week },
                            Modifier.weight(1f).semantics { contentDescription = "Semana anterior" },
                            secondary = true
                        )
                        SagaButton("Hoje", { week = weekOf(current); selected = current }, Modifier.weight(1f), secondary = true)
                        SagaButton(
                            "›",
                            { week = start.plusWeeks(1).toString(); selected = week },
                            Modifier.weight(1f).semantics { contentDescription = "Próxima semana" },
                            secondary = true
                        )
                    }
                }
                item {
                    SectionHeader(
                        if (week == weekOf(current)) "Semana atual" else "Semana selecionada",
                        displayPeriod(week, days.last())
                    )
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        days.forEach { date ->
                            val d = LocalDate.parse(date)
                            val active = selected == date
                            val isToday = date == current
                            val summaryForDay = agendaDay(s, date)
                            val extraCount = summaryForDay.tasks.size + summaryForDay.blocks.size + summaryForDay.events.size
                            Column(
                                Modifier.width(72.dp).heightIn(min = 88.dp)
                                    .background(if (active) Blue.copy(alpha = .18f) else if (isToday) Gold.copy(alpha = .1f) else Surface, dayShape)
                                    .border(1.dp, if (active) Blue else if (isToday) Gold.copy(alpha = .55f) else ColdIron.copy(alpha = .25f), dayShape)
                                    .clickable { selected = date }
                                    .padding(8.dp)
                                    .semantics(mergeDescendants = true) {
                                        contentDescription = "Selecionar ${displayDate(date)}"
                                        this.selected = active
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(listOf("SEG", "TER", "QUA", "QUI", "SEX", "SÁB", "DOM")[d.dayOfWeek.value - 1], fontSize = 10.sp, letterSpacing = .5.sp, color = if (active) Blue else if (isToday) Gold else Muted)
                                Text(d.dayOfMonth.toString(), fontFamily = SagaFont, fontSize = 22.sp, color = if (active) Blue else Ink)
                                Text(
                                    if (date in s.restDates) "Descanso" else "${summaryForDay.habits.count { s.record(it.id, date)?.complete == true }}/${summaryForDay.habits.size}",
                                    fontSize = 10.sp,
                                    color = if (active) Blue else Muted
                                )
                                if (extraCount > 0) {
                                    Text("+$extraCount", fontSize = 9.sp, color = Gold)
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                item {
                    val parsed = LocalDate.parse(selected)
                    val monthStart = parsed.withDayOfMonth(1)
                    val offset = monthStart.dayOfWeek.value - 1
                    val len = monthStart.lengthOfMonth()
                    val cellShape = CutCornerShape(4.dp)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            IconButton({
                                val prev = parsed.minusMonths(1).toString()
                                selected = prev
                                week = weekOf(prev)
                            }) { Icon(Icons.Default.ChevronLeft, "Mês anterior") }
                            Text(
                                monthStart.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("pt-BR"))).replaceFirstChar { it.uppercase() },
                                fontFamily = SagaFont,
                                fontSize = 20.sp
                            )
                            IconButton({
                                val next = parsed.plusMonths(1).toString()
                                selected = next
                                week = weekOf(next)
                            }) { Icon(Icons.Default.ChevronRight, "Próximo mês") }
                        }
                        Row(Modifier.fillMaxWidth()) {
                            weekdays.forEach { Text(it, Modifier.weight(1f), color = Muted, fontSize = 11.sp) }
                        }
                        for (row in 0 until (offset + len + 6) / 7) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                for (col in 0..6) {
                                    val n = row * 7 + col - offset + 1
                                    val d = if (n in 1..len) monthStart.withDayOfMonth(n).toString() else ""
                                    val marker = if (d.isNotBlank()) monthMarker(s, d) else null
                                    val isSel = d == selected
                                    Box(
                                        Modifier.weight(1f).aspectRatio(1f).clip(cellShape)
                                            .background(
                                                when {
                                                    isSel -> Blue.copy(alpha = .22f)
                                                    marker != null && marker.habitsDone > 0 -> Green.copy(alpha = .14f)
                                                    marker != null && marker.hasActivity -> Gold.copy(alpha = .09f)
                                                    else -> Surface
                                                }
                                            )
                                            .border(
                                                1.dp,
                                                when {
                                                    isSel -> Blue
                                                    marker?.hasDeadline == true -> Violet.copy(alpha = .65f)
                                                    marker?.rest == true -> Green.copy(alpha = .45f)
                                                    d.isNotBlank() -> ColdIron.copy(alpha = .2f)
                                                    else -> Color.Transparent
                                                },
                                                cellShape
                                            )
                                            .clickable(enabled = d.isNotBlank()) { selected = d; week = weekOf(d) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (d.isNotBlank() && marker != null) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(n.toString(), color = if (isSel) Blue else Ink, fontSize = 12.sp)
                                                val dots = buildString {
                                                    if (marker.rest) append("☽")
                                                    if (marker.habitsPlanned > 0) append("·")
                                                    if (marker.tasksCount + marker.blocksCount + marker.eventsCount > 0) append("◆")
                                                }
                                                if (dots.isNotEmpty()) {
                                                    Text(dots, fontSize = 8.sp, lineHeight = 9.sp, color = if (marker.hasDeadline) Violet else Gold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Text("Legenda: · hábitos · ◆ tarefas/blocos/compromissos · borda violeta = prazo · ☽ descanso", color = Muted, fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(
                    displayDate(selected),
                    "${days.sumOf { date -> s.due(date).count { s.record(it.id, date)?.complete == true } }}/${days.sumOf { s.due(it).size }} atividades na semana selecionada"
                )
                MetricPair(
                    "$done/${due.size}",
                    "hábitos concluídos neste dia",
                    if (due.isEmpty()) "—" else "${done * 100 / due.size}%",
                    "cumprimento"
                )
                Meter(if (due.isEmpty()) 0f else done.toFloat() / due.size, Blue, slim = true)
                if (selected >= current) {
                    Toggle("Descanso global neste dia", selected in s.restDates) { v -> vm.run { vm.repo.rest(selected, v) } }
                }
                if (selected in s.restDates) {
                    Text("O descanso tem prioridade sobre inclusões pontuais. As exceções ficam guardadas.", color = Muted, fontSize = 13.sp)
                }
                Hairline()
            }
        }

        if (daySummary.overlappingBlockIds.isNotEmpty()) {
            item {
                StatusSeal("Atenção: há blocos de tempo com horários sobrepostos nesta data.", Gold)
            }
        }

        if (daySummary.deadlines.isNotEmpty()) {
            item { SectionHeader("Prazos de Caminhos") }
            items(daySummary.deadlines, key = { "deadline:${it.id}" }) { p ->
                AetherSurface(SurfaceKind.Heroic, Modifier.clickable { onOpenPath(p.id) }) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Eyebrow("Prazo do caminho", Gold)
                            Text(p.name, style = MaterialTheme.typography.titleMedium)
                        }
                        StatusSeal("Prazo hoje", Gold)
                    }
                }
            }
        }

        if (daySummary.events.isNotEmpty()) {
            item { SectionHeader("Compromissos", "Agendamentos pessoais · não concedem XP") }
            items(daySummary.events, key = { "event:${it.id}" }) { ev ->
                AetherSurface(modifier = Modifier.testTag("event:${ev.id}")) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(
                            checked = ev.completed,
                            onCheckedChange = { vm.run { vm.repo.toggleEvent(ev.id) } },
                            modifier = Modifier.size(48.dp).semantics { contentDescription = "Concluir compromisso ${ev.title}" }
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                ev.title,
                                style = MaterialTheme.typography.titleMedium,
                                textDecoration = if (ev.completed) TextDecoration.LineThrough else TextDecoration.None
                            )
                            val timeLabel = if (ev.startTime.isNotBlank()) "${ev.startTime}${if (ev.endTime.isNotBlank()) "–${ev.endTime}" else ""}" else "Sem horário"
                            Text("${ev.kind} · $timeLabel", color = Muted, fontSize = 12.sp)
                        }
                        TextButton({ eventEditor = ev }) { Text("Editar", fontSize = 12.sp) }
                        TextButton({ deleteEvent = ev }) { Text("Excluir", color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                    }
                    if (ev.description.isNotBlank()) Text(ev.description, color = Muted, fontSize = 12.sp)
                }
            }
        }

        if (daySummary.blocks.isNotEmpty()) {
            item { SectionHeader("Blocos de Tempo", "Tempo planejado comparado ao foco efetivo") }
            items(daySummary.blocks, key = { "block:${it.id}" }) { b ->
                val plannedMin = timeBlockMinutes(b)
                val measured = blockFocus[b.id] ?: BlockFocus()
                val actualMin = measured.minutes
                val overlap = b.id in daySummary.overlappingBlockIds
                val linkLabel = when {
                    b.taskId.isNotBlank() -> s.pathTasks.find { it.id == b.taskId }?.title
                    b.pathId.isNotBlank() -> s.paths.find { it.id == b.pathId }?.name
                    b.habitId.isNotBlank() -> s.habits.find { it.id == b.habitId }?.name
                    else -> null
                }
                AetherSurface(
                    modifier = Modifier.testTag("block:${b.id}").then(
                        if (overlap) Modifier.border(1.dp, Gold.copy(alpha = .75f), CutCornerShape(4.dp)) else Modifier
                    )
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${b.startTime} – ${b.endTime} · ${b.category}", color = habitColor(b.color.coerceIn(0, 4)), fontSize = 12.sp)
                            Text(b.title, style = MaterialTheme.typography.titleMedium)
                            if (!linkLabel.isNullOrBlank()) Text("Vínculo: $linkLabel", color = Muted, fontSize = 11.sp)
                        }
                        if (overlap) StatusSeal("Sobreposto", Gold)
                    }
                    Text("Planejado: $plannedMin min · ${if (measured.uncertain) "Foco comprovado: ≥" else "Foco real: "}$actualMin min", color = if (actualMin > 0) Violet else Muted, fontSize = 12.sp)
                    if (measured.uncertain) Text("Parte da sessão não tem horário efetivo comprovável.", color = Muted, fontSize = 11.sp)
                    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (b.pathId.isNotBlank() || b.taskId.isNotBlank()) {
                            TextButton({ onStartPathFocus(b.pathId, b.taskId, b.title) }, enabled = s.focus == null && (b.taskId.isBlank() || s.pathTasks.any { it.id == b.taskId && !it.archived && (b.pathId.isBlank() || it.pathId == b.pathId) })) {
                                Text("Iniciar foco", color = Violet, fontSize = 12.sp)
                            }
                        }
                        TextButton({ blockEditor = b }) { Text("Editar", fontSize = 12.sp) }
                        TextButton({ deleteBlock = b }) { Text("Excluir", color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                    }
                }
            }
        }

        if (daySummary.tasks.isNotEmpty()) {
            item { SectionHeader("Tarefas de Caminhos nesta data", "Conclua diretamente sem XP inventado") }
            items(daySummary.tasks, key = { "ptask:${it.id}" }) { t ->
                val parentPath = s.paths.find { it.id == t.pathId }
                AetherSurface(modifier = Modifier.testTag("agenda_task:${t.id}")) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(
                            checked = t.completed,
                            onCheckedChange = { checked -> vm.run { vm.repo.toggleTask(t.id, checked) } },
                            modifier = Modifier.size(48.dp).semantics { contentDescription = "Alternar ${t.title}" }
                        )
                        Column(Modifier.weight(1f).clickable(enabled = parentPath != null) { parentPath?.let { onOpenPath(it.id) } }) {
                            Text(
                                t.title,
                                style = MaterialTheme.typography.titleMedium,
                                textDecoration = if (t.completed) TextDecoration.LineThrough else TextDecoration.None
                            )
                            Text(
                                buildList {
                                    parentPath?.let { add(it.name) }
                                    if (t.scheduledTime.isNotBlank()) add(t.scheduledTime)
                                    if (t.deadline == selected) add("Prazo hoje")
                                    add(priorityLabels[t.priority.coerceIn(priorityLabels.indices)])
                                }.joinToString(" · "),
                                color = Muted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(
                "Agenda do dia",
                if (selected < current) "Histórico de programação · sem ajustes retroativos" else "Horários do hábito · ajustes válidos só nesta data"
            )
        }
        if (due.isEmpty()) {
            item {
                Empty("Nenhuma atividade prevista. Você pode incluir um hábito ativo ou reservar este dia para descanso.")
            }
        }
        items(due, key = { it.id }) { h ->
            val comp = s.record(h.id, selected)?.complete == true
            AetherSurface(
                modifier = if (comp) Modifier.border(1.dp, Green.copy(alpha = .45f), CutCornerShape(4.dp)) else Modifier
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AttributeEmblem(h.attribute, Modifier.size(30.dp))
                    Text(h.time.ifBlank { "Sem horário" }, color = if (comp) Green else Blue, fontSize = 12.sp)
                    Column(Modifier.weight(1f)) {
                        Text(h.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (comp) "◆ Concluído" else "${h.target} ${if (h.type == 2 && (h.unit.isBlank() || h.unit == "vez")) "minutos" else h.unit} · ${h.xp} XP",
                            fontSize = 12.sp,
                            color = if (comp) Green else Muted
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (selected <= current) {
                        SagaButton(
                            "Registrar",
                            {
                                onRecord(
                                    h,
                                    s.record(h.id, selected) ?: HabitRecord(
                                        "${h.id}@$selected", h.id, selected, 0, h.target,
                                        h.xp, h.coins, h.points, h.attribute, h.proportional, h.type, h.category, h.unit, h.name
                                    )
                                )
                            },
                            Modifier.weight(1f),
                            completed = comp
                        )
                    }
                    if (selected >= current) {
                        SagaButton("Ajustar", { editingHabit = h }, Modifier.weight(1f), secondary = true)
                    }
                }
            }
        }
        if (selected >= current) {
            item {
                SectionHeader("Ajustes pontuais", "Incluir, dispensar ou voltar à programação normal. Não concede XP.")
            }
            items(s.habits.filter { !it.paused && !it.archived && !it.deleted && it.created <= selected }, key = { "adjust:${it.id}" }) { h ->
                val exception = s.plans.find { it.habitId == h.id && it.date == selected }
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable { editingHabit = h },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(h.name, fontSize = 14.sp)
                        Text(
                            when (exception?.include) {
                                true -> "◆ Incluído nesta data"
                                false -> "◇ Dispensado nesta data"
                                null -> "Programação normal"
                            },
                            color = when (exception?.include) {
                                true -> Blue
                                false -> Gold
                                null -> Muted
                            },
                            fontSize = 12.sp
                        )
                    }
                    TextButton({ editingHabit = h }) { Text("Ajustar ${h.name}") }
                }
            }
        }
    }

    editingHabit?.let { h ->
        AetherDialog(
            onDismissRequest = { editingHabit = null },
            title = h.name,
            confirmButton = { TextButton({ editingHabit = null }) { Text("Cancelar") } }
        ) {
            Text("Somente ${displayDate(selected)}. Pausa, arquivo, exclusão e descanso continuam com prioridade.", color = Muted)
            listOf("Programação normal", "Incluir nesta data", "Dispensar nesta data").forEachIndexed { i, label ->
                SagaButton(
                    label,
                    { vm.run { vm.repo.plan(h.id, selected, i); editingHabit = null } },
                    Modifier.fillMaxWidth(),
                    secondary = i == 0
                )
            }
        }
    }

    blockEditor?.let { b ->
        TimeBlockEditorDialog(
            initial = b,
            s = s,
            onDismiss = { blockEditor = null },
            onSave = { updated ->
                vm.run { vm.repo.saveTimeBlock(updated); blockEditor = null }
            }
        )
    }

    eventEditor?.let { ev ->
        AgendaEventEditorDialog(
            initial = ev,
            onDismiss = { eventEditor = null },
            onSave = { updated ->
                vm.run { vm.repo.saveEvent(updated); eventEditor = null }
            }
        )
    }

    deleteBlock?.let { b ->
        AetherDialog(
            onDismissRequest = { deleteBlock = null },
            title = "Excluir bloco de tempo?",
            confirmButton = {
                SagaButton("Excluir", { vm.run { vm.repo.deleteTimeBlock(b.id) }; deleteBlock = null }, destructive = true)
            },
            dismissButton = { TextButton({ deleteBlock = null }) { Text("Cancelar") } }
        ) {
            Text("O bloco “${b.title}” será removido da agenda. Sessões de foco já realizadas permanecem intactas.")
        }
    }

    deleteEvent?.let { ev ->
        AetherDialog(
            onDismissRequest = { deleteEvent = null },
            title = "Excluir compromisso?",
            confirmButton = {
                SagaButton("Excluir", { vm.run { vm.repo.deleteEvent(ev.id) }; deleteEvent = null }, destructive = true)
            },
            dismissButton = { TextButton({ deleteEvent = null }) { Text("Cancelar") } }
        ) {
            Text("O compromisso “${ev.title}” será removido da agenda.")
        }
    }
}

@Composable
fun TimeBlockEditorDialog(
    initial: TimeBlock,
    s: GameState,
    onDismiss: () -> Unit,
    onSave: (TimeBlock) -> Unit
) {
    var title by rememberSaveable(initial.id) { mutableStateOf(initial.title) }
    var date by rememberSaveable(initial.id) { mutableStateOf(initial.date) }
    var startTime by rememberSaveable(initial.id) { mutableStateOf(initial.startTime) }
    var endTime by rememberSaveable(initial.id) { mutableStateOf(initial.endTime) }
    var category by rememberSaveable(initial.id) { mutableStateOf(initial.category) }
    var color by rememberSaveable(initial.id) { mutableIntStateOf(initial.color) }
    var desc by rememberSaveable(initial.id) { mutableStateOf(initial.description) }
    var pathId by rememberSaveable(initial.id) { mutableStateOf(initial.pathId) }
    var taskId by rememberSaveable(initial.id) { mutableStateOf(initial.taskId) }
    var habitId by rememberSaveable(initial.id) { mutableStateOf(initial.habitId) }

    val draft = initial.copy(
        title = title.trim(),
        date = date.trim(),
        startTime = startTime.trim(),
        endTime = endTime.trim(),
        category = category.trim().ifBlank { "Foco" },
        color = color,
        description = desc,
        pathId = pathId,
        taskId = taskId,
        habitId = habitId
    )
    val overlapPreview = remember(draft, s.timeBlocks) {
        val sameDay = s.timeBlocks.filter { it.date == draft.date && it.id != draft.id } + draft
        draft.id in timeBlockOverlaps(sameDay)
    }

    FullForm(
        if (initial.title.isBlank()) "Novo bloco de tempo" else "Editar bloco de tempo",
        onDismiss,
        { onSave(draft) }
    ) {
        if (overlapPreview) {
            StatusSeal("Este horário sobrepõe outro bloco existente nessa data.", Gold)
        }
        FormField(title, { title = it }, "Título do bloco")
        FormField(date, { date = it }, "Data · AAAA-MM-DD")
        FormField(startTime, { startTime = it }, "Início · HH:mm")
        FormField(endTime, { endTime = it }, "Término · HH:mm")
        FormField(category, { category = it }, "Categoria")
        ChipRow(listOf("Ouro", "Azul", "Violeta", "Verde", "Coral"), color, { color = it })
        FormField(desc, { desc = it }, "Descrição opcional", multi = true)
        val openPaths = s.paths.filter { it.state !in listOf(4, 5) || it.id == pathId }
        if (openPaths.isNotEmpty()) {
            Eyebrow("Vincular a um caminho (opcional)")
            val pNames = listOf("Nenhum") + openPaths.map { it.name }
            val pIds = listOf("") + openPaths.map { it.id }
            ChipRow(pNames, pIds.indexOf(pathId).coerceAtLeast(0), {
                pathId = pIds[it]
                if (pathId.isBlank()) taskId = ""
            })
        }
        val pathTasks = s.pathTasks.filter { !it.archived && (pathId.isBlank() || it.pathId == pathId) }
        if (pathTasks.isNotEmpty()) {
            Eyebrow("Vincular a uma tarefa (opcional)")
            val tNames = listOf("Nenhuma") + pathTasks.map { it.title }
            val tIds = listOf("") + pathTasks.map { it.id }
            ChipRow(tNames, tIds.indexOf(taskId).coerceAtLeast(0), { taskId = tIds[it] })
        }
        val activeHabits = s.habits.filter { !it.deleted && !it.archived }
        if (activeHabits.isNotEmpty()) {
            Eyebrow("Vincular a um hábito (opcional)")
            val hNames = listOf("Nenhum") + activeHabits.map { it.name }
            val hIds = listOf("") + activeHabits.map { it.id }
            ChipRow(hNames, hIds.indexOf(habitId).coerceAtLeast(0), { habitId = hIds[it] })
        }
    }
}

@Composable
fun AgendaEventEditorDialog(
    initial: AgendaEvent,
    onDismiss: () -> Unit,
    onSave: (AgendaEvent) -> Unit
) {
    var title by rememberSaveable(initial.id) { mutableStateOf(initial.title) }
    var date by rememberSaveable(initial.id) { mutableStateOf(initial.date) }
    var startTime by rememberSaveable(initial.id) { mutableStateOf(initial.startTime) }
    var endTime by rememberSaveable(initial.id) { mutableStateOf(initial.endTime) }
    var kind by rememberSaveable(initial.id) { mutableStateOf(initial.kind) }
    var desc by rememberSaveable(initial.id) { mutableStateOf(initial.description) }

    FullForm(
        if (initial.title.isBlank()) "Novo compromisso" else "Editar compromisso",
        onDismiss,
        {
            onSave(
                initial.copy(
                    title = title.trim(),
                    date = date.trim(),
                    startTime = startTime.trim(),
                    endTime = endTime.trim(),
                    kind = kind,
                    description = desc
                )
            )
        }
    ) {
        FormField(title, { title = it }, "Título do compromisso")
        Eyebrow("Tipo")
        ChipRow(eventKinds, eventKinds.indexOf(kind).coerceAtLeast(0), { kind = eventKinds[it] })
        FormField(date, { date = it }, "Data · AAAA-MM-DD")
        FormField(startTime, { startTime = it }, "Início opcional · HH:mm")
        FormField(endTime, { endTime = it }, "Término opcional · HH:mm")
        FormField(desc, { desc = it }, "Observações", multi = true)
        Panel(accent = Blue) {
            Text("Compromissos organizam seu calendário real e não concedem XP ou moedas.", color = Muted, fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InboxScreen(
    s: GameState,
    vm: GameViewModel,
    onOpenPath: (String) -> Unit = {}
) {
    var quickTitle by rememberSaveable { mutableStateOf("") }
    var quickNotes by rememberSaveable { mutableStateOf("") }
    var triageTaskItem by remember { mutableStateOf<InboxItem?>(null) }
    var triageEventItem by remember { mutableStateOf<InboxItem?>(null) }
    var deleteCandidate by remember { mutableStateOf<InboxItem?>(null) }

    val items = remember(s.inboxItems) { s.inboxItems.filter { !it.archived } }

    LazyColumn(
        Modifier.fillMaxSize().testTag("inbox_list"),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Title("Caixa de Entrada", "Capture rápido ideias e demandas antes de decidir onde pertencem.")
        }
        item {
            AetherSurface(SurfaceKind.Heroic) {
                FormField(quickTitle, { quickTitle = it }, "Capturar item rápido")
                FormField(quickNotes, { quickNotes = it }, "Notas opcionais", multi = true)
                Action(
                    "+ Capturar",
                    {
                        vm.run {
                            vm.repo.saveInboxItem(InboxItem(title = quickTitle.trim(), notes = quickNotes))
                            quickTitle = ""
                            quickNotes = ""
                        }
                    },
                    Modifier.fillMaxWidth(),
                    enabled = quickTitle.isNotBlank()
                )
            }
        }
        if (items.isEmpty()) {
            item { Empty("Caixa de Entrada vazia. Use o campo acima para registrar algo rapidamente.") }
        }
        items(items, key = { it.id }) { item ->
            AetherSurface(modifier = Modifier.testTag("inbox:${item.id}")) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text(displayDate(item.created), color = Muted, fontSize = 11.sp)
                }
                if (item.notes.isNotBlank()) {
                    Text(item.notes, color = Muted, fontSize = 13.sp)
                }
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SagaButton("Organizar em tarefa", { triageTaskItem = item })
                    SagaButton("Agendar compromisso", { triageEventItem = item }, secondary = true)
                    TextButton({ deleteCandidate = item }) {
                        Text("Excluir", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    triageTaskItem?.let { item ->
        InboxTriageTaskDialog(
            item = item,
            paths = s.paths,
            stages = s.pathStages,
            onDismiss = { triageTaskItem = null },
            onConfirm = { task, subtasks ->
                vm.run {
                    vm.repo.triageInboxToTask(item.id, task, subtasks)
                    triageTaskItem = null
                    if (task.pathId.isNotBlank()) onOpenPath(task.pathId)
                }
            }
        )
    }

    triageEventItem?.let { item ->
        AgendaEventEditorDialog(
            initial = AgendaEvent(title = item.title, description = item.notes, date = today()),
            onDismiss = { triageEventItem = null },
            onSave = { ev ->
                vm.run { vm.repo.triageInboxToEvent(item.id, ev); triageEventItem = null }
            }
        )
    }

    deleteCandidate?.let { item ->
        AetherDialog(
            onDismissRequest = { deleteCandidate = null },
            title = "Excluir da Caixa de Entrada?",
            confirmButton = {
                SagaButton("Excluir", { vm.run { vm.repo.deleteInboxItem(item.id) }; deleteCandidate = null }, destructive = true)
            },
            dismissButton = { TextButton({ deleteCandidate = null }) { Text("Cancelar") } }
        ) {
            Text("“${item.title}” será removido permanentemente.")
        }
    }
}

@Composable
private fun InboxTriageTaskDialog(
    item: InboxItem,
    paths: List<PersonalPath>,
    stages: List<PathStage>,
    onDismiss: () -> Unit,
    onConfirm: (PathTask, List<String>) -> Unit
) {
    var title by rememberSaveable(item.id) { mutableStateOf(item.title) }
    var notes by rememberSaveable(item.id) { mutableStateOf(item.notes) }
    var pathId by rememberSaveable(item.id) { mutableStateOf("") }
    var stageId by rememberSaveable(item.id) { mutableStateOf("") }
    var priority by rememberSaveable(item.id) { mutableIntStateOf(1) }
    var scheduledDate by rememberSaveable(item.id) { mutableStateOf(today()) }
    var scheduledTime by rememberSaveable(item.id) { mutableStateOf("") }
    var deadline by rememberSaveable(item.id) { mutableStateOf("") }
    var subtasksText by rememberSaveable(item.id) { mutableStateOf("") }

    val availableStages = stages.filter { it.pathId == pathId }.sortedBy { it.position }
    if (stageId.isNotBlank() && availableStages.none { it.id == stageId }) stageId = ""

    FullForm(
        "Organizar em tarefa",
        onDismiss,
        {
            val subtasks = subtasksText.lines().map { it.trim() }.filter { it.isNotBlank() }
            onConfirm(
                PathTask(
                    pathId = pathId,
                    stageId = if (pathId.isBlank()) "" else stageId,
                    title = title.trim(),
                    priority = priority,
                    deadline = deadline.trim(),
                    scheduledDate = scheduledDate.trim(),
                    scheduledTime = scheduledTime.trim(),
                    notes = notes
                ),
                subtasks
            )
        }
    ) {
        FormField(title, { title = it }, "Título da tarefa")
        Eyebrow("Prioridade")
        ChipRow(priorityLabels, priority, { priority = it })
        val openPaths = paths.filter { it.state !in listOf(4, 5) }
        if (openPaths.isNotEmpty()) {
            Eyebrow("Caminho (opcional)")
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
        FormField(scheduledTime, { scheduledTime = it }, "Horário opcional · HH:mm")
        FormField(deadline, { deadline = it }, "Prazo opcional · AAAA-MM-DD")
        FormField(subtasksText, { subtasksText = it }, "Subtarefas iniciais (uma por linha)", multi = true)
        FormField(notes, { notes = it }, "Notas", multi = true)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PendingCenterScreen(
    s: GameState,
    vm: GameViewModel,
    onOpenPath: (String) -> Unit
) {
    val day = currentDay()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val buckets = remember(s, day) { pendingBuckets(s, day) }
    val labels = listOf("Hoje", "Próximos dias", "Atrasadas", "Sem data", "Alta prioridade", "Concluídas")
    val list = when (tab) {
        0 -> buckets.todayTasks
        1 -> buckets.upcomingTasks
        2 -> buckets.overdueTasks
        3 -> buckets.undatedTasks
        4 -> buckets.highPriorityTasks
        else -> buckets.completedTasks
    }
    var quickTaskDialog by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier.fillMaxSize().testTag("pending_list"),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Title("Central de Pendências", "Todas as entregas ativas agrupadas por urgência e horizonte.")
        }
        item {
            AetherisTabs(labels, tab, { tab = it })
        }
        item {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${list.size} ${if (list.size == 1) "tarefa" else "tarefas"} em ${labels[tab]}",
                        Modifier.weight(1f),
                        color = Muted,
                        fontSize = 12.sp
                    )
                    SagaButton("+ Nova tarefa", { quickTaskDialog = true }, secondary = true)
                }
                Text("Projetos arquivados ou cancelados permanecem fora das pendências ativas.", color = Muted, fontSize = 11.sp)
            }
        }
        if (list.isEmpty()) {
            item { Empty("Nenhuma tarefa em ${labels[tab].lowercase()}.") }
        }
        items(list, key = { it.id }) { t ->
            val parent = s.paths.find { it.id == t.pathId }
            val stage = s.pathStages.find { it.id == t.stageId }
            val overdue = isTaskOverdue(t, day)
            val subs = s.pathSubtasks.filter { it.taskId == t.id }
            AetherSurface(
                modifier = Modifier.testTag("pending:${t.id}").then(
                    if (overdue) Modifier.border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = .65f), CutCornerShape(4.dp))
                    else Modifier
                )
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Checkbox(
                        checked = t.completed,
                        onCheckedChange = { checked -> vm.run { vm.repo.toggleTask(t.id, checked) } },
                        modifier = Modifier.size(48.dp).semantics { contentDescription = "Alternar ${t.title}" }
                    )
                    Column(Modifier.weight(1f).clickable(enabled = parent != null) { parent?.let { onOpenPath(it.id) } }) {
                        Text(
                            t.title,
                            style = MaterialTheme.typography.titleMedium,
                            textDecoration = if (t.completed) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Text(
                            buildList {
                                add(parent?.name ?: "Tarefa avulsa")
                                stage?.let { add(it.name) }
                                add(priorityLabels[t.priority.coerceIn(priorityLabels.indices)])
                                if (t.scheduledDate.isNotBlank()) add("Agenda ${displayDate(t.scheduledDate)}")
                                if (t.deadline.isNotBlank()) add("Prazo ${displayDate(t.deadline)}")
                                if (subs.isNotEmpty()) add("${subs.count { it.done }}/${subs.size} subtarefas")
                            }.joinToString(" · "),
                            color = if (overdue) MaterialTheme.colorScheme.error else Muted,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (overdue) StatusSeal("Atrasada", MaterialTheme.colorScheme.error)
                }
                if (parent != null) {
                    TextButton({ onOpenPath(parent.id) }) {
                        Text("Abrir ${parent.name}", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (quickTaskDialog) {
        TaskEditorDialog(
            initial = PathTask(title = "", scheduledDate = if (tab == 0) day else ""),
            paths = s.paths,
            stages = s.pathStages,
            onDismiss = { quickTaskDialog = false },
            onSave = { task ->
                vm.run { vm.repo.saveTask(task); quickTaskDialog = false }
            }
        )
    }
}
