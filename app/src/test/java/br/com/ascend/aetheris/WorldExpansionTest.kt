package br.com.ascend.aetheris

import androidx.room.Room
import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
class WorldExpansionTest {
    private lateinit var db: GameDatabase
    private lateinit var repo: GameRepository

    @Before
    fun setup() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), GameDatabase::class.java).build()
        repo = GameRepository(db)
        repo.refresh()
        repo.create(Player(name = "Freya", heroClass = 1, created = LocalDate.now().minusDays(30).toString()), emptyList())
    }

    @After
    fun close() {
        db.close()
    }

    private suspend fun reachLevel(targetLevel: Int) {
        var idx = 0
        while (repo.state.value!!.level < targetLevel) {
            val h = Habit(id = "lvl_${idx++}", name = "Marco $idx", xp = 400, created = LocalDate.now().minusDays(30).toString())
            repo.habit(h)
            repo.progress(h, today(), 1)
        }
    }

    private suspend fun recordRegisteredFocusSession(habit: Habit, seconds: Long = 900L, register: Boolean = true) {
        repo.startFocus(habit, 25)
        val active = db.dao().focus()!!
        if (seconds > 0L) {
            db.dao().focus(active.copy(elapsed = seconds, startedAt = 0L))
        } else {
            db.dao().focus(active.copy(elapsed = 0L, startedAt = 0L))
        }
        repo.endFocus(register = register)
    }

    @Test
    fun atlasStatesAndLevelGatesRespectAllSixRegions() = runBlocking {
        val lvl1 = repo.state.value!!
        assertEquals(RegionAtlasState.Available, regionAtlasState(lvl1, 0))
        assertEquals(RegionAtlasState.Locked, regionAtlasState(lvl1, 1))
        assertEquals(RegionAtlasState.Locked, regionAtlasState(lvl1, 2))
        assertEquals(RegionAtlasState.Locked, regionAtlasState(lvl1, 3))
        assertEquals(RegionAtlasState.Locked, regionAtlasState(lvl1, 4))
        assertEquals(RegionAtlasState.Locked, regionAtlasState(lvl1, 5))

        reachLevel(5)
        val lvl5 = repo.state.value!!
        assertEquals(RegionAtlasState.Available, regionAtlasState(lvl5, 1))
        assertEquals(RegionAtlasState.Available, regionAtlasState(lvl5, 2))
        assertEquals(RegionAtlasState.Locked, regionAtlasState(lvl5, 3))

        reachLevel(20)
        val lvl20 = repo.state.value!!
        assertEquals(RegionAtlasState.Available, regionAtlasState(lvl20, 3))
        // Regions 4 and 5 must remain explicitly non-interactive in 1.7.2
        assertEquals(RegionAtlasState.NoInteractiveYet, regionAtlasState(lvl20, 4))
        assertEquals(RegionAtlasState.NoInteractiveYet, regionAtlasState(lvl20, 5))
        assertEquals("Sem conteúdo interativo ainda", RegionAtlasState.NoInteractiveYet.label)
    }

    @Test
    fun bibliotecaDasErasFullPlaythroughWithCaminhosMimirAndAntiSpam() = runBlocking {
        reachLevel(5)
        assertTrue(expeditionAvailable(repo.state.value!!, libraryExpeditions[0]))
        assertFalse(expeditionAvailable(repo.state.value!!, libraryExpeditions[1]))

        // Pre-existing completed PathTask must NOT count toward lib_portico
        val path = PersonalPath(id = "path_estudo", name = "Estudo Nórdico")
        repo.savePath(path)
        val preTask = PathTask(id = "t_pre", pathId = path.id, title = "Leitura antiga", completed = false)
        repo.saveTask(preTask)
        repo.toggleTask(preTask.id, true)

        // 1) O Primeiro Manuscrito via EXPEDITION_METRIC_PATH_TASKS (2 + 2 tasks across 2 phases)
        repo.startExpedition("lib_portico", emptyList(), EXPEDITION_METRIC_PATH_TASKS)
        assertEquals(RegionAtlasState.InProgress, regionAtlasState(repo.state.value!!, 2))
        val p0 = repo.state.value!!.quests.first { it.id == "exp:lib_portico:0" }
        assertEquals(0L, questProgress(p0, repo.state.value!!))

        val t1 = PathTask(id = "t1", pathId = path.id, title = "Catalogar notas 1")
        val t2 = PathTask(id = "t2", pathId = path.id, title = "Catalogar notas 2")
        repo.saveTask(t1); repo.saveTask(t2)
        repo.toggleTask(t1.id, true); repo.toggleTask(t2.id, true)
        assertEquals(2L, questProgress(repo.state.value!!.quests.first { it.id == "exp:lib_portico:0" }, repo.state.value!!))
        repo.advanceExpedition("lib_portico")

        // Phase 2 must start at 0 and NOT reuse t1/t2
        val p1 = repo.state.value!!.quests.first { it.id == "exp:lib_portico:1" }
        assertEquals(0L, questProgress(p1, repo.state.value!!))
        val t3 = PathTask(id = "t3", pathId = path.id, title = "Catalogar notas 3")
        val t4 = PathTask(id = "t4", pathId = path.id, title = "Catalogar notas 4")
        repo.saveTask(t3); repo.saveTask(t4)
        repo.toggleTask(t3.id, true); repo.toggleTask(t4.id, true)

        // Concurrent advance must be idempotent (only 1 succeeds)
        val xpBeforePortico = repo.state.value!!.xp
        val concurrent = coroutineScope { (1..6).map { async { runCatching { repo.advanceExpedition("lib_portico") } } }.awaitAll() }
        assertEquals(1, concurrent.count { it.isSuccess })
        assertTrue(expeditionComplete(repo.state.value!!, libraryExpeditions[0]))
        assertEquals(xpBeforePortico + 55, repo.state.value!!.xp)
        assertTrue(repo.state.value!!.achievements.any { it.id == "library_portico" })
        assertEquals(1, libraryStage(repo.state.value!!))

        // 2) As Páginas do Silêncio via EXPEDITION_METRIC_FLASHCARD_DAYS (anti-spam: multiple reviews same day = 1 day)
        val nb = KnowledgeNotebook(id = "nb1", name = "Runas", description = "Estudos")
        repo.saveNotebook(nb)
        val fc1 = Flashcard(id = "fc1", notebookId = nb.id, question = "Fehu", answer = "Riqueza", nextReviewDate = today())
        val fc2 = Flashcard(id = "fc2", notebookId = nb.id, question = "Ansuz", answer = "Sabedoria", nextReviewDate = today())
        repo.saveFlashcard(fc1); repo.saveFlashcard(fc2)

        // Pre-phase review on today() must be snapshotted into baseline
        repo.reviewFlashcard(fc1.id, 2, today())
        repo.startExpedition("lib_acervo", emptyList(), EXPEDITION_METRIC_FLASHCARD_DAYS)
        val a0 = repo.state.value!!.quests.first { it.id == "exp:lib_acervo:0" }
        assertEquals(0L, questProgress(a0, repo.state.value!!))

        // Multiple reviews on day+1 count as ONLY 1 distinct day
        val fc3 = Flashcard(id = "fc3", notebookId = nb.id, question = "Raido", answer = "Jornada", nextReviewDate = today())
        val fc4 = Flashcard(id = "fc4", notebookId = nb.id, question = "Kenaz", answer = "Chama", nextReviewDate = today())
        val fc5 = Flashcard(id = "fc5", notebookId = nb.id, question = "Dagaz", answer = "Alvorada", nextReviewDate = today())
        repo.saveFlashcard(fc3); repo.saveFlashcard(fc4); repo.saveFlashcard(fc5)

        val d1 = LocalDate.now().plusDays(1).toString()
        val d2 = LocalDate.now().plusDays(2).toString()
        val d3 = LocalDate.now().plusDays(3).toString()
        val d4 = LocalDate.now().plusDays(4).toString()
        repo.reviewFlashcard(fc1.id, 2, d1)
        repo.reviewFlashcard(fc2.id, 3, d1)
        assertEquals(1L, questProgress(repo.state.value!!.quests.first { it.id == "exp:lib_acervo:0" }, repo.state.value!!))
        repo.advanceExpedition("lib_acervo")

        // Phase 2 requires 1 new distinct day (d1 is in baseline)
        assertEquals(0L, questProgress(repo.state.value!!.quests.first { it.id == "exp:lib_acervo:1" }, repo.state.value!!))
        repo.reviewFlashcard(fc3.id, 2, d2)
        repo.advanceExpedition("lib_acervo")

        // Phase 3 requires 2 new distinct days (d3, d4); multiple reviews on d3 still count as 1
        repo.reviewFlashcard(fc4.id, 2, d3)
        repo.reviewFlashcard(fc5.id, 2, d3) // same day -> still 1/2
        assertEquals(1L, questProgress(repo.state.value!!.quests.first { it.id == "exp:lib_acervo:2" }, repo.state.value!!))
        val d4Card = Flashcard(id = "fc6", notebookId = nb.id, question = "Mannaz", answer = "Herói", nextReviewDate = d4)
        repo.saveFlashcard(d4Card)
        repo.reviewFlashcard(d4Card.id, 3, d4)
        repo.advanceExpedition("lib_acervo")
        assertTrue(expeditionComplete(repo.state.value!!, libraryExpeditions[1]))
        assertTrue(repo.state.value!!.assets.any { it.id == "scribes-ring" })

        // 3) O Juramento da Sabedoria (Vigia das Eras — 3 narrative phases) via HABIT FALLBACK
        val fallbackHabits = (1..8).map { i ->
            Habit(id = "lib_h_$i", name = "Estudo $i", xp = 0).also { repo.habit(it) }
        }
        repo.startExpedition("lib_camara", fallbackHabits.map { it.id }, EXPEDITION_METRIC_HABITS)
        var used = 0
        for (phase in 0..2) {
            val q = repo.state.value!!.quests.first { it.id == "exp:lib_camara:$phase" }
            assertEquals(EXPEDITION_METRIC_HABITS, q.metric)
            repeat(q.target) {
                repo.progress(fallbackHabits[used++], today(), 1)
            }
            repo.advanceExpedition("lib_camara")
        }

        val finalState = repo.state.value!!
        assertEquals(100, libraryProgress(finalState))
        assertEquals(2, libraryStage(finalState))
        assertEquals(RegionAtlasState.Completed, regionAtlasState(finalState, 2))
        assertTrue(finalState.assets.any { it.id == "eternal-memory-medallion" })
        assertTrue(finalState.achievements.any { it.id == "library_ages" })
        assertTrue(specializationTitles(finalState).contains("Guardião das Eras"))
        assertTrue(hallMemories(finalState).any { it.detail.contains("Biblioteca das Eras") })
    }

    @Test
    fun montanhasDaDisciplinaFocusSessionsRestDaysAndRelics() = runBlocking {
        reachLevel(8)
        assertTrue(expeditionAvailable(repo.state.value!!, mountainsExpeditions[0]))

        // Timed habit for Focus sessions
        val timed = Habit(id = "foco_ritual", name = "Meditação Fria", type = 2, target = 20, unit = "min", xp = 0)
        repo.habit(timed)

        // 1) A Trilha de Ferro via habit fallback (2 + 3 habits)
        val trailHabits = (1..5).map { i -> Habit(id = "mnt_h_$i", name = "Subida $i", xp = 0).also { repo.habit(it) } }
        repo.startExpedition("mnt_trilha", trailHabits.map { it.id }, EXPEDITION_METRIC_HABITS)
        repo.progress(trailHabits[0], today(), 1); repo.progress(trailHabits[1], today(), 1)
        repo.advanceExpedition("mnt_trilha")
        repo.progress(trailHabits[2], today(), 1); repo.progress(trailHabits[3], today(), 1); repo.progress(trailHabits[4], today(), 1)
        repo.advanceExpedition("mnt_trilha")
        assertTrue(expeditionComplete(repo.state.value!!, mountainsExpeditions[0]))
        assertTrue(repo.state.value!!.achievements.any { it.id == "mountains_trail" })
        assertEquals(1, mountainsStage(repo.state.value!!))

        // Pre-phase Focus session must NOT count toward mnt_torre
        recordRegisteredFocusSession(timed, seconds = 600L, register = true)

        // 2) A Chama que Não Cessa via EXPEDITION_METRIC_FOCUS_SESSIONS (1 + 2 + 2 sessions)
        repo.startExpedition("mnt_torre", emptyList(), EXPEDITION_METRIC_FOCUS_SESSIONS)
        // Cancelled and 0-second sessions MUST NOT count
        recordRegisteredFocusSession(timed, seconds = 600L, register = false)
        recordRegisteredFocusSession(timed, seconds = 0L, register = true)
        assertEquals(0L, questProgress(repo.state.value!!.quests.first { it.id == "exp:mnt_torre:0" }, repo.state.value!!))

        // Valid registered session counts
        recordRegisteredFocusSession(timed, seconds = 900L, register = true)
        assertEquals(1L, questProgress(repo.state.value!!.quests.first { it.id == "exp:mnt_torre:0" }, repo.state.value!!))
        repo.advanceExpedition("mnt_torre")

        // Enabling rest days must NEVER penalize or revert expedition progress
        repo.rest(today(), true)
        assertTrue(expeditionClaimValid(repo.state.value!!, mountainsExpeditions[1], 0))
        repo.rest(today(), false)

        // Phase 2 (2 new sessions)
        assertEquals(0L, questProgress(repo.state.value!!.quests.first { it.id == "exp:mnt_torre:1" }, repo.state.value!!))
        recordRegisteredFocusSession(timed, seconds = 600L, register = true)
        recordRegisteredFocusSession(timed, seconds = 600L, register = true)
        repo.advanceExpedition("mnt_torre")

        // Phase 3 (2 new sessions)
        recordRegisteredFocusSession(timed, seconds = 600L, register = true)
        recordRegisteredFocusSession(timed, seconds = 600L, register = true)
        repo.advanceExpedition("mnt_torre")
        assertTrue(expeditionComplete(repo.state.value!!, mountainsExpeditions[1]))
        assertTrue(repo.state.value!!.assets.any { it.id == "boreal-vigil-brooch" })

        // 3) O Silêncio dos Cumes (Guardião de Granito) via EXPEDITION_METRIC_FOCUS_SESSIONS (1 + 2 + 2)
        repo.startExpedition("mnt_observatorio", emptyList(), EXPEDITION_METRIC_FOCUS_SESSIONS)
        for (need in listOf(1, 2, 2)) {
            repeat(need) { recordRegisteredFocusSession(timed, seconds = 600L, register = true) }
            repo.advanceExpedition("mnt_observatorio")
        }

        val s = repo.state.value!!
        assertEquals(100, mountainsProgress(s))
        assertEquals(2, mountainsStage(s))
        assertEquals(RegionAtlasState.Completed, regionAtlasState(s, 3))
        assertTrue(s.assets.any { it.id == "firmament-compass" })
        assertTrue(s.achievements.any { it.id == "mountains_discipline" })
        assertTrue(specializationTitles(s).contains("Testemunha dos Cumes"))
        assertTrue(hallMemories(s).any { it.detail.contains("Montanhas da Disciplina") })

        // Relics can be equipped and NEVER purchased directly from shop
        val compassAsset = s.assets.first { it.id == "firmament-compass" }
        repo.equip(compassAsset)
        assertTrue(repo.state.value!!.assets.first { it.id == "firmament-compass" }.equipped)
        for (relicId in expeditionRelicIds) {
            val item = shopItems.first { it.id == relicId }
            assertTrue("Relic $relicId must not be purchasable", runCatching { repo.buy(item) }.isFailure)
            assertTrue("Relic $relicId must have expedition origin", itemOrigin(item).startsWith("Expedição:"))
        }
    }

    @Test
    fun npcsRespondToProgressAndSpecializationsAndBackupRoundTripsCleanly() = runBlocking {
        reachLevel(8)
        // First meeting lines before starting any expedition
        val (eydisFirst, eydisNoSpec) = npcDialogue(eydisNpc, repo.state.value!!)
        assertEquals(eydisNpc.firstMeeting, eydisFirst)
        assertNull(eydisNoSpec)

        val (haldorFirst, haldorNoSpec) = npcDialogue(haldorNpc, repo.state.value!!)
        assertEquals(haldorNpc.firstMeeting, haldorFirst)
        assertNull(haldorNoSpec)

        // Selecting Erudito specialization (heroClass = 1 -> Mago) adds personalized flavor
        repo.selectSpecialization("erudito")
        val (_, eydisErudito) = npcDialogue(eydisNpc, repo.state.value!!)
        val (_, haldorErudito) = npcDialogue(haldorNpc, repo.state.value!!)
        assertNotNull(eydisErudito)
        assertNotNull(haldorErudito)
        assertTrue(eydisErudito!!.contains("Erudito"))
        assertTrue(haldorErudito!!.contains("Erudito"))

        // Start a Library expedition so Eydis shifts to availableLine and ExplorationJournal tracks it
        repo.startExpedition("lib_portico", emptyList(), EXPEDITION_METRIC_PATH_TASKS)
        val (eydisAvail, _) = npcDialogue(eydisNpc, repo.state.value!!)
        assertEquals(eydisNpc.availableLine, eydisAvail)

        val journal = explorationJournal(repo.state.value!!)
        assertEquals(1, journal.activeExpeditions.size)
        assertEquals("lib_portico", journal.activeExpeditions.first().first.id)
        assertEquals(23, journal.totalAvailableMarks) // 7 Forest + 8 Library + 8 Mountains

        // Personal sagas MUST reject expedition-only metrics 4..6
        val badSaga = Quest(name = "Inválida", metric = EXPEDITION_METRIC_FOCUS_SESSIONS, target = 2, xp = 10)
        assertTrue(runCatching { repo.saga(badSaga) }.isFailure)

        // Backup v10 round-trip preserves active integrated expedition state 100%
        val snapshot = repo.state.value!!
        val encoded = Backup.encode(snapshot)
        val decoded = Backup.decode(encoded)
        assertEquals(snapshot, decoded)
        repo.importState(decoded)
        assertEquals(snapshot, repo.state.value)

        // Legacy v8/v9 backup envelopes decode cleanly without inventing exploration progress
        val gson = Gson()
        val payload = gson.toJson(snapshot.copy(quests = emptyList(), claims = emptyList()))
        val digest = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray()).joinToString("") { "%02x".format(it) }
        val legacyDecoded = Backup.decode(gson.toJson(BackupEnvelope(version = 9, sha256 = digest, payload = payload)))
        assertEquals(0, libraryProgress(legacyDecoded))
        assertEquals(0, mountainsProgress(legacyDecoded))
    }
}
