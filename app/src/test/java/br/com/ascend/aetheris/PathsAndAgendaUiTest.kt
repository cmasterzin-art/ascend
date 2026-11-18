package br.com.ascend.aetheris

import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
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
import org.robolectric.shadows.ShadowDialog
import java.io.File
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = AscendApp::class, qualifiers = "w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PathsAndAgendaUiTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()
    private val app get() = RuntimeEnvironment.getApplication() as AscendApp
    private val vm get() = ViewModelProvider(compose.activity)[GameViewModel::class.java]

    @Before
    fun setup() {
        runBlocking { app.preferences.edit { it.clear() } }
        wait { app.repository.state.value != null }
        vm.run {
            app.repository.create(Player(name = "Sigrid"), emptyList())
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
        compose.runOnIdle {
            val dialog = ShadowDialog.getLatestDialog()
            val view = if (dialog?.isShowing == true) dialog.window!!.decorView else compose.activity.window.decorView
            val bitmap = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(bitmap))
            val file = File("build/reports/screenshots/$name.png")
            file.parentFile!!.mkdirs()
            file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    private fun back() {
        compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.waitForIdle()
    }

    private fun seedWorkspace() {
        val d = today()
        val tomorrow = LocalDate.now().plusDays(1).toString()
        val yesterday = LocalDate.now().minusDays(1).toString()
        vm.run {
            val h = Habit(id = "ritual-ui", name = "Caminhar ao amanhecer", attribute = 1, xp = 35, coins = 10)
            app.repository.habit(h)
            val p1 = PersonalPath(
                id = "p-forge",
                name = "Forjar o Guia Nórdico",
                description = "Consolidar arquitetura pessoal, rituais e metas trimestrais.",
                category = "Estudo",
                priority = 2,
                state = 1,
                deadline = LocalDate.now().plusDays(12).toString(),
                attribute = 2,
                criteria = "Todas as etapas revisadas e documentadas."
            )
            val p2 = PersonalPath(
                id = "p-done",
                name = "Inventário da Oficina",
                description = "Organizar ferramentas antigas.",
                category = "Casa",
                priority = 1,
                state = 3,
                completedDate = d
            )
            app.repository.savePath(p1)
            app.repository.savePath(p2)
            app.repository.saveStage(PathStage(id = "st-1", pathId = p1.id, name = "Etapa I · Estrutura", targetNote = "Base sólida e mapa mental", position = 0))
            app.repository.saveTask(
                PathTask(
                    id = "tk-1",
                    pathId = p1.id,
                    stageId = "st-1",
                    title = "Desenhar mapa do trimestre",
                    description = "Separar entregas semanais sem sobrecarga.",
                    priority = 3,
                    scheduledDate = d,
                    scheduledTime = "09:00",
                    deadline = tomorrow
                )
            )
            app.repository.saveTask(
                PathTask(
                    id = "tk-2",
                    pathId = p1.id,
                    stageId = "st-1",
                    title = "Revisar pendências antigas",
                    priority = 2,
                    deadline = yesterday
                )
            )
            app.repository.saveSubtask(PathSubtask(id = "sb-1", taskId = "tk-1", title = "Listar prioridades", position = 0))
            app.repository.saveSubtask(PathSubtask(id = "sb-2", taskId = "tk-1", title = "Definir blocos", position = 1))
            app.repository.linkHabitToPath(p1.id, h.id, true)
            app.repository.saveTimeBlock(
                TimeBlock(
                    id = "tb-1",
                    title = "Bloco de arquitetura",
                    date = d,
                    startTime = "09:00",
                    endTime = "10:30",
                    pathId = p1.id,
                    taskId = "tk-1"
                )
            )
            app.repository.saveTimeBlock(
                TimeBlock(
                    id = "tb-2",
                    title = "Revisão paralela",
                    date = d,
                    startTime = "10:00",
                    endTime = "11:00"
                )
            )
            app.repository.saveEvent(
                AgendaEvent(
                    id = "ev-1",
                    title = "Conselho semanal",
                    date = d,
                    startTime = "15:00",
                    endTime = "16:00",
                    kind = "Reunião",
                    description = "Alinhar prioridades da semana."
                )
            )
            app.repository.saveInboxItem(InboxItem(id = "in-1", title = "Catalogar referências visuais", notes = "Verificar paleta de carvão e bronze."))
        }
        wait { app.repository.state.value!!.paths.size == 2 && app.repository.state.value!!.inboxItems.size == 1 }
    }

    @Test
    fun pathsStagesTasksSubtasksFocusAndMemoryHallFlowWorks() {
        seedWorkspace()

        // Rumos do Dia na Jornada
        compose.onNodeWithTag("journey_list").performScrollToNode(hasTestTag("rumos_do_dia"))
        compose.onNodeWithText("Abrir rumos").performScrollTo().performClick()
        compose.onNodeWithText("Tarefas prioritárias", ignoreCase = true).assertExists()
        shot("tour-jornada-rumos-170")

        // Abrir Caminhos pelo atalho na Jornada
        compose.onNodeWithTag("journey_list").performScrollToIndex(0)
        compose.onAllNodesWithText("Caminhos").onFirst().performScrollTo().performClick()
        compose.onNodeWithTag("paths_list").performScrollToNode(hasText("Forjar o Guia Nórdico"))
        compose.onNodeWithText("Forjar o Guia Nórdico").assertExists()
        shot("tour-caminhos-170")

        // Abrir detalhes do caminho, alternar tarefa e verificar que não cria XP inventado
        val xpBefore = app.repository.state.value!!.xp
        compose.onNodeWithText("Abrir").performClick()
        compose.onNodeWithText("Etapa I · Estrutura").assertExists()
        shot("tour-caminho-detalhe-170")

        compose.onNodeWithTag("path_detail_list").performScrollToNode(hasContentDescription("Alternar Desenhar mapa do trimestre"))
        compose.onNodeWithContentDescription("Alternar Desenhar mapa do trimestre").performClick()
        wait { app.repository.state.value!!.pathTasks.first { it.id == "tk-1" }.completed }
        assertEquals(xpBefore, app.repository.state.value!!.xp)

        // Subtarefa + Foco em projeto/tarefa
        compose.onNodeWithTag("path_detail_list").performScrollToIndex(0)
        compose.onNodeWithText("Iniciar foco").performClick()
        compose.onNodeWithText("Duração em minutos").performTextReplacement("2")
        compose.onNodeWithText("Iniciar").performClick()
        wait { app.repository.state.value!!.focus != null }
        vm.run {
            val f = app.repository.state.value!!.focus!!
            app.database.dao().focus(f.copy(elapsed = 120, startedAt = 0))
            app.repository.refresh()
        }
        wait { app.repository.state.value!!.focus?.elapsed == 120L }
        compose.onNodeWithTag("journey_list").performScrollToNode(hasText("Registrar"))
        compose.onNodeWithText("Registrar").performClick()
        wait { app.repository.state.value!!.focusSessions.size == 1 }
        assertEquals(xpBefore, app.repository.state.value!!.xp)
        assertEquals("", app.repository.state.value!!.focusSessions.single().habitId)

        // Salão das Memórias inclui filtro Caminhos com projeto concluído
        compose.onNodeWithTag("nav:world").performClick()
        compose.onNodeWithText("Crônicas").performClick()
        compose.onNodeWithText("Salão das Memórias").performScrollTo().performClick()
        compose.onNodeWithText("Caminhos").performScrollTo().performClick()
        compose.onNodeWithText("Inventário da Oficina").assertExists()
        compose.onNodeWithText("Caminho concluído · Casa.", substring = true).assertExists()
    }

    @Test
    fun agendaModesInboxTriageAndPendingCenterWorkEndToEnd() {
        seedWorkspace()

        // Agenda 2.0: Semana (default), Dia, Mês
        compose.onNodeWithText("Planejador").performScrollTo().performClick()
        compose.onNodeWithTag("planner_list").performScrollToNode(hasText("Atenção: há blocos de tempo com horários sobrepostos nesta data."))
        compose.onNodeWithText("Atenção: há blocos de tempo com horários sobrepostos nesta data.").assertExists()
        compose.onNodeWithTag("planner_list").performScrollToIndex(0)
        shot("tour-agenda-semana-170")

        compose.onNodeWithText("Dia").performClick()
        shot("tour-agenda-dia-170")

        compose.onNodeWithText("Mês").performClick()
        shot("tour-agenda-mes-170")

        // Abrir Caixa de Entrada e organizar em tarefa
        compose.onNodeWithTag("planner_list").performScrollToIndex(0)
        compose.onNodeWithText("Entrada (1)").performClick()
        compose.onNodeWithText("Catalogar referências visuais").assertExists()
        shot("tour-entrada-170")

        compose.onNodeWithText("Organizar em tarefa").performClick()
        compose.onNodeWithText("Salvar").performClick()
        wait { app.repository.state.value!!.inboxItems.isEmpty() && app.repository.state.value!!.pathTasks.any { it.title == "Catalogar referências visuais" } }

        // Central de Pendências
        back()
        compose.onNodeWithTag("planner_list").performScrollToIndex(0)
        compose.onNodeWithText("Pendências (3)").performClick()
        compose.onNodeWithText("Central de Pendências").assertExists()
        shot("tour-pendencias-170")

        compose.onNodeWithText("Atrasadas").performClick()
        compose.onNodeWithText("Revisar pendências antigas").assertExists()
    }

    @Test
    @Config(qualifiers = "w320dp-h740dp-mdpi")
    fun narrow320DpLightThemeLargeTextAndMinimalModeStayReadable() {
        seedWorkspace()
        vm.theme("light")
        vm.setting("minimal", true)
        wait { vm.appearance.value == "light" && vm.minimal.value }
        compose.runOnIdle {
            compose.activity.setContent {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, 1.3f)) {
                    Ascend()
                }
            }
        }
        compose.waitForIdle()

        compose.onNodeWithText("Caminhos").performScrollTo().performClick()
        compose.onNodeWithTag("paths_list").performScrollToNode(hasText("Forjar o Guia Nórdico"))
        compose.onNodeWithText("Forjar o Guia Nórdico").assertIsDisplayed()
        shot("caminhos-320-170")

        back()
        compose.onNodeWithText("Planejador").performScrollTo().performClick()
        compose.onNodeWithText("Planejador Semanal").assertIsDisplayed()
        shot("agenda-320-170")

        compose.onNodeWithText("Pendências (2)").performScrollTo().performClick()
        compose.onNodeWithText("Central de Pendências").assertIsDisplayed()
        shot("pendencias-320-170")
    }
}
