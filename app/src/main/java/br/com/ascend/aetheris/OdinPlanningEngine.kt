package br.com.ascend.aetheris

import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * ASCEND 1.7.5 / 1.7.5.1 — A Mesa de Odin
 * Motor determinístico, 100% offline, sem força bruta exponencial e sem IA externa.
 * Cobre disponibilidade, grafo de dependências, caminho crítico (CPM), geração de propostas
 * adaptativas, simulador de capacidade, replanejamento, Matriz de Eisenhower e recorrências.
 */

data class MinuteInterval(val start: Int, val end: Int) {
    val minutes: Int get() = (end - start).coerceAtLeast(0)
    fun overlaps(other: MinuteInterval): Boolean = start < other.end && other.start < end
}

/**
 * 1.7.5.1 (P1 — Horários Passados): Referência temporal injetável e determinística.
 * Evita consultas dispersas ao relógio do sistema durante uma mesma operação e permite
 * filtrar janelas já transcorridas no dia corrente sem afetar consultas históricas explícitas.
 */
data class PlanningClock(
    val referenceDate: String = today(),
    val referenceMinuteOfDay: Int = 0
) {
    companion object {
        fun fromClock(clock: Clock = Clock.systemDefaultZone()): PlanningClock {
            val zdt = ZonedDateTime.now(clock)
            return PlanningClock(
                referenceDate = zdt.toLocalDate().toString(),
                referenceMinuteOfDay = zdt.hour * 60 + zdt.minute
            )
        }

        fun at(date: String, time: String = "00:00"): PlanningClock =
            PlanningClock(
                referenceDate = date,
                referenceMinuteOfDay = parseTimeToMinutes(time) ?: 0
            )
    }
}

/**
 * 1.7.5.1 (P1 — Dependências): Diferenciação explícita do estado de agendamento de uma tarefa.
 * Uma tarefa parcialmente agendada NUNCA é tratada como conclusão projetada para liberar dependentes.
 */
enum class TaskScheduleState(val label: String) {
    UNSCHEDULED("Não agendada"),
    PARTIALLY_SCHEDULED("Parcialmente agendada"),
    FULLY_SCHEDULED("Integralmente agendada"),
    COMPLETED("Realmente concluída")
}

enum class TaskWorkflowState(val label: String) {
    AVAILABLE("Disponível"),
    IN_PROGRESS("Em andamento"),
    BLOCKED("Bloqueada"),
    COMPLETED("Concluída"),
    OVERDUE("Atrasada"),
    ARCHIVED("Arquivada")
}

enum class EisenhowerQuadrant(val id: String, val title: String, val subtitle: String) {
    Q1_DO("q1", "Fazer agora", "Importante e urgente"),
    Q2_PLAN("q2", "Planejar", "Importante e não urgente"),
    Q3_DELEGATE("q3", "Reduzir / Simplificar", "Urgente e menos importante"),
    Q4_ELIMINATE("q4", "Reavaliar / Adiar", "Nem urgente nem importante")
}

enum class CapacityPeriodKind(val label: String) {
    TODAY("Hoje"),
    NEXT_7_DAYS("Próximos 7 dias"),
    CALENDAR_WEEK("Semana civil"),
    NEXT_30_DAYS("Próximos 30 dias"),
    CUSTOM("Intervalo personalizado")
}

enum class CapacityStatus(val label: String) {
    UNCONFIGURED("Disponibilidade não configurada"),
    COMFORTABLE("Confortável"),
    BALANCED("Equilibrada"),
    TIGHT("Apertada"),
    OVERLOADED("Sobrecarga")
}

data class CriticalPathNode(
    val task: PathTask,
    val durationMinutes: Int,
    val estimatedMissing: Boolean,
    val earlyStart: Int,
    val earlyFinish: Int,
    val lateStart: Int,
    val lateFinish: Int,
    val slackMinutes: Int,
    val isCritical: Boolean,
    val earliestDate: String,
    val finishDate: String
)

data class CriticalPathResult(
    val pathId: String,
    val hasCycle: Boolean,
    val totalCriticalMinutes: Int,
    val nodes: List<CriticalPathNode>,
    val criticalSequence: List<CriticalPathNode>,
    val missingEstimateTaskIds: List<String>,
    val effortProjectedDate: String?,
    val availabilityProjectedDate: String?,
    val deadlineRisk: Boolean,
    val premises: List<String>
)

data class PlannedSuggestion(
    val id: String,
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val minutes: Int,
    val sourceType: String, // task | habit | mimir
    val taskId: String = "",
    val pathId: String = "",
    val habitId: String = "",
    val areaId: String = "",
    val category: String = "Foco",
    val color: Int = 1,
    val reason: String = "",
    val splitIndex: Int = 1,
    val splitTotal: Int = 1,
    val selected: Boolean = true,
    val pinned: Boolean = false
)

data class UnscheduledItem(
    val id: String,
    val title: String,
    val sourceType: String,
    val remainingMinutes: Int,
    val reason: String,
    val deadline: String = "",
    val scheduleState: TaskScheduleState = TaskScheduleState.UNSCHEDULED
)

data class OdinProposal(
    val strategy: OdinStrategyDef,
    val dateFrom: String,
    val dateTo: String,
    val suggestions: List<PlannedSuggestion>,
    val unscheduled: List<UnscheduledItem>,
    val conflicts: List<String>,
    val totalAvailableMinutes: Int,
    val totalSuggestedMinutes: Int,
    val usedHistoryPreference: Boolean,
    val preferredHistoryBucket: String?,
    val taskScheduleStates: Map<String, TaskScheduleState> = emptyMap()
)

data class CapacitySimulation(
    val periodLabel: String,
    val dateFrom: String,
    val dateTo: String,
    val configuredAvailableMinutes: Int,
    val fixedEventsMinutes: Int,
    val existingBlocksMinutes: Int,
    val freeCapacityMinutes: Int,
    val pendingTasksDemandMinutes: Int,
    val habitsDemandMinutes: Int,
    val mimirReviewDemandMinutes: Int,
    val totalUnallocatedDemandMinutes: Int,
    val balanceMinutes: Int,
    val utilizationPercent: Int,
    val status: CapacityStatus,
    val unestimatedTasks: List<PathTask>,
    val incompatibleDeadlineTasks: List<PathTask>,
    val warnings: List<String>
)

enum class ReplanActionKind(val label: String) {
    TRANSFER("Transferir para próximo horário livre"),
    SPLIT("Dividir em blocos menores"),
    POSTPONE("Adiar para a próxima semana"),
    SEND_TO_PENDING("Enviar para Central de Pendências"),
    REMOVE_SCHEDULE("Remover agendamento")
}

data class ReplanSlotOption(
    val action: ReplanActionKind,
    val summary: String,
    val targetDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val secondDate: String = "",
    val secondStartTime: String = "",
    val secondEndTime: String = ""
)

data class ReplanItem(
    val id: String,
    val title: String,
    val taskId: String,
    val blockId: String,
    val pathId: String,
    val areaId: String,
    val originalDate: String,
    val minutes: Int,
    val reason: String,
    val options: List<ReplanSlotOption>
)

object OdinPlanningEngine {

    const val MIN_HISTORY_SESSIONS_FOR_SOFT_PREF = 5
    const val DEFAULT_UNESTIMATED_CPM_FALLBACK_MIN = 30

    // =========================================================================
    // 1. INTERVALOS, PROJEÇÃO DE RECORRÊNCIAS E DISPONIBILIDADE REAL
    // =========================================================================

    fun mergeIntervals(intervals: List<MinuteInterval>): List<MinuteInterval> {
        val valid = intervals.filter { it.end > it.start }.sortedWith(compareBy<MinuteInterval> { it.start }.thenBy { it.end })
        if (valid.isEmpty()) return emptyList()
        val merged = mutableListOf(valid.first())
        for (i in 1 until valid.size) {
            val cur = valid[i]
            val last = merged.last()
            if (cur.start <= last.end) {
                merged[merged.lastIndex] = MinuteInterval(last.start, max(last.end, cur.end))
            } else {
                merged += cur
            }
        }
        return merged
    }

    fun subtractIntervals(base: List<MinuteInterval>, busy: List<MinuteInterval>): List<MinuteInterval> {
        if (base.isEmpty()) return emptyList()
        val sortedBusy = mergeIntervals(busy)
        if (sortedBusy.isEmpty()) return mergeIntervals(base)
        var current = mergeIntervals(base)
        for (b in sortedBusy) {
            val next = mutableListOf<MinuteInterval>()
            for (w in current) {
                if (!w.overlaps(b)) {
                    next += w
                } else {
                    if (b.start > w.start) next += MinuteInterval(w.start, b.start)
                    if (b.end < w.end) next += MinuteInterval(b.end, w.end)
                }
            }
            current = next
            if (current.isEmpty()) break
        }
        return current
    }

