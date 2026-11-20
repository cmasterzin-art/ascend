package br.com.ascend.aetheris

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.DayOfWeek
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = AscendApp::class)
class OdinCouncilTest {
    private lateinit var ctx: Context
    private lateinit var db: GameDatabase
    private lateinit var repo: GameRepository

    @Before
    fun setup() {
        ctx = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(ctx, GameDatabase::class.java).allowMainThreadQueries().build()
        repo = GameRepository(db)
        runBlocking {
            repo.create(Player(name = "Astrid", heroClass = 1), emptyList())
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun s(): GameState = repo.state.value!!

    // =========================================================================
    // ETAPA 0: Persistência de favoritos do Codex Rúnico + regra conservadora foco
    // =========================================================================

    @Test
    fun etapa0_codexFavoritesPersistInDataStoreAndFocusRulePolicyDocumented() = runBlocking {
        val key = stringSetPreferencesKey("codex_favorite_runes")
        ctx.preferences.edit { it.remove(key) }
        ctx.preferences.edit { it[key] = setOf("Ansuz", "Raido") }
        val loaded = ctx.preferences.data.first()[key]
        assertEquals(setOf("Ansuz", "Raido"), loaded)

        val legacyQuest = Quest(id = "q-leg", name = "Legado", focusRuleVersion = 0)
        val modernQuest = Quest(id = "q-mod", name = "Moderno", focusRuleVersion = 1)
        assertTrue(expeditionFocusRulePolicy(legacyQuest).contains("legada", true))
        assertTrue(expeditionFocusRulePolicy(modernQuest).contains("300s", true))
    }

    // =========================================================================
    // PILAR 1: MESA DE ODIN — Disponibilidade, Determinismo, Estratégias, Apply/Undo, Replan
    // =========================================================================

    @Test
    fun odin_unconfiguredAvailabilityNeverAssumes24HoursFree() = runBlocking {
        val day = today()
        assertFalse(OdinPlanningEngine.hasConfiguredAvailability(s()))
        assertTrue(OdinPlanningEngine.freeIntervalsForDate(s(), day).isEmpty())

        repo.saveTask(PathTask(id = "t1", title = "Revisar capítulos", estimateMinutes = 60))
        val prop = OdinPlanningEngine.generateOdinProposal(s(), day, day)
        assertTrue(prop.suggestions.isEmpty())
        assertTrue(prop.unscheduled.any { it.id == "t1" })
        assertTrue(prop.conflicts.isNotEmpty())
    }

    @Test
    fun odin_respectsFixedEventsRestDaysExceptionsDailyLimitAndBuffers() = runBlocking {
        val mon = LocalDate.parse(today()).with(DayOfWeek.MONDAY).toString()
        val tue = LocalDate.parse(mon).plusDays(1).toString()
        val wed = LocalDate.parse(mon).plusDays(2).toString()

        repo.saveAvailabilityWindow(AvailabilityWindow(id = "w-mon", dayOfWeek = 1, startTime = "09:00", endTime = "12:00"))
        repo.saveAvailabilityWindow(AvailabilityWindow(id = "w-tue", dayOfWeek = 2, startTime = "09:00", endTime = "12:00"))
        repo.saveAvailabilityWindow(AvailabilityWindow(id = "w-wed", dayOfWeek = 3, startTime = "09:00", endTime = "12:00"))
        // Fixed event blocks 10:00..11:00 on Monday
        repo.saveEvent(AgendaEvent(id = "ev1", title = "Reunião fixa", date = mon, startTime = "10:00", endTime = "11:00"))
        // Tuesday blocked by AvailabilityException
        repo.saveAvailabilityException(AvailabilityException(id = "ex-tue", date = tue, blocked = true, note = "Feriado"))
        // Wednesday blocked by RestDay (ensure date >= player.created)
        db.dao().restDay(RestDay(wed))
        repo.refresh()

        val monFree = OdinPlanningEngine.freeIntervalsForDate(s(), mon)
        assertEquals(listOf(MinuteInterval(540, 600), MinuteInterval(660, 720)), monFree)
        assertTrue(OdinPlanningEngine.freeIntervalsForDate(s(), tue).isEmpty())
        assertTrue(OdinPlanningEngine.freeIntervalsForDate(s(), wed).isEmpty())

        // Task requiring 100 min must split cleanly across the two 60-min windows on Monday without colliding with 10:00..11:00
        repo.saveTask(PathTask(id = "t-split", title = "Projeto longo", estimateMinutes = 100, priority = 3))
        val prop = OdinPlanningEngine.generateOdinProposal(s(), mon, wed, strategyId = "strategist")
        assertTrue(prop.suggestions.isNotEmpty())
        val eventIv = MinuteInterval(600, 660)
        prop.suggestions.forEach { sug ->
            assertEquals(mon, sug.date)
            val iv = MinuteInterval(parseTimeToMinutes(sug.startTime)!!, parseTimeToMinutes(sug.endTime)!!)
            assertFalse("Suggestion overlapped fixed event!", iv.overlaps(eventIv))
        }
    }

    @Test
    fun odin_isDeterministicAndSixStrategiesDifferWithoutChangingRpgEconomy() = runBlocking {
        val start = today()
        val end = LocalDate.parse(start).plusDays(6).toString()
        val xpBefore = s().xp
        val coinsBefore = s().coins

        repo.applyDefaultAvailability()
        val studyPath = PersonalPath(id = "p-study", name = "Estudos", category = "Estudo", attribute = 0)
        val craftPath = PersonalPath(id = "p-craft", name = "Construção", category = "Criatividade", attribute = 4)
        repo.savePath(studyPath)
        repo.savePath(craftPath)
        repo.saveTask(PathTask(id = "ts1", pathId = studyPath.id, title = "Estudo profundo", estimateMinutes = 90, priority = 2, deadline = end))
        repo.saveTask(PathTask(id = "tc1", pathId = craftPath.id, title = "Protótipo criativo", estimateMinutes = 110, priority = 2, deadline = end))
        repo.saveTask(PathTask(id = "tu1", pathId = studyPath.id, title = "Entrega urgente", estimateMinutes = 45, priority = 3, deadline = start, urgent = true, important = true))

        val p1 = OdinPlanningEngine.generateOdinProposal(s(), start, end, strategyId = "strategist")
        val p2 = OdinPlanningEngine.generateOdinProposal(s(), start, end, strategyId = "strategist")
        assertEquals("Odin planner must be 100% deterministic", p1, p2)

        val byStrat = odinStrategies.associate { it.id to OdinPlanningEngine.generateOdinProposal(s(), start, end, strategyId = it.id) }
        assertEquals(6, byStrat.size)
        // Guardian vs Berserker must respect different load/buffer profiles or ordering
        assertNotEquals(byStrat["guardian"]!!.suggestions, byStrat["berserker"]!!.suggestions)
        // Changing strategy NEVER alters XP or coins
        repo.setOdinStrategy("berserker")
        assertEquals(xpBefore, s().xp)
        assertEquals(coinsBefore, s().coins)
    }

    @Test
    fun odin_transactionalApplyPartialRollbackAndSmartUndoPreservingManualEdits() = runBlocking {
        val start = LocalDate.parse(today()).with(DayOfWeek.MONDAY).toString()
        val end = LocalDate.parse(start).plusDays(4).toString()
        repo.applyDefaultAvailability()
        repo.saveTask(PathTask(id = "t-a", title = "Tarefa Alfa", estimateMinutes = 60, priority = 3))
        repo.saveTask(PathTask(id = "t-b", title = "Tarefa Beta", estimateMinutes = 60, priority = 2))

        val proposal = OdinPlanningEngine.generateOdinProposal(s(), start, end)
        assertTrue(proposal.suggestions.size >= 2)

        // Apply full proposal
        val applied = repo.applyOdinProposal(proposal)
        val createdBlocks = s().timeBlocks.filter { it.sourcePlanId == applied.id }
        assertEquals(proposal.suggestions.size, createdBlocks.size)

        // Repeated application of the EXACT same proposal must fail and rollback cleanly (conflict detection!)
        val countBeforeDup = s().timeBlocks.size
        assertTrue(runCatching { repo.applyOdinProposal(proposal) }.isFailure)
        assertEquals(countBeforeDup, s().timeBlocks.size)

        // Edit first block manually & pin it; keep second untouched
        val editedBlock = createdBlocks.first()
        repo.saveTimeBlock(editedBlock.copy(title = "Editado pelo usuário", pinned = true))
        assertTrue(s().timeBlocks.first { it.id == editedBlock.id }.modifiedManually)

        // Smart undo must delete untouched blocks and PRESERVE the manually edited block!
        val (removed, preserved) = repo.undoAppliedPlan(applied.id)
        assertEquals(1, preserved)
        assertEquals(createdBlocks.size - 1, removed)
        assertTrue(s().timeBlocks.any { it.id == editedBlock.id && it.title == "Editado pelo usuário" })
        assertTrue(s().appliedPlans.first { it.id == applied.id }.undone)
    }

    @Test
    fun odin_historySoftPreferenceRequiresMinimumFiveSessions() = runBlocking {
        assertNull(OdinPlanningEngine.preferredBucketFromHistory(s()))
        val baseTs = java.time.ZonedDateTime.of(LocalDate.parse(today()), java.time.LocalTime.of(19, 30), java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        repeat(4) { i ->
            db.dao().focusSession(FocusSession("fs-$i", targetLabel = "Estudo", date = today(), startedAt = baseTs, endedAt = baseTs + 1800_000L, elapsedSeconds = 1800L, plannedMinutes = 30, status = "completed", registered = true))
        }
        repo.refresh()
        assertNull("Must not activate below minimum threshold of 5 sessions", OdinPlanningEngine.preferredBucketFromHistory(s()))

        db.dao().focusSession(FocusSession("fs-4", targetLabel = "Estudo", date = today(), startedAt = baseTs, endedAt = baseTs + 1800_000L, elapsedSeconds = 1800L, plannedMinutes = 30, status = "completed", registered = true))
        repo.refresh()
        assertEquals("evening", OdinPlanningEngine.preferredBucketFromHistory(s()))
    }

    @Test
    fun odin_capacitySimulatorDeduplicatesLinkedBlocksAndFlagsUnestimatedAndDeadlines() = runBlocking {
        val mon = LocalDate.parse(today()).with(DayOfWeek.MONDAY).toString()
        repo.saveAvailabilityWindow(AvailabilityWindow(id = "w-m", dayOfWeek = 1, startTime = "09:00", endTime = "11:00")) // 120 min
        repo.saveTask(PathTask(id = "t-linked", title = "Tarefa vinculada", estimateMinutes = 60, deadline = mon))
        repo.saveTask(PathTask(id = "t-unest", title = "Sem estimativa", estimateMinutes = 0, deadline = mon))
        repo.saveTask(PathTask(id = "t-huge", title = "Impossível hoje", estimateMinutes = 240, deadline = mon))
        // Link a 60-min block to t-linked -> remaining demand for t-linked must be 0 (NO DOUBLE COUNTING!)
        repo.saveTimeBlock(TimeBlock(id = "b-linked", title = "Bloco vinculado", date = mon, startTime = "09:00", endTime = "10:00", taskId = "t-linked"))

        val sim = OdinPlanningEngine.simulateCapacity(s(), CapacityPeriodKind.TODAY, todayDate = mon)
        assertEquals(120, sim.configuredAvailableMinutes)
        assertEquals(60, sim.existingBlocksMinutes)
        assertEquals(240, sim.pendingTasksDemandMinutes) // ONLY t-huge (240), NOT 240 + 60!
        assertEquals(1, sim.unestimatedTasks.size)
        assertEquals("t-unest", sim.unestimatedTasks.first().id)
        assertTrue(sim.incompatibleDeadlineTasks.any { it.id == "t-huge" })
        assertEquals(CapacityStatus.OVERLOADED, sim.status)

        // All 5 period kinds resolve cleanly
        CapacityPeriodKind.entries.forEach { k ->
            val res = OdinPlanningEngine.simulateCapacity(s(), k, todayDate = mon)
            assertTrue(res.dateTo >= res.dateFrom)
        }
    }

    @Test
    fun odin_adaptiveReplanningNeverAutoAppliesAndAppliesWithoutRpgPenalty() = runBlocking {
        val yesterday = LocalDate.parse(today()).minusDays(1).toString()
        val xpBefore = s().xp
        val coinsBefore = s().coins
        repo.applyDefaultAvailability()
        repo.saveTask(PathTask(id = "t-miss", title = "Leitura atrasada", estimateMinutes = 40, scheduledDate = yesterday))
        repo.saveTimeBlock(TimeBlock(id = "b-miss", title = "Leitura atrasada", date = yesterday, startTime = "09:00", endTime = "09:40", taskId = "t-miss"))

        val items = OdinPlanningEngine.generateReplanProposal(s(), today())
        assertEquals(1, items.size)
        assertTrue(items.first().options.any { it.action == ReplanActionKind.TRANSFER })
        assertTrue(items.first().options.any { it.action == ReplanActionKind.SEND_TO_PENDING })
        assertTrue(items.first().options.any { it.action == ReplanActionKind.REMOVE_SCHEDULE })

        // Choosing TRANSFER moves the block without RPG penalty
        val transfer = items.first().options.first { it.action == ReplanActionKind.TRANSFER }
        repo.applyReplanChoice(items.first(), transfer)
        assertEquals(transfer.targetDate, s().timeBlocks.first { it.id == "b-miss" }.date)
        assertEquals(xpBefore, s().xp)
        assertEquals(coinsBefore, s().coins)
    }

    // =========================================================================
    // PILAR 2: CAMINHOS 3.0 — Dependências, Ciclos, CPM, Kanban, Marcos, 6 Modelos
    // =========================================================================

    @Test
    fun caminhos30_dependenciesCyclesOverrideCpmKanbanMilestonesAndTemplates() = runBlocking {
        repo.applyDefaultAvailability()
        val p = PersonalPath(id = "p-cpm", name = "Projeto CPM", deadline = LocalDate.parse(today()).plusDays(5).toString())
        repo.savePath(p)
        repo.saveTask(PathTask(id = "t1", pathId = p.id, title = "Base", estimateMinutes = 60))
        repo.saveTask(PathTask(id = "t2", pathId = p.id, title = "Núcleo", estimateMinutes = 120))
        repo.saveTask(PathTask(id = "t3", pathId = p.id, title = "Paralela curta", estimateMinutes = 30))
        repo.saveTask(PathTask(id = "t4", pathId = p.id, title = "Fechamento", estimateMinutes = 60))

        // Self-dependency forbidden
        assertTrue(runCatching { repo.addTaskDependency("t1", "t1") }.isFailure)

        // Valid DAG: t1 -> t2 -> t4 and t1 -> t3 -> t4
        repo.addTaskDependency("t2", "t1")
        repo.addTaskDependency("t3", "t1")
        repo.addTaskDependency("t4", "t2")
        repo.addTaskDependency("t4", "t3")

        // Cycle t1 -> t4 forbidden!
        assertTrue(runCatching { repo.addTaskDependency("t1", "t4") }.isFailure)

        // Workflow states
        val map = s().pathTasks.associateBy { it.id }
        assertEquals(TaskWorkflowState.AVAILABLE, OdinPlanningEngine.taskWorkflowState(map["t1"]!!, map, s().taskDependencies))
        assertEquals(TaskWorkflowState.BLOCKED, OdinPlanningEngine.taskWorkflowState(map["t2"]!!, map, s().taskDependencies))

        // Moving blocked task to Kanban "doing" without override reason fails; with reason succeeds and records override!
        assertTrue(runCatching { repo.setTaskKanbanColumn("t2", "doing") }.isFailure)
        repo.setTaskKanbanColumn("t2", "doing", "Protótipo adiantado pelo cliente")
        assertEquals(1, s().pathTasks.first { it.id == "t2" }.workflowStatus)
        assertTrue(s().taskDependencies.first { it.taskId == "t2" && it.dependsOnTaskId == "t1" }.overridden)

        // CPM on fresh DAG (re-enable dependency to verify exact critical path t1->t2->t4 = 240 min)
        val p2 = PersonalPath(id = "p-cpm2", name = "CPM Puro")
        repo.savePath(p2)
        repo.saveTask(PathTask(id = "c1", pathId = p2.id, title = "C1", estimateMinutes = 60))
        repo.saveTask(PathTask(id = "c2", pathId = p2.id, title = "C2", estimateMinutes = 120))
        repo.saveTask(PathTask(id = "c3", pathId = p2.id, title = "C3", estimateMinutes = 30))
        repo.saveTask(PathTask(id = "c4", pathId = p2.id, title = "C4", estimateMinutes = 60))
        repo.addTaskDependency("c2", "c1")
        repo.addTaskDependency("c3", "c1")
        repo.addTaskDependency("c4", "c2")
        repo.addTaskDependency("c4", "c3")
        val cpm = OdinPlanningEngine.computeCriticalPath(p2, s(), today())
        assertFalse(cpm.hasCycle)
        assertEquals(240, cpm.totalCriticalMinutes)
        assertEquals(listOf("c1", "c2", "c4"), cpm.criticalSequence.map { it.task.id })
        assertEquals(90, cpm.nodes.first { it.task.id == "c3" }.slackMinutes)
        assertNotNull(cpm.effortProjectedDate)
        assertNotNull(cpm.availabilityProjectedDate)

        // Deleting a task removes its dependencies coherently
        repo.deleteTask("c3")
        assertTrue(s().taskDependencies.none { it.taskId == "c3" || it.dependsOnTaskId == "c3" })

        // Milestones & all 6 reusable templates (never copy completed history!)
        repo.savePathMilestone(PathMilestone(id = "m1", pathId = p.id, title = "Marco 1", targetDate = today()))
        repo.togglePathMilestone("m1")
        assertTrue(s().pathMilestones.first { it.id == "m1" }.completed)

        assertEquals(6, projectTemplates.size)
        val createdId = repo.createPathFromTemplate("tpl-app", "Meu App")
        val tplTasks = s().pathTasks.filter { it.pathId == createdId }
        assertTrue(tplTasks.isNotEmpty())
        assertTrue(tplTasks.none { it.completed })
        assertTrue(s().pathMilestones.any { it.pathId == createdId && !it.completed })
        assertTrue(s().taskDependencies.any { d -> tplTasks.any { it.id == d.taskId } })
    }

    // =========================================================================
    // PILAR 3: AGENDA 3.0 — Fixação & Recorrências
    // =========================================================================

    @Test
    fun agenda30_pinningAndRecurrenceExpansionAndSingleExclusion() = runBlocking {
        val start = "2026-06-01" // Monday
        val rule = RecurrenceRule(
            id = "rec-1",
            entityType = "block",
            templateId = "blk-rec",
            pattern = "weekdays",
            daysOfWeek = "1,3,5",
            startDate = start,
            endDate = "2026-06-14"
        )
        repo.saveRecurrenceRule(rule)
        val occ1 = OdinPlanningEngine.expandRecurrenceOccurrences(s().recurrenceRules.first(), "2026-06-01", "2026-06-14")
        assertEquals(listOf("2026-06-01", "2026-06-03", "2026-06-05", "2026-06-08", "2026-06-10", "2026-06-12"), occ1)

        // Exclude single occurrence without touching earlier dates
        repo.excludeRecurrenceDate("rec-1", "2026-06-05")
        val occ2 = OdinPlanningEngine.expandRecurrenceOccurrences(s().recurrenceRules.first(), "2026-06-01", "2026-06-14")
        assertFalse("2026-06-05" in occ2)
        assertTrue("2026-06-01" in occ2)
        assertTrue("2026-06-08" in occ2)
    }

    // =========================================================================
    // PILAR 4: FORJA DO TEMPO — Pomodoro, Pausas nunca somam foco, Bloco, Anomalia, Sons
    // =========================================================================

    @Test
    fun timeForge_pomodoroNeverCountsBreaksAndLinksToBlockAndSynthesizesAudio() = runBlocking {
        val h = Habit(id = "h-focus", name = "Estudar", type = 2, target = 60, unit = "min")
        repo.habit(h)
        val blk = TimeBlock(id = "blk-focus", title = "Bloco de Estudo", date = today(), startTime = "09:00", endTime = "10:00", habitId = h.id)
        repo.saveTimeBlock(blk)

        repo.startTimeForgeFocus(
            mode = "pomodoro",
            workMinutes = 25,
            shortBreakMinutes = 5,
            longBreakMinutes = 15,
            totalCycles = 4,
            blockId = blk.id
        )
        val active = s().focus!!
        assertEquals("pomodoro", active.mode)
        assertEquals("work", active.phase)
        assertEquals(blk.id, active.blockId)

        // Simulate 1500s work elapsed & advance into short_break
        db.dao().focus(active.copy(elapsed = 1500L, startedAt = 0L))
        repo.refresh()
        repo.advanceFocusPhase()

        val breakFocus = s().focus!!
        assertEquals("short_break", breakFocus.phase)
        assertEquals(1, s().focusSessions.size)
        assertEquals(1500L, s().focusSessions.first().elapsedSeconds)
        assertEquals(blk.id, s().focusSessions.first().blockId)
        assertEquals(25, s().record(h.id, today())?.progress)

        // Simulate 300s in break and end -> MUST NOT create FocusSession or add habit minutes!
        db.dao().focus(breakFocus.copy(elapsed = 300L, startedAt = 0L))
        repo.refresh()
        repo.endFocus(register = true)
        assertNull(s().focus)
        assertEquals("Break must NEVER create a FocusSession", 1, s().focusSessions.size)
        assertEquals("Break must NEVER add habit minutes", 25, s().record(h.id, today())?.progress)

        // Clock anomaly detection when wall clock jumps backwards
        val anomalyFocus = Focus(startedAt = 1_000_000L, startedRealtimeMs = 50_000L)
        assertTrue(anomalyFocus.hasClockAnomaly(now = 900_000L, nowRealtimeMs = 60_000L))
        assertEquals(10L, anomalyFocus.seconds(now = 900_000L, nowRealtimeMs = 60_000L))

        // Procedural ambient audio synthesis produces non-silent PCM for all 3 scenes and silence for off
        listOf("rain", "hearth", "wind").forEach { scene ->
            val pcm = AmbientAudioEngine.synthesizeFrame(scene, 512)
            assertTrue("Scene $scene should produce non-zero waveform", pcm.any { it.toInt() != 0 })
        }
        assertTrue(AmbientAudioEngine.synthesizeFrame("off", 128).all { it.toInt() == 0 })
    }

    // =========================================================================
    // PILAR 5: CONSELHO DOS NOVE — Áreas opcionais & Objetivos com progresso real
    // =========================================================================

    @Test
    fun councilOfNine_areasAndStrategicGoalsComputeVerifiableProgressWithoutRpgEconomy() = runBlocking {
        val xpBefore = s().xp
        val coinsBefore = s().coins
        repo.seedDefaultLifeAreas()
        assertEquals(9, s().lifeAreas.size)

        val p = PersonalPath(id = "p-goal", name = "Domínio Técnico", areaId = "area-estudos")
        repo.savePath(p)
        repo.saveTask(PathTask(id = "tg1", pathId = p.id, title = "Módulo 1", completed = true, completedDate = today()))
        repo.saveTask(PathTask(id = "tg2", pathId = p.id, title = "Módulo 2", completed = false))

        val g = StrategicGoal(id = "g1", title = "Concluir trilha técnica", areaId = "area-estudos", horizon = "quarterly", linkedPathIds = p.id)
        repo.saveStrategicGoal(g)
        val (pct, detail) = strategicGoalProgress(s().strategicGoals.first(), s(), today())
        assertEquals(50, pct)
        assertTrue(detail.contains("50%"))

        // Deleting a life area unlinks items cleanly without deleting the path
        repo.deleteLifeArea("area-estudos")
        assertEquals(8, s().lifeAreas.size)
        assertEquals("", s().paths.first { it.id == p.id }.areaId)
        assertEquals(xpBefore, s().xp)
        assertEquals(coinsBefore, s().coins)
    }

    // =========================================================================
    // MIGRAÇÃO ROOM v10 → v11 & BACKUP v13 (compat v12 + validação + rollback)
    // =========================================================================

    @Test
    fun roomMigration10To11PreservesAllDataAndCreatesStrategicTables() = runBlocking {
        val dbName = "ascend_v10_to_v11_${System.nanoTime()}.db"
        ctx.deleteDatabase(dbName)
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(ctx)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(10) {
                    override fun onCreate(sq: SupportSQLiteDatabase) {
                        // Create minimal v10 schema from real migrations 1..10
                        sq.execSQL("CREATE TABLE `player` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `heroClass` INTEGER NOT NULL, `color` INTEGER NOT NULL, `face` INTEGER NOT NULL, `outfit` INTEGER NOT NULL, `frame` INTEGER NOT NULL, `title` TEXT NOT NULL, `goals` TEXT NOT NULL, `created` TEXT NOT NULL, `specialization` TEXT NOT NULL DEFAULT '', PRIMARY KEY(`id`))")
                        sq.execSQL("CREATE TABLE `habits` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `icon` INTEGER NOT NULL, `color` INTEGER NOT NULL, `category` TEXT NOT NULL, `attribute` INTEGER NOT NULL, `difficulty` INTEGER NOT NULL, `type` INTEGER NOT NULL, `target` INTEGER NOT NULL, `unit` TEXT NOT NULL, `days` TEXT NOT NULL, `time` TEXT NOT NULL, `xp` INTEGER NOT NULL, `coins` INTEGER NOT NULL, `points` INTEGER NOT NULL, `proportional` INTEGER NOT NULL, `reminder` INTEGER NOT NULL, `paused` INTEGER NOT NULL, `archived` INTEGER NOT NULL, `deleted` INTEGER NOT NULL, `position` INTEGER NOT NULL, `created` TEXT NOT NULL, `scheduleHistory` TEXT NOT NULL, `templateId` TEXT NOT NULL DEFAULT '', `narrativeName` TEXT NOT NULL DEFAULT '', `trackStreak` INTEGER NOT NULL DEFAULT 1, PRIMARY KEY(`id`))")
                        sq.execSQL("CREATE TABLE `records` (`id` TEXT NOT NULL, `habitId` TEXT NOT NULL, `date` TEXT NOT NULL, `progress` INTEGER NOT NULL, `target` INTEGER NOT NULL, `xp` INTEGER NOT NULL, `coins` INTEGER NOT NULL, `points` INTEGER NOT NULL, `attribute` INTEGER NOT NULL, `proportional` INTEGER NOT NULL, `type` INTEGER NOT NULL, `category` TEXT NOT NULL, `unit` TEXT NOT NULL, `name` TEXT NOT NULL, `scheduled` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
                        sq.execSQL("CREATE INDEX `index_records_habitId` ON `records` (`habitId`)")
                        sq.execSQL("CREATE TABLE `ledger` (`id` TEXT NOT NULL, `xp` INTEGER NOT NULL, `coins` INTEGER NOT NULL, `points` INTEGER NOT NULL, `attribute` INTEGER NOT NULL, `date` TEXT NOT NULL, `label` TEXT NOT NULL, PRIMARY KEY(`id`))")
                        sq.execSQL("CREATE TABLE `quests` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `period` INTEGER NOT NULL, `metric` INTEGER NOT NULL, `target` INTEGER NOT NULL, `habitIds` TEXT NOT NULL, `xp` INTEGER NOT NULL, `coins` INTEGER NOT NULL, `deadline` TEXT NOT NULL, `special` TEXT NOT NULL, `attribute` INTEGER NOT NULL, `sagaStart` TEXT NOT NULL DEFAULT '', `sagaBaseline` TEXT NOT NULL DEFAULT '', `sagaState` INTEGER NOT NULL DEFAULT 0, `sagaEnd` TEXT NOT NULL DEFAULT '', `sagaStopped` INTEGER NOT NULL DEFAULT 0, `focusRuleVersion` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`id`))")
                        sq.execSQL("CREATE TABLE `claims` (`id` TEXT NOT NULL, `questId` TEXT NOT NULL, `occurrence` TEXT NOT NULL, `date` TEXT NOT NULL, `xp` INTEGER NOT NULL, `coins` INTEGER NOT NULL, `target` INTEGER NOT NULL, `metric` INTEGER NOT NULL, `habitIds` TEXT NOT NULL, `period` INTEGER NOT NULL, `attribute` INTEGER NOT NULL, `deadline` TEXT NOT NULL, `sagaStart` TEXT NOT NULL DEFAULT '', `sagaBaseline` TEXT NOT NULL DEFAULT '', `focusRuleVersion` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`id`))")
                        sq.execSQL("CREATE TABLE `assets` (`id` TEXT NOT NULL, `kind` TEXT NOT NULL, `stage` INTEGER NOT NULL, `equipped` INTEGER NOT NULL, `date` TEXT NOT NULL, `memorySpeaker` TEXT NOT NULL DEFAULT '', `memoryText` TEXT NOT NULL DEFAULT '', PRIMARY KEY(`id`))")
                        sq.execSQL("CREATE TABLE `achievements` (`id` TEXT NOT NULL, `date` TEXT NOT NULL, PRIMARY KEY(`id`))")
                        sq.execSQL("CREATE TABLE `diary` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `text` TEXT NOT NULL, `date` TEXT NOT NULL, PRIMARY KEY(`id`))")
                        sq.execSQL("CREATE TABLE `routines` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `habitIds` TEXT NOT NULL, `time` TEXT NOT NULL, PRIMARY KEY(`id`))")
                        sq.execSQL("CREATE TABLE `rest_days` (`date` TEXT NOT NULL, PRIMARY KEY(`date`))")
                        sq.execSQL("CREATE TABLE `plan_exceptions` (`id` TEXT NOT NULL, `habitId` TEXT NOT NULL, `date` TEXT NOT NULL, `include` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
                        sq.execSQL("CREATE INDEX `index_plan_exceptions_habitId` ON `plan_exceptions` (`habitId`)")
                        sq.execSQL("CREATE TABLE `focus` (`id` INTEGER NOT NULL, `habitId` TEXT NOT NULL, `date` TEXT NOT NULL, `duration` INTEGER NOT NULL, `elapsed` INTEGER NOT NULL, `startedAt` INTEGER NOT NULL, `baseline` INTEGER NOT NULL, `sessionId` TEXT NOT NULL DEFAULT '', `beganAt` INTEGER NOT NULL DEFAULT 0, `pathId` TEXT NOT NULL DEFAULT '', `taskId` TEXT NOT NULL DEFAULT '', `targetLabel` TEXT NOT NULL DEFAULT '', PRIMARY KEY(`id`))")
                        sq.execSQL("CREATE INDEX `index_focus_habitId` ON `focus` (`habitId`)")
                        sq.execSQL("CREATE INDEX `index_focus_pathId` ON `focus` (`pathId`)")
                        sq.execSQL("CREATE INDEX `index_focus_taskId` ON `focus` (`taskId`)")
                        sq.execSQL("CREATE TABLE `focus_sessions` (`id` TEXT NOT NULL, `habitId` TEXT NOT NULL, `habitName` TEXT NOT NULL, `date` TEXT NOT NULL, `startedAt` INTEGER NOT NULL, `endedAt` INTEGER NOT NULL, `elapsedSeconds` INTEGER NOT NULL, `plannedMinutes` INTEGER NOT NULL, `status` TEXT NOT NULL, `registered` INTEGER NOT NULL, `pathId` TEXT NOT NULL DEFAULT '', `taskId` TEXT NOT NULL DEFAULT '', `targetLabel` TEXT NOT NULL DEFAULT '', PRIMARY KEY(`id`))")
                        sq.execSQL("CREATE INDEX `index_focus_sessions_habitId` ON `focus_sessions` (`habitId`)")
                        sq.execSQL("CREATE INDEX `index_focus_sessions_pathId` ON `focus_sessions` (`pathId`)")
                        sq.execSQL("CREATE INDEX `index_focus_sessions_taskId` ON `focus_sessions` (`taskId`)")
                        sq.execSQL("CREATE TABLE `paths` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `category` TEXT NOT NULL, `icon` INTEGER NOT NULL, `color` INTEGER NOT NULL, `startDate` TEXT NOT NULL, `deadline` TEXT NOT NULL, `priority` INTEGER NOT NULL, `state` INTEGER NOT NULL, `attribute` INTEGER NOT NULL, `criteria` TEXT NOT NULL, `notes` TEXT NOT NULL, `completedDate` TEXT NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                        sq.execSQL("CREATE TABLE `path_stages` (`id` TEXT NOT NULL, `pathId` TEXT NOT NULL, `name` TEXT NOT NULL, `targetNote` TEXT NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`pathId`) REFERENCES `paths`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
                        sq.execSQL("CREATE INDEX `index_path_stages_pathId` ON `path_stages` (`pathId`)")
                        sq.execSQL("CREATE TABLE `path_tasks` (`id` TEXT NOT NULL, `pathId` TEXT NOT NULL, `stageId` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `priority` INTEGER NOT NULL, `deadline` TEXT NOT NULL, `scheduledDate` TEXT NOT NULL, `scheduledTime` TEXT NOT NULL, `estimateMinutes` INTEGER NOT NULL, `completed` INTEGER NOT NULL, `completedDate` TEXT NOT NULL, `archived` INTEGER NOT NULL, `position` INTEGER NOT NULL, `notes` TEXT NOT NULL, `created` TEXT NOT NULL, PRIMARY KEY(`id`))")
                        sq.execSQL("CREATE INDEX `index_path_tasks_pathId` ON `path_tasks` (`pathId`)")
                        sq.execSQL("CREATE INDEX `index_path_tasks_stageId` ON `path_tasks` (`stageId`)")
                        sq.execSQL("CREATE INDEX `index_path_tasks_scheduledDate` ON `path_tasks` (`scheduledDate`)")
                        sq.execSQL("CREATE INDEX `index_path_tasks_deadline` ON `path_tasks` (`deadline`)")
                        sq.execSQL("CREATE TABLE `path_subtasks` (`id` TEXT NOT NULL, `taskId` TEXT NOT NULL, `title` TEXT NOT NULL, `done` INTEGER NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`taskId`) REFERENCES `path_tasks`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
                        sq.execSQL("CREATE INDEX `index_path_subtasks_taskId` ON `path_subtasks` (`taskId`)")
                        sq.execSQL("CREATE TABLE `path_habit_links` (`id` TEXT NOT NULL, `pathId` TEXT NOT NULL, `habitId` TEXT NOT NULL, `created` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`pathId`) REFERENCES `paths`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
                        sq.execSQL("CREATE INDEX `index_path_habit_links_pathId` ON `path_habit_links` (`pathId`)")
                        sq.execSQL("CREATE INDEX `index_path_habit_links_habitId` ON `path_habit_links` (`habitId`)")
                        sq.execSQL("CREATE TABLE `time_blocks` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `date` TEXT NOT NULL, `startTime` TEXT NOT NULL, `endTime` TEXT NOT NULL, `category` TEXT NOT NULL, `color` INTEGER NOT NULL, `description` TEXT NOT NULL, `pathId` TEXT NOT NULL, `taskId` TEXT NOT NULL, `habitId` TEXT NOT NULL, PRIMARY KEY(`id`))")
                        sq.execSQL("CREATE INDEX `index_time_blocks_date` ON `time_blocks` (`date`)")
                        sq.execSQL("CREATE INDEX `index_time_blocks_pathId` ON `time_blocks` (`pathId`)")
                        sq.execSQL("CREATE INDEX `index_time_blocks_taskId` ON `time_blocks` (`taskId`)")
                        sq.execSQL("CREATE INDEX `index_time_blocks_habitId` ON `time_blocks` (`habitId`)")
                        sq.execSQL("CREATE TABLE `agenda_events` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `date` TEXT NOT NULL, `startTime` TEXT NOT NULL, `endTime` TEXT NOT NULL, `kind` TEXT NOT NULL, `description` TEXT NOT NULL, `completed` INTEGER NOT NULL, `archived` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                        sq.execSQL("CREATE INDEX `index_agenda_events_date` ON `agenda_events` (`date`)")
                        sq.execSQL("CREATE TABLE `inbox_items` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `notes` TEXT NOT NULL, `created` TEXT NOT NULL, `archived` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                        sq.execSQL("CREATE TABLE `knowledge_notebooks` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `category` TEXT NOT NULL, `icon` INTEGER NOT NULL, `cover` INTEGER NOT NULL, `pathId` TEXT NOT NULL, `archived` INTEGER NOT NULL, `created` TEXT NOT NULL, `updated` TEXT NOT NULL, PRIMARY KEY(`id`))")
                        sq.execSQL("CREATE INDEX `index_knowledge_notebooks_pathId` ON `knowledge_notebooks` (`pathId`)")
                        sq.execSQL("CREATE TABLE `knowledge_pages` (`id` TEXT NOT NULL, `notebookId` TEXT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `tags` TEXT NOT NULL, `favorite` INTEGER NOT NULL, `archived` INTEGER NOT NULL, `pathId` TEXT NOT NULL, `taskId` TEXT NOT NULL, `created` TEXT NOT NULL, `updated` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`notebookId`) REFERENCES `knowledge_notebooks`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
                        sq.execSQL("CREATE INDEX `index_knowledge_pages_notebookId` ON `knowledge_pages` (`notebookId`)")
                        sq.execSQL("CREATE INDEX `index_knowledge_pages_pathId` ON `knowledge_pages` (`pathId`)")
                        sq.execSQL("CREATE INDEX `index_knowledge_pages_taskId` ON `knowledge_pages` (`taskId`)")
                        sq.execSQL("CREATE TABLE `knowledge_page_links` (`id` TEXT NOT NULL, `sourcePageId` TEXT NOT NULL, `targetPageId` TEXT NOT NULL, `created` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`sourcePageId`) REFERENCES `knowledge_pages`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`targetPageId`) REFERENCES `knowledge_pages`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
                        sq.execSQL("CREATE INDEX `index_knowledge_page_links_sourcePageId` ON `knowledge_page_links` (`sourcePageId`)")
                        sq.execSQL("CREATE INDEX `index_knowledge_page_links_targetPageId` ON `knowledge_page_links` (`targetPageId`)")
                        sq.execSQL("CREATE TABLE `knowledge_references` (`id` TEXT NOT NULL, `pageId` TEXT NOT NULL, `author` TEXT NOT NULL, `title` TEXT NOT NULL, `year` TEXT NOT NULL, `source` TEXT NOT NULL, `url` TEXT NOT NULL, `location` TEXT NOT NULL, `notes` TEXT NOT NULL, `created` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`pageId`) REFERENCES `knowledge_pages`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
                        sq.execSQL("CREATE INDEX `index_knowledge_references_pageId` ON `knowledge_references` (`pageId`)")
                        sq.execSQL("CREATE TABLE `flashcards` (`id` TEXT NOT NULL, `notebookId` TEXT NOT NULL, `pageId` TEXT NOT NULL, `question` TEXT NOT NULL, `answer` TEXT NOT NULL, `tags` TEXT NOT NULL, `repetitions` INTEGER NOT NULL, `intervalDays` INTEGER NOT NULL, `easeFactor` REAL NOT NULL, `nextReviewDate` TEXT NOT NULL, `lastReviewDate` TEXT NOT NULL, `archived` INTEGER NOT NULL, `created` TEXT NOT NULL, `updated` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`notebookId`) REFERENCES `knowledge_notebooks`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
                        sq.execSQL("CREATE INDEX `index_flashcards_notebookId` ON `flashcards` (`notebookId`)")
                        sq.execSQL("CREATE INDEX `index_flashcards_pageId` ON `flashcards` (`pageId`)")
                        sq.execSQL("CREATE INDEX `index_flashcards_nextReviewDate` ON `flashcards` (`nextReviewDate`)")
                        sq.execSQL("CREATE TABLE `flashcard_reviews` (`id` TEXT NOT NULL, `flashcardId` TEXT NOT NULL, `reviewDate` TEXT NOT NULL, `rating` INTEGER NOT NULL, `intervalBefore` INTEGER NOT NULL, `intervalAfter` INTEGER NOT NULL, `easeAfter` REAL NOT NULL, `reviewedAt` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`flashcardId`) REFERENCES `flashcards`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
                        sq.execSQL("CREATE INDEX `index_flashcard_reviews_flashcardId` ON `flashcard_reviews` (`flashcardId`)")
                        sq.execSQL("CREATE INDEX `index_flashcard_reviews_reviewDate` ON `flashcard_reviews` (`reviewDate`)")
                        sq.execSQL("CREATE TABLE `equipment_loadouts` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `headId` TEXT NOT NULL, `outfitId` TEXT NOT NULL, `accessoryId` TEXT NOT NULL, `weaponId` TEXT NOT NULL, `companionId` TEXT NOT NULL, `frameId` TEXT NOT NULL, `backgroundId` TEXT NOT NULL, `updated` TEXT NOT NULL, PRIMARY KEY(`id`))")
                        sq.execSQL("CREATE TABLE `heraldry_profile` (`id` INTEGER NOT NULL, `symbolId` TEXT NOT NULL, `shapeId` TEXT NOT NULL, `metalId` TEXT NOT NULL, `secondaryColorId` TEXT NOT NULL, `inscriptionRunes` TEXT NOT NULL, `ornamentId` TEXT NOT NULL, `bannerId` TEXT NOT NULL, `showOnPortrait` INTEGER NOT NULL, `updated` TEXT NOT NULL, PRIMARY KEY(`id`))")
                        sq.execSQL("INSERT INTO `player` VALUES (1, 'Freyja', 1, 1, 0, 0, 0, 'O Desperto', '', '${today()}', '')")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                }).build()
        )
        helper.writableDatabase.close()
        helper.close()

        val migrated = Room.databaseBuilder(ctx, GameDatabase::class.java, dbName)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
            .allowMainThreadQueries()
            .build()
        try {
            val r = GameRepository(migrated)
            r.refresh()
            assertEquals("Freyja", r.state.value!!.player!!.name)
            r.applyDefaultAvailability()
            r.seedDefaultLifeAreas()
            assertEquals(9, r.state.value!!.lifeAreas.size)
        } finally {
            migrated.close()
            ctx.deleteDatabase(dbName)
        }
    }

    @Test
    fun backupV13RoundTripAndCycleRejectionAreAtomic() = runBlocking {
        repo.applyDefaultAvailability()
        repo.seedDefaultLifeAreas()
        repo.setOdinStrategy("scholar")
        val p = PersonalPath(id = "p-bkp", name = "Projeto Backup", areaId = "area-estudos")
        repo.savePath(p)
        repo.saveTask(PathTask(id = "tb1", pathId = p.id, title = "Passo 1", estimateMinutes = 45, important = true, urgent = true))
        repo.saveTask(PathTask(id = "tb2", pathId = p.id, title = "Passo 2", estimateMinutes = 60))
        repo.addTaskDependency("tb2", "tb1")
        repo.savePathMilestone(PathMilestone(id = "ms1", pathId = p.id, title = "Entrega", targetDate = today()))
        repo.saveStrategicGoal(StrategicGoal(id = "sg1", title = "Meta anual", areaId = "area-estudos", horizon = "annual", linkedPathIds = p.id))

        val before = s()
        val encoded = Backup.encode(before)
        assertTrue(encoded.contains("\"version\":13"))
        val decoded = Backup.decode(encoded)
        assertEquals(before, decoded)

        // Reject cyclic dependencies in backup with atomic rollback
        val cyclic = before.copy(
            taskDependencies = before.taskDependencies + TaskDependency(id = "bad-cyc", taskId = "tb1", dependsOnTaskId = "tb2")
        )
        assertTrue(runCatching { Backup.decode(Backup.encode(cyclic)) }.isFailure)
        assertTrue(runCatching { repo.importState(cyclic) }.isFailure)
        assertEquals(before, s())

        // Reject invalid availability window (endTime <= startTime)
        val badWindow = before.copy(
            availabilityWindows = listOf(AvailabilityWindow(id = "bad-w", dayOfWeek = 1, startTime = "14:00", endTime = "10:00"))
        )
        assertTrue(runCatching { Backup.decode(Backup.encode(badWindow)) }.isFailure)
    }

    // =========================================================================
    // SEÇÃO 32 — BENCHMARKS DE DESEMPENHO (50, 200 E 1.000 TAREFAS)
    // =========================================================================

    @Test
    fun performanceBenchmarks_50_200_1000_Tasks() {
        val start = today()
        val end = LocalDate.parse(start).plusDays(13).toString()
        val windows = defaultAvailabilityWindows()

        listOf(50, 200, 1000).forEach { count ->
            val path = PersonalPath(id = "bench-$count", name = "Benchmark $count")
            val tasks = (0 until count).map { i ->
                PathTask(
                    id = "bt-$i",
                    pathId = path.id,
                    title = "Tarefa $i",
                    priority = i % 4,
                    estimateMinutes = 30 + (i % 4) * 15,
                    deadline = LocalDate.parse(start).plusDays((i % 14).toLong()).toString(),
                    position = i
                )
            }
            // Linear chains of length 5 across tasks (20% dependencies, DAG garantido)
            val deps = (0 until count).mapNotNull { i ->
                if (i % 5 != 0) TaskDependency(id = "bd-$i", taskId = "bt-$i", dependsOnTaskId = "bt-${i - 1}") else null
            }
            val benchState = GameState(
                player = Player(name = "Bench"),
                paths = listOf(path),
                pathTasks = tasks,
                taskDependencies = deps,
                availabilityWindows = windows
            )

            val t0 = System.nanoTime()
            val hasCycle = OdinPlanningEngine.hasDependencyCycle(deps)
            val cpm = OdinPlanningEngine.computeCriticalPath(path, benchState, start)
            val proposal = OdinPlanningEngine.generateOdinProposal(benchState, start, end, strategyId = "strategist")
            val cap = OdinPlanningEngine.simulateCapacity(benchState, CapacityPeriodKind.NEXT_7_DAYS, start)
            val elapsedMs = (System.nanoTime() - t0) / 1_000_000L

            println("ODIN_BENCHMARK tasks=$count deps=${deps.size} elapsedMs=$elapsedMs suggestions=${proposal.suggestions.size} unscheduled=${proposal.unscheduled.size} cpmNodes=${cpm.nodes.size} capStatus=${cap.status}")

            assertFalse(hasCycle)
            assertEquals(count, cpm.nodes.size)
            assertTrue(proposal.suggestions.isNotEmpty())
            assertTrue("Benchmark for $count tasks took too long: ${elapsedMs}ms", elapsedMs < 4000L)
        }
    }
}
