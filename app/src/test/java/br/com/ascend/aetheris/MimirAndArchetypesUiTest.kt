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
class MimirAndArchetypesUiTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()
    private val app get() = RuntimeEnvironment.getApplication() as AscendApp
    private val vm get() = ViewModelProvider(compose.activity)[GameViewModel::class.java]

    @Before
    fun setup() {
        runBlocking { app.preferences.edit { it.clear() } }
        wait { app.repository.state.value != null }
        vm.run {
            app.repository.create(Player(name = "Astrid", heroClass = 1), emptyList())
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
            val view = compose.activity.window.decorView
            val bitmap = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(bitmap))
            val file = File("build/reports/screenshots/$name.png")
            file.parentFile!!.mkdirs()
            file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    private fun seedMimirAndArchetypeWorkspace() {
        val d = today()
        vm.run {
            // Advance hero to level 6 so both specializations and class skills are unlockable
            for (i in 0 until 12) {
                app.database.dao().reward(
                    Reward(
                        id = "seed-xp-$i",
                        date = d,
                        label = "Estudo registrado",
                        xp = 180,
                        coins = 5,
                        attribute = 2,
                        points = 8
                    )
                )
            }
            app.repository.refresh()
            val path = PersonalPath(
                id = "p-mimir",
                name = "Arquitetura de Sistemas",
                description = "Consolidar fundamentos de engenharia e escrita técnica.",
                category = "Estudo",
                priority = 2,
                state = 1,
                attribute = 2
            )
            app.repository.savePath(path)

            val nb1 = KnowledgeNotebook(
                id = "nb-edd",
                name = "Runas e Epistemologia",
                description = "Notas de estudo profundo, sínteses de leitura e referências.",
                category = "Estudo",
                cover = 0,
                pathId = path.id
            )
            val nb2 = KnowledgeNotebook(
                id = "nb-arch",
                name = "Códice de Engenharia",
                description = "Padrões de persistência offline, Room e composição visual.",
                category = "Projeto",
                cover = 5,
                pathId = path.id
            )
            app.repository.saveNotebook(nb1)
            app.repository.saveNotebook(nb2)

            val pg1 = KnowledgePage(
                id = "pg-memory",
                notebookId = nb1.id,
                title = "Memória Espaçada e Retenção",
                content = "# Princípio da Recuperação Ativa\n\nA consolidação ocorre quando o **esforço de lembrar** precede a revisão.\n\n## Regra prática\n- Revise em intervalos crescentes\n- Use perguntas atômicas em `SM-2`\n\n> A sabedoria de Mímir não se acumula por pressa, mas por retorno deliberado.",
                tags = "memória, sm2, estudo",
                favorite = true,
                pathId = path.id
            )
            val pg2 = KnowledgePage(
                id = "pg-futhark",
                notebookId = nb1.id,
                title = "Notas sobre Síntese Técnica",
                content = "## Estrutura de Notas\n\nCada nota deve responder a uma ideia central e apontar para fontes verificáveis.",
                tags = "escrita, síntese"
            )
            app.repository.savePage(pg1)
            app.repository.savePage(pg2)
            app.repository.linkPages(pg1.id, pg2.id)
            app.repository.linkPages(pg2.id, pg1.id)
            app.repository.saveReference(
                KnowledgeReference(
                    id = "ref-ebbinghaus",
                    pageId = pg1.id,
                    author = "Hermann Ebbinghaus",
                    title = "Über das Gedächtnis",
                    year = "1885",
                    source = "Monografia Experimental",
                    location = "Cap. IV"
                )
            )
            app.repository.saveFlashcard(
                Flashcard(
                    id = "fc-1",
                    notebookId = nb1.id,
                    pageId = pg1.id,
                    question = "Qual é o objetivo central da repetição espaçada (SM-2)?",
                    answer = "Revisar cada conceito pouco antes do esquecimento, ajustando o intervalo conforme o esforço real de lembrança.",
                    tags = "memória, sm2",
                    nextReviewDate = d
                )
            )
            app.repository.saveFlashcard(
                Flashcard(
                    id = "fc-2",
                    notebookId = nb1.id,
                    pageId = pg2.id,
                    question = "Por que manter ligações explícitas e backlinks entre páginas?",
                    answer = "Para revelar relações entre conceitos sem depender apenas de pastas rígidas.",
                    tags = "escrita",
                    nextReviewDate = LocalDate.now().minusDays(1).toString()
                )
            )
        }
        wait {
            val st = app.repository.state.value!!
            st.level >= 6 && st.notebooks.size == 2 && st.knowledgePages.size == 2 && st.flashcards.size == 2
        }
        // Dismiss any celebration dialog (e.g. Level 5 achievement / Primeiro Tomo) triggered by seeding
        repeat(4) {
            compose.waitForIdle()
            val cont = compose.onAllNodesWithText("Continuar a jornada", ignoreCase = true)
            if (cont.fetchSemanticsNodes().isNotEmpty()) {
                cont.onFirst().performClick()
                compose.waitForIdle()
            }
        }
    }

    @Test
    fun mimirLibraryNotebookPageAndSpacedReviewFlow() {
        seedMimirAndArchetypeWorkspace()

        // Discrete Runas da Memória entry appears in Rumos do Dia when cards are due
        compose.onNodeWithText("Runas da Memória", substring = true).performScrollTo().assertExists()

        // Navigate via Mundo -> Crônicas -> Abrir Biblioteca de Mímir
        compose.onNodeWithText("Mundo").performClick()
        compose.onNodeWithText("Crônicas").performClick()
        compose.onNodeWithText("Abrir Biblioteca de Mímir").performScrollTo().performClick()
        compose.waitForIdle()

        compose.onNodeWithText("Runas e Epistemologia").assertExists()
        compose.onNodeWithText("Códice de Engenharia").assertExists()
        shot("mimir-biblioteca-171")

        // Open "Runas e Epistemologia" notebook (sorted after "Códice de Engenharia" alphabetically on same date)
        compose.onNodeWithText("Runas e Epistemologia").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Memória Espaçada e Retenção").assertExists()
        shot("mimir-caderno-171")

        // Open "Memória Espaçada e Retenção" page -> Visualizar Markdown + links + backlinks + reference
        compose.onNodeWithText("Memória Espaçada e Retenção").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Princípio da Recuperação Ativa").assertExists()
        shot("mimir-editor-171")

        // Toggle Editar tab and verify Save action exists
        compose.onAllNodesWithText("Editar").onFirst().performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Salvar página").performScrollTo().assertExists()

        // Go back to notebook, then start Runas da Memória review
        compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.waitForIdle()
        compose.onNodeWithText("Revisar (2)").performScrollTo().performClick()
        compose.waitForIdle()

        // Reveal answer & capture review screenshot
        compose.onNodeWithTag("reveal_answer").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Bom").assertExists()
        compose.onNodeWithText("Fácil").assertExists()
        shot("mimir-revisao-171")

        // Rate first card "Bom" and confirm persistence without extra XP/coins
        val coinsBefore = app.repository.state.value!!.coins
        val xpBefore = app.repository.state.value!!.xp
        compose.onNodeWithText("Bom").performScrollTo().performClick()
        wait { app.repository.state.value!!.flashcardReviews.size == 1 }
        assertEquals(coinsBefore, app.repository.state.value!!.coins)
        assertEquals(xpBefore, app.repository.state.value!!.xp)
    }

    @Test
    fun heroSpecializationsAndExpandedSkillTreeFlow() {
        seedMimirAndArchetypeWorkspace()

        // Navigate to Herói -> Perfil -> ArchetypeAscensionPanel
        compose.onNodeWithText("Herói").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Escolher Erudito").performScrollTo().performClick()
        wait { app.repository.state.value!!.player?.specialization == "erudito" }

        compose.onNodeWithText("Ascensão do Arquétipo · Mago", ignoreCase = true).performScrollTo()
        compose.onNodeWithText("Erudito · Leitor de Mímir").assertExists()
        shot("heroi-especializacoes-171")

        // Switch to Habilidades tab -> unlock mage_mimir skill -> capture screenshot
        compose.onNodeWithText("Habilidades").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Olhar de Mímir").assertExists()
        compose.onAllNodesWithText("Despertar habilidade").onFirst().performScrollTo().performClick()
        wait { app.repository.state.value!!.assets.any { it.id == "skill:mage_mimir" } }

        compose.onNodeWithText("Despertada na constelação").assertExists()
        shot("heroi-habilidades-171")

        // Verify original universal skills still exist below
        compose.onNodeWithText("Constelações Universais · Relíquias", ignoreCase = true).performScrollTo().assertExists()
    }

    @Test
    @Config(qualifiers = "w320dp-h740dp-mdpi")
    fun mimirNarrow320DpLightThemeLargeTextAndMinimalModeStayReadable() {
        seedMimirAndArchetypeWorkspace()
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

        compose.onNodeWithText("Mundo").performClick()
        compose.onNodeWithText("Crônicas").performClick()
        compose.onNodeWithText("Abrir Biblioteca de Mímir").performScrollTo().performClick()
        compose.waitForIdle()

        compose.onNodeWithText("Runas e Epistemologia").performScrollTo().assertIsDisplayed()
        shot("mimir-320-171")
    }
}