    /**
     * 1.7.5.1 (P1 — Recorrências): Projeta virtualmente sob demanda blocos recorrentes para [date]
     * sem materializar centenas de registros no banco e sem duplicar o template na data original
     * ou instâncias já materializadas.
     */
    fun effectiveBlocksForDate(s: GameState, date: String): List<TimeBlock> {
        val materialized = s.timeBlocks.filter { it.date == date }
        if (s.recurrenceRules.isEmpty()) return materialized
        val blocksById = s.timeBlocks.associateBy { it.id }
        val projected = mutableListOf<TimeBlock>()
        for (rule in s.recurrenceRules) {
            if (!rule.active || rule.entityType != "block") continue
            val template = blocksById[rule.templateId]
                ?: s.timeBlocks.firstOrNull { it.recurrenceId == rule.id }
                ?: continue
            if (date !in expandRecurrenceOccurrences(rule, date, date)) continue
            val alreadyMaterialized = materialized.any { b ->
                b.id == template.id ||
                    b.recurrenceId == rule.id ||
                    (b.title == template.title && b.startTime == template.startTime && b.endTime == template.endTime)
            }
            if (!alreadyMaterialized) {
                projected += template.copy(
                    id = "${template.id}@rec:$date",
                    date = date,
                    recurrenceId = rule.id,
                    sourcePlanId = ""
                )
            }
        }
        return materialized + projected
    }

    /**
     * 1.7.5.1 (P1 — Recorrências): Projeta virtualmente sob demanda compromissos recorrentes para [date]
     * sem duplicar o compromisso template na data original.
     */
    fun effectiveEventsForDate(s: GameState, date: String): List<AgendaEvent> {
        val materialized = s.agendaEvents.filter { !it.archived && it.date == date }
        if (s.recurrenceRules.isEmpty()) return materialized
        val eventsById = s.agendaEvents.associateBy { it.id }
        val projected = mutableListOf<AgendaEvent>()
        for (rule in s.recurrenceRules) {
            if (!rule.active || rule.entityType != "event") continue
            val template = eventsById[rule.templateId]
                ?: s.agendaEvents.firstOrNull { it.recurrenceId == rule.id }
                ?: continue
            if (template.archived) continue
            if (date !in expandRecurrenceOccurrences(rule, date, date)) continue
            val alreadyMaterialized = materialized.any { ev ->
                ev.id == template.id ||
                    ev.recurrenceId == rule.id ||
                    (ev.title == template.title && ev.startTime == template.startTime && ev.endTime == template.endTime)
            }
            if (!alreadyMaterialized) {
                projected += template.copy(
                    id = "${template.id}@rec:$date",
                    date = date,
                    recurrenceId = rule.id,
                    completed = false
                )
            }
        }
        return materialized + projected
    }

    /**
     * Retorna apenas as janelas AUTORIZADAS pelo usuário para [date], respeitando dias de descanso
     * e exceções de disponibilidade. Nunca presume 24 horas livres.
     */
    fun authorizedWindowsForDate(s: GameState, date: String): List<MinuteInterval> {
        if (date in s.restDates) return emptyList()
        val localDate = runCatching { LocalDate.parse(date) }.getOrNull() ?: return emptyList()
        val exceptions = s.availabilityExceptions.filter { it.date == date }
        if (exceptions.any { it.blocked }) return emptyList()
        val overrideWindows = exceptions.filter { !it.blocked }.mapNotNull { ex ->
            val st = parseTimeToMinutes(ex.startTime)
            val en = parseTimeToMinutes(ex.endTime)
            if (st != null && en != null && en > st) MinuteInterval(st, en) else null
        }
        if (overrideWindows.isNotEmpty()) return mergeIntervals(overrideWindows)

        val dow = localDate.dayOfWeek.value
        val dayWindows = s.availabilityWindows
            .filter { it.enabled && it.dayOfWeek == dow }
            .mapNotNull { w ->
                val st = parseTimeToMinutes(w.startTime)
                val en = parseTimeToMinutes(w.endTime)
                if (st != null && en != null && en > st) MinuteInterval(st, en) else null
            }
        return mergeIntervals(dayWindows)
    }

    /**
     * Retorna intervalos ocupados por compromissos fixos (incluindo ocorrências recorrentes)
     * e blocos de horário (incluindo ocorrências recorrentes) em [date].
     */
    fun busyIntervalsForDate(
        s: GameState,
        date: String,
        includeUnpinnedBlocks: Boolean = true,
        ignorePlanId: String = "",
        ignoreBlockId: String = ""
    ): List<MinuteInterval> {
        val busy = mutableListOf<MinuteInterval>()
        for (ev in effectiveEventsForDate(s, date)) {
            val st = parseTimeToMinutes(ev.startTime)
            val en = parseTimeToMinutes(ev.endTime)
            if (st != null && en != null && en > st) busy += MinuteInterval(st, en)
        }
        for (blk in effectiveBlocksForDate(s, date)) {
            if (ignoreBlockId.isNotBlank() && blk.id == ignoreBlockId) continue
            if (ignorePlanId.isNotBlank() && blk.sourcePlanId == ignorePlanId && !blk.pinned && !blk.modifiedManually) continue
            val isRecurringOccurrence = blk.recurrenceId.isNotBlank() ||
                s.recurrenceRules.any { it.active && it.entityType == "block" && it.templateId == blk.id }
            if (!includeUnpinnedBlocks && !blk.pinned && !isRecurringOccurrence) continue
            val st = parseTimeToMinutes(blk.startTime)
            val en = parseTimeToMinutes(blk.endTime)
            if (st != null && en != null && en > st) busy += MinuteInterval(st, en)
        }
        return mergeIntervals(busy)
    }

    fun freeIntervalsForDate(
        s: GameState,
        date: String,
        includeUnpinnedBlocks: Boolean = true,
        ignorePlanId: String = "",
        ignoreBlockId: String = "",
        minStartMinute: Int = 0
    ): List<MinuteInterval> {
        val auth = authorizedWindowsForDate(s, date)
        if (auth.isEmpty()) return emptyList()
        val busy = busyIntervalsForDate(s, date, includeUnpinnedBlocks, ignorePlanId, ignoreBlockId)
        val free = subtractIntervals(auth, busy)
        if (minStartMinute <= 0) return free
        return free.mapNotNull { iv ->
            val st = max(iv.start, minStartMinute)
            if (iv.end > st) MinuteInterval(st, iv.end) else null
        }
    }

    fun hasConfiguredAvailability(s: GameState): Boolean =
        s.availabilityWindows.any { it.enabled && validTimeInterval(it.startTime, it.endTime) } ||
            s.availabilityExceptions.any { !it.blocked && validTimeInterval(it.startTime, it.endTime) }

    // =========================================================================
    // 2. GRAFO DE DEPENDÊNCIAS, CICLOS, ORDENAÇÃO TOPOLÓGICA E ESTADOS
    // =========================================================================

    fun hasDependencyCycle(dependencies: List<TaskDependency>): Boolean {
        val active = dependencies.filter { !it.overridden }
        if (active.any { it.taskId == it.dependsOnTaskId }) return true
        val adj = mutableMapOf<String, MutableList<String>>()
        val inDegree = mutableMapOf<String, Int>()
        for (dep in active) {
            inDegree.putIfAbsent(dep.dependsOnTaskId, 0)
            inDegree.putIfAbsent(dep.taskId, 0)
            adj.getOrPut(dep.dependsOnTaskId) { mutableListOf() }.add(dep.taskId)
            inDegree[dep.taskId] = (inDegree[dep.taskId] ?: 0) + 1
        }
        val queue = ArrayDeque<String>()
        inDegree.forEach { (node, deg) -> if (deg == 0) queue.add(node) }
        var visited = 0
        while (queue.isNotEmpty()) {
            val u = queue.removeFirst()
            visited++
            for (v in adj[u].orEmpty()) {
                val nd = (inDegree[v] ?: 0) - 1
                inDegree[v] = nd
                if (nd == 0) queue.add(v)
            }
        }
        return visited != inDegree.size
    }

    fun wouldCreateCycle(
        dependencies: List<TaskDependency>,
        taskId: String,
        dependsOnTaskId: String
    ): Boolean {
        if (taskId.isBlank() || dependsOnTaskId.isBlank() || taskId == dependsOnTaskId) return true
        val candidate = dependencies + TaskDependency(id = "probe", taskId = taskId, dependsOnTaskId = dependsOnTaskId)
        return hasDependencyCycle(candidate)
    }

    fun blockingPrerequisites(
        taskId: String,
        tasksById: Map<String, PathTask>,
        dependencies: List<TaskDependency>
    ): List<PathTask> = dependencies
        .filter { it.taskId == taskId && !it.overridden }
        .mapNotNull { tasksById[it.dependsOnTaskId] }
        .filter { !it.completed && !it.archived }

    fun taskWorkflowState(
        task: PathTask,
        tasksById: Map<String, PathTask>,
        dependencies: List<TaskDependency>,
        todayDate: String = today()
    ): TaskWorkflowState {
        if (task.archived) return TaskWorkflowState.ARCHIVED
        if (task.completed) return TaskWorkflowState.COMPLETED
        val blocked = blockingPrerequisites(task.id, tasksById, dependencies).isNotEmpty()
        if (blocked) return TaskWorkflowState.BLOCKED
        if (task.deadline.isNotBlank() && task.deadline < todayDate) return TaskWorkflowState.OVERDUE
        if (task.workflowStatus == 1) return TaskWorkflowState.IN_PROGRESS
        return TaskWorkflowState.AVAILABLE
    }

