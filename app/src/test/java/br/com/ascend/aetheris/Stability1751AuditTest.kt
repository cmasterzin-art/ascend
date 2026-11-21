package br.com.ascend.aetheris

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * ASCEND 1.7.5.1 — Etapa I: Auditoria Independente e Estabilização Técnica
 * Suíte determinística cobrindo os 6 riscos prioritários (P0 + 5 P1s) e as verificações
 * complementares de concorrência, atomicidade, idempotência e integridade referencial.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = AscendApp::class)
class Stability1751AuditTest {
    private lateinit var ctx: Context
    private lateinit var db: GameDatabase
    private lateinit var repo: GameRepository

    @Before
    fun setup() {
        ctx = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(ctx, GameDatabase::class.java).allowMainThreadQueries().build()
        repo = GameRepository(db)
        runBlocking {
            repo.create(Player(name = "Sigurd", heroClass = 0), emptyList())
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun s(): GameState = repo.state.value!!

    // =========================================================================
    // SEÇÃO 6 (P0): POMODORO AUTOMÁTICO, CICLO DE VIDA, CONCORRÊNCIA E RELÓGIO
    // =========================================================================

    @Test
    fun p0_pomodoroAutoAdvanceEnabledDisabledCyclesPauseEarlyEndConcurrencyAndClockAnomaly() = runBlocking {
        val h = Habit(id = "h-pomo", name = "Estudo Rúnico", type = 2, target = 100, unit = "min", xp = 40, coins = 10, proportional = true)
        repo.habit(h)

        // 1) autoAdvance = false -> o término do tempo NÃO avança a fase sozinho
        repo.startTimeForgeFocus(
            mode = "pomodoro",
            workMinutes = 25,
            shortBreakMinutes = 5,
            longBreakMinutes = 15,
            totalCycles = 2,
            autoAdvance = false,
            habitId = h.id
        )
        val fManual = s().focus!!
        val after25mWall = fManual.startedAt + 25 * 60_000L
        val after25mMono = fManual.startedRealtimeMs + 25 * 60_000L
        assertTrue(fManual.isPhaseCompleted(after25mWall, after25mMono))
        assertFalse("reconcileFocusTimer must NOT transition when autoAdvance=false", repo.reconcileFocusTimer(after25mWall, after25mMono))
        assertEquals("work", s().focus!!.phase)
        assertEquals(0, s().focusSessions.size)
        // Encerramento antecipado com registro
        repo.endFocus(register = false)
        assertNull(s().focus)
        assertEquals(1, s().focusSessions.size)
        assertEquals("cancelled", s().focusSessions.first().status)

        // 2) autoAdvance = true -> fim do 1º ciclo avança para short_break, fim da pausa avança para ciclo 2, fim do último ciclo encerra
        repo.startTimeForgeFocus(
            mode = "pomodoro",
            workMinutes = 20,
            shortBreakMinutes = 5,
            longBreakMinutes = 15,
            totalCycles = 2,
            autoAdvance = true,
            habitId = h.id
        )
        val c1Work = s().focus!!
        assertEquals("work", c1Work.phase)
        assertEquals(1, c1Work.currentCycle)

        // Antes de completar 20 min, reconcileFocusTimer não transiciona
        assertFalse(repo.reconcileFocusTimer(c1Work.startedAt + 10 * 60_000L, c1Work.startedRealtimeMs + 10 * 60_000L))
        assertEquals("work", s().focus!!.phase)

        // Duas chamadas concorrentes no fim do 1º ciclo -> apenas UMA transição ocorre e apenas UMA FocusSession de trabalho é criada!
        val c1EndWall = c1Work.startedAt + 20 * 60_000L
        val c1EndMono = c1Work.startedRealtimeMs + 20 * 60_000L
        val concurrentResults = listOf(
            async { repo.reconcileFocusTimer(c1EndWall, c1EndMono) },
            async { repo.reconcileFocusTimer(c1EndWall, c1EndMono) }
        ).awaitAll()
        assertEquals("Exactly one concurrent reconcile call must perform the phase transition", 1, concurrentResults.count { it })

        val c1Break = s().focus!!
        assertEquals("short_break", c1Break.phase)
        assertEquals(1, c1Break.currentCycle)
        assertTrue("Break must start automatically when autoAdvance=true", c1Break.startedAt > 0L)
        val workSessionsAfterC1 = s().focusSessions.filter { it.registered }
        assertEquals(1, workSessionsAfterC1.size)
        assertEquals(1200L, workSessionsAfterC1.single().elapsedSeconds)
        assertEquals(20, s().record(h.id, today())?.progress)

        // Retorno do segundo plano após os 5 minutos de pausa terem expirado:
        // reconcilia de short_break -> work (ciclo 2) SEM contar pausa como foco!
        val breakEndWall = c1Break.startedAt + 5 * 60_000L
        val breakEndMono = c1Break.startedRealtimeMs + 5 * 60_000L
        assertTrue(repo.reconcileFocusTimer(breakEndWall, breakEndMono))

        val c2Work = s().focus!!
        assertEquals("work", c2Work.phase)
        assertEquals(2, c2Work.currentCycle)
        assertEquals("Break must NEVER create a FocusSession", 1, s().focusSessions.count { it.registered })
        assertEquals("Break must NEVER add habit progress", 20, s().record(h.id, today())?.progress)

        // Pausa manual durante o 2º ciclo (após 10 min = 600s decorridos) -> enquanto pausado (startedAt == 0L), reconcileFocusTimer nunca avança
        db.dao().focus(c2Work.copy(elapsed = 600L))
        repo.pauseFocus()
        val paused = s().focus!!
        assertEquals(0L, paused.startedAt)
        assertTrue(paused.elapsed >= 600L)
        assertEquals(1, paused.pauseCount)
        assertFalse("Paused timer must never auto-advance", repo.reconcileFocusTimer(System.currentTimeMillis() + 3600_000L, paused.startedRealtimeMs + 3600_000L))

        // Retomar e verificar proteção contra anomalia de relógio do sistema (+3h no wall clock, mas 5 min no monotônico -> 15/20 min, não encerra; depois 10 min no monotônico -> 20/20 min, encerra!)
        repo.pauseFocus() // retoma
        val resumed = s().focus!!
        val c2EndWallWithClockJump = resumed.startedAt + 3 * 3600_000L // relógio adiantado 3h
        val c2MidMono5m = resumed.startedRealtimeMs + 5 * 60_000L
        assertTrue(resumed.hasClockAnomaly(c2EndWallWithClockJump, c2MidMono5m))
        assertFalse("Clock jump must NOT prematurely complete cycle when monotonic clock is only at 15/20 min", repo.reconcileFocusTimer(c2EndWallWithClockJump, c2MidMono5m))

        val c2EndMonoReal = resumed.startedRealtimeMs + 10 * 60_000L
        assertTrue("End of final cycle must complete and clear Focus", repo.reconcileFocusTimer(c2EndWallWithClockJump, c2EndMonoReal))
        assertNull("Focus must be cleared after last cycle completes", s().focus)

        val allRegistered = s().focusSessions.filter { it.registered }
        assertEquals("Exactly 2 work sessions must be recorded for 2 cycles", 2, allRegistered.size)
        assertTrue("Second session must flag clockAnomaly", allRegistered.first { it.id == resumed.sessionId }.clockAnomaly)
        assertEquals("Total habit progress must equal sum of work cycles (20 + 20 = 40 min)", 40, s().record(h.id, today())?.progress)
    }

    // =========================================================================
    // SEÇÃO 7 (P1): RECORRÊNCIAS OPERACIONAIS NA AGENDA E MESA DE ODIN
    // =========================================================================

    @Test
    fun p1_recurrenceRulesProjectedAcrossAgendaAndOdinPlanning() = runBlocking {
        // 1) Padrões: diária, semanal, dias selecionados, mensal, fevereiro, ano bissexto, limites de mês e data de término
        val daily = RecurrenceRule(id = "r-daily", entityType = "block", templateId = "b1", pattern = "daily", startDate = "2026-06-01", endDate = "2026-06-04")
        assertEquals(listOf("2026-06-01", "2026-06-02", "2026-06-03", "2026-06-04"), OdinPlanningEngine.expandRecurrenceOccurrences(daily, "2026-05-30", "2026-06-10"))

        val weekly = RecurrenceRule(id = "r-weekly", entityType = "block", templateId = "b1", pattern = "weekly", startDate = "2026-06-01", endDate = "2026-06-15")
        assertEquals(listOf("2026-06-01", "2026-06-08", "2026-06-15"), OdinPlanningEngine.expandRecurrenceOccurrences(weekly, "2026-06-01", "2026-06-20"))

        val weekdays = RecurrenceRule(id = "r-wd", entityType = "block", templateId = "b1", pattern = "weekdays", daysOfWeek = "2,4", startDate = "2026-06-01", endDate = "2026-06-07")
        assertEquals(listOf("2026-06-02", "2026-06-04"), OdinPlanningEngine.expandRecurrenceOccurrences(weekdays, "2026-06-01", "2026-06-07"))

        // Mensal iniciando dia 31 -> respeita fevereiro não-bissexto (2026-02-28), limites 30 dias (2026-04-30) e ano bissexto (2028-02-29)
        val monthly31 = RecurrenceRule(id = "r-m31", entityType = "event", templateId = "ev1", pattern = "monthly", startDate = "2026-01-31")
        assertEquals(
            listOf("2026-01-31", "2026-02-28", "2026-03-31", "2026-04-30"),
            OdinPlanningEngine.expandRecurrenceOccurrences(monthly31, "2026-01-01", "2026-04-30")
        )
        assertEquals(
            listOf("2028-01-31", "2028-02-29", "2028-03-31"),
            OdinPlanningEngine.expandRecurrenceOccurrences(monthly31, "2028-01-01", "2028-03-31")
        )

        // 2) Integração real com agendaDay(), monthMarker(), busyIntervalsForDate(), freeIntervalsForDate() e generateOdinProposal()
        val mon = "2026-06-01" // Segunda
        val tue = "2026-06-02" // Terça
        val wed = "2026-06-03" // Quarta
        for (dow in 1..5) {
            repo.saveAvailabilityWindow(AvailabilityWindow(id = "w-$dow", dayOfWeek = dow, startTime = "09:00", endTime = "11:00"))
        }

        // Bloco template na segunda das 09:00 às 10:00 + regra diária até quarta
        val tplBlock = TimeBlock(id = "blk-rec-tpl", title = "Treino Rúnico Diário", date = mon, startTime = "09:00", endTime = "10:00", recurrenceId = "rule-blk-daily")
        repo.saveTimeBlock(tplBlock)
        repo.saveRecurrenceRule(
            RecurrenceRule(
                id = "rule-blk-daily",
                entityType = "block",
                templateId = tplBlock.id,
                pattern = "daily",
                startDate = mon,
                endDate = wed
            )
        )

        // Compromisso template na segunda das 10:00 às 10:30 + regra diária até quarta
        val tplEvent = AgendaEvent(id = "ev-rec-tpl", title = "Conselho Diário", date = mon, startTime = "10:00", endTime = "10:30", recurrenceId = "rule-ev-daily")
        repo.saveEvent(tplEvent)
        repo.saveRecurrenceRule(
            RecurrenceRule(
                id = "rule-ev-daily",
                entityType = "event",
                templateId = tplEvent.id,
                pattern = "daily",
                startDate = mon,
                endDate = wed
            )
        )

        // Não duplica na segunda-feira (data do template)
        val monDay = agendaDay(s(), mon)
        assertEquals("Template date must not duplicate recurring block", 1, monDay.blocks.size)
        assertEquals("Template date must not duplicate recurring event", 1, monDay.events.size)

        // Projeta virtualmente na terça-feira em agendaDay e monthMarker
        val tueDay = agendaDay(s(), tue)
        assertEquals(1, tueDay.blocks.size)
        assertEquals("Treino Rúnico Diário", tueDay.blocks.single().title)
        assertEquals(1, tueDay.events.size)
        assertEquals("Conselho Diário", tueDay.events.single().title)
        val tueMarker = monthMarker(s(), tue)
        assertEquals(1, tueMarker.blocksCount)
        assertEquals(1, tueMarker.eventsCount)

        // Ocupa 09:00..10:30 na terça-feira, deixando livre apenas 10:30..11:00 (30 min)
        assertEquals(listOf(MinuteInterval(540, 630)), OdinPlanningEngine.busyIntervalsForDate(s(), tue))
        assertEquals(listOf(MinuteInterval(630, 660)), OdinPlanningEngine.freeIntervalsForDate(s(), tue))

        // Exclusão de ocorrência na quarta-feira libera 09:00..10:00 na quarta sem afetar segunda ou terça
        repo.excludeRecurrenceDate("rule-blk-daily", wed)
        assertEquals(0, agendaDay(s(), wed).blocks.size)
        assertEquals(1, agendaDay(s(), tue).blocks.size)

        // Ocorrência recorrente impede sugestão conflitante na Mesa de Odin
        repo.saveTask(PathTask(id = "t-rec-test", title = "Tarefa de 30m", estimateMinutes = 30, deadline = tue))
        val proposal = OdinPlanningEngine.generateOdinProposal(s(), tue, tue)
        assertEquals(1, proposal.suggestions.size)
        val sug = proposal.suggestions.single()
        assertEquals(tue, sug.date)
        assertEquals("10:30", sug.startTime)
        assertEquals("11:00", sug.endTime)

        // Mudança da série para começar na quarta não apaga o bloco histórico materializado na segunda
        repo.saveRecurrenceRule(
            s().recurrenceRules.first { it.id == "rule-blk-daily" }.copy(startDate = wed, excludedDates = "")
        )
        assertEquals("Historical materialized block on Monday must remain intact", 1, agendaDay(s(), mon).blocks.size)
        assertEquals("Tuesday before new series startDate must no longer project", 0, agendaDay(s(), tue).blocks.size)
    }

    // =========================================================================
    // SEÇÃO 8 (P1): REPLANEJAMENTO, CONFLITOS E AUSÊNCIA DE FALLBACK ÀS 09H
    // =========================================================================

    @Test
    fun p1_replanValidatesConflictsRejectsStaleOptionsAndNeverFallsBackTo9am() = runBlocking {
        val yesterday = "2026-06-07" // Domingo
        val mon = "2026-06-08" // Segunda
        val tue = "2026-06-09" // Terça
        val nextMon = "2026-06-15" // Próxima segunda

        repo.saveTask(PathTask(id = "t-late", title = "Relatório Crítico", estimateMinutes = 60, scheduledDate = yesterday))
        repo.saveTimeBlock(TimeBlock(id = "b-late", title = "Relatório Crítico", date = yesterday, startTime = "09:00", endTime = "10:00", taskId = "t-late"))

        // 1) Sem disponibilidade na próxima semana -> POSTPONE NÃO pode inventar bloco às 09:00!
        val itemsNoAvail = OdinPlanningEngine.generateReplanProposal(s(), mon)
        assertEquals(1, itemsNoAvail.size)
        assertFalse(
            "POSTPONE must NOT fall back to arbitrary 09:00 when no availability exists",
            itemsNoAvail.single().options.any { it.action == ReplanActionKind.POSTPONE }
        )

        // 2) Configurar janelas para TRANSFER (segunda 09:00..10:00), SPLIT (terça 09:00..09:30 e quarta 09:00..09:30) e POSTPONE (próxima segunda 09:00..10:00)
        repo.saveAvailabilityWindow(AvailabilityWindow(id = "w-1", dayOfWeek = 1, startTime = "09:00", endTime = "10:00"))
        repo.saveAvailabilityWindow(AvailabilityWindow(id = "w-2", dayOfWeek = 2, startTime = "09:00", endTime = "09:40"))
        repo.saveAvailabilityWindow(AvailabilityWindow(id = "w-3", dayOfWeek = 3, startTime = "09:00", endTime = "09:40"))

        val item = OdinPlanningEngine.generateReplanProposal(s(), mon).single()
        val transferOpt = item.options.first { it.action == ReplanActionKind.TRANSFER }
        val splitOpt = item.options.first { it.action == ReplanActionKind.SPLIT }
        val postponeOpt = item.options.first { it.action == ReplanActionKind.POSTPONE }
        assertEquals(mon, transferOpt.targetDate)
        assertEquals(nextMon, postponeOpt.targetDate)

        // 3) Criar compromisso que ocupa o horário sugerido por TRANSFER -> aplicar sugestão antiga deve FALHAR sem persistência parcial
        repo.saveEvent(AgendaEvent(id = "ev-conflict-1", title = "Urgência médica", date = transferOpt.targetDate, startTime = transferOpt.startTime, endTime = transferOpt.endTime))
        assertTrue(runCatching { repo.applyReplanChoice(item, transferOpt) }.isFailure)
        assertEquals("Old block must remain untouched on rollback", yesterday, s().timeBlocks.first { it.id == "b-late" }.date)
        assertEquals("Task scheduledDate must remain untouched on rollback", yesterday, s().pathTasks.first { it.id == "t-late" }.scheduledDate)

        // 4) Criar bloco fixado ou compromisso que ocupa a 2ª metade do SPLIT -> aplicar SPLIT antigo deve FALHAR sem apagar o bloco original!
        repo.saveEvent(AgendaEvent(id = "ev-conflict-2", title = "Conflito na 2ª parte", date = splitOpt.secondDate, startTime = splitOpt.secondStartTime, endTime = splitOpt.secondEndTime))
        assertTrue(runCatching { repo.applyReplanChoice(item, splitOpt) }.isFailure)
        assertTrue("Original block must NOT be deleted when second split slot fails validation", s().timeBlocks.any { it.id == "b-late" && it.date == yesterday })
        assertEquals("No partial split block may be persisted", 1, s().timeBlocks.size)

        // 5) Criar compromisso ocupando o horário do POSTPONE -> aplicar POSTPONE antigo deve FALHAR atomicamente
        repo.saveEvent(AgendaEvent(id = "ev-conflict-3", title = "Conflito na próxima semana", date = postponeOpt.targetDate, startTime = postponeOpt.startTime, endTime = postponeOpt.endTime))
        assertTrue(runCatching { repo.applyReplanChoice(item, postponeOpt) }.isFailure)
        assertEquals(yesterday, s().timeBlocks.first { it.id == "b-late" }.date)

        // 6) Remover o conflito de POSTPONE -> agora POSTPONE é aplicado com sucesso
        repo.deleteEvent("ev-conflict-3")
        repo.applyReplanChoice(item, postponeOpt)
        assertEquals(nextMon, s().timeBlocks.first { it.id == "b-late" }.date)
        assertEquals(nextMon, s().pathTasks.first { it.id == "t-late" }.scheduledDate)
    }

    // =========================================================================
    // SEÇÃO 9 (P1): DEPENDÊNCIAS PARCIALMENTE AGENDADAS E SELEÇÃO PARCIAL
    // =========================================================================

    @Test
    fun p1_dependenciesRequireFullSchedulingAndRejectInvalidPartialSelection() = runBlocking {
        val mon = "2026-06-01"
        val tue = "2026-06-02"
        // Apenas 60 min disponíveis na segunda-feira e 150 min na terça-feira (comportando 60m de A + 30m de B + 30m de C + buffers)
        repo.saveAvailabilityWindow(AvailabilityWindow(id = "w-mon", dayOfWeek = 1, startTime = "09:00", endTime = "10:00"))
        repo.saveAvailabilityWindow(AvailabilityWindow(id = "w-tue", dayOfWeek = 2, startTime = "09:00", endTime = "11:30"))

        // Tarefa A exige 120 min; Tarefa B (30 min) depende de A; Tarefa C (30 min) depende de B (A -> B -> C)
        repo.saveTask(PathTask(id = "t-A", title = "Fundação A", estimateMinutes = 120, priority = 3))
        repo.saveTask(PathTask(id = "t-B", title = "Estrutura B", estimateMinutes = 30, priority = 2))
        repo.saveTask(PathTask(id = "t-C", title = "Acabamento C", estimateMinutes = 30, priority = 1))
        repo.addTaskDependency("t-B", "t-A")
        repo.addTaskDependency("t-C", "t-B")

        // 1) Horizonte apenas de segunda-feira (60 min livres):
        // A só consegue alocar 60/120 min (PARTIALLY_SCHEDULED).
        // B e C NÃO podem ser agendadas e devem constar em unscheduled como bloqueadas!
        val propPartial = OdinPlanningEngine.generateOdinProposal(s(), mon, mon, includeHabits = false, includeMimirReviews = false)
        assertEquals(TaskScheduleState.PARTIALLY_SCHEDULED, propPartial.taskScheduleStates["t-A"])
        assertEquals(TaskScheduleState.UNSCHEDULED, propPartial.taskScheduleStates["t-B"])
        assertEquals(TaskScheduleState.UNSCHEDULED, propPartial.taskScheduleStates["t-C"])
        assertTrue(propPartial.suggestions.all { it.taskId == "t-A" })
        assertTrue(propPartial.unscheduled.any { it.id == "t-A" && it.remainingMinutes == 60 })
        val unsB = propPartial.unscheduled.first { it.id == "t-B" }
        assertTrue("Reason must report partially scheduled prerequisite", unsB.reason.contains("parcialmente agendado", ignoreCase = true))
        assertTrue(propPartial.unscheduled.any { it.id == "t-C" })

        // 2) Horizonte segunda + terça (60 min na seg + 120 min na ter = 180 min livres; strategy berserker para usar até 98% do limite diário):
        // A (120 min) é agendada integralmente (60m seg + 60m ter até 10:00), liberando B e depois C na terça!
        repo.savePlanningConfig(PlanningConfig(strategyId = "berserker", dailyLimitMinutes = 240, bufferMinutes = 0, minBlockMinutes = 15, maxBlockMinutes = 90))
        val propFull = OdinPlanningEngine.generateOdinProposal(s(), mon, tue, strategyId = "berserker", includeHabits = false, includeMimirReviews = false)
        assertEquals(TaskScheduleState.FULLY_SCHEDULED, propFull.taskScheduleStates["t-A"])
        assertEquals(TaskScheduleState.FULLY_SCHEDULED, propFull.taskScheduleStates["t-B"])
        assertEquals(TaskScheduleState.FULLY_SCHEDULED, propFull.taskScheduleStates["t-C"])

        // 3) Aplicação parcial inválida: usuário desmarca uma fatia de A mantendo B marcada -> deve REJEITAR!
        val aSuggestionIds = propFull.suggestions.filter { it.taskId == "t-A" }.map { it.id }
        val bSuggestionIds = propFull.suggestions.filter { it.taskId == "t-B" }.map { it.id }
        assertTrue(aSuggestionIds.size >= 2)
        val invalidPartialSelection = (listOf(aSuggestionIds.first()) + bSuggestionIds).toSet()
        assertTrue(
            "Partial selection that leaves prerequisite A partially unscheduled while selecting dependent B must be rejected",
            runCatching { repo.applyOdinProposal(propFull, selectedIds = invalidPartialSelection) }.isFailure
        )
        assertTrue("No blocks may be persisted after rejected partial selection", s().timeBlocks.isEmpty())

        // 4) Aplicação parcial válida: selecionar todas as fatias de A e B (desmarcando apenas C, que é folha final) -> PERMITIDO!
        val validPartialSelection = (aSuggestionIds + bSuggestionIds).toSet()
        val rec = repo.applyOdinProposal(propFull, selectedIds = validPartialSelection)
        assertTrue(rec.blockIds.isNotBlank())
        assertTrue(s().timeBlocks.any { it.taskId == "t-A" })
        assertTrue(s().timeBlocks.any { it.taskId == "t-B" })
        assertFalse(s().timeBlocks.any { it.taskId == "t-C" })
    }

    @Test
    fun p1_diamondDependenciesAndExistingBlocksRequireFullCoverageBeforeReleasingDependent() = runBlocking {
        val mon = "2026-06-01"
        repo.saveAvailabilityWindow(AvailabilityWindow(id = "w-mon", dayOfWeek = 1, startTime = "08:00", endTime = "18:00"))
        // A (60m) e B (60m) -> D (30m)
        repo.saveTask(PathTask(id = "d-A", title = "Pré A", estimateMinutes = 60))
        repo.saveTask(PathTask(id = "d-B", title = "Pré B", estimateMinutes = 60))
        repo.saveTask(PathTask(id = "d-D", title = "Convergência D", estimateMinutes = 30))
        repo.addTaskDependency("d-D", "d-A")
        repo.addTaskDependency("d-D", "d-B")

        // Se d-A já possui um bloco existente de apenas 30 min (parcial: faltam 30 min) e não há capacidade para o restante de d-A/d-B
        repo.saveTimeBlock(TimeBlock(id = "blk-part-a", title = "Parte de A", date = mon, startTime = "08:00", endTime = "08:30", taskId = "d-A"))
        repo.savePlanningConfig(PlanningConfig(strategyId = "berserker", dailyLimitMinutes = 60, bufferMinutes = 0, minBlockMinutes = 15, maxBlockMinutes = 60))

        // Com limite diário de 60 min e 30 min já usados por blk-part-a, restam apenas 30 min no dia:
        // d-A pode completar seus 30 min restantes, mas d-B (60 min) não cabe -> d-D deve continuar bloqueada!
        val prop = OdinPlanningEngine.generateOdinProposal(s(), mon, mon, strategyId = "berserker", includeHabits = false, includeMimirReviews = false)
        assertFalse("Convergent task D must NOT be scheduled when B is not fully scheduled", prop.suggestions.any { it.taskId == "d-D" })
        assertTrue(prop.unscheduled.any { it.id == "d-D" })
    }

    // =========================================================================
    // SEÇÃO 10 (P1): DESFAZIMENTO SEGURO (PRESERVAÇÃO DE EDIÇÕES MANUAIS E PLANOS SUCESSIVOS)
    // =========================================================================

    @Test
    fun p1_undoAppliedPlanPreservesManualTaskEditsAndHandlesSequentialPlans() = runBlocking {
        val mon = "2026-06-01"
        val tue = "2026-06-02"
        repo.saveAvailabilityWindow(AvailabilityWindow(id = "w-1", dayOfWeek = 1, startTime = "09:00", endTime = "12:00"))
        repo.saveAvailabilityWindow(AvailabilityWindow(id = "w-2", dayOfWeek = 2, startTime = "09:00", endTime = "12:00"))

        // 1) Criar tarefa sem data, aplicar plano, alterar manualmente a data da tarefa, desfazer plano -> data manual DEVE ser preservada!
        repo.saveTask(PathTask(id = "t-manual", title = "Tarefa Editada Manualmente", estimateMinutes = 45))
        repo.saveTask(PathTask(id = "t-untouched", title = "Tarefa Intacta", estimateMinutes = 45))

        val prop1 = OdinPlanningEngine.generateOdinProposal(s(), mon, mon, includeHabits = false, includeMimirReviews = false)
        val plan1 = repo.applyOdinProposal(prop1)

        val tManualAfterPlan = s().pathTasks.first { it.id == "t-manual" }
        assertEquals(mon, tManualAfterPlan.scheduledDate)

        // Usuário altera manualmente a data e horário da tarefa t-manual (sem editar o TimeBlock)
        repo.saveTask(tManualAfterPlan.copy(scheduledDate = "2026-06-10", scheduledTime = "16:30"))

        val (removed1, preserved1) = repo.undoAppliedPlan(plan1.id)
        assertEquals(2, removed1)
        assertEquals(0, preserved1)
        val tManualAfterUndo = s().pathTasks.first { it.id == "t-manual" }
        assertEquals("Manual task scheduledDate edit MUST NOT be overwritten by undoAppliedPlan", "2026-06-10", tManualAfterUndo.scheduledDate)
        assertEquals("Manual task scheduledTime edit MUST NOT be overwritten by undoAppliedPlan", "16:30", tManualAfterUndo.scheduledTime)

        // Já a tarefa que não foi editada manualmente volta ao seu estado anterior ("", "")
        val tUntouchedAfterUndo = s().pathTasks.first { it.id == "t-untouched" }
        assertEquals("", tUntouchedAfterUndo.scheduledDate)
        assertEquals("", tUntouchedAfterUndo.scheduledTime)

        // Repetição de undo no mesmo plano é rejeitada de forma segura
        assertTrue(runCatching { repo.undoAppliedPlan(plan1.id) }.isFailure)

        // 2) Dois planos aplicados sucessivamente: desfazer o 1º não corrompe o 2º
        repo.saveTask(PathTask(id = "t-seq", title = "Tarefa em Dois Planos", estimateMinutes = 120))
        val propSeq1 = OdinPlanningEngine.generateOdinProposal(s(), mon, mon, includeHabits = false, includeMimirReviews = false)
        val firstSliceOnly = setOf(propSeq1.suggestions.first { it.taskId == "t-seq" }.id)
        val appliedSeq1 = repo.applyOdinProposal(propSeq1, selectedIds = firstSliceOnly)

        val propSeq2 = OdinPlanningEngine.generateOdinProposal(s(), tue, tue, includeHabits = false, includeMimirReviews = false)
        val appliedSeq2 = repo.applyOdinProposal(propSeq2)

        // Desfazer o 1º plano deve remover apenas os blocos do 1º plano e manter a tarefa agendada na data do 2º plano ativo
        repo.undoAppliedPlan(appliedSeq1.id)
        assertTrue(s().timeBlocks.none { it.sourcePlanId == appliedSeq1.id })
        val seq2Blocks = s().timeBlocks.filter { it.sourcePlanId == appliedSeq2.id }
        assertTrue(seq2Blocks.isNotEmpty())
        assertEquals(tue, s().pathTasks.first { it.id == "t-seq" }.scheduledDate)
    }

    // =========================================================================
    // SEÇÃO 11 (P1): HORÁRIOS PASSADOS NO DIA CORRENTE E REFERÊNCIA TEMPORAL INJETÁVEL
    // =========================================================================

    @Test
    fun p1_pastHoursFilteredOnCurrentDayWithInjectableReferenceClock() = runBlocking {
        val mon = "2026-06-01"
        val tue = "2026-06-02"
        // Janela das 08:00 às 20:00 na segunda e terça
        repo.saveAvailabilityWindow(AvailabilityWindow(id = "w-mon", dayOfWeek = 1, startTime = "08:00", endTime = "20:00"))
        repo.saveAvailabilityWindow(AvailabilityWindow(id = "w-tue", dayOfWeek = 2, startTime = "08:00", endTime = "20:00"))
        repo.saveTask(PathTask(id = "t-clock", title = "Tarefa Sensível ao Relógio", estimateMinutes = 60))

        // 1) Geração às 09:00 -> nenhuma sugestão antes de 09:00 em mon
        val prop9 = OdinPlanningEngine.generateOdinProposal(
            s = s(),
            fromDate = mon,
            toDate = tue,
            includeHabits = false,
            includeMimirReviews = false,
            clock = PlanningClock.at(mon, "09:00")
        )
        val sug9 = prop9.suggestions.first { it.taskId == "t-clock" }
        assertEquals(mon, sug9.date)
        assertTrue("At 09:00, startTime (${sug9.startTime}) must be >= 09:00", parseTimeToMinutes(sug9.startTime)!! >= 9 * 60)

        // 2) Geração às 18:00 (via java.time.Clock.fixed com fuso horário explícito) -> nenhuma sugestão antes de 18:00 em mon
        val fixedClock18 = Clock.fixed(Instant.parse("2026-06-01T21:00:00Z"), ZoneId.of("America/Sao_Paulo")) // 18:00 BRT
        val prop18 = OdinPlanningEngine.generateOdinProposal(
            s = s(),
            fromDate = mon,
            toDate = tue,
            includeHabits = false,
            includeMimirReviews = false,
            clock = PlanningClock.fromClock(fixedClock18)
        )
        val sug18 = prop18.suggestions.first { it.taskId == "t-clock" }
        assertEquals(mon, sug18.date)
        assertTrue("At 18:00, startTime (${sug18.startTime}) must be >= 18:00", parseTimeToMinutes(sug18.startTime)!! >= 18 * 60)

        // 3) Geração às 23:59 -> dia corrente (mon) não tem mais janela útil; sugestão passa limpa para terça às 08:00 (virada do dia)
        val prop2359 = OdinPlanningEngine.generateOdinProposal(
            s = s(),
            fromDate = mon,
            toDate = tue,
            includeHabits = false,
            includeMimirReviews = false,
            clock = PlanningClock.at(mon, "23:59")
        )
        val sug2359 = prop2359.suggestions.first { it.taskId == "t-clock" }
        assertEquals("At 23:59 on Monday, task must roll over to Tuesday", tue, sug2359.date)
        assertEquals("08:00", sug2359.startTime)

        // 4) Data futura explícita (gerando na segunda às 18:00 um plano que começa na terça) -> terça começa normalmente às 08:00
        val propFuture = OdinPlanningEngine.generateOdinProposal(
            s = s(),
            fromDate = tue,
            toDate = tue,
            includeHabits = false,
            includeMimirReviews = false,
            clock = PlanningClock.at(mon, "18:00")
        )
        assertEquals("08:00", propFuture.suggestions.first { it.taskId == "t-clock" }.startTime)

        // 5) Consulta histórica explícita (fromDate < referenceDate) -> não trunca janelas históricas
        val propHistorical = OdinPlanningEngine.generateOdinProposal(
            s = s(),
            fromDate = mon,
            toDate = mon,
            includeHabits = false,
            includeMimirReviews = false,
            clock = PlanningClock.at(tue, "18:00")
        )
        assertEquals("08:00", propHistorical.suggestions.first { it.taskId == "t-clock" }.startTime)
    }

    // =========================================================================
    // SEÇÃO 12: VERIFICAÇÕES COMPLEMENTARES DE INTEGRIDADE E CONCORRÊNCIA
    // =========================================================================

    @Test
    fun section12_complementaryConcurrencyDeletedTaskArchivedPathAndMidnightFocusChecks() = runBlocking {
        val mon = LocalDate.parse(today()).with(DayOfWeek.MONDAY).toString()
        repo.saveAvailabilityWindow(AvailabilityWindow(id = "w-1", dayOfWeek = 1, startTime = "09:00", endTime = "12:00"))
        val p = PersonalPath(id = "p-comp", name = "Projeto Ativo", state = 1)
        repo.savePath(p)
        repo.saveTask(PathTask(id = "t-comp", pathId = p.id, title = "Entrega 1", estimateMinutes = 60))

        val proposal = OdinPlanningEngine.generateOdinProposal(s(), mon, mon, includeHabits = false, includeMimirReviews = false)
        assertEquals(1, proposal.suggestions.size)

        // 1) Caminho arquivado após geração -> aplicação da proposta antiga deve falhar
        repo.setPathState(p.id, 4) // Arquivado
        assertTrue(runCatching { repo.applyOdinProposal(proposal) }.isFailure)
        repo.setPathState(p.id, 1) // Reativa

        // 2) Tarefa excluída após geração -> aplicação da proposta antiga deve falhar
        repo.deleteTask("t-comp")
        assertTrue(runCatching { repo.applyOdinProposal(proposal) }.isFailure)

        // Recria tarefa e gera nova proposta
        repo.saveTask(PathTask(id = "t-comp", pathId = p.id, title = "Entrega 1", estimateMinutes = 60))
        val freshProposal = OdinPlanningEngine.generateOdinProposal(s(), mon, mon, includeHabits = false, includeMimirReviews = false)

        // 3) Duas aplicações concorrentes da mesma proposta -> exatamente uma sucede e a outra falha sem duplicar blocos
        val results = listOf(
            async { runCatching { repo.applyOdinProposal(freshProposal) }.isSuccess },
            async { runCatching { repo.applyOdinProposal(freshProposal) }.isSuccess }
        ).awaitAll()
        assertEquals("Exactly one concurrent proposal application must succeed", 1, results.count { it })
        assertEquals(1, s().timeBlocks.size)

        // 4) Sessão de foco vinculada a projeto e atravessando meia-noite
        repo.startTimeForgeFocus(
            mode = "deep",
            workMinutes = 30,
            totalCycles = 1,
            autoAdvance = true,
            pathId = p.id,
            taskId = "t-comp"
        )
        val f = s().focus!!
        val endAfter30m = f.startedAt + 30 * 60_000L
        val endMono30m = f.startedRealtimeMs + 30 * 60_000L
        assertTrue(repo.reconcileFocusTimer(endAfter30m, endMono30m))
        assertNull(s().focus)
        assertEquals(30L, pathFocusMinutes(s(), p.id))
        assertEquals(30L, taskFocusMinutes(s(), "t-comp"))
    }
}
