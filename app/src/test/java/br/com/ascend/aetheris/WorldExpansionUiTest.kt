package br.com.ascend.aetheris

import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
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
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = AscendApp::class, qualifiers = "w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WorldExpansionUiTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()
    private val app get() = RuntimeEnvironment.getApplication() as AscendApp
    private val vm get() = ViewModelProvider(compose.activity)[GameViewModel::class.java]

    @Before
    fun setup() {
        runBlocking { app.preferences.edit { it.clear() } }
        wait { app.repository.state.value != null }
        vm.run {
            app.repository.create(Player(name = "Sigrid", heroClass = 1), emptyList())
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

    private fun settle() {
        repeat(6) {
            compose.waitForIdle()
            val reward = compose.onAllNodesWithText("Continuar a jornada")
            if (reward.fetchSemanticsNodes().isEmpty()) return
            reward.onFirst().performClick()
        }
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
            val bitmap = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(bitmap))
            val file = File("build/reports/screenshots/$name.png")
            file.parentFile!!.mkdirs()
            file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    private fun seedRichHero(targetLevel: Int = 12) {
        vm.run {
            var idx = 0
            while (app.repository.state.value!!.level < targetLevel) {
                val h = Habit(id = "seed_lvl_${idx++}", name = "Marco $idx", xp = 450, created = LocalDate.now().minusDays(20).toString())
                app.repository.habit(h)
                app.repository.progress(h, today(), 1)
            }
            app.repository.selectSpecialization("erudito")
            app.repository.habit(Habit(id = "h_read", name = "Leitura profunda", type = 2, target = 25, unit = "min", xp = 35, coins = 8))
            app.repository.habit(Habit(id = "h_walk", name = "Caminhada ao vento", type = 0, target = 1, unit = "vez", xp = 25, coins = 6))
            val path = PersonalPath(id = "p_norte", name = "Domínio das Runas")
            app.repository.savePath(path)
            app.repository.saveTask(PathTask(id = "pt1", pathId = path.id, title = "Estudar Elder Futhark", scheduledDate = today()))
            app.repository.saveEvent(AgendaEvent(id = "ev1", title = "Vigília de Estudo", date = today(), startTime = "19:00"))
            val nb = KnowledgeNotebook(id = "nb_mimir", name = "Caderno do Norte", description = "Anotações e revisões")
            app.repository.saveNotebook(nb)
            app.repository.savePage(KnowledgePage(id = "pg1", notebookId = nb.id, title = "Runas Históricas vs Selos", content = "Distinção entre Futhark antigo e selos fictícios de Aetheris."))
            app.repository.saveFlashcard(Flashcard(id = "fc_m1", notebookId = nb.id, question = "Qual a diferença entre Biblioteca de Mímir e Biblioteca das Eras?", answer = "Mímir é o caderno funcional; Eras é a região narrativa do Atlas.", nextReviewDate = today()))
        }
        wait {
            val s = app.repository.state.value!!
            s.level >= targetLevel &&
                activeSpecialization(s)?.id == "erudito" &&
                s.notebooks.isNotEmpty() &&
                s.flashcards.isNotEmpty()
        }
        settle()
    }

    @Test
    fun componentBoardAndDesignSystem30() {
        compose.runOnIdle {
            compose.activity.setContent {
                AscendTheme(mode = "dark", reducedMotion = true, minimal = false) {
                    Surface(Modifier.fillMaxSize(), color = Night, contentColor = Ink) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .background(Night)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Title("Design System 3.0", "Prancha de Componentes · Renascimento Rúnico")
                            Eyebrow("6 Famílias de Botões Viking", Gold)
                            SagaButton("Ferro Rúnico", {}, Modifier.fillMaxWidth(), style = VikingButtonStyle.FerroRunico)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SagaButton("Bronze Forjado", {}, Modifier.weight(1f), style = VikingButtonStyle.BronzeForjado)
                                SagaButton("Cerimonial", {}, Modifier.weight(1f), style = VikingButtonStyle.CerimonialViking)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SagaButton("Confirmação", {}, Modifier.weight(1f), style = VikingButtonStyle.Confirmacao)
                                SagaButton("Destrutivo", {}, Modifier.weight(1f), style = VikingButtonStyle.Destrutivo)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SagaButton("Compacto", {}, Modifier.weight(1f), style = VikingButtonStyle.CompactoRunico)
                                SagaButton("Desabilitado", {}, Modifier.weight(1f), enabled = false, style = VikingButtonStyle.FerroRunico)
                            }
                            VikingKnotDivider(variant = 0, height = 14.dp)
                            Eyebrow("Runas Históricas (Elder Futhark vetorial)", Blue)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                HistoricalFutharkRune.entries.take(8).forEach { r ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        FutharkRuneGlyph(r, Gold, size = 20.dp)
                                        Text(r.name.take(4), color = Muted, fontSize = 9.sp)
                                    }
                                }
                            }
                            Eyebrow("Selos Fictícios de Aetheris & Ícones Funcionais", Gold)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                AetherisSeal(AetherisSealKind.OpenBook, size = 28.dp)
                                AetherisSeal(AetherisSealKind.MountainPeaks, size = 28.dp)
                                AetherisSeal(AetherisSealKind.CompassStar, size = 28.dp)
                                VikingFunctionalIcon(VikingIcon.Longship, Gold, size = 24.dp)
                                VikingFunctionalIcon(VikingIcon.Codex, Gold, size = 24.dp)
                                VikingFunctionalIcon(VikingIcon.LanternHourglass, Gold, size = 24.dp)
                                VikingCheckSeal(size = 26.dp)
                            }
                            Eyebrow("Controles Forjados & Relíquias Regionais", Green)
                            Meter(0.68f, Gold, slim = false)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                RelicArt("accessory", Gold, itemId = "eternal-memory-medallion")
                                RelicArt("accessory", Gold, itemId = "firmament-compass")
                                RelicArt("accessory", Violet, itemId = "scribes-ring")
                                RelicArt("accessory", Violet, itemId = "boreal-vigil-brooch")
                            }
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithText("Design System 3.0").assertIsDisplayed()
        compose.onNodeWithText("Ferro Rúnico").assertIsDisplayed()
        compose.onNodeWithText("Cerimonial").assertIsDisplayed()
        shot("component-board-172")
    }

    @Test
    fun atlas20ZoomAccessibleListAndNoInteractiveRegions() {
        seedRichHero(targetLevel = 12)
        compose.onNodeWithTag("nav:world").performClick()
        compose.waitForIdle()

        // Verify zoom controls work and stay bounded
        compose.onNodeWithText("Zoom 100%").assertIsDisplayed()
        compose.onNodeWithContentDescription("Ampliar mapa").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Zoom 135%").assertIsDisplayed()
        compose.onNodeWithContentDescription("Centralizar mapa").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Zoom 100%").assertIsDisplayed()

        // Capture Atlas 2.0 overview
        shot("atlas-2-0-172")

        // Verify accessible region list and explicit non-interactive state for Santuário do Equilíbrio (lvl 12)
        compose.onNodeWithContentDescription("Santuário do Equilíbrio, revelada").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onAllNodesWithText("Sem conteúdo interativo ainda", substring = true).onFirst().assertExists()
        compose.onNodeWithTag("atlas_accessible_list").performScrollTo().assertExists()
    }

    @Test
    fun bibliotecaDasErasVisualStates() {
        seedRichHero(targetLevel = 10)
        compose.onNodeWithTag("nav:world").performClick()
        compose.onNodeWithContentDescription("Biblioteca das Eras, revelada").performScrollTo().performClick()
        compose.onNodeWithText("Explorar região").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Biblioteca das Eras").assertIsDisplayed()
        compose.onNodeWithTag("exploration_list").performScrollToNode(hasText("Eydis"))
        compose.onNodeWithText("Eydis").assertIsDisplayed()
        compose.onNodeWithTag("exploration_list").performScrollToIndex(0)
        shot("biblioteca-eras-abandonada-172")

        var libErr: Throwable? = null
        vm.run {
            try {
                var counter = 0
                for (e in libraryExpeditions) {
                    val needed = e.phases.sumOf { it.target }
                    val hs = (1..needed).map { Habit(id = "ui_lib_${counter++}", name = "Estudo $counter", xp = 0).also { app.repository.habit(it) } }
                    app.repository.startExpedition(e.id, hs.map { it.id }, EXPEDITION_METRIC_HABITS)
                    var u = 0
                    for (p in e.phases) {
                        repeat(p.target) { app.repository.progress(hs[u++], today(), 1) }
                        app.repository.advanceExpedition(e.id)
                    }
                }
            } catch (t: Throwable) {
                libErr = t
            }
        }
        compose.waitUntil(60000) {
            Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
            libErr != null || libraryProgress(app.repository.state.value!!) == 100
        }
        assertNull("Library expedition setup failed: $libErr / ${vm.error.value}", libErr)
        assertEquals(100, libraryProgress(app.repository.state.value!!))
        settle()
        compose.onNodeWithTag("exploration_list").performScrollToIndex(0)
        shot("biblioteca-eras-restaurada-172")
    }

    @Test
    fun montanhasDaDisciplinaAndRelicsVisualStates() {
        seedRichHero(targetLevel = 10)
        compose.onNodeWithTag("nav:world").performClick()
        compose.onNodeWithContentDescription("Montanhas da Disciplina, revelada").performScrollTo().performClick()
        compose.onNodeWithText("Explorar região").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("exploration_list").performScrollToNode(hasText("Haldor"))
        compose.onNodeWithText("Haldor").assertIsDisplayed()
        compose.onNodeWithTag("exploration_list").performScrollToIndex(0)
        shot("montanhas-abandonada-172")

        var mntErr: Throwable? = null
        vm.run {
            try {
                var counter = 0
                for (e in mountainsExpeditions) {
                    val needed = e.phases.sumOf { it.target }
                    val hs = (1..needed).map { Habit(id = "ui_mnt_${counter++}", name = "Trilha $counter", xp = 0).also { app.repository.habit(it) } }
                    app.repository.startExpedition(e.id, hs.map { it.id }, EXPEDITION_METRIC_HABITS)
                    var u = 0
                    for (p in e.phases) {
                        repeat(p.target) { app.repository.progress(hs[u++], today(), 1) }
                        app.repository.advanceExpedition(e.id)
                    }
                }
            } catch (t: Throwable) {
                mntErr = t
            }
        }
        compose.waitUntil(60000) {
            Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
            mntErr != null || mountainsProgress(app.repository.state.value!!) == 100
        }
        assertNull("Mountains expedition setup failed: $mntErr / ${vm.error.value}", mntErr)
        assertEquals(100, mountainsProgress(app.repository.state.value!!))
        settle()
        compose.onNodeWithTag("exploration_list").performScrollToIndex(0)
        shot("montanhas-restaurada-172")

        vm.run {
            val compass = app.repository.state.value!!.assets.first { it.id == "firmament-compass" }
            app.repository.equip(compass)
        }
        wait { app.repository.state.value!!.assets.any { it.id == "firmament-compass" && it.equipped } }
        compose.onNodeWithTag("nav:character").performClick()
        compose.waitForIdle()
        shot("heroi-172")
        compose.onNodeWithText("Inventário").performClick()
        compose.waitForIdle()
        shot("reliquias-172")
    }

    @Test
    fun coreScreensTour172() {
        seedRichHero(targetLevel = 8)
        // Jornada
        compose.onNodeWithTag("nav:journey").performClick()
        compose.waitForIdle()
        shot("jornada-172")

        // Foco (start active focus session so compact FocusCard renders cleanly at top of Jornada)
        vm.run {
            val timed = app.repository.state.value!!.habits.first { it.id == "h_read" }
            app.repository.startFocus(timed, 25)
        }
        wait { app.repository.state.value!!.focus != null }
        compose.waitForIdle()
        shot("foco-172")
        vm.run { app.repository.endFocus(false) }
        wait { app.repository.state.value!!.focus == null }

        // Agenda (via Jornada -> Planejador)
        compose.onNodeWithText("Planejador").performScrollTo().performClick()
        compose.waitForIdle()
        shot("agenda-172")

        // Biblioteca de Mímir (via Mundo -> Crônicas -> Abrir Biblioteca de Mímir)
        compose.onNodeWithTag("nav:world").performClick()
        compose.onNodeWithText("Crônicas").performClick()
        compose.onNodeWithText("Abrir Biblioteca de Mímir").performScrollTo().performClick()
        compose.waitForIdle()
        shot("mimir-172")
    }

    @Test
    @Config(qualifiers = "w320dp-h740dp-mdpi")
    fun lightThemeAndNarrow320Font130() {
        seedRichHero(targetLevel = 8)
        vm.theme("light")
        wait { vm.appearance.value == "light" }
        compose.onNodeWithTag("nav:world").performClick()
        compose.waitForIdle()
        shot("tema-claro-172")

        // 130% font scale on 320dp narrow screen
        compose.runOnIdle {
            compose.activity.setContent {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, 1.3f)) {
                    Ascend()
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithTag("nav:world").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Atlas de Aetheris").assertExists()
        shot("narrow-320-130-172")
    }
}
