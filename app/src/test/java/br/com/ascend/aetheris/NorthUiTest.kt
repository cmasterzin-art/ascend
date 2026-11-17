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
class NorthUiTest{
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as AscendApp
    private val vm get()=ViewModelProvider(compose.activity)[GameViewModel::class.java]
    @Before fun setup(){runBlocking{app.preferences.edit{it.clear()}};wait{app.repository.state.value!=null};vm.run{app.repository.create(Player(name="Sigrid"),emptyList());vm.setting("reducedMotion",true)};wait{app.repository.state.value!!.player!=null&&vm.reducedMotion.value}}
    @After fun close(){compose.activityRule.scenario.close();app.database.close()}
    private fun wait(test:()->Boolean){compose.waitUntil(15000){Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();test()};compose.waitForIdle();val dismiss=compose.onAllNodesWithContentDescription("Dismiss");if(dismiss.fetchSemanticsNodes().isNotEmpty())dismiss.onFirst().performClick()}
    private fun open(){compose.onNodeWithText("Hábitos").performClick();compose.onNodeWithText("Explorar hábitos").performScrollTo().performClick();compose.onNodeWithText("Grimório dos Hábitos").assertExists()}
    private fun shot(name:String){compose.waitForIdle();compose.runOnIdle{val dialog=org.robolectric.shadows.ShadowDialog.getLatestDialog();val view=if(dialog?.isShowing==true)dialog.window!!.decorView else compose.activity.window.decorView;val bitmap=android.graphics.Bitmap.createBitmap(view.width,view.height,android.graphics.Bitmap.Config.ARGB_8888);view.draw(android.graphics.Canvas(bitmap));val file=java.io.File("build/reports/screenshots/$name.png");file.parentFile!!.mkdirs();file.outputStream().use{bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}}
    @Test fun createSagaThroughEditorAndClaimConsolidatedReward(){
        vm.run{app.repository.habit(Habit(id="north-focus",name="Estudo do Norte",type=2,target=60,xp=0))};wait{app.repository.state.value!!.habits.isNotEmpty()}
        compose.onNodeWithText("Missões").performClick();compose.onNodeWithText("Sagas").performScrollTo().performClick()
        compose.onNodeWithText("Iniciar saga").performScrollTo().performClick();compose.onNodeWithText("Criar saga personalizada").performScrollTo().performClick()
        compose.onNodeWithText("Nome da saga").performScrollTo().performTextReplacement("O Juramento de Sigrid")
        compose.onNodeWithText("Meta · conclusões").performScrollTo().performTextReplacement("1")
        compose.onNodeWithText("Estudo do Norte").performScrollTo();compose.onAllNodes(isToggleable()).onFirst().performClick()
        compose.onNodeWithText("XP final").performScrollTo().performTextReplacement("150");shot("saga-editor")
        compose.onNodeWithText("Iniciar jornada").performClick();wait{app.repository.state.value!!.quests.any{it.sagaStart.isNotBlank()}}
        compose.onNodeWithText("O Juramento de Sigrid").performScrollTo();shot("saga-iniciada")
        vm.run{app.repository.progress(app.repository.state.value!!.habits.single(),today(),60)};wait{app.repository.state.value!!.records.any{it.complete}}
        compose.onNodeWithText("Resgatar juramento").performScrollTo();shot("saga-cumprida")
        compose.onNodeWithText("Resgatar juramento").performClick();wait{app.repository.state.value!!.claims.any{it.sagaStart.isNotBlank()}}
        compose.onNodeWithText("O juramento foi cumprido",substring=true,ignoreCase=true).assertExists();shot("recompensas-norte")
        compose.onNodeWithText("Continuar a jornada").performClick();compose.onNodeWithText("Jornada").performClick()
        compose.onNodeWithTag("journey_list").performScrollToNode(hasText("Guardar esta memória"));compose.onNodeWithText("Guardar esta memória").performScrollTo();shot("dialogo-saga")
    }
    @Test @Config(qualifiers="w320dp-h740dp-mdpi") fun sagaWithLargeTextAndReducedMotion(){
        compose.runOnIdle{compose.activity.setContent{val density=LocalDensity.current;CompositionLocalProvider(LocalDensity provides Density(density.density,1.3f)){Ascend()}}};compose.waitForIdle()
        vm.run{val h=Habit(id="north-walk",name="Caminhada",xp=0);app.repository.habit(h);app.repository.saga(Quest(name="Os Caminhos do Norte",habitIds=h.id,target=20))};wait{app.repository.state.value!!.quests.any{it.sagaStart.isNotBlank()}}
        compose.onNodeWithText("Missões").performClick();compose.onNodeWithText("Sagas").performScrollTo().performClick()
        compose.onNodeWithText("Editar saga").performScrollTo().assertIsDisplayed();shot("saga-320-texto130")
    }
}
