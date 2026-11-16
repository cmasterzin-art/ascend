package br.com.ascend.aetheris

import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=AscendApp::class,qualifiers="w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class GrimoireUiTest{
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as AscendApp
    private val vm get()=ViewModelProvider(compose.activity)[GameViewModel::class.java]
    @Before fun setup(){runBlocking{app.preferences.edit{it.clear()}};wait{app.repository.state.value!=null};vm.run{app.repository.create(Player(name="Sigrid"),emptyList());vm.setting("reducedMotion",true)};wait{app.repository.state.value!!.player!=null&&vm.reducedMotion.value}}
    @After fun close(){compose.activityRule.scenario.close();app.database.close()}
    private fun wait(test:()->Boolean){compose.waitUntil(15000){Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();test()};compose.waitForIdle();val dismiss=compose.onAllNodesWithContentDescription("Dismiss");if(dismiss.fetchSemanticsNodes().isNotEmpty())dismiss.onFirst().performClick()}
    private fun open(){compose.onNodeWithText("Hábitos").performClick();compose.onNodeWithText("Explorar hábitos").performScrollTo().performClick();compose.onNodeWithText("Grimório dos Hábitos").assertExists()}
    private fun shot(name:String){compose.waitForIdle();compose.runOnIdle{val dialog=org.robolectric.shadows.ShadowDialog.getLatestDialog();val view=if(dialog?.isShowing==true)dialog.window!!.decorView else compose.activity.window.decorView;val bitmap=android.graphics.Bitmap.createBitmap(view.width,view.height,android.graphics.Bitmap.Config.ARGB_8888);view.draw(android.graphics.Canvas(bitmap));val file=java.io.File("build/reports/screenshots/$name.png");file.parentFile!!.mkdirs();file.outputStream().use{bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}}
    @Test fun personalizeTemplateCompleteEditAndKeepOriginalReward(){
        open();compose.onNodeWithText("Intelecto").performScrollTo().performClick();shot("grimoire-catalogo")
        compose.onNodeWithContentDescription("Exibir nomes narrativos").performScrollTo().performClick()
        compose.onNodeWithText("Buscar nome ou palavra-chave").performScrollTo().performTextInput("tomos justica")
        compose.onNodeWithTag("catalog_list").performScrollToNode(hasText("Adicionar"));shot("grimoire-busca")
        compose.onNodeWithText("Adicionar").performClick()
        compose.onNodeWithText("Nome do hábito").performScrollTo().performTextReplacement("Meu estudo de Direito")
        compose.onNodeWithText("XP ao atingir a meta").performScrollTo().performTextReplacement("73")
        compose.onNodeWithText("Meta em minutos").performScrollTo().performTextReplacement("15")
        compose.onNodeWithContentDescription("Domingo").performScrollTo().performClick();shot("grimoire-personalizar")
        compose.onNodeWithText("Salvar").performClick();wait{app.repository.state.value!!.habits.size==1}
        assertEquals("1,2,3,4,5,6",app.repository.state.value!!.habits.single().days)
        compose.onNodeWithText("Jornada").performClick();compose.onNodeWithText("Os Tomos da Justiça").assertExists()
        compose.onNodeWithContentDescription("Registrar progresso").performScrollTo().performClick();compose.onNodeWithText("Atingir meta").performScrollTo().performClick();compose.onNodeWithText("Salvar").performClick();wait{app.repository.state.value!!.xp==73L}
        compose.onNodeWithText("Meu estudo de Direito").performScrollTo().performClick();compose.onNodeWithText("Editar",useUnmergedTree=true).performClick()
        compose.onNodeWithText("XP ao atingir a meta").performScrollTo().performTextReplacement("95");compose.onNodeWithText("Salvar").performClick();wait{app.repository.state.value!!.habits.single().xp==95}
        assertEquals(73,app.repository.state.value!!.records.single().xp)
        runBlocking{val fresh=GameRepository(app.database);fresh.refresh();assertEquals("study_law",fresh.state.value!!.habits.single().templateId);assertEquals(95,fresh.state.value!!.habits.single().xp)}
    }
    @Test fun suggestionsCanBeSelectedDismissedAndRestored(){
        open();compose.onNodeWithTag("catalog_list").performScrollToNode(hasText("Escolher interesses"));compose.onNodeWithText("Escolher interesses").performClick()
        compose.onNodeWithTag("catalog_list").performScrollToNode(hasContentDescription("Interesse: Intelecto"));compose.onNodeWithContentDescription("Interesse: Intelecto").performClick();wait{vm.catalogInterests.value==setOf("Intelecto")}
        compose.onNodeWithText("Escolher interesses").performScrollTo().performClick()
        compose.onNodeWithTag("catalog_list").performScrollToNode(hasTestTag("suggest:study_law"));shot("grimoire-sugestoes")
        compose.onNode(hasText("Dispensar sugestão") and hasAnyAncestor(hasTestTag("suggest:study_law"))).performScrollTo().performClick();wait{"study_law" in vm.dismissedSuggestions.value}
        assertTrue(app.repository.state.value!!.habits.isEmpty())
        compose.onNodeWithTag("catalog_list").performScrollToNode(hasText("Reexibir sugestões dispensadas"));compose.onNodeWithText("Reexibir sugestões dispensadas").performClick();wait{vm.dismissedSuggestions.value.isEmpty()}
    }
    @Test @Config(qualifiers="w320dp-h740dp-mdpi")
    fun routinePreviewAndPartialSelectionWorkOnNarrowScreen(){
        compose.runOnIdle{compose.activity.setContent{val density=LocalDensity.current;CompositionLocalProvider(LocalDensity provides Density(density.density,1.3f)){Ascend()}}};compose.waitForIdle()
        open();shot("grimoire-320-texto130")
        compose.onNodeWithText("Intelecto").performScrollTo().performClick()
        compose.onNodeWithTag("catalog_list").performScrollToNode(hasTestTag("template:presentation"))
        compose.onNode(hasText("Adicionar") and hasAnyAncestor(hasTestTag("template:presentation"))).performScrollTo().assertIsDisplayed();shot("grimoire-cartao-320")
        compose.onNodeWithTag("catalog_list").performScrollToIndex(0)
        compose.onNodeWithText("Modelos de rotina").performScrollTo().performClick()
        compose.onNodeWithTag("catalog_list").performScrollToNode(hasText("Examinar Ritual da Aurora"));compose.onNodeWithText("Examinar Ritual da Aurora").performClick()
        compose.onNodeWithContentDescription("Incluir Alongamento").performScrollTo().performClick();compose.onNodeWithContentDescription("Incluir Ler por lazer").performScrollTo().performClick();shot("rotina-selecao-320")
        compose.onNodeWithText("Personalizar Planejar o dia").performScrollTo().performClick();compose.onNodeWithText("XP ao atingir a meta").performScrollTo().performTextReplacement("44");compose.onNodeWithContentDescription("Lembrete local opcional").performScrollTo().performClick();compose.onNodeWithText("Salvar").performClick()
        compose.onNodeWithText("Adicionar seleção").performClick();wait{app.repository.state.value!!.routines.size==1}
        assertEquals(2,app.repository.state.value!!.habits.size);assertEquals(44,app.repository.state.value!!.habits.first{it.templateId=="plan_day"}.xp);assertTrue(app.repository.state.value!!.habits.first{it.templateId=="plan_day"}.reminder);assertEquals(0L,app.repository.state.value!!.xp)
    }
    @Test fun buildingStagesHaveDifferentPaintingsAndPersistentState(){
        vm.run{val h=Habit(name="Preparar a reconstrução",coins=500,xp=0);app.repository.habit(h);app.repository.progress(h,today(),1)};wait{app.repository.state.value!!.coins>=500}
        compose.onNodeWithText("Mundo").performClick();compose.onNodeWithText("Vila").performClick()
        compose.onNodeWithContentDescription("A Biblioteca: ruínas").performScrollTo();shot("biblioteca-ruinas")
        vm.run{app.repository.restore(0)};wait{app.repository.state.value!!.assets.any{it.id=="building:0"&&it.stage==1}}
        compose.onNodeWithContentDescription("A Biblioteca: em reconstrução").performScrollTo();shot("biblioteca-parcial")
        vm.run{app.repository.restore(0)};wait{app.repository.state.value!!.assets.any{it.id=="building:0"&&it.stage==2}}
        compose.onNodeWithContentDescription("A Biblioteca: restaurada").performScrollTo();shot("biblioteca-restaurada")
    }
}