    fun topologicalSortTasks(
        tasks: List<PathTask>,
        dependencies: List<TaskDependency>
    ): Pair<List<PathTask>, Boolean> {
        val byId = tasks.associateBy { it.id }
        val adj = mutableMapOf<String, MutableList<String>>()
        val inDegree = mutableMapOf<String, Int>()
        tasks.forEach { inDegree[it.id] = 0 }
        for (dep in dependencies) {
            if (dep.overridden) continue
            if (dep.taskId !in byId || dep.dependsOnTaskId !in byId) continue
            if (dep.taskId == dep.dependsOnTaskId) return tasks to true
            adj.getOrPut(dep.dependsOnTaskId) { mutableListOf() }.add(dep.taskId)
            inDegree[dep.taskId] = (inDegree[dep.taskId] ?: 0) + 1
        }
        val comparator = compareBy<PathTask>(
            { it.deadline.ifBlank { "9999-12-31" } },
            { -it.priority },
            { it.position },
            { it.id }
        )
        val ready = java.util.PriorityQueue(comparator)
        tasks.forEach { t -> if ((inDegree[t.id] ?: 0) == 0) ready.add(t) }
        val result = mutableListOf<PathTask>()
        while (ready.isNotEmpty()) {
            val u = ready.poll() ?: break
            result += u
            for (vId in adj[u.id].orEmpty()) {
                val nd = (inDegree[vId] ?: 0) - 1
                inDegree[vId] = nd
                if (nd == 0) byId[vId]?.let { ready.add(it) }
            }
        }
        val hasCycle = result.size != tasks.size
        if (hasCycle) {
            val seen = result.map { it.id }.toSet()
            result += tasks.filter { it.id !in seen }.sortedWith(comparator)
        }
        return result to hasCycle
    }

    // =========================================================================
    // 3. CAMINHO CRÍTICO (CPM) — CAMINHOS 3.0
    // =========================================================================

    fun computeCriticalPath(
        path: PersonalPath,
        state: GameState,
        todayDate: String = today()
    ): CriticalPathResult {
        val tasks = state.pathTasks.filter { it.pathId == path.id && !it.archived }
        if (tasks.isEmpty()) {
            return CriticalPathResult(
                pathId = path.id,
                hasCycle = false,
                totalCriticalMinutes = 0,
                nodes = emptyList(),
                criticalSequence = emptyList(),
                missingEstimateTaskIds = emptyList(),
                effortProjectedDate = null,
                availabilityProjectedDate = null,
                deadlineRisk = false,
                premises = listOf("Nenhuma tarefa ativa neste Caminho.")
            )
        }

        val taskIds = tasks.map { it.id }.toSet()
        val deps = state.taskDependencies.filter { !it.overridden && it.taskId in taskIds && it.dependsOnTaskId in taskIds }
        val (ordered, hasCycle) = topologicalSortTasks(tasks, deps)
        val preds = mutableMapOf<String, MutableList<String>>()
        val succs = mutableMapOf<String, MutableList<String>>()
        for (d in deps) {
            preds.getOrPut(d.taskId) { mutableListOf() }.add(d.dependsOnTaskId)
            succs.getOrPut(d.dependsOnTaskId) { mutableListOf() }.add(d.taskId)
        }

        val missingEstimateIds = mutableListOf<String>()
        val durations = mutableMapOf<String, Int>()
        for (t in ordered) {
            if (t.completed) {
                durations[t.id] = 0
            } else if (t.estimateMinutes > 0) {
                durations[t.id] = t.estimateMinutes
            } else {
                missingEstimateIds += t.id
                durations[t.id] = DEFAULT_UNESTIMATED_CPM_FALLBACK_MIN
            }
        }

        val es = mutableMapOf<String, Int>()
        val ef = mutableMapOf<String, Int>()
        for (t in ordered) {
            val start = preds[t.id].orEmpty().maxOfOrNull { ef[it] ?: 0 } ?: 0
            es[t.id] = start
            ef[t.id] = start + (durations[t.id] ?: 0)
        }
        val projectDuration = ef.values.maxOrNull() ?: 0

        val ls = mutableMapOf<String, Int>()
        val lf = mutableMapOf<String, Int>()
        for (t in ordered.asReversed()) {
            val finish = succs[t.id].orEmpty().minOfOrNull { ls[it] ?: projectDuration } ?: projectDuration
            lf[t.id] = finish
            ls[t.id] = finish - (durations[t.id] ?: 0)
        }

        val config = resolvedPlanningConfig(state)
        val dailyEffortBaseline = config.dailyLimitMinutes.coerceIn(60, 480)
        val baseDate = runCatching { LocalDate.parse(todayDate) }.getOrElse { LocalDate.now() }

        val nodes = ordered.map { t ->
            val tEs = es[t.id] ?: 0
            val tEf = ef[t.id] ?: 0
            val tLs = ls[t.id] ?: 0
            val tLf = lf[t.id] ?: 0
            val slack = (tLs - tEs).coerceAtLeast(0)
            val startDayOffset = tEs / dailyEffortBaseline
            val endDayOffset = if (tEf <= 0) 0 else (tEf - 1) / dailyEffortBaseline
            CriticalPathNode(
                task = t,
                durationMinutes = durations[t.id] ?: 0,
                estimatedMissing = t.id in missingEstimateIds,
                earlyStart = tEs,
                earlyFinish = tEf,
                lateStart = tLs,
                lateFinish = tLf,
                slackMinutes = slack,
                isCritical = slack == 0 && !t.completed,
                earliestDate = baseDate.plusDays(startDayOffset.toLong()).toString(),
                finishDate = baseDate.plusDays(endDayOffset.toLong()).toString()
            )
        }

        val criticalSeq = nodes.filter { it.isCritical }
        val effortDays = if (projectDuration <= 0) 0 else ceil(projectDuration.toDouble() / dailyEffortBaseline).toInt()
        val effortDate = if (projectDuration <= 0) todayDate else baseDate.plusDays(max(0, effortDays - 1).toLong()).toString()

        // Projeção baseada na disponibilidade configurada real (caminhando dia a dia até 365 dias)
        val availabilityDate: String? = if (projectDuration <= 0) {
            todayDate
        } else if (!hasConfiguredAvailability(state)) {
            null
        } else {
            var remaining = projectDuration
            var found: String? = null
            for (offset in 0 until 365) {
                val d = baseDate.plusDays(offset.toLong()).toString()
                val free = freeIntervalsForDate(state, d).sumOf { it.minutes }
                val usable = min(free, config.dailyLimitMinutes)
                if (usable > 0) {
                    remaining -= usable
                    if (remaining <= 0) {
                        found = d
                        break
                    }
                }
            }
            found
        }

        val premises = mutableListOf<String>()
        premises += "Base de esforço nominal: ${dailyEffortBaseline} min/dia."
        if (missingEstimateIds.isNotEmpty()) {
            premises += "${missingEstimateIds.size} tarefa(s) sem estimativa receberam referência provisória de ${DEFAULT_UNESTIMATED_CPM_FALLBACK_MIN} min para continuidade do grafo."
        }
        if (availabilityDate == null && projectDuration > 0) {
            premises += if (!hasConfiguredAvailability(state)) {
                "Sem janelas de disponibilidade configuradas para projetar data real no calendário."
            } else {
                "Disponibilidade configurada insuficiente nos próximos 365 dias para concluir o caminho crítico."
            }
        }
        if (hasCycle) {
            premises += "Atenção: ciclo de dependências detectado; revise as relações antes de confiar nas folgas."
        }

        val deadlineRisk = path.deadline.isNotBlank() && (
            (availabilityDate != null && availabilityDate > path.deadline) ||
                (availabilityDate == null && effortDate > path.deadline)
            )

        return CriticalPathResult(
            pathId = path.id,
            hasCycle = hasCycle,
            totalCriticalMinutes = projectDuration,
            nodes = nodes,
            criticalSequence = criticalSeq,
            missingEstimateTaskIds = missingEstimateIds,
            effortProjectedDate = effortDate,
            availabilityProjectedDate = availabilityDate,
            deadlineRisk = deadlineRisk,
            premises = premises
        )
    }

    // =========================================================================
    // 4. PREFERÊNCIA SUAVE HISTÓRICA (OPCIONAL, COM MÍNIMO DE DADOS)
    // =========================================================================

    fun preferredBucketFromHistory(s: GameState): String? {
        val validSessions = s.focusSessions.filter { it.elapsedSeconds >= 300L && it.startedAt > 0L }
        if (validSessions.size < MIN_HISTORY_SESSIONS_FOR_SOFT_PREF) return null
        val counts = mutableMapOf("morning" to 0, "afternoon" to 0, "evening" to 0)
        val zone = java.time.ZoneId.systemDefault()
        for (sess in validSessions) {
            val hour = java.time.Instant.ofEpochMilli(sess.startedAt).atZone(zone).hour
            val bucket = when {
                hour < 12 -> "morning"
                hour < 18 -> "afternoon"
                else -> "evening"
            }
            counts[bucket] = (counts[bucket] ?: 0) + 1
        }
        return counts.maxByOrNull { it.value }?.takeIf { it.value > 0 }?.key
    }

    private fun intervalBucket(startMinutes: Int): String = when {
        startMinutes < 12 * 60 -> "morning"
        startMinutes < 18 * 60 -> "afternoon"
        else -> "evening"
    }

    // =========================================================================
    // 5. MOTOR ADAPTATIVO DA MESA DE ODIN (DETERMINÍSTICO, O(N log N + D*W))
    // =========================================================================

