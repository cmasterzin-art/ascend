package br.com.ascend.aetheris

import androidx.room.Room
import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
class LivingHeroAndForgeTest {
    private lateinit var db: GameDatabase
    private lateinit var repo: GameRepository
    private val context get() = RuntimeEnvironment.getApplication() as AscendApp
    private val created = LocalDate.now().minusDays(20).toString()
    private val timedHabit = Habit(
        id = "h-focus-forge",
        name = "Estudo focado",
        attribute = 0,
        type = 2,
        target = 25,
        unit = "min",
        xp = 80,
        coins = 15,
        points = 5,
        created = created
    )

    private fun s() = repo.state.value!!

    @Before
    fun setup() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java).build()
        repo = GameRepository(db)
        repo.refresh()
        repo.create(Player(name = "Ragnar", heroClass = 4, face = 2, created = created), listOf(timedHabit))
    }

    @After
    fun close() {
        db.close()
    }

    private suspend fun grantProgress(targetLevel: Int, extraCoins: Long = 1000L) {
        var guard = 0
        while ((s().level < targetLevel || s().coins < extraCoins) && guard < 60) {
            db.dao().reward(
                Reward(
                    id = "seed-forge-$guard",
                    date = today(),
                    label = "Registro de teste",
                    xp = if (s().level < targetLevel) 220 else 0,
                    coins = if (s().coins < extraCoins) 80 else 0,
                    attribute = 2,
                    points = 10
                )
            )
            repo.refresh()
            guard++
        }
        assertTrue(s().level >= targetLevel)
        assertTrue(s().coins >= extraCoins)
    }

    private suspend fun recordFocusSession(habit: Habit, elapsedSeconds: Long, register: Boolean) {
        repo.startFocus(habit, 25)
        val active = db.dao().focus()!!
        db.dao().focus(active.copy(elapsed = elapsedSeconds, startedAt = 0L))
        repo.endFocus(register)
    }

    @Test
    fun livingPortraitCoversAllItemsAndAll10SpecializationsWithoutMutatingState() = runBlocking {
        // Every shop/forge/relic/skill item must have a deterministic representation
        shopItems.forEach { item ->
            val rep = portraitRepresentationFor(item)
            assertNotNull("Missing representation for ${item.id}", rep)
            assertTrue(rep.label.isNotBlank())
        }

        // All 10 specializations must have distinct visual themes with verified Elder Futhark runes
        assertEquals(10, specializationDefs.size)
        val themes = specializationDefs.map { spec ->
            val theme = specializationVisualTheme(spec.id)
            assertNotNull("Missing SpecializationVisualTheme for ${spec.id}", theme)
            theme!!
        }
        assertEquals(10, themes.map { it.label }.distinct().size)

        // Preview overrides reflect in resolveHeroLook without granting ownership or changing GameState
        val beforeAssets = s().assets.toList()
        val look = resolveHeroLook(
            s(),
            overrides = mapOf(
                "head" to "oath-helm",
                "outfit" to "vigil-armor",
                "accessory" to "yggdrasil-brooch",
                "frame" to "nine-realms-frame"
            )
        )
        assertEquals("oath-helm", look.headId)
        assertEquals("vigil-armor", look.outfitId)
        assertEquals("yggdrasil-brooch", look.accessoryId)
        assertEquals("nine-realms-frame", look.frameId)
        assertEquals(beforeAssets, s().assets)
    }

    @Test
    fun darianForgeRecipesStagesEconomyExclusivityAndConcurrentIdempotency() = runBlocking {
        assertEquals(12, forgeRecipes.size)
        assertEquals(12, forgeItemIds.size)

        // Stage 0 (ruínas): all 8 recipes are locked
        grantProgress(targetLevel = 10, extraCoins = 1200L)
        assertEquals(0, forgeStage(s()))
        forgeRecipes.forEach { r ->
            assertEquals("Recipe ${r.id} must be locked at stage 0", ForgeRecipeState.Locked, forgeRecipeState(s(), r))
            assertTrue("Crafting ${r.id} at stage 0 must fail", runCatching { repo.craftForgeItem(r.id) }.isFailure)
        }

        // Forge items are NEVER purchasable from the Shop
        forgeItemIds.forEach { id ->
            val item = shopItems.first { it.id == id }
            assertTrue("Forge item $id must not be buyable", runCatching { repo.buy(item) }.isFailure)
            assertTrue("Forge item $id origin must cite Darian", itemOrigin(item).contains("Forja de Darian"))
        }

        // Expedition relics are NEVER craftable in the Forge
        expeditionRelicIds.forEach { relicId ->
            assertNull("Expedition relic $relicId must not exist in forgeRecipes", forgeRecipeById(relicId))
            assertTrue(runCatching { repo.craftForgeItem(relicId) }.isFailure)
        }

        // Restore building:1 Stage 1 (preserves 2-stage village restoration)
        repo.restore(1)
        assertEquals(1, forgeStage(s()))
        assertEquals(ForgeRecipeState.Available, forgeRecipeState(s(), forgeRecipeById("oath-helm")!!))
        assertEquals(ForgeRecipeState.Locked, forgeRecipeState(s(), forgeRecipeById("rune-mantle")!!))

        // Concurrent crafting idempotency: 6 parallel attempts succeed ONCE, debit 45 coins ONCE, grant 0 XP
        val coinsBefore = s().coins
        val xpBefore = s().xp
        val results = (1..6).map {
            async { runCatching { repo.craftForgeItem("oath-helm") } }
        }.awaitAll()
        assertEquals(1, results.count { it.isSuccess })
        assertEquals(5, results.count { it.isFailure })
        assertEquals(coinsBefore - 45L, s().coins)
        assertEquals(xpBefore, s().xp)
        assertEquals(1, s().assets.count { it.id == "oath-helm" })
        assertEquals(1, s().ledger.count { it.id == "forge:oath-helm" })
        assertEquals(ForgeRecipeState.Crafted, forgeRecipeState(s(), forgeRecipeById("oath-helm")!!))

        // Unlock achievements needed for remaining recipes and restore Stage 2
        repo.saveNotebook(KnowledgeNotebook(id = "nb-forge", name = "Notas da Forja"))
        assertTrue(s().achievements.any { it.id == "mimir_first_tome" })
        db.dao().unlock(Unlocked(id = "forest", date = today()))
        db.dao().unlock(Unlocked(id = "library_portico", date = today()))
        db.dao().unlock(Unlocked(id = "mountains_trail", date = today()))
        repo.refresh()
        repo.restore(1)
        assertEquals(2, forgeStage(s()))

        // Mestre da Forja grants exclusive dialogue & identity badge with ZERO economic discount
        repo.selectSpecialization("mestre_da_forja")
        assertTrue(isForgeMasterSpecialization(s()))
        val masterDialogue = darianWorkshopDialogue(s())
        assertTrue(masterDialogue.first.contains("Mestres da Forja"))
        val coinsBeforeMasterCraft = s().coins
        val xpBeforeMasterCraft = s().xp
        repo.craftForgeItem("vigil-armor") // exact cost 110, zero discount, zero XP
        assertEquals(coinsBeforeMasterCraft - 110L, s().coins)
        assertEquals(xpBeforeMasterCraft, s().xp)
        assertTrue(s().assets.any { it.id == "vigil-armor" })

        // Craft remaining recipes to verify all 8 succeed cleanly
        listOf("north-mail", "scribe-robes", "yggdrasil-brooch", "mimir-runic-crown", "rune-mantle", "nine-realms-frame").forEach { id ->
            val pre = s().coins
            val cost = forgeRecipeById(id)!!.cost.toLong()
            repo.craftForgeItem(id)
            assertEquals(pre - cost, s().coins)
            assertTrue(s().assets.any { it.id == id })
        }
        assertEquals(8, s().assets.count { it.id in forgeItemIds })
    }

    @Test
    fun studioGuardAndThreeLoadoutsSaveRenameApplyMissingItemsAndClear() = runBlocking {
        grantProgress(targetLevel = 8, extraCoins = 1000L)
        // Buy/craft several items across slots
        repo.restore(1)
        repo.restore(1)
        repo.craftForgeItem("oath-helm")
        repo.craftForgeItem("north-mail")
        repo.craftForgeItem("rune-mantle")
        repo.buy(shopItems.first { it.id == "traveler-helm" })
        repo.buy(shopItems.first { it.id == "frame" })

        // Studio MUST reject unowned/locked item — preview never grants ownership
        assertTrue(
            runCatching {
                repo.applyStudioSelection(mapOf("head" to "mimir-runic-crown"))
            }.isFailure
        )
        assertFalse(s().assets.any { it.id == "mimir-runic-crown" })

        // Studio applies valid owned combination atomically
        repo.applyStudioSelection(
            mapOf(
                "head" to "oath-helm",
                "outfit" to "north-mail",
                "frame" to "frame"
            )
        )
        assertTrue(s().assets.first { it.id == "oath-helm" }.equipped)
        assertTrue(s().assets.first { it.id == "north-mail" }.equipped)
        assertTrue(s().assets.first { it.id == "frame" }.equipped)

        // Default 3 loadouts exist
        val defaults = resolvedLoadouts(s())
        assertEquals(listOf("Jornada", "Conselho", "Expedição"), defaults.map { it.name })

        // Save current look into loadout:1 and switch to a different look in loadout:2
        repo.saveCurrentAsLoadout("loadout:1")
        repo.renameLoadout("loadout:1", "Vigília de Inverno")
        repo.applyStudioSelection(mapOf("head" to "traveler-helm", "outfit" to "rune-mantle"))
        repo.saveCurrentAsLoadout("loadout:2")

        // Apply loadout:1 restores oath-helm + north-mail + frame and unequips traveler-helm / rune-mantle
        repo.applyLoadout("loadout:1")
        assertEquals("Vigília de Inverno", resolvedLoadouts(s()).first { it.id == "loadout:1" }.name)
        assertTrue(s().assets.first { it.id == "oath-helm" }.equipped)
        assertTrue(s().assets.first { it.id == "north-mail" }.equipped)
        assertTrue(s().assets.first { it.id == "frame" }.equipped)
        assertFalse(s().assets.first { it.id == "traveler-helm" }.equipped)
        assertFalse(s().assets.first { it.id == "rune-mantle" }.equipped)

        // Incompatible slot assignment must fail validation
        assertTrue(
            runCatching {
                repo.saveLoadout(EquipmentLoadout(id = "loadout:3", name = "Inválido", outfitId = "oath-helm"))
            }.isFailure
        )

        // Unowned item cannot be saved directly via saveLoadout
        val loadoutWithMissing = EquipmentLoadout(
            id = "loadout:3",
            name = "Expedição",
            headId = "oath-helm",
            accessoryId = "yggdrasil-brooch" // not owned
        )
        assertTrue(runCatching { repo.saveLoadout(loadoutWithMissing) }.isFailure)

        // If a persisted loadout references a missing item, it is reported explicitly and skipped without silent substitution
        db.dao().loadout(loadoutWithMissing)
        repo.refresh()
        assertEquals(listOf("yggdrasil-brooch"), loadoutMissingItems(loadoutWithMissing, s()))
        repo.applyLoadout("loadout:3")
        assertTrue(s().assets.first { it.id == "oath-helm" }.equipped)
        assertFalse(s().assets.any { it.id == "yggdrasil-brooch" })

        // Clear loadout resets slots while preserving name
        repo.clearLoadout("loadout:3")
        assertTrue(loadoutIsEmpty(resolvedLoadouts(s()).first { it.id == "loadout:3" }))
    }

    @Test
    fun bug161FocusExpeditionEnforces5MinMinimumWhilePreservingLegacyClaims() = runBlocking {
        grantProgress(targetLevel = 8, extraCoins = 300L)
        // Complete first mountain expedition (mnt_trilha) via 6 simple habits on today() to unlock mnt_torre
        val trailHabits = (1..6).map { i ->
            Habit(id = "h-trail-$i", name = "Passo $i", type = 0, target = 1, unit = "vez", xp = 10, coins = 2, created = created)
        }
        trailHabits.forEach { repo.habit(it) }
        repo.startExpedition("mnt_trilha", trailHabits.map { it.id }, EXPEDITION_METRIC_HABITS)
        trailHabits.take(3).forEach { repo.progress(it, today(), 1) }
        repo.advanceExpedition("mnt_trilha")
        trailHabits.drop(3).forEach { repo.progress(it, today(), 1) }
        repo.advanceExpedition("mnt_trilha")

        // Start mnt_torre with EXPEDITION_METRIC_FOCUS_SESSIONS
        repo.startExpedition("mnt_torre", emptyList(), EXPEDITION_METRIC_FOCUS_SESSIONS)
        val q0 = { s().quests.first { it.id == "exp:mnt_torre:0" } }

        // Short focus sessions (< 300s = 5 min) MUST NOT advance expedition progress
        recordFocusSession(timedHabit, elapsedSeconds = 1L, register = true)
        recordFocusSession(timedHabit, elapsedSeconds = 60L, register = true)
        recordFocusSession(timedHabit, elapsedSeconds = 299L, register = true)
        recordFocusSession(timedHabit, elapsedSeconds = 600L, register = false) // cancelled
        assertEquals(0L, questProgress(q0(), s()))
        assertTrue(runCatching { repo.advanceExpedition("mnt_torre") }.isFailure)

        // Effective focus session >= 300s (exactly 5 min) DOES advance expedition progress
        recordFocusSession(timedHabit, elapsedSeconds = EXPEDITION_MIN_FOCUS_SECONDS, register = true)
        assertEquals(1L, questProgress(q0(), s()))
        repo.advanceExpedition("mnt_torre")
        assertTrue(expeditionClaimValid(s(), mountainsExpeditions[1], 0))

        // Legacy 1.7.2 claim compatibility: a pre-existing claim backed by a 120s session remains valid
        val nowMs = System.currentTimeMillis()
        val legacySession = FocusSession(
            id = "legacy-focus-120s",
            habitId = timedHabit.id,
            habitName = timedHabit.name,
            date = today(),
            startedAt = nowMs - 120_000L,
            endedAt = nowMs,
            elapsedSeconds = 120L,
            plannedMinutes = 10,
            status = "completed",
            registered = true
        )
        // Baseline recorded in 1.7.2 already captured all prior registered focus sessions >= 1s
        val legacyBaseline = s().focusSessions
            .filter { it.registered && it.status != "cancelled" && it.elapsedSeconds >= 1L }
            .sortedBy { it.id }
            .joinToString(";") { "${it.id}=1" }
        val legacyQuest = Quest(
            id = "exp:mnt_torre:1",
            name = "Legacy phase",
            target = 1,
            metric = EXPEDITION_METRIC_FOCUS_SESSIONS,
            sagaStart = today(),
            sagaBaseline = legacyBaseline
        )
        val legacyClaim = Claim(
            id = "legacy-claim-172",
            questId = legacyQuest.id,
            occurrence = "once",
            date = today(),
            target = 1,
            metric = EXPEDITION_METRIC_FOCUS_SESSIONS,
            sagaStart = today(),
            sagaBaseline = legacyBaseline
        )
        val stateWithLegacy = s().copy(
            quests = s().quests.filterNot { it.id == legacyQuest.id } + legacyQuest,
            claims = s().claims + legacyClaim,
            focusSessions = s().focusSessions + legacySession
        )
        assertTrue(rawClaimValid(legacyClaim, stateWithLegacy))
        // Deleting the backing session STILL invalidates the legacy claim
        val stateWithoutSession = stateWithLegacy.copy(
            focusSessions = stateWithLegacy.focusSessions.filterNot { it.id == legacySession.id }
        )
        assertFalse(rawClaimValid(legacyClaim, stateWithoutSession))
    }

    @Test
    fun bug162ArchivingCompletedPathTasksPreservesExpeditionCreditWhileReopeningOrDeletingRemovesIt() = runBlocking {
        grantProgress(targetLevel = 5, extraCoins = 200L)
        val path = PersonalPath(id = "path-archive-test", name = "Caminho das Runas")
        repo.savePath(path)

        // Task already completed & archived BEFORE expedition must enter baseline and never count
        val preTask = PathTask(id = "t-pre", pathId = path.id, title = "Anterior", completed = true, completedDate = today(), archived = true)
        repo.saveTask(preTask)

        repo.startExpedition("lib_portico", emptyList(), EXPEDITION_METRIC_PATH_TASKS)
        val q0 = { s().quests.first { it.id == "exp:lib_portico:0" } }
        assertEquals(0L, questProgress(q0(), s()))

        val t1 = PathTask(id = "t-1", pathId = path.id, title = "Tarefa 1")
        val t2 = PathTask(id = "t-2", pathId = path.id, title = "Tarefa 2")
        repo.saveTask(t1)
        repo.saveTask(t2)
        repo.toggleTask(t1.id, true)
        repo.toggleTask(t2.id, true)
        assertEquals(2L, questProgress(q0(), s()))
        repo.advanceExpedition("lib_portico")
        assertTrue(expeditionClaimValid(s(), libraryExpeditions[0], 0))

        // Administrative archiving of completed tasks MUST preserve credit & claim validity
        repo.saveTask(s().pathTasks.first { it.id == t1.id }.copy(archived = true))
        repo.saveTask(s().pathTasks.first { it.id == t2.id }.copy(archived = true))
        assertEquals(2L, questProgress(q0(), s()))
        assertTrue(expeditionClaimValid(s(), libraryExpeditions[0], 0))

        // Reopening (completed = false) removes credit & invalidates claim
        repo.toggleTask(t1.id, false)
        assertEquals(1L, questProgress(q0(), s()))
        assertFalse(expeditionClaimValid(s(), libraryExpeditions[0], 0))

        // Re-completing restores credit; deleting removes credit
        repo.toggleTask(t1.id, true)
        assertTrue(expeditionClaimValid(s(), libraryExpeditions[0], 0))
        repo.deleteTask(t2.id)
        assertEquals(1L, questProgress(q0(), s()))
        assertFalse(expeditionClaimValid(s(), libraryExpeditions[0], 0))
    }

    @Test
    fun backupV11RoundTripBackwardCompatibleV10AndAtomicRollbackOnCorruptLoadout() = runBlocking {
        grantProgress(targetLevel = 8, extraCoins = 800L)
        repo.restore(1)
        repo.craftForgeItem("oath-helm")
        repo.craftForgeItem("north-mail")
        repo.applyStudioSelection(mapOf("head" to "oath-helm", "outfit" to "north-mail"))
        repo.saveCurrentAsLoadout("loadout:1", "Guarda Real")

        val jsonV11 = Backup.encode(s())
        assertTrue(jsonV11.contains("\"version\":12"))
        assertTrue(jsonV11.contains("Guarda Real"))

        // Round-trip v12 preserves loadouts & forged assets
        val decodedV11 = Backup.decode(jsonV11)
        assertEquals("Guarda Real", decodedV11.loadouts.first { it.id == "loadout:1" }.name)
        assertEquals("oath-helm", decodedV11.loadouts.first { it.id == "loadout:1" }.headId)

        // Legacy v10 backup (without loadouts field) decodes cleanly with empty loadouts list
        val gson = Gson()
        val payloadObj = gson.toJsonTree(s()).asJsonObject
        payloadObj.remove("loadouts")
        val legacyPayload = gson.toJson(payloadObj)
        val sha = MessageDigest.getInstance("SHA-256")
            .digest(legacyPayload.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        val legacyV10Json = gson.toJson(BackupEnvelope(version = 10, sha256 = sha, payload = legacyPayload))
        val decodedV10 = Backup.decode(legacyV10Json)
        assertTrue(decodedV10.loadouts.isEmpty())
        assertEquals("Ragnar", decodedV10.player?.name)

        // Atomic rollback: importing corrupt loadout (wrong item kind in headId) fails and preserves current DB
        val corruptState = s().copy(
            loadouts = listOf(EquipmentLoadout(id = "loadout:1", name = "Corrompido", headId = "north-mail"))
        )
        assertTrue(runCatching { repo.importState(corruptState) }.isFailure)
        assertEquals("Guarda Real", s().loadouts.first { it.id == "loadout:1" }.name)
    }

    @Test
    fun roomMigration8To9PreservesExistingDataAndCreatesEquipmentLoadoutsTable() = runBlocking {
        grantProgress(targetLevel = 6, extraCoins = 400L)
        repo.restore(1)
        repo.restore(1)
        repo.selectSpecialization("mestre_da_forja")
        repo.saveNotebook(KnowledgeNotebook(id = "nb-v8", name = "Tomo v8"))
        val before = s()

        val dbName = "migration-v8-to-v9.db"
        context.deleteDatabase(dbName)
        legacyDatabase(context, dbName, 8, before)

        val migrated = Room.databaseBuilder(context, GameDatabase::class.java, dbName)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
            .allowMainThreadQueries()
            .build()
        try {
            val migRepo = GameRepository(migrated)
            migRepo.refresh()
            val st = migRepo.state.value!!
            assertEquals("Ragnar", st.player?.name)
            assertEquals("mestre_da_forja", st.player?.specialization)
            assertEquals(2, forgeStage(st))
            assertEquals(1, st.notebooks.size)
            assertTrue(st.loadouts.isEmpty())

            // Saving a loadout into the newly migrated equipment_loadouts table works
            migRepo.saveLoadout(EquipmentLoadout(id = "loadout:1", name = "Jornada"))
            assertEquals(1, migRepo.state.value!!.loadouts.size)
        } finally {
            migrated.close()
            context.deleteDatabase(dbName)
        }
    }
}
