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
class MimirAndArchetypesTest {
    private lateinit var db: GameDatabase
    private lateinit var repo: GameRepository
    private val context get() = RuntimeEnvironment.getApplication() as AscendApp
    private val created = LocalDate.now().minusDays(14).toString()
    private val studyHabit = Habit(
        id = "h-mimir-study",
        name = "Leitura profunda",
        attribute = 2,
        type = 2,
        target = 30,
        xp = 120,
        coins = 10,
        points = 5,
        created = created
    )

    private fun s() = repo.state.value!!

    @Before
    fun setup() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java).build()
        repo = GameRepository(db)
        repo.refresh()
        repo.create(Player(name = "Astrid", heroClass = 1, created = created), listOf(studyHabit))
    }

    @After
    fun close() {
        db.close()
    }

    private suspend fun grantXpToReachLevel(targetLevel: Int) {
        var guard = 0
        while (s().level < targetLevel && guard < 40) {
            db.dao().reward(
                Reward(
                    id = "xp-boost-$guard",
                    date = today(),
                    label = "Treino histórico",
                    xp = 200,
                    coins = 0,
                    attribute = 2,
                    points = 10
                )
            )
            repo.refresh()
            guard++
        }
        assertTrue("Expected level >= $targetLevel, got ${s().level}", s().level >= targetLevel)
    }

    @Test
    fun notebooksPagesTagsSearchSortDuplicateMoveAndCascadeDelete() = runBlocking {
        val nb1 = KnowledgeNotebook(id = "nb-1", name = "  Runas Antigas ", description = "Estudos de epigrafia", category = "Estudo", cover = 0)
        val nb2 = KnowledgeNotebook(id = "nb-2", name = "Arquitetura", description = "Notas de engenharia", category = "Projeto", cover = 5)
        repo.saveNotebook(nb1)
        repo.saveNotebook(nb2)
        assertEquals(2, s().notebooks.size)
        assertEquals("Runas Antigas", s().notebooks.first { it.id == "nb-1" }.name)

        val p1 = KnowledgePage(
            id = "pg-1",
            notebookId = "nb-1",
            title = "Futhark Antigo",
            content = "# Futhark\n\nEstudo de **inscrições** em pedra e `algoritmos`.\n\n> Sabedoria exige silêncio.",
            tags = " #Runas, história , RUNAS ; epigrafia "
        )
        val p2 = KnowledgePage(
            id = "pg-2",
            notebookId = "nb-1",
            title = "Anotações de Campo",
            content = "Resumo de leitura sobre sagas islandesas.",
            tags = "sagas, leitura"
        )
        repo.savePage(p1)
        repo.savePage(p2)
        val savedP1 = s().knowledgePages.first { it.id == "pg-1" }
        assertEquals("Runas, história, epigrafia", savedP1.tags)

        // Toggle favorite
        repo.togglePageFavorite("pg-1")
        assertTrue(s().knowledgePages.first { it.id == "pg-1" }.favorite)

        // Search & Sort
        assertEquals(listOf("pg-1"), searchKnowledgePages(s().knowledgePages, query = "epigrafia").map { it.id })
        assertEquals(listOf("pg-1"), searchKnowledgePages(s().knowledgePages, query = "algoritmos").map { it.id })
        assertEquals(listOf("pg-1"), searchKnowledgePages(s().knowledgePages, favoritesOnly = true).map { it.id })
        assertEquals(listOf("pg-2", "pg-1"), searchKnowledgePages(s().knowledgePages, sort = PageSort.NAME).map { it.id })

        // Add reference & duplicate page
        repo.saveReference(
            KnowledgeReference(
                id = "ref-1",
                pageId = "pg-1",
                author = "Snorri Sturluson",
                title = "Edda em Prosa",
                year = "1220",
                source = "Manuscrito de Uppsala",
                location = "Cap. II"
            )
        )
        repo.duplicatePage("pg-1")
        assertEquals(3, s().knowledgePages.size)
        val copy = s().knowledgePages.first { it.id != "pg-1" && it.id != "pg-2" }
        assertEquals("Futhark Antigo (cópia)", copy.title)
        assertFalse(copy.favorite)
        assertEquals(1, s().knowledgeReferences.count { it.pageId == copy.id })

        // Move page to nb-2
        repo.movePage(copy.id, "nb-2")
        assertEquals("nb-2", s().knowledgePages.first { it.id == copy.id }.notebookId)

        // Archive and unarchive notebook
        repo.archiveNotebook("nb-2", true)
        assertTrue(s().notebooks.first { it.id == "nb-2" }.archived)
        repo.archiveNotebook("nb-2", false)
        assertFalse(s().notebooks.first { it.id == "nb-2" }.archived)

        // Add flashcard & review in nb-1, then cascade delete nb-1
        val card = Flashcard(id = "fc-1", notebookId = "nb-1", pageId = "pg-1", question = "O que é Futhark?", answer = "Alfabeto rúnico antigo.")
        repo.saveFlashcard(card)
        repo.reviewFlashcard("fc-1", rating = 2, reviewDate = today())
        repo.linkPages("pg-1", "pg-2")

        repo.deleteNotebook("nb-1")
        assertTrue(s().notebooks.none { it.id == "nb-1" })
        assertTrue(s().knowledgePages.none { it.notebookId == "nb-1" })
        assertTrue(s().knowledgeLinks.isEmpty())
        assertTrue(s().knowledgeReferences.none { it.pageId == "pg-1" })
        assertTrue(s().flashcards.none { it.notebookId == "nb-1" })
        assertTrue(s().flashcardReviews.none { it.flashcardId == "fc-1" })
        // nb-2 and its moved copy remain intact
        assertEquals(1, s().notebooks.size)
        assertEquals(1, s().knowledgePages.size)
    }

    @Test
    fun pageLinksBacklinksReferencesAndCleanDeletionWithoutOrphans() = runBlocking {
        repo.saveNotebook(KnowledgeNotebook(id = "nb-l", name = "Códigos"))
        repo.savePage(KnowledgePage(id = "p-a", notebookId = "nb-l", title = "Princípios"))
        repo.savePage(KnowledgePage(id = "p-b", notebookId = "nb-l", title = "Aplicações"))
        repo.savePage(KnowledgePage(id = "p-c", notebookId = "nb-l", title = "Síntese"))

        // Self-link rejected
        var selfLinkFailed = false
        try {
            repo.linkPages("p-a", "p-a")
        } catch (_: IllegalArgumentException) {
            selfLinkFailed = true
        }
        assertTrue(selfLinkFailed)

        repo.linkPages("p-a", "p-b")
        repo.linkPages("p-c", "p-b")
        assertEquals(listOf("p-b"), outgoingLinkedPages(s(), "p-a").map { it.id })
        assertEquals(listOf("p-a", "p-c"), backlinkPages(s(), "p-b").map { it.id })

        // Rename target page: links and backlinks stay intact
        repo.savePage(s().knowledgePages.first { it.id == "p-b" }.copy(title = "Aplicações Avançadas"))
        assertEquals(listOf("Aplicações Avançadas"), outgoingLinkedPages(s(), "p-a").map { it.title })
        assertEquals(listOf("p-a", "p-c"), backlinkPages(s(), "p-b").map { it.id })

        // Attach reference and flashcard to p-b, then delete p-b
        repo.saveReference(KnowledgeReference(id = "ref-b", pageId = "p-b", title = "Artigo Base", author = "Autor"))
        repo.saveFlashcard(Flashcard(id = "fc-b", notebookId = "nb-l", pageId = "p-b", question = "Pergunta?", answer = "Resposta."))
        repo.deletePage("p-b")

        assertTrue(s().knowledgeLinks.isEmpty())
        assertTrue(s().knowledgeReferences.isEmpty())
        assertEquals("", s().flashcards.single { it.id == "fc-b" }.pageId)
    }

    @Test
    fun sm2DeterministicAlgorithmSanitizationAndReviewRules() = runBlocking {
        val baseDate = "2026-05-01"
        val fresh = Flashcard(id = "c1", notebookId = "nb", question = "Q", answer = "A", nextReviewDate = baseDate)

        // Rating 0 (Errei) -> reset repetitions to 0, interval 1 day, ease drops by 0.20
        val r0 = computeNextReview(fresh, rating = 0, reviewDate = baseDate)
        assertEquals(0, r0.repetitions)
        assertEquals(1, r0.intervalDays)
        assertEquals(2.3, r0.easeFactor, 0.001)
        assertEquals("2026-05-02", r0.nextReviewDate)

        // Rating 2 (Bom): 1st -> 1d, 2nd -> 6d, 3rd -> round(6 * ease)
        val g1 = computeNextReview(fresh, rating = 2, reviewDate = baseDate)
        assertEquals(1, g1.repetitions)
        assertEquals(1, g1.intervalDays)
        assertEquals("2026-05-02", g1.nextReviewDate)

        val g2 = computeNextReview(
            fresh.copy(repetitions = g1.repetitions, intervalDays = g1.intervalDays, easeFactor = g1.easeFactor),
            rating = 2,
            reviewDate = g1.nextReviewDate
        )
        assertEquals(2, g2.repetitions)
        assertEquals(6, g2.intervalDays)
        assertEquals("2026-05-08", g2.nextReviewDate)

        val g3 = computeNextReview(
            fresh.copy(repetitions = g2.repetitions, intervalDays = g2.intervalDays, easeFactor = g2.easeFactor),
            rating = 2,
            reviewDate = g2.nextReviewDate
        )
        assertEquals(3, g3.repetitions)
        assertTrue(g3.intervalDays >= 12)

        // Rating 1 (Difícil) and 3 (Fácil)
        val hard1 = computeNextReview(fresh, rating = 1, reviewDate = baseDate)
        assertEquals(1, hard1.repetitions)
        assertEquals(1, hard1.intervalDays)
        assertTrue(hard1.easeFactor < 2.5)

        val easy1 = computeNextReview(fresh, rating = 3, reviewDate = baseDate)
        assertEquals(1, easy1.repetitions)
        assertEquals(4, easy1.intervalDays)
        assertTrue(easy1.easeFactor > 2.5)

        // Invalid / NaN / extreme easeFactor sanitization
        val badCard = fresh.copy(easeFactor = Double.NaN, repetitions = -3, intervalDays = -10)
        val sanitized = computeNextReview(badCard, rating = 2, reviewDate = baseDate)
        assertTrue(sanitized.easeFactor in 1.3..3.5)
        assertEquals(1, sanitized.repetitions)
        assertEquals(1, sanitized.intervalDays)

        val lowEase = computeNextReview(fresh.copy(easeFactor = 0.2), rating = 0, reviewDate = baseDate)
        assertEquals(1.3, lowEase.easeFactor, 0.001)
    }

    @Test
    fun flashcardSessionPersistenceNoExtraEconomyAndDuplicateReviewBlocked() = runBlocking {
        val d1 = LocalDate.now().minusDays(2).toString()
        val d2 = LocalDate.now().minusDays(1).toString()
        val d3 = today()

        repo.saveNotebook(KnowledgeNotebook(id = "nb-r", name = "Memória"))
        repo.saveFlashcard(Flashcard(id = "fc-1", notebookId = "nb-r", question = "Q1", answer = "A1", nextReviewDate = d1))
        repo.saveFlashcard(Flashcard(id = "fc-2", notebookId = "nb-r", question = "Q2", answer = "A2", nextReviewDate = d1))
        repo.saveFlashcard(Flashcard(id = "fc-arch", notebookId = "nb-r", question = "Q3", answer = "A3", nextReviewDate = d1, archived = true))

        assertEquals(listOf("fc-1", "fc-2"), dueFlashcards(s(), d1).map { it.id })

        val xpBefore = s().xp
        val coinsBefore = s().coins
        val ledgerBefore = s().ledger.size

        // Review only fc-1 (simulating an interrupted session before fc-2)
        repo.reviewFlashcard("fc-1", rating = 2, reviewDate = d1)
        assertEquals(xpBefore, s().xp)
        assertEquals(coinsBefore, s().coins)
        assertEquals(ledgerBefore, s().ledger.size)

        // fc-1 is now scheduled for d2, while fc-2 remains due on d1
        assertEquals(listOf("fc-2"), dueFlashcards(s(), d1).map { it.id })
        assertEquals(d2, s().flashcards.first { it.id == "fc-1" }.nextReviewDate)

        // Same-day duplicate review of fc-1 is blocked
        var duplicateBlocked = false
        try {
            repo.reviewFlashcard("fc-1", rating = 3, reviewDate = d1)
        } catch (_: IllegalArgumentException) {
            duplicateBlocked = true
        }
        assertTrue(duplicateBlocked)

        // Next day (d2): fc-1 is due again and progresses to 6-day interval
        repo.reviewFlashcard("fc-1", rating = 2, reviewDate = d2)
        assertEquals(6, s().flashcards.first { it.id == "fc-1" }.intervalDays)
        assertEquals(2, s().flashcardReviews.count { it.flashcardId == "fc-1" })

        // Archiving the notebook excludes remaining cards from dueFlashcards
        repo.archiveNotebook("nb-r", true)
        assertTrue(dueFlashcards(s(), d3).isEmpty())
    }

    @Test
    fun deletingPathOrTaskUnlinksKnowledgeWithoutDeletingNotebooksOrPages() = runBlocking {
        val path = PersonalPath(id = "path-m", name = "Estudo Nórdico")
        repo.savePath(path)
        val task = PathTask(id = "task-m", pathId = "path-m", title = "Ler Edda")
        repo.saveTask(task)

        repo.saveNotebook(KnowledgeNotebook(id = "nb-p", name = "Caderno do Caminho", pathId = "path-m"))
        repo.savePage(KnowledgePage(id = "pg-p", notebookId = "nb-p", title = "Nota vinculada", pathId = "path-m", taskId = "task-m"))

        repo.deleteTask("task-m")
        assertEquals("", s().knowledgePages.single { it.id == "pg-p" }.taskId)
        assertEquals("path-m", s().knowledgePages.single { it.id == "pg-p" }.pathId)

        repo.deletePath("path-m")
        assertEquals(1, s().notebooks.size)
        assertEquals(1, s().knowledgePages.size)
        assertEquals("", s().notebooks.single { it.id == "nb-p" }.pathId)
        assertEquals("", s().knowledgePages.single { it.id == "pg-p" }.pathId)
    }

    @Test
    fun archetypeSpecializationsSkillsAndPreservationAcrossSwitches() = runBlocking {
        // 5 classes × 2 specializations = 10 specializations; 6 universal + 10 class = 16 skills
        assertEquals(10, specializationDefs.size)
        for (cls in 0..4) {
            assertEquals(2, specializationsForClass(cls).size)
            assertEquals(2, skillDefs.count { it.heroClass == cls })
        }
        assertEquals(6, skillDefs.count { it.heroClass == -1 })

        // Level 1 cannot select specialization (requires level 3)
        var lowLevelRejected = false
        try {
            repo.selectSpecialization("erudito")
        } catch (_: IllegalArgumentException) {
            lowLevelRejected = true
        }
        assertTrue(lowLevelRejected)

        // Advance to level 6 -> 2 skill points available (level / 3 = 2)
        grantXpToReachLevel(6)
        assertTrue(skillPoints(s()) >= 2)

        // Wrong class specialization rejected (hero is class 1 = Mago; berserker is class 0)
        var wrongClassRejected = false
        try {
            repo.selectSpecialization("berserker")
        } catch (_: IllegalArgumentException) {
            wrongClassRejected = true
        }
        assertTrue(wrongClassRejected)

        // Select Erudito, unlock mage_mimir (lvl 3) and mage_runes (lvl 6, parent mage_mimir)
        repo.selectSpecialization("erudito")
        assertEquals("erudito", s().player!!.specialization)
        assertTrue("Leitor de Mímir" in specializationTitles(s()))

        val pointsBefore = skillPoints(s())
        val coinsBefore = s().coins
        repo.unlockSkill("mage_mimir")
        assertEquals(pointsBefore - 1, skillPoints(s()))
        assertEquals(coinsBefore, s().coins)
        assertTrue(s().assets.any { it.id == "skill:mage_mimir" })

        // Double unlock rejected
        var doubleUnlockRejected = false
        try {
            repo.unlockSkill("mage_mimir")
        } catch (_: IllegalArgumentException) {
            doubleUnlockRejected = true
        }
        assertTrue(doubleUnlockRejected)

        // Unlock second mage ascension skill
        repo.unlockSkill("mage_runes")
        assertTrue(s().assets.any { it.id == "skill:mage_runes" })

        // Switch specialization within same class: unlocked skills remain intact
        repo.selectSpecialization("arquimago_runico")
        assertEquals("arquimago_runico", s().player!!.specialization)
        assertTrue(s().assets.any { it.id == "skill:mage_mimir" })
        assertTrue(s().assets.any { it.id == "skill:mage_runes" })

        // Change main class to Explorador (3): incompatible specialization cleared, unlocked skills preserved!
        repo.player(s().player!!.copy(heroClass = 3))
        assertEquals("", s().player!!.specialization)
        assertTrue(s().assets.any { it.id == "skill:mage_mimir" })
        assertTrue(s().assets.any { it.id == "skill:mage_runes" })

        // Can now select Explorador specialization
        repo.selectSpecialization("cartografo")
        assertEquals("cartografo", s().player!!.specialization)

        // Can still unlock original universal skill (level 3) once points exist
        grantXpToReachLevel(9)
        repo.unlockSkill("scholar")
        assertTrue(s().assets.any { it.id == "skill:scholar" })
        assertTrue(s().assets.any { it.id == "scholar-bg" })
    }

    @Test
    fun knowledgeMilestonesAreIdempotentAndGrantZeroCoins() = runBlocking {
        val xpBefore = s().xp
        val coinsBefore = s().coins
        val ledgerBefore = s().ledger.size

        repo.saveNotebook(KnowledgeNotebook(id = "nb-ms", name = "Tomo I"))
        assertTrue(s().achievements.any { it.id == "mimir_first_tome" })

        for (i in 1..10) {
            repo.savePage(KnowledgePage(id = "pg-ms-$i", notebookId = "nb-ms", title = "Página $i", content = "Conteúdo $i"))
        }
        repo.linkPages("pg-ms-1", "pg-ms-2")
        repo.linkPages("pg-ms-2", "pg-ms-3")
        repo.linkPages("pg-ms-3", "pg-ms-4")
        assertTrue(s().achievements.any { it.id == "mimir_sage" })

        for (i in 1..10) {
            repo.saveFlashcard(
                Flashcard(
                    id = "fc-ms-$i",
                    notebookId = "nb-ms",
                    question = "P$i",
                    answer = "R$i",
                    nextReviewDate = LocalDate.now().minusDays(6).toString()
                )
            )
        }

        // Review all 10 distinct flashcards across 5 distinct days (2 per day)
        val d = LocalDate.now()
        for (offset in 4 downTo 0) {
            val day = d.minusDays(offset.toLong()).toString()
            val firstIdx = (4 - offset) * 2 + 1
            repo.reviewFlashcard("fc-ms-$firstIdx", rating = 2, reviewDate = day)
            repo.reviewFlashcard("fc-ms-${firstIdx + 1}", rating = 2, reviewDate = day)
        }
        assertTrue(s().achievements.any { it.id == "mimir_first_rune" })
        assertTrue(s().achievements.any { it.id == "mimir_ten_cards" })
        assertTrue(s().achievements.any { it.id == "mimir_five_days" })

        // Verify zero coins / zero XP / zero ledger entries created by knowledge milestones
        assertEquals(xpBefore, s().xp)
        assertEquals(coinsBefore, s().coins)
        assertEquals(ledgerBefore, s().ledger.size)

        // Delete & recreate notebook -> milestones remain idempotent (no duplicates, no coins)
        repo.deleteNotebook("nb-ms")
        repo.saveNotebook(KnowledgeNotebook(id = "nb-ms-2", name = "Tomo II"))
        assertEquals(1, s().achievements.count { it.id == "mimir_first_tome" })
        assertEquals(coinsBefore, s().coins)
    }

    @Test
    fun migration7To8PreservesAllV7DataAndSupportsMimirAndSpecializations() = runBlocking {
        repo.progress(studyHabit, today(), 30)
        val path = PersonalPath(id = "p-mig", name = "Projeto v7", state = 1)
        repo.savePath(path)
        repo.saveStage(PathStage(id = "st-mig", pathId = "p-mig", name = "Etapa 1"))
        repo.saveTask(PathTask(id = "tk-mig", pathId = "p-mig", stageId = "st-mig", title = "Tarefa v7", scheduledDate = today()))
        repo.saveSubtask(PathSubtask(id = "sb-mig", taskId = "tk-mig", title = "Subtarefa v7"))
        repo.linkHabitToPath("p-mig", studyHabit.id, true)
        repo.saveTimeBlock(TimeBlock(id = "tb-mig", title = "Bloco v7", date = today(), startTime = "09:00", endTime = "10:00", pathId = "p-mig"))
        repo.saveEvent(AgendaEvent(id = "ev-mig", title = "Evento v7", date = today(), startTime = "14:00", endTime = "15:00"))
        repo.saveInboxItem(InboxItem(id = "in-mig", title = "Captura v7"))
        val before = s()

        val dbName = "migration-v7-to-v8.db"
        context.deleteDatabase(dbName)
        legacyDatabase(context, dbName, 7, before)

        val migrated = Room.databaseBuilder(context, GameDatabase::class.java, dbName)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
            .allowMainThreadQueries()
            .build()
        try {
            val migratedRepo = GameRepository(migrated)
            migratedRepo.refresh()
            val after = migratedRepo.state.value!!

            assertEquals(before.player?.name, after.player?.name)
            assertEquals("", after.player?.specialization)
            assertEquals(before.habits.size, after.habits.size)
            assertEquals(before.records.size, after.records.size)
            assertEquals(before.paths.map { it.id }, after.paths.map { it.id })
            assertEquals(before.pathTasks.map { it.id }, after.pathTasks.map { it.id })
            assertEquals(before.timeBlocks.map { it.id }, after.timeBlocks.map { it.id })
            assertEquals(before.agendaEvents.map { it.id }, after.agendaEvents.map { it.id })
            assertEquals(before.inboxItems.map { it.id }, after.inboxItems.map { it.id })
            assertTrue(after.notebooks.isEmpty())
            assertTrue(after.knowledgePages.isEmpty())
            assertTrue(after.flashcards.isEmpty())

            // Can immediately create v8 entities on migrated database
            migratedRepo.saveNotebook(KnowledgeNotebook(id = "nb-post", name = "Caderno Pós-Migração", pathId = "p-mig"))
            migratedRepo.savePage(KnowledgePage(id = "pg-post", notebookId = "nb-post", title = "Página Pós-Migração", content = "# OK"))
            migratedRepo.saveFlashcard(Flashcard(id = "fc-post", notebookId = "nb-post", pageId = "pg-post", question = "Migrou?", answer = "Sim."))
            assertEquals(1, migratedRepo.state.value!!.notebooks.size)
            assertEquals(1, migratedRepo.state.value!!.knowledgePages.size)
            assertEquals(1, migratedRepo.state.value!!.flashcards.size)
        } finally {
            migrated.close()
            context.deleteDatabase(dbName)
        }
    }

    @Test
    fun backupV9BackwardCompatibilityV10RoundTripAndAtomicRollback() = runBlocking {
        // 1. Legacy v9 backup (without specialization or Mímir arrays) decodes cleanly with zero fictional data
        val legacyPayloadMap = linkedMapOf<String, Any?>(
            "player" to linkedMapOf(
                "id" to 1,
                "name" to "Legado9",
                "heroClass" to 0,
                "face" to 0,
                "color" to 0,
                "outfit" to 0,
                "frame" to 0,
                "title" to "O Desperto",
                "goals" to "",
                "created" to created
            ),
            "habits" to listOf(studyHabit),
            "plans" to emptyList<Any>(),
            "records" to emptyList<Any>(),
            "ledger" to emptyList<Any>(),
            "quests" to emptyList<Any>(),
            "claims" to emptyList<Any>(),
            "assets" to emptyList<Any>(),
            "achievements" to emptyList<Any>(),
            "journal" to emptyList<Any>(),
            "routines" to emptyList<Any>(),
            "restDays" to emptyList<Any>(),
            "focusSessions" to emptyList<Any>(),
            "paths" to emptyList<Any>(),
            "pathStages" to emptyList<Any>(),
            "pathTasks" to emptyList<Any>(),
            "pathSubtasks" to emptyList<Any>(),
            "pathHabitLinks" to emptyList<Any>(),
            "timeBlocks" to emptyList<Any>(),
            "agendaEvents" to emptyList<Any>(),
            "inboxItems" to emptyList<Any>(),
            "focus" to null
        )
        val gson = Gson()
        val payloadJson = gson.toJson(legacyPayloadMap)
        val sha = MessageDigest.getInstance("SHA-256").digest(payloadJson.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
        val v9Envelope = gson.toJson(BackupEnvelope(version = 9, sha256 = sha, payload = payloadJson))

        val decodedV9 = Backup.decode(v9Envelope)
        assertEquals("Legado9", decodedV9.player!!.name)
        assertEquals("", decodedV9.player!!.specialization)
        assertTrue(decodedV9.notebooks.isEmpty())
        assertTrue(decodedV9.knowledgePages.isEmpty())
        assertTrue(decodedV9.flashcards.isEmpty())

        // 2. Full v10 round-trip with Mímir + Archetypes
        grantXpToReachLevel(3)
        repo.selectSpecialization("erudito")
        repo.unlockSkill("mage_mimir")
        repo.saveNotebook(KnowledgeNotebook(id = "nb-bk", name = "Backup Mímir"))
        repo.savePage(KnowledgePage(id = "pg-bk1", notebookId = "nb-bk", title = "Nota 1", content = "## Markdown", tags = "runas, estudo", favorite = true))
        repo.savePage(KnowledgePage(id = "pg-bk2", notebookId = "nb-bk", title = "Nota 2", content = "Ligada"))
        repo.linkPages("pg-bk1", "pg-bk2")
        repo.saveReference(KnowledgeReference(id = "ref-bk", pageId = "pg-bk1", author = "Autor", title = "Obra"))
        repo.saveFlashcard(Flashcard(id = "fc-bk", notebookId = "nb-bk", pageId = "pg-bk1", question = "Pergunta", answer = "Resposta"))
        repo.reviewFlashcard("fc-bk", rating = 2, reviewDate = today())

        val snapshot = s()
        val encodedV10 = Backup.encode(snapshot)
        assertTrue(encodedV10.contains("\"version\":12"))
        val decodedV10 = Backup.decode(encodedV10)
        assertEquals("erudito", decodedV10.player!!.specialization)
        assertEquals(1, decodedV10.notebooks.size)
        assertEquals(2, decodedV10.knowledgePages.size)
        assertEquals(1, decodedV10.knowledgeLinks.size)
        assertEquals(1, decodedV10.knowledgeReferences.size)
        assertEquals(1, decodedV10.flashcards.size)
        assertEquals(1, decodedV10.flashcardReviews.size)

        // Restore into a fresh DB
        val db2 = Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java).build()
        try {
            val repo2 = GameRepository(db2)
            repo2.importState(decodedV10)
            val restored = repo2.state.value!!
            assertEquals("erudito", restored.player!!.specialization)
            assertEquals(snapshot.notebooks, restored.notebooks)
            assertEquals(snapshot.knowledgePages, restored.knowledgePages)
            assertEquals(snapshot.knowledgeLinks, restored.knowledgeLinks)
            assertEquals(snapshot.knowledgeReferences, restored.knowledgeReferences)
            assertEquals(snapshot.flashcards, restored.flashcards)
            assertEquals(snapshot.flashcardReviews, restored.flashcardReviews)

            // 3. Atomic rollback on corrupted/invalid state import (orphan page referencing missing notebook)
            val invalid = restored.copy(
                knowledgePages = restored.knowledgePages + KnowledgePage(id = "orphan", notebookId = "missing-nb", title = "Órfã")
            )
            var failed = false
            try {
                repo2.importState(invalid)
            } catch (_: IllegalArgumentException) {
                failed = true
            }
            assertTrue(failed)
            assertEquals(2, repo2.state.value!!.knowledgePages.size)
        } finally {
            db2.close()
        }
    }

    @Test
    fun movePageDeleteDestinationNotebookPreservesFlashcardAndRestoresBackupCleanly() = runBlocking {
        repo.saveNotebook(KnowledgeNotebook(id = "nb-src", name = "Origem"))
        repo.saveNotebook(KnowledgeNotebook(id = "nb-dst", name = "Destino"))
        repo.savePage(KnowledgePage(id = "pg-mv", notebookId = "nb-src", title = "Nota Migrada", content = "Conteúdo"))
        val yesterday = LocalDate.now().minusDays(1).toString()
        repo.saveFlashcard(
            Flashcard(
                id = "fc-keep",
                notebookId = "nb-src",
                pageId = "pg-mv",
                question = "O que preservar?",
                answer = "O flashcard e seu histórico.",
                nextReviewDate = yesterday
            )
        )
        repo.reviewFlashcard("fc-keep", rating = 2, reviewDate = yesterday)

        // Move page to nb-dst -> flashcard stays in nb-src with review history and cleanly unlinked pageId
        repo.movePage("pg-mv", "nb-dst")
        assertEquals("nb-dst", s().knowledgePages.single { it.id == "pg-mv" }.notebookId)
        val afterMoveCard = s().flashcards.single { it.id == "fc-keep" }
        assertEquals("nb-src", afterMoveCard.notebookId)
        assertEquals("", afterMoveCard.pageId)
        assertEquals(1, s().flashcardReviews.count { it.flashcardId == "fc-keep" })

        // Delete destination notebook -> backup export & restore succeed with full integrity
        repo.deleteNotebook("nb-dst")
        assertTrue(s().notebooks.none { it.id == "nb-dst" })
        assertTrue(s().knowledgePages.none { it.id == "pg-mv" })
        assertEquals(1, s().flashcards.count { it.id == "fc-keep" })
        assertEquals(1, s().flashcardReviews.count { it.flashcardId == "fc-keep" })

        val encoded = Backup.encode(s())
        val decoded = Backup.decode(encoded)
        val db2 = Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java).build()
        try {
            val repo2 = GameRepository(db2)
            repo2.importState(decoded)
            val restored = repo2.state.value!!
            assertEquals(1, restored.flashcards.size)
            assertEquals("fc-keep", restored.flashcards.single().id)
            assertEquals("", restored.flashcards.single().pageId)
            assertEquals(1, restored.flashcardReviews.size)
        } finally {
            db2.close()
        }
    }

    @Test
    fun deleteNotebookDefensivelyUnlinksLegacyCrossNotebookFlashcardPageId() = runBlocking {
        repo.saveNotebook(KnowledgeNotebook(id = "nb-a", name = "Caderno A"))
        repo.saveNotebook(KnowledgeNotebook(id = "nb-b", name = "Caderno B"))
        repo.savePage(KnowledgePage(id = "pg-b", notebookId = "nb-b", title = "Página em B"))
        // Simulate legacy 1.7.1 state where flashcard in nb-a points to pg-b in nb-b
        db.dao().flashcard(
            Flashcard(
                id = "fc-legacy",
                notebookId = "nb-a",
                pageId = "pg-b",
                question = "Legado?",
                answer = "Sim."
            )
        )
        repo.refresh()

        repo.deleteNotebook("nb-b")
        val surviving = s().flashcards.single { it.id == "fc-legacy" }
        assertEquals("", surviving.pageId)
        // Backup export & validation pass cleanly
        val decoded = Backup.decode(Backup.encode(s()))
        assertEquals("", decoded.flashcards.single { it.id == "fc-legacy" }.pageId)
    }

    @Test
    fun archivedNotebookDoesNotLeakIntoSearchFavoritesTagsOrReviews() = runBlocking {
        repo.saveNotebook(KnowledgeNotebook(id = "nb-act", name = "Ativo"))
        repo.saveNotebook(KnowledgeNotebook(id = "nb-arc", name = "Arquivado"))
        repo.savePage(
            KnowledgePage(
                id = "pg-act",
                notebookId = "nb-act",
                title = "Nota Ativa",
                content = "Texto sobre runas",
                tags = "runas, ativa",
                favorite = true
            )
        )
        repo.savePage(
            KnowledgePage(
                id = "pg-arc",
                notebookId = "nb-arc",
                title = "Nota Arquivada",
                content = "Texto secreto sobre runas",
                tags = "runas, oculta",
                favorite = true
            )
        )
        repo.linkPages("pg-act", "pg-arc")
        repo.saveFlashcard(
            Flashcard(
                id = "fc-arc",
                notebookId = "nb-arc",
                pageId = "pg-arc",
                question = "Q oculta",
                answer = "A oculta",
                nextReviewDate = LocalDate.now().plusDays(2).toString()
            )
        )

        repo.archiveNotebook("nb-arc", true)
        val archivedIds = archivedNotebookIds(s())
        assertEquals(setOf("nb-arc"), archivedIds)

        // Global active lists exclude archived notebook content
        assertEquals(listOf("pg-act"), activeKnowledgePages(s()).map { it.id })
        assertEquals(
            listOf("pg-act"),
            searchKnowledgePages(s().knowledgePages, query = "runas", archivedNotebookIds = archivedIds).map { it.id }
        )
        assertEquals(
            listOf("pg-act"),
            searchKnowledgePages(s().knowledgePages, favoritesOnly = true, archivedNotebookIds = archivedIds).map { it.id }
        )
        val tags = activeKnowledgeTags(s()).toMap()
        assertEquals(1, tags["runas"])
        assertNull(tags["oculta"])
        assertTrue(dueFlashcards(s(), LocalDate.now().plusDays(5).toString()).isEmpty())
        assertNull(nextFlashcardReviewDate(s()))

        // Explicit archived viewing preserves pages, links, and allows opening the archived notebook directly
        assertEquals(
            listOf("pg-arc"),
            searchKnowledgePages(s().knowledgePages, notebookId = "nb-arc", archivedNotebookIds = archivedIds).map { it.id }
        )
        assertEquals(
            setOf("pg-act", "pg-arc"),
            searchKnowledgePages(s().knowledgePages, includeArchived = true, archivedNotebookIds = archivedIds).map { it.id }.toSet()
        )
        assertEquals(listOf("pg-arc"), outgoingLinkedPages(s(), "pg-act").map { it.id })
    }

    @Test
    fun viewModelDraftStoreProtectsLargeUnsavedTextSafely() {
        val vm = GameViewModel(context)
        val largeMarkdown = "# Rascunho\n" + "Linha de estudo nórdico.\n".repeat(4000)
        vm.setDraft("page_edit:p1:content", largeMarkdown)
        assertTrue(vm.hasDraft("page_edit:p1:content"))
        assertEquals(largeMarkdown, vm.getDraft("page_edit:p1:content", ""))
        vm.clearDraftsWithPrefix("page_edit:p1:")
        assertFalse(vm.hasDraft("page_edit:p1:content"))
        assertEquals("fallback", vm.getDraft("page_edit:p1:content", "fallback"))
    }
}
