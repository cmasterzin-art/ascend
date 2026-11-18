package br.com.ascend.aetheris

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.*
import org.junit.Test

class FocusAttributionTest {
    private val date = "2024-02-29"
    private fun epoch(time: String) = LocalDate.parse(date).atTime(LocalTime.parse(time))
        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private fun session(from: String, to: String, seconds: Long, path: String = "a") =
        FocusSession("f", date = date, startedAt = epoch(from), endedAt = epoch(to),
            elapsedSeconds = seconds, plannedMinutes = 120, status = "interrupted",
            registered = true, taskId = "t", pathId = path)
    private fun block(id: String, from: String, to: String) =
        TimeBlock(id = id, title = id, date = date, startTime = from, endTime = to, taskId = "t")

    @Test fun movedTaskDoesNotMoveHistoricalFocus() {
        val state = GameState(pathTasks = listOf(PathTask(id = "t", pathId = "b", title = "Tarefa")),
            focusSessions = listOf(session("08:00", "09:00", 3600)))
        assertEquals(60L, pathFocusMinutes(state, "a"))
        assertEquals(0L, pathFocusMinutes(state, "b"))
        assertEquals(0L, pathFocusMinutesOnDate(state, "b", date))
    }

    @Test fun blocksPartitionTimeWithoutDuplicatingIt() {
        val blocks = listOf(block("first", "08:00", "09:00"), block("overlap", "08:30", "09:30"),
            block("adjacent", "09:30", "10:00"), block("evening", "18:00", "19:00"))
        val state = GameState(timeBlocks = blocks, focusSessions = listOf(session("08:45", "09:45", 3600)))
        assertEquals(listOf(15L, 30L, 15L, 0L), blocks.map { timeBlockActualMinutes(state, it) })
    }

    @Test fun suspendedPathsKeepTasksOutsideActiveSurfaces() {
        val task = PathTask(id = "t", pathId = "a", title = "Tarefa", scheduledDate = date)
        for (status in 0..5) {
            val state = GameState(paths = listOf(PersonalPath(id = "a", state = status)), pathTasks = listOf(task))
            val count = if (status <= 1) 1 else 0
            assertEquals(count, pendingBuckets(state, date).todayTasks.size)
            assertEquals(count, agendaDay(state, date).tasks.size)
            assertEquals(count, monthMarker(state, date).tasksCount)
        }
    }

    @Test fun pausesReportOnlyProvenMinutes() {
        val left = block("left", "08:00", "09:00")
        val right = block("right", "09:00", "10:00")
        val state = GameState(timeBlocks = listOf(left, right),
            focusSessions = listOf(session("08:30", "09:30", 2700)))
        val result = timeBlockFocus(state)
        assertEquals(BlockFocus(900, true), result[left.id])
        assertEquals(BlockFocus(900, true), result[right.id])
        val enclosing = block("all", "08:30", "09:30")
        assertEquals(BlockFocus(2700, false), timeBlockFocus(state.copy(timeBlocks = listOf(enclosing)))[enclosing.id])
    }

    @Test fun nextActivitiesRespectStartEndDayAndUntimedEvents() {
        val early = AgendaEvent(id = "early", date = date, title = "Cedo", startTime = "08:00")
        val active = AgendaEvent(id = "active", date = date, title = "Agora", startTime = "17:00", endTime = "18:30")
        val later = AgendaEvent(id = "later", date = date, title = "Depois", startTime = "19:00")
        val untimed = AgendaEvent(id = "untimed", date = date, title = "Sem hora")
        val ongoing = block("ongoing", "17:00", "18:30")
        val evening = block("evening", "19:00", "20:00")
        val state = GameState(agendaEvents = listOf(early, active, later, untimed),
            timeBlocks = listOf(ongoing, evening))
        assertEquals(early, rumosDoDia(state, date, LocalTime.of(8, 0)).nextEvent)
        val atSix = rumosDoDia(state, date, LocalTime.of(18, 0))
        assertEquals(active, atSix.nextEvent)
        assertTrue(atSix.eventInProgress)
        assertTrue(atSix.blockInProgress)
        assertEquals(listOf(untimed), atSix.untimedEvents)
        val end = rumosDoDia(state, date, LocalTime.of(18, 30))
        assertEquals(later, end.nextEvent)
        assertEquals(evening, end.nextBlock)
        assertFalse(end.eventInProgress)
        assertNull(rumosDoDia(state, date, LocalTime.of(19, 1)).nextEvent)
        assertNull(rumosDoDia(state, "2024-03-01", LocalTime.MIDNIGHT).nextBlock)
        assertEquals(later, rumosDoDia(state.copy(agendaEvents = listOf(active.copy(completed = true), later)),
            date, LocalTime.of(18, 0)).nextEvent)
    }
}