    private data class WorkCandidate(
        val id: String,
        val title: String,
        val sourceType: String, // task | habit | mimir
        val remainingMinutes: Int,
        val deadline: String,
        val priority: Int,
        val isStudy: Boolean,
        val isCreative: Boolean,
        val isCritical: Boolean,
        val taskId: String = "",
        val pathId: String = "",
        val habitId: String = "",
        val areaId: String = "",
        val category: String = "Foco",
        val color: Int = 1,
        val preferredDay: String = "",
        val score: Double = 0.0,
        val reasonTags: List<String> = emptyList()
    )

    fun generateOdinProposal(
        s: GameState,
        fromDate: String,
        toDate: String,
        strategyId: String = resolvedPlanningConfig(s).strategyId,
        config: PlanningConfig = resolvedPlanningConfig(s),
        includeHabits: Boolean = true,
        includeMimirReviews: Boolean = true,
        preserveExistingBlocks: Boolean = true,
        clock: PlanningClock = PlanningClock(referenceDate = fromDate, referenceMinuteOfDay = 0)
    ): OdinProposal {
        val strategy = odinStrategyById(strategyId)
        val startLocal = runCatching { LocalDate.parse(fromDate) }.getOrElse { LocalDate.now() }
        val endLocal = runCatching { LocalDate.parse(toDate) }.getOrElse { startLocal }
        val effectiveEnd = if (endLocal.isBefore(startLocal)) startLocal else endLocal
        val totalDays = (ChronoUnit.DAYS.between(startLocal, effectiveEnd).toInt() + 1).coerceIn(1, 60)
        val dates = (0 until totalDays).map { startLocal.plusDays(it.toLong()).toString() }

        val conflicts = mutableListOf<String>()
        val unscheduled = mutableListOf<UnscheduledItem>()
        if (hasDependencyCycle(s.taskDependencies)) {
            conflicts += "Ciclo detectado nas dependências de tarefas. Quebre o ciclo em Caminhos 3.0 para liberação plena."
        }

        val dailyCapacityCap = max(
            config.minBlockMinutes,
            (config.dailyLimitMinutes * strategy.targetDailyLoadRatio).toInt()
        )
        val buffer = max(config.bufferMinutes, strategy.preferredBufferMinutes)
        val minSlice = config.minBlockMinutes.coerceIn(15, 60)
        val maxSlice = min(config.maxBlockMinutes.coerceAtLeast(minSlice), strategy.preferredMaxBlockMinutes.coerceAtLeast(minSlice))

        // Disponibilidade diária real (nunca assume 24h) + filtro de horários já transcorridos no dia de referência
        val freeByDate = linkedMapOf<String, MutableList<MinuteInterval>>()
        val remainingDailyCap = mutableMapOf<String, Int>()
        var totalAvailableMinutes = 0
        for (d in dates) {
            val minStart = if (d == clock.referenceDate && clock.referenceMinuteOfDay > 0) clock.referenceMinuteOfDay else 0
            val intervals = freeIntervalsForDate(
                s = s,
                date = d,
                includeUnpinnedBlocks = preserveExistingBlocks,
                minStartMinute = minStart
            ).toMutableList()
            val dayFree = intervals.sumOf { it.minutes }
            freeByDate[d] = intervals
            val existingBlocksMin = if (preserveExistingBlocks) {
                effectiveBlocksForDate(s, d).sumOf { blockDurationMinutes(it) }
            } else {
                effectiveBlocksForDate(s, d).filter { it.pinned || it.recurrenceId.isNotBlank() }.sumOf { blockDurationMinutes(it) }
            }
            val cap = min(dayFree, (dailyCapacityCap - existingBlocksMin).coerceAtLeast(0))
            remainingDailyCap[d] = cap
            totalAvailableMinutes += cap
        }
        if (totalAvailableMinutes == 0) {
            conflicts += "Nenhuma janela livre disponível entre $fromDate e ${dates.last()}. Configure sua disponibilidade ou revise compromissos fixos."
        }

        // Pré-cálculo de tarefas críticas nos caminhos ativos
        val criticalTaskIds = mutableSetOf<String>()
        val activePathsById = s.paths.filter { it.state !in 3..5 }.associateBy { it.id }
        for (p in activePathsById.values) {
            val cpm = computeCriticalPath(p, s, fromDate)
            criticalTaskIds += cpm.criticalSequence.map { it.task.id }
        }

        // Mapeamento de minutos já alocados em blocos existentes (para evitar duplicidade e verificar cobertura integral)
        val existingAllocatedByTask = mutableMapOf<String, Int>()
        val existingLastSlotByTask = mutableMapOf<String, Pair<String, Int>>()
        val existingHabitBlocks = mutableSetOf<String>() // "habitId@date"
        for (d in dates) {
            for (b in effectiveBlocksForDate(s, d)) {
                if (preserveExistingBlocks || b.pinned || b.recurrenceId.isNotBlank()) {
                    if (b.taskId.isNotBlank()) {
                        existingAllocatedByTask[b.taskId] = (existingAllocatedByTask[b.taskId] ?: 0) + blockDurationMinutes(b)
                        val endM = parseTimeToMinutes(b.endTime)
                        if (endM != null) {
                            val prev = existingLastSlotByTask[b.taskId]
                            if (prev == null || b.date > prev.first || (b.date == prev.first && endM > prev.second)) {
                                existingLastSlotByTask[b.taskId] = b.date to endM
                            }
                        }
                    }
                    if (b.habitId.isNotBlank()) {
                        existingHabitBlocks += "${b.habitId}@${b.date}"
                    }
                }
            }
        }
        // Também considera blocos existentes fora do intervalo [fromDate..toDate] mas anteriores ou iguais a toDate para tarefas já integralmente alocadas
        for (b in s.timeBlocks) {
            if (b.date !in dates && b.date <= dates.last() && (preserveExistingBlocks || b.pinned)) {
                if (b.taskId.isNotBlank()) {
                    existingAllocatedByTask[b.taskId] = (existingAllocatedByTask[b.taskId] ?: 0) + blockDurationMinutes(b)
                    val endM = parseTimeToMinutes(b.endTime)
                    if (endM != null) {
                        val prev = existingLastSlotByTask[b.taskId]
                        if (prev == null || b.date > prev.first || (b.date == prev.first && endM > prev.second)) {
                            existingLastSlotByTask[b.taskId] = b.date to endM
                        }
                    }
                }
            }
        }

        val allTasksById = s.pathTasks.associateBy { it.id }
        val activeTasks = s.pathTasks.filter { t ->
            !t.completed && !t.archived && (t.pathId.isBlank() || t.pathId in activePathsById)
        }
        val (topoTasks, _) = topologicalSortTasks(activeTasks, s.taskDependencies)
        val topoRank = topoTasks.withIndex().associate { it.value.id to it.index }

        val historyBucket = if (config.useHistorySoftPreference) preferredBucketFromHistory(s) else null
        val targetStudyBucket = historyBucket ?: config.preferredStudyPeriod.takeIf { it != "any" }

        // Mapa explícito do estado de agendamento de cada tarefa
        val taskScheduleStates = mutableMapOf<String, TaskScheduleState>()
        s.pathTasks.forEach { t ->
            if (t.completed) {
                taskScheduleStates[t.id] = TaskScheduleState.COMPLETED
            }
        }

        // Mapa de conclusão projetada (date, endMinute) para respeitar finish-to-start!
        // 1.7.5.1 (P1 — Dependências): Somente tarefas INTEGRALMENTE agendadas entram em scheduledCompletion!
        val scheduledCompletion = mutableMapOf<String, Pair<String, Int>>()

        // Montar candidatos (tarefas, hábitos de tempo, revisões Mímir)
        val taskCandidates = mutableListOf<WorkCandidate>()
        for (t in topoTasks) {
            val alreadyAlloc = existingAllocatedByTask[t.id] ?: 0
            val baseEst = if (t.estimateMinutes > 0) t.estimateMinutes else 30
            val needed = baseEst - alreadyAlloc
            if (needed <= 0) {
                taskScheduleStates[t.id] = TaskScheduleState.FULLY_SCHEDULED
                existingLastSlotByTask[t.id]?.let { scheduledCompletion[t.id] = it }
                continue
            }

            val path = activePathsById[t.pathId]
            val category = path?.category?.ifBlank { "Projeto" } ?: "Tarefa"
            val isStudy = category.contains("Estud", true) || category.contains("Leitur", true) || path?.attribute == 0
            val isCreative = category.contains("Criativ", true) || category.contains("Tecnolog", true) || path?.attribute == 4
            val isCritical = t.id in criticalTaskIds

            var score = 10.0 + (t.priority + 1) * 12.0 * strategy.priorityWeight
            val reasons = mutableListOf<String>()

            if (t.deadline.isNotBlank()) {
                val daysToDeadline = ChronoUnit.DAYS.between(startLocal, runCatching { LocalDate.parse(t.deadline) }.getOrElse { startLocal }).toInt()
                when {
                    daysToDeadline < 0 -> {
                        score += 65.0 * strategy.deadlineWeight
                        reasons += "prazo vencido (${t.deadline})"
                    }
                    daysToDeadline <= 2 -> {
                        score += 48.0 * strategy.deadlineWeight
                        reasons += "prazo imediato (${t.deadline})"
                    }
                    daysToDeadline <= 7 -> {
                        score += 28.0 * strategy.deadlineWeight
                        reasons += "prazo na semana (${t.deadline})"
                    }
                    else -> {
                        score += max(4.0, 20.0 - daysToDeadline * 0.4) * strategy.deadlineWeight
                    }
                }
            }
            if (isCritical) {
                score += 34.0 * strategy.criticalPathWeight
                reasons += "caminho crítico"
            }
            if (isStudy) {
                score += 18.0 * strategy.studyFocusWeight
                if (strategy.id == "scholar") reasons += "prioridade de estudo (${strategy.name})"
            }
            if (isCreative) {
                score += 18.0 * strategy.creativeBlockWeight
                if (strategy.id == "artificer") reasons += "bloco de construção (${strategy.name})"
            }
            if (t.important && t.urgent) {
                score += 24.0
                reasons += "importante e urgente"
            } else if (t.important) {
                score += 12.0
            }
            if (t.estimateMinutes <= 0) {
                reasons += "duração padrão 30 min (sem estimativa)"
            }
            if (reasons.isEmpty()) reasons += "estratégia ${strategy.name}"

            taskCandidates += WorkCandidate(
                id = "cand-task-${t.id}",
                title = t.title,
                sourceType = "task",
                remainingMinutes = needed,
                deadline = t.deadline,
                priority = t.priority,
                isStudy = isStudy,
                isCreative = isCreative,
                isCritical = isCritical,
                taskId = t.id,
                pathId = t.pathId,
                areaId = t.areaId.ifBlank { path?.areaId.orEmpty() },
                category = category,
                color = path?.color ?: 1,
                score = score,
                reasonTags = reasons
            )
        }

        // Hábitos programados (type == 2 -> minutos, ou rotina essencial)
        val habitCandidates = mutableListOf<WorkCandidate>()
        if (includeHabits) {
            val activeHabits = s.habits.filter { !it.deleted && !it.archived && !it.paused }
            for (d in dates) {
                for (h in activeHabits) {
                    if (!s.planned(h, d)) continue
                    if (s.record(h.id, d)?.complete == true) continue
                    if ("${h.id}@$d" in existingHabitBlocks) continue
                    val dur = if (h.type == 2) h.target.coerceIn(15, 120) else 25
                    val isStudy = h.attribute == 0 || h.category.contains("Estud", true)
                    habitCandidates += WorkCandidate(
                        id = "cand-habit-${h.id}-$d",
                        title = h.name,
                        sourceType = "habit",
                        remainingMinutes = dur,
                        deadline = d,
                        priority = 2,
                        isStudy = isStudy,
                        isCreative = h.attribute == 4,
                        isCritical = false,
                        habitId = h.id,
                        areaId = h.areaId,
                        category = h.category.ifBlank { "Hábito" },
                        color = h.color,
                        preferredDay = d,
                        score = 38.0 * strategy.habitStabilityWeight + if (isStudy) 10.0 * strategy.studyFocusWeight else 0.0,
                        reasonTags = listOf("hábito programado em $d", "constância (${strategy.name})")
                    )
                }
            }
        }

        // Revisões pendentes de Mímir (agrupadas num bloco de estudo se houver cartões devidos)
        val mimirCandidates = mutableListOf<WorkCandidate>()
        if (includeMimirReviews) {
            val dueCards = dueFlashcards(s, fromDate)
            if (dueCards.isNotEmpty()) {
                val estMin = (dueCards.size * 2).coerceIn(15, 45)
                mimirCandidates += WorkCandidate(
                    id = "cand-mimir-$fromDate",
                    title = "Revisão Mímir (${dueCards.size} runas)",
                    sourceType = "mimir",
                    remainingMinutes = estMin,
                    deadline = fromDate,
                    priority = 2,
                    isStudy = true,
                    isCreative = false,
                    isCritical = false,
                    category = "Mímir",
                    color = 2,
                    preferredDay = fromDate,
                    score = 32.0 * strategy.studyFocusWeight + 12.0,
                    reasonTags = listOf("${dueCards.size} flashcard(s) vencidos em Mímir", "revisão espaçada")
                )
            }
        }

        val suggestions = mutableListOf<PlannedSuggestion>()
        var suggestionSeq = 0
        val lastCategoryByDate = mutableMapOf<String, String>()

        fun allocateCandidate(cand: WorkCandidate, earliestDate: String, earliestMinOnDate: Int): Int {
            var remaining = cand.remainingMinutes
            val plannedSlices = mutableListOf<Triple<String, Int, Int>>() // date, start, end

            // Ordenar datas válidas (preferindo preferredDay se houver; respeitando earliestDate)
            val candidateDates = dates.filter { it >= earliestDate }.let { list ->
                if (cand.preferredDay.isNotBlank() && cand.preferredDay in list) {
                    listOf(cand.preferredDay)
                } else list
            }

            for (d in candidateDates) {
                if (remaining <= 0) break
                var dayCap = remainingDailyCap[d] ?: 0
                if (dayCap < minSlice && remaining >= minSlice) continue
                val intervals = freeByDate[d] ?: continue
                if (intervals.isEmpty()) continue

                // Ordena janelas do dia: preferência suave por período de estudo/histórico + agrupamento
                val preferredBucket = if (cand.isStudy) targetStudyBucket else historyBucket
                val orderedIdx = intervals.indices.sortedWith(
                    compareByDescending<Int> { idx ->
                        var wScore = 0
                        val iv = intervals[idx]
                        if (preferredBucket != null && intervalBucket(iv.start) == preferredBucket) wScore += 3
                        if (config.groupSimilar && lastCategoryByDate[d] == cand.category) wScore += 2
                        if (iv.minutes >= min(remaining, maxSlice)) wScore += 1
                        wScore
                    }.thenBy { intervals[it].start }
                )

                var consumedInDay = false
                for (idx in orderedIdx) {
                    if (remaining <= 0 || dayCap <= 0) break
                    val currentIntervals = freeByDate[d] ?: break
                    if (idx >= currentIntervals.size) continue
                    val iv = currentIntervals[idx]
                    val minStart = if (d == earliestDate) max(iv.start, earliestMinOnDate) else iv.start
                    val avail = iv.end - minStart
                    if (avail <= 0) continue

                    val desired = min(remaining, min(maxSlice, dayCap))
                    if (avail < minSlice && desired >= minSlice && avail < remaining) continue
                    val take = min(avail, desired)
                    if (take < 10) continue

                    val slotStart = minStart
                    val slotEnd = slotStart + take
                    plannedSlices += Triple(d, slotStart, slotEnd)

                    // Atualiza intervalos livres do dia descontando [slotStart, slotEnd + buffer]
                    val busyWithBuffer = MinuteInterval(slotStart, min(24 * 60, slotEnd + buffer))
                    freeByDate[d] = subtractIntervals(currentIntervals, listOf(busyWithBuffer)).toMutableList()
                    dayCap = (dayCap - take).coerceAtLeast(0)
                    remainingDailyCap[d] = dayCap
                    remaining -= take
                    lastCategoryByDate[d] = cand.category
                    consumedInDay = true
                    break // evita múltiplas fatias da mesma tarefa no mesmo passe antes de avançar
                }
                if (consumedInDay && remaining > 0 && cand.sourceType != "task") break
            }

            if (plannedSlices.isNotEmpty()) {
                val totalParts = plannedSlices.size
                var latestSliceEnd: Pair<String, Int>? = existingLastSlotByTask[cand.taskId]
                plannedSlices.forEachIndexed { idx, (d, st, en) ->
                    suggestionSeq++
                    val partNote = if (totalParts > 1) " (parte ${idx + 1}/$totalParts)" else ""
                    val reasonText = buildString {
                        append(cand.reasonTags.joinToString(" · "))
                        if (historyBucket != null && intervalBucket(st) == historyBucket) {
                            append(" · janela alinhada ao seu histórico real")
                        }
                        if (totalParts > 1) append(" · dividido para respeitar janelas livres")
                    }
                    suggestions += PlannedSuggestion(
                        id = "odin-${strategy.id}-$d-${formatMinutesToTime(st)}-$suggestionSeq",
                        title = "${cand.title}$partNote",
                        date = d,
                        startTime = formatMinutesToTime(st),
                        endTime = formatMinutesToTime(en),
                        minutes = en - st,
                        sourceType = cand.sourceType,
                        taskId = cand.taskId,
                        pathId = cand.pathId,
                        habitId = cand.habitId,
                        areaId = cand.areaId,
                        category = cand.category,
                        color = cand.color,
                        reason = reasonText,
                        splitIndex = idx + 1,
                        splitTotal = totalParts
                    )
                    val curEnd = latestSliceEnd
                    if (curEnd == null || d > curEnd.first || (d == curEnd.first && en > curEnd.second)) {
                        latestSliceEnd = d to en
                    }
                }
                if (cand.taskId.isNotBlank()) {
                    if (remaining == 0) {
                        // 1.7.5.1 (P1): Somente tarefas INTEGRALMENTE agendadas liberam dependentes finish-to-start!
                        taskScheduleStates[cand.taskId] = TaskScheduleState.FULLY_SCHEDULED
                        latestSliceEnd?.let { scheduledCompletion[cand.taskId] = it }
                    } else {
                        taskScheduleStates[cand.taskId] = TaskScheduleState.PARTIALLY_SCHEDULED
                    }
                }
            } else if (cand.taskId.isNotBlank()) {
                val hadExisting = (existingAllocatedByTask[cand.taskId] ?: 0) > 0
                taskScheduleStates[cand.taskId] = if (hadExisting) TaskScheduleState.PARTIALLY_SCHEDULED else TaskScheduleState.UNSCHEDULED
            }
            return remaining
        }

        // 1) Se estratégia Guardião, hábitos vão primeiro para garantir estabilidade; caso contrário, mistura por pontuação
        val orderedTasks = taskCandidates.sortedWith(
            compareBy<WorkCandidate> { topoRank[it.taskId] ?: Int.MAX_VALUE }
                .thenByDescending { it.score }
                .thenBy { it.id }
        )

        val nonTaskQueue = (habitCandidates + mimirCandidates).sortedWith(
            compareByDescending<WorkCandidate> { it.score }.thenBy { it.preferredDay }.thenBy { it.id }
        )

        if (strategy.id == "guardian") {
            for (c in nonTaskQueue) {
                val rem = allocateCandidate(c, c.preferredDay.ifBlank { fromDate }, 0)
                if (rem > 0) {
                    unscheduled += UnscheduledItem(c.id, c.title, c.sourceType, rem, "Sem janela livre suficiente em ${c.preferredDay.ifBlank { fromDate }}.", c.deadline)
                }
            }
        }

        // Alocar tarefas respeitando estritamente pré-requisitos finish-to-start
        val readyQueue = orderedTasks.toMutableList()
        val taskCandById = taskCandidates.associateBy { it.taskId }
        val activeDepEdges = s.taskDependencies.filter { !it.overridden && it.taskId in taskCandById && it.dependsOnTaskId in taskCandById }
        val inDeg = mutableMapOf<String, Int>()
        val adj = mutableMapOf<String, MutableList<String>>()
        taskCandById.keys.forEach { inDeg[it] = 0 }
        for (e in activeDepEdges) {
            adj.getOrPut(e.dependsOnTaskId) { mutableListOf() }.add(e.taskId)
            inDeg[e.taskId] = (inDeg[e.taskId] ?: 0) + 1
        }
        val pq = java.util.PriorityQueue(compareByDescending<WorkCandidate> { it.score }.thenBy { it.id })
        taskCandById.forEach { (id, c) -> if ((inDeg[id] ?: 0) == 0) pq.add(c) }
        val scheduledTaskIds = mutableSetOf<String>()

        while (pq.isNotEmpty()) {
            val c = pq.poll() ?: break
            scheduledTaskIds += c.taskId

            // Verifica pré-requisitos pendentes (não concluídos)
            val unmetPrereqs = blockingPrerequisites(c.taskId, allTasksById, s.taskDependencies)
            var blockedByIncompletePrereq = false
            var earliestDate = fromDate
            var earliestMin = 0
            for (pre in unmetPrereqs) {
                val preState = taskScheduleStates[pre.id] ?: TaskScheduleState.UNSCHEDULED
                val comp = scheduledCompletion[pre.id]
                if ((preState != TaskScheduleState.FULLY_SCHEDULED && preState != TaskScheduleState.COMPLETED) || comp == null) {
                    blockedByIncompletePrereq = true
                    break
                } else {
                    if (comp.first > earliestDate || (comp.first == earliestDate && comp.second + buffer > earliestMin)) {
                        earliestDate = comp.first
                        earliestMin = comp.second + buffer
                    }
                }
            }

            if (blockedByIncompletePrereq) {
                taskScheduleStates[c.taskId] = TaskScheduleState.UNSCHEDULED
                val partialPrereqs = unmetPrereqs.filter { taskScheduleStates[it.id] == TaskScheduleState.PARTIALLY_SCHEDULED }
                val unscheduledPrereqs = unmetPrereqs.filter { scheduledCompletion[it.id] == null && taskScheduleStates[it.id] != TaskScheduleState.PARTIALLY_SCHEDULED }
                val reasonMsg = when {
                    partialPrereqs.isNotEmpty() && unscheduledPrereqs.isNotEmpty() ->
                        "Bloqueada por pré-requisito parcialmente agendado (${partialPrereqs.joinToString(", ") { it.title }}) e não agendado (${unscheduledPrereqs.joinToString(", ") { it.title }})."
                    partialPrereqs.isNotEmpty() ->
                        "Bloqueada por pré-requisito parcialmente agendado: ${partialPrereqs.joinToString(", ") { it.title }}."
                    else ->
                        "Bloqueada por pré-requisito não concluído/agendado: ${unscheduledPrereqs.joinToString(", ") { it.title }}."
                }
                unscheduled += UnscheduledItem(
                    id = c.taskId,
                    title = c.title,
                    sourceType = "task",
                    remainingMinutes = c.remainingMinutes,
                    reason = reasonMsg,
                    deadline = c.deadline,
                    scheduleState = TaskScheduleState.UNSCHEDULED
                )
            } else {
                val rem = allocateCandidate(c, earliestDate, earliestMin)
                if (rem > 0) {
                    val stateAfter = taskScheduleStates[c.taskId] ?: TaskScheduleState.UNSCHEDULED
                    val cause = when {
                        totalAvailableMinutes == 0 -> "Sem disponibilidade configurada no período."
                        c.deadline.isNotBlank() && c.deadline < dates.last() -> "Capacidade insuficiente antes do prazo (${c.deadline})."
                        stateAfter == TaskScheduleState.PARTIALLY_SCHEDULED -> "Parcialmente agendada: capacidade livre insuficiente no período selecionado (faltam ${rem} min)."
                        else -> "Capacidade livre insuficiente no período selecionado (faltam ${rem} min)."
                    }
                    unscheduled += UnscheduledItem(
                        id = c.taskId,
                        title = c.title,
                        sourceType = "task",
                        remainingMinutes = rem,
                        reason = cause,
                        deadline = c.deadline,
                        scheduleState = stateAfter
                    )
                }
            }

            for (nextId in adj[c.taskId].orEmpty()) {
                val nd = (inDeg[nextId] ?: 0) - 1
                inDeg[nextId] = nd
                if (nd == 0) taskCandById[nextId]?.let { pq.add(it) }
            }
        }

        // Tarefas eventualmente presas em ciclo
        for (c in readyQueue) {
            if (c.taskId !in scheduledTaskIds) {
                taskScheduleStates[c.taskId] = TaskScheduleState.UNSCHEDULED
                unscheduled += UnscheduledItem(
                    id = c.taskId,
                    title = c.title,
                    sourceType = "task",
                    remainingMinutes = c.remainingMinutes,
                    reason = "Bloqueada por ciclo de dependências.",
                    deadline = c.deadline,
                    scheduleState = TaskScheduleState.UNSCHEDULED
                )
            }
        }

        if (strategy.id != "guardian") {
            for (c in nonTaskQueue) {
                val rem = allocateCandidate(c, c.preferredDay.ifBlank { fromDate }, 0)
                if (rem > 0) {
                    unscheduled += UnscheduledItem(c.id, c.title, c.sourceType, rem, "Sem janela livre suficiente em ${c.preferredDay.ifBlank { fromDate }}.", c.deadline)
                }
            }
        }

        // Detecta prazos impossíveis no período
        for (u in unscheduled) {
            if (u.deadline.isNotBlank() && u.deadline in fromDate..dates.last()) {
                conflicts += "Prazo em risco: \"${u.title}\" (${u.deadline}) não coube integralmente na disponibilidade."
            }
        }

        val sortedSuggestions = suggestions.sortedWith(compareBy<PlannedSuggestion> { it.date }.thenBy { it.startTime })
        return OdinProposal(
            strategy = strategy,
            dateFrom = fromDate,
            dateTo = dates.last(),
            suggestions = sortedSuggestions,
            unscheduled = unscheduled,
            conflicts = conflicts.distinct(),
            totalAvailableMinutes = totalAvailableMinutes,
            totalSuggestedMinutes = sortedSuggestions.sumOf { it.minutes },
            usedHistoryPreference = historyBucket != null,
            preferredHistoryBucket = historyBucket,
            taskScheduleStates = taskScheduleStates
        )
    }

