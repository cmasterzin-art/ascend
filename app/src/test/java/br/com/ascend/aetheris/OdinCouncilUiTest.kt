package br.com.ascend.aetheris

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
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

/**
 * ASCEND 1.7.5 — A Mesa de Odin
 * Suíte UI Robolectric + Compose capturando TODAS as 17 telas reais exigidas pela Seção 35.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = AscendApp::class, qualifiers = "w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OdinCouncilUiTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()
    private val app get() = RuntimeEnvironment.getApplication() as AscendApp
    private val vm get() = ViewModelProvider(compose.activity)[GameViewModel::class.java]

    @Before
    fun setup() {
        runBlocking { app.preferences.edit { it.clear() } }
        wait { app.repository.state.value != null }
        vm.run {
            app.repository.create(Player(name = "Sigrid", heroClass = 1, face = 0), emptyList())
            vm.setting("reducedMotion", true)
        }
        wait { app.repository.state.value!!.player != null && vm.reducedMotion.value }
    }

    @After
    fun close() {
        AmbientAudioEngine.stop()
        compose.activityRule.scenario.close()
        app.database.close()
    }

    private fun wait(test: () -> Boolean) {
        compose.waitUntil(15000) {
            Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
            test()
        }
        compose.waitForIdle()
    }

    private fun shot(name: String) {
        compose.waitForIdle()
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
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

    private fun seedCouncilState(): PersonalPath {
        val d0 = today()
        val yesterday = LocalDate.parse(d0).minusDays(1).toString()
        val d3 = LocalDate.parse(d0).plusDays(3).toString()
        val d7 = LocalDate.parse(d0).plusDays(7).toString()
        val d14 = LocalDate.parse(d0).plusDays(14).toString()

        vm.run {
            app.repository.applyDefaultAvailability()
            app.repository.seedDefaultLifeAreas()
            app.repository.setOdinStrategy("strategist")

            val habit1 = Habit(id = "h-est", name = "Estudo dirigido", type = 2, target = 45, unit = "min", attribute = 0, areaId = "area-estudos")
            val habit2 = Habit(id = "h-vig", name = "Caminhada matinal", type = 0, target = 1, unit = "vez", attribute = 1, areaId = "area-saude")
            app.repository.habit(habit1)
            app.repository.habit(habit2)

            val path = PersonalPath(
                id = "path-odin-175",
                name = "Arquitetura de Aetheris",
                description = "Consolidação técnica e documentação da 1.7.5",
                category = "Tecnologia",
                priority = 3,
                state = 1,
                attribute = 0,
                startDate = d0,
                deadline = d14,
                areaId = "area-carreira"
            )
            app.repository.savePath(path)
            app.repository.saveStage(PathStage(id = "st-1", pathId = path.id, name = "Motor Estratégico", position = 0))
            app.repository.saveStage(PathStage(id = "st-2", pathId = path.id, name = "Interface Nórdica", position = 1))

            app.repository.saveTask(PathTask(id = "t-1", pathId = path.id, stageId = "st-1", title = "Modelar intervalos livres", estimateMinutes = 60, priority = 3, deadline = d3, scheduledDate = d0, workflowStatus = 1, important = true, urgent = true, areaId = "area-carreira"))
            app.repository.saveTask(PathTask(id = "t-2", pathId = path.id, stageId = "st-1", title = "Implementar caminho crítico", estimateMinutes = 90, priority = 3, deadline = d7, important = true, urgent = false, areaId = "area-carreira"))
            app.repository.saveTask(PathTask(id = "t-3", pathId = path.id, stageId = "st-2", title = "Refinar Gantt e Kanban", estimateMinutes = 75, priority = 2, deadline = d14, important = true, urgent = false, areaId = "area-criatividade"))
            app.repository.saveTask(PathTask(id = "t-4", pathId = path.id, stageId = "st-2", title = "Revisar notas atrasadas", estimateMinutes = 40, priority = 2, scheduledDate = yesterday, deadline = yesterday, important = false, urgent = true, areaId = "area-estudos"))
            app.repository.saveTask(PathTask(id = "t-5", pathId = path.id, stageId = "st-1", title = "Levantamento inicial", estimateMinutes = 30, priority = 1, completed = true, completedDate = d0, areaId = "area-carreira"))

            app.repository.addTaskDependency("t-2", "t-1")
            app.repository.addTaskDependency("t-3", "t-2")
            app.repository.savePathMilestone(PathMilestone(id = "ms-1", pathId = path.id, title = "Núcleo determinístico pronto", targetDate = d3))
            app.repository.savePathMilestone(PathMilestone(id = "ms-2", pathId = path.id, title = "Entrega 1.7.5 validada", targetDate = d14))

            app.repository.saveEvent(AgendaEvent(id = "ev-1", title = "Conselho de arquitetura", date = d0, startTime = "10:30", endTime = "11:30", kind = "Reunião", areaId = "area-carreira"))
            val blk1 = TimeBlock(id = "blk-1", title = "Foco · Motor Estratégico", date = d0, startTime = "09:00", endTime = "10:00", category = "Projeto", pathId = path.id, taskId = "t-1", pinned = true, sourcePlanId = "plan-seed", areaId = "area-carreira")
            val blk2 = TimeBlock(id = "blk-2", title = "Revisão diária Mímir", date = d0, startTime = "14:00", endTime = "14:45", category = "Estudo", areaId = "area-estudos")
            val blkMiss = TimeBlock(id = "blk-miss", title = "Revisar notas atrasadas", date = yesterday, startTime = "15:00", endTime = "15:40", category = "Estudo", pathId = path.id, taskId = "t-4", areaId = "area-estudos")
            app.repository.saveTimeBlock(blk1)
            app.repository.saveTimeBlock(blk2)
            app.repository.saveTimeBlock(blkMiss)
            app.repository.saveRecurrenceRule(RecurrenceRule(id = "rec-mimir", entityType = "block", templateId = blk2.id, pattern = "weekdays", daysOfWeek = "1,3,5", startDate = d0))

            app.repository.saveNotebook(KnowledgeNotebook(id = "nb-1", name = "Caderno Estratégico", areaId = "area-estudos"))
            app.repository.saveFlashcard(Flashcard(id = "fc-1", notebookId = "nb-1", question = "O que é folga no CPM?", answer = "LS - ES (tempo que uma tarefa pode atrasar sem atrasar o projeto).", nextReviewDate = d0))

            app.repository.saveStrategicGoal(StrategicGoal(id = "goal-q", title = "Concluir Arquitetura 1.7.5", description = "Consolidar motor offline e Gantt/Kanban", areaId = "area-carreira", horizon = "quarterly", periodLabel = "2026 · T2", linkedPathIds = path.id))
            app.repository.saveStrategicGoal(StrategicGoal(id = "goal-a", title = "Constância de Estudo Anual", description = "Manter ritmo sustentável de estudos", areaId = "area-estudos", horizon = "annual", periodLabel = "2026", linkedHabitIds = habit1.id))
            app.repository.refresh()
        }
        wait { app.repository.state.value!!.strategicGoals.size == 2 && app.repository.state.value!!.lifeAreas.size == 9 }
        return app.repository.state.value!!.paths.first { it.id == "path-odin-175" }
    }

    private fun renderScreen(dark: Boolean = true, fontScale: Float = 1f, widthDp: Int? = null, content: @Composable () -> Unit) {
        compose.activity.runOnUiThread {
            compose.activity.setContent {
                val baseDensity = LocalDensity.current
                val themeStr = if (dark) "dark" else "light"
                CompositionLocalProvider(LocalDensity provides Density(baseDensity.density, fontScale = fontScale)) {
                    AscendTheme(themeStr, reducedMotion = true, minimal = false) {
                        val mod = if (widthDp != null) Modifier.width(widthDp.dp).fillMaxHeight() else Modifier.fillMaxSize()
                        Surface(mod, color = Night) {
                            content()
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
    }

    @Test
    fun captureAll17RequiredScreenshotsFor175() {
        val path = seedCouncilState()
        val st = { app.repository.state.value!! }

        // 01 — Mesa de Odin (Salão do Conselho)
        renderScreen { OdinCouncilScreen(st(), vm, initialTab = 0) }
        compose.onNodeWithText("Mesa de Odin").assertExists()
        shot("01-mesa-de-odin")

        // 02 — Configuração de disponibilidade
        renderScreen { OdinCouncilScreen(st(), vm, initialTab = 5) }
        compose.onNodeWithText("Janelas semanais autorizadas", ignoreCase = true).assertExists()
        shot("02-configuracao-disponibilidade")

        // 03 — Proposta de plano semanal
        renderScreen { OdinCouncilScreen(st(), vm, initialTab = 1) }
        compose.onNodeWithText("Estratégia da prévia").assertExists()
        shot("03-proposta-plano-semanal")

        // 04 — Estratégias de Odin (scroll no Salão para destacar a grade de 6 medalhões)
        renderScreen { OdinCouncilScreen(st(), vm, initialTab = 0) }
        compose.onNodeWithTag("odin_council_list").performTouchInput { swipeUp() }
        compose.waitForIdle()
        shot("04-estrategias-odin")

        // 05 — Simulador de capacidade
        renderScreen { OdinCouncilScreen(st(), vm, initialTab = 2) }
        compose.onNodeWithText("Simulador de capacidade", ignoreCase = true).assertExists()
        shot("05-simulador-capacidade")

        // 06 — Replanejamento adaptativo
        renderScreen { OdinCouncilScreen(st(), vm, initialTab = 3) }
        compose.onNodeWithText("Replanejamento adaptativo", ignoreCase = true).assertExists()
        shot("06-replanejamento-adaptativo")

        // 07 — Matriz de Eisenhower
        renderScreen { OdinCouncilScreen(st(), vm, initialTab = 4) }
        compose.onNodeWithText("Matriz de Eisenhower", ignoreCase = true).assertExists()
        shot("07-matriz-eisenhower")

        // 08 — Caminhos 3.0: Dependências e Caminho Crítico
        renderScreen {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Path30AdvancedSection(path = path, s = st(), vm = vm, initialMode = 2)
            }
        }
        compose.onNodeWithText("Caminho Crítico (CPM)", ignoreCase = true).assertExists()
        shot("08-caminhos-dependencias-critico")

        // 09 — Gantt interativo
        renderScreen {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Path30AdvancedSection(path = path, s = st(), vm = vm, initialMode = 0)
            }
        }
        compose.onNodeWithTag("gantt_chart_canvas").assertExists()
        shot("09-gantt-interativo")

        // 10 — Quadro Kanban
        renderScreen {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Path30AdvancedSection(path = path, s = st(), vm = vm, initialMode = 1)
            }
        }
        compose.onNodeWithTag("kanban_board").assertExists()
        shot("10-quadro-kanban")

        // 11 — Agenda 3.0 com recorrência e fixação
        renderScreen { PlannerScreen(st(), vm, { _, _ -> }) }
        compose.onNodeWithTag("planner_list").performScrollToNode(hasTestTag("block:blk-1"))
        compose.waitForIdle()
        shot("11-agenda-30-recorrencia-fixacao")

        // 12 — Forja do Tempo (Pomodoro ativo)
        vm.run {
            app.repository.startTimeForgeFocus(
                mode = "pomodoro",
                workMinutes = 25,
                shortBreakMinutes = 5,
                longBreakMinutes = 15,
                totalCycles = 4,
                blockId = "blk-1",
                targetLabel = "Motor Estratégico · Pomodoro"
            )
            vm.setAmbientScene("rain")
        }
        wait { app.repository.state.value!!.focus != null }
        renderScreen { TimeForgeScreen(st(), vm, initialImmersive = false) }
        compose.onNodeWithText("Forja do Tempo").assertExists()
        shot("12-forja-do-tempo-pomodoro")

        // 13 — Modo Imersivo de Foco
        renderScreen { TimeForgeScreen(st(), vm, initialImmersive = true) }
        compose.onNodeWithTag("immersive_focus_view").assertExists()
        shot("13-modo-imersivo-foco")
        vm.run { app.repository.endFocus(register = false) }
        wait { app.repository.state.value!!.focus == null }

        // 14 — Conselho dos Nove (Áreas da Vida)
        renderScreen { CouncilOfNineScreen(st(), vm, initialTab = 0) }
        compose.onNodeWithText("Conselho dos Nove").assertExists()
        shot("14-conselho-dos-nove")

        // 15 — Objetivos Pessoais
        renderScreen { CouncilOfNineScreen(st(), vm, initialTab = 1) }
        compose.onNodeWithText("Concluir Arquitetura 1.7.5").assertExists()
        shot("15-objetivos-pessoais")

        // 16 — Tema claro (Mesa de Odin)
        renderScreen(dark = false) { OdinCouncilScreen(st(), vm, initialTab = 0) }
        shot("16-tema-claro-175")

        // 17 — Tela estreita 320dp + fonte 130%
        renderScreen(dark = true, fontScale = 1.3f, widthDp = 320) { OdinCouncilScreen(st(), vm, initialTab = 0) }
        shot("17-estreito-320dp-fonte-130-175")

        val expected = listOf(
            "01-mesa-de-odin", "02-configuracao-disponibilidade", "03-proposta-plano-semanal",
            "04-estrategias-odin", "05-simulador-capacidade", "06-replanejamento-adaptativo",
            "07-matriz-eisenhower", "08-caminhos-dependencias-critico", "09-gantt-interativo",
            "10-quadro-kanban", "11-agenda-30-recorrencia-fixacao", "12-forja-do-tempo-pomodoro",
            "13-modo-imersivo-foco", "14-conselho-dos-nove", "15-objetivos-pessoais",
            "16-tema-claro-175", "17-estreito-320dp-fonte-130-175"
        )
        expected.forEach { name ->
            val f = File("build/reports/screenshots/$name.png")
            assertTrue("Missing screenshot $name.png", f.exists() && f.length() > 2000L)
        }
    }
}
