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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = AscendApp::class)
class RunicHeritageTest {
    private val context get() = RuntimeEnvironment.getApplication() as AscendApp
    private lateinit var db: GameDatabase
    private lateinit var repo: GameRepository

    @Before
    fun setup() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = GameRepository(db)
        repo.refresh()
        repo.create(Player(name = "Astrid", heroClass = 4, face = 0), emptyList())
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun s(): GameState = repo.state.value!!

    private suspend fun grantProgress(targetLevel: Int, extraCoins: Long = 0L) {
        var guard = 0
        while (s().level < targetLevel && guard < 120) {
            db.dao().reward(
                Reward(
                    id = "seed-lv-$guard",
                    date = today(),
                    label = "Treino de herói",
                    xp = 250,
                    coins = 50,
                    attribute = 2,
                    points = 10
                )
            )
            repo.refresh()
            guard++
        }
        if (extraCoins > 0L) {
            db.dao().reward(
                Reward(
                    id = "seed-coins-${System.nanoTime()}",
                    date = today(),
                    label = "Tesouro de teste",
                    xp = 0,
                    coins = extraCoins
                )
            )
            repo.refresh()
        }
    }

    private suspend fun unlockAllForgeRequirements() {
        grantProgress(targetLevel = 12, extraCoins = 3200L)
        repo.saveNotebook(KnowledgeNotebook(id = "nb-forge", name = "Códice de Mímir"))
        db.dao().unlock(Unlocked(id = "forest", date = today()))
        db.dao().unlock(Unlocked(id = "library_portico", date = today()))
        db.dao().unlock(Unlocked(id = "mountains_trail", date = today()))
        db.dao().unlock(Unlocked(id = "mimir_first_rune", date = today()))
        val path = PersonalPath(id = "p-forge", name = "Trilha do Norte", description = "Constância")
        repo.savePath(path)
        repo.saveTask(PathTask(id = "pt-1", pathId = path.id, title = "Passo 1", completed = true, completedDate = today()))
        repo.savePath(path.copy(state = 3, completedDate = today()))
        val h = Habit(id = "h-foco", name = "Estudo focado", type = 2, target = 15, unit = "min", xp = 30, coins = 10)
        repo.habit(h)
        repo.progress(h, today(), 15)
        repo.refresh()
        repo.restore(1)
        repo.restore(1)
    }

    // =========================================================================
    // 6.1: Equipamento Idempotente (equipItem / unequipItem / setEquipment)
    // =========================================================================

    @Test
    fun idempotentEquipmentCommandsEnforceOwnershipSlotAndZeroEconomyImpact() = runBlocking {
        grantProgress(targetLevel = 4, extraCoins = 500L)
        val cloak = shopItems.first { it.id == "cloak" }
        val armor = shopItems.first { it.id == "armor" }
        repo.buy(cloak)
        repo.buy(armor)
        val coinsBefore = s().coins
        val xpBefore = s().xp

        // Repeated equipItem stays equipped (never toggles off)
        repo.equipItem(cloak.id)
        repo.equipItem(cloak.id)
        assertTrue(s().assets.first { it.id == cloak.id }.equipped)

        // Repeated unequipItem stays unequipped
        repo.unequipItem(cloak.id)
        repo.unequipItem(cloak.id)
        assertFalse(s().assets.first { it.id == cloak.id }.equipped)

        // Switching within the same slot guarantees strictly 1 equipped item per slot
        repo.setEquipment("outfit", cloak.id)
        repo.setEquipment("outfit", armor.id)
        assertFalse(s().assets.first { it.id == cloak.id }.equipped)
        assertTrue(s().assets.first { it.id == armor.id }.equipped)

        // Clearing slot via setEquipment(slot, null)
        repo.setEquipment("outfit", null)
        assertTrue(s().assets.none { it.kind == "outfit" && it.equipped })

        // Validation: unowned item and wrong slot fail
        assertTrue(runCatching { repo.equipItem("oath-helm") }.isFailure)
        assertTrue(runCatching { repo.setEquipment("weapon", cloak.id) }.isFailure)

        // Concurrent repeated equipItem calls stay consistent & idempotent
        val results = coroutineScope {
            (1..6).map { async { runCatching { repo.equipItem(cloak.id) } } }.awaitAll()
        }
        assertTrue(results.all { it.isSuccess })
        assertTrue(s().assets.first { it.id == cloak.id }.equipped)
        assertEquals(1, s().assets.count { it.kind == "outfit" && it.equipped })

        // Zero economic side effects
        assertEquals(coinsBefore, s().coins)
        assertEquals(xpBefore, s().xp)
    }