    // =========================================================================
    // 6. SIMULADOR DE CAPACIDADE (5 PERÍODOS, SEM CONTAGEM DUPLICADA, COM RECORRÊNCIAS)
    // =========================================================================

    fun resolveCapacityRange(
        kind: CapacityPeriodKind,
        todayDate: String = today(),
        customFrom: String = todayDate,
        customTo: String = todayDate
    ): Pair<String, String> {
        val base = runCatching { LocalDate.parse(todayDate) }.getOrElse { LocalDate.now() }
        return when (kind) {
            CapacityPeriodKind.TODAY -> base.toString() to base.toString()
            CapacityPeriodKind.NEXT_7_DAYS -> base.toString() to base.plusDays(6).toString()
            CapacityPeriodKind.CALENDAR_WEEK -> {
                val mon = base.with(DayOfWeek.MONDAY)
                mon.toString() to mon.plusDays(6).toString()
            }
            CapacityPeriodKind.NEXT_30_DAYS -> base.toString() to base.plusDays(29).toString()
            CapacityPeriodKind.CUSTOM -> {
                val s = runCatching { LocalDate.parse(customFrom) }.getOrElse { base }
                val e = runCatching { LocalDate.parse(customTo) }.getOrElse { s }
                if (e.isBefore(s)) s.toString() to s.toString() else s.toString() to e.toString()
            }
        }
    }

