package br.com.ascend.aetheris

import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
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
class ProductivityUiTest{
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as AscendApp
    private val vm get()=ViewModelProvider(compose.activity)[GameViewModel::class.java]
    @Before fun setup(){runBlocking{app.preferences.edit{it.clear()}};wait{app.repository.state.value!=null};vm.run{app.repository.create(Player(name="Sigrid"),emptyList());vm.setting("reducedMotion",true)};wait{app.repository.state.value!!.player!=null&&vm.reducedMotion.value}}
    @After fun close(){compose.activityRule.scenario.close();app.database.close()}
    private fun wait(test:()->Boolean){compose.waitUntil(15000){Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();test()};compose.waitForIdle();val dismiss=compose.onAllNodesWithContentDescription("Dismiss");if(dismiss.fetchSemanticsNodes().isNotEmpty())dismiss.onFirst().performClick()}
    private fun shot(name:String){compose.waitForIdle();compose.runOnIdle{val dialog=org.robolectric.shadows.ShadowDialog.getLatestDialog();val view=if(dialog?.isShowing==true)dialog.window!!.decorView else compose.activity.window.decorView;val bitmap=android.graphics.Bitmap.createBitmap(view.width,view.height,android.graphics.Bitmap.Config.ARGB_8888);view.draw(android.graphics.Canvas(bitmap));val file=java.io.File("build/reports/screenshots/$name.png");file.parentFile!!.mkdirs();file.outputStream().use{bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}}

    private fun back(){compose.runOnIdle{compose.activity.onBackPressedDispatcher.onBackPressed()};compose.waitForIdle()}
    private fun seeds(){vm.run{val hs=listOf(Habit(id="walk",name="Caminhar",xp=20),Habit(id="learn",name="Estudo sereno",type=2,target=60,xp=60,proportional=true),Habit(id="read",name="Leitura diária",type=1,target=20,unit="páginas",xp=30,proportional=true));hs.forEach{app.repository.habit(it)};app.repository.progress(hs[0],today(),1);app.repository.progress(hs[1],today(),30);app.repository.progress(hs[2],today(),6)};wait{app.repository.state.value!!.records.size==3}}
    @Test fun plannerExceptionsFlowIntoJourneyWidgetAndAnalyses(){
        seeds();shot("jornada-160")
        compose.onNodeWithText("Planejador").performScrollTo().performClick();compose.onNodeWithTag("planner_list").performScrollToIndex(0);shot("planejador-160")
        compose.onNodeWithTag("planner_list").performScrollToNode(hasText("Ajustar Caminhar"));compose.onNodeWithText("Ajustar Caminhar").performClick();shot("planejador-ajuste-160");compose.onNodeWithText("Dispensar nesta data").performClick();wait{app.repository.state.value!!.plans.size==1}
        assertEquals(2,widgetDay(app.repository.state.value!!).due);assertEquals(59L,app.repository.state.value!!.xp)
        compose.onNodeWithTag("planner_list").performScrollToIndex(0);shot("planejador-dia-160");back()
        compose.onNodeWithText("Análises").performScrollTo().performClick();compose.onNodeWithText("Central de Análises").assertExists();compose.waitUntil(10000){compose.onAllNodesWithText("Evolução de XP",ignoreCase=true).fetchSemanticsNodes().isNotEmpty()};shot("analises-160")
        compose.onNodeWithText("Leitura diária").performScrollTo().performClick();compose.onNodeWithText("Análise · Leitura diária").assertExists();compose.waitUntil(10000){compose.onAllNodesWithText("Evolução de XP",ignoreCase=true).fetchSemanticsNodes().isNotEmpty()};shot("analise-habito-160")
    }
    @Test fun actualFocusControlsCreateAPersistentSessionWithoutExtraXp(){
        seeds();compose.onNodeWithTag("journey_list").performScrollToNode(hasContentDescription("Iniciar foco em Estudo sereno"));compose.onNodeWithContentDescription("Iniciar foco em Estudo sereno").performScrollTo().performClick();compose.onNodeWithText("Duração em minutos").performTextReplacement("1");compose.onNodeWithText("Iniciar").performClick()
        runBlocking{kotlinx.coroutines.withTimeout(10000){while(app.repository.state.value!!.focus==null){Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();kotlinx.coroutines.delay(10)}};val f=app.repository.state.value!!.focus!!;app.database.dao().focus(f.copy(elapsed=60,startedAt=0));app.repository.refresh()}
        wait{app.repository.state.value!!.focus?.elapsed==60L}
        val sessionId=app.repository.state.value!!.focus!!.sessionId
        compose.activityRule.scenario.recreate();wait{app.repository.state.value!!.focus?.sessionId==sessionId}
        assertEquals(60L,app.repository.state.value!!.focus!!.seconds())
        compose.onNodeWithTag("journey_list").performScrollToNode(hasText("Registrar"));compose.onNodeWithText("Registrar").performScrollTo();shot("cronometro-165");compose.onNodeWithText("Registrar").performClick();wait{app.repository.state.value!!.focusSessions.size==1};assertEquals(60L,app.repository.state.value!!.xp)
        compose.onNodeWithTag("journey_list").performScrollToIndex(0);compose.onAllNodesWithText("Foco").onFirst().performScrollTo().performClick();compose.onNodeWithText("Histórico de Foco").assertExists();shot("foco-historico-160")
        compose.onNodeWithTag("focus_history_list").performScrollToNode(hasText("Detalhes da sessão"));compose.onNodeWithText("Detalhes da sessão").performClick();shot("foco-sessao-160");compose.onNodeWithText("Fechar").performClick();back()
        assertEquals(1,app.repository.state.value!!.focusSessions.size)
    }
    @Test @Config(qualifiers="w320dp-h740dp-mdpi") fun narrowPlannerAndAnalyticsUseTheRealLightThemeWithLargeText(){
        vm.theme("light");vm.setting("minimal",true);wait{vm.appearance.value=="light"&&vm.minimal.value}
        compose.runOnIdle{compose.activity.setContent{val density=LocalDensity.current;CompositionLocalProvider(LocalDensity provides Density(density.density,1.3f)){Ascend()}}};compose.waitForIdle()
        compose.onNodeWithText("Planejador").performScrollTo().performClick();compose.onNodeWithContentDescription("Próxima semana").performScrollTo().assertIsDisplayed().performClick();shot("planejador-320-160")
        compose.onNodeWithTag("planner_list").performScrollToNode(hasText("Nenhuma atividade prevista. Você pode incluir um hábito ativo ou reservar este dia para descanso."));back()
        compose.onNodeWithText("Análises").performScrollTo().performClick();compose.onNodeWithText("Central de Análises").assertExists();compose.waitUntil(10000){compose.onAllNodesWithText("Evolução de XP",ignoreCase=true).fetchSemanticsNodes().isNotEmpty()};shot("analises-320-160");compose.onNodeWithText("30 dias").performScrollTo().performClick();back()
        compose.onNodeWithText("Foco").performScrollTo().performClick();compose.onNodeWithTag("focus_history_list").performScrollToNode(hasText("Nenhuma sessão detalhada neste período. Seus registros antigos de minutos continuam nas análises de hábitos."));shot("foco-vazio-320-160")
    }
}