    // =========================================================================
    // 6.2: Retrocompatibilidade Versionada das Expedições de Foco
    // =========================================================================

    @Test
    fun expeditionFocusRuleVersionPreservesLegacyPhasesAndEnforces300sOnNewPhases() = runBlocking {
        grantProgress(targetLevel = 8, extraCoins = 100L)
        val h = Habit(id = "h-exp", name = "Foco curto", type = 2, target = 1, unit = "min", xp = 10, coins = 5)
        repo.habit(h)
        // Insert a 120s (< 300s) completed focus session today
        val now = System.currentTimeMillis()
        db.dao().focusSession(
            FocusSession(
                id = "fs-short-120",
                habitId = h.id,
                date = today(),
                startedAt = now - 120_000L,
                endedAt = now,
                plannedMinutes = 2,
                elapsedSeconds = 120L,
                status = "completed",
                registered = true
            )
        )
        repo.refresh()

        // 1) Legacy unclaimed focus phase (exp:mnt_torre:0, metric = 4, focusRuleVersion = 0) counts the 120s session
        val legacyQuest = Quest(
            id = "exp:mnt_torre:0",
            name = "Fase legada",
            description = "Iniciada na 1.7.2",
            period = 2,
            metric = 4,
            target = 1,
            xp = 50,
            coins = 20,
            sagaStart = today(),
            sagaBaseline = "",
            focusRuleVersion = EXPEDITION_FOCUS_RULE_LEGACY_1S
        )
        assertEquals(1L, expeditionMetricProgress(legacyQuest, s()))

        // Legacy claim (focusRuleVersion = 0) remains valid across reconcile
        db.dao().quest(legacyQuest)
        val legacyClaim = Claim(
            id = "${legacyQuest.id}@${today()}",
            questId = legacyQuest.id,
            occurrence = today(),
            date = today(),
            xp = 50,
            coins = 20,
            target = 1,
            metric = 4,
            period = 2,
            sagaStart = today(),
            sagaBaseline = "",
            focusRuleVersion = EXPEDITION_FOCUS_RULE_LEGACY_1S
        )
        db.dao().claim(legacyClaim)
        repo.refresh()
        assertTrue(rawClaimValid(legacyClaim, s()))

        // 2) New focus phase generated by expeditionPhaseQuest sets focusRuleVersion = 1 (300s)
        val generatedNewFocusPhase = expeditionPhaseQuest(mountainsExpeditions[1], 0, emptyList(), s(), EXPEDITION_METRIC_FOCUS_SESSIONS)
        assertEquals(EXPEDITION_FOCUS_RULE_V1_300S, generatedNewFocusPhase.focusRuleVersion)

        val newQuest = legacyQuest.copy(
            id = "exp:mnt_torre:1",
            focusRuleVersion = EXPEDITION_FOCUS_RULE_V1_300S
        )
        db.dao().quest(newQuest)
        repo.refresh()
        assertEquals(0L, expeditionMetricProgress(newQuest, s()))

        val newClaimWithShortOnly = legacyClaim.copy(
            id = "${newQuest.id}@${today()}",
            questId = newQuest.id,
            focusRuleVersion = EXPEDITION_FOCUS_RULE_V1_300S
        )
        assertFalse(rawClaimValid(newClaimWithShortOnly, s()))

        // Adding a 300s session satisfies the v1 phase & claim
        db.dao().focusSession(
            FocusSession(
                id = "fs-valid-300",
                habitId = h.id,
                date = today(),
                startedAt = now - 300_000L,
                endedAt = now,
                plannedMinutes = 5,
                elapsedSeconds = 300L,
                status = "completed",
                registered = true
            )
        )
        repo.refresh()
        assertEquals(1L, expeditionMetricProgress(newQuest, s()))
        assertTrue(rawClaimValid(newClaimWithShortOnly, s()))
    }