    fun simulateCapacity(
        s: GameState,
        kind: CapacityPeriodKind,
        todayDate: String = today(),
        customFrom: String = todayDate,
        customTo: String = todayDate
    ): CapacitySimulation {
        val (fromDate, toDate) = resolveCapacityRange(kind, todayDate, customFrom, customTo)
        val start = LocalDate.parse(fromDate)
        val end = LocalDate.parse(toDate)
        val days = (ChronoUnit.DAYS.between(start, end).toInt() + 1).coerceIn(1, 90)
        val dates = (0 until days).map { start.plusDays(it.toLong()).toString() }
        val config = resolvedPlanningConfig(s)

        var configuredAvail = 0
        var fixedEventsMin = 0
        var existingBlocksMin = 0
        var freeCapMin = 0

        // Deduplicação real: minutos já alocados em blocos vinculados (incluindo recorrências) NÃO são somados de novo!
        val allocatedByTask = mutableMapOf<String, Int>()
        val allocatedHabitDates = mutableSetOf<String>()

        for (d in dates) {
            val auth = authorizedWindowsForDate(s, d)
            val authMin = min(auth.sumOf { it.minutes }, config.dailyLimitMinutes)
            configuredAvail += authMin

            val dayEvents = effectiveEventsForDate(s, d)
                .sumOf { ev ->
                    val st = parseTimeToMinutes(ev.startTime)
                    val en = parseTimeToMinutes(ev.endTime)
                    if (st != null && en != null && en > st) en - st else 0
                }
            fixedEventsMin += dayEvents

            val effBlocks = effectiveBlocksForDate(s, d)
            val dayBlocks = effBlocks.sumOf { blockDurationMinutes(it) }
            existingBlocksMin += dayBlocks

            for (b in effBlocks) {
                if (b.taskId.isNotBlank()) {
                    allocatedByTask[b.taskId] = (allocatedByTask[b.taskId] ?: 0) + blockDurationMinutes(b)
                }
                if (b.habitId.isNotBlank()) {
                    allocatedHabitDates += "${b.habitId}@${b.date}"
                }
            }

            val dayFree = min(freeIntervalsForDate(s, d).sumOf { it.minutes }, (config.dailyLimitMinutes - dayBlocks).coerceAtLeast(0))
            freeCapMin += dayFree
        }

        val activePathIds = s.paths.filter { it.state !in 3..5 }.map { it.id }.toSet()
        val relevantTasks = s.pathTasks.filter { t ->
            !t.completed && !t.archived && (t.pathId.isBlank() || t.pathId in activePathIds) && (
                (t.deadline.isNotBlank() && t.deadline <= toDate) ||
                    (t.scheduledDate.isNotBlank() && t.scheduledDate in fromDate..toDate) ||
                    (t.deadline.isBlank() && t.scheduledDate.isBlank())
                )
        }

        val unestimatedTasks = relevantTasks.filter { it.estimateMinutes <= 0 }
        var pendingTasksDemand = 0
        val incompatibleDeadlineTasks = mutableListOf<PathTask>()

        for (t in relevantTasks) {
            if (t.estimateMinutes <= 0) continue
            val remaining = (t.estimateMinutes - (allocatedByTask[t.id] ?: 0)).coerceAtLeast(0)
            pendingTasksDemand += remaining
            if (t.deadline.isNotBlank() && t.deadline <= toDate && remaining > 0) {
                val capUntilDeadline = dates.filter { it <= t.deadline }.sumOf { d ->
                    min(freeIntervalsForDate(s, d).sumOf { it.minutes }, config.dailyLimitMinutes)
                }
                if (remaining > capUntilDeadline) {
                    incompatibleDeadlineTasks += t
                }
            }
        }

        var habitsDemand = 0
        val activeHabits = s.habits.filter { !it.deleted && !it.archived && !it.paused }
        for (d in dates) {
            for (h in activeHabits) {
                if (s.planned(h, d) && s.record(h.id, d)?.complete != true && "${h.id}@$d" !in allocatedHabitDates) {
                    habitsDemand += if (h.type == 2) h.target.coerceIn(10, 120) else 20
                }
            }
        }

        val dueCards = dueFlashcards(s, toDate)
        val mimirDemand = if (dueCards.isEmpty()) 0 else (dueCards.size * 2).coerceIn(15, 60)

        val totalUnallocatedDemand = pendingTasksDemand + habitsDemand + mimirDemand
        val balance = freeCapMin - totalUnallocatedDemand
        val totalCommittedPlusDemand = existingBlocksMin + totalUnallocatedDemand
        val utilization = if (configuredAvail <= 0) {
            if (totalCommittedPlusDemand > 0) 100 else 0
        } else {
            ((totalCommittedPlusDemand * 100.0) / configuredAvail).toInt().coerceAtLeast(0)
        }

        val status = when {
            !hasConfiguredAvailability(s) -> CapacityStatus.UNCONFIGURED
            balance < 0 || utilization > 100 -> CapacityStatus.OVERLOADED
            utilization >= 85 -> CapacityStatus.TIGHT
            utilization >= 55 -> CapacityStatus.BALANCED
            else -> CapacityStatus.COMFORTABLE
        }

        val warnings = mutableListOf<String>()
        if (status == CapacityStatus.UNCONFIGURED) {
            warnings += "Configure sua disponibilidade semanal para medições exatas de capacidade."
        }
        if (unestimatedTasks.isNotEmpty()) {
            warnings += "${unestimatedTasks.size} tarefa(s) pendente(s) sem estimativa de duração não puderam ser somadas à carga."
        }
        if (incompatibleDeadlineTasks.isNotEmpty()) {
            warnings += "${incompatibleDeadlineTasks.size} tarefa(s) possuem prazo incompatível com a capacidade livre disponível."
        }
        if (balance < 0) {
            warnings += "Déficit estimado de ${-balance} min no período; reduza escopo, divida tarefas ou amplie janelas."
        }

        return CapacitySimulation(
            periodLabel = kind.label,
            dateFrom = fromDate,
            dateTo = toDate,
            configuredAvailableMinutes = configuredAvail,
            fixedEventsMinutes = fixedEventsMin,
            existingBlocksMinutes = existingBlocksMin,
            freeCapacityMinutes = freeCapMin,
            pendingTasksDemandMinutes = pendingTasksDemand,
            habitsDemandMinutes = habitsDemand,
            mimirReviewDemandMinutes = mimirDemand,
            totalUnallocatedDemandMinutes = totalUnallocatedDemand,
            balanceMinutes = balance,
            utilizationPercent = utilization,
            status = status,
            unestimatedTasks = unestimatedTasks,
            incompatibleDeadlineTasks = incompatibleDeadlineTasks,
            warnings = warnings
        )
    }

