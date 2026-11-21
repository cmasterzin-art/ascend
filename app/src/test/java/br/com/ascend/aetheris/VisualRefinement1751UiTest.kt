package br.com.ascend.aetheris

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
 * ASCEND 1.7.5.1 — Etapa II (Nordic Viking Visual Refinement)
 * Suíte UI Robolectric + Compose capturando as 14 capturas reais comparativas
 * do Design System 3.2.1 para inspeção visual e validação contínua.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = AscendApp::class, qualifiers = "w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class VisualRefinement1751UiTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()
    private val app get() = RuntimeEnvironment.getApplication() as AscendApp
    private val vm get() = ViewModelProvider(compose.activity)[GameViewModel::class.java]

    private var hostMounted = false
    private val slotDark = mutableStateOf(true)
    private val slotFontScale = mutableFloatStateOf(1f)
    private val slotWidthDp = mutableStateOf<Int?>(null)
    private val slotKey = mutableIntStateOf(0)
    private val slotContent = mutableStateOf<@Composable () -> Unit>({})

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
            val view = compose.activity.findViewById<android.view.View>(android.R.id.content) ?: compose.activity.window.decorView
            val bitmap = android.graphics.Bitmap.createBitmap(
                view.width.coerceAtLeast(1),
                view.height.coerceAtLeast(1),
                android.graphics.Bitmap.Config.ARGB_8888
            )
            view.draw(android.graphics.Canvas(bitmap))
            val file = File("build/reports/screenshots/1.7.5.1/$name.png")
            file.parentFile!!.mkdirs()
            file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    private fun seedRefinedState(): PersonalPath {
        val d0 = today()
        val yesterday = LocalDate.parse(d0).minusDays(1).toString()
        val d2 = LocalDate.parse(d0).plusDays(2).toString()
        val d5 = LocalDate.parse(d0).plusDays(5).toString()
        val d10 = LocalDate.parse(d0).plusDays(10).toString()

        vm.run {
            app.repository.applyDefaultAvailability()
            app.repository.seedDefaultLifeAreas()
            app.repository.setOdinStrategy("strategist")

            val habit1 = Habit(id = "h-est", name = "Estudo dirigido", type = 2, target = 45, unit = "min", attribute = 0, areaId = "area-estudos")
            val habit2 = Habit(id = "h-vig", name = "Caminhada matinal", type = 0, target = 1, unit = "vez", attribute = 1, areaId = "area-saude")
            app.repository.habit(habit1)
            app.repository.habit(habit2)

            val path = PersonalPath(
                id = "path-odin-1751",
                name = "Arquitetura de Aetheris",
                description = "Estabilização e refinamento visual nórdico 1.7.5.1",
                category = "Tecnologia",
                priority = 3,
                state = 1,
                attribute = 0,
                startDate = d0,
                deadline = d10,
                areaId = "area-carreira"
            )
            app.repository.savePath(path)
            app.repository.saveStage(PathStage(id = "st-1", pathId = path.id, name = "Motor Estratégico", position = 0))
            app.repository.saveStage(PathStage(id = "st-2", pathId = path.id, name = "Forja Visual", position = 1))

            app.repository.saveTask(PathTask(id = "t-1", pathId = path.id, stageId = "st-1", title = "Modelar intervalos livres", estimateMinutes = 60, priority = 3, deadline = d2, scheduledDate = d0, workflowStatus = 1, important = true, urgent = true, areaId = "area-carreira"))
            app.repository.saveTask(PathTask(id = "t-2", pathId = path.id, stageId = "st-1", title = "Implementar caminho crítico", estimateMinutes = 90, priority = 3, deadline = d5, workflowStatus = 0, important = true, urgent = false, areaId = "area-carreira"))
            app.repository.saveTask(PathTask(id = "t-3", pathId = path.id, stageId = "st-2", title = "Refinar Gantt e Kanban", estimateMinutes = 75, priority = 2, deadline = d10, workflowStatus = 0, important = true, urgent = false, areaId = "area-criatividade"))
            app.repository.saveTask(PathTask(id = "t-4", pathId = path.id, stageId = "st-2", title = "Revisar notas atrasadas", estimateMinutes = 40, priority = 2, scheduledDate = yesterday, deadline = yesterday, workflowStatus = 0, important = false, urgent = true, areaId = "area-estudos"))
            app.repository.saveTask(PathTask(id = "t-5", pathId = path.id, stageId = "st-1", title = "Levantamento inicial", estimateMinutes = 30, priority = 1, completed = true, workflowStatus = 0, completedDate = d0, areaId = "area-carreira"))

            app.repository.addTaskDependency("t-2", "t-1")
            app.repository.addTaskDependency("t-3", "t-2")
            app.repository.savePathMilestone(PathMilestone(id = "ms-1", pathId = path.id, title = "Núcleo determinístico pronto", targetDate = d2))
            app.repository.savePathMilestone(PathMilestone(id = "ms-2", pathId = path.id, title = "Entrega 1.7.5.1 validada", targetDate = d10))

            app.repository.saveEvent(AgendaEvent(id = "ev-1", title = "Conselho de arquitetura", date = d0, startTime = "10:30", endTime = "11:30", kind = "Reunião", areaId = "area-carreira"))
            val blk1 = TimeBlock(id = "blk-1", title = "Foco · Motor Estratégico", date = d0, startTime = "09:00", endTime = "10:00", category = "Projeto", pathId = path.id, taskId = "t-1", pinned = true, sourcePlanId = "plan-seed", areaId = "area-carreira")
            // Bloco-base ontem com recorrência diária para projetar ocorrência virtual @rec:d0 na Agenda de hoje!
            val blkRecBase = TimeBlock(id = "blk-rec", title = "Revisão diária Mímir", date = yesterday, startTime = "14:00", endTime = "14:45", category = "Estudo", areaId = "area-estudos")
            val blkMiss = TimeBlock(id = "blk-miss", title = "Revisar notas atrasadas", date = yesterday, startTime = "15:00", endTime = "15:40", category = "Estudo", pathId = path.id, taskId = "t-4", areaId = "area-estudos")
            app.repository.saveTimeBlock(blk1)
            app.repository.saveTimeBlock(blkRecBase)
            app.repository.saveTimeBlock(blkMiss)
            app.repository.saveRecurrenceRule(RecurrenceRule(id = "rec-mimir", entityType = "block", templateId = blkRecBase.id, pattern = "daily", startDate = yesterday))

            app.repository.saveNotebook(KnowledgeNotebook(id = "nb-1", name = "Caderno Estratégico", areaId = "area-estudos"))
            app.repository.saveFlashcard(Flashcard(id = "fc-1", notebookId = "nb-1", question = "O que é folga no CPM?", answer = "LS - ES.", nextReviewDate = d0))

            app.repository.saveStrategicGoal(StrategicGoal(id = "goal-q", title = "Concluir Arquitetura 1.7.5.1", description = "Consolidar estabilidade e estética nórdica", areaId = "area-carreira", horizon = "quarterly", periodLabel = "2026 · T2", linkedPathIds = path.id))
            app.repository.saveStrategicGoal(StrategicGoal(id = "goal-a", title = "Constância de Estudo Anual", description = "Manter ritmo sustentável de estudos", areaId = "area-estudos", horizon = "annual", periodLabel = "2026", linkedHabitIds = habit1.id))
            app.repository.refresh()
        }
        wait { app.repository.state.value!!.strategicGoals.size == 2 && app.repository.state.value!!.lifeAreas.size == 9 }
        return app.repository.state.value!!.paths.first { it.id == "path-odin-1751" }
    }

    private fun renderScreen(dark: Boolean = true, fontScale: Float = 1f, widthDp: Int? = null, content: @Composable () -> Unit) {
        compose.activity.runOnUiThread {
            slotDark.value = dark
            slotFontScale.floatValue = fontScale
            slotWidthDp.value = widthDp
            slotContent.value = content
            slotKey.intValue += 1
            if (!hostMounted) {
                hostMounted = true
                val cv = androidx.compose.ui.platform.ComposeView(compose.activity).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setContent {
                        val baseDensity = LocalDensity.current
                        val themeStr = if (slotDark.value) "dark" else "light"
                        val w = slotWidthDp.value
                        val k = slotKey.intValue
                        CompositionLocalProvider(LocalDensity provides Density(baseDensity.density, fontScale = slotFontScale.floatValue)) {
                            AscendTheme(themeStr, reducedMotion = true, minimal = false) {
                                Box(Modifier.fillMaxSize()) {
                                    val mod = if (w != null) Modifier.width(w.dp).fillMaxHeight() else Modifier.fillMaxSize()
                                    Surface(mod, color = Night) {
                                        key(k) {
                                            slotContent.value()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                compose.activity.setContentView(cv)
            }
        }
        compose.waitForIdle()
    }

    @Test
    fun captureAll14ComparativeScreenshotsFor1751() {
        val path = seedRefinedState()
        val st = { app.repository.state.value!! }

        // 01 — Mesa de Odin refinada
        renderScreen { OdinCouncilScreen(st(), vm, initialTab = 0) }
        compose.onNodeWithText("Mesa de Odin").assertExists()
        shot("01-mesa-de-odin-refinada")

        // 02 — Botões vikings refinados (catálogo real das 6 famílias DS 3.2.1 + estados + marcadores)
        renderScreen {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Title("Forja de Botões · DS 3.2.1", "Seis famílias metálicas em restored_council_buttons_atlas, estados visuais e marcadores rúnicos.")
                CouncilMaterialSurface(CouncilMaterial.NordicWood) {
                    NordicSectionHeader("Hierarquia das 6 famílias metálicas", rune = HistoricalFutharkRune.Tiwaz)
                    SagaButton("Cerimonial Viking · Ação decisiva", {}, Modifier.fillMaxWidth(), style = VikingButtonStyle.CerimonialViking)
                    SagaButton("Bronze Forjado · Ação principal", {}, Modifier.fillMaxWidth(), style = VikingButtonStyle.BronzeForjado)
                    SagaButton("Ferro Rúnico · Ação secundária", {}, Modifier.fillMaxWidth(), style = VikingButtonStyle.FerroRunico)
                    SagaButton("Confirmação · Aplicar e salvar", {}, Modifier.fillMaxWidth(), style = VikingButtonStyle.Confirmacao)
                    SagaButton("Destrutivo · Excluir ou reverter", {}, Modifier.fillMaxWidth(), style = VikingButtonStyle.Destrutivo)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SagaButton("Compacto Rúnico", {}, Modifier.weight(1f), style = VikingButtonStyle.CompactoRunico)
                        SagaButton("Concluído", {}, Modifier.weight(1f), completed = true)
                    }
                    SagaButton("Estado desativado (contraste reduzido)", {}, Modifier.fillMaxWidth(), enabled = false, style = VikingButtonStyle.BronzeForjado)
                }
                CouncilMaterialSurface(CouncilMaterial.DarkIron) {
                    NordicSectionHeader("Marcadores de Planejamento & Divisores", rune = HistoricalFutharkRune.Ansuz)
                    val markNames = listOf("Crítica", "Dependência", "Marco", "Prazo", "Conflito", "Fixada", "Recorrente", "Concluído")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        PlanningMark.entries.take(4).forEachIndexed { idx, m ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                NordicPlanningMark(m, Modifier.size(28.dp))
                                Text(markNames[idx], color = Muted, fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        PlanningMark.entries.drop(4).forEachIndexed { idx, m ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                NordicPlanningMark(m, Modifier.size(28.dp))
                                Text(markNames[idx + 4], color = Muted, fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }
                    NordicStrategyDivider(StrategyFrameAsset.UrnesDivider, rune = HistoricalFutharkRune.Tiwaz)
                    NordicStrategyDivider(StrategyFrameAsset.BorreDivider, rune = HistoricalFutharkRune.Jera, accent = Blue)
                }
            }
        }
        shot("02-botoes-vikings-refinados")

        // 03 — Estratégias de Odin refinadas
        renderScreen { OdinCouncilScreen(st(), vm, initialTab = 0) }
        compose.onNodeWithTag("odin_council_list").performTouchInput { swipeUp() }
        compose.waitForIdle()
        shot("03-estrategias-odin-refinadas")

        // 04 — Propostas refinadas
        renderScreen { OdinCouncilScreen(st(), vm, initialTab = 1) }
        compose.onNodeWithText("Estratégia da prévia").assertExists()
        shot("04-propostas-refinadas")

        // 05 — Simulador de capacidade refinado
        renderScreen { OdinCouncilScreen(st(), vm, initialTab = 2) }
        compose.onNodeWithText("Simulador de capacidade", ignoreCase = true).assertExists()
        shot("05-simulador-capacidade-refinado")

        // 06 — Gantt refinado
        renderScreen {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Path30AdvancedSection(path = path, s = st(), vm = vm, initialMode = 0)
            }
        }
        compose.onNodeWithTag("gantt_chart_canvas").assertExists()
        shot("06-gantt-refinado")

        // 07 — Kanban refinado
        renderScreen {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Path30AdvancedSection(path = path, s = st(), vm = vm, initialMode = 1)
            }
        }
        compose.onNodeWithTag("kanban_board").assertExists()
        shot("07-kanban-refinado")

        // 08 — Agenda com recorrência projetada (@rec:) e fixação
        renderScreen { PlannerScreen(st(), vm, { _, _ -> }) }
        compose.onNodeWithTag("planner_list").performScrollToNode(hasTestTag("block:blk-1"))
        compose.waitForIdle()
        shot("08-agenda-recorrencia-refinada")

        // 09 — Forja do Tempo (Trabalho · aro bronze + medalhões de ciclo)
        vm.run {
            app.repository.startTimeForgeFocus(
                mode = "pomodoro",
                workMinutes = 25,
                shortBreakMinutes = 5,
                longBreakMinutes = 15,
                totalCycles = 4,
                autoAdvance = true,
                blockId = "blk-1",
                targetLabel = "Motor Estratégico · Pomodoro"
            )
            vm.setAmbientScene("rain")
        }
        wait { app.repository.state.value!!.focus != null }
        renderScreen { TimeForgeScreen(st(), vm, initialImmersive = false) }
        compose.onNodeWithText("Forja do Tempo").assertExists()
        shot("09-forja-do-tempo-trabalho")

        // 10 — Forja do Tempo (Descanso · aro prata oxidada + selo pausa regenerativa)
        vm.run { app.repository.advanceFocusPhase() }
        wait { app.repository.state.value!!.focus?.isBreakPhase == true }
        renderScreen { TimeForgeScreen(st(), vm, initialImmersive = false) }
        compose.onNodeWithText("PAUSA CURTA · NÃO CONTA COMO FOCO").assertExists()
        shot("10-forja-do-tempo-descanso")

        // 11 — Modo Imersivo refinado (voltando ao foco para contraste ouro/carvão)
        vm.run { app.repository.advanceFocusPhase() }
        wait { app.repository.state.value!!.focus?.isBreakPhase == false }
        renderScreen { TimeForgeScreen(st(), vm, initialImmersive = true) }
        compose.onNodeWithTag("immersive_focus_view").assertExists()
        shot("11-modo-imersivo-refinado")
        vm.run { app.repository.endFocus(register = false) }
        wait { app.repository.state.value!!.focus == null }

        // 12 — Conselho dos Nove refinado
        renderScreen { CouncilOfNineScreen(st(), vm, initialTab = 0) }
        compose.onNodeWithText("Conselho dos Nove").assertExists()
        shot("12-conselho-dos-nove-refinado")

        // 13 — Tema claro 1.7.5.1
        renderScreen(dark = false) { OdinCouncilScreen(st(), vm, initialTab = 0) }
        shot("13-tema-claro-1751")

        // 14 — Tela estreita 320dp + fonte 130%
        renderScreen(dark = true, fontScale = 1.3f, widthDp = 320) { OdinCouncilScreen(st(), vm, initialTab = 0) }
        shot("14-estreito-320dp-fonte-130-1751")

        val expected = listOf(
            "01-mesa-de-odin-refinada",
            "02-botoes-vikings-refinados",
            "03-estrategias-odin-refinadas",
            "04-propostas-refinadas",
            "05-simulador-capacidade-refinado",
            "06-gantt-refinado",
            "07-kanban-refinado",
            "08-agenda-recorrencia-refinada",
            "09-forja-do-tempo-trabalho",
            "10-forja-do-tempo-descanso",
            "11-modo-imersivo-refinado",
            "12-conselho-dos-nove-refinado",
            "13-tema-claro-1751",
            "14-estreito-320dp-fonte-130-1751"
        )
        expected.forEach { name ->
            val f = File("build/reports/screenshots/1.7.5.1/$name.png")
            assertTrue("Missing screenshot $name.png", f.exists() && f.length() > 2000L)
        }
    }
}