    // =========================================================================
    // 6.3: Aplicação Parcial Segura de Configurações (Loadouts)
    // =========================================================================

    @Test
    fun partialLoadoutPreviewAndApplyPreserveUnrelatedEquippedSlots() = runBlocking {
        grantProgress(targetLevel = 5, extraCoins = 600L)
        repo.buy(shopItems.first { it.id == "traveler-helm" })
        repo.buy(shopItems.first { it.id == "silver-blade" })
        repo.buy(shopItems.first { it.id == "corvus" })

        // Currently equipped: weapon=silver-blade, companion=corvus (head empty)
        repo.equipItem("silver-blade")
        repo.equipItem("corvus")

        // Persisted partial loadout (e.g. after backup/state change) referencing owned traveler-helm + missing north-mail
        val partial = EquipmentLoadout(
            id = "loadout:1",
            name = "Jornada Parcial",
            headId = "traveler-helm",
            outfitId = "north-mail" // NOT owned
        )
        db.dao().loadout(partial)
        repo.refresh()

        val preview = previewLoadoutApplication(partial, s())
        assertEquals(listOf("traveler-helm"), preview.availableItemIds)
        assertEquals(listOf("north-mail"), preview.missingItemIds)
        assertTrue(preview.preservesOtherSlots)
        assertEquals(1, preview.slotChanges.size)
        assertEquals("head", preview.slotChanges.first().kind)
        assertEquals("traveler-helm", preview.slotChanges.first().nextItemId)

        // Applying partial loadout equips traveler-helm AND preserves silver-blade & corvus!
        repo.applyLoadout("loadout:1")
        assertTrue(s().assets.first { it.id == "traveler-helm" }.equipped)
        assertTrue(s().assets.first { it.id == "silver-blade" }.equipped)
        assertTrue(s().assets.first { it.id == "corvus" }.equipped)
    }

    // =========================================================================
    // Pilar 1: Heráldica Rúnica de Aetheris
    // =========================================================================