    // =========================================================================
    // 7. REPLANEJAMENTO ADAPTATIVO (SEM FALLBACK FIXO ÀS 09H, ZERO PUNIÇÃO NO RPG)
    // =========================================================================

    fun generateReplanProposal(
        s: GameState,
        todayDate: String = today(),
        clock: PlanningClock = PlanningClock(referenceDate = todayDate, referenceMinuteOfDay = 0)
    ): List<ReplanItem> {
        val base = runCatching { LocalDate.parse(todayDate) }.getOrElse { LocalDate.now() }
        val tasksById = s.pathTasks.associateBy { it.id }
        val config = resolvedPlanningConfig(s)
        val buffer = config.bufferMinutes.coerceAtLeast(0)
        val items = mutableListOf<ReplanItem>()
        val seenTaskIds = mutableSetOf<String>()

        fun buildOptions(minutes: Int, taskId: String = "", blockId: String = ""): List<ReplanSlotOption> {
            val dur = minutes.coerceIn(20, 120)
            val half1 = max(15, dur / 2)
            val half2 = max(15, dur - half1)
            val blockedByPrereqs = taskId.isNotBlank() && blockingPrerequisites(taskId, tasksById, s.taskDependencies).isNotEmpty()

            var transferSlot: Triple<String, Int, Int>? = null
            val splitSlots = mutableListOf<Triple<String, Int, Int>>()
            var nextWeekSlot: Triple<String, Int, Int>? = null

            if (!blockedByPrereqs) {
                // Próximo horário livre em 0..10 dias respeitando horário corrente, limite diário e buffers
                for (offset in 0..10) {
                    val d = base.plusDays(offset.toLong()).toString()
                    val minStart = if (d == clock.referenceDate && clock.referenceMinuteOfDay > 0) clock.referenceMinuteOfDay else 0
                    val existingMin = effectiveBlocksForDate(s, d).filter { it.id != blockId }.sumOf { blockDurationMinutes(it) }
                    val remainingDaily = (config.dailyLimitMinutes - existingMin).coerceAtLeast(0)
                    val freeIvs = freeIntervalsForDate(s, d, ignoreBlockId = blockId, minStartMinute = minStart)

                    for (iv in freeIvs) {
                        if (transferSlot == null && iv.minutes >= dur && remainingDaily >= dur) {
                            transferSlot = Triple(d, iv.start, iv.start + dur)
                        }
                        if (splitSlots.isEmpty() && iv.minutes >= half1 && remainingDaily >= half1) {
                            splitSlots += Triple(d, iv.start, iv.start + half1)
                            // Se a mesma janela comportar a segunda metade após o buffer e dentro do limite diário
                            val secondStartInSame = iv.start + half1 + buffer
                            if (iv.end - secondStartInSame >= half2 && remainingDaily >= half1 + half2) {
                                splitSlots += Triple(d, secondStartInSame, secondStartInSame + half2)
                            }
                        } else if (splitSlots.size == 1 && iv.minutes >= half2) {
                            val first = splitSlots[0]
                            if (d != first.first && remainingDaily >= half2) {
                                splitSlots += Triple(d, iv.start, iv.start + half2)
                            } else if (d == first.first && iv.start >= first.third + buffer && remainingDaily >= half1 + half2) {
                                splitSlots += Triple(d, iv.start, iv.start + half2)
                            }
                        }
                    }
                }
                // Próxima semana (7..14 dias) — somente janelas reais! Nunca inventa 09:00 se indisponível.
                for (offset in 7..14) {
                    val d = base.plusDays(offset.toLong()).toString()
                    val existingMin = effectiveBlocksForDate(s, d).filter { it.id != blockId }.sumOf { blockDurationMinutes(it) }
                    if (config.dailyLimitMinutes - existingMin < dur) continue
                    val found = freeIntervalsForDate(s, d, ignoreBlockId = blockId).firstOrNull { it.minutes >= dur }
                    if (found != null) {
                        nextWeekSlot = Triple(d, found.start, found.start + dur)
                        break
                    }
                }
            }

            val opts = mutableListOf<ReplanSlotOption>()
            if (transferSlot != null) {
                opts += ReplanSlotOption(
                    action = ReplanActionKind.TRANSFER,
                    summary = "Mover para ${transferSlot.first} · ${formatMinutesToTime(transferSlot.second)}–${formatMinutesToTime(transferSlot.third)}",
                    targetDate = transferSlot.first,
                    startTime = formatMinutesToTime(transferSlot.second),
                    endTime = formatMinutesToTime(transferSlot.third)
                )
            }
            if (splitSlots.size >= 2) {
                val a = splitSlots[0]
                val b = splitSlots[1]
                opts += ReplanSlotOption(
                    action = ReplanActionKind.SPLIT,
                    summary = "Dividir em ${a.first} (${formatMinutesToTime(a.second)}–${formatMinutesToTime(a.third)}) + ${b.first} (${formatMinutesToTime(b.second)}–${formatMinutesToTime(b.third)})",
                    targetDate = a.first,
                    startTime = formatMinutesToTime(a.second),
                    endTime = formatMinutesToTime(a.third),
                    secondDate = b.first,
                    secondStartTime = formatMinutesToTime(b.second),
                    secondEndTime = formatMinutesToTime(b.third)
                )
            }
            if (nextWeekSlot != null) {
                opts += ReplanSlotOption(
                    action = ReplanActionKind.POSTPONE,
                    summary = "Adiar para ${nextWeekSlot.first} · ${formatMinutesToTime(nextWeekSlot.second)}–${formatMinutesToTime(nextWeekSlot.third)}",
                    targetDate = nextWeekSlot.first,
                    startTime = formatMinutesToTime(nextWeekSlot.second),
                    endTime = formatMinutesToTime(nextWeekSlot.third)
                )
            }
            opts += ReplanSlotOption(
                action = ReplanActionKind.SEND_TO_PENDING,
                summary = "Retirar da data vencida e manter na Central de Pendências"
            )
            opts += ReplanSlotOption(
                action = ReplanActionKind.REMOVE_SCHEDULE,
                summary = "Remover apenas o agendamento/bloco sem alterar seu progresso no RPG"
            )
            return opts
        }

        val activePathIds = s.paths.filter { !isPathInactiveForPending(it.state) }.map { it.id }.toSet()

        // Blocos passados vinculados a tarefas ainda não concluídas
        for (b in s.timeBlocks.filter { it.date < todayDate }.sortedBy { it.date }) {
            val linkedTask = tasksById[b.taskId]
            if (linkedTask != null && !linkedTask.completed && !linkedTask.archived && (linkedTask.pathId.isBlank() || linkedTask.pathId in activePathIds)) {
                seenTaskIds += linkedTask.id
                val mins = blockDurationMinutes(b).coerceAtLeast(20)
                val opts = buildOptions(mins, linkedTask.id, b.id)
                val hasTimeOptions = opts.any { it.action in setOf(ReplanActionKind.TRANSFER, ReplanActionKind.SPLIT, ReplanActionKind.POSTPONE) }
                val reasonSuffix = if (!hasTimeOptions) " (sem janela livre disponível; mantenha em Pendências ou configure disponibilidade)" else ""
                items += ReplanItem(
                    id = "replan-blk-${b.id}",
                    title = b.title,
                    taskId = linkedTask.id,
                    blockId = b.id,
                    pathId = b.pathId.ifBlank { linkedTask.pathId },
                    areaId = b.areaId.ifBlank { linkedTask.areaId },
                    originalDate = b.date,
                    minutes = mins,
                    reason = "Bloco agendado em ${b.date} ainda não concluído.$reasonSuffix",
                    options = opts
                )
            }
        }

        // Tarefas agendadas ou vencidas antes de hoje sem bloco já capturado
        for (t in s.pathTasks.filter { !it.completed && !it.archived && it.id !in seenTaskIds && (it.pathId.isBlank() || it.pathId in activePathIds) }) {
            val missedDate = when {
                t.scheduledDate.isNotBlank() && t.scheduledDate < todayDate -> t.scheduledDate
                t.deadline.isNotBlank() && t.deadline < todayDate -> t.deadline
                else -> ""
            }
            if (missedDate.isNotBlank()) {
                val mins = (if (t.estimateMinutes > 0) t.estimateMinutes else 30).coerceIn(20, 120)
                val opts = buildOptions(mins, t.id, "")
                val hasTimeOptions = opts.any { it.action in setOf(ReplanActionKind.TRANSFER, ReplanActionKind.SPLIT, ReplanActionKind.POSTPONE) }
                val baseReason = if (t.deadline.isNotBlank() && t.deadline < todayDate) "Prazo ultrapassado (${t.deadline})." else "Agendada para $missedDate sem conclusão."
                val reasonSuffix = if (!hasTimeOptions) " Sem janela livre disponível no momento." else ""
                items += ReplanItem(
                    id = "replan-task-${t.id}",
                    title = t.title,
                    taskId = t.id,
                    blockId = "",
                    pathId = t.pathId,
                    areaId = t.areaId,
                    originalDate = missedDate,
                    minutes = mins,
                    reason = "$baseReason$reasonSuffix",
                    options = opts
                )
            }
        }
        return items
    }

