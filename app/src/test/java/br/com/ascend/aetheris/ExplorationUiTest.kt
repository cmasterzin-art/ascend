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
class ExplorationUiTest{
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as AscendApp
    private val vm get()=ViewModelProvider(compose.activity)[GameViewModel::class.java]
    @Before fun setup(){runBlocking{app.preferences.edit{it.clear()}};wait{app.repository.state.value!=null};vm.run{app.repository.create(Player(name="Sigrid"),emptyList());vm.setting("reducedMotion",true)};wait{app.repository.state.value!!.player!=null&&vm.reducedMotion.value}}
    @After fun close(){compose.activityRule.scenario.close();app.database.close()}
    private fun wait(test:()->Boolean){compose.waitUntil(15000){Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();test()};compose.waitForIdle();val dismiss=compose.onAllNodesWithContentDescription("Dismiss");if(dismiss.fetchSemanticsNodes().isNotEmpty())dismiss.onFirst().performClick()}
    private fun oldOpen(){compose.onNodeWithText("Hábitos").performClick();compose.onNodeWithText("Explorar hábitos").performScrollTo().performClick();compose.onNodeWithText("Grimório dos Hábitos").assertExists()}
    private fun shot(name:String){compose.waitForIdle();compose.runOnIdle{val dialog=org.robolectric.shadows.ShadowDialog.getLatestDialog();val view=if(dialog?.isShowing==true)dialog.window!!.decorView else compose.activity.window.decorView;val bitmap=android.graphics.Bitmap.createBitmap(view.width,view.height,android.graphics.Bitmap.Config.ARGB_8888);view.draw(android.graphics.Canvas(bitmap));val file=java.io.File("build/reports/screenshots/$name.png");file.parentFile!!.mkdirs();file.outputStream().use{bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}}

    private fun settle(){compose.waitForIdle();val reward=compose.onAllNodesWithText("Continuar a jornada");if(reward.fetchSemanticsNodes().isNotEmpty())reward.onFirst().performClick()}
    private fun listScroll(text:String){compose.onNodeWithTag("exploration_list").performScrollToNode(hasText(text,ignoreCase=true))}
    private fun back(){compose.runOnIdle{compose.activity.onBackPressedDispatcher.onBackPressed()};compose.waitForIdle()}
    private fun gate(){vm.run{val h=Habit(name="Primeira trilha",xp=250);app.repository.habit(h);app.repository.progress(h,today(),1)};wait{app.repository.state.value!!.level>=3};settle()}
    private fun enterForest(capture:Boolean=true){compose.onNodeWithTag("nav:world").performClick();compose.onNodeWithContentDescription("Floresta da Persistência, revelada").performScrollTo().performClick();compose.onNodeWithText("Explorar região").performScrollTo();if(capture)shot("atlas-exploracao-150");compose.onNodeWithText("Explorar região").performClick();compose.onNodeWithTag("exploration_list").performScrollToIndex(0);if(capture)shot("floresta-inicial-150");listScroll("Visitar O Marco dos Retornos")}
    private suspend fun fresh(e:ExpeditionDef):List<Habit>{val hs=(1..e.phases.sumOf{it.target}).map{Habit(name="${e.id} atividade $it",xp=0).also{h->app.repository.habit(h)}};app.repository.startExpedition(e.id,hs.map{it.id});return hs}
    private suspend fun phase(e:ExpeditionDef,hs:List<Habit>){val s=app.repository.state.value!!;val q=s.quests.first{it.id==expeditionQuestId(e,expeditionPhaseIndex(s,e))};hs.filter{s.record(it.id)?.complete!=true}.take(q.target).forEach{app.repository.progress(it,today(),1)};app.repository.advanceExpedition(e.id)}
    @Test fun forestExpeditionsBossAndMemoriesArePlayable(){
        gate();vm.run{(1..5).forEach{app.repository.habit(Habit(id="trail$it",name="Meu ritual $it",xp=0))}};wait{app.repository.state.value!!.habits.size==6}
        enterForest();compose.onNodeWithText("Visitar O Marco dos Retornos").performClick();compose.onNodeWithText("Abrir expedição").performScrollTo().performClick()
        (1..5).forEach{i->compose.onNodeWithTag("exploration_list").performScrollToNode(hasContentDescription("Vincular Meu ritual $i"));compose.onNodeWithContentDescription("Vincular Meu ritual $i").performClick()}
        listScroll("Iniciar expedição");compose.onNodeWithText("Iniciar expedição").performClick();wait{app.repository.state.value!!.quests.any{it.id=="exp:roots:0"}}
        listScroll("Resgatar expedição");shot("expedicao-ativa-150")
        vm.run{app.repository.state.value!!.habits.filter{it.id.startsWith("trail")}.forEach{app.repository.progress(it,today(),1)}};wait{questProgress(app.repository.state.value!!.quests.first{it.id=="exp:roots:0"},app.repository.state.value!!)==5L}
        listScroll("Resgatar expedição");shot("expedicao-marco-150");compose.onNodeWithText("Resgatar expedição").performClick();wait{expeditionComplete(app.repository.state.value!!,forestExpeditions[0])};settle();back();back()
        listScroll("Visitar O Santuário das Raízes");compose.onNodeWithText("Visitar O Santuário das Raízes").performClick();compose.onNodeWithContentDescription("Santuário · abandonado").performScrollTo();shot("santuario-abandonado-150")
        var shrine:List<Habit> = emptyList();vm.run{shrine=fresh(forestExpeditions[1]);phase(forestExpeditions[1],shrine)};wait{sanctuaryStage(app.repository.state.value!!)==1};settle();compose.onNodeWithContentDescription("Santuário · em recuperação").performScrollTo();shot("santuario-parcial-150")
        vm.run{phase(forestExpeditions[1],shrine);phase(forestExpeditions[1],shrine)};wait{sanctuaryStage(app.repository.state.value!!)==2};settle();compose.onNodeWithContentDescription("Santuário · restaurado").performScrollTo();shot("santuario-restaurado-150");back()
        listScroll("Visitar O Coração da Névoa");compose.onNodeWithText("Visitar O Coração da Névoa").performClick();compose.onNodeWithText("Abrir expedição").performScrollTo().performClick()
        var vigil:List<Habit> = emptyList();vm.run{vigil=fresh(forestExpeditions[2])};wait{app.repository.state.value!!.quests.any{it.id=="exp:vigil:0"}}
        listScroll("Fase 1 de 3 · O Chamado");shot("guardiao-fase1-150")
        vm.run{phase(forestExpeditions[2],vigil)};wait{app.repository.state.value!!.quests.any{it.id=="exp:vigil:1"}};settle();listScroll("Fase 2 de 3 · A Resistência");shot("guardiao-fase2-150")
        vm.run{phase(forestExpeditions[2],vigil)};wait{app.repository.state.value!!.quests.any{it.id=="exp:vigil:2"}};settle();listScroll("Fase 3 de 3 · O Juramento");shot("guardiao-fase3-150")
        vm.run{phase(forestExpeditions[2],vigil)};wait{forestProgress(app.repository.state.value!!)==100};settle();listScroll("Registro preservado nas Memórias. Nenhuma nova recompensa por reabrir.");shot("guardiao-concluido-150")
        back();back();compose.onNodeWithTag("exploration_list").performScrollToIndex(0);shot("floresta-final-150")
        compose.onNodeWithTag("nav:character").performClick();compose.onNodeWithText("Salão das Memórias").performScrollTo().performClick();compose.onNode(hasText("Mundo") and !hasTestTag("nav:world")).performScrollTo().performClick();shot("memorias-floresta-150")
    }
    @Test @Config(qualifiers="w320dp-h740dp-mdpi") fun narrowLightRegionAndLockedPlace(){
        gate();vm.theme("light");vm.setting("minimal",true);wait{vm.appearance.value=="light"&&vm.minimal.value}
        compose.runOnIdle{compose.activity.setContent{val density=LocalDensity.current;CompositionLocalProvider(LocalDensity provides Density(density.density,1.3f)){Ascend()}}};compose.waitForIdle()
        enterForest(false);listScroll("Requer a expedição anterior resgatada e nível 3.");shot("floresta-bloqueada-320-150")
        compose.onNodeWithTag("exploration_list").performScrollToNode(hasText("Visitar O Marco dos Retornos"));compose.onNodeWithText("Visitar O Marco dos Retornos").performClick();compose.onNodeWithText("Abrir expedição").performScrollTo().assertIsDisplayed();shot("local-320-claro-150")
        back();back();compose.onNodeWithText("Atlas de Aetheris").assertExists()
    }
}