    @Test
    fun heraldryCatalogUnlocksValidationTransliterationAndPersistence() = runBlocking {
        assertEquals(8, heraldrySymbols.size)
        assertEquals(4, heraldryShapes.size)
        assertEquals(4, heraldryMetals.size)
        assertEquals(5, heraldrySecondaryColors.size)
        assertEquals(5, heraldryOrnaments.size)
        assertEquals(3, heraldryBanners.size)

        // Starters unlocked at level 1
        assertTrue(heraldrySymbolUnlocked(s(), "raven"))
        assertTrue(heraldrySymbolUnlocked(s(), "oak"))
        assertTrue(heraldrySymbolUnlocked(s(), "shield"))
        assertTrue(heraldrySymbolUnlocked(s(), "mountain"))
        assertTrue(heraldryMetalUnlocked(s(), "iron"))
        assertTrue(heraldryMetalUnlocked(s(), "bronze"))
        assertTrue(heraldryBannerUnlocked(s(), "wool"))

        // Locked options fail validation at level 1
        val lockedProfile = HeraldryProfile(
            symbolId = "axe", // requires Forge stage 1+
            shapeId = "round_shield",
            metalId = "gold", // requires level 8+ or consecrated / collection
            secondaryColorId = "fjord_blue",
            inscriptionRunes = "Ansuz,Othala",
            ornamentId = "none",
            bannerId = "wool"
        )
        assertTrue(runCatching { repo.saveHeraldry(lockedProfile) }.isFailure)

        // > 6 runes fails validation
        val tooManyRunes = HeraldryProfile(
            symbolId = "raven",
            inscriptionRunes = "Fehu,Uruz,Thurisaz,Ansuz,Raido,Kenaz,Gebo"
        )
        assertTrue(runCatching { repo.saveHeraldry(tooManyRunes) }.isFailure)

        // Transliteration maps short word cleanly (max 6 runes)
        val translit = approximateRuneTransliteration("Norte")
        assertEquals(5, translit.size)
        assertEquals(HistoricalFutharkRune.Nauthiz, translit[0])
        assertEquals(HistoricalFutharkRune.Othala, translit[1])
        assertTrue(approximateRuneTransliteration("AetherisLongo").size <= 6)

        // Saving valid starter heraldry persists with 0 XP / 0 coins impact
        val coinsBefore = s().coins
        val xpBefore = s().xp
        val valid = HeraldryProfile(
            symbolId = "oak",
            shapeId = "runic_plaque",
            metalId = "bronze",
            secondaryColorId = "forest_moss",
            inscriptionRunes = "Ansuz,Raido,Othala",
            ornamentId = "iron_filigree",
            bannerId = "leather",
            showOnPortrait = true
        )
        repo.saveHeraldry(valid)
        assertEquals("oak", s().heraldry?.symbolId)
        assertEquals("runic_plaque", s().heraldry?.shapeId)
        assertEquals("Ansuz,Raido,Othala", s().heraldry?.inscriptionRunes)
        assertEquals(coinsBefore, s().coins)
        assertEquals(xpBefore, s().xp)
    }

    // =========================================================================
    // Pilar 2: Codex Rúnico (24 Elder Futhark) & Fallback Explícito
    // =========================================================================

    @Test
    fun elderFuthark24RunesPreserveOriginal12AndExplicitSealFallback() {
        assertEquals(24, HistoricalFutharkRune.entries.size)
        assertEquals(24, codexRuneEntries.size)
        assertEquals(6, codexSurfaces.size)
        assertEquals((1..24).toList(), HistoricalFutharkRune.canonicalOrder.map { it.futharkOrder })

        // Original 12 rune ordinals preserved 0..11
        val original12 = listOf(
            HistoricalFutharkRune.Fehu,
            HistoricalFutharkRune.Ansuz,
            HistoricalFutharkRune.Raido,
            HistoricalFutharkRune.Kenaz,
            HistoricalFutharkRune.Gebo,
            HistoricalFutharkRune.Wunjo,
            HistoricalFutharkRune.Isa,
            HistoricalFutharkRune.Berkano,
            HistoricalFutharkRune.Ehwaz,
            HistoricalFutharkRune.Mannaz,
            HistoricalFutharkRune.Laguz,
            HistoricalFutharkRune.Dagaz
        )
        original12.forEachIndexed { idx, rune ->
            assertEquals("Original rune ${rune.name} must preserve ordinal $idx", idx, rune.ordinal)
        }

        // Every AetherisSealKind maps explicitly to a valid historical rune
        AetherisSealKind.entries.forEach { seal ->
            assertNotNull(sealFallbackRune(seal))
        }

        // Historical disclaimer differentiates Elder (24) vs Younger Futhark (16)
        assertTrue(CODEX_HISTORICAL_DISCLAIMER.contains("24"))
        assertTrue(CODEX_HISTORICAL_DISCLAIMER.contains("16"))
        assertTrue(CODEX_HISTORICAL_DISCLAIMER.contains("Younger Futhark"))
    }

    // =========================================================================
    // Pilares 3, 4 e 5: Forja (12 receitas), Aprimoramentos (3 estágios) & 3 Coleções Lendárias
    // =========================================================================

