package br.com.ascend.aetheris

import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = AscendApp::class, qualifiers = "w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LivingHeroAndForgeUiTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()
    private val app get() = RuntimeEnvironment.getApplication() as AscendApp
    private val vm get() = ViewModelProvider(compose.activity)[GameViewModel::class.java]

    @Before
    fun setup() {
        runBlocking { app.preferences.edit { it.clear() } }
        wait { app.repository.state.value != null }
        vm.run {
            app.repository.create(Player(name = "Sigrid", heroClass = 4, face = 2), emptyList())
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

    private fun seedForgedHero(specId: String = "senhor_da_guerra") {
        vm.run {
            var guard = 0
            while ((app.repository.state.value!!.level < 10 || app.repository.state.value!!.coins < 950L) && guard < 50) {
                app.database.dao().reward(
                    Reward(
                        id = "ui-seed-$guard",
                        date = today(),
                        label = "Treino de herói",
                        xp = if (app.repository.state.value!!.level < 10) 240 else 0,
                        coins = if (app.repository.state.value!!.coins < 950L) 100 else 0,
                        attribute = 2,
                        points = 10
                    )
                )
                app.repository.refresh()
                guard++
            }
            app.repository.habit(Habit(id = "h_forja", name = "Vigília matinal", type = 0, target = 1, unit = "vez", xp = 30, coins = 8))
            app.repository.habit(Habit(id = "h_estudo", name = "Leitura das Runas", type = 2, target = 25, unit = "min", xp = 40, coins = 10))
            app.repository.saveNotebook(KnowledgeNotebook(id = "nb_ui", name = "Tomo do Norte"))
            app.database.dao().unlock(Unlocked(id = "forest", date = today()))
            app.database.dao().unlock(Unlocked(id = "library_portico", date = today()))
            app.database.dao().unlock(Unlocked(id = "mountains_trail", date = today()))
            app.repository.refresh()

            // Restore Forge Stage 1 -> Stage 2
            app.repository.restore(1)
            app.repository.restore(1)

            // Craft Forge items & buy a few shop items
            app.repository.craftForgeItem("oath-helm")
            app.repository.craftForgeItem("north-mail")
            app.repository.craftForgeItem("scribe-robes")
            app.repository.craftForgeItem("yggdrasil-brooch")
            app.repository.craftForgeItem("mimir-runic-crown")
            app.repository.craftForgeItem("nine-realms-frame")
            app.repository.buy(shopItems.first { it.id == "silver-blade" })
            app.repository.buy(shopItems.first { it.id == "corvus" })

            app.repository.selectSpecialization(specId)
            app.repository.applyStudioSelection(
                mapOf(
                    "head" to "oath-helm",
                    "outfit" to "north-mail",
                    "accessory" to "yggdrasil-brooch",
                    "weapon" to "silver-blade",
                    "companion" to "corvus",
                    "frame" to "nine-realms-frame"
                )
            )
            app.repository.saveCurrentAsLoadout("loadout:1", "Jornada")
            app.repository.saveLoadout(
                EquipmentLoadout(
                    id = "loadout:2",
                    name = "Conselho",
                    headId = "mimir-runic-crown",
                    outfitId = "scribe-robes",
                    frameId = "nine-realms-frame"
                )
            )
        }
        wait {
            val st = app.repository.state.value!!
            st.assets.any { it.id == "nine-realms-frame" && it.equipped } &&
                st.player?.specialization == specId &&
                st.loadouts.size >= 2
        }
    }

    @Test
    fun captureLivingPortraitBaseEquippedAndZoom() {
        // 01: Base Living Hero across all 3 skin tones (100% preserved original portraits)
        val baseState = app.repository.state.value!!
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Title("Retrato Vivo · Base", "Os três tons originais preservados integralmente.")
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                listOf(0 to "Claro", 2 to "Médio", 1 to "Escuro").forEach { (face, lbl) ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        LivingHeroPortrait(
                                            p = baseState.player!!.copy(face = face),
                                            s = baseState,
                                            size = 108.dp
                                        )
                                        Text(lbl, color = Muted, fontSize = 12.sp)
                                    }
                                }
                            }
                            LivingHeroPortrait(p = baseState.player!!, s = baseState, size = 196.dp)
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
        shot("01-retrato-base")

        // 02: Equipped Forge gear across all 3 skin tones (verifying shoulder/brow alignment & visible faces)
        seedForgedHero("mestre_da_forja")
        val equippedState = app.repository.state.value!!
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
                            Title("Retrato Vivo · Forja", "Armaduras, elmos abertos, fíbulas e molduras calibrados.")
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                listOf(
                                    Triple(0, "Elmo + Cota", mapOf("head" to "oath-helm", "outfit" to "north-mail", "accessory" to "yggdrasil-brooch", "frame" to "nine-realms-frame")),
                                    Triple(2, "Coroa + Escriba", mapOf("head" to "mimir-runic-crown", "outfit" to "scribe-robes", "frame" to "rune-frame", "background" to "scholar-bg")),
                                    Triple(1, "Elmo + Vigília", mapOf("head" to "oath-helm", "outfit" to "vigil-armor", "accessory" to "mist-clasp", "frame" to "nine-realms-frame"))
                                ).forEach { (face, lbl, overrides) ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        LivingHeroPortrait(
                                            p = equippedState.player!!.copy(face = face),
                                            s = equippedState,
                                            size = 110.dp,
                                            previewOverrides = overrides
                                        )
                                        Text(lbl, color = Gold, fontSize = 11.sp)
                                    }
                                }
                            }
                            LivingHeroPortrait(p = equippedState.player!!, s = equippedState, size = 210.dp)
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
        shot("02-retrato-equipado-forja")

        // 03: Real PortraitScreen zoom with specialization crest & layer list
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        PortraitScreen(equippedState)
                    }
                }
            }
        }
        compose.waitForIdle()
        shot("03-retrato-especializacao-zoom")
    }

    @Test
    fun captureJourneyHeroArsenalStudioAndLoadouts() {
        seedForgedHero("tecelao_de_runas")
        val st = app.repository.state.value!!

        // 04: Jornada with Living Hero CompactHero
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        Journey(
                            s = st,
                            onProgress = { _, _ -> },
                            onEdit = {},
                            onFocus = {},
                            onAdd = {},
                            onJournal = {},
                            vm = vm,
                            relaxed = false
                        )
                    }
                }
            }
        }
        compose.waitForIdle()
        shot("04-jornada-retrato-vivo")

        // 05: Herói profile with Living Hero HeroStage + specialization seal + Forge shortcut
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
        shot("05-heroi-perfil-retrato-vivo")

        // 06: Arsenal dos Nove Reinos
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
                            Equipment(st, vm, onOpenForge = {})
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
        shot("06-arsenal-nove-reinos")

        // 07: Estúdio de Personalização
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        HeroCustomizationStudio(
                            s = st,
                            previousAppearance = null,
                            onDismiss = {},
                            onSave = {},
                            inline = true
                        )
                    }
                }
            }
        }
        compose.waitForIdle()
        shot("07-estudio-personalizacao")

        // 08: Estúdio previewing a LOCKED item (vigil-armor is not crafted in seedForgedHero)
        compose.onNodeWithText("Traje").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Armadura da Vigília").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Bloqueado").assertExists()
        compose.onNodeWithText("PRÉVIA COM ITENS BLOQUEADOS").performScrollTo()
        compose.waitForIdle()
        shot("08-estudio-preview-bloqueado")

        // 09: 3 Conjuntos de Equipamento deck
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Title("Conjuntos do Herói", "Três aparências completas prontas para alternar.")
                            LoadoutDeckSection(st, vm, onRename = {})
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
        shot("09-conjuntos-equipamento")
    }

    @Test
    fun captureVillageForgeMasterLightThemeAndNarrow320() {
        seedForgedHero("tecelao_de_runas")
        val st = app.repository.state.value!!

        // 10: Vila do Despertar showing A Forja (building:1) with Abrir A Forja de Darian
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        WorldScreen(st, vm)
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithText("Vila").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Abrir A Forja de Darian").performScrollTo()
        compose.waitForIdle()
        shot("10-vila-forja-darian")

        // 11: A Forja de Darian (Oficina)
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        ForgeScreen(st, vm)
                    }
                }
            }
        }
        compose.waitForIdle()
        shot("11-forja-darian-oficina")

        // 12: A Forja de Darian com especialização Mestre da Forja
        vm.run { app.repository.selectSpecialization("mestre_da_forja") }
        wait { app.repository.state.value!!.player?.specialization == "mestre_da_forja" }
        val masterState = app.repository.state.value!!
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        ForgeScreen(masterState, vm)
                    }
                }
            }
        }
        compose.waitForIdle()
        shot("12-forja-darian-mestre")

        // 13: Tema Claro no Arsenal dos Nove Reinos
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                AscendTheme(mode = "light", reducedMotion = true) {
                    Surface(color = Night, contentColor = Ink) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            Equipment(masterState, vm, onOpenForge = {})
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
        shot("13-tema-claro-arsenal")

        // 14: Tela estreita 320dp + fonte 130% na Forja de Darian
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                val baseDensity = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(baseDensity.density, fontScale = 1.3f)) {
                    AscendTheme(mode = "dark", reducedMotion = true) {
                        Surface(color = Night, contentColor = Ink) {
                            Box(Modifier.width(320.dp).fillMaxHeight()) {
                                ForgeScreen(masterState, vm)
                            }
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
        shot("14-estreito-320dp-fonte-130")
    }
}
