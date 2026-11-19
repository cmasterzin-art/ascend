package br.com.ascend.aetheris

import androidx.room.Room
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.security.MessageDigest
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = AscendApp::class)
class PathsAndAgendaTest {
    private lateinit var db: GameDatabase
    private lateinit var repo: GameRepository
    private val context get() = RuntimeEnvironment.getApplication() as AscendApp
    private val created = LocalDate.now().minusDays(10).toString()
    private val habit = Habit(
        id = "h-focus",
        name = "Estudo focado",
        type = 2,
        target = 60,
        xp = 60,
        coins = 12,
        points = 6,
        proportional = true,
        created = created
    )

    private fun s() = repo.state.value!!

    @Before
    fun setup() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java).build()
        repo = GameRepository(db)
        repo.refresh()
        repo.create(Player(name = "Sigrid", created = created), listOf(habit))
    }

    @After
    fun close() {
        db.close()
    }

    @Test
    fun schemaSixMigrationPreservesDataAndSupportsPathsAndAgenda() = runBlocking {
        repo.progress(habit, today(), 30)
        repo.plan(habit.id, today(), 1)
        repo.startFocus(habit, 25)
        db.dao().focus(s().focus!!.copy(elapsed = 120, startedAt = 0))
        repo.endFocus(true)
        val before = s()

        val dbName = "migration-v6-to-v7.db"
        legacyDatabase(context, dbName, 6, before)
        val migratedDb = Room.databaseBuilder(context, GameDatabase::class.java, dbName)
            .addMigrations(MIGRATION_6_7,MIGRATION_7_8)
            .build()
        try {
            val migratedRepo = GameRepository(migratedDb)
            migratedRepo.refresh()
            val expectedFromV6 = before.copy(focusSessions = before.focusSessions.map { it.copy(targetLabel = "") })
            assertEquals(expectedFromV6, migratedRepo.state.value)

            val path = PersonalPath(id = "path-v7", name = "Caminho Migrado", state = 1, deadline = LocalDate.now().plusDays(7).toString())
            val task = PathTask(id = "task-v7", pathId = path.id, title = "Primeira entrega")
            val block = TimeBlock(id = "blk-v7", title = "Bloco pós-migração", pathId = path.id, taskId = task.id)
            migratedRepo.savePath(path)
            migratedRepo.saveTask(task)
            migratedRepo.saveTimeBlock(block)

            val reopened = GameRepository(migratedDb)
            reopened.refresh()
            assertEquals(1, reopened.state.value!!.paths.size)
            assertEquals("Caminho Migrado", reopened.state.value!!.paths.single().name)
            assertEquals("Primeira entrega", reopened.state.value!!.pathTasks.single().title)
            assertEquals("Bloco pós-migração", reopened.state.value!!.timeBlocks.single().title)
        } finally {
            migratedDb.close()
            context.deleteDatabase(dbName)
        }
    }

    @Test
    fun pathStagesTasksSubtasksDuplicateMoveAndDeletePreserveConsistency() = runBlocking {
        val p1 = PersonalPath(id = "p1", name = "Construir oficina", state = 1, deadline = LocalDate.now().plusDays(5).toString())
        val p2 = PersonalPath(id = "p2", name = "Segundo caminho", state = 0)
        repo.savePath(p1)
        repo.savePath(p2)

        // Empty project must report 0/0 (0%) without fake progress
        assertEquals(PathProgress(0, 0), pathProgress(s(), p1.id))
        assertEquals(0, pathProgress(s(), p1.id).percent)

        val st1 = PathStage(id = "st1", pathId = p1.id, name = "Base", position = 0)
        val st2 = PathStage(id = "st2", pathId = p1.id, name = "Acabamento", position = 1)
        repo.saveStage(st1)
        repo.saveStage(st2)

        val t1 = PathTask(id = "t1", pathId = p1.id, stageId = st1.id, title = "Comprar madeira", priority = 2, position = 0)
        val t2 = PathTask(id = "t2", pathId = p1.id, stageId = st1.id, title = "Montar bancada", priority = 1, position = 1)
        repo.saveTask(t1)
        repo.saveTask(t2)

        val sub1 = PathSubtask(id = "sub1", taskId = t1.id, title = "Medir chapas", position = 0)
        val sub2 = PathSubtask(id = "sub2", taskId = t1.id, title = "Cortar ripas", position = 1)
        repo.saveSubtask(sub1)
        repo.saveSubtask(sub2)

        // Reorder and toggle subtasks
        repo.reorderSubtask(sub2.id, -1)
        val orderedSubs = s().pathSubtasks.filter { it.taskId == t1.id }.sortedBy { it.position }
        assertEquals(listOf("sub2", "sub1"), orderedSubs.map { it.id })
        repo.toggleSubtask(sub1.id)
        assertTrue(s().pathSubtasks.first { it.id == sub1.id }.done)

        // Complete task & reopen task -> progress recalculates cleanly, XP untouched
        val xpBefore = s().xp
        val coinsBefore = s().coins
        repo.toggleTask(t1.id)
        assertEquals(PathProgress(1, 2), pathProgress(s(), p1.id))
        assertEquals(50, pathProgress(s(), p1.id).percent)
        assertEquals(xpBefore, s().xp)
        assertEquals(coinsBefore, s().coins)

        repo.toggleTask(t1.id)
        assertEquals(PathProgress(0, 2), pathProgress(s(), p1.id))

        // Move task across stages and across paths
        repo.moveTask(t2.id, p1.id, st2.id)
        assertEquals(st2.id, s().pathTasks.first { it.id == t2.id }.stageId)
        assertEquals(PathProgress(0, 1), stageProgress(s(), st1.id))
        assertEquals(PathProgress(0, 1), stageProgress(s(), st2.id))

        repo.moveTask(t2.id, p2.id, "")
        assertEquals(p2.id, s().pathTasks.first { it.id == t2.id }.pathId)
        assertEquals("", s().pathTasks.first { it.id == t2.id }.stageId)

        // Duplicate task and path
        repo.duplicateTask(t1.id)
        assertEquals(2, s().pathTasks.count { it.pathId == p1.id })
        assertEquals(4, s().pathSubtasks.size)

        repo.duplicatePath(p1.id, includeStructure = true)
        assertEquals(3, s().paths.size)
        val clone = s().paths.first { it.id != p1.id && it.id != p2.id }
        assertTrue(clone.name.contains("cópia"))
        assertEquals(2, s().pathTasks.count { it.pathId == clone.id })

        // Complete path & check Salão das Memórias
        repo.setPathState(p1.id, 3, s().pathTasks.filter { it.pathId == p1.id && !it.completed }.map { it.id }.toSet())
        val completedPath = s().paths.first { it.id == p1.id }
        assertEquals(3, completedPath.state)
        assertTrue(completedPath.completedDate.isNotBlank())
        assertTrue(hallMemories(s()).any { it.category == "Caminhos" && it.title.contains(p1.name) })
        assertEquals(xpBefore, s().xp)
        assertEquals(coinsBefore, s().coins)

        // Focus on path then delete path: historical session targetLabel preserved
        repo.startPathFocus(pathId = clone.id, minutes = 20)
        db.dao().focus(s().focus!!.copy(elapsed = 300, startedAt = 0))
        repo.endFocus(true)
        assertEquals(1, s().focusSessions.size)
        repo.deletePath(clone.id)
        assertFalse(s().paths.any { it.id == clone.id })
        val preservedSession = s().focusSessions.single()
        assertEquals("", preservedSession.pathId)
        assertTrue(preservedSession.targetLabel.isNotBlank())
    }

    @Test
    fun habitLinksAndProjectFocusDoNotDuplicateOrInventXp() = runBlocking {
        val path = PersonalPath(id = "p-study", name = "Domínio Técnico", state = 1)
        val task = PathTask(id = "t-study", pathId = path.id, title = "Revisar capítulo 3")
        repo.savePath(path)
        repo.saveTask(task)

        repo.linkHabitToPath(path.id, habit.id, true)
        // Idempotent duplicate link rejected/ignored safely, single link present
        assertEquals(1, s().pathHabitLinks.size)

        // Completing linked habit grants ONLY habit's normal XP (once)
        repo.progress(habit, today(), 60)
        assertEquals(60L, s().xp)
        val coinsAfterHabit = s().coins
        assertTrue(coinsAfterHabit >= 12L)

        // Project/task focus records actual elapsed minutes with 0 extra XP/coins
        repo.startPathFocus(pathId = path.id, taskId = task.id, minutes = 30)
        val running = s().focus!!
        assertEquals("", running.habitId)
        assertEquals(path.id, running.pathId)
        assertEquals(task.id, running.taskId)
        db.dao().focus(running.copy(elapsed = 180, startedAt = 0))
        repo.pauseFocus()
        repo.endFocus(true)

        assertEquals(60L, s().xp)
        assertEquals(coinsAfterHabit, s().coins)
        assertEquals(1, s().focusSessions.size)
        val session = s().focusSessions.single()
        assertTrue(session.registered)
        assertEquals("", session.habitId)
        assertEquals(task.id, session.taskId)
        assertEquals(3L, pathFocusMinutes(s(), path.id))
        assertEquals(3L, taskFocusMinutes(s(), task.id))

        repo.linkHabitToPath(path.id, habit.id, false)
        assertTrue(s().pathHabitLinks.isEmpty())
        assertEquals(60L, s().xp)
    }

    @Test
    fun agendaTimeBlocksEventsInboxTriageAndPendingBucketsWorkAtomically() = runBlocking {
        val activePath = PersonalPath(id = "p-act", name = "Projeto Ativo", state = 1)
        val archivedPath = PersonalPath(id = "p-arc", name = "Projeto Arquivado", state = 4)
        val cancelledPath = PersonalPath(id = "p-can", name = "Projeto Cancelado", state = 5)
        repo.savePath(activePath)
        repo.savePath(archivedPath)
        repo.savePath(cancelledPath)

        val todayStr = today()
        val tomorrowStr = LocalDate.now().plusDays(1).toString()
        val yesterdayStr = LocalDate.now().minusDays(1).toString()

        val tToday = PathTask(id = "tt", pathId = activePath.id, title = "Hoje", scheduledDate = todayStr, priority = 2)
        val tOverdue = PathTask(id = "to", pathId = activePath.id, title = "Atrasada", deadline = yesterdayStr, priority = 3)
        val tUpcoming = PathTask(id = "tu", pathId = activePath.id, title = "Futura", scheduledDate = tomorrowStr, priority = 1)
        val tUndated = PathTask(id = "tnd", pathId = activePath.id, title = "Sem data", priority = 0)
        val tDone = PathTask(id = "td", pathId = activePath.id, title = "Concluída", completed = true, completedDate = todayStr)
        val tArchivedTask = PathTask(id = "tar", pathId = activePath.id, title = "Tarefa Arquivada", archived = true, scheduledDate = todayStr)
        val tInArchivedPath = PathTask(id = "tip", pathId = archivedPath.id, title = "De caminho arquivado", scheduledDate = todayStr)
        val tInCancelledPath = PathTask(id = "tic", pathId = cancelledPath.id, title = "De caminho cancelado", scheduledDate = todayStr)
        listOf(tToday, tOverdue, tUpcoming, tUndated, tDone, tArchivedTask, tInArchivedPath, tInCancelledPath).forEach {
            repo.saveTask(it)
        }

        val buckets = pendingBuckets(s(), todayStr)
        assertEquals(listOf("tt"), buckets.todayTasks.map { it.id })
        assertEquals(listOf("to"), buckets.overdueTasks.map { it.id })
        assertEquals(listOf("tu"), buckets.upcomingTasks.map { it.id })
        assertEquals(listOf("tnd"), buckets.undatedTasks.map { it.id })
        assertEquals(listOf("to", "tt"), buckets.highPriorityTasks.map { it.id })
        assertEquals(listOf("td"), buckets.completedTasks.map { it.id })

        // Time block overlaps and actual focus minutes comparison
        val b1 = TimeBlock(id = "b1", title = "Foco 1", date = todayStr, startTime = "09:00", endTime = "10:30", pathId = activePath.id, taskId = tToday.id)
        val b2 = TimeBlock(id = "b2", title = "Sobreposto", date = todayStr, startTime = "10:00", endTime = "11:00")
        val b3 = TimeBlock(id = "b3", title = "Adjacente sem conflito", date = todayStr, startTime = "11:00", endTime = "12:00")
        listOf(b1, b2, b3).forEach { repo.saveTimeBlock(it) }
        val overlaps = timeBlockOverlaps(s().timeBlocks)
        assertEquals(setOf("b1", "b2"), overlaps)

        repo.startPathFocus(pathId = activePath.id, taskId = tToday.id, minutes = 25)
        db.dao().focus(s().focus!!.copy(elapsed = 240, startedAt = 0))
        repo.endFocus(true)
        // Give the recorded fixture a real four-minute wall-clock window inside b1.
        val atNine = LocalDate.parse(todayStr).atTime(9, 0).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        db.dao().focusSession(s().focusSessions.single().copy(startedAt = atNine, endedAt = atNine + 240_000))
        repo.refresh()
        assertEquals(4L, timeBlockActualMinutes(s(), b1))

        // Agenda events grant 0 XP
        val ev = AgendaEvent(id = "ev1", title = "Consulta", date = todayStr, startTime = "14:00", endTime = "15:00", kind = "Reunião")
        repo.saveEvent(ev)
        repo.toggleEvent(ev.id)
        assertTrue(s().agendaEvents.single().completed)
        assertEquals(0L, s().xp)
        assertEquals(0L, s().coins)

        // Inbox capture and atomic triage to task (with rollback check) and to event
        val in1 = InboxItem(id = "in1", title = "Ideia estruturada", notes = "Detalhes")
        val in2 = InboxItem(id = "in2", title = "Compromisso avulso")
        repo.saveInboxItem(in1)
        repo.saveInboxItem(in2)
        assertEquals(2, s().inboxItems.size)

        db.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER fail_triage BEFORE DELETE ON inbox_items BEGIN SELECT RAISE(ABORT, 'triage rollback'); END"
        )
        try {
            val failed = runCatching {
                repo.triageInboxToTask(in1.id, PathTask(id = "triaged-t", pathId = activePath.id, title = "Ideia estruturada"), listOf("Passo 1", "Passo 2"))
            }
            assertTrue(failed.isFailure)
            assertTrue(s().inboxItems.any { it.id == in1.id })
            assertFalse(s().pathTasks.any { it.id == "triaged-t" })
        } finally {
            db.openHelper.writableDatabase.execSQL("DROP TRIGGER fail_triage")
        }

        repo.triageInboxToTask(in1.id, PathTask(id = "triaged-t", pathId = activePath.id, title = "Ideia estruturada"), listOf("Passo 1", "Passo 2"))
        assertFalse(s().inboxItems.any { it.id == in1.id })
        assertTrue(s().pathTasks.any { it.id == "triaged-t" })
        assertEquals(2, s().pathSubtasks.count { it.taskId == "triaged-t" })

        repo.triageInboxToEvent(in2.id, AgendaEvent(id = "triaged-ev", title = "Compromisso avulso", date = todayStr))
        assertFalse(s().inboxItems.any { it.id == in2.id })
        assertTrue(s().agendaEvents.any { it.id == "triaged-ev" })

        val rumos = rumosDoDia(s(), todayStr)
        assertEquals(1, rumos.overdueCount)
        assertTrue(rumos.pendingTodayCount >= 1)
        assertEquals(3, rumos.plannedBlocksCount)
    }

    @Test
    fun backupNineRoundTripCompatibilityAndAtomicRollback() = runBlocking {
        val path = PersonalPath(id = "bp1", name = "Backup Path", state = 1, deadline = LocalDate.now().plusDays(10).toString())
        val stage = PathStage(id = "bs1", pathId = path.id, name = "Etapa Backup", position = 0)
        val task = PathTask(id = "bt1", pathId = path.id, stageId = stage.id, title = "Tarefa Backup", priority = 2)
        val sub = PathSubtask(id = "bsub1", taskId = task.id, title = "Sub Backup")
        val block = TimeBlock(id = "bbl1", title = "Bloco Backup", pathId = path.id, taskId = task.id)
        val ev = AgendaEvent(id = "bev1", title = "Evento Backup")
        val inbox = InboxItem(id = "bin1", title = "Inbox Backup")
        repo.savePath(path)
        repo.saveStage(stage)
        repo.saveTask(task)
        repo.saveSubtask(sub)
        repo.linkHabitToPath(path.id, habit.id, true)
        repo.saveTimeBlock(block)
        repo.saveEvent(ev)
        repo.saveInboxItem(inbox)

        val before = s()
        val encoded = Backup.encode(before)
        assertTrue(encoded.contains("\"version\":10"))
        assertEquals(before, Backup.decode(encoded))

        // Backward compatibility with v8 envelope (without 1.7.0 fields)
        val gson = Gson()
        val obj = gson.toJsonTree(before).asJsonObject
        listOf("paths", "pathStages", "pathTasks", "pathSubtasks", "pathHabitLinks", "timeBlocks", "agendaEvents", "inboxItems").forEach {
            obj.remove(it)
        }
        val v8Payload = gson.toJson(obj)
        val v8Hash = MessageDigest.getInstance("SHA-256").digest(v8Payload.toByteArray()).joinToString("") { "%02x".format(it) }
        val v8Restored = Backup.decode(gson.toJson(BackupEnvelope(version = 8, sha256 = v8Hash, payload = v8Payload)))
        assertTrue(v8Restored.paths.isEmpty())
        assertEquals(before.habits, v8Restored.habits)

        // Invalid backup rejected before touching state
        val badState = before.copy(paths = listOf(path.copy(state = 99)))
        assertTrue(runCatching { Backup.decode(Backup.encode(badState)) }.isFailure)
        assertTrue(runCatching { repo.importState(badState) }.isFailure)
        assertEquals(before, s())

        // Rollback when DB trigger aborts mid-restore
        val incoming = before.copy(player = before.player!!.copy(name = "Novo Nome"))
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER fail_v9_restore BEFORE INSERT ON paths BEGIN SELECT RAISE(ABORT, 'abort'); END")
        try {
            assertTrue(runCatching { repo.importState(incoming) }.isFailure)
            val reload = GameRepository(db)
            reload.refresh()
            assertEquals(before, reload.state.value)
        } finally {
            db.openHelper.writableDatabase.execSQL("DROP TRIGGER fail_v9_restore")
        }

        repo.importState(Backup.decode(Backup.encode(incoming)))
        assertEquals(incoming, s())
    }

    @Test fun activeFocusKeepsItsSnapshotAcrossEditsMovesAndDeletes() = runBlocking {
        val a = PersonalPath(id = "a", name = "A")
        val b = PersonalPath(id = "b", name = "B")
        repo.savePath(a)
        repo.savePath(b)
        val task = PathTask(id = "task", title = "Trabalho")
        repo.saveTask(task)
        repo.startPathFocus(taskId = task.id, minutes = 60)
        repo.moveTask(task.id, a.id)
        db.dao().focus(s().focus!!.copy(elapsed = 180, startedAt = 0))
        repo.endFocus(true)
        assertEquals(3L, taskFocusMinutes(s(), task.id))
        assertEquals(0L, pathFocusMinutes(s(), a.id))
        assertTrue(runCatching { repo.startPathFocus(b.id, task.id, 60) }.isFailure)
        repo.startPathFocus(taskId = task.id, minutes = 60)
        repo.saveTask(s().pathTasks.single().copy(pathId = b.id))
        db.dao().focus(s().focus!!.copy(elapsed = 3600, startedAt = 0))
        repo.endFocus(true)
        assertEquals(60L, pathFocusMinutes(s(), a.id))
        assertEquals(0L, pathFocusMinutes(s(), b.id))
        repo.deletePath(a.id)
        assertEquals(0L, pathFocusMinutes(s(), b.id))
        assertEquals(63L, taskFocusMinutes(s(), task.id))
        repo.startPathFocus(taskId = task.id, minutes = 5)
        repo.deleteTask(task.id)
        repo.deletePath(b.id)
        val restored = Backup.decode(Backup.encode(s()))
        repo.importState(restored)
        assertEquals("Trabalho", s().focus!!.targetLabel)
        repo.endFocus(true)
        assertEquals(3, s().focusSessions.size)
        assertEquals(63L, s().focusSessions.sumOf { it.elapsedSeconds } / 60)
        assertEquals(0L, s().xp)
        assertEquals(0L, s().coins)
    }

    @Test fun closingWithOpenTasksRequiresCurrentConfirmationAndReopeningRestoresPending() = runBlocking {
        val path = PersonalPath(id = "a", name = "A")
        repo.savePath(path)
        val task = PathTask(id = "task", pathId = path.id, title = "Trabalho", scheduledDate = today())
        repo.saveTask(task)
        repo.setPathState(path.id, 2)
        assertTrue(agendaDay(s(), today()).tasks.isEmpty())
        assertTrue(runCatching { repo.setPathState(path.id, 3) }.isFailure)
        assertTrue(runCatching { repo.savePath(path.copy(state = 3)) }.isFailure)
        repo.setPathState(path.id, 3, setOf(task.id))
        assertTrue(pendingBuckets(s()).todayTasks.isEmpty())
        assertTrue(hallMemories(s()).any { it.category == "Caminhos" })
        assertFalse(s().pathTasks.single().completed)
        repo.setPathState(path.id, 1)
        assertEquals("", s().paths.single().completedDate)
        assertEquals(listOf(task.id), agendaDay(s(), today()).tasks.map { it.id })
        repo.toggleTask(task.id, true)
        repo.toggleTask(task.id, true)
        assertTrue(s().pathTasks.single().completed)
    }

    @Test fun triageAndStageEditsRejectInvalidWritesWithoutRemovingData() = runBlocking {
        val a = PersonalPath(id = "a", name = "A")
        val b = PersonalPath(id = "b", name = "B")
        repo.savePath(a)
        repo.savePath(b)
        val stage = PathStage(id = "stage", pathId = a.id, name = "Etapa")
        repo.saveStage(stage)
        val task = PathTask(id = "t", pathId = a.id, stageId = stage.id, title = "Original")
        repo.saveTask(task)
        assertTrue(runCatching { repo.saveStage(stage.copy(pathId = b.id)) }.isFailure)
        assertTrue(runCatching { repo.saveTask(task.copy(estimateMinutes = -1)) }.isFailure)
        val item = InboxItem(id = "incoming", title = "Ideia")
        repo.saveInboxItem(item)
        assertTrue(runCatching { repo.triageInboxToTask(item.id, task.copy(title = "Sobrescrita")) }.isFailure)
        assertEquals("Original", s().pathTasks.single().title)
        assertEquals(item.id, s().inboxItems.single().id)
        repo.triageInboxToEvent(item.id, AgendaEvent(title = "Agendado"))
        assertTrue(runCatching { repo.triageInboxToEvent(item.id, AgendaEvent(title = "Repetido")) }.isFailure)
        assertEquals(1, s().agendaEvents.size)
    }

    @Test fun activeSessionCannotOverwriteHistoryOnBackupRestore() = runBlocking {
        val path = PersonalPath(id = "a", name = "A")
        repo.savePath(path)
        repo.startPathFocus(pathId = path.id, minutes = 5)
        val active = s().focus!!
        repo.endFocus(true)
        val valid = s()
        val invalid = valid.copy(focus = active)
        assertTrue(runCatching { Backup.decode(Backup.encode(invalid)) }.isFailure)
        assertTrue(runCatching { repo.importState(invalid) }.isFailure)
        assertEquals(valid, s())
    }
}