    @Test
    fun forge12RecipesThreeStageUpgradesAndThreeLegendaryCollections() = runBlocking {
        unlockAllForgeRequirements()

        assertEquals(12, forgeRecipes.size)
        assertEquals(3, legendaryCollections.size)
        assertEquals(12, legendaryCollections.flatMap { it.itemIds }.distinct().size)

        // New 4 recipes are blocked from Shop
        val new4 = listOf("vigil-axe", "runic-staff", "northern-blade", "constellation-brooch")
        new4.forEach { id ->
            val item = shopItems.first { it.id == id }
            assertTrue("New Forge item $id must be blocked in Shop", runCatching { repo.buy(item) }.isFailure)
        }

        // Craft all 4 items of Sabedoria de Mímir (note: scribe-robes & rune-mantle share "outfit" slot!)
        val xpBefore = s().xp
        val mimirItems = listOf("mimir-runic-crown", "scribe-robes", "runic-staff", "rune-mantle")
        mimirItems.forEach { repo.craftForgeItem(it) }
        assertEquals(xpBefore, s().xp) // 0 XP from crafting

        // Ledger uses forge:<recipeId>
        assertTrue(s().ledger.any { it.id == "forge:runic-staff" })

        // Collection completes automatically by ownership even when 2 pieces share the outfit slot
        val mimirCol = legendaryCollectionById("mimir_wisdom")!!
        assertTrue(isCollectionComplete(s(), mimirCol))
        assertTrue(collectionRewardClaimed(s(), mimirCol))
        assertTrue("Herdeiro de Mímir" in specializationTitles(s()))
        assertTrue("Herdeiro de Mímir" in completedCollectionTitles(s()))

        // Repeated claimCollectionReward is idempotent with 0 XP / 0 coins
        val coinsAfterCol = s().coins
        repo.claimCollectionReward("mimir_wisdom")
        repo.claimCollectionReward("mimir_wisdom")
        assertEquals(coinsAfterCol, s().coins)
        assertEquals(xpBefore, s().xp)

        // Independent from the 3 Equipment Loadouts (Jornada / Conselho / Expedição)
        assertEquals(3, resolvedLoadouts(s()).size)

        // 3-Stage Forge Upgrades: Forjado (1) -> Gravado (2) -> Consagrado (3)
        val staffRecipe = forgeRecipeById("runic-staff")!!
        val staffAsset = s().assets.first { it.id == "runic-staff" }
        assertEquals(FORGE_STAGE_FORGED, staffAsset.stage)

        // Cannot skip directly from 1 to 3
        assertTrue(runCatching { repo.upgradeForgeItem("runic-staff", FORGE_STAGE_CONSECRATED) }.isFailure)
        // Cannot upgrade unowned or non-forge item
        assertTrue(runCatching { repo.upgradeForgeItem("vigil-axe", FORGE_STAGE_ENGRAVED) }.isFailure)
        assertTrue(runCatching { repo.upgradeForgeItem("traveler-helm", FORGE_STAGE_ENGRAVED) }.isFailure)

        // Concurrent upgrade to Stage 2 charges exactly once and yields 0 XP
        val coinsBeforeUp2 = s().coins
        val up2Results = coroutineScope {
            (1..5).map { async { runCatching { repo.upgradeForgeItem("runic-staff", FORGE_STAGE_ENGRAVED) } } }.awaitAll()
        }
        assertEquals(1, up2Results.count { it.isSuccess })
        assertEquals(FORGE_STAGE_ENGRAVED, s().assets.first { it.id == "runic-staff" }.stage)
        assertEquals(coinsBeforeUp2 - forgeUpgradeCost(staffRecipe, FORGE_STAGE_ENGRAVED), s().coins)
        assertTrue(s().ledger.any { it.id == "forge-upgrade:runic-staff:2" })
        assertEquals(xpBefore, s().xp)

        // Upgrade Stage 2 -> Stage 3 (Consagrado)
        val coinsBeforeUp3 = s().coins
        repo.upgradeForgeItem("runic-staff", FORGE_STAGE_CONSECRATED)
        assertEquals(FORGE_STAGE_CONSECRATED, s().assets.first { it.id == "runic-staff" }.stage)
        assertEquals(coinsBeforeUp3 - forgeUpgradeCost(staffRecipe, FORGE_STAGE_CONSECRATED), s().coins)
        assertTrue(s().ledger.any { it.id == "forge-upgrade:runic-staff:3" })
        assertEquals(xpBefore, s().xp)

        // Cannot upgrade beyond stage 3
        assertTrue(runCatching { repo.upgradeForgeItem("runic-staff", 4) }.isFailure)
        assertTrue(upgradeVisualScopeFor("runic-staff").isNotBlank())
    }

