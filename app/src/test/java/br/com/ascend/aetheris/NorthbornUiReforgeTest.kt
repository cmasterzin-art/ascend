package br.com.ascend.aetheris

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
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
 * ASCEND 1.7.5.9 — A Reforma de Aetheris (NORTHBORN Design System 4.0)
 * Suíte UI Robolectric + Compose capturando todas as 30 telas obrigatórias
 * em build/reports/screenshots/1.7.5.9.3/ para inspeção visual e validação de regressão.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = AscendApp::class, qualifiers = "w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NorthbornUiReforgeTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()
    private val app get() = RuntimeEnvironment.getApplication() as AscendApp
    private val vm get() = ViewModelProvider(compose.activity)[GameViewModel::class.java]

    private var hostMounted = false
    private val slotDark = mutableStateOf(true)
    private val slotMinimal = mutableStateOf(false)
    private val slotReducedMotion = mutableStateOf(true)
    private val slotFontScale = mutableFloatStateOf(1f)
    private val slotWidthDp = mutableStateOf<Int?>(null)
    private val slotKey = mutableIntStateOf(0)
    private val slotContent = mutableStateOf<@Composable () -> Unit>({})

    @Before
    fun setup() {
        runBlocking { app.preferences.edit { it.clear() } }
        wait { app.repository.state.value != null }
        vm.run {
            app.repository.create(Player(name = "Sigrid", heroClass = 1, face = 0, title = "Portadora da Brasa"), emptyList())
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
            val file = File("build/reports/screenshots/1.7.5.9.3/$name.png")
            file.parentFile!!.mkdirs()
            file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    private fun seedNorthbornState(): Pair<PersonalPath, Habit> {
        val d0 = today()
        val yesterday = LocalDate.parse(d0).minusDays(1).toString()
        val d2 = LocalDate.parse(d0).plusDays(2).toString()
        val d5 = LocalDate.parse(d0).plusDays(5).toString()
        val d10 = LocalDate.parse(d0).plusDays(10).toString()

        vm.run {
            app.repository.applyDefaultAvailability()
            app.repository.seedDefaultLifeAreas()
            app.repository.setOdinStrategy("strategist")

            val habit1 = Habit(id = "h-est", name = "Estudo dirigido", type = 2, target = 45, unit = "minutos", xp = 60, coins = 12, attribute = 0, color = 1, areaId = "area-estudos", created = yesterday)
            val habit2 = Habit(id = "h-vig", name = "Caminhada matinal", type = 0, target = 1, unit = "vez", xp = 30, coins = 6, attribute = 1, color = 3, areaId = "area-saude")
            val habit3 = Habit(id = "h-lei", name = "Leitura técnica", type = 1, target = 20, unit = "páginas", xp = 35, coins = 8, attribute = 0, color = 0, areaId = "area-estudos")
            app.repository.habit(habit1)
            app.repository.habit(habit2)
            app.repository.habit(habit3)
            app.repository.progress(habit2, d0, 1)
            app.repository.progress(habit1, yesterday, 45)

            val path = PersonalPath(
                id = "path-northborn",
                name = "Arquitetura de Aetheris",
                description = "Consolidação visual NORTHBORN 4.0 e planejamento determinístico",
                category = "Tecnologia",
                priority = 3,
                state = 1,
                attribute = 0,
                startDate = d0,
                deadline = d10,
                areaId = "area-carreira"
            )
            app.repository.savePath(path)
            app.repository.saveStage(PathStage(id = "st-1", pathId = path.id, name = "Núcleo Estratégico", position = 0))
            app.repository.saveStage(PathStage(id = "st-2", pathId = path.id, name = "Reforma Visual", position = 1))

            app.repository.saveTask(PathTask(id = "t-1", pathId = path.id, stageId = "st-1", title = "Modelar intervalos livres", estimateMinutes = 60, priority = 3, deadline = d2, scheduledDate = d0, workflowStatus = 1, important = true, urgent = true, areaId = "area-carreira"))
            app.repository.saveTask(PathTask(id = "t-2", pathId = path.id, stageId = "st-1", title = "Validar caminho crítico", estimateMinutes = 90, priority = 3, deadline = d5, workflowStatus = 0, important = true, urgent = false, areaId = "area-carreira"))
            app.repository.saveTask(PathTask(id = "t-3", pathId = path.id, stageId = "st-2", title = "Revisar contraste claro/escuro", estimateMinutes = 75, priority = 2, deadline = d10, workflowStatus = 0, important = true, urgent = false, areaId = "area-criatividade"))
            app.repository.saveTask(PathTask(id = "t-4", pathId = path.id, stageId = "st-2", title = "Revisar notas atrasadas", estimateMinutes = 40, priority = 2, scheduledDate = yesterday, deadline = yesterday, workflowStatus = 0, important = false, urgent = true, areaId = "area-estudos"))
            app.repository.saveTask(PathTask(id = "t-5", pathId = path.id, stageId = "st-1", title = "Auditoria global de telas", estimateMinutes = 30, priority = 1, completed = true, workflowStatus = 0, completedDate = d0, areaId = "area-carreira"))

            app.repository.addTaskDependency("t-2", "t-1")
            app.repository.addTaskDependency("t-3", "t-2")
            app.repository.savePathMilestone(PathMilestone(id = "ms-1", pathId = path.id, title = "Design System 4.0 ativo", targetDate = d2))
            app.repository.savePathMilestone(PathMilestone(id = "ms-2", pathId = path.id, title = "Entrega 1.7.5.9", targetDate = d10))

            app.repository.saveEvent(AgendaEvent(id = "ev-1", title = "Conselho de arquitetura", date = d0, startTime = "10:30", endTime = "11:30", kind = "Reunião", areaId = "area-carreira"))
            val blk1 = TimeBlock(id = "blk-1", title = "Foco · Núcleo Estratégico", date = d0, startTime = "09:00", endTime = "10:00", category = "Projeto", pathId = path.id, taskId = "t-1", pinned = true, sourcePlanId = "plan-seed", areaId = "area-carreira")
            val blkRecBase = TimeBlock(id = "blk-rec", title = "Revisão diária Mímir", date = yesterday, startTime = "14:00", endTime = "14:45", category = "Estudo", areaId = "area-estudos")
            app.repository.saveTimeBlock(blk1)
            app.repository.saveTimeBlock(blkRecBase)
            app.repository.saveRecurrenceRule(RecurrenceRule(id = "rec-mimir", entityType = "block", templateId = blkRecBase.id, pattern = "daily", startDate = yesterday))

            app.repository.saveNotebook(KnowledgeNotebook(id = "nb-1", name = "Caderno de Estratégia", description = "Notas de arquitetura, planejamento e runas históricas.", category = "Estudos", areaId = "area-estudos"))
            app.repository.savePage(KnowledgePage(id = "pg-1", notebookId = "nb-1", title = "Princípios NORTHBORN", content = "Menos caixas repetitivas, mais espaço negativo e tipografia autoral.", tags = "design,nordico"))
            app.repository.saveFlashcard(Flashcard(id = "fc-1", notebookId = "nb-1", pageId = "pg-1", question = "O que define a folga de uma tarefa no Caminho Crítico (CPM)?", answer = "A diferença entre o início mais tardio (LS) e o início mais cedo (ES). Folga zero indica tarefa crítica.", tags = "cpm,odin", nextReviewDate = d0))

            app.repository.saveStrategicGoal(StrategicGoal(id = "goal-q", title = "Consolidar Reforma NORTHBORN", description = "Elevar coerência material e clareza tipográfica em todos os módulos", areaId = "area-carreira", horizon = "quarterly", periodLabel = "2026 · T2", linkedPathIds = path.id))
            app.repository.saveStrategicGoal(StrategicGoal(id = "goal-a", title = "Constância de Estudo Anual", description = "Manter ritmo sustentável de estudos sem sobrecarga", areaId = "area-estudos", horizon = "annual", periodLabel = "2026", linkedHabitIds = habit1.id))
            app.repository.refresh()
        }
        wait { app.repository.state.value!!.strategicGoals.size == 2 && app.repository.state.value!!.lifeAreas.size == 9 }
        val st = app.repository.state.value!!
        return st.paths.first { it.id == "path-northborn" } to st.habits.first { it.id == "h-est" }
    }

    private fun renderScreen(
        dark: Boolean = true,
        minimal: Boolean = false,
        fontScale: Float = 1f,
        widthDp: Int? = null,
        reducedMotion: Boolean = true,
        content: @Composable () -> Unit
    ) {
        compose.activity.runOnUiThread {
            slotDark.value = dark
            slotMinimal.value = minimal
            slotReducedMotion.value = reducedMotion
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
                        val base = LocalDensity.current
                        val isDark = slotDark.value
                        val isMinimal = slotMinimal.value
                        val scale = slotFontScale.floatValue
                        val w = slotWidthDp.value
                        val key = slotKey.intValue
                        val body = slotContent.value
                        CompositionLocalProvider(
                            LocalDensity provides Density(base.density, fontScale = scale)
                        ) {
                            AscendTheme(if (isDark) "dark" else "light", reducedMotion = slotReducedMotion.value, minimal = isMinimal) {
                                Box(Modifier.fillMaxSize().background(LocalNorthborn.current.canvas)) {
                                    Surface(
                                        color = LocalNorthborn.current.canvas,
                                        modifier = if (w != null) Modifier.width(w.dp).fillMaxHeight() else Modifier.fillMaxSize()
                                    ) {
                                        key(key) { body() }
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
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
    }

    @Composable
    private fun controlSpecimens(onCommit: () -> Unit) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Controles de Aetheris", style = MaterialTheme.typography.headlineMedium)
            NorthbornButton("Registrar progresso", onCommit, modifier = Modifier.fillMaxWidth())
            NorthbornButton("Editar registro", {}, NorthbornButtonRole.Secondary, Modifier.fillMaxWidth())
            NorthbornButton("Receber recompensa", {}, NorthbornButtonRole.Ceremonial, Modifier.fillMaxWidth())
            NorthbornButton("Confirmar ritual", {}, NorthbornButtonRole.Confirm, Modifier.fillMaxWidth())
            NorthbornButton("Remover registro", {}, NorthbornButtonRole.Destructive, Modifier.fillMaxWidth())
            NorthbornButton("Registrando…", onCommit, loading = true, modifier = Modifier.fillMaxWidth())
            NorthbornButton("Indisponível", onCommit, enabled = false, modifier = Modifier.fillMaxWidth())
            NordicCompactAction("Editar prazo", {}, rune = HistoricalFutharkRune.Tiwaz)
            ChipRow(listOf("Agenda", "Mês"), 0, {})
        }
    }

    @Test
    fun forgedControlsKeepTheirActionsAndStatesAcrossAppearanceModes() {
        var commits = 0
        renderScreen { controlSpecimens { commits++ } }
        compose.onNodeWithText("Registrar progresso").performClick()
        compose.onNodeWithText("Registrando…").assertIsNotEnabled()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Em andamento"))
        compose.onNodeWithText("Indisponível").assertIsNotEnabled()
        compose.onNodeWithText("Agenda").assertIsSelected()
        compose.onNodeWithText("Mês").assertIsNotSelected()
        compose.runOnIdle { assertTrue(commits == 1) }
        shot("57-forja-controles-escuro")

        renderScreen(dark = false, fontScale = 1.3f, widthDp = 320) { controlSpecimens {} }
        compose.onNodeWithText("Receber recompensa").assertIsDisplayed()
        compose.onNodeWithText("Editar prazo").assertHeightIsAtLeast(48.dp)
        shot("58-forja-controles-claro-320-130")

        renderScreen(minimal = true, fontScale = 1.3f, widthDp = 320) { controlSpecimens {} }
        compose.onNodeWithText("Registrar progresso").assertIsEnabled()
        shot("59-forja-controles-minimalista")
    }

    @Test
    fun captureAll30NorthbornReforgeScreenshots() {
        val (path, estHabit) = seedNorthbornState()
        val s0 = app.repository.state.value!!

        // 01. Jornada / tela inicial completa
        renderScreen {
            Scaffold(
                containerColor = LocalNorthborn.current.canvas,
                bottomBar = { AetherisNavigation("journey") {} }
            ) { pad ->
                Box(Modifier.padding(pad)) {
                    Journey(s0, { _, _ -> }, {}, {}, {}, {}, vm, relaxed = false)
                }
            }
        }
        compose.onNodeWithText("Seus próximos passos").assertExists()
        shot("01-jornada")

        // 02. Barra de navegação inferior e transição de contexto (aba Herói / Mundo / Missões)
        renderScreen {
            Scaffold(
                containerColor = LocalNorthborn.current.canvas,
                bottomBar = { AetherisNavigation("hero") {} }
            ) { pad ->
                Box(Modifier.padding(pad)) {
                    CharacterScreen(s0, vm, onSettings = {}, onJournal = {})
                }
            }
        }
        shot("02-navegacao-inferior")

        // 03. Fluxo de registro de hábito
        renderScreen {
            Column(Modifier.fillMaxSize().background(LocalNorthborn.current.canvas)) {
                NorthbornTopBar("Registrar · ${estHabit.name}", onBack = {}, trailing = {
                    NorthbornButton("Salvar", {}, role = NorthbornButtonRole.Primary)
                })
                Page {
                    FormField(today(), {}, "Data · AAAA-MM-DD")
                    Text("Meta deste registro: ${estHabit.target} ${estHabit.unit}", color = LocalNorthborn.current.accentBronze)
                    FormField("30", {}, "Progresso total nesta data", number = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5, 15, 25).forEach { step -> NorthbornButton("+$step", {}, role = NorthbornButtonRole.Secondary) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NorthbornButton("Desfazer", {}, role = NorthbornButtonRole.Secondary, modifier = Modifier.weight(1f))
                        NorthbornButton("Atingir meta", {}, role = NorthbornButtonRole.Primary, modifier = Modifier.weight(1f))
                    }
                    NorthbornSurface(role = NorthbornSurfaceRole.Informative) {
                        Text("Informe o total realizado no dia. O aplicativo concede apenas a diferença de recompensa.", color = LocalNorthborn.current.inkSecondary)
                    }
                }
            }
        }
        shot("03-registro-habito")

        // 04. Lista de hábitos
        renderScreen {
            Scaffold(
                containerColor = LocalNorthborn.current.canvas,
                bottomBar = { AetherisNavigation("habits") {} }
            ) { pad ->
                Box(Modifier.padding(pad)) {
                    HabitsScreen(s0, vm, onAdd = {}, onEdit = {}, onProgress = { _, _ -> }, onFocus = {})
                }
            }
        }
        compose.onNodeWithText("Estudo dirigido").assertExists()
        shot("04-lista-habitos")

        // 05. Perfil do herói e Retrato Vivo
        renderScreen {
            CharacterScreen(s0, vm, onSettings = {}, onJournal = {})
        }
        shot("05-perfil-heroi")

        // 06. Mesa de Odin (Salão)
        renderScreen {
            OdinCouncilScreen(s = s0, vm = vm, initialTab = 0)
        }
        compose.onNodeWithText("Mesa de Odin").assertExists()
        shot("06-mesa-de-odin")

        // 07. Proposta da Mesa de Odin
        renderScreen {
            OdinCouncilScreen(s = s0, vm = vm, initialTab = 1)
        }
        compose.onNodeWithText("Estratégia da prévia").assertExists()
        shot("07-propostas-odin")

        // 08. Agenda diária
        renderScreen {
            PlannerScreen(s = s0, vm = vm, onRecord = { _, _ -> })
        }
        compose.onNodeWithText("Dia").performClick()
        compose.waitForIdle()
        shot("08-agenda-diaria")

        // 09. Agenda mensal
        renderScreen {
            PlannerScreen(s = s0, vm = vm, onRecord = { _, _ -> })
        }
        compose.onNodeWithText("Mês").performClick()
        compose.waitForIdle()
        shot("09-agenda-mensal")

        // 10. Caminhos
        renderScreen {
            PathsScreen(s = s0, vm = vm, onOpenPath = {}, onNewPath = {}, onEditPath = {})
        }
        compose.onNodeWithText("Arquitetura de Aetheris").assertExists()
        shot("10-caminhos")

        // 11. Gantt
        renderScreen {
            Column(Modifier.fillMaxSize().background(LocalNorthborn.current.canvas).verticalScroll(rememberScrollState()).padding(18.dp)) {
                Path30AdvancedSection(path = path, s = s0, vm = vm, initialMode = 0)
            }
        }
        compose.onNodeWithTag("gantt_chart_canvas").assertExists()
        shot("11-gantt")

        // 12. Kanban
        renderScreen {
            Column(Modifier.fillMaxSize().background(LocalNorthborn.current.canvas).verticalScroll(rememberScrollState()).padding(18.dp)) {
                Path30AdvancedSection(path = path, s = s0, vm = vm, initialMode = 1)
            }
        }
        compose.onNodeWithTag("kanban_board").assertExists()
        shot("12-kanban")

        // Inicia foco ativo para 13 e 14
        vm.run {
            app.repository.startTimeForgeFocus(
                mode = "pomodoro",
                workMinutes = 25,
                shortBreakMinutes = 5,
                longBreakMinutes = 15,
                totalCycles = 4,
                autoAdvance = false,
                blockId = "blk-1",
                targetLabel = "Foco · Núcleo Estratégico"
            )
        }
        wait { app.repository.state.value!!.focus != null }
        val sFocus = app.repository.state.value!!

        // 13. Forja do Tempo
        renderScreen {
            TimeForgeScreen(s = sFocus, vm = vm, initialImmersive = false)
        }
        shot("13-forja-do-tempo")

        // 14. Modo Imersivo Nórdico
        renderScreen {
            TimeForgeScreen(s = sFocus, vm = vm, initialImmersive = true)
        }
        compose.onNodeWithTag("immersive_focus_view").assertExists()
        shot("14-modo-imersivo")

        vm.run { app.repository.endFocus(register = false) }
        wait { app.repository.state.value!!.focus == null }

        // 15. Conselho dos Nove
        renderScreen {
            CouncilOfNineScreen(s = s0, vm = vm, initialTab = 0)
        }
        shot("15-conselho-dos-nove")

        // 16. Biblioteca de Mímir
        renderScreen {
            MimirLibraryScreen(s = s0, vm = vm, onOpenNotebook = {}, onOpenPage = {}, onStartReview = {}, onOpenCodex = {})
        }
        compose.onNodeWithText("Caderno de Estratégia").assertExists()
        shot("16-biblioteca-mimir")

        // 17. Flashcards SM-2
        renderScreen {
            FlashcardReviewScreen(notebookId = "", s = s0, vm = vm, onDone = {})
        }
        compose.onNodeWithTag("flashcard_stage").assertExists()
        shot("17-flashcards")

        // 18. Arsenal
        renderScreen {
            Equipment(s = s0, vm = vm, store = false, onOpenForge = {}, onOpenHeraldry = {})
        }
        shot("18-arsenal")

        // 19. Forja de Darian
        renderScreen {
            ForgeScreen(s = s0, vm = vm)
        }
        shot("19-forja-darian")

        // 20. Atlas de Aetheris
        renderScreen {
            WorldScreen(s = s0, vm = vm)
        }
        compose.onNodeWithTag("atlas_legend").assertExists()
        shot("20-atlas")

        // 21. Exploração (Floresta da Persistência — região explorável)
        renderScreen {
            RegionScreen(index = 1, s = s0, onLocation = {})
        }
        shot("21-exploracao")

        // 22. Codex Rúnico
        renderScreen {
            RunicCodexScreen(onOpenHeraldry = {}, onBack = {}, initialRuneName = "Ansuz", vm = vm)
        }
        shot("22-codex-runico")

        // 23. Configurações
        renderScreen {
            SettingsScreen(s = s0, vm = vm, relaxed = false)
        }
        compose.onNodeWithText("ASCEND · versão 1.7.5.9.3", ignoreCase = true).assertExists()
        shot("23-configuracoes")

        // 24. Onboarding
        renderScreen {
            Onboarding(onRestore = {}, onCreate = { _, _ -> })
        }
        compose.onNodeWithText("Aetheris espera por você.").assertExists()
        shot("24-onboarding")

        // 25. Tema claro (Jornada + Navegação inferior em papel linho documental!)
        renderScreen(dark = false) {
            Scaffold(
                containerColor = LocalNorthborn.current.canvas,
                bottomBar = { AetherisNavigation("journey") {} }
            ) { pad ->
                Box(Modifier.padding(pad)) {
                    Journey(s0, { _, _ -> }, {}, {}, {}, {}, vm, relaxed = false)
                }
            }
        }
        shot("25-tema-claro")

        // 26. Tema escuro (Mesa de Odin + Estratégias em carvão mineral!)
        renderScreen(dark = true) {
            OdinCouncilScreen(s = s0, vm = vm, initialTab = 0)
        }
        shot("26-tema-escuro")

        // 27. Modo minimalista
        renderScreen(dark = true, minimal = true) {
            Scaffold(
                containerColor = LocalNorthborn.current.canvas,
                bottomBar = { AetherisNavigation("journey") {} }
            ) { pad ->
                Box(Modifier.padding(pad)) {
                    Journey(s0, { _, _ -> }, {}, {}, {}, {}, vm, relaxed = false)
                }
            }
        }
        shot("27-modo-minimalista")

        // 28. Tela estreita 320dp
        renderScreen(dark = true, widthDp = 320) {
            Scaffold(
                containerColor = LocalNorthborn.current.canvas,
                bottomBar = { AetherisNavigation("journey") {} }
            ) { pad ->
                Box(Modifier.padding(pad)) {
                    Journey(s0, { _, _ -> }, {}, {}, {}, {}, vm, relaxed = false)
                }
            }
        }
        shot("28-tela-320dp")

        // 29. Fonte ampliada 130%
        renderScreen(dark = true, widthDp = 320, fontScale = 1.3f) {
            Scaffold(
                containerColor = LocalNorthborn.current.canvas,
                bottomBar = { AetherisNavigation("journey") {} }
            ) { pad ->
                Box(Modifier.padding(pad)) {
                    Journey(s0, { _, _ -> }, {}, {}, {}, {}, vm, relaxed = false)
                }
            }
        }
        compose.onNodeWithText("Forja do Tempo").assertIsDisplayed()
        compose.onNodeWithText("Planejador").assertIsDisplayed()
        shot("29-fonte-130")

        // 30. Diálogos e formulários (showcase real de formulário + controles NORTHBORN 4.0)
        renderScreen {
            Column(Modifier.fillMaxSize().background(LocalNorthborn.current.canvas)) {
                NorthbornTopBar("Novo hábito", onBack = {}, trailing = {
                    NorthbornButton("Salvar", {}, role = NorthbornButtonRole.Primary)
                })
                Page {
                    FormField("Leitura da noite", {}, "Nome do hábito")
                    ChipRow(listOf("Simples", "Quantidade", "Tempo", "Repetições"), 2, {})
                    Toggle("Recompensa proporcional ao progresso", true, {})
                    NorthbornDivider(NorthbornDividerStyle.DiamondKnot)
                    NorthbornSurface(role = NorthbornSurfaceRole.Ceremonial) {
                        NorthbornEditorialHeader("Confirmação arquitetônica", kicker = "Diálogo 4.0")
                        Text("As mudanças deste formulário serão aplicadas apenas aos novos registros, preservando o histórico.", color = LocalNorthborn.current.inkSecondary)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NorthbornButton("Cancelar", {}, role = NorthbornButtonRole.Secondary, modifier = Modifier.weight(1f))
                            NorthbornButton("Confirmar", {}, role = NorthbornButtonRole.Confirm, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        shot("30-dialogos-formularios")

        // The 4.1 sample uses the same seeded content as the previous tour.
        renderScreen(dark = true, widthDp = 320, fontScale = 1.3f) {
            OdinCouncilScreen(s0, vm)
        }
        compose.onNodeWithText("Mesa de Odin").assertIsDisplayed()
        compose.onNodeWithText("Gerar plano").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithText("Estratégia da prévia").assertExists()
        compose.onNodeWithText("14 dias").assertIsDisplayed()
        shot("31-odin-320-fonte-130-proposta")
        renderScreen(dark = true, widthDp = 320, fontScale = 1.3f) {
            OdinCouncilScreen(s0, vm)
        }
        shot("32-odin-320-fonte-130")
        renderScreen(dark = false, widthDp = 360) {
            MimirLibraryScreen(s0, vm, {}, {}, {}, {})
        }
        compose.onNodeWithText("A Biblioteca de Mímir").assertIsDisplayed()
        shot("33-mimir-claro-360")
        renderScreen(dark = true, minimal = true, widthDp = 320, fontScale = 1.3f) {
            MimirLibraryScreen(s0, vm, {}, {}, {}, {})
        }
        compose.onNodeWithText("Cadernos").assertIsDisplayed()
        shot("34-mimir-minimalista-320")
        renderScreen(dark = false, widthDp = 360, fontScale = 1.3f) {
            PageDetailScreen("pg-1", s0, vm, {}, {})
        }
        compose.onNodeWithText("Princípios NORTHBORN").assertIsDisplayed()
        shot("35-mimir-leitura")
        compose.onNodeWithText("+ Referência").performScrollTo().assertIsDisplayed()
        shot("39-mimir-referencias-360-fonte-130")
        renderScreen(dark = true, widthDp = 360) {
            FlashcardReviewScreen("", s0, vm, {})
        }
        compose.onNodeWithTag("reveal_answer").performScrollTo().performClick()
        compose.onNodeWithText("Bom").assertExists()
        shot("36-mimir-resposta")
        renderScreen(dark = false, minimal = true, widthDp = 360) {
            OdinCouncilScreen(s0, vm)
        }
        shot("37-odin-minimalista-claro-360")
        renderScreen(dark = true, widthDp = 360) {
            Scaffold(bottomBar = { AetherisNavigation("journey") {} }) { pad ->
                Box(Modifier.padding(pad)) {
                    Journey(s0, { _, _ -> }, {}, {}, {}, {}, vm, relaxed = false)
                }
            }
        }
        val allDestinations = compose.onNodeWithContentDescription("Todos os destinos").performScrollTo()
        shot("38-jornada-360")
        allDestinations.performClick()
        compose.onNodeWithText("Conselho 9").assertIsDisplayed()

        renderScreen(dark = true, widthDp = 320, fontScale = 1.3f) {
            var route by remember { mutableStateOf("mimir") }
            Scaffold(bottomBar = { AetherisNavigation(route) { route = it } }) { pad ->
                Box(Modifier.padding(pad)) { MimirLibraryScreen(s0, vm, {}, {}, {}, {}) }
            }
        }
        compose.onNodeWithTag("nav:world").assertIsSelected()
        shot("40-navegacao-320")
        compose.onNodeWithTag("nav:missions").performClick().assertIsSelected()

        renderScreen {
            val symbols = listOf(
                "Jornada" to Engraving.Ship, "Hábitos" to Engraving.Tally, "Missões" to Engraving.Banner,
                "Mundo" to Engraving.Compass, "Herói" to Engraving.Shield, "Odin" to Engraving.Council,
                "Forja" to Engraving.Forge, "Planejador" to Engraving.Calendar, "Mímir" to Engraving.Folio,
                "Caminhos" to Engraving.Cairns, "Foco" to Engraving.Focus, "Análises" to Engraving.Analysis,
                "Criar" to Engraving.Add, "Configurações" to Engraving.Settings,
                "Voltar" to Engraving.Back, "Confirmar" to Engraving.Check
            )
            EngravedPage {
                Text("Iconografia de Aetheris")
                symbols.chunked(3).forEach { group ->
                    Row(Modifier.fillMaxWidth()) {
                        group.forEach { (label, engraving) ->
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                EngravedIcon(engraving, Gold, size = 32.dp, raised = true)
                                Text(label)
                            }
                        }
                        repeat(3 - group.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
                listOf("Lã tecida" to EngravedRail.Voyage, "Madeira entalhada" to EngravedRail.Council,
                    "Metal estampado" to EngravedRail.Folio).forEach { (label, rail) ->
                    Text(label)
                    EngravedDivider(rail)
                }
            }
        }
        shot("41-icones-e-divisores")

        renderScreen(dark = true, widthDp = 360, reducedMotion = false) {
            FlashcardReviewScreen("", s0, vm, {})
        }
        val reveal = compose.onNodeWithTag("reveal_answer").performScrollTo()
        compose.mainClock.autoAdvance = false
        try {
            reveal.performClick()
            compose.mainClock.advanceTimeBy(48)
            compose.onNodeWithTag("mimir_answer").assertIsDisplayed()
            shot("42-mimir-movimento-inicio")
            compose.mainClock.advanceTimeBy(110)
            shot("43-mimir-movimento-meio")
            compose.mainClock.advanceTimeBy(300)
            shot("44-mimir-movimento-fim")
        } finally {
            compose.mainClock.autoAdvance = true
        }
        val firstMotionFrame = File("build/reports/screenshots/1.7.5.9.3/42-mimir-movimento-inicio.png")
        val lastMotionFrame = File("build/reports/screenshots/1.7.5.9.3/44-mimir-movimento-fim.png")
        assertTrue("A revelação deve produzir quadros distintos",
            !firstMotionFrame.readBytes().contentEquals(lastMotionFrame.readBytes()))

        renderScreen { MissionsScreen(s0, vm, bosses = true) }
        compose.onNodeWithText("Contratos da jornada").assertIsDisplayed()
        shot("45-missoes")
        compose.onNodeWithText("Sagas").performScrollTo().performClick()
        compose.onNodeWithText("Sagas Pessoais").performScrollTo().assertIsDisplayed()
        shot("46-sagas")

        renderScreen { WeeklyScreen(s0) }
        compose.onNodeWithText("Crônica da Semana").assertIsDisplayed()
        shot("47-cronica-semanal")
        renderScreen { MemoryHall(s0) }
        shot("48-memorias")
        val diary = Journal(id = "northborn-review", title = "À margem do fiorde", text = "A chuva passou. Voltei ao caderno depois do estudo.")
        renderScreen(dark = true, minimal = true, widthDp = 320, fontScale = 1.3f) {
            JournalScreen(s0.copy(journal = listOf(diary)), vm)
        }
        compose.onNodeWithText(diary.text).performScrollTo().assertIsDisplayed()
        shot("49-diario-minimalista-320")

        renderScreen(dark = false, widthDp = 320, fontScale = 1.3f) {
            PlannerScreen(s0, vm, onRecord = { _, _ -> })
        }
        compose.onNodeWithText("Planejador Semanal").assertIsDisplayed()
        compose.onNodeWithText("Dia").performClick()
        compose.onNodeWithText("Pendências (", substring = true).assertIsDisplayed()
        shot("50-planejador-claro-320")
        renderScreen { AnalysisScreen(s0) }
        shot("51-analises")
        renderScreen { FocusHistoryScreen(s0) }
        shot("52-historico-de-foco")
        renderScreen { GrimoireScreen(s0, vm, onAdd = {}) }
        shot("53-grimorio")
        renderScreen { InboxScreen(s0, vm) }
        shot("54-caixa-de-entrada")
        renderScreen { PendingCenterScreen(s0, vm, onOpenPath = {}) }
        shot("55-pendencias")
        renderScreen { HeraldryEditorScreen(s0, vm, onOpenCodex = {}, onBack = {}) }
        shot("56-heraldica")

        val expected = listOf(
            "01-jornada", "02-navegacao-inferior", "03-registro-habito", "04-lista-habitos", "05-perfil-heroi",
            "06-mesa-de-odin", "07-propostas-odin", "08-agenda-diaria", "09-agenda-mensal", "10-caminhos",
            "11-gantt", "12-kanban", "13-forja-do-tempo", "14-modo-imersivo", "15-conselho-dos-nove",
            "16-biblioteca-mimir", "17-flashcards", "18-arsenal", "19-forja-darian", "20-atlas",
            "21-exploracao", "22-codex-runico", "23-configuracoes", "24-onboarding", "25-tema-claro",
            "26-tema-escuro", "27-modo-minimalista", "28-tela-320dp", "29-fonte-130", "30-dialogos-formularios",
            "40-navegacao-320", "41-icones-e-divisores", "45-missoes", "46-sagas", "47-cronica-semanal",
            "48-memorias", "49-diario-minimalista-320", "50-planejador-claro-320", "51-analises",
            "52-historico-de-foco", "53-grimorio", "54-caixa-de-entrada", "55-pendencias", "56-heraldica"
        )
        expected.forEach { name ->
            val f = File("build/reports/screenshots/1.7.5.9.3/$name.png")
            assertTrue("Captura esperada ausente ou vazia: ${f.path}", f.exists() && f.length() > 1000L)
        }
    }
}
