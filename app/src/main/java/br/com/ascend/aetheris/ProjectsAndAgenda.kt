package br.com.ascend.aetheris

import androidx.room.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

val pathStates = listOf("Planejado", "Em andamento", "Pausado", "Concluído", "Arquivado", "Cancelado")
val priorityLabels = listOf("Baixa", "Média", "Alta", "Essencial")
val eventKinds = listOf("Compromisso", "Aula", "Consulta", "Reunião", "Entrega")

@Entity(tableName = "paths")
data class PersonalPath(
    @PrimaryKey val id: String = uid(),
    val name: String = "",
    val description: String = "",
    val category: String = "Pessoal",
    val icon: Int = 0,
    val color: Int = 0,
    val startDate: String = today(),
    val deadline: String = "",
    val priority: Int = 1,
    val state: Int = 1,
    val attribute: Int = -1,
    val criteria: String = "",
    val notes: String = "",
    val completedDate: String = "",
    val position: Int = 0
)

@Entity(
    tableName = "path_stages",
    foreignKeys = [ForeignKey(entity = PersonalPath::class, parentColumns = ["id"], childColumns = ["pathId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("pathId")]
)
data class PathStage(
    @PrimaryKey val id: String = uid(),
    val pathId: String,
    val name: String,
    val targetNote: String = "",
    val position: Int = 0
)

@Entity(
    tableName = "path_tasks",
    indices = [Index("pathId"), Index("stageId"), Index("scheduledDate"), Index("deadline")]
)
data class PathTask(
    @PrimaryKey val id: String = uid(),
    val pathId: String = "",
    val stageId: String = "",
    val title: String,
    val description: String = "",
    val priority: Int = 1,
    val deadline: String = "",
    val scheduledDate: String = "",
    val scheduledTime: String = "",
    val estimateMinutes: Int = 0,
    val completed: Boolean = false,
    val completedDate: String = "",
    val archived: Boolean = false,
    val position: Int = 0,
    val notes: String = "",
    val created: String = today()
)

@Entity(
    tableName = "path_subtasks",
    foreignKeys = [ForeignKey(entity = PathTask::class, parentColumns = ["id"], childColumns = ["taskId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("taskId")]
)
data class PathSubtask(
    @PrimaryKey val id: String = uid(),
    val taskId: String,
    val title: String,
    val done: Boolean = false,
    val position: Int = 0
)

@Entity(
    tableName = "path_habit_links",
    foreignKeys = [
        ForeignKey(entity = PersonalPath::class, parentColumns = ["id"], childColumns = ["pathId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = Habit::class, parentColumns = ["id"], childColumns = ["habitId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("pathId"), Index("habitId")]
)
data class PathHabitLink(
    @PrimaryKey val id: String,
    val pathId: String,
    val habitId: String,
    val created: String = today()
)

@Entity(
    tableName = "time_blocks",
    indices = [Index("date"), Index("pathId"), Index("taskId"), Index("habitId")]
)
data class TimeBlock(
    @PrimaryKey val id: String = uid(),
    val title: String,
    val date: String = today(),
    val startTime: String = "09:00",
    val endTime: String = "10:00",
    val category: String = "Foco",
    val color: Int = 1,
    val description: String = "",
    val pathId: String = "",
    val taskId: String = "",
    val habitId: String = ""
)

@Entity(
    tableName = "agenda_events",
    indices = [Index("date")]
)
data class AgendaEvent(
    @PrimaryKey val id: String = uid(),
    val title: String,
    val date: String = today(),
    val startTime: String = "",
    val endTime: String = "",
    val kind: String = "Compromisso",
    val description: String = "",
    val completed: Boolean = false,
    val archived: Boolean = false
)

@Entity(tableName = "inbox_items")
data class InboxItem(
    @PrimaryKey val id: String = uid(),
    val title: String,
    val notes: String = "",
    val created: String = today(),
    val archived: Boolean = false
)

data class PathProgress(val done: Int, val total: Int) {
    val fraction: Float get() = if (total <= 0) 0f else done.toFloat() / total
    val percent: Int get() = if (total <= 0) 0 else (done * 100 / total)
}

fun pathProgress(s: GameState, pathId: String): PathProgress {
    val tasks = s.pathTasks.filter { it.pathId == pathId && !it.archived }
    return PathProgress(tasks.count { it.completed }, tasks.size)
}

fun stageProgress(s: GameState, stageId: String): PathProgress {
    val tasks = s.pathTasks.filter { it.stageId == stageId && !it.archived }
    return PathProgress(tasks.count { it.completed }, tasks.size)
}

fun pathDeadlineDays(path: PersonalPath, refDate: String = today()): Long? =
    if (path.deadline.isBlank()) null
    else ChronoUnit.DAYS.between(LocalDate.parse(refDate), LocalDate.parse(path.deadline))

// A missing historical path is unassigned. A task's current parent is never a substitute.
fun pathFocusMinutes(s: GameState, pathId: String): Long =
    if (pathId.isBlank()) 0 else s.focusSessions
        .filter { it.registered && it.pathId == pathId }.sumOf { it.elapsedSeconds } / 60

fun taskFocusMinutes(s: GameState, taskId: String): Long =
    if (taskId.isBlank()) 0 else s.focusSessions
        .filter { it.registered && it.taskId == taskId }.sumOf { it.elapsedSeconds } / 60

fun pathFocusMinutesOnDate(s: GameState, pathId: String, date: String): Long =
    if (pathId.isBlank()) 0 else s.focusSessions
        .filter { it.registered && it.date == date && it.pathId == pathId }
        .sumOf { it.elapsedSeconds } / 60

fun isPathInactiveForPending(state: Int): Boolean = state !in 0..1

fun isTaskActivePending(s: GameState, t: PathTask): Boolean {
    if (t.archived) return false
    if (t.pathId.isBlank()) return true
    val parent = s.paths.find { it.id == t.pathId } ?: return false
    return !isPathInactiveForPending(parent.state)
}

fun isTaskOverdue(t: PathTask, refDate: String = today()): Boolean =
    !t.completed && !t.archived && (
        (t.deadline.isNotBlank() && t.deadline < refDate) ||
        (t.scheduledDate.isNotBlank() && t.scheduledDate < refDate)
    )

fun isTaskDueSoon(t: PathTask, refDate: String = today(), daysAhead: Long = 3): Boolean {
    if (t.completed || t.archived || isTaskOverdue(t, refDate)) return false
    val limit = LocalDate.parse(refDate).plusDays(daysAhead).toString()
    val d = t.deadline
    val sc = t.scheduledDate
    return (d.isNotBlank() && d in refDate..limit) || (sc.isNotBlank() && sc in refDate..limit)
}

data class PendingBuckets(
    val todayTasks: List<PathTask>,
    val upcomingTasks: List<PathTask>,
    val overdueTasks: List<PathTask>,
    val undatedTasks: List<PathTask>,
    val highPriorityTasks: List<PathTask>,
    val completedTasks: List<PathTask>
)

fun pendingBuckets(s: GameState, refDate: String = today()): PendingBuckets {
    val activeScope = s.pathTasks.filter { isTaskActivePending(s, it) }
    val pendingComparator = compareByDescending<PathTask> { it.priority }
        .thenBy { it.deadline.ifBlank { "9999-99-99" } }
        .thenBy { it.scheduledDate.ifBlank { "9999-99-99" } }
        .thenBy { it.position }
    val open = activeScope.filter { !it.completed }.sortedWith(pendingComparator)
    val overdue = open.filter { isTaskOverdue(it, refDate) }
    val todayList = open.filter { (it.scheduledDate == refDate || it.deadline == refDate) }
    val upcoming = open.filter {
        !isTaskOverdue(it, refDate) &&
        it.scheduledDate != refDate &&
        it.deadline != refDate &&
        (it.scheduledDate > refDate || it.deadline > refDate)
    }
    val undated = open.filter { it.scheduledDate.isBlank() && it.deadline.isBlank() }
    val high = open.filter { it.priority >= 2 }
    val completed = s.pathTasks
        .filter { !it.archived && it.completed && (it.pathId.isBlank() || s.paths.find { p -> p.id == it.pathId }?.state !in listOf(4, 5)) }
        .sortedWith(compareByDescending<PathTask> { it.completedDate }.thenBy { it.position })
    return PendingBuckets(todayList, upcoming, overdue, undated, high, completed)
}

fun timeBlockMinutes(b: TimeBlock): Int {
    val start = LocalTime.parse(b.startTime)
    val end = LocalTime.parse(b.endTime)
    return ChronoUnit.MINUTES.between(start, end).toInt().coerceAtLeast(0)
}

data class BlockFocus(val seconds: Long = 0, val uncertain: Boolean = false) {
    val minutes: Long get() = seconds / 60
}

private data class BlockWindow(val block: TimeBlock, val start: Long, val end: Long) {
    val specificity: Int get() = if (block.taskId.isNotBlank() || block.habitId.isNotBlank()) 0 else 1

    fun matches(session: FocusSession): Boolean = with(block) {
        when {
            taskId.isNotBlank() -> session.taskId == taskId && (pathId.isBlank() || session.pathId == pathId)
            habitId.isNotBlank() -> session.habitId == habitId
            pathId.isNotBlank() -> session.pathId == pathId
            else -> false
        }
    }
}

/**
 * Assign overlapping time to the most specific target, then earliest start and stable ID.
 * Session v7 has a wall-clock envelope and total effective seconds, but no pause intervals.
 * An allocated window can prove only its duration minus all potentially idle time.
 */
fun timeBlockFocus(s: GameState, zone: ZoneId = ZoneId.systemDefault()): Map<String, BlockFocus> {
    fun boundary(date: LocalDate, text: String): Long? {
        val local = date.atTime(LocalTime.parse(text))
        val offset = zone.rules.getValidOffsets(local).singleOrNull() ?: return null
        return local.toInstant(offset).toEpochMilli()
    }
    val totals = mutableMapOf<String, BlockFocus>()
    val windows = s.timeBlocks.mapNotNull { block ->
        if (block.taskId.isBlank() && block.habitId.isBlank() && block.pathId.isBlank()) return@mapNotNull null
        val date = LocalDate.parse(block.date)
        val start = boundary(date, block.startTime)
        val end = boundary(date, block.endTime)
        if (start == null || end == null) {
            totals[block.id] = BlockFocus(uncertain = true)
            return@mapNotNull null
        }
        if (start < end) BlockWindow(block, start, end) else null
    }
    val byDate = windows.groupBy { it.block.date }
    for (session in s.focusSessions) {
        if (!session.registered || session.elapsedSeconds <= 0 || session.startedAt <= 0) continue
        val firstDay = Instant.ofEpochMilli(session.startedAt).atZone(zone).toLocalDate()
        val lastDay = Instant.ofEpochMilli(session.endedAt).atZone(zone).toLocalDate()
        val validClock = firstDay.toString() == session.date && session.endedAt >= session.startedAt &&
            session.elapsedSeconds <= (session.endedAt - session.startedAt) / 1000
        if (!validClock) {
            byDate[session.date].orEmpty().filter { it.matches(session) }.forEach {
                totals[it.block.id] = totals.getOrDefault(it.block.id, BlockFocus()).copy(uncertain = true)
            }
            continue
        }
        val days = if (firstDay == lastDay) byDate[session.date].orEmpty()
        else byDate.filterKeys { it >= firstDay.toString() && it <= lastDay.toString() }.values.flatten()
        val candidates = days.filter {
            it.matches(session) && it.start < session.endedAt && it.end > session.startedAt
        }.sortedWith(compareBy<BlockWindow> { it.specificity }.thenBy { it.start }.thenBy { it.block.id })
        if (candidates.isEmpty()) continue

        val cuts = candidates.flatMap {
            listOf(maxOf(it.start, session.startedAt), minOf(it.end, session.endedAt))
        }.distinct().sorted()
        val assigned = mutableMapOf<String, Long>()
        for ((from, to) in cuts.zipWithNext()) {
            val owner = candidates.firstOrNull { it.start <= from && it.end >= to } ?: continue
            assigned[owner.block.id] = assigned.getOrDefault(owner.block.id, 0L) + to - from
        }
        val idleMs = session.endedAt - session.startedAt - session.elapsedSeconds * 1000
        for ((id, windowMs) in assigned) {
            val guaranteed = (windowMs - idleMs).coerceAtLeast(0) / 1000
            val possible = minOf(windowMs / 1000, session.elapsedSeconds)
            val old = totals.getOrDefault(id, BlockFocus())
            totals[id] = BlockFocus(old.seconds + guaranteed, old.uncertain || possible > guaranteed)
        }
    }
    return totals
}

fun timeBlockActualMinutes(s: GameState, b: TimeBlock): Long =
    timeBlockFocus(if (s.timeBlocks.any { it.id == b.id }) s else s.copy(timeBlocks = s.timeBlocks + b))
        .getOrDefault(b.id, BlockFocus()).minutes

fun timeBlockOverlaps(blocks: List<TimeBlock>): Set<String> {
    if (blocks.size < 2) return emptySet()
    val parsed = blocks.mapNotNull { b ->
        runCatching { Triple(b.id, LocalTime.parse(b.startTime), LocalTime.parse(b.endTime)) }.getOrNull()
    }
    val ids = HashSet<String>()
    for (i in parsed.indices) {
        for (j in i + 1 until parsed.size) {
            val (id1, s1, e1) = parsed[i]
            val (id2, s2, e2) = parsed[j]
            if (s1 < e2 && s2 < e1) {
                ids += id1
                ids += id2
            }
        }
    }
    return ids
}

data class AgendaDaySummary(
    val date: String,
    val habits: List<Habit>,
    val tasks: List<PathTask>,
    val blocks: List<TimeBlock>,
    val overlappingBlockIds: Set<String>,
    val events: List<AgendaEvent>,
    val deadlines: List<PersonalPath>
) {
    val isEmpty: Boolean
        get() = habits.isEmpty() && tasks.isEmpty() && blocks.isEmpty() && events.isEmpty() && deadlines.isEmpty()
}

fun agendaDay(s: GameState, date: String): AgendaDaySummary {
    val habits = s.due(date).sortedWith(compareBy<Habit> { it.time.ifBlank { "99:99" } }.thenBy { it.position })
    val tasks = s.pathTasks.filter {
        (isTaskActivePending(s, it) || it.completed && !it.archived) && (it.scheduledDate == date || it.deadline == date)
    }.sortedWith(compareBy<PathTask> { it.completed }.thenBy { it.scheduledTime.ifBlank { "99:99" } }.thenByDescending { it.priority }.thenBy { it.position })
    val blocks = s.timeBlocks.filter { it.date == date }.sortedBy { LocalTime.parse(it.startTime) }
    val overlaps = timeBlockOverlaps(blocks)
    val events = s.agendaEvents.filter { !it.archived && it.date == date }
        .sortedWith(compareBy<AgendaEvent> { it.completed }.thenBy { if (it.startTime.isBlank()) LocalTime.MAX else LocalTime.parse(it.startTime) })
    val deadlines = s.paths.filter { !isPathInactiveForPending(it.state) && it.deadline == date }
    return AgendaDaySummary(date, habits, tasks, blocks, overlaps, events, deadlines)
}

data class MonthDayMarker(
    val date: String,
    val habitsPlanned: Int,
    val habitsDone: Int,
    val tasksCount: Int,
    val blocksCount: Int,
    val eventsCount: Int,
    val hasDeadline: Boolean,
    val rest: Boolean
) {
    val hasActivity: Boolean
        get() = habitsPlanned > 0 || tasksCount > 0 || blocksCount > 0 || eventsCount > 0 || hasDeadline
}

fun monthMarker(s: GameState, date: String): MonthDayMarker {
    val habits = s.due(date)
    val done = habits.count { s.record(it.id, date)?.complete == true }
    val tasks = s.pathTasks.count { isTaskActivePending(s, it) && !it.completed && (it.scheduledDate == date || it.deadline == date) }
    val blocks = s.timeBlocks.count { it.date == date }
    val events = s.agendaEvents.count { !it.archived && it.date == date }
    val hasDeadline = s.paths.any { !isPathInactiveForPending(it.state) && it.deadline == date }
    return MonthDayMarker(date, habits.size, done, tasks, blocks, events, hasDeadline, date in s.restDates)
}

data class RumosDoDia(
    val nextEvent: AgendaEvent?,
    val nextBlock: TimeBlock?,
    val priorityTasks: List<PathTask>,
    val upcomingPaths: List<PersonalPath>,
    val plannedBlocksCount: Int,
    val pendingTodayCount: Int,
    val overdueCount: Int,
    val inboxCount: Int,
    val eventInProgress: Boolean,
    val blockInProgress: Boolean,
    val untimedEvents: List<AgendaEvent>
)

fun rumosDoDia(s: GameState, refDate: String = today(), now: LocalTime = LocalTime.now()): RumosDoDia {
    val day = agendaDay(s, refDate)
    val buckets = pendingBuckets(s, refDate)
    val openEvents = day.events.filterNot { it.completed }
    val nextEvent = openEvents.firstOrNull {
        if (it.startTime.isBlank()) false else {
            val start = LocalTime.parse(it.startTime)
            val end = it.endTime.takeIf(String::isNotBlank)?.let(LocalTime::parse)
            start >= now || end != null && now < end
        }
    }
    val nextBlock = day.blocks.firstOrNull { LocalTime.parse(it.endTime) > now }
    val priorityTasks = (buckets.overdueTasks + buckets.todayTasks + buckets.highPriorityTasks)
        .distinctBy { it.id }
        .take(3)
    val limit = LocalDate.parse(refDate).plusDays(7).toString()
    val upcomingPaths = s.paths.filter { p ->
        p.state == 1 && (p.deadline.isBlank() || p.deadline <= limit)
    }.sortedWith(compareBy<PersonalPath> { it.deadline.ifBlank { "9999-99-99" } }.thenByDescending { it.priority }).take(3)
    val pendingTodayCount = buckets.todayTasks.count { !it.completed }
    val inboxCount = s.inboxItems.count { !it.archived }
    return RumosDoDia(
        nextEvent = nextEvent,
        nextBlock = nextBlock,
        priorityTasks = priorityTasks,
        upcomingPaths = upcomingPaths,
        plannedBlocksCount = day.blocks.size,
        pendingTodayCount = pendingTodayCount,
        overdueCount = buckets.overdueTasks.size,
        inboxCount = inboxCount,
        eventInProgress = nextEvent?.let { it.endTime.isNotBlank() && LocalTime.parse(it.startTime) <= now && now < LocalTime.parse(it.endTime) } == true,
        blockInProgress = nextBlock?.let { LocalTime.parse(it.startTime) <= now } == true,
        untimedEvents = openEvents.filter { it.startTime.isBlank() }
    )
}