    // =========================================================================
    // Pilar 6: Arsenal 2.1 — Busca, Filtros Combináveis e Ordenação
    // =========================================================================

    @Test
    fun arsenalSearchCombinableFiltersAndSortingWorkDeterministically() = runBlocking {
        unlockAllForgeRequirements()
        repo.craftForgeItem("oath-helm")
        repo.craftForgeItem("vigil-axe")
        repo.upgradeForgeItem("vigil-axe", FORGE_STAGE_ENGRAVED)
        repo.equipItem("vigil-axe")
        repo.buy(shopItems.first { it.id == "night" })
        repo.buy(shopItems.first { it.id == "frame" })

        // Category separation: Fundos (6) vs Molduras (7)
        val bgs = filterAndSortArsenalItems(s(), store = false, ArsenalFilterState(category = 6))
        assertEquals(listOf("night"), bgs.map { it.id })
        val frames = filterAndSortArsenalItems(s(), store = false, ArsenalFilterState(category = 7))
        assertEquals(listOf("frame"), frames.map { it.id })

        // Ownership filters: Equipados (1) & Fabricáveis (3)
        val equipped = filterAndSortArsenalItems(s(), store = false, ArsenalFilterState(ownership = 1))
        assertEquals(listOf("vigil-axe"), equipped.map { it.id })
        val craftable = filterAndSortArsenalItems(s(), store = false, ArsenalFilterState(ownership = 3))
        assertEquals(10, craftable.size) // 12 - 2 crafted

        // Upgrade stage filter: Gravado (2)
        val engraved = filterAndSortArsenalItems(s(), store = false, ArsenalFilterState(upgradeStage = FORGE_STAGE_ENGRAVED))
        assertEquals(listOf("vigil-axe"), engraved.map { it.id })

        // Collection filter: Juramento de Ferro returns all 4 items of the collection
        val ironOath = filterAndSortArsenalItems(s(), store = false, ArsenalFilterState(collectionId = "iron_oath"))
        assertEquals(4, ironOath.size)

        // Search query by name/collection
        val queryResult = filterAndSortArsenalItems(s(), store = false, ArsenalFilterState(query = "Vigília", ownership = 0))
        assertTrue(queryResult.any { it.id == "vigil-axe" })

        // Sorting by Stage puts Gravado (stage 2) ahead of Forjado (stage 1)
        val byStage = filterAndSortArsenalItems(s(), store = false, ArsenalFilterState(origin = "forge", sort = ArsenalSort.STAGE))
        assertEquals("vigil-axe", byStage.first().id)
    }

    // =========================================================================
    // Room v9 -> v10 Migration & Backup v12 Compatibility + Atomic Rollback
    // =========================================================================

