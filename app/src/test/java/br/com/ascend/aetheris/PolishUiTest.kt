package br.com.ascend.aetheris

import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
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
class PolishUiTest{
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as AscendApp
    private val vm get()=ViewModelProvider(compose.activity)[GameViewModel::class.java]
    @Before fun setup(){runBlocking{app.preferences.edit{it.clear()}};wait{app.repository.state.value!=null};vm.run{app.repository.create(Player(name="Sigrid"),emptyList());vm.setting("reducedMotion",true)};wait{app.repository.state.value!!.player!=null&&vm.reducedMotion.value}}
    @After fun close(){compose.activityRule.scenario.close();app.database.close()}
    private fun wait(test:()->Boolean){compose.waitUntil(15000){Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();test()};compose.waitForIdle();val dismiss=compose.onAllNodesWithContentDescription("Dismiss");if(dismiss.fetchSemanticsNodes().isNotEmpty())dismiss.onFirst().performClick()}
    private fun open(){compose.onNodeWithText("Hábitos").performClick();compose.onNodeWithText("Explorar hábitos").performScrollTo().performClick();compose.onNodeWithText("Grimório dos Hábitos").assertExists()}
    private fun shot(name:String){compose.waitForIdle();compose.runOnIdle{val dialog=org.robolectric.shadows.ShadowDialog.getLatestDialog();val view=if(dialog?.isShowing==true)dialog.window!!.decorView else compose.activity.window.decorView;val bitmap=android.graphics.Bitmap.createBitmap(view.width,view.height,android.graphics.Bitmap.Config.ARGB_8888);view.draw(android.graphics.Canvas(bitmap));val file=java.io.File("build/reports/screenshots/$name.png");file.parentFile!!.mkdirs();file.outputStream().use{bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}}

    private fun deliver(intent:android.content.Intent){compose.activity.javaClass.getDeclaredMethod("onNewIntent",android.content.Intent::class.java).apply{isAccessible=true}.invoke(compose.activity,intent)}
    @Test fun widgetLinksAreConsumedAndReturningToHeroKeepsTheRoute(){
        compose.onNodeWithTag("nav:character").performClick()
        compose.runOnIdle{deliver(android.content.Intent(compose.activity.intent).putExtra("widget_journey",true))}
        compose.onNodeWithText("ASCEND  /  MINHA JORNADA").assertExists();assertFalse(compose.activity.intent.hasExtra("widget_journey"))
        vm.run{app.repository.habit(Habit(id="focus-polish",name="Estudo sereno",type=2,target=30))};wait{app.repository.state.value!!.habits.isNotEmpty()}
        compose.runOnIdle{deliver(android.content.Intent(compose.activity.intent).putExtra("habit","focus-polish").putExtra("widget_journey",true))}
        compose.onNodeWithText("Registrar · Estudo sereno").assertExists();assertFalse(compose.activity.intent.hasExtra("habit"));assertTrue(app.repository.state.value!!.records.isEmpty())
        compose.onNodeWithContentDescription("Voltar").performClick();compose.onNodeWithTag("nav:character").performClick()
        compose.onNodeWithText("Seu aventureiro").assertExists()
    }
    @Test fun memoryAndWeekUseReadableDatesAndEmptyFilters(){
        vm.run{val h=Habit(name="Pequeno ritual",xp=0);app.repository.habit(h);app.repository.progress(h,today(),1);app.repository.rememberNarrative(nextNarrative(app.repository.state.value!!)!!.id)};wait{app.repository.state.value!!.assets.any{it.kind=="memory"}}
        compose.onNodeWithTag("nav:world").performClick();compose.onNodeWithText("Crônicas").performClick();compose.onNodeWithText("Crônica da Semana").performScrollTo().performClick();compose.onNodeWithText(displayPeriod(weekOf(today()),today()),ignoreCase=true).assertExists();shot("cronica-151")
        compose.runOnIdle{compose.activity.onBackPressedDispatcher.onBackPressed()};compose.waitForIdle();compose.onNodeWithText("Salão das Memórias").performScrollTo().performClick();compose.onNodeWithText("História").performScrollTo().performClick()
        compose.onNodeWithTag("memory_list").performScrollToNode(hasText("Conversa guardada em ${displayDate(today())}"));shot("memorias-151")
        compose.onNodeWithTag("memory_list").performScrollToIndex(0);compose.onNodeWithText("Sagas").performScrollTo().performClick();compose.onNodeWithText("Ainda não há inscrições nesta parte do salão. O lugar permanece reservado.").assertExists();shot("memorias-vazio-151")
    }
    @Test @Config(qualifiers="w320dp-h740dp-mdpi") fun dayTargetsAndHeroAreUsableWithLargeText(){
        compose.runOnIdle{compose.activity.setContent{val density=LocalDensity.current;CompositionLocalProvider(LocalDensity provides Density(density.density,1.3f)){Ascend()}}};compose.waitForIdle()
        compose.onNodeWithTag("nav:habits").performClick();compose.onNodeWithText("+ Criar hábito").performScrollTo().performClick();compose.onNodeWithText("Nome do hábito").performTextReplacement("Ritual confortável")
        compose.onNodeWithContentDescription("Domingo").performScrollTo().assertIsDisplayed();compose.onNodeWithContentDescription("Domingo").assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp);shot("dias-320-151")
        compose.onNodeWithContentDescription("Domingo").performClick();compose.onNodeWithText("Salvar").performClick();wait{app.repository.state.value!!.habits.isNotEmpty()};assertFalse(app.repository.state.value!!.habits.single().days.split(',').contains("7"))
        compose.onNodeWithTag("nav:character").performClick();shot("perfil-320-151");compose.onNodeWithText("Inventário").performClick();shot("inventario-320-151")
    }
}
