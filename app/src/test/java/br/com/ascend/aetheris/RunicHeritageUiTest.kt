package br.com.ascend.aetheris

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = AscendApp::class, qualifiers = "w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RunicHeritageUiTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()
    private val app get() = RuntimeEnvironment.getApplication() as AscendApp
    private val vm get() = ViewModelProvider(compose.activity)[GameViewModel::class.java]

    @Before
    fun setup() {
        runBlocking { app.preferences.edit { it.clear() } }
        wait { app.repository.state.value != null }
        vm.run {
            app.repository.create(Player(name = "Sigrid", heroClass = 4, face = 0), emptyList())
            vm.setting("reducedMotion", true)
        }
        wait { app.repository.state.value!!.player != null && vm.reducedMotion.value }
    }

    @After
    fun close() {
        compose.activityRule.scenario.close()
        app.database.close()
    }

    private fun wait(test: () -> Boolean) {
        compose.waitUntil(15000) {
            Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
            test()
        }
        compose.waitForIdle()
        val dismiss = compose.onAllNodesWithContentDescription("Dismiss")
        if (dismiss.fetchSemanticsNodes().isNotEmpty()) dismiss.onFirst().performClick()
    }

    private fun shot(name: String) {
        compose.waitForIdle()
        val dismiss = compose.onAllNodesWithContentDescription("Dismiss")
        if (dismiss.fetchSemanticsNodes().isNotEmpty()) {
            dismiss.onFirst().performClick()
            compose.waitForIdle()
        }
        compose.runOnIdle {
            val view = compose.activity.window.decorView
            val bitmap = android.graphics.Bitmap.createBitmap(
                view.width.coerceAtLeast(1),
                view.height.coerceAtLeast(1),
                android.graphics.Bitmap.Config.ARGB_8888
            )
            view.draw(android.graphics.Canvas(bitmap))
            val file = File("build/reports/screenshots/$name.png")
            file.parentFile!!.mkdirs()
            file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    private fun seedHeritageState() {
        vm.run {
            app.database.dao().reward(
                Reward(
                    id = "heritage-seed-bulk",
                    date = today(),
                    label = "Legado das Runas",
                    xp = 15000L,
                    coins = 3600L,
                    attribute = 2,
                    points = 100
                )
            )
            app.repository.refresh()
            app.repository.saveNotebook(KnowledgeNotebook(id = "nb-heritage", name = "Crônica de Mímir"))
            app.database.dao().unlock(Unlocked(id = "forest", date = today()))
            app.database.dao().unlock(Unlocked(id = "library_portico", date = today()))
            app.database.dao().unlock(Unlocked(id = "mountains_trail", date = today()))
            app.database.dao().unlock(Unlocked(id = "mimir_first_rune", date = today()))
            val path = PersonalPath(id = "path-heritage", name = "Caminho Boreal", description = "Constância")
            app.repository.savePath(path)
            app.repository.saveTask(PathTask(id = "pt-h1", pathId = path.id, title = "Rito", completed = true, completedDate = today()))
            app.repository.savePath(path.copy(state = 3, completedDate = today()))
            app.repository.refresh()

            app.repository.restore(1)
            app.repository.restore(1)
            app.repository.selectSpecialization("mestre_da_forja")

            // Craft items across collections (complete Sabedoria de Mímir + partial Juramento de Ferro & Vigília Boreal)
            app.repository.craftForgeItem("mimir-runic-crown")
            app.repository.craftForgeItem("scribe-robes")
            app.repository.craftForgeItem("runic-staff")
            app.repository.craftForgeItem("rune-mantle")
            app.repository.craftForgeItem("oath-helm")
            app.repository.craftForgeItem("vigil-axe")
            app.repository.craftForgeItem("constellation-brooch")
            app.repository.craftForgeItem("nine-realms-frame")

            app.repository.saveHeraldry(
                HeraldryProfile(
                    symbolId = "yggdrasil",
                    shapeId = "oval_shield",
                    metalId = "gold",
                    secondaryColorId = "fjord_blue",
                    inscriptionRunes = "Ansuz,Raido,Othala",
                    ornamentId = "bronze_borre",
                    bannerId = "ceremonial",
                    showOnPortrait = true
                )
            )
            app.repository.applyStudioSelection(
                mapOf(
                    "head" to "oath-helm",
                    "outfit" to "scribe-robes",
                    "accessory" to "constellation-brooch",
                    "weapon" to "vigil-axe",
                    "frame" to "nine-realms-frame"
                )
            )
        }
        wait {
            val st = app.repository.state.value!!
            st.heraldry?.symbolId == "yggdrasil" &&
                st.assets.any { it.id == "collection:mimir_wisdom" } &&
                st.assets.any { it.id == "vigil-axe" && it.equipped }
        }
    }

    @Test
    fun captureHeraldryAndCodexScreenshots() {
        seedHeritageState()
        val st = app.repository.state.value!!

        // 01: Editor de Heráldica Rúnica
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        HeraldryEditorScreen(st, vm, onOpenCodex = {}, onBack = {})
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithText("Heráldica Rúnica de Aetheris").assertExists()
        shot("01-editor-brasao")

        // 02: Perfil do Herói com Brasão e atalhos
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        CharacterScreen(st, vm, onSettings = {}, onJournal = {})
                    }
                }
            }
        }
        compose.waitForIdle()
        shot("02-brasao-heroi")

        // 03: Retrato Ampliado com Estandarte Pessoal e camadas
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        PortraitScreen(st, onOpenHeraldry = {})
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithText("BRASÃO E ESTANDARTE PESSOAL").performScrollTo()
        compose.waitForIdle()
        shot("03-estandarte-pessoal")

        // 04: Codex Rúnico (24 runas históricas do Elder Futhark)
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        RunicCodexScreen(onOpenHeraldry = {}, onBack = {})
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithText("Codex Rúnico · Elder Futhark").assertExists()
        shot("04-codex-24-runas")

        // 05: Codex Rúnico — detalhe histórico + separação ficcional de Ansuz
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        RunicCodexScreen(onOpenHeraldry = {}, onBack = {}, initialRuneName = "Ansuz")
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithText("Associação narrativa em Aetheris (ficção)").performScrollTo()
        compose.waitForIdle()
        shot("05-codex-runa-detalhe")
    }

    @Test
    fun captureForgeUpgradesAndCollectionsScreenshots() {
        seedHeritageState()
        val stStage1 = app.repository.state.value!!

        // 06: Forja de Darian com 12 receitas
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        ForgeScreen(stStage1, vm)
                    }
                }
            }
        }
        compose.waitForIdle()
        shot("06-forja-12-receitas")

        // 07: Aprimoramento estágio 1 — Forjado
        compose.onNodeWithText("Aprimorar").performClick()
        compose.waitForIdle()
        shot("07-aprimoramento-forjado")

        // 08: Aprimoramento estágio 2 — Gravado
        vm.run {
            app.repository.upgradeForgeItem("oath-helm", FORGE_STAGE_ENGRAVED)
            app.repository.upgradeForgeItem("scribe-robes", FORGE_STAGE_ENGRAVED)
            app.repository.upgradeForgeItem("vigil-axe", FORGE_STAGE_ENGRAVED)
        }
        wait { app.repository.state.value!!.assets.first { it.id == "vigil-axe" }.stage == FORGE_STAGE_ENGRAVED }
        val stStage2 = app.repository.state.value!!
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        ForgeScreen(stStage2, vm)
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithText("Aprimorar").performClick()
        compose.waitForIdle()
        shot("08-aprimoramento-gravado")

        // 09: Aprimoramento estágio 3 — Consagrado
        vm.run {
            app.repository.upgradeForgeItem("oath-helm", FORGE_STAGE_CONSECRATED)
            app.repository.upgradeForgeItem("scribe-robes", FORGE_STAGE_CONSECRATED)
            app.repository.upgradeForgeItem("nine-realms-frame", FORGE_STAGE_ENGRAVED)
            app.repository.upgradeForgeItem("nine-realms-frame", FORGE_STAGE_CONSECRATED)
        }
        wait {
            app.repository.state.value!!.assets.first { it.id == "oath-helm" }.stage == FORGE_STAGE_CONSECRATED &&
                app.repository.state.value!!.assets.first { it.id == "nine-realms-frame" }.stage == FORGE_STAGE_CONSECRATED
        }
        val stStage3 = app.repository.state.value!!
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        ForgeScreen(stStage3, vm)
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithText("Aprimorar").performClick()
        compose.waitForIdle()
        shot("09-aprimoramento-consagrado")

        // 10: Coleções Lendárias (visão geral)
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        Page {
                            Title("Coleções Lendárias", "Três linhagens temáticas da Forja de Darian.")
                            LegendaryCollectionsSection(stStage3, vm)
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
        shot("10-colecoes-lendarias")

        // 11: Coleção Lendária concluída (Sabedoria de Mímir — 4/4, Título Herdeiro de Mímir)
        compose.onNodeWithTag("collection_card_mimir_wisdom").performScrollTo()
        compose.waitForIdle()
        shot("11-colecao-concluida")
    }

    @Test
    fun captureArsenalLivingPortrait11LightThemeNarrowAndEtapa0Flows() {
        seedHeritageState()
        vm.run {
            app.repository.upgradeForgeItem("scribe-robes", FORGE_STAGE_ENGRAVED)
            app.repository.upgradeForgeItem("scribe-robes", FORGE_STAGE_CONSECRATED)
            app.repository.upgradeForgeItem("nine-realms-frame", FORGE_STAGE_ENGRAVED)
            app.repository.upgradeForgeItem("nine-realms-frame", FORGE_STAGE_CONSECRATED)
            app.repository.upgradeForgeItem("vigil-axe", FORGE_STAGE_ENGRAVED)
        }
        wait { app.repository.state.value!!.assets.first { it.id == "nine-realms-frame" }.stage == FORGE_STAGE_CONSECRATED }
        val st = app.repository.state.value!!

        // 12: Arsenal 2.1 com busca e filtros avançados expandidos
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            Equipment(st, vm, onOpenForge = {}, onOpenHeraldry = {})
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithTag("arsenal_filters_toggle").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithText("ESTÁGIO DA FORJA").performScrollTo()
        compose.waitForIdle()
        shot("12-arsenal-filtros")

        // 13: Retrato Vivo 1.1 — acabamentos Forjado / Gravado / Consagrado + novos itens + brasão
        val stage1State = st.copy(
            assets = st.assets.map { if (it.id in forgeItemIds) it.copy(stage = FORGE_STAGE_FORGED) else it }
        )
        val stage2State = st.copy(
            assets = st.assets.map { if (it.id in forgeItemIds) it.copy(stage = FORGE_STAGE_ENGRAVED) else it }
        )
        val stage3State = st.copy(
            assets = st.assets.map { if (it.id in forgeItemIds) it.copy(stage = FORGE_STAGE_CONSECRATED) else it }
        )
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Title("Retrato Vivo 1.1 · Acabamentos", "Forjado → Gravado → Consagrado + Brasão Rúnico.")
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                listOf(
                                    "Forjado (1)" to stage1State,
                                    "Gravado (2)" to stage2State,
                                    "Consagrado (3)" to stage3State
                                ).forEach { (lbl, stateVariant) ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        LivingHeroPortrait(stateVariant.player!!, stateVariant, size = 112.dp)
                                        Text(lbl, color = Gold, fontSize = 11.sp)
                                    }
                                }
                            }
                            LivingHeroPortrait(stage3State.player!!, stage3State, size = 216.dp)
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
        shot("13-retrato-vivo-11-acabamentos")

        // 14: Tema Claro 1.7.3.1 (Editor de Heráldica Rúnica em contraste claro)
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "light", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        HeraldryEditorScreen(st, vm, onOpenCodex = {}, onBack = {})
                    }
                }
            }
        }
        compose.waitForIdle()
        shot("14-tema-claro-1731")

        // 15: Tela estreita 320dp + fonte 130% em Arsenal 2.1
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 1.3f)) {
                    AscendTheme(mode = "dark", reducedMotion = true) {
                        Surface(color = Night, contentColor = Ink) {
                            Box(Modifier.width(320.dp).fillMaxHeight()) {
                                Column(
                                    Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(10.dp)
                                ) {
                                    Equipment(st, vm, onOpenForge = {}, onOpenHeraldry = {})
                                }
                            }
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
        shot("15-estreito-320dp-fonte-130-1731")

        // Etapa 0 — 6.3 UI check: partial loadout opens explicit confirmation dialog before applying
        vm.run {
            app.database.dao().loadout(
                EquipmentLoadout(
                    id = "loadout:3",
                    name = "Expedição Parcial",
                    headId = "oath-helm",
                    outfitId = "north-mail" // NOT crafted in seedHeritageState
                )
            )
            app.repository.refresh()
        }
        wait { app.repository.state.value!!.loadouts.any { it.id == "loadout:3" && it.outfitId == "north-mail" } }
        val stPartial = app.repository.state.value!!
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                            LoadoutDeckSection(stPartial, vm, onRename = {})
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithText("Expedição Parcial").performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("apply_loadout_btn").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Aplicar configuração parcial?").assertExists()

        // Etapa 0 — 6.4 UI check: Studio discard confirmation & staying open on save error
        var studioDismissed = false
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        HeroCustomizationStudio(
                            s = st,
                            previousAppearance = null,
                            onDismiss = { studioDismissed = true },
                            onSave = { /* simulate error: do not dismiss */ },
                            inline = false,
                            saveErrorMessage = "Falha simulada de persistência"
                        )
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithText("Não foi possível salvar: Falha simulada de persistência").assertExists()
        compose.onNodeWithText("Nenhum (Cabeça)").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Descartar alterações no Estúdio?").assertExists()
        assertFalse(studioDismissed)
    }
}