    @Test
    fun roomMigration9To10AndBackupV12RoundTripWithV11CompatAndAtomicRollback() = runBlocking {
        unlockAllForgeRequirements()
        repo.craftForgeItem("oath-helm")
        repo.craftForgeItem("vigil-axe")
        repo.upgradeForgeItem("vigil-axe", FORGE_STAGE_ENGRAVED)
        repo.saveCurrentAsLoadout("loadout:1", "Vigília Real")
        repo.saveHeraldry(
            HeraldryProfile(
                symbolId = "axe",
                shapeId = "oval_shield",
                metalId = "silver",
                secondaryColorId = "fjord_blue",
                inscriptionRunes = "Ansuz,Othala",
                ornamentId = "silver_ringerike",
                bannerId = "ceremonial",
                showOnPortrait = true
            )
        )

        val before = s()

        // 1) Backup v12 round-trip
        val encodedV12 = Backup.encode(before)
        assertTrue(encodedV12.contains("\"version\":13"))
        val decodedV12 = Backup.decode(encodedV12)
        assertEquals(before.heraldry, decodedV12.heraldry)
        assertEquals(FORGE_STAGE_ENGRAVED, decodedV12.assets.first { it.id == "vigil-axe" }.stage)
        repo.importState(decodedV12)
        assertEquals(before.heraldry, s().heraldry)

        // 2) Backward compatibility with Backup v11 (without heraldry or focusRuleVersion)
        val gson = Gson()
        val obj = gson.toJsonTree(before).asJsonObject
        obj.remove("heraldry")
        obj["quests"].asJsonArray.forEach { it.asJsonObject.remove("focusRuleVersion") }
        obj["claims"].asJsonArray.forEach { it.asJsonObject.remove("focusRuleVersion") }
        val payloadV11 = gson.toJson(obj)
        val shaV11 = MessageDigest.getInstance("SHA-256")
            .digest(payloadV11.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        val v11Decoded = Backup.decode(gson.toJson(BackupEnvelope(version = 11, sha256 = shaV11, payload = payloadV11)))
        assertNull(v11Decoded.heraldry)
        assertTrue(v11Decoded.quests.all { it.focusRuleVersion == 0 })
        assertTrue(v11Decoded.claims.all { it.focusRuleVersion == 0 })

        // 3) Atomic rollback on invalid heraldry or invalid upgrade stage
        val badHeraldry = before.copy(heraldry = HeraldryProfile(symbolId = "invalid_sym"))
        assertTrue(runCatching { repo.importState(badHeraldry) }.isFailure)
        assertEquals("axe", s().heraldry?.symbolId)

        val badStage = before.copy(
            assets = before.assets.map { if (it.id == "vigil-axe") it.copy(stage = 4) else it }
        )
        assertTrue(runCatching { repo.importState(badStage) }.isFailure)
        assertEquals(FORGE_STAGE_ENGRAVED, s().assets.first { it.id == "vigil-axe" }.stage)

        // 4) Real SQLite Room v9 -> v10 migration preserves loadouts, forged assets, and sets legacy focusRuleVersion = 0
        val dbName = "migration-v9-to-v10.db"
        context.deleteDatabase(dbName)
        legacyDatabase(context, dbName, 9, before.copy(heraldry = null))
        val migrated = Room.databaseBuilder(context, GameDatabase::class.java, dbName)
            .addMigrations(
                MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11
            )
            .allowMainThreadQueries()
            .build()
        try {
            val migRepo = GameRepository(migrated)
            migRepo.refresh()
            val st = migRepo.state.value!!
            assertEquals("Astrid", st.player?.name)
            assertEquals("Vigília Real", st.loadouts.first { it.id == "loadout:1" }.name)
            assertEquals(FORGE_STAGE_ENGRAVED, st.assets.first { it.id == "vigil-axe" }.stage)
            assertNull(st.heraldry)
            assertTrue(st.quests.all { it.focusRuleVersion == 0 })

            // Newly migrated heraldry_profile table is fully writable
            migRepo.saveHeraldry(HeraldryProfile(symbolId = "raven", inscriptionRunes = "Fehu,Ansuz"))
            assertEquals("raven", migRepo.state.value!!.heraldry?.symbolId)
        } finally {
            migrated.close()
            context.deleteDatabase(dbName)
        }
    }
}