    // =========================================================================
    // 8. MATRIZ DE EISENHOWER (EXCLUSIVAMENTE CRITÉRIOS EXPLÍCITOS DO USUÁRIO)
    // =========================================================================

    fun eisenhowerQuadrant(task: PathTask): EisenhowerQuadrant = when {
        task.important && task.urgent -> EisenhowerQuadrant.Q1_DO
        task.important && !task.urgent -> EisenhowerQuadrant.Q2_PLAN
        !task.important && task.urgent -> EisenhowerQuadrant.Q3_DELEGATE
        else -> EisenhowerQuadrant.Q4_ELIMINATE
    }

    fun groupByEisenhower(tasks: List<PathTask>): Map<EisenhowerQuadrant, List<PathTask>> {
        val active = tasks.filter { !it.completed && !it.archived }
        return EisenhowerQuadrant.entries.associateWith { q ->
            active.filter { eisenhowerQuadrant(it) == q }
                .sortedWith(compareBy<PathTask> { it.deadline.ifBlank { "9999-12-31" } }.thenByDescending { it.priority })
        }
    }

    // =========================================================================
    // 9. MOTOR DE RECORRÊNCIAS (AGENDA 3.0)
    // =========================================================================

    fun expandRecurrenceOccurrences(
        rule: RecurrenceRule,
        fromDate: String,
        toDate: String
    ): List<String> {
        if (!rule.active) return emptyList()
        val ruleStart = runCatching { LocalDate.parse(rule.startDate) }.getOrNull() ?: return emptyList()
        val ruleEnd = rule.endDate.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val rangeStart = runCatching { LocalDate.parse(fromDate) }.getOrNull() ?: return emptyList()
        val rangeEnd = runCatching { LocalDate.parse(toDate) }.getOrNull() ?: return emptyList()
        if (rangeEnd.isBefore(rangeStart)) return emptyList()

        val effectiveStart = if (rangeStart.isBefore(ruleStart)) ruleStart else rangeStart
        val effectiveEnd = if (ruleEnd != null && ruleEnd.isBefore(rangeEnd)) ruleEnd else rangeEnd
        if (effectiveEnd.isBefore(effectiveStart)) return emptyList()

        val excluded = rule.excludedDates.split(',').map { it.trim() }.filter { it.isNotBlank() }.toSet()
        val allowedDays = rule.daysOfWeek.split(',').mapNotNull { it.trim().toIntOrNull() }.toSet()
        val out = mutableListOf<String>()
        var d = effectiveStart
        var guard = 0
        while (!d.isAfter(effectiveEnd) && guard < 3660) {
            guard++
            val ds = d.toString()
            if (ds !in excluded) {
                val matches = when (rule.pattern) {
                    "daily" -> true
                    "weekly" -> d.dayOfWeek == ruleStart.dayOfWeek
                    "weekdays" -> if (allowedDays.isEmpty()) d.dayOfWeek.value in 1..5 else d.dayOfWeek.value in allowedDays
                    "monthly" -> d.dayOfMonth == min(ruleStart.dayOfMonth, d.lengthOfMonth())
                    else -> false
                }
                if (matches) out += ds
            }
            d = d.plusDays(1)
        }
        return out
    }
}
